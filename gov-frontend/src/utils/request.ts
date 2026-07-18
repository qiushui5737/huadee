import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * Axios请求封装
 * C端: token存 localStorage['c_token']
 * B端: token存 localStorage['admin_token']
 */
const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 - 自动携带Token
request.interceptors.request.use(
  (config) => {
    // 判断当前是C端还是B端
    const audience = String(config.headers['X-Token-Audience'] || '')
    const isAdmin = audience ? audience === 'admin' : window.location.pathname.startsWith('/admin')
    const token = localStorage.getItem(isAdmin ? 'admin_token' : 'c_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 - 统一处理结果
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      if (res.code === 401) {
        const isAdmin = window.location.pathname.startsWith('/admin')
        localStorage.removeItem(isAdmin ? 'admin_token' : 'c_token')
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      const isAdmin = window.location.pathname.startsWith('/admin')
      localStorage.removeItem(isAdmin ? 'admin_token' : 'c_token')
      if (isAdmin) {
        window.location.href = '/admin/login'
      } else {
        window.location.href = '/login'
      }
    }
    ElMessage.error(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
