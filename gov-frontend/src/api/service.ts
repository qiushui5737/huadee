import request from '@/utils/request'

// B1-事项目录
export const categories = () => request.get('/service/catalog/categories')
export const items = (params: any) => request.get('/service/catalog/items', { params })
export const itemDetail = (id: number) => request.get(`/service/catalog/items/${id}`)

// 管理端服务管理
export const myItems = () => request.get('/service/catalog/my-items')
export const createItem = (data: any) => request.post('/service/catalog/items', data)
export const updateItem = (id: number, data: any) => request.put(`/service/catalog/items/${id}`, data)
export const deleteItem = (id: number) => request.delete(`/service/catalog/items/${id}`)

// B2-动态表单 & B3-审批流程
export const formSchema = (itemId: number) => request.get(`/service/form/schema/${itemId}`)
export const submitForm = (data: any) => request.post('/service/form/submit', data)
export const formProgress = (acceptNo: string) => request.get(`/service/form/progress/${acceptNo}`)
export const records = (params?: any) => request.get('/service/form/records', { params })
export const recordDetail = (acceptNo: string) => request.get(`/service/form/records/${acceptNo}`)

// 草稿
export const getDrafts = () => request.get('/service/form/drafts')
export const deleteDraft = (acceptNo: string) => request.delete(`/service/form/drafts/${acceptNo}`)

// 文件上传
export const uploadFile = (file: File, acceptNo: string) => {
  const fd = new FormData()
  fd.append('file', file)
  fd.append('acceptNo', acceptNo)
  return request.post('/service/form/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const getAttachments = (acceptNo: string) => request.get(`/service/form/attachments/${acceptNo}`)

// 补充材料
export const requestSupplement = (acceptNo: string, reason: string) => request.post(`/service/form/supplement/${acceptNo}`, { reason })
export const supplementDone = (acceptNo: string) => request.post(`/service/form/supplement-done/${acceptNo}`)

// 服务评价
export const submitRating = (data: any) => request.post('/service/form/rating', data)
export const getAllRatings = () => request.get('/service/form/ratings')

// B4-进度查询 & B5-缴费证照
export const calculatePayment = (acceptNo: string) => request.get(`/service/payment/calculate/${acceptNo}`)
export const pay = (data: any) => request.post('/service/payment/pay', data)
export const getLicense = (acceptNo: string) => request.get(`/service/payment/license/${acceptNo}`)
export const downloadLicense = (acceptNo: string) => request.get(`/service/payment/download/${acceptNo}`)
export const paymentRecords = (params?: any) => request.get('/service/payment/records', { params })

// 审批管理
export const approvalRecords = (params?: any) => request.get('/service/form/records/approval', { params })
export const approve = (acceptNo: string, data: any) => request.post(`/service/form/approve/${acceptNo}`, data)
export const batchApprove = (data: any) => request.post('/service/form/batch-approve', data)

// 进度询问
export const inquiryProgress = (acceptNo: string, content: string) => request.post('/service/form/inquiry', { acceptNo, content })
export const getInquiries = (acceptNo: string) => request.get(`/service/form/inquiries/${acceptNo}`)
export const getAllInquiries = () => request.get('/service/form/inquiries')
export const replyInquiry = (inquiryId: number, reply: string) => request.post(`/service/form/inquiry/${inquiryId}/reply`, { reply })
