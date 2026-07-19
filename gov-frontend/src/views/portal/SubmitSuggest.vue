<template>
  <div class="submit-suggest-page">
    <div class="page-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item><a href="/">棣栭〉</a></el-breadcrumb-item>
          <el-breadcrumb-item><a href="/interaction">浜掑姩浜ゆ祦</a></el-breadcrumb-item>
          <el-breadcrumb-item>鎰忚寰侀泦</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">鎰忚寰侀泦</h1>
      </div>
    </div>

    <div class="page-content">
      <el-card shadow="hover" class="suggest-card">
        <template #header>
          <div class="card-header">
            <el-icon><EditPen /></el-icon>
            <span>鎻愪氦鎮ㄧ殑鎰忚寤鸿</span>
          </div>
        </template>

        <el-form ref="formRef" :model="formData" label-width="120px" :rules="formRules">
          <el-form-item label="寤鸿绫诲瀷" prop="type">
            <el-select v-model="formData.type" placeholder="璇烽€夋嫨寤鸿绫诲瀷">
              <el-option label="鏀垮姟鏈嶅姟" value="service" />
              <el-option label="鍩庡競寤鸿" value="city" />
              <el-option label="鏁欒偛鍖荤枟" value="edu_med" />
              <el-option label="浜ら€氬嚭琛? value="traffic" />
              <el-option label="鍏朵粬" value="other" />
            </el-select>
          </el-form-item>

          <el-form-item label="涓婚" prop="title">
            <el-input v-model="formData.title" placeholder="璇疯緭鍏ュ缓璁富棰? />
          </el-form-item>

          <el-form-item label="濮撳悕" prop="name">
            <el-input v-model="formData.name" placeholder="璇疯緭鍏ユ偍鐨勫鍚? />
          </el-form-item>

          <el-form-item label="鑱旂郴鐢佃瘽" prop="phone">
            <el-input v-model="formData.phone" placeholder="璇疯緭鍏ヨ仈绯荤數璇? />
          </el-form-item>

          <el-form-item label="寤鸿鍐呭" prop="content">
            <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="璇疯缁嗘弿杩版偍鐨勫缓璁?.." />
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button @click="handleCancel">
                <el-icon><ArrowLeft /></el-icon> 杩斿洖
              </el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                <el-icon><Check /></el-icon> 鎻愪氦寤鸿
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 杩涘害鏌ヨ寮圭獥 -->
    <el-dialog v-model="queryVisible" title="寤鸿杩涘害鏌ヨ" width="550px">
      <div style="margin-bottom: 16px; display: flex; gap: 10px;">
        <el-input v-model="querySuggestNo" placeholder="璇疯緭鍏ュ缓璁崟鍙? @keyup.enter="handleQuery" />
        <el-button type="primary" :loading="queryLoading" @click="handleQuery">鏌ヨ</el-button>
      </div>
      <div v-if="queryResult" class="query-result">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="寤鸿鍗曞彿">{{ queryResult.suggestNo }}</el-descriptions-item>
          <el-descriptions-item label="寤鸿鏍囬">{{ queryResult.title }}</el-descriptions-item>
          <el-descriptions-item label="寤鸿绫诲瀷">{{ queryResult.type }}</el-descriptions-item>
          <el-descriptions-item label="鐘舵€?>
            <el-tag :type="statusTagType(queryResult.status)">{{ queryResult.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="鎻愪氦鏃堕棿">{{ queryResult.createTime }}</el-descriptions-item>
          <el-descriptions-item label="绛斿鏈熼檺">{{ queryResult.deadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="绛斿鍐呭" v-if="queryResult.replyContent">
            <div style="white-space: pre-wrap;">{{ queryResult.replyContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="绛斿鏃堕棿" v-if="queryResult.replyTime">{{ queryResult.replyTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadUserFile } from 'element-plus'
import { submitSuggestion, suggestionProgress } from '@/api/interaction'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const formData = reactive({
  type: '',
  title: '',
  name: '',
  phone: '',
  content: ''
})

const formRules = {
  type: [
    { required: true, message: '璇烽€夋嫨寤鸿绫诲瀷', trigger: 'change' }
  ],
  title: [
    { required: true, message: '璇疯緭鍏ュ缓璁富棰?, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '璇疯緭鍏ュ鍚?, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '璇疯緭鍏ヨ仈绯荤數璇?, trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '璇疯緭鍏ユ纭殑鎵嬫満鍙?, trigger: 'blur' }
  ],
  content: [
    { required: true, message: '璇疯緭鍏ュ缓璁唴瀹?, trigger: 'blur' }
  ]
}

// 鏌ヨ鍥炲寮圭獥
const queryVisible = ref(false)
const querySuggestNo = ref('')
const queryResult = ref<any>(null)
const queryLoading = ref(false)

const showQuery = () => {
  queryVisible.value = true
  queryResult.value = null
  querySuggestNo.value = ''
}

const handleQuery = async () => {
  if (!querySuggestNo.value.trim()) {
    ElMessage.warning('璇疯緭鍏ュ缓璁崟鍙?)
    return
  }
  queryLoading.value = true
  try {
    const res: any = await suggestionProgress(querySuggestNo.value.trim())
    if (res.code === 200) {
      queryResult.value = res.data
    } else {
      ElMessage.error(res.message || '鏌ヨ澶辫触')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '鏌ヨ澶辫触锛岃妫€鏌ュ崟鍙锋槸鍚︽纭?)
  } finally {
    queryLoading.value = false
  }
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '寰呭彈鐞?: 'warning', '澶勭悊涓?: '', '宸茬瓟澶?: 'success', '宸插姙缁?: 'info' }
  return map[status] || ''
}

// 鎻愪氦寤鸿
const handleSubmit = async () => {
  await formRef.value.validate()

  try {
    await ElMessageBox.confirm(
      '鎻愪氦鍚庝笉鍙慨鏀癸紝璇风‘璁や俊鎭～鍐欐纭€?,
      '纭鎻愪氦',
      {
        confirmButtonText: '纭畾鎻愪氦',
        cancelButtonText: '鍐嶆鏌ヤ竴涓?,
        type: 'warning'
      }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    const res: any = await submitSuggestion({
      title: form.title,
      content: form.content,
      type: form.type,
      realName: form.name,
      idCard: form.idCard,
      email: form.email,
      phone: form.phone,
      address: form.address,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress,
      isPublic: form.isPublic,
      isSecret: form.isSecret
    })

    if (res.code === 200) {
      ElMessageBox.alert(
        `鎮ㄧ殑寤鸿宸叉彁浜ゆ垚鍔燂紒<br/>寤鸿鍗曞彿锛?strong>${res.data.suggestNo}</strong><br/>绛斿鏈熼檺锛?{res.data.deadline || '-'}<br/><br/>璇风墷璁板缓璁崟鍙凤紝鐢ㄤ簬鏌ヨ寤鸿杩涘害銆俙,
        '鎻愪氦鎴愬姛',
        { dangerouslyUseHTMLString: true, confirmButtonText: '鐭ラ亾浜? }
      )
      // 閲嶇疆琛ㄥ崟
      formRef.value.resetFields()
      form.content = ''
    } else {
      ElMessage.error(res.message || '鎻愪氦澶辫触')
    }
  } catch (error) {
    ElMessage.error('鎻愪氦澶辫触锛岃绋嶅悗閲嶈瘯')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/interaction')
}
</script>

<style scoped>
.submit-suggest-page {
  min-height: calc(100vh - 100px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 30px 40px;
  color: #fff;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-top: 16px;
  margin-bottom: 0;
}

.page-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 40px;
}

.suggest-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>
