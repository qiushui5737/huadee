import request from '@/utils/request'

// ===== D1 部门子站管理 =====
export const siteList = (params: any) => request.get('/cms/site/list', { params })
export const siteGet = (id: number) => request.get(`/cms/site/${id}`)
export const siteGetByCode = (siteCode: string) => request.get(`/cms/site/by-code/${siteCode}`)
export const siteSave = (data: any) => request.post('/cms/site', data)
export const siteUpdate = (data: any) => request.put('/cms/site', data)
export const siteDelete = (id: number) => request.delete(`/cms/site/${id}`)

// ===== D2 栏目管理 =====
export const columnList = (params: any) => request.get('/cms/column/list', { params })
export const columnTree = (params: any) => request.get('/cms/column/tree', { params })
export const columnSave = (data: any) => request.post('/cms/column', data)
export const columnUpdate = (data: any) => request.put('/cms/column', data)
export const columnDelete = (id: number) => request.delete(`/cms/column/${id}`)

// ===== D2 内容发布 =====
export const contentList = (params: any) => request.get('/cms/content/list', { params })
export const contentGet = (id: number) => request.get(`/cms/content/${id}`)
export const contentSave = (data: any) => request.post('/cms/content', data)
export const contentUpdate = (data: any) => request.put('/cms/content', data)
export const contentDelete = (id: number) => request.delete(`/cms/content/${id}`)
export const contentPublish = (id: number) => request.post(`/cms/content/${id}/publish`)
export const contentOffline = (id: number) => request.post(`/cms/content/${id}/offline`)

// ===== D3 内容审核 =====
export const auditList = (params: any) => request.get('/cms/audit/list', { params })
export const auditContent = (id: number, data: any) => request.post(`/cms/audit/${id}`, data)

// ===== D4 信息公开目录 =====
export const disclosureCatalog = (params: any) => request.get('/cms/disclosure/catalog', { params })
export const disclosureSave = (data: any) => request.post('/cms/disclosure', data)
export const disclosureUpdate = (data: any) => request.put('/cms/disclosure', data)
export const disclosureDelete = (id: number) => request.delete(`/cms/disclosure/${id}`)

// ===== D5 搜索索引管理 =====
export const searchIndexList = (params: any) => request.get('/cms/search-index/list', { params })
export const searchIndexSync = (indexName: string) => request.post(`/cms/search-index/sync/${indexName}`)
export const searchIndexRebuild = (indexName: string) => request.post(`/cms/search-index/rebuild/${indexName}`)
// ===== 兼容旧接口 (Home.vue 等页面使用) =====
export const sites = () => siteList({})
export const hotWords = () => request.get('/ai/search/hot')

export const disclosureNode = (id: number) => request.get('/cms/disclosure/catalog/' + id)
