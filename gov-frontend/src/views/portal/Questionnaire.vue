<template>
  <div class="questionnaire-page">
    <div class="page-header">
      <h1>民意调查</h1>
      <p class="subtitle">您的意见对我们很重要</p>
    </div>

    <!-- 问卷列表 -->
    <div v-if="!currentQ">
      <el-row :gutter="20">
        <el-col :span="8" v-for="q in questionnaireList" :key="q.id">
          <el-card shadow="hover" class="q-card" @click="openQuestionnaire(q.id)">
            <div class="q-title">{{ q.title }}</div>
            <div class="q-desc">{{ q.description || '暂无说明' }}</div>
            <div class="q-meta">
              <el-tag :type="q.status === '已发布' ? 'success' : 'info'" size="small">{{ q.status }}</el-tag>
              <span class="q-count">{{ q.totalAnswers }} 人参与</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="questionnaireList.length === 0" description="暂无问卷" />
    </div>

    <!-- 问卷填写 -->
    <div v-else>
      <el-card shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>{{ currentQ.title }}</span>
            <el-button @click="currentQ = null" size="small">返回列表</el-button>
          </div>
        </template>

        <p style="color: #666; margin-bottom: 20px;">{{ currentQ.description }}</p>

        <el-form ref="answerFormRef" label-position="top">
          <div v-for="(q, idx) in currentQ.questions" :key="q.id" class="question-item">
            <div class="question-text">
              <span class="q-num">{{ idx + 1 }}.</span>
              {{ q.questionText }}
              <el-tag v-if="q.required" type="danger" size="small" style="margin-left: 5px;">必答</el-tag>
            </div>

            <!-- 单选 -->
            <el-radio-group v-if="q.questionType === 'single'" v-model="answers[q.id]" class="answer-options">
              <el-radio v-for="opt in q.options" :key="opt" :value="opt" style="display: block; margin: 8px 0;">{{ opt }}</el-radio>
            </el-radio-group>

            <!-- 多选 -->
            <el-checkbox-group v-if="q.questionType === 'multiple'" v-model="answers[q.id]" class="answer-options">
              <el-checkbox v-for="opt in q.options" :key="opt" :value="opt" style="display: block; margin: 8px 0;">{{ opt }}</el-checkbox>
            </el-checkbox-group>

            <!-- 文本 -->
            <el-input v-if="q.questionType === 'text'" v-model="answers[q.id]" type="textarea" :rows="3" placeholder="请输入您的回答..." />
          </div>
        </el-form>

        <el-divider />

        <el-form label-width="80px">
          <el-form-item label="姓名">
            <el-input v-model="answerUser.userName" placeholder="请输入姓名（选填）" style="width: 200px;" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="answerUser.phone" placeholder="请输入电话（选填）" style="width: 200px;" />
          </el-form-item>
        </el-form>

        <el-button type="primary" :loading="submitting" @click="submitAnswer" style="margin-top: 10px;">提交问卷</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { questionnaireList as fetchQList, questionnaireDetail, submitQuestionnaire } from '@/api/interaction'

const questionnaireList = ref<any[]>([])
const currentQ = ref<any>(null)
const answers = ref<Record<number, any>>({})
const answerUser = reactive({ userName: '', phone: '' })
const submitting = ref(false)

const loadList = async () => {
  try {
    const res: any = await fetchQList({ status: '已发布', page: 1, size: 20 })
    if (res.code === 200) {
      questionnaireList.value = res.data.records
    }
  } catch (e) { /* ignore */ }
}

const openQuestionnaire = async (id: number) => {
  try {
    const res: any = await questionnaireDetail(id)
    if (res.code === 200) {
      currentQ.value = res.data
      answers.value = {}
    }
  } catch (e) {
    ElMessage.error('加载问卷失败')
  }
}

const submitAnswer = async () => {
  submitting.value = true
  try {
    const payload = {
      userName: answerUser.userName,
      phone: answerUser.phone,
      answers: currentQ.value.questions.map((q: any) => ({
        questionId: q.id,
        answerText: q.questionType === 'text' ? (answers.value[q.id] || '') : undefined,
        answerOptions: q.questionType !== 'text' ? (Array.isArray(answers.value[q.id]) ? answers.value[q.id] : answers.value[q.id] ? [answers.value[q.id]] : []) : undefined
      }))
    }
    const res: any = await submitQuestionnaire(currentQ.value.id, payload)
    if (res.code === 200) {
      ElMessage.success('提交成功，感谢您的参与！')
      currentQ.value = null
      loadList()
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.questionnaire-page { max-width: 1200px; margin: 0 auto; padding: 20px; }
.page-header { text-align: center; margin-bottom: 30px;
  h1 { font-size: 28px; color: #1a3a5c; margin: 0; }
  .subtitle { color: #888; margin-top: 8px; font-size: 14px; }
}
.q-card { cursor: pointer; margin-bottom: 20px; transition: transform 0.2s;
  &:hover { transform: translateY(-2px); }
  .q-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; color: #333; }
  .q-desc { font-size: 13px; color: #888; margin-bottom: 12px; height: 36px; overflow: hidden; }
  .q-meta { display: flex; justify-content: space-between; align-items: center;
    .q-count { font-size: 12px; color: #999; }
  }
}
.question-item { margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0;
  .question-text { font-size: 15px; font-weight: 500; margin-bottom: 12px;
    .q-num { color: #409eff; margin-right: 4px; }
  }
}
</style>
