import request from '@/utils/request'

// B1-事项目录
export const categories = () => request.get('/service/catalog/categories')
export const items = (params: any) => request.get('/service/catalog/items', { params })
export const itemDetail = (id: number) => request.get(`/service/catalog/items/${id}`)

// B2-动态表单 & B3-审批流程
export const formSchema = (itemId: number) => request.get(`/service/form/schema/${itemId}`)
export const submitForm = (data: any) => request.post('/service/form/submit', data)
export const formProgress = (acceptNo: string) => request.get(`/service/form/progress/${acceptNo}`)
