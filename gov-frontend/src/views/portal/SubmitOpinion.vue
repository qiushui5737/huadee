<template>
  <div class="submit-opinion">
    <div class="page-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <h1>我要参与</h1>
    </div>

    <el-card v-loading="loading" shadow="hover">
      <div v-if="collection" class="opinion-header">
        <h3 class="opinion-title">
          <span class="title-label">意见征集的主题：</span>
          {{ collection.title }}
        </h3>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 800px; margin: 0 auto;">
        <el-form-item label="标题：" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名：" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码：" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号：" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址：" prop="address">
              <el-input v-model="form.address" placeholder="请输入通信地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="留言内容：" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入内容" maxlength="1000" show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-button type="danger" size="large" @click="submitOpinion" :loading="submitting" style="width: 300px;">
            提交意见
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { collectionPublicDetail, submitCollectionOpinion } from '@/api/interaction'

const route = useRoute()
const router = useRouter()
const collectionId = Number(route.params.id)

const loading = ref(false)
const submitting = ref(false)
const collection = ref<any>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  realName: '',
  phone: '',
  idCard: '',
  address: '',
  content: ''
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }]
}

const goBack = () => {
  router.push({ name: 'collection-detail', params: { id: collectionId } })
}

const loadCollection = async () => {
  if (!collectionId) {
    ElMessage.error('征集ID无效')
    return
  }
  loading.value = true
  try {
    const res: any = await collectionPublicDetail(collectionId)
    if (res.code === 200) {
      collection.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const submitOpinion = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await ElMessageBox.confirm('确认提交您的意见吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }

    submitting.value = true
    try {
      const res: any = await submitCollectionOpinion(collectionId, form)
      if (res.code === 200) {
        ElMessage.success('意见提交成功！')
        formRef.value?.resetFields()
        router.push({ name: 'collection-detail', params: { id: collectionId } })
      } else {
        ElMessage.error(res.message || '提交失败')
      }
    } catch (e) {
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadCollection()
})
</script>

<style scoped lang="scss">
.submit-opinion {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  h1 {
    font-size: 24px;
    color: #1a3a5c;
    margin: 0;
  }
}

.opinion-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;

  .opinion-title {
    font-size: 18px;
    color: #c00;
    font-weight: 600;
    margin: 0;
    padding-left: 12px;
    border-left: 4px solid #c00;

    .title-label {
      color: #c00;
    }
  }
}
</style>
