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
export const userReplyMessage = (id: number, data: any) => request.post(`/interaction/message/${id}/user-reply`, data)

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

// C6-我要咨询
export const submitConsultation = (data: any) => request.post('/consultation', data)
export const consultationProgress = (consultNo: string) => request.get(`/consultation/progress/${consultNo}`)
export const consultationList = (params: any) => request.get('/consultation/list', { params })
export const replyConsultation = (consultNo: string, data: any) => request.post(`/consultation/${consultNo}/reply`, data)
export const updateConsultationStatus = (consultNo: string, data: any) => request.patch(`/consultation/${consultNo}/status`, data)
export const finishConsultation = (consultNo: string) => request.post(`/consultation/${consultNo}/finish`)
export const consultationStats = () => request.get('/consultation/stats')

// C7-我要建议
export const submitSuggestion = (data: any) => request.post('/suggestion', data)
export const suggestionProgress = (suggestNo: string) => request.get(`/suggestion/progress/${suggestNo}`)
export const suggestionList = (params: any) => request.get('/suggestion/list', { params })
export const replySuggestion = (suggestNo: string, data: any) => request.post(`/suggestion/${suggestNo}/reply`, data)
export const updateSuggestionStatus = (suggestNo: string, data: any) => request.patch(`/suggestion/${suggestNo}/status`, data)
export const finishSuggestion = (suggestNo: string) => request.post(`/suggestion/${suggestNo}/finish`)
export const suggestionStats = () => request.get('/suggestion/stats')

// C8-我要投诉
export const submitComplaint = (data: any) => request.post('/complaint', data)
export const complaintProgress = (complaintNo: string) => request.get(`/complaint/progress/${complaintNo}`)
export const complaintList = (params: any) => request.get('/complaint/list', { params })
export const replyComplaint = (complaintNo: string, data: any) => request.post(`/complaint/${complaintNo}/reply`, data)
export const updateComplaintStatus = (complaintNo: string, data: any) => request.patch(`/complaint/${complaintNo}/status`, data)
export const finishComplaint = (complaintNo: string) => request.post(`/complaint/${complaintNo}/finish`)
export const complaintStats = () => request.get('/complaint/stats')

// C9-意见征集（C端）
export const collectionPublicList = (params: any) => request.get('/collection/public', { params })
export const collectionPublicDetail = (id: number) => request.get(`/collection/public/${id}`)
export const collectionPublicFeedback = (id: number) => request.get(`/collection/public/${id}/feedback`)
export const submitCollectionOpinion = (id: number, data: any) => request.post(`/collection/${id}/opinion`, data)

// B端-意见征集管理
export const collectionList = (params: any) => request.get('/collection', { params })
export const createCollection = (data: any) => request.post('/collection', data)
export const updateCollection = (id: number, data: any) => request.put(`/collection/${id}`, data)
export const deleteCollection = (id: number) => request.delete(`/collection/${id}`)
export const publishCollection = (id: number) => request.post(`/collection/${id}/publish`)
export const finishCollection = (id: number) => request.post(`/collection/${id}/finish`)
export const addCollectionFeedback = (id: number, data: any) => request.post(`/collection/${id}/feedback`, data)
export const collectionOpinions = (id: number, params: any) => request.get(`/collection/${id}/opinions`, { params })
export const replyCollectionOpinion = (opinionId: number, data: any) => request.post(`/collection/opinion/${opinionId}/reply`, data)
export const collectionOpinionStats = (id: number) => request.get(`/collection/${id}/opinion-stats`)
