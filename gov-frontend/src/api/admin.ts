import request from '@/utils/request'

// E4-认证权限
export const adminLogin = (data: { username: string; password: string }) =>
  request.post('/admin/auth/login', data)
export const adminInfo = () => request.get('/admin/auth/info')

// E1-E2-统计大屏
export const dashboard = () => request.get('/admin/stats/dashboard')
export const report = (params: any) => request.get('/admin/stats/report', { params })

// E3-绩效管理
export const performanceRanking = () => request.get('/admin/performance/ranking')

// E5-消息中心
export const messageList = (params: any) => request.get('/admin/message/list', { params })
