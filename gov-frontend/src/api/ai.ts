import request from '@/utils/request'

// A1-智能搜索
export const search = (keyword: string, page = 1, size = 10) =>
  request.get('/ai/search', { params: { keyword, page, size } })

export const hotKeywords = () => request.get('/ai/search/hot')

// A3-智能问答(流式)
export const chatStream = (question: string) =>
  new EventSource(`/api/v1/ai/chat/stream?question=${encodeURIComponent(question)}`)

export const chatHistory = (sessionId: string) =>
  request.get(`/ai/chat/history/${sessionId}`)

// A4-敏感词检查
export const checkSensitive = (text: string) =>
  request.post('/ai/sensitive/check', { text })
