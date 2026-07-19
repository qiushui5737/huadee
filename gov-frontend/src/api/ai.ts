import request from '@/utils/request'

export const search = (keyword: string, page = 1, size = 10) =>
  request.get('/ai/search', { params: { keyword, page, size } })

export const semanticSearch = (keyword: string, page = 1, size = 10) =>
  request.get('/ai/search/semantic', { params: { keyword, page, size } })

export const hotKeywords = () => request.get('/ai/search/hot')

export const chatStream = (question: string, sessionId?: string) => {
  const params = new URLSearchParams({ question })
  if (sessionId) params.set('sessionId', sessionId)
  const apiOrigin = import.meta.env.VITE_API_ORIGIN || 'http://127.0.0.1:8080'
  return new EventSource(`${apiOrigin}/api/v1/ai/chat/stream?${params.toString()}`)
}

export const chatOnce = (question: string, sessionId?: string) =>
  request.post('/ai/chat', { question, sessionId })

export const chatHistory = (sessionId: string) =>
  request.get(`/ai/chat/history/${sessionId}`)

export const chatAudit = (params: { keyword?: string; status?: string; page?: number; size?: number }) =>
  request.get('/ai/chat/audit', { params })

export const checkSensitive = (text: string) =>
  request.post('/ai/sensitive/check', { text })

export const sensitiveWords = () => request.get('/ai/sensitive/words')
