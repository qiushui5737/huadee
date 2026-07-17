import request from '@/utils/request'

// E4-认证权限
export const adminLogin = (data: { username: string; password: string }) =>
  request.post('/admin/auth/login', data)
export interface RegisterData {
  username: string; password: string; confirmPassword: string; realName: string
  gender: string; idCard: string; phone: string; email: string; deptCode: string; address: string
}
export const adminRegister = (data: RegisterData) => request.post('/admin/auth/register', data)
export const adminInfo = () => request.get('/admin/auth/info')
export const adminLogout = () => request.post('/admin/auth/logout')
export const updateProfile = (data: { realName: string; gender: string; phone: string; email: string; address: string }) =>
  request.put('/admin/auth/profile', data)
export const changePassword = (data: { oldPassword: string; newPassword: string; confirmPassword: string }) =>
  request.put('/admin/auth/password', data)

// E1-E2-统计大屏
export const dashboard = () => request.get('/admin/stats/dashboard')
export const report = (params: any) => request.get('/admin/stats/report', { params })

// E3-绩效管理
export const performanceRanking = () => request.get('/admin/performance/ranking')

// E5-消息中心
export const messageList = (params: any) => request.get('/admin/message/list', { params })
