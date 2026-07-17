<template>
  <div class="q-mgmt">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>问卷管理</span>
          <el-button type="primary" size="small" @click="showCreate">创建问卷</el-button>
        </div>
      </template>

      <el-table :data="listData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已发布' ? 'success' : row.status === '已关闭' ? 'info' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAnswers" label="参与人数" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">题目</el-button>
            <el-button link type="success" size="small" @click="showStats(row)">统计</el-button>
            <el-button v-if="row.status === '草稿'" link type="warning" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === '已发布'" link type="info" size="small" @click="handleClose(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadList" />
    </el-card>

    <!-- 创建问卷弹窗 -->
    <el-dialog v-model="createVisible" title="创建问卷" width="700px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="问卷标题"><el-input v-model="createForm.title" placeholder="请输入问卷标题" /></el-form-item>
        <el-form-item label="问卷说明"><el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="请输入问卷说明（选填）" /></el-form-item>
      </el-form>

      <el-divider content-position="left">题目列表</el-divider>

      <div v-for="(q, idx) in createForm.questions" :key="idx" class="question-edit">
        <el-row :gutter="10" align="middle">
          <el-col :span="12">
            <el-input v-model="q.questionText" :placeholder="`第${idx + 1}题题目`" size="small" />
          </el-col>
          <el-col :span="4">
            <el-select v-model="q.questionType" size="small">
              <el-option label="单选" value="single" />
              <el-option label="多选" value="multiple" />
              <el-option label="文本" value="text" />
            </el-select>
          </el-col>
          <el-col :span="2">
            <el-switch v-model="q.required" size="small" />
          </el-col>
          <el-col :span="2">
            <el-button type="danger" link size="small" @click="createForm.questions.splice(idx, 1)">删除</el-button>
          </el-col>
        </el-row>
        <div v-if="q.questionType !== 'text'" style="margin-top: 8px;">
          <el-input v-for="(opt, oi) in q.options" :key="oi" v-model="q.options[oi]" :placeholder="`选项${oi + 1}`" size="small" style="margin-bottom: 5px; width: 200px; margin-right: 5px;" />
          <el-button link type="primary" size="small" @click="q.options.push('')">+ 添加选项</el-button>
        </div>
      </div>

      <el-button type="primary" link @click="createForm.questions.push({ questionText: '', questionType: 'single', required: true, options: ['选项A', '选项B'] })" style="margin-top: 10px;">+ 添加题目</el-button>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 题目详情弹窗 -->
    <el-dialog v-model="detailVisible" title="问卷题目" width="600px">
      <div v-if="currentQ">
        <h3>{{ currentQ.title }}</h3>
        <div v-for="(q, idx) in currentQ.questions" :key="q.id" style="margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0;">
          <div><strong>{{ idx + 1 }}. {{ q.questionText }}</strong> <el-tag size="small">{{ q.questionType === 'single' ? '单选' : q.questionType === 'multiple' ? '多选' : '文本' }}</el-tag></div>
          <div v-if="q.options && q.options.length" style="margin-left: 20px; color: #666; margin-top: 5px;">
            <div v-for="opt in q.options" :key="opt">{{ opt }}</div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 统计结果弹窗 -->
    <el-dialog v-model="statsVisible" title="统计结果" width="700px">
      <div v-if="statsData">
        <h3>{{ statsData.title }} <el-tag style="margin-left: 10px;">{{ statsData.totalAnswers }} 份答卷</el-tag></h3>
        <div v-for="qs in statsData.questions" :key="qs.questionId" style="margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0;">
          <div style="font-weight: 600; margin-bottom: 10px;">{{ qs.questionText }} <el-tag size="small">{{ qs.questionType === 'single' ? '单选' : qs.questionType === 'multiple' ? '多选' : '文本' }}</el-tag></div>
          <!-- 选择题统计 -->
          <div v-if="qs.optionStats">
            <div v-for="(count, opt) in qs.optionStats" :key="opt" style="margin-bottom: 8px;">
              <div style="display: flex; align-items: center; gap: 10px;">
                <span style="width: 120px; font-size: 13px;">{{ opt }}</span>
                <el-progress :percentage="statsData.totalAnswers > 0 ? Math.round((count / statsData.totalAnswers) * 100) : 0" style="flex: 1;" :stroke-width="16" />
                <span style="width: 60px; font-size: 13px; color: #666;">{{ count }}票</span>
              </div>
            </div>
          </div>
          <!-- 文本题回答 -->
          <div v-if="qs.answers">
            <div v-for="(a, ai) in qs.answers" :key="ai" style="background: #f5f7fa; padding: 8px 12px; border-radius: 4px; margin-bottom: 6px; font-size: 13px;">{{ a || '(空)' }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionnaireList, createQuestionnaire, publishQuestionnaire, closeQuestionnaire, questionnaireDetail, questionnaireStatistics } from '@/api/interaction'

const listData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const createVisible = ref(false)
const creating = ref(false)
const createForm = reactive({ title: '', description: '', questions: [] as any[] })

const detailVisible = ref(false)
const currentQ = ref<any>(null)

const statsVisible = ref(false)
const statsData = ref<any>(null)

const loadList = async () => {
  try {
    const res: any = await questionnaireList({ page: currentPage.value, size: pageSize })
    if (res.code === 200) { listData.value = res.data.records; total.value = res.data.total }
  } catch (e) { /* ignore */ }
}

const showCreate = () => {
  createForm.title = ''
  createForm.description = ''
  createForm.questions = [{ questionText: '', questionType: 'single', required: true, options: ['选项A', '选项B'] }]
  createVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.title.trim()) { ElMessage.warning('请输入问卷标题'); return }
  creating.value = true
  try {
    const res: any = await createQuestionnaire(createForm)
    if (res.code === 200) { ElMessage.success('创建成功'); createVisible.value = false; loadList() }
  } catch (e) { ElMessage.error('创建失败') } finally { creating.value = false }
}

const showDetail = async (row: any) => {
  const res: any = await questionnaireDetail(row.id)
  if (res.code === 200) { currentQ.value = res.data; detailVisible.value = true }
}

const handlePublish = async (row: any) => {
  await ElMessageBox.confirm(`确认发布问卷「${row.title}」？`, '发布确认')
  const res: any = await publishQuestionnaire(row.id)
  if (res.code === 200) { ElMessage.success('发布成功'); loadList() }
}

const handleClose = async (row: any) => {
  await ElMessageBox.confirm(`确认关闭问卷「${row.title}」？关闭后不可再填写。`, '关闭确认')
  const res: any = await closeQuestionnaire(row.id)
  if (res.code === 200) { ElMessage.success('已关闭'); loadList() }
}

const showStats = async (row: any) => {
  const res: any = await questionnaireStatistics(row.id)
  if (res.code === 200) { statsData.value = res.data; statsVisible.value = true }
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.q-mgmt { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; font-size: 16px; }
.question-edit { background: #fafafa; padding: 12px; border-radius: 6px; margin-bottom: 10px; }
</style>
