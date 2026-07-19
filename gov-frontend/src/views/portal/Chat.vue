<template>
  <section class="chat-page">
    <div class="chat-shell">
      <aside class="side-panel">
        <div class="side-head">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能问答</span>
        </div>
        <el-button type="primary" plain :icon="Plus" @click="newSession">新会话</el-button>
        <div class="quick-list">
          <button v-for="item in examples" :key="item" @click="askExample(item)">{{ item }}</button>
        </div>
        <div class="guard-box">
          <el-icon><Warning /></el-icon>
          <div>
            <strong>敏感词过滤</strong>
            <p>提问会先经过本地敏感词检测，再进入知识库问答。</p>
          </div>
        </div>
      </aside>

      <main class="dialog-panel">
        <div class="dialog-head">
          <div>
            <h1>政务智能助手</h1>
            <p>基于知识库、办事事项和信息公开内容进行 RAG 检索回答。</p>
          </div>
          <el-tag effect="plain">SSE 实时输出</el-tag>
        </div>

        <div ref="scrollBox" class="message-list">
          <div v-for="msg in messages" :key="msg.id" class="message-row" :class="msg.role">
            <div class="avatar">{{ msg.role === 'user' ? '问' : '答' }}</div>
            <div class="bubble">
              <div
                v-if="msg.role === 'assistant'"
                class="markdown-body"
                v-html="renderMarkdown(msg.content)"
              />
              <p v-else>{{ msg.content }}</p>
              <div v-if="msg.thinking" class="thinking-pill">
                <span class="thinking-spinner" />
                <span>正在理解问题并检索知识库</span>
                <i />
                <i />
                <i />
              </div>
              <div v-if="msg.sources?.length" class="source-list">
                <span v-for="source in msg.sources" :key="`${source.type}-${source.id}`">
                  {{ source.type }}：{{ source.title }}
                </span>
              </div>
            </div>
          </div>
          <el-empty v-if="messages.length === 0" description="请输入政务问题开始咨询" />
        </div>

        <div class="composer">
          <el-input
            v-model="question"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            resize="none"
            placeholder="例如：健康证明怎么办理？"
            @keydown.ctrl.enter="send"
          />
          <div class="composer-actions">
            <div class="check-result" :class="{ danger: sensitiveResult && !sensitiveResult.passed }">
              {{ sensitiveText }}
            </div>
            <el-button type="primary" :icon="Promotion" :loading="streaming" @click="send">发送</el-button>
          </div>
        </div>
      </main>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { ChatDotRound, Plus, Promotion, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { chatStream, checkSensitive } from '@/api/ai'

const examples = ['学生资助怎么申请？', '健康证明办理需要什么材料？', '如何查询办事进度？', '政府信息公开怎么申请？']
const question = ref('')
const sessionId = ref(localStorage.getItem('ai_session_id') || '')
const messages = ref<any[]>([])
const streaming = ref(false)
const sensitiveResult = ref<any>(null)
const scrollBox = ref<HTMLElement>()

let typingTimer: number | undefined
let chunkQueue: string[] = []
let streamDone = false
let activeMessageIndex = -1
let waitingForFirstChunk = false

const sensitiveText = computed(() => {
  if (!sensitiveResult.value) return '输入内容将自动检测'
  return sensitiveResult.value.passed ? '敏感词检测通过' : `命中 ${sensitiveResult.value.hitCount} 个敏感词`
})

async function send() {
  const text = question.value.trim()
  if (!text || streaming.value) return

  const check: any = await checkSensitive(text)
  sensitiveResult.value = check.data
  if (!check.data?.passed) {
    ElMessage.warning('当前问题包含敏感内容，系统将给出合规提示。')
  }

  const userMsg = { id: Date.now(), role: 'user', content: text }
  const aiMsg = { id: Date.now() + 1, role: 'assistant', content: '', sources: [] as any[], thinking: true }
  messages.value.push(userMsg, aiMsg)
  question.value = ''
  streaming.value = true
  activeMessageIndex = messages.value.length - 1
  chunkQueue = []
  streamDone = false
  waitingForFirstChunk = true
  startTypingLoop()
  await scrollToBottom()

  const es = chatStream(text, sessionId.value)
  es.addEventListener('meta', (event: MessageEvent) => {
    const meta = JSON.parse(event.data)
    sessionId.value = meta.sessionId
    localStorage.setItem('ai_session_id', sessionId.value)
    updateActiveMessage({ sources: meta.sources || [] })
  })
  es.addEventListener('chunk', (event: MessageEvent) => {
    if (waitingForFirstChunk) {
      updateActiveMessage({ content: '', thinking: false })
      waitingForFirstChunk = false
    }
    chunkQueue.push(...Array.from(String(event.data)))
    startTypingLoop()
  })
  es.addEventListener('done', () => {
    streamDone = true
    es.close()
    finishIfQueueEmpty()
  })
  es.addEventListener('error', () => {
    streamDone = true
    es.close()
    finishIfQueueEmpty()
  })
}

function startTypingLoop() {
  if (typingTimer) return
  typingTimer = window.setInterval(async () => {
    if (activeMessageIndex < 0) return
    const take = chunkQueue.shift()
    if (take) {
      const msg = messages.value[activeMessageIndex]
      updateActiveMessage({ content: `${msg.content}${take}` })
      await scrollToBottom()
      return
    }
    finishIfQueueEmpty()
  }, 35)
}

function finishIfQueueEmpty() {
  if (!streamDone || chunkQueue.length > 0) return
  if (typingTimer) {
    clearInterval(typingTimer)
    typingTimer = undefined
  }
  updateActiveMessage({ thinking: false })
  activeMessageIndex = -1
  streaming.value = false
}

function updateActiveMessage(patch: Record<string, any>) {
  if (activeMessageIndex < 0) return
  const current = messages.value[activeMessageIndex]
  if (!current) return
  messages.value[activeMessageIndex] = { ...current, ...patch }
}

function askExample(text: string) {
  question.value = text
  send()
}

function newSession() {
  sessionId.value = ''
  localStorage.removeItem('ai_session_id')
  messages.value = []
  sensitiveResult.value = null
  chunkQueue = []
  streamDone = false
  waitingForFirstChunk = false
  activeMessageIndex = -1
  streaming.value = false
  if (typingTimer) {
    clearInterval(typingTimer)
    typingTimer = undefined
  }
}

async function scrollToBottom() {
  await nextTick()
  if (scrollBox.value) {
    scrollBox.value.scrollTop = scrollBox.value.scrollHeight
  }
}

function renderMarkdown(value: string) {
  if (!value) return ''
  return value
    .split('\n')
    .map((line) => renderLine(line))
    .join('')
}

function renderLine(line: string) {
  const escaped = inlineMarkdown(escapeHtml(line.trimEnd()))
  if (!escaped.trim()) return '<div class="md-gap"></div>'
  const heading = escaped.match(/^#{1,6}\s*(.+)/)
  if (heading) return `<h3>${heading[1]}</h3>`
  const bullet = escaped.match(/^[-*]\s*(.+)/)
  if (bullet) return `<div class="md-list">• ${bullet[1]}</div>`
  const ordered = escaped.match(/^(\d+\.)\s*(.+)/)
  if (ordered) return `<div class="md-list"><strong>${ordered[1]}</strong> ${ordered[2]}</div>`
  return `<p>${escaped}</p>`
}

function inlineMarkdown(value: string) {
  return value.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}
</script>

<style scoped>
.chat-page { min-height: calc(100vh - 132px); background: #eef3f8; padding: 20px; }
.chat-shell { max-width: 1180px; margin: 0 auto; display: grid; grid-template-columns: 278px minmax(0,1fr); gap: 18px; }
.side-panel, .dialog-panel { background: #fff; border: 1px solid #e3e8ef; border-radius: 8px; }
.side-panel { padding: 18px; display: flex; flex-direction: column; gap: 16px; }
.side-head { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: 700; color: #1f2a3d; }
.quick-list { display: grid; gap: 8px; }
.quick-list button { text-align: left; border: 1px solid #e5eaf2; background: #f8fafc; border-radius: 6px; padding: 10px 12px; color: #344054; cursor: pointer; }
.quick-list button:hover { border-color: #0f5fb8; color: #0f5fb8; background: #f3f8ff; }
.guard-box { display: grid; grid-template-columns: 28px 1fr; gap: 10px; padding: 12px; border-radius: 8px; background: #fff8ed; color: #7a4b12; }
.guard-box p { margin: 4px 0 0; font-size: 12px; line-height: 1.5; }
.dialog-panel { min-height: 680px; display: grid; grid-template-rows: auto minmax(0,1fr) auto; }
.dialog-head { padding: 18px 20px; border-bottom: 1px solid #edf0f5; display: flex; align-items: center; justify-content: space-between; gap: 14px; }
.dialog-head h1 { margin: 0; font-size: 22px; color: #172033; letter-spacing: 0; }
.dialog-head p { margin: 6px 0 0; color: #667085; font-size: 13px; }
.message-list { padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.message-row { display: grid; grid-template-columns: 34px minmax(0,1fr); gap: 10px; max-width: 82%; }
.message-row.user { margin-left: auto; grid-template-columns: minmax(0,1fr) 34px; }
.message-row.user .avatar { grid-column: 2; background: #0f5fb8; }
.message-row.user .bubble { grid-column: 1; grid-row: 1; background: #eaf3ff; }
.avatar { width: 34px; height: 34px; border-radius: 50%; background: #0a7f83; color: #fff; display: grid; place-items: center; font-weight: 700; }
.bubble { border-radius: 8px; background: #f6f8fb; padding: 12px 14px; color: #243044; line-height: 1.7; }
.bubble p { margin: 0; white-space: pre-wrap; }
.markdown-body :deep(p) { margin: 0 0 8px; }
.markdown-body :deep(h3) { font-size: 16px; margin: 12px 0 6px; color: #1f2a3d; }
.markdown-body :deep(strong) { font-weight: 700; color: #111827; }
.markdown-body :deep(.md-list) { margin: 4px 0; padding-left: 2px; }
.markdown-body :deep(.md-gap) { height: 8px; }
.thinking-pill { display: inline-flex; align-items: center; gap: 8px; padding: 8px 10px; border-radius: 999px; background: #eef6ff; color: #0f5fb8; font-size: 14px; }
.thinking-spinner { width: 14px; height: 14px; border-radius: 50%; border: 2px solid rgba(15,95,184,.22); border-top-color: #0f5fb8; animation: spin .9s linear infinite; }
.thinking-pill i { width: 5px; height: 5px; border-radius: 50%; background: #0f5fb8; opacity: .35; animation: dotPulse 1.2s ease-in-out infinite; }
.thinking-pill i:nth-child(4) { animation-delay: .18s; }
.thinking-pill i:nth-child(5) { animation-delay: .36s; }
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes dotPulse { 0%, 80%, 100% { transform: translateY(0); opacity: .3; } 40% { transform: translateY(-3px); opacity: 1; } }
.source-list { margin-top: 10px; display: flex; flex-wrap: wrap; gap: 6px; }
.source-list span { font-size: 12px; color: #0f5fb8; background: #eef6ff; padding: 4px 7px; border-radius: 5px; }
.composer { border-top: 1px solid #edf0f5; padding: 14px; }
.composer-actions { margin-top: 10px; display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.check-result { font-size: 13px; color: #667085; }
.check-result.danger { color: #c2410c; }
@media (max-width: 900px) {
  .chat-shell { grid-template-columns: 1fr; }
  .message-row { max-width: 100%; }
}
</style>
