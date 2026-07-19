<template>
  <div class="collection-mgmt">
    <!-- ===== 列表视图 ===== -->
    <template v-if="!detailMode">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>意见征集管理</span>
            <div class="header-actions">
              <el-input v-model="filterKeyword" placeholder="搜索征集主题..." style="width: 220px; margin-right: 10px;" clearable @clear="loadList" @keyup.enter="loadList">
                <template #append>
                  <el-button @click="loadList">搜索</el-button>
                </template>
              </el-input>
              <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 130px;" @change="loadList">
                <el-option label="未开始" value="未开始" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已结束" value="已结束" />
              </el-select>
              <el-button type="primary" @click="showCreate">新建征集</el-button>
            </div>
          </div>
        </template>

        <el-table :data="listData" stripe style="width: 100%">
          <el-table-column prop="title" label="征集主题" min-width="300" show-overflow-tooltip />
          <el-table-column prop="deptName" label="征集单位" width="140" />
          <el-table-column label="征集时间" width="220">
            <template #default="{ row }">
              {{ row.startDate }} 至 {{ row.endDate }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" @click="enterDetail(row)">
                  <el-icon><View /></el-icon>查看详情
                </el-button>
                <el-button v-if="row.status !== '进行中'" type="success" @click="handlePublish(row)">
                  <el-icon><Promotion /></el-icon>发布
                </el-button>
                <el-button v-if="row.status === '进行中'" type="warning" @click="handleFinish(row)">
                  <el-icon><CircleClose /></el-icon>停止
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination v-if="total > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadList" />
      </el-card>
    </template>

    <!-- ===== 详情视图 ===== -->
    <template v-else>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 12px;">
              <el-button @click="exitDetail">
                <el-icon><ArrowLeft /></el-icon>返回列表
              </el-button>
              <span style="font-size: 18px; font-weight: 600; color: #1a3a5c;">征集详情</span>
              <el-tag :type="statusTagType(detailData.status)" size="large">{{ detailData.status }}</el-tag>
            </div>
          </div>
        </template>

        <!-- 征集基本信息 -->
        <el-descriptions :column="2" border size="large" class="detail-info">
          <el-descriptions-item label="征集主题" :span="2">
            <span style="font-size: 16px; font-weight: 600;">{{ detailData.title }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="征集单位">{{ detailData.deptName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="征集时间">{{ detailData.startDate }} 至 {{ detailData.endDate }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detailData.contactName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="征集说明" :span="2">
            <div style="white-space: pre-wrap; line-height: 1.8;">{{ detailData.description || '暂无说明' }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="detailData.attachmentName" label="附件" :span="2">
            {{ detailData.attachmentName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detailData.feedback" label="结果反馈" :span="2">
            <div style="white-space: pre-wrap; line-height: 1.8; color: #409eff;">{{ detailData.feedback }}</div>
            <div style="color: #909399; font-size: 13px; margin-top: 6px;" v-if="detailData.feedbackTime">反馈时间：{{ detailData.feedbackTime }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <el-button type="warning" @click="showEditFromDetail">
            <el-icon><Edit /></el-icon>编辑征集
          </el-button>
          <el-button type="info" @click="showFeedbackFromDetail">
            <el-icon><Document /></el-icon>结果反馈
          </el-button>
          <el-button v-if="detailData.status !== '进行中'" type="success" @click="handlePublish(detailData)">
            <el-icon><Promotion /></el-icon>发布
          </el-button>
          <el-button v-if="detailData.status === '进行中'" type="warning" @click="handleFinish(detailData)">
            <el-icon><CircleClose /></el-icon>停止
          </el-button>
        </div>
      </el-card>

      <!-- 意见管理区域 -->
      <el-card shadow="hover" style="margin-top: 16px;">
        <template #header>
          <div class="card-header">
            <span style="font-size: 16px; font-weight: 600;">意见管理</span>
          </div>
        </template>

        <!-- 统计卡片 -->
        <el-row :gutter="20" style="margin-bottom: 18px;">
          <el-col :span="6">
            <div class="stat-box">
              <div class="stat-label">总意见数</div>
              <div class="stat-value">{{ opinionStats.total || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-box pending">
              <div class="stat-label">待处理</div>
              <div class="stat-value">{{ opinionStats.pending || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="stat-box adopted">
              <div class="stat-label">已采纳</div>
              <div class="stat-value">{{ opinionStats.adopted || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="stat-box partial">
              <div class="stat-label">部分采纳</div>
              <div class="stat-value">{{ opinionStats.partiallyAdopted || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="5">
            <div class="stat-box rejected">
              <div class="stat-label">不采纳</div>
              <div class="stat-value">{{ opinionStats.rejected || 0 }}</div>
            </div>
          </el-col>
        </el-row>

        <!-- 意见列表 -->
        <el-table :data="opinionList" stripe style="width: 100%;" :row-class-name="opinionRowClass">
          <el-table-column prop="title" label="意见标题" min-width="160" show-overflow-tooltip />
          <el-table-column prop="realName" label="姓名" width="90" />
          <el-table-column prop="phone" label="电话" width="120" />
          <el-table-column prop="content" label="留言内容" min-width="220" show-overflow-tooltip />
          <el-table-column prop="status" label="处理状态" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.status === '待处理'" type="warning">待处理</el-tag>
              <el-tag v-else-if="row.status === '已采纳'" type="success" effect="dark">已采纳</el-tag>
              <el-tag v-else-if="row.status === '部分采纳'" type="warning" effect="dark">部分采纳</el-tag>
              <el-tag v-else-if="row.status === '不采纳'" type="danger" effect="dark">不采纳</el-tag>
              <el-tag v-else>{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="170" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === '待处理'">
                <el-button type="success" size="small" @click="showHandleOpinion(row, '已采纳')">
                  <el-icon><Select /></el-icon>采纳
                </el-button>
                <el-button type="warning" size="small" @click="showHandleOpinion(row, '部分采纳')">
                  <el-icon><Edit /></el-icon>部分采纳
                </el-button>
                <el-button type="danger" size="small" @click="showHandleOpinion(row, '不采纳')">
                  <el-icon><CloseBold /></el-icon>不采纳
                </el-button>
              </template>
              <el-button v-else type="info" size="small" @click="showReplyDetail(row)">
                <el-icon><View /></el-icon>查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination v-if="opinionTotal > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="opinionTotal" :page-size="opinionPageSize" v-model:current-page="opinionCurrentPage" @current-change="loadOpinions" />
      </el-card>
    </template>

    <!-- ===== 弹窗区 ===== -->

    <!-- 新建/编辑征集弹窗 -->
    <el-dialog v-model="editVisible" :title="isEdit ? '编辑征集' : '新建征集'" width="750px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="110px">
        <el-form-item label="征集主题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入征集主题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="征集说明" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="5" placeholder="请输入征集说明" />
        </el-form-item>
        <el-form-item label="征集单位" prop="deptName">
          <el-input v-model="editForm.deptName" placeholder="请输入征集单位" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="editForm.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="editForm.startDate" type="date" placeholder="选择开始日期" style="width: 100%;" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="editForm.endDate" type="date" placeholder="选择结束日期" style="width: 100%;" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="附件名称">
          <el-input v-model="editForm.attachmentName" placeholder="请输入附件名称（选填）" />
        </el-form-item>
        <el-form-item label="附件路径">
          <el-input v-model="editForm.attachmentUrl" placeholder="请输入附件路径（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 处理意见弹窗 -->
    <el-dialog v-model="handleVisible" :title="handleDialogTitle" width="650px">
      <div v-if="handleItem" class="handle-opinion">
        <el-descriptions :column="1" border style="margin-bottom: 18px;">
          <el-descriptions-item label="意见标题">{{ handleItem.title }}</el-descriptions-item>
          <el-descriptions-item label="留言人">{{ handleItem.realName }}（{{ handleItem.phone }}）</el-descriptions-item>
          <el-descriptions-item label="留言内容">
            <div class="preview">{{ handleItem.content }}</div>
          </el-descriptions-item>
        </el-descriptions>
        <el-form label-width="100px">
          <el-form-item label="处理结果" required>
            <el-radio-group v-model="handleStatus">
              <el-radio value="已采纳">已采纳</el-radio>
              <el-radio value="部分采纳">部分采纳</el-radio>
              <el-radio value="不采纳">不采纳</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="反馈理由" required>
            <el-input v-model="handleReason" type="textarea" :rows="5" placeholder="请输入反馈理由..." />
          </el-form-item>
          <el-form-item label="操作人">
            <el-input v-model="handleOperator" placeholder="请输入操作人姓名" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button :type="handleButtonType" :loading="handling" @click="submitHandleOpinion">
          {{ handleButtonText }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 结果反馈弹窗 -->
    <el-dialog v-model="feedbackDlgVisible" title="结果反馈" width="650px">
      <div style="margin-bottom: 15px;">
        <h4 style="color: #1a3a5c; margin: 0;">{{ detailData.title }}</h4>
      </div>
      <el-form label-width="90px">
        <el-form-item label="反馈内容" required>
          <el-input v-model="feedbackContent" type="textarea" :rows="6" placeholder="请输入征集结果反馈内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDlgVisible = false">取消</el-button>
        <el-button type="primary" :loading="feedbackSaving" @click="handleSaveFeedback">保存反馈</el-button>
      </template>
    </el-dialog>

    <!-- 查看处理详情弹窗 -->
    <el-dialog v-model="replyDetailVisible" title="处理详情" width="650px">
      <el-descriptions :column="1" border v-if="replyDetailItem">
        <el-descriptions-item label="意见标题">{{ replyDetailItem.title }}</el-descriptions-item>
        <el-descriptions-item label="留言人">{{ replyDetailItem.realName }}（{{ replyDetailItem.phone }}）</el-descriptions-item>
        <el-descriptions-item label="留言内容">
          <div class="preview">{{ replyDetailItem.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="处理结果">
          <el-tag :type="replyDetailItem.status === '已采纳' ? 'success' : 'danger'" effect="dark">{{ replyDetailItem.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="反馈理由">{{ replyDetailItem.replyContent }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ replyDetailItem.replyBy }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ replyDetailItem.replyTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  View, Promotion, CircleClose, ArrowLeft, Edit, Document,
  Select, CloseBold
} from '@element-plus/icons-vue'
import {
  collectionList, createCollection, updateCollection,
  publishCollection, finishCollection, addCollectionFeedback,
  collectionOpinions, replyCollectionOpinion, collectionOpinionStats,
  collectionPublicDetail
} from '@/api/interaction'

// ===== 列表相关 =====
const listData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterStatus = ref('')
const filterKeyword = ref('')

// ===== 详情模式 =====
const detailMode = ref(false)
const detailData = ref<any>({})

// ===== 编辑弹窗 =====
const editVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: null as number | null,
  title: '', description: '', deptName: '', contactName: '', contactPhone: '',
  startDate: '', endDate: '', attachmentName: '', attachmentUrl: ''
})
const editRules: FormRules = {
  title: [{ required: true, message: '请输入征集主题', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

// ===== 意见管理 =====
const opinionList = ref<any[]>([])
const opinionTotal = ref(0)
const opinionCurrentPage = ref(1)
const opinionPageSize = 10
const opinionStats = ref<any>({})

const handleVisible = ref(false)
const handleItem = ref<any>(null)
const handleStatus = ref('已采纳')
const handleReason = ref('')
const handleOperator = ref('系统管理员')
const handling = ref(false)

const replyDetailVisible = ref(false)
const replyDetailItem = ref<any>(null)

// ===== 结果反馈 =====
const feedbackDlgVisible = ref(false)
const feedbackContent = ref('')
const feedbackSaving = ref(false)

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '进行中': 'success', '已结束': 'info', '未开始': 'warning' }
  return map[status] || ''
}

const opinionRowClass = ({ row }: { row: any }) => {
  if (row.status === '已采纳') return 'row-adopted'
  if (row.status === '部分采纳') return 'row-partial'
  if (row.status === '不采纳') return 'row-rejected'
  return ''
}

// ===== 列表操作 =====
const loadList = async () => {
  try {
    const params: any = { page: currentPage.value, size: pageSize }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    const res: any = await collectionList(params)
    if (res.code === 200) {
      listData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) { /* ignore */ }
}

const showCreate = () => {
  isEdit.value = false
  Object.assign(editForm, {
    id: null, title: '', description: '', deptName: '',
    contactName: '', contactPhone: '', startDate: '', endDate: '',
    attachmentName: '', attachmentUrl: ''
  })
  editVisible.value = true
}

const handleSave = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (isEdit.value && editForm.id) {
        const res: any = await updateCollection(editForm.id, editForm)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          editVisible.value = false
          // 如果在详情模式编辑，刷新详情
          if (detailMode.value) enterDetail(detailData.value)
          else loadList()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } else {
        const res: any = await createCollection(editForm)
        if (res.code === 200) {
          ElMessage.success('创建成功')
          editVisible.value = false
          loadList()
        } else {
          ElMessage.error(res.message || '创建失败')
        }
      }
    } catch (e) {
      ElMessage.error('网络异常')
    } finally {
      saving.value = false
    }
  })
}

const handlePublish = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认发布征集「${row.title}」吗？`, '提示', { type: 'warning' })
    const res: any = await publishCollection(row.id)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      if (detailMode.value) enterDetail(detailData.value)
      else loadList()
    }
  } catch (e) { /* ignore */ }
}

const handleFinish = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认停止征集「${row.title}」吗？`, '提示', { type: 'warning' })
    const res: any = await finishCollection(row.id)
    if (res.code === 200) {
      ElMessage.success('已停止')
      if (detailMode.value) enterDetail(detailData.value)
      else loadList()
    }
  } catch (e) { /* ignore */ }
}

// ===== 详情视图 =====
const enterDetail = async (row: any) => {
  detailMode.value = true
  // 加载详情数据
  try {
    const res: any = await collectionPublicDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
    } else {
      detailData.value = row
    }
  } catch (e) {
    detailData.value = row
  }
  // 加载意见
  opinionCurrentPage.value = 1
  await loadOpinionStats(row.id)
  await loadOpinions()
}

const exitDetail = () => {
  detailMode.value = false
  detailData.value = {}
  loadList()
}

const showEditFromDetail = () => {
  isEdit.value = true
  const d = detailData.value
  Object.assign(editForm, {
    id: d.id, title: d.title, description: d.description,
    deptName: d.deptName, contactName: d.contactName, contactPhone: d.contactPhone,
    startDate: d.startDate, endDate: d.endDate,
    attachmentName: d.attachmentName, attachmentUrl: d.attachmentUrl
  })
  editVisible.value = true
}

const showFeedbackFromDetail = () => {
  feedbackContent.value = detailData.value.feedback || ''
  feedbackDlgVisible.value = true
}

// ===== 意见管理 =====
const loadOpinionStats = async (id: number) => {
  try {
    const res: any = await collectionOpinionStats(id)
    if (res.code === 200) opinionStats.value = res.data
  } catch (e) { /* ignore */ }
}

const loadOpinions = async () => {
  if (!detailData.value?.id) return
  try {
    const res: any = await collectionOpinions(detailData.value.id, {
      page: opinionCurrentPage.value, size: opinionPageSize
    })
    if (res.code === 200) {
      opinionList.value = res.data.records
      opinionTotal.value = res.data.total
    }
  } catch (e) { /* ignore */ }
}

const showHandleOpinion = (row: any, status: string) => {
  handleItem.value = row
  handleStatus.value = status
  handleReason.value = ''
  handleOperator.value = '系统管理员'
  handleVisible.value = true
}

const handleDialogTitle = computed(() => {
  const map: Record<string, string> = { '已采纳': '采纳意见', '部分采纳': '部分采纳意见', '不采纳': '不采纳意见' }
  return map[handleStatus.value] || '处理意见'
})

const handleButtonType = computed(() => {
  const map: Record<string, string> = { '已采纳': 'success', '部分采纳': 'warning', '不采纳': 'danger' }
  return map[handleStatus.value] || 'primary'
})

const handleButtonText = computed(() => {
  const map: Record<string, string> = { '已采纳': '确认采纳', '部分采纳': '确认部分采纳', '不采纳': '确认不采纳' }
  return map[handleStatus.value] || '确认'
})

const submitHandleOpinion = async () => {
  if (!handleReason.value.trim()) {
    ElMessage.warning('请输入反馈理由')
    return
  }
  handling.value = true
  try {
    const res: any = await replyCollectionOpinion(handleItem.value.id, {
      replyContent: handleReason.value,
      operator: handleOperator.value,
      status: handleStatus.value
    })
    if (res.code === 200) {
      const msgMap: Record<string, string> = { '已采纳': '已采纳该意见', '部分采纳': '已标记为部分采纳', '不采纳': '已标记为不采纳' }
      ElMessage.success(msgMap[handleStatus.value] || '处理成功')
      handleVisible.value = false
      loadOpinions()
      loadOpinionStats(detailData.value.id)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    handling.value = false
  }
}

const showReplyDetail = (row: any) => {
  replyDetailItem.value = row
  replyDetailVisible.value = true
}

// ===== 结果反馈 =====
const handleSaveFeedback = async () => {
  if (!feedbackContent.value.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  feedbackSaving.value = true
  try {
    const res: any = await addCollectionFeedback(detailData.value.id, { feedback: feedbackContent.value })
    if (res.code === 200) {
      ElMessage.success('反馈保存成功')
      feedbackDlgVisible.value = false
      // 刷新详情
      enterDetail(detailData.value)
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    feedbackSaving.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.collection-mgmt {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.detail-info {
  :deep(.el-descriptions__label) {
    width: 110px;
    font-weight: 600;
    background: #f5f7fa;
  }
}

.detail-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
}

.stat-box {
  padding: 16px 12px;
  border-radius: 8px;
  text-align: center;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;

  .stat-label {
    font-size: 13px;
    color: #606266;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 26px;
    font-weight: 700;
    color: #67c23a;
  }

  &.pending {
    background: #fdf6ec;
    border-color: #faecd8;
    .stat-value { color: #e6a23c; }
  }

  &.adopted {
    background: #f0f9eb;
    border-color: #e1f3d8;
    .stat-value { color: #67c23a; }
  }

  &.partial {
    background: #fdf6ec;
    border-color: #faecd8;
    .stat-value { color: #e6a23c; }
  }

  &.rejected {
    background: #fef0f0;
    border-color: #fde2e2;
    .stat-value { color: #f56c6c; }
  }
}

.preview {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.6;
  max-height: 120px;
  overflow-y: auto;
  white-space: pre-wrap;
}

.handle-opinion {
  :deep(.el-descriptions__label) {
    width: 100px;
    font-weight: 600;
  }
}

:deep(.row-adopted) {
  background-color: #f0f9eb !important;
}

:deep(.row-partial) {
  background-color: #fdf6ec !important;
}

:deep(.row-rejected) {
  background-color: #fef0f0 !important;
}
</style>
