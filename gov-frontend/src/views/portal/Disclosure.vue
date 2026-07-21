<template>
  <div class="disclosure-page">
    <div class="page-header">
      <h1>依申请公开</h1>
      <p class="subtitle">政府信息公开申请</p>
      <el-button type="primary" plain style="margin-top: 10px;" @click="$router.push('/disclosure/my-apply')">
        <el-icon><List /></el-icon> 我的申请
      </el-button>
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
            <el-form-item label="申请文件" prop="fileId">
              <div class="file-selector">
                <el-button type="primary" plain size="small" @click="showFileDialog">选择文件</el-button>
                <div v-if="selectedFile" class="selected-file">
                  <el-icon><Document /></el-icon>
                  <span class="file-name">{{ selectedFile.fileName }}</span>
                  <el-tag size="small" type="info">{{ selectedFile.deptName }}</el-tag>
                  <el-button link type="danger" size="small" @click="clearFile">取消</el-button>
                </div>
                <span v-else class="no-file-hint">请选择需要申请公开的保密文件</span>
              </div>
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

          <div v-if="queryResult" class="query-result">
            <div class="result-header">
              <el-icon :size="20" :style="{ color: queryResult.status === '已答复' ? '#67c23a' : queryResult.status === '已驳回' ? '#f56c6c' : '#409eff' }">
                <SuccessFilled v-if="queryResult.status === '已答复'" />
                <CircleCloseFilled v-else-if="queryResult.status === '已驳回'" />
                <InfoFilled v-else />
              </el-icon>
              <span class="result-title">{{ queryResult.applyNo }}</span>
              <el-tag :type="statusTagType(queryResult.status)" size="small">{{ queryResult.status }}</el-tag>
            </div>

            <el-descriptions :column="1" border size="small" style="margin-top: 15px;">
              <el-descriptions-item label="申请人">{{ queryResult.applicant }}</el-descriptions-item>
              <el-descriptions-item label="申请内容" v-if="queryResult.content">{{ queryResult.content }}</el-descriptions-item>
              <el-descriptions-item label="申请文件" v-if="queryResult.fileInfo">
                <el-icon><Document /></el-icon> {{ queryResult.fileInfo.fileName }}
                <el-tag size="small" type="info" style="margin-left: 6px;">{{ queryResult.fileInfo.deptName }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="答复期限">{{ queryResult.deadline }}</el-descriptions-item>
              <el-descriptions-item label="答复内容" v-if="queryResult.replyContent">
                <div style="white-space: pre-wrap;">{{ queryResult.replyContent }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="答复人" v-if="queryResult.replyBy">{{ queryResult.replyBy }}</el-descriptions-item>
            </el-descriptions>

            <!-- 审批流程记录 -->
            <div v-if="auditRecords.length > 0" class="audit-section">
              <div class="section-title">审批流程</div>
              <el-timeline>
                <el-timeline-item v-for="record in auditRecords" :key="record.id" :timestamp="record.operateTime" placement="top" :type="record.action === 'approve' ? 'success' : record.action === 'reject' ? 'danger' : 'primary'">
                  <div>{{ record.nodeName }} - {{ record.operatorName }}</div>
                  <div v-if="record.opinion" class="audit-opinion">{{ record.opinion }}</div>
                </el-timeline-item>
              </el-timeline>
            </div>

            <!-- 已受理/已答复时显示文件区域 -->
            <div v-if="canViewFile && queryResult.fileId" class="file-section">
              <div class="section-title">
                <el-icon><Document /></el-icon> 申请查看的文件
              </div>
              <div v-if="fileAccessInfo" class="file-card">
                <div class="file-info-row">
                  <el-icon><Document /></el-icon>
                  <span class="file-name">{{ fileAccessInfo.fileName }}</span>
                  <el-tag size="small" type="info">{{ fileAccessInfo.deptName }}</el-tag>
                </div>
                <div class="file-meta" v-if="fileAccessInfo.fileSize">
                  文件大小：{{ formatFileSize(fileAccessInfo.fileSize) }}
                  <span v-if="fileAccessInfo.fileType"> | 类型：{{ fileAccessInfo.fileType }}</span>
                </div>
                <div class="file-actions">
                  <el-button v-if="fileAccessInfo.fileUrl" type="primary" size="small" @click="previewFile">
                    <el-icon><View /></el-icon> 在线阅读
                  </el-button>
                  <el-button v-if="fileAccessInfo.fileUrl" type="success" size="small" @click="downloadFile">
                    <el-icon><Download /></el-icon> 下载文件
                  </el-button>
                  <span v-else class="no-file-tip">该文件暂未提供在线查看，请联系相关部门获取</span>
                </div>
              </div>
              <div v-else class="file-loading">
                <el-skeleton :rows="2" animated />
              </div>
            </div>
          </div>

          <el-empty v-else description="输入申请编号查询进度" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 文件选择弹窗 -->
    <el-dialog v-model="fileDialogVisible" title="选择申请公开的文件" width="700px">
      <div class="file-search">
        <el-input v-model="fileKeyword" placeholder="搜索文件名称..." size="small" style="width: 240px;" clearable @clear="loadFileList" @keyup.enter="loadFileList">
          <template #append><el-button @click="loadFileList">搜索</el-button></template>
        </el-input>
        <el-select v-model="fileCategory" placeholder="文件分类" clearable size="small" style="width: 120px;" @change="loadFileList">
          <el-option label="政策法规" value="policy" />
          <el-option label="财政预算" value="finance" />
          <el-option label="规划计划" value="planning" />
          <el-option label="统计数据" value="statistics" />
          <el-option label="其他" value="other" />
        </el-select>
      </div>
      <el-table :data="fileListData" stripe style="width: 100%; margin-top: 15px;" highlight-current-row @current-change="handleFileSelect">
        <el-table-column prop="fileCode" label="文件编号" width="130" />
        <el-table-column prop="fileName" label="文件名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="deptName" label="所属部门" width="120" show-overflow-tooltip />
        <el-table-column prop="fileCategory" label="分类" width="90">
          <template #default="{ row }">{{ categoryLabel(row.fileCategory) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="fileTotal > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="fileTotal" :page-size="filePageSize" v-model:current-page="fileCurrentPage" @current-change="loadFileList" />
      <template #footer>
        <el-button @click="fileDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!tempSelectedFile" @click="confirmFileSelect">确认选择</el-button>
      </template>
    </el-dialog>

    <!-- 提交成功弹窗 -->
    <el-dialog v-model="successVisible" title="申请提交成功" width="400px" :close-on-click-modal="false">
      <el-result icon="success" title="您的申请已提交">
        <template #extra>
          <p>申请编号：<strong style="color: #409eff;">{{ submitResult.applyNo }}</strong></p>
          <p>状态：{{ submitResult.status }}</p>
          <p>答复期限：{{ submitResult.deadline }}</p>
          <p style="color: #999; font-size: 12px;">请保存申请编号用于查询进度</p>
          <div style="margin-top: 15px;">
            <el-button type="primary" plain @click="goToMyApply">查看我的申请</el-button>
          </div>
        </template>
      </el-result>
      <template #footer>
        <el-button type="primary" @click="successVisible = false">确定</el-button>
      </template>
    </el-dialog>

    <!-- 文件在线阅读弹窗 -->
    <el-dialog v-model="previewVisible" title="在线阅读" width="85%" top="5vh" :close-on-click-modal="true" destroy-on-close>
      <div class="preview-container">
        <iframe v-if="previewUrl" :src="previewUrl" class="preview-iframe" frameborder="0"></iframe>
        <el-empty v-else description="文件预览不可用" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, List, Download, SuccessFilled, CircleCloseFilled, InfoFilled, View } from '@element-plus/icons-vue'
import { applyDisclosure, disclosureProgress, disclosureFilePublicList, disclosureAuditRecords, disclosureFileAccess, myDisclosureApplications } from '@/api/interaction'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  applicant: '',
  idCard: '',
  phone: '',
  fileId: null as number | null,
  content: '',
  purpose: '',
  acquireMethod: '电子邮件'
})

const rules = {
  applicant: [{ required: true, message: '请输入申请人姓名', trigger: 'blur' }],
  fileId: [{ required: true, message: '请选择申请公开的文件', trigger: 'change' }],
  content: [{ required: true, message: '请输入申请内容', trigger: 'blur' }],
  acquireMethod: [{ required: true, message: '请选择获取方式', trigger: 'change' }]
}

// 文件选择相关
const fileDialogVisible = ref(false)
const fileKeyword = ref('')
const fileCategory = ref('')
const fileListData = ref<any[]>([])
const fileTotal = ref(0)
const fileCurrentPage = ref(1)
const filePageSize = 10
const selectedFile = ref<any>(null)
const tempSelectedFile = ref<any>(null)

const categoryLabel = (code: string) => {
  const map: Record<string, string> = {
    policy: '政策法规', finance: '财政预算', planning: '规划计划', statistics: '统计数据', other: '其他'
  }
  return map[code] || code || '-'
}

const showFileDialog = () => {
  fileKeyword.value = ''
  fileCategory.value = ''
  fileCurrentPage.value = 1
  tempSelectedFile.value = null
  fileDialogVisible.value = true
  loadFileList()
}

const loadFileList = async () => {
  try {
    const res: any = await disclosureFilePublicList({
      keyword: fileKeyword.value || undefined,
      category: fileCategory.value || undefined,
      page: fileCurrentPage.value,
      size: filePageSize
    })
    if (res.code === 200) {
      fileListData.value = res.data.records
      fileTotal.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载文件列表失败')
  }
}

const handleFileSelect = (row: any) => {
  tempSelectedFile.value = row
}

const confirmFileSelect = () => {
  if (tempSelectedFile.value) {
    selectedFile.value = tempSelectedFile.value
    form.fileId = tempSelectedFile.value.id
    fileDialogVisible.value = false
  }
}

const clearFile = () => {
  selectedFile.value = null
  form.fileId = null
}

const queryNo = ref('')
const queryResult = ref<any>(null)
const auditRecords = ref<any[]>([])
const fileAccessInfo = ref<any>(null)
const previewVisible = ref(false)
const previewUrl = ref('')

// 已受理、已答复状态都可以查看文件
const canViewFile = computed(() => {
  if (!queryResult.value) return false
  return queryResult.value.status === '已受理' || queryResult.value.status === '已答复'
})

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
  selectedFile.value = null
  form.fileId = null
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
      fileAccessInfo.value = null
      // 加载审批流程记录
      await loadAuditRecords()
      // 如果已受理或已答复且有文件，自动加载文件信息
      if (canViewFile.value && queryResult.value.fileId) {
        await loadFileAccessInfo()
      }
    } else {
      ElMessage.error(res.message || '查询失败')
      queryResult.value = null
      auditRecords.value = []
      fileAccessInfo.value = null
    }
  } catch (e) {
    ElMessage.error('查询失败')
    queryResult.value = null
    auditRecords.value = []
    fileAccessInfo.value = null
  }
}

const loadAuditRecords = async () => {
  if (!queryResult.value) return
  try {
    const res: any = await disclosureAuditRecords(queryResult.value.applyNo)
    if (res.code === 200) {
      auditRecords.value = res.data
    }
  } catch (e) {
    console.error('加载审批记录失败', e)
  }
}

const loadFileAccessInfo = async () => {
  if (!queryResult.value || !queryResult.value.fileId) return
  try {
    const res: any = await disclosureFileAccess(queryResult.value.applyNo, queryResult.value.fileId)
    if (res.code === 200) {
      fileAccessInfo.value = res.data
    }
  } catch (e) {
    console.error('加载文件信息失败', e)
  }
}

const downloadFile = () => {
  if (!fileAccessInfo.value?.fileUrl) {
    ElMessage.warning('文件链接不可用')
    return
  }
  // 强制下载
  const link = document.createElement('a')
  link.href = fileAccessInfo.value.fileUrl
  link.download = fileAccessInfo.value.fileName || '下载文件'
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const previewFile = () => {
  if (!fileAccessInfo.value?.fileUrl) {
    ElMessage.warning('文件链接不可用')
    return
  }
  previewUrl.value = fileAccessInfo.value.fileUrl
  previewVisible.value = true
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '已受理': 'warning', '审核中': '', '已答复': 'success', '已驳回': 'danger' }
  return map[status] || ''
}

const formatFileSize = (size: number) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const goToMyApply = () => {
  successVisible.value = false
  router.push('/disclosure/my-apply')
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
.file-selector {
  width: 100%;
  .selected-file {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 8px;
    padding: 8px 12px;
    background: #f0f9eb;
    border: 1px solid #e1f3d8;
    border-radius: 4px;
    .file-name {
      font-weight: 500;
      color: #333;
    }
  }
  .no-file-hint {
    color: #999;
    font-size: 13px;
    margin-left: 10px;
  }
}
.file-search {
  display: flex;
  gap: 10px;
}

// 查询结果样式
.query-result {
  margin-top: 10px;
}
.result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  border: 1px solid #ebeef5;
  .result-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}
.audit-section {
  margin-top: 20px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.audit-opinion {
  color: #666;
  font-size: 12px;
  margin-top: 2px;
}
.file-section {
  margin-top: 20px;
}
.file-card {
  padding: 16px;
  background: linear-gradient(135deg, #f0f9eb 0%, #ffffff 100%);
  border: 1px solid #e1f3d8;
  border-radius: 8px;
  .file-info-row {
    display: flex;
    align-items: center;
    gap: 8px;
    .file-name {
      font-weight: 500;
      color: #303133;
      font-size: 14px;
    }
  }
  .file-meta {
    margin-top: 8px;
    color: #909399;
    font-size: 12px;
  }
  .file-actions {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 12px;
    .no-file-tip {
      color: #909399;
      font-size: 13px;
    }
  }
}
.file-loading {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.preview-container {
  width: 100%;
  height: 75vh;
  .preview-iframe {
    width: 100%;
    height: 100%;
    border-radius: 4px;
  }
}
</style>

