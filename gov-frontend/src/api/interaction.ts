import request from '@/utils/request'

// C1-留言系统
export const submitMessage = (data: any) => request.post('/interaction/message', data)
export const messageList = (params: any) => request.get('/interaction/message', { params })
export const messageDetail = (id: number) => request.get(`/interaction/message/${id}`)
export const hotMessages = () => request.get('/interaction/message/hot')
export const messageStats = () => request.get('/interaction/message/stats')
export const replyMessage = (id: number, data: any) => request.post(`/interaction/message/${id}/reply`, data)
export const dispatchMessage = (id: number, data: any) => request.post(`/interaction/message/${id}/dispatch`, data)
export const finishMessage = (id: number) => request.post(`/interaction/message/${id}/finish`)
export const superviseMessage = (id: number, data: any) => request.post(`/interaction/message/${id}/supervise`, data)
export const rateMessage = (id: number, data: any) => request.post(`/interaction/message/${id}/rate`, data)

// C3-依申请公开
export const applyDisclosure = (data: any) => request.post('/disclosure/apply', data)
export const disclosureProgress = (applyNo: string) => request.get(`/disclosure/progress/${applyNo}`)
export const disclosureList = (params: any) => request.get('/disclosure/list', { params })
export const auditDisclosure = (applyNo: string, data: any) => request.post(`/disclosure/audit/${applyNo}`, data)
export const disclosureStats = () => request.get('/disclosure/stats')

// C5-民意调查问卷
export const questionnaireList = (params: any) => request.get('/interaction/questionnaire', { params })
export const questionnaireDetail = (id: number) => request.get(`/interaction/questionnaire/${id}`)
export const createQuestionnaire = (data: any) => request.post('/interaction/questionnaire', data)
export const publishQuestionnaire = (id: number) => request.post(`/interaction/questionnaire/${id}/publish`)
export const closeQuestionnaire = (id: number) => request.post(`/interaction/questionnaire/${id}/close`)
export const submitQuestionnaire = (id: number, data: any) => request.post(`/interaction/questionnaire/${id}/submit`, data)
export const questionnaireStatistics = (id: number) => request.get(`/interaction/questionnaire/${id}/statistics`)
