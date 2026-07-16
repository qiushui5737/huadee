import request from '@/utils/request'

// C1-留言系统
export const submitMessage = (data: any) => request.post('/interaction/message', data)
export const messageList = (params: any) => request.get('/interaction/message', { params })
export const hotMessages = () => request.get('/interaction/message/hot')
export const replyMessage = (id: number, data: any) => request.post(`/interaction/message/${id}/reply`, data)
export const dispatchMessage = (id: number, data: any) => request.post(`/interaction/message/${id}/dispatch`, data)
export const finishMessage = (id: number) => request.post(`/interaction/message/${id}/finish`)

// C3-依申请公开
export const applyDisclosure = (data: any) => request.post('/disclosure/apply', data)
export const disclosureProgress = (applyNo: string) => request.get(`/disclosure/progress/${applyNo}`)
export const disclosureList = (params: any) => request.get('/disclosure/list', { params })
export const auditDisclosure = (applyNo: string, data: any) => request.post(`/disclosure/audit/${applyNo}`, data)
