<template>
  <div class="disclosure-file-mgmt">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>保密文件管理</span>
          <div class="header-actions">
            <el-input v-model="filterKeyword" placeholder="搜索文件名称/编号..." size="small" style="width: 200px; margin-right: 10px;" clearable @clear="loadList" @keyup.enter="loadList">
              <template #append>
                <el-button @click="loadList">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="filterCategory" placeholder="文件分类" clearable size="small" style="width: 120px; margin-right: 10px;" @change="loadList">
              <el-option label="政策法规" value="policy" />
              <el-option label="财政预算" value="finance" />
              <el-option label="规划计划" value="planning" />
              <el-option label="统计数据" value="statistics" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px;" @change="loadList">
              <el-option label="保密" value="secret" />
              <el-option label="已批准公开" value="approved" />
              <el-option label="已公开" value="public" />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px;">
        <el-col :span="6" v-for="item in statusCards" :key="item.label">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num" :class="item.color">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 操作按钮 -->
      <div style="margin-bottom: 15px;">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 新增文件
        </el-button>
      </div>

      <!-- 文件列表 -->
      <el-table :data="listData" stripe style="width: 100%">
        <el-table-column prop="fileCode" label="文件编号" width="140" />
        <el-table-column prop="fileName" label="文件名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <a v-if="row.fileUrl" :href="row.fileUrl" download class="file-link">{{ row.fileName }}</a>
            <span v-else>{{ row.fileName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileCategory" label="分类" width="100">
          <template #default="{ row }">
            {{ categoryLabel(row.fileCategory) }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="所属部门" width="120" show-overflow-tooltip />
        <el-table-column prop="confidentialityLevel" label="保密级别" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.confidentialityLevel)" size="small">
              {{ levelLabel(row.confidentialityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button v-if="row.status !== 'public'" link type="success" size="small" @click="handlePublish(row)">公开</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadList" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑文件' : '新增文件'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="文件名称" prop="fileName">
          <el-input v-model="form.fileName" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="上传文件" prop="fileUrl">
          <el-upload
            v-model:file-list="fileList"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="1"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.gif,.txt"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 pdf、doc、docx、xls、xlsx、jpg、png 等格式，文件大小不超过50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="文件分类" prop="fileCategory">
          <el-select v-model="form.fileCategory" placeholder="请选择文件分类" style="width: 100%;">
            <el-option label="政策法规" value="policy" />
            <el-option label="财政预算" value="finance" />
            <el-option label="规划计划" value="planning" />
            <el-option label="统计数据" value="statistics" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入所属部门" />
        </el-form-item>
        <el-form-item label="保密级别" prop="confidentialityLevel">
          <el-select v-model="form.confidentialityLevel" placeholder="请选择保密级别" style="width: 100%;">
            <el-option label="秘密" value="secret" />
            <el-option label="机密" value="confidential" />
            <el-option label="绝密" value="top_secret" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入文件描述（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="文件详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="文件编号">{{ detailData.fileCode }}</el-descriptions-item>
        <el-descriptions-item label="文件名称">{{ detailData.fileName }}</el-descriptions-item>
        <el-descriptions-item label="文件分类">{{ categoryLabel(detailData.fileCategory) }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detailData.deptName }}</el-descriptions-item>
        <el-descriptions-item label="保密级别">
          <el-tag :type="levelTagType(detailData.confidentialityLevel)" size="small">
            {{ levelLabel(detailData.confidentialityLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(detailData.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">{{ detailData.fileType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="文件描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文件下载" :span="2">
          <a v-if="detailData.fileUrl" :href="detailData.fileUrl" download class="file-link">点击下载</a>
          <span v-else>未上传文件</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import {
  disclosureFileList,
  disclosureFileDetail,
  createDisclosureFile,
  updateDisclosureFile,
  deleteDisclosureFile,
  publishDisclosureFile,
  disclosureFileStats,
  uploadFile,
  deleteUploadedFile
} from '@/api/interaction'

const listData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')

const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref()
const detailData = ref<any>(null)

const form = reactive({
  fileName: '',
  fileUrl: '',
  fileCategory: '',
  deptName: '',
  confidentialityLevel: 'secret',
  description: ''
})

const fileList = ref<UploadFile[]>([])
const uploading = ref(false)

const rules = {
  fileName: [{ required: true, message: '请输入文件名称', trigger: 'blur' }],
  fileCategory: [{ required: true, message: '请选择文件分类', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入所属部门', trigger: 'blur' }],
  confidentialityLevel: [{ required: true, message: '请选择保密级别', trigger: 'change' }]
}

const stats = ref<any>({})

const statusCards = computed(() => [
  { label: '文件总数', value: stats.value.total || 0, color: 'text-blue' },
  { label: '保密文件', value: stats.value.secretCount || 0, color: 'text-red' },
  { label: '已批准公开', value: stats.value.approvedCount || 0, color: 'text-orange' },
  { label: '已公开', value: stats.value.publicCount || 0, color: 'text-green' }
])

const categoryLabel = (code: string) => {
  const map: Record<string, string> = {
    policy: '政策法规',
    finance: '财政预算',
    planning: '规划计划',
    statistics: '统计数据',
    other: '其他'
  }
  return map[code] || code || '-'
}

const levelLabel = (level: string) => {
  const map: Record<string, string> = {
    secret: '秘密',
    confidential: '机密',
    top_secret: '绝密'
  }
  return map[level] || level || '-'
}

const levelTagType = (level: string) => {
  const map: Record<string, string> = {
    secret: 'warning',
    confidential: 'danger',
    top_secret: 'danger'
  }
  return map[level] || ''
}

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    secret: '保密',
    approved: '已批准',
    public: '已公开'
  }
  return map[status] || status || '-'
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    secret: 'danger',
    approved: 'warning',
    public: 'success'
  }
  return map[status] || ''
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const formatFileSize = (size: number) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const loadList = async () => {
  try {
    const res: any = await disclosureFileList({
      keyword: filterKeyword.value || undefined,
      category: filterCategory.value || undefined,
      status: filterStatus.value || undefined,
      page: currentPage.value,
      size: pageSize
    })
    if (res.code === 200) {
      listData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载列表失败')
  }
}

const loadStats = async () => {
  try {
    const res: any = await disclosureFileStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  Object.assign(form, {
    fileName: '',
    fileUrl: '',
    fileCategory: '',
    deptName: '',
    confidentialityLevel: 'secret',
    description: ''
  })
  fileList.value = []
  dialogVisible.value = true
}

const showEditDialog = (row: any) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    fileName: row.fileName,
    fileUrl: row.fileUrl || '',
    fileCategory: row.fileCategory,
    deptName: row.deptName,
    confidentialityLevel: row.confidentialityLevel,
    description: row.description || ''
  })
  // 如果有文件URL，显示已上传文件
  if (row.fileUrl) {
    fileList.value = [{
      name: row.fileName,
      url: row.fileUrl,
      status: 'success'
    }]
  } else {
    fileList.value = []
  }
  dialogVisible.value = true
}

const showDetail = async (row: any) => {
  try {
    const res: any = await disclosureFileDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    // 如果有新文件，先上传
    if (fileList.value.length > 0 && fileList.value[0].raw) {
      uploading.value = true
      try {
        const res: any = await uploadFile(fileList.value[0].raw)
        if (res.code === 200) {
          form.fileUrl = res.data.fileUrl
          // 自动使用文件名作为文件名称（如果未填写）
          if (!form.fileName) {
            form.fileName = res.data.fileName
          }
        } else {
          ElMessage.error(res.message || '文件上传失败')
          submitting.value = false
          return
        }
      } catch (e) {
        ElMessage.error('文件上传失败')
        submitting.value = false
        return
      } finally {
        uploading.value = false
      }
    }

    if (isEdit.value && editId.value) {
      const res: any = await updateDisclosureFile(editId.value, form)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadList()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res: any = await createDisclosureFile(form)
      if (res.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadList()
        loadStats()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleFileChange = (file: UploadFile) => {
  // 检查文件大小
  if (file.size && file.size > 50 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过50MB')
    fileList.value = []
    return false
  }
  // 自动使用文件名作为文件名称（如果未填写）
  if (!form.fileName && file.name) {
    form.fileName = file.name.replace(/\.[^/.]+$/, '')
  }
  return true
}

const handleFileRemove = () => {
  form.fileUrl = ''
}

const handlePublish = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认将文件「${row.fileName}」设为公开？`, '提示', {
      type: 'warning'
    })
    const res: any = await publishDisclosureFile(row.id, '管理员')
    if (res.code === 200) {
      ElMessage.success('文件已公开')
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除文件「${row.fileName}」？`, '提示', {
      type: 'warning'
    })
    const res: any = await deleteDisclosureFile(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadList()
  loadStats()
})
</script>

<style scoped lang="scss">
.disclosure-file-mgmt {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-actions {
    display: flex;
    align-items: center;
  }
}

.stat-card {
  text-align: center;
  padding: 10px;

  .stat-num {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 8px;

    &.text-blue { color: #409eff; }
    &.text-red { color: #f56c6c; }
    &.text-orange { color: #e6a23c; }
    &.text-green { color: #67c23a; }
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
  }
}

.file-link {
  color: #409eff;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}
</style>
