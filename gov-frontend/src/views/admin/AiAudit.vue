<template>
  <div class="ai-admin-page">
    <header class="page-head">
      <div>
        <h2>AI 服务管理</h2>
        <p>问答审计、敏感词策略与搜索索引统一管理</p>
      </div>
      <el-button :icon="Refresh" @click="refreshCurrent">刷新</el-button>
    </header>

    <el-tabs v-model="activeTab" @tab-change="refreshCurrent">
      <el-tab-pane label="问答审计" name="audit">
        <div class="stats-strip">
          <div><span>累计问答</span><strong>{{ stats.total || 0 }}</strong></div>
          <div><span>今日问答</span><strong>{{ stats.today || 0 }}</strong></div>
          <div><span>敏感命中</span><strong class="danger">{{ stats.sensitive || 0 }}</strong></div>
          <div><span>待人工审核</span><strong class="warning">{{ stats.pending || 0 }}</strong></div>
          <div><span>平均响应</span><strong>{{ stats.avgResponseMs || 0 }} ms</strong></div>
        </div>

        <div class="toolbar">
          <el-input v-model="keyword" placeholder="搜索问题、回答或会话ID" clearable @keyup.enter="loadAudits" />
          <el-select v-model="status" clearable placeholder="风险或审核状态">
            <el-option label="敏感命中" value="sensitive" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadAudits">查询</el-button>
        </div>

        <el-table :data="records" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="74" />
          <el-table-column prop="username" label="用户" width="110">
            <template #default="{ row }">{{ row.username || '匿名用户' }}</template>
          </el-table-column>
          <el-table-column prop="question" label="群众问题" min-width="240" show-overflow-tooltip />
          <el-table-column prop="modelName" label="模型" width="180" show-overflow-tooltip />
          <el-table-column label="风险" width="96">
            <template #default="{ row }">
              <el-tag :type="row.riskLevel > 0 ? 'danger' : 'success'">
                {{ row.riskLevel > 0 ? `${row.riskLevel}级` : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核" width="105">
            <template #default="{ row }">
              <el-tag :type="reviewTagType(row.reviewStatus)">{{ reviewLabel(row.reviewStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="responseMs" label="耗时(ms)" width="100" />
          <el-table-column prop="createTime" label="时间" width="175" />
          <el-table-column label="操作" width="176" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="View" @click="showDetail(row.id)">详情</el-button>
              <el-button v-if="row.reviewStatus === 'pending'" link type="warning" :icon="Finished" @click="review(row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="loadAudits" />
      </el-tab-pane>

      <el-tab-pane label="敏感词库" name="sensitive">
        <div class="section-actions">
          <div class="level-legend">
            <el-tag>1级 自动替换</el-tag><el-tag type="warning">2级 人工复核</el-tag><el-tag type="danger">3级 直接拦截</el-tag>
          </div>
          <el-button type="primary" :icon="Plus" @click="openWordDialog()">新增敏感词</el-button>
        </div>
        <el-table :data="words" border stripe v-loading="wordLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="word" label="敏感词" min-width="180" />
          <el-table-column prop="category" label="分类" min-width="140" />
          <el-table-column label="等级" width="160">
            <template #default="{ row }"><el-tag :type="levelTagType(row.level)">{{ row.level }}级 · {{ levelLabel(row.level) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="匹配算法" min-width="190"><template #default>DFA 双向最大匹配</template></el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" :icon="EditPen" @click="openWordDialog(row)">编辑</el-button>
              <el-button link type="danger" :icon="Delete" @click="removeWord(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="搜索索引" name="search">
        <div class="search-status" v-loading="searchLoading">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Elasticsearch">
              <el-tag :type="searchState.available ? 'success' : 'danger'">{{ searchState.available ? '运行正常' : '不可用' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="索引名称">{{ searchState.indexName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="索引文档">{{ searchState.documentCount || 0 }} 条</el-descriptions-item>
            <el-descriptions-item label="向量引擎">{{ searchState.embedding?.engine || '-' }}</el-descriptions-item>
            <el-descriptions-item label="向量模型">{{ searchState.embedding?.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="向量维度">{{ searchState.embedding?.dimensions || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-button type="primary" :icon="RefreshRight" :loading="rebuilding" @click="rebuildIndex">全量重建索引</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-drawer v-model="detailVisible" title="问答审计详情" size="52%">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="会话ID" :span="2">{{ detail.sessionId }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.username || '匿名用户' }}</el-descriptions-item>
        <el-descriptions-item label="模型">{{ detail.modelName }}</el-descriptions-item>
        <el-descriptions-item label="问题" :span="2"><div class="long-text">{{ detail.question }}</div></el-descriptions-item>
        <el-descriptions-item label="AI回答" :span="2"><div class="long-text">{{ detail.answer }}</div></el-descriptions-item>
        <el-descriptions-item label="知识来源" :span="2"><div class="source-row"><el-tag v-for="source in parseSources(detail.sources)" :key="`${source.type}-${source.id}`">{{ source.type }} · {{ source.title }}</el-tag></div></el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detail.reviewComment || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="wordDialogVisible" :title="wordForm.id ? '编辑敏感词' : '新增敏感词'" width="460px">
      <el-form label-position="top">
        <el-form-item label="敏感词"><el-input v-model="wordForm.word" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="分类"><el-input v-model="wordForm.category" placeholder="例如：违法、辱骂、隐私" /></el-form-item>
        <el-form-item label="处置等级">
          <el-segmented v-model="wordForm.level" :options="levelOptions" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="wordDialogVisible = false">取消</el-button><el-button type="primary" @click="saveWord">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Delete, EditPen, Finished, Plus, Refresh, RefreshRight, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminSensitiveWords, aiSearchStatus, chatAudit, chatAuditDetail, chatAuditStats,
  createSensitiveWord, deleteSensitiveWord, rebuildAiSearch, reviewChatAudit, updateSensitiveWord
} from '@/api/ai'

const activeTab = ref('audit')
const records = ref<any[]>([])
const stats = ref<any>({})
const keyword = ref('')
const status = ref('')
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref<any>(null)
const words = ref<any[]>([])
const wordLoading = ref(false)
const wordDialogVisible = ref(false)
const wordForm = reactive<any>({ id: null, word: '', category: 'default', level: 1 })
const levelOptions = [{ label: '1级 替换', value: 1 }, { label: '2级 复核', value: 2 }, { label: '3级 拦截', value: 3 }]
const searchState = ref<any>({})
const searchLoading = ref(false)
const rebuilding = ref(false)

onMounted(() => refreshCurrent())

async function refreshCurrent() {
  if (activeTab.value === 'audit') await Promise.all([loadAudits(), loadStats()])
  else if (activeTab.value === 'sensitive') await loadWords()
  else await loadSearchStatus()
}

async function loadAudits() {
  loading.value = true
  try {
    const res: any = await chatAudit({ keyword: keyword.value, status: status.value, page: page.value, size })
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function loadStats() { const res: any = await chatAuditStats(); stats.value = res.data || {} }
async function showDetail(id: number) { const res: any = await chatAuditDetail(id); detail.value = res.data; detailVisible.value = true }

async function review(row: any) {
  const result: any = await ElMessageBox.prompt('填写审核意见', '人工审核', {
    inputPlaceholder: '说明通过或驳回原因', confirmButtonText: '审核通过', cancelButtonText: '驳回', distinguishCancelAndClose: true
  }).catch((action) => action === 'cancel' ? { value: '', rejected: true } : Promise.reject(action))
  await reviewChatAudit(row.id, result.rejected ? 'rejected' : 'approved', result.value || '')
  ElMessage.success('审核结果已保存')
  await refreshCurrent()
}

async function loadWords() { wordLoading.value = true; try { const res: any = await adminSensitiveWords(); words.value = res.data || [] } finally { wordLoading.value = false } }
function openWordDialog(row?: any) { Object.assign(wordForm, row ? { ...row } : { id: null, word: '', category: 'default', level: 1 }); wordDialogVisible.value = true }
async function saveWord() {
  if (!wordForm.word.trim()) return ElMessage.warning('请输入敏感词')
  const payload = { word: wordForm.word.trim(), category: wordForm.category.trim() || 'default', level: Number(wordForm.level) }
  if (wordForm.id) await updateSensitiveWord(wordForm.id, payload); else await createSensitiveWord(payload)
  wordDialogVisible.value = false; ElMessage.success('敏感词已保存'); await loadWords()
}
async function removeWord(row: any) { await ElMessageBox.confirm(`确认删除敏感词“${row.word}”吗？`, '删除确认', { type: 'warning' }); await deleteSensitiveWord(row.id); await loadWords() }

async function loadSearchStatus() { searchLoading.value = true; try { const res: any = await aiSearchStatus(); searchState.value = res.data || {} } finally { searchLoading.value = false } }
async function rebuildIndex() { rebuilding.value = true; try { const res: any = await rebuildAiSearch(); ElMessage.success(`索引重建完成，共 ${res.data?.documentCount || 0} 条`); await loadSearchStatus() } finally { rebuilding.value = false } }

function reviewLabel(value: string) { return ({ pending: '待审核', approved: '已通过', rejected: '已驳回', auto_passed: '自动通过' } as any)[value] || value || '-' }
function reviewTagType(value: string) { return value === 'rejected' ? 'danger' : value === 'pending' ? 'warning' : 'success' }
function levelLabel(level: number) { return level === 3 ? '拦截' : level === 2 ? '复核' : '替换' }
function levelTagType(level: number) { return level === 3 ? 'danger' : level === 2 ? 'warning' : '' }
function parseSources(value: unknown) { if (Array.isArray(value)) return value; try { return JSON.parse(String(value || '[]')) } catch { return [] } }
</script>

<style scoped>
.ai-admin-page { padding: 20px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 22px; color: #172033; }
.page-head p { margin: 5px 0 0; color: #667085; }
.stats-strip { display: grid; grid-template-columns: repeat(5,minmax(0,1fr)); border: 1px solid #e4e7ed; margin-bottom: 14px; }
.stats-strip > div { padding: 14px 16px; border-right: 1px solid #e4e7ed; }
.stats-strip > div:last-child { border-right: 0; }
.stats-strip span { display: block; color: #667085; font-size: 13px; }
.stats-strip strong { display: block; margin-top: 6px; font-size: 22px; color: #172033; }
.stats-strip .danger { color: #c53030; } .stats-strip .warning { color: #b45309; }
.toolbar { display: grid; grid-template-columns: minmax(260px,1fr) 190px 94px; gap: 10px; margin-bottom: 14px; }
.el-pagination { margin-top: 14px; justify-content: flex-end; }
.section-actions { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.level-legend, .source-row { display: flex; flex-wrap: wrap; gap: 8px; }
.search-status { display: grid; gap: 16px; max-width: 900px; }
.search-status .el-button { justify-self: start; }
.long-text { white-space: pre-wrap; line-height: 1.75; max-height: 320px; overflow-y: auto; }
@media (max-width: 900px) { .stats-strip { grid-template-columns: repeat(2,1fr); } .toolbar { grid-template-columns: 1fr; } }
</style>
