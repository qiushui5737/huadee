import request from '@/utils/request'

export const search = (keyword: string, page = 1, size = 10) =>
  request.get('/ai/search', { params: { keyword, page, size } })

export const semanticSearch = (keyword: string, page = 1, size = 10) =>
  request.get('/ai/search/semantic', { params: { keyword, page, size } })

export const hotKeywords = () => request.get('/ai/search/hot')

type ChatStreamHandlers = {
  onMeta: (data: any) => void
  onChunk: (data: string) => void
  onDone: () => void
  onError: (message?: string) => void
}

export const chatStream = (question: string, sessionId: string | undefined, handlers: ChatStreamHandlers) => {
  const apiOrigin = import.meta.env.VITE_API_ORIGIN || 'http://127.0.0.1:8080'
  const controller = new AbortController()
  const token = localStorage.getItem('c_token')

  void fetch(`${apiOrigin}/api/v1/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({ question, sessionId }),
    signal: controller.signal
  }).then(async (response) => {
    if (!response.ok || !response.body) throw new Error(`HTTP ${response.status}`)
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
      const events = buffer.split('\n\n')
      buffer = events.pop() || ''
      for (const block of events) dispatchSseBlock(block, handlers)
    }
    if (buffer.trim()) dispatchSseBlock(buffer, handlers)
  }).catch((error) => {
    if (error?.name !== 'AbortError') handlers.onError(error?.message)
  })

  return { close: () => controller.abort() }
}

function dispatchSseBlock(block: string, handlers: ChatStreamHandlers) {
  let eventName = 'message'
  const dataLines: string[] = []
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) eventName = line.slice(6).trim()
    if (line.startsWith('data:')) dataLines.push(line.slice(5).trimStart())
  }
  const data = dataLines.join('\n')
  if (eventName === 'meta') handlers.onMeta(JSON.parse(data))
  else if (eventName === 'chunk') handlers.onChunk(data)
  else if (eventName === 'done') handlers.onDone()
  else if (eventName === 'error') handlers.onError(data)
}

export const chatOnce = (question: string, sessionId?: string) =>
  request.post('/ai/chat', { question, sessionId })

export const chatHistory = (sessionId: string) =>
  request.get(`/ai/chat/history/${sessionId}`)

export const chatConversations = () =>
  request.get('/ai/chat/conversations')

export const createChatConversation = (title = '新会话') =>
  request.post('/ai/chat/conversations', { title })

export const renameChatConversation = (sessionId: string, title: string) =>
  request.put(`/ai/chat/conversations/${sessionId}`, { title })

export const deleteChatConversation = (sessionId: string) =>
  request.delete(`/ai/chat/conversations/${sessionId}`)

export const chatAudit = (params: { keyword?: string; status?: string; page?: number; size?: number }) =>
  request.get('/ai/admin/audits', { params })

export const chatAuditStats = () => request.get('/ai/admin/audits/stats')
export const chatAuditDetail = (id: number) => request.get(`/ai/admin/audits/${id}`)
export const reviewChatAudit = (id: number, status: 'approved' | 'rejected', comment: string) =>
  request.put(`/ai/admin/audits/${id}/review`, { status, comment })

export const adminSensitiveWords = () => request.get('/ai/admin/sensitive-words')
export const createSensitiveWord = (data: { word: string; category: string; level: number }) =>
  request.post('/ai/admin/sensitive-words', data)
export const updateSensitiveWord = (id: number, data: { word: string; category: string; level: number }) =>
  request.put(`/ai/admin/sensitive-words/${id}`, data)
export const deleteSensitiveWord = (id: number) => request.delete(`/ai/admin/sensitive-words/${id}`)

export const aiSearchStatus = () => request.get('/ai/admin/search/status')
export const rebuildAiSearch = () => request.post('/ai/admin/search/rebuild')

export const checkSensitive = (text: string) =>
  request.post('/ai/sensitive/check', { text })

export const sensitiveWords = () => request.get('/ai/sensitive/words')
