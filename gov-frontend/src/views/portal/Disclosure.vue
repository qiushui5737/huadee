<template>
  <div class="disclosure-page">
    <div class="page-header">
      <h1>依申请公开</h1>
      <p class="subtitle">政府信息公开申请</p>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：申请表单 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span>在线申请</span></template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="申请人" prop="applicant">
              <el-input v-model="form.applicant" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="申请内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请详细描述您需要公开的政府信息..." maxlength="1000" show-word-limit />
            </el-form-item>
            <el-form-item label="用途说明">
              <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请说明申请用途（选填）" />
            </el-form-item>
            <el-form-item label="获取方式" prop="acquireMethod">
              <el-radio-group v-model="form.acquireMethod">
                <el-radio value="邮寄">邮寄</el-radio>
                <el-radio value="电子邮件">电子邮件</el-radio>
                <el-radio value="自行领取">自行领取</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：进度查询 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span>进度查询</span></template>

          <el-input v-model="queryNo" placeholder="请输入申请编号" style="margin-bottom: 15px;">
            <template #append>
              <el-button @click="handleQuery">查询</el-button>
            </template>
          </el-input>

          <el-result v-if="queryResult" :icon="queryResult.status === '已答复' ? 'success' : 'info'" :title="queryResult.applyNo" :sub-title="`状态：${queryResult.status}`">
            <template #extra>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="申请人">{{ queryResult.applicant }}</el-descriptions-item>
                <el-descriptions-item label="答复期限">{{ queryResult.deadline }}</el-descriptions-item>
                <el-descriptions-item label="答复内容" v-if="queryResult.replyContent">{{ queryResult.replyContent }}</el-descriptions-item>
                <el-descriptions-item label="答复人" v-if="queryResult.replyBy">{{ queryResult.replyBy }}</el-descriptions-item>
              </el-descriptions>
            </template>
          </el-result>

          <el-empty v-else description="输入申请编号查询进度" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 提交成功弹窗 -->
    <el-dialog v-model="successVisible" title="申请提交成功" width="400px" :close-on-click-modal="false">
      <el-result icon="success" title="您的申请已提交">
        <template #extra>
          <p>申请编号：<strong style="color: #409eff;">{{ submitResult.applyNo }}</strong></p>
          <p>状态：{{ submitResult.status }}</p>
          <p>答复期限：{{ submitResult.deadline }}</p>
          <p style="color: #999; font-size: 12px;">请保存申请编号用于查询进度</p>
        </template>
      </el-result>
      <template #footer>
        <el-button type="primary" @click="successVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { applyDisclosure, disclosureProgress } from '@/api/interaction'

const formRef = ref()
const submitting = ref(false)
const form = reactive({
  applicant: '',
  idCard: '',
  phone: '',
  content: '',
  purpose: '',
  acquireMethod: '电子邮件'
})

const rules = {
  applicant: [{ required: true, message: '请输入申请人姓名', trigger: 'blur' }],
  content: [{ required: true, message: '请输入申请内容', trigger: 'blur' }],
  acquireMethod: [{ required: true, message: '请选择获取方式', trigger: 'change' }]
}

const queryNo = ref('')
const queryResult = ref<any>(null)

const successVisible = ref(false)
const submitResult = ref<any>(null)

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const res: any = await applyDisclosure(form)
    if (res.code === 200) {
      submitResult.value = res.data
      successVisible.value = true
      resetForm()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleQuery = async () => {
  if (!queryNo.value.trim()) {
    ElMessage.warning('请输入申请编号')
    return
  }
  try {
    const res: any = await disclosureProgress(queryNo.value.trim())
    if (res.code === 200) {
      queryResult.value = res.data
    } else {
      ElMessage.error(res.message || '查询失败')
      queryResult.value = null
    }
  } catch (e) {
    ElMessage.error('查询失败')
    queryResult.value = null
  }
}
</script>

<style scoped lang="scss">
.disclosure-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  text-align: center;
  margin-bottom: 30px;
  h1 { font-size: 28px; color: #1a3a5c; margin: 0; }
  .subtitle { color: #888; margin-top: 8px; font-size: 14px; }
}
</style>
<template>
  <div style="padding:20px;">
    <el-card>
      <h1>Disclosure - 依申请公开 - 在线申请与进度查询</h1>
      <el-empty description="页面骨架 - 待实现" />
    </el-card>
  </div>
</template>
<script setup lang="ts"></script>
