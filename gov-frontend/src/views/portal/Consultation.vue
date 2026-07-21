<template>
  <div class="consultation-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回上页</el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1>我要咨询</h1>
      <p class="subtitle">接受自然人、法人和其他组织对全省各政府部门办理行政权力及公共服务事项的咨询。</p>
    </div>

    <!-- 说明文字 -->
    <div class="notice-box">
      <p>尊敬的用户：</p>
      <p>&emsp;&emsp;您好！我们会进行您的个人信息进行严格保密。您的个人信息绝不会向外公开，请根据您的实际情况如实填写。</p>
      <p>&emsp;&emsp;如需查询咨询回复情况，您可以注册成为四川政务服务网用户，然后登录四川政务服务网，在用户中心进行查看，也可以通过首页的回复查询输入查询码查询。</p>
      <p class="note">（注：<span class="required-mark">*</span>为必填项）</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：咨询表单 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="24" color="#409eff"><ChatDotRound /></el-icon>
              <span>欢迎发送离线咨询信息!</span>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="130px" class="consult-form">
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="form.realName" placeholder="请输入真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号码" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="电话号码">
                  <el-input v-model="form.telephone" placeholder="请输入您的电话号码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="电子邮箱">
                  <el-input v-model="form.email" placeholder="请输入您的电子邮箱" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="咨询主题" prop="title">
              <el-input v-model="form.title" placeholder="请输入咨询主题，最多不超过50个字" maxlength="50" show-word-limit />
            </el-form-item>

            <el-form-item label="咨询市州" prop="city">
              <el-select v-model="form.city" placeholder="请选择您要咨询的市州" style="width: 100%;">
                <el-option v-for="c in cityOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>

            <el-form-item label="事件发生地" prop="district">
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-select v-model="form.district" placeholder="请选择" clearable style="width: 100%;">
                    <el-option v-for="d in districtOptions" :key="d" :label="d" :value="d" />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-select v-model="form.street" placeholder="请选择" clearable style="width: 100%;">
                    <el-option v-for="s in streetOptions" :key="s" :label="s" :value="s" />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-select v-model="form.town" placeholder="请选择" clearable style="width: 100%;">
                    <el-option v-for="t in townOptions" :key="t" :label="t" :value="t" />
                  </el-select>
                </el-col>
              </el-row>
            </el-form-item>

            <el-form-item label="事件发生具体地址">
              <el-input v-model="form.address" placeholder="请输入地址，最多不超过50个字" maxlength="50" show-word-limit />
            </el-form-item>

            <el-form-item label="附件上传">
              <div class="upload-tip">（支持DOC、DOCX、XLS、XLSX、JPG、TXT、PNG格式文件，图片将直接预览）</div>
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :limit="5"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :on-exceed="handleExceed"
                accept=".doc,.docx,.xls,.xlsx,.jpg,.jpeg,.txt,.png"
                list-type="picture-card"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>

            <el-form-item label="咨询内容" prop="content">
              <div class="content-header">
                <span class="char-count">{{ form.content.length }}/1000</span>
              </div>
              <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入您的咨询内容，1000字以内" maxlength="1000" show-word-limit />
            </el-form-item>

            <el-form-item label="是否愿意公开" prop="isPublic">
              <el-radio-group v-model="form.isPublic">
                <el-radio :value="true">是</el-radio>
                <el-radio :value="false">否</el-radio>
              </el-radio-group>
              <span class="form-tip">（选择公开，将会在四川政务服务网上展示您的咨询信息）</span>
            </el-form-item>

            <el-form-item label="是否保密" prop="isConfidential">
              <el-radio-group v-model="form.isConfidential">
                <el-radio :value="true">是</el-radio>
                <el-radio :value="false">否</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <div class="form-actions">
                <el-button @click="showQueryDialog">回复查询</el-button>
                <el-button type="primary" :loading="submitting" @click="handleSubmit">提交咨询</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：进度查询 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>进度查询</span></template>

          <el-input v-model="queryNo" placeholder="请输入咨询单号" style="margin-bottom: 15px;">
            <template #append>
              <el-button @click="handleQuery">查询</el-button>
            </template>
          </el-input>

          <el-result v-if="queryResult" :icon="queryResult.status === '已答复' || queryResult.status === '已办结' ? 'success' : 'info'" :title="queryResult.consultNo" :sub-title="`状态：${queryResult.status}`">
            <template #extra>
              <el-button type="primary" @click="goToDetail(queryResult.consultNo)">查看详情</el-button>
              <el-descriptions :column="1" border size="small" style="margin-top: 16px;">
                <el-descriptions-item label="咨询主题">{{ queryResult.title }}</el-descriptions-item>
                <el-descriptions-item label="咨询人">{{ queryResult.realName }}</el-descriptions-item>
                <el-descriptions-item label="提交时间">{{ queryResult.createTime }}</el-descriptions-item>
                <el-descriptions-item label="答复期限">{{ queryResult.deadline }}</el-descriptions-item>
                <el-descriptions-item label="答复内容" v-if="queryResult.replyContent">{{ queryResult.replyContent }}</el-descriptions-item>
                <el-descriptions-item label="答复人" v-if="queryResult.replyBy">{{ queryResult.replyBy }}</el-descriptions-item>
                <el-descriptions-item label="答复时间" v-if="queryResult.replyTime">{{ queryResult.replyTime }}</el-descriptions-item>
              </el-descriptions>
            </template>
          </el-result>

          <el-empty v-else description="输入咨询单号查询进度" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 提交成功弹窗 -->
    <el-dialog v-model="successVisible" title="咨询提交成功" width="450px" :close-on-click-modal="false">
      <el-result icon="success" title="您的咨询已提交">
        <template #extra>
          <p>咨询单号：<strong style="color: #409eff; font-size: 18px;">{{ submitResult.consultNo }}</strong></p>
          <p>状态：{{ submitResult.status }}</p>
          <p>答复期限：{{ submitResult.deadline }}</p>
          <p style="color: #999; font-size: 12px; margin-top: 10px;">请保存咨询单号用于查询进度</p>
        </template>
      </el-result>
      <template #footer>
        <el-button type="primary" @click="successVisible = false">确定</el-button>
      </template>
    </el-dialog>

    <!-- 回复查询弹窗 -->
    <el-dialog v-model="queryDialogVisible" title="回复查询" width="450px">
      <el-input v-model="queryDialogNo" placeholder="请输入咨询单号" style="margin-bottom: 16px;">
        <template #append>
          <el-button type="primary" @click="handleDialogQuery">查询</el-button>
        </template>
      </el-input>
      <div v-if="queryDialogResult">
        <el-button type="primary" size="small" @click="goToDetail(queryDialogResult.consultNo)" style="margin-bottom: 12px;">查看详情</el-button>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="咨询单号">{{ queryDialogResult.consultNo }}</el-descriptions-item>
          <el-descriptions-item label="咨询主题">{{ queryDialogResult.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="queryDialogResult.status === '已答复' || queryDialogResult.status === '已办结' ? 'success' : 'warning'" size="small">{{ queryDialogResult.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="答复内容" v-if="queryDialogResult.replyContent">{{ queryDialogResult.replyContent }}</el-descriptions-item>
          <el-descriptions-item label="答复人" v-if="queryDialogResult.replyBy">{{ queryDialogResult.replyBy }}</el-descriptions-item>
          <el-descriptions-item label="答复时间" v-if="queryDialogResult.replyTime">{{ queryDialogResult.replyTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="输入咨询单号后点击查询" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Plus, ArrowLeft } from '@element-plus/icons-vue'
import { submitConsultation, consultationProgress } from '@/api/interaction'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 检查登录状态
const checkLogin = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再操作')
    router.push({ name: 'portal-login', query: { redirect: '/consultation' } })
    return false
  }
  return true
}

// 四川省市州
const cityOptions = [
  '成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市',
  '广元市', '遂宁市', '内江市', '乐山市', '南充市', '眉山市',
  '宜宾市', '广安市', '达州市', '雅安市', '巴中市', '资阳市',
  '阿坝藏族羌族自治州', '甘孜藏族自治州', '凉山彝族自治州'
]

// 区/县选项
const districtOptions = ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '龙泉驿区', '青白江区', '新都区', '温江区', '双流区', '郫都区', '新津区']
const streetOptions = ['春熙路街道', '盐市口街道', '牛市口街道', '书院街街道', '督院街街道']
const townOptions = ['社区一', '社区二', '社区三', '社区四']

const formRef = ref()
const submitting = ref(false)
const form = reactive({
  realName: '',
  phone: '',
  telephone: '',
  email: '',
  title: '',
  city: '',
  district: '',
  street: '',
  town: '',
  address: '',
  content: '',
  isPublic: true,
  isConfidential: false
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  title: [{ required: true, message: '请输入咨询主题', trigger: 'blur' }],
  city: [{ required: true, message: '请选择咨询市州', trigger: 'change' }],
  content: [{ required: true, message: '请输入咨询内容', trigger: 'blur' }]
}

const queryNo = ref('')
const queryResult = ref<any>(null)

const successVisible = ref(false)
const submitResult = ref<any>(null)

// 回复查询弹窗
const queryDialogVisible = ref(false)
const queryDialogNo = ref('')
const queryDialogResult = ref<any>(null)

const handleSubmit = async () => {
  if (!checkLogin()) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const submitData = {
      ...form,
      attachment: JSON.stringify(uploadedFiles.value)
    }
    const res: any = await submitConsultation(submitData)
    if (res.code === 200) {
      submitResult.value = res.data
      successVisible.value = true
      formRef.value?.resetFields()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  } finally {
    submitting.value = false
  }
}

const handleQuery = async () => {
  if (!checkLogin()) return
  if (!queryNo.value.trim()) {
    ElMessage.warning('请输入咨询单号')
    return
  }
  try {
    const res: any = await consultationProgress(queryNo.value.trim())
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

const showQueryDialog = () => {
  queryDialogNo.value = ''
  queryDialogResult.value = null
  queryDialogVisible.value = true
}

const handleDialogQuery = async () => {
  if (!checkLogin()) return
  if (!queryDialogNo.value.trim()) {
    ElMessage.warning('请输入咨询单号')
    return
  }
  try {
    const res: any = await consultationProgress(queryDialogNo.value.trim())
    if (res.code === 200) {
      queryDialogResult.value = res.data
    } else {
      ElMessage.error(res.message || '查询失败')
      queryDialogResult.value = null
    }
  } catch (e) {
    ElMessage.error('查询失败')
    queryDialogResult.value = null
  }
}

const handleExceed = () => {
  ElMessage.warning('最多上传5个附件')
}

// 附件管理
const uploadedFiles = ref<any[]>([])

const handleFileChange = (file: any, fileList: any[]) => {
  const raw = file.raw
  if (!raw) return
  // 图片文件转base64用于预览和提交
  if (raw.type.startsWith('image/')) {
    const reader = new FileReader()
    reader.onload = (e) => {
      file.url = e.target?.result as string
      file.isImage = true
      updateAttachments(fileList)
    }
    reader.readAsDataURL(raw)
  } else {
    file.isImage = false
    updateAttachments(fileList)
  }
}

const handleFileRemove = (_file: any, fileList: any[]) => {
  updateAttachments(fileList)
}

const updateAttachments = (fileList: any[]) => {
  uploadedFiles.value = fileList.map((f: any) => ({
    name: f.name,
    url: f.url || '',
    isImage: !!f.isImage
  }))
}

const goToDetail = (consultNo: string) => {
  router.push({ name: 'consultation-detail', params: { consultNo } })
}
</script>

<style scoped lang="scss">
.back-bar {
  margin-bottom: 12px;
}

.consultation-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
  h1 {
    font-size: 28px;
    color: #1a3a5c;
    margin: 0;
  }
  .subtitle {
    color: #888;
    margin-top: 8px;
    font-size: 14px;
  }
}

.notice-box {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px 20px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #606266;
  line-height: 1.8;

  .note {
    color: #f56c6c;
    margin-top: 8px;
  }
  .required-mark {
    color: #f56c6c;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1a3a5c;
}

.consult-form {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-left: 8px;
  }
  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-bottom: 8px;
  }
  .content-header {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 4px;
    .char-count {
      font-size: 12px;
      color: #909399;
    }
  }
  .form-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    margin-top: 10px;
  }
}
</style>
