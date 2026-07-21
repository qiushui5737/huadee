import request from '@/utils/request'

// D1-部门子站 & D2-CMS内容
export const sites = () => request.get('/cms/sites')
export const contentList = (params: any) => request.get('/cms/content/list', { params })
export const publishContent = (data: any) => request.post('/cms/content', data)
export const auditContent = (id: number, data: any) => request.post(`/cms/audit/${id}`, data)
export const disclosureCatalog = () => request.get('/cms/disclosure/catalog')
