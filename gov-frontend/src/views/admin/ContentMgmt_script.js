// ===== 导入 =====
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  siteList, siteSave, siteUpdate, siteDelete,
  columnTree, columnSave, columnUpdate, columnDelete,
  contentList, contentSave, contentUpdate, contentDelete,
  pubApi, offApi,
  auditContent, auditHistory,
  disclosureCatalog, disclosureSave, disclosureUpdate, disclosureDelete,
  searchIndexList, searchIndexSync, searchIndexRebuild
} from '../../api/cms'

// ===== D1 子站 =====
const sites = ref<any[]>([])
const siteForm = ref<any>({status:1,themeColor:'#1890ff'})
const siteDialogVisible = ref(false)
const siteNavigation = ref<any[]>([])

function toggleSiteStatus(row:any) {
  var newStatus = row.status === 1 ? 0 : 1
  var action = newStatus === 0 ? '停用' : '启用'
  ElMessageBox.confirm('确认' + action + '子站' + row.siteName + '吗？', '提示')
    .then(function() {
      siteUpdate(Object.assign({}, row, {status:newStatus}))
        .then(function() {
          ElMessage.success(action + '成功')
          loadSites()
        })
    })
    .catch(function(){})
}

function handleSiteDelete(id:number) {
  siteDelete(id).then(function() {
    ElMessage.success('删除成功')
    loadSites()
  })
}

function loadSites() {
  siteList({page:1,size:100}).then(function(r){sites.value=r.data.records||r.data})
}

function openSiteDialog(row?: any) {
  siteForm.value = row ? {...row} : {status:1,themeColor:'#1890ff'}
  siteNavigation.value = [
    {name:'首页',columnId:null},
    {name:'通知公告',columnId:null},
    {name:'政策文件',columnId:null},
    {name:'办事指南',columnId:null}
  ]
  siteDialogVisible.value = true
}

function addNavItem() { siteNavigation.value.push({name:'新导航',columnId:null}) }

function saveSite() {
  const api = siteForm.value.id ? siteUpdate(siteForm.value) : siteSave(siteForm.value)
  api.then(function() {
    ElMessage.success('保存成功')
    siteDialogVisible.value = false
    loadSites()
  })
}

// ===== D2 栏目 =====
const colSiteCode = ref('')
const columns = ref<any[]>([])
const colDialogVisible = ref(false)
const colForm = ref<any>({})

function loadColumns() {
  columnTree({siteCode:colSiteCode.value||undefined}).then(function(r){columns.value=r.data||[]})
}

function openColumnDialog(row?: any) {
  colForm.value = row ? {...row} : {siteCode:colSiteCode.value,parentId:0,isShow:1}
  colDialogVisible.value = true
}

function saveColumn() {
  const api = colForm.value.id ? columnUpdate(colForm.value) : columnSave(colForm.value)
  api.then(function() {
    ElMessage.success('保存成功')
    colDialogVisible.value = false
    loadColumns()
  })
}

function handleColumnDelete(id:number) {
  columnDelete(id).then(function() {
    ElMessage.success('删除成功')
    loadColumns()
  })
}

// ===== D2 内容 =====
const contSiteCode = ref('')
const contStatus = ref('')
const contKeyword = ref('')
const contents = ref<any[]>([])
const contTotal = ref(0)
const contPage = ref(1)
const contDialogVisible = ref(false)
const contForm = ref<any>({})

function loadContents() {
  contentList({
    page:contPage.value,size:10,
    siteCode:contSiteCode.value||undefined,
    status:contStatus.value||undefined,
    keyword:contKeyword.value||undefined
  }).then(function(r) {
    contents.value = r.data.records || r.data
    contTotal.value = r.data.total || 0
  })
}

function openContentDialog(row?: any) {
  if (row) {
    contForm.value = {...row}
  } else {
    contForm.value = {siteCode:contSiteCode.value,status:'draft',contentType:'article'}
  }
  contDialogVisible.value = true
}

function saveContent() {
  const api = contForm.value.id ? contentUpdate(contForm.value) : contentSave(contForm.value)
  api.then(function() {
    ElMessage.success('保存成功')
    contDialogVisible.value = false
    loadContents()
  })
}

function saveAndPublish() {
  contForm.value.status = '待审核'
  const saveApi = contForm.value.id ? contentUpdate(contForm.value) : contentSave(contForm.value)
  saveApi.then(function() {
    ElMessage.success('已提交审核，等待管理员审核')
    contDialogVisible.value = false
    loadContents()
  })
}

function contentPublish(id:number) {
  pubApi(id).then(function() {
    ElMessage.success('发布成功')
    loadContents()
  })
}

function contentOffline(id:number) {
  offApi(id).then(function() {
    ElMessage.success('已下线')
    loadContents()
  })
}

function doDeleteContent(id:number) {
  contentDelete(id).then(function() {
    ElMessage.success('已删除')
    loadContents()
  })
}

// ===== D3 审核 =====
const auditSiteCode = ref('')
const auditContents = ref<any[]>([])
const auditPreviewVisible = ref(false)
const auditPreviewContent = ref<any>({})
const auditOpinion = ref('')
const auditCurrentId = ref(0)

function loadAuditContents() {
  contentList({page:1,size:50,siteCode:auditSiteCode.value||undefined,status:'待审核'})
    .then(function(r) { auditContents.value = r.data.records || r.data })
}

function openAuditPreview(row:any) {
  auditPreviewContent.value = row
  auditCurrentId.value = row.id
  auditOpinion.value = ''
  auditPreviewVisible.value = true
}

function doAudit(action:string) {
  auditContent(auditCurrentId.value, {action:action, opinion:auditOpinion.value, operatorName:'审核员'})
    .then(function() {
      ElMessage.success(action === 'approve' ? '审核通过' : '已驳回')
      auditPreviewVisible.value = false
      loadAuditContents()
    })
}

function auditReject(id:number) {
  auditContent(id, {action:'reject', opinion:'内容不符合要求，请修改后重新提交', operatorName:'审核员'})
    .then(function() {
      ElMessage.success('已驳回')
      loadAuditContents()
    })
}

function auditContent2(id:number, action:string, opinion:string) {
  auditContent(id, {action:action, opinion:opinion, operatorName:'审核员'})
    .then(function() {
      ElMessage.success('审核完成')
      loadAuditContents()
    })
}

// ===== D4 公开目录 =====
const disclosureTree = ref<any[]>([])
const disclosureDialogVisible = ref(false)
const disclosureForm = ref<any>({})

function saveDisclosure() {
  const api = disclosureForm.value.id ? disclosureUpdate(disclosureForm.value) : disclosureSave(disclosureForm.value)
  api.then(function() {
    ElMessage.success('保存成功')
    disclosureDialogVisible.value = false
    loadDisclosure()
  })
}

function handleDisclosureDelete(id:number) {
  if (!id) { ElMessage.error('无法获取目录ID'); return }
  disclosureDelete(id).then(function() {
    ElMessage.success('删除成功')
    loadDisclosure()
  })
}

function buildDisclosureTree(flat: any[]) {
  const map: Record<number,any> = {}
  const roots: any[] = []
  flat.forEach(function(n: any) { map[n.id] = Object.assign({}, n, {children:[]}) })
  flat.forEach(function(n: any) {
    if (n.parentId === 0) { roots.push(map[n.id]) }
    else if (map[n.parentId]) { map[n.parentId].children.push(map[n.id]) }
  })
  return roots
}

function openDisclosureDialog(row?: any) {
  disclosureForm.value = row ? {...row} : {parentId:0, level:1, sortOrder:0}
  disclosureDialogVisible.value = true
}

// ===== D5 搜索索引 =====
const searchIndexes = ref<any[]>([])
function loadSearchIndexes() {
  searchIndexList({page:1, size:20}).then(function(r) { searchIndexes.value = r.data.records || r.data })
}
function syncIndex(name:string) { searchIndexSync(name).then(function() { ElMessage.success('同步任务已触发') }) }
function rebuildIndex(name:string) { searchIndexRebuild(name).then(function() { ElMessage.success('重建任务已触发') }) }

// ===== 工具函数 =====
function statusTag(s:string) {
  const m: Record<string,string> = {
    'draft':'info', '待审核':'warning', '初审':'warning', '复审':'warning',
    'published':'success', 'rejected':'danger', 'offline':'info'
  }
  return m[s] || ''
}

function loadDisclosure() {
  disclosureCatalog({}).then(function(r) {
    const flat = r.data || []
    disclosureTree.value = buildDisclosureTree(flat)
  })
}

onMounted(function() {
  loadSites()
  loadColumns()
  loadContents()
  loadAuditContents()
  loadDisclosure()
  loadSearchIndexes()
})

watch(colSiteCode, function() { loadColumns() })
watch(contSiteCode, function() { loadContents() })
watch(auditSiteCode, function() { loadAuditContents() })
watch(contKeyword, function(val: string) { if (val === '') loadContents() })
watch(activeTab, function(tab: string) {
  if (tab === 'audits') loadAuditContents()
  if (tab === 'contents') loadContents()
})
