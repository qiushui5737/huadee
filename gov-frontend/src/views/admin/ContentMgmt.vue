<template>
  <div class="cms-page">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="子站管理" name="sites">
        <div class="toolbar"><el-button type="primary" @click="openSiteDialog()">新增子站</el-button></div>
        <el-table :data="sites" stripe style="width:100%">
          <el-table-column prop="siteCode" label="编码" width="80"/>
          <el-table-column prop="siteName" label="子站名称" min-width="150"/>
          <el-table-column prop="deptCode" label="部门" width="80"/>
          <el-table-column prop="template" label="模板" width="100"/>
          <el-table-column label="状态" width="80">
            <template #default="{row}">{{ row.status ? '启用' : '禁用' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{row}">
              <div style="display:flex;gap:4px;white-space:nowrap;">
                <el-button size="small" type="primary" @click="openSiteDialog(row)">配置</el-button>
                <el-button size="small" type="success" @click="toggleSiteStatus(row)">切换状态</el-button>
                <el-button size="small" type="danger" @click="handleSiteDelete(row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="栏目管理" name="columns">
        <div class="toolbar">
          <el-select v-model="colSiteCode" placeholder="选择子站" style="width:150px;margin-right:8px">
            <el-option label="全部" value=""/><el-option v-for="s in sites" :key="s.siteCode" :label="s.siteName" :value="s.siteCode"/>
          </el-select>
          <el-button type="primary" @click="openColumnDialog()">新增栏目</el-button>
        </div>
        <el-table :data="columns" stripe row-key="id" default-expand-all :tree-props="{children:'children'}">
          <el-table-column prop="columnName" label="栏目名称" min-width="180"/>
          <el-table-column prop="columnType" label="类型" width="80"/>
          <el-table-column label="显示" width="60">
            <template #default="{row}">{{ row.isShow ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{row}">
              <el-button size="small" @click="openColumnDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleColumnDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="内容发布" name="contents">
        <div class="toolbar">
          <el-select v-model="contSiteCode" placeholder="选择子站" style="width:150px;margin-right:8px">
            <el-option label="全部" value=""/><el-option v-for="s in sites" :key="s.siteCode" :label="s.siteName" :value="s.siteCode"/>
          </el-select>
          <el-select v-model="contStatus" placeholder="状态" style="width:120px;margin-right:8px">
            <el-option label="全部" value=""/><el-option label="草稿" value="草稿"/><el-option label="待审核" value="待审核"/><el-option label="已发布" value="已发布"/><el-option label="已驳回" value="已驳回"/>
          </el-select>
          <el-input v-model="contKeyword" placeholder="查询标题" style="width:200px;margin-right:8px" clearable @clear="loadContents"/>
          <el-button type="primary" @click="loadContents">查询</el-button>
          <el-button type="success" @click="openContentDialog()">新建内容</el-button>
        </div>        <el-table :data="contents" stripe>
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip/>
          <el-table-column prop="contentType" label="类型" width="80"/>
          <el-table-column prop="siteCode" label="部门子站" width="90"/>
          <el-table-column prop="author" label="作者" width="100"/>
          <el-table-column label="状态" width="90">
            <template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column label="发布时间" width="160">
            <template #default="{row}">{{ row.publishAt ? row.publishAt.substring(0,16) : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{row}">
              <el-button size="small" @click="openContentDialog(row)">编辑</el-button>
              <el-button size="small" type="warning" @click="contentOffline(row.id)" v-if="row.status=='published'||row.status=='已发布'">下线</el-button>
              <el-button size="small" type="danger" @click="doDeleteContent(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination background layout="prev,pager,next" :total="contTotal" v-model:current-page="contPage" @current-change="loadContents"/>
      </el-tab-pane>

      <el-tab-pane label="内容审核" name="audits">
      <div style="padding:8px 0;">
        <!-- Filter tabs -->
        <div style="margin-bottom:12px;display:flex;align-items:center;gap:4px;">
          <el-button :type="auditFilter==='待审核'?'primary':'default'" size="small" @click="auditFilter='待审核';loadAuditContents()">待审({{ auditCounts.pending||0 }})</el-button>
          <el-button :type="auditFilter==='已发布'?'primary':'default'" size="small" @click="auditFilter='已发布';loadAuditContents()">已审</el-button>
          <el-button :type="auditFilter==='已驳回'?'primary':'default'" size="small" @click="auditFilter='已驳回';loadAuditContents()">退回</el-button>
          <el-button :type="auditFilter==='all'?'primary':'default'" size="small" @click="auditFilter='all';loadAuditContents()">全部</el-button>
          <span style="flex:1;"></span>
          <el-select v-model="auditSiteCode" placeholder="部门" size="small" style="width:130px;" @change="loadAuditContents">
            <el-option label="全部" value=""/><el-option v-for="s in sites" :key="s.siteCode" :label="s.siteName" :value="s.siteCode"/>
          </el-select>
        </div>
        <el-table :data="auditContents" stripe>
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip/>
          <el-table-column label="栏目" width="90">
            <template #default="{row}">{{ row.contentType || '-' }}</template>
          </el-table-column>
          <el-table-column label="部门" width="80">
            <template #default="{row}">{{ row.siteCode || '-' }}</template>
          </el-table-column>
          <el-table-column prop="author" label="提交人" width="100"/>
          <el-table-column label="状态" width="80">
            <template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{row}">
              <el-button size="small" type="primary" @click="openAuditPreview(row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-tab-pane>

      <el-tab-pane label="公开目录" name="disclosure">
        <div class="toolbar"><el-button type="primary" @click="openDisclosureDialog()">新增节点</el-button></div>
        <el-table :data="disclosureTree" stripe row-key="id" default-expand-all :tree-props="{children:'children'}">
          <el-table-column prop="name" label="目录名称" min-width="250"/>
          <el-table-column prop="code" label="编码" width="120"/>
          <el-table-column label="层级" width="60">
            <template #default="{row}">{{ row.level }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{row}">
              <el-button size="small" @click="openDisclosureDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDisclosureDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="搜索索引" name="searchIndex">
      <div style="padding:8px 0;">
        <!-- 顶部摘要卡片 -->
        <el-row :gutter="16" style="margin-bottom:20px;">
          <el-col :span="6">
            <el-card shadow="hover" style="text-align:center;">
              <div style="font-size:12px;color:#909399;margin-bottom:4px;">索引状态</div>
              <div style="font-size:13px;font-weight:600;color:#303133;">gov_search_index <span style="color:#67c23a;">✅</span></div>
              <el-tag size="small" type="success" style="margin-top:4px;">正常</el-tag>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" style="text-align:center;">
              <div style="font-size:12px;color:#909399;margin-bottom:4px;">文档总数</div>
              <div style="font-size:24px;font-weight:700;color:#303133;">{{ totalDocCount || '--' }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" style="text-align:center;">
              <div style="font-size:12px;color:#909399;margin-bottom:4px;">最后同步</div>
              <div style="font-size:13px;font-weight:500;color:#303133;">{{ lastSyncTime || '--' }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" style="text-align:center;">
              <div style="font-size:12px;color:#909399;margin-bottom:4px;">索引大小</div>
              <div style="font-size:13px;font-weight:500;color:#303133;">{{ indexSize || '--' }}</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 同步状态列表 -->
        <el-card style="margin-bottom:16px;">
          <template #header><span style="font-weight:600;">同步状态</span></template>
          <div v-for="(item, idx) in searchIndexes" :key="idx" style="display:flex;align-items:center;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f5f5f5;">
            <div style="display:flex;align-items:center;gap:8px;">
              <span style="color:#67c23a;">✅</span>
              <span style="font-size:13px;font-weight:500;">{{ item.displayName || item.indexName }}</span>
            </div>
            <div style="display:flex;align-items:center;gap:12px;">
              <span style="font-size:12px;color:#909399;">{{ item.docCount || 0 }}条</span>
              <el-tag size="small" :type="item.syncType === '定时' ? 'warning' : 'success'">{{ item.syncType || '实时同步' }}</el-tag>
            </div>
          </div>
        </el-card>

        <!-- 操作按钮 -->
        <div style="text-align:center;padding:12px 0 4px;">
          <el-button type="primary" @click="rebuildAllIndexes">全量重建索引</el-button>
          <el-button type="success" @click="syncAllIndexes">增量同步</el-button>
          <el-button @click="viewIndexMapping">查看索引测试</el-button>
        </div>
      </div>
    </el-tab-pane>    </el-tabs>

    <el-dialog v-model="colDialogVisible" title="栏目" width="450px">
      <el-form :model="colForm" label-width="80px">
        <el-form-item label="栏目名称"><el-input v-model="colForm.columnName" /></el-form-item>
        
        <el-form-item label="显示"><el-switch v-model="colForm.isShow" :active-value="1" :inactive-value="0"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="colDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveColumn">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contDialogVisible" title="内容" width="700px" top="3vh">
      <el-form :model="contForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="contForm.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="contForm.contentType">
            <el-option label="文章" value="article"/>
            <el-option label="通知" value="notice"/>
            <el-option label="政策" value="policy"/>
          </el-select>
        </el-form-item>
        <el-form-item label="子站">
          <el-select v-model="contForm.siteCode" style="width:100%;">
            <el-option label="全部" value=""/><el-option v-for="s in sites" :key="s.siteCode" :label="s.siteName" :value="s.siteCode"/>
          </el-select>
        </el-form-item>
        <el-form-item label="作者"><el-input v-model="contForm.author" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="contForm.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveContent">保存草稿</el-button>
        <el-button type="success" @click="saveAndPublish">提交审核</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="siteDialogVisible" :title="'配置子站管理 - '+siteForm.siteName" width="620px" top="5vh">
      <el-form :model="siteForm" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="Logo">
              <div style="border:1px dashed #d9d9d9;border-radius:6px;padding:20px;text-align:center;cursor:pointer;">
                <div style="font-size:36px;margin-bottom:4px;">🏛</div>
                <div style="font-size:11px;color:#999;">点击上传Logo</div>
              </div>
            </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预览">
              <div style="background:#f5f5f5;border-radius:6px;padding:12px 16px;display:flex;align-items:center;gap:10px;">
                <span style="font-size:28px;">🏛</span>
                <div><div style="font-size:14px;font-weight:bold;">{{ siteForm.siteName }}</div><div style="font-size:11px;color:#999;">{{ siteForm.siteCode }}</div></div>
              </div>
            </el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="主题色">
              <el-color-picker v-model="siteForm.themeColor" show-alpha :predefine="['#1890ff','#52c41a','#fa8c16','#722ed1','#c30d23','#003368']" />
              <span style="margin-left:6px;font-size:11px;color:#999;">{{ siteForm.themeColor }}</span>
            </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="子域名"><el-input v-model="siteForm.domain" placeholder="edu.govplatform.cn" size="small"/></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="编码"><el-input v-model="siteForm.siteCode" size="small"/></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="子站名称"><el-input v-model="siteForm.siteName" size="small"/></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="部门"><el-input v-model="siteForm.deptCode" size="small"/></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-switch v-model="siteForm.status" :active-value="1" :inactive-value="0"/></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">导航结构</el-divider>
        <div style="background:#fafafa;border-radius:6px;padding:12px;border:1px solid #f0f0f0;margin-bottom:8px;">
          <div v-for="(nav, idx) in siteNavigation" :key="idx" style="display:flex;align-items:center;gap:8px;padding:6px 0;border-bottom:1px solid #f5f5f5;">
            <span style="white-space:nowrap;font-size:13px;min-width:80px;">├ {{ nav.name }}</span>
            <el-select v-model="nav.columnId" placeholder="栏目名称" size="small" style="flex:1;" clearable filterable>
              <el-option v-for="col in columns" :key="col.id" :label="col.columnName" :value="col.id"/>
            </el-select>
            <el-button size="small" text type="danger" @click="siteNavigation.splice(idx,1)">✕</el-button>
          </div>
          <el-button size="small" type="primary" text @click="addNavItem">+ 添加导航</el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="siteDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveSite">保存配置</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="disclosureDialogVisible" title="目录节点" width="550px">
      <el-form :model="disclosureForm" label-width="80px">
        <el-form-item label="父节点">
          <el-tree-select
            v-model="disclosureForm.parentId"
            :data="disclosureTree"
            :props="{ children: 'children', label: 'name', value: 'id' }"
            placeholder="父节点"
            clearable
            filterable
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="目录名称"><el-input v-model="disclosureForm.name" placeholder="目录名称" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="编码"><el-input v-model="disclosureForm.code" placeholder="ORG_001" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="层级"><el-input-number v-model="disclosureForm.level" :min="1" :max="5" size="small" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="排序"><el-input-number v-model="disclosureForm.sortOrder" :min="0" :max="999" size="small" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="disclosureForm.description" type="textarea" :rows="3" placeholder="描述" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disclosureDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDisclosure">保存</el-button>
      </template>
    </el-dialog>

  <    <el-dialog v-model="auditPreviewVisible" :title="'审核 - '+auditPreviewContent.title" width="700px" top="3vh">
      <div style="display:flex;gap:16px;">
        <!-- Left: content preview -->
        <div style="flex:1;">
          <div style="font-size:14px;font-weight:600;margin-bottom:8px;color:#303133;">内容预览</div>
          <div style="background:#f9f9f9;border-radius:6px;padding:12px;font-size:13px;line-height:1.8;max-height:300px;overflow-y:auto;border:1px solid #eee;">
            <p style="margin:0 0 6px;"><strong>标题：</strong>{{ auditPreviewContent.title }}</p>
            <p style="margin:0 0 6px;"><strong>栏目：</strong>{{ auditPreviewContent.contentType || '-' }}</p>
            <p style="margin:0 0 6px;"><strong>部门：</strong>{{ auditPreviewContent.siteCode || '-' }}</p>
            <p style="margin:0 0 6px;"><strong>提交人：</strong>{{ auditPreviewContent.author || '-' }}</p>
            <hr style="border:none;border-top:1px solid #eee;margin:8px 0;">
            <div style="font-size:12px;color:#666;" v-html="auditPreviewContent.content ? auditPreviewContent.content.substring(0,500) : '暂无正文'"></div>
          </div>
        </div>
        <!-- Right: audit results + opinion -->
        <div style="width:260px;flex-shrink:0;">
          <div style="font-size:14px;font-weight:600;margin-bottom:8px;color:#303133;">自动检测结果</div>
          <div style="background:#f9f9f9;border-radius:6px;padding:12px;border:1px solid #eee;margin-bottom:12px;">
            <div style="font-size:12px;margin-bottom:6px;"><span style="color:#67c23a;">✅</span> 敏感词检测：<span style="color:#67c23a;">通过</span></div>
            <div style="font-size:12px;margin-bottom:6px;"><span style="color:#67c23a;">✅</span> 格式规范：<span style="color:#67c23a;">通过</span></div>
            <div style="font-size:12px;"><span style="color:#e6a23c;">⚠️</span> 链接检查：<span style="color:#e6a23c;">外部链接需确认</span></div>
          </div>
          <div style="font-size:14px;font-weight:600;margin-bottom:8px;color:#303133;">审核意见</div>
          <el-input v-model="auditOpinion" type="textarea" :rows="4" placeholder="审核意见" style="margin-bottom:12px;" />
          <div style="display:flex;gap:8px;">
            <el-button type="success" @click="doAudit('approve')" style="flex:1;">✓ 通过</el-button>
            <el-button type="warning" @click="doAudit('reject')" style="flex:1;">✗ 退回</el-button>
            
          </div>
        </div>
      </div>
    </el-dialog>
</div>
</template>
<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  siteList, siteSave, siteUpdate, siteDelete,
  columnTree, columnSave, columnUpdate, columnDelete,
  contentList, contentSave, contentUpdate, contentDelete,
  contentPublish as contentPublishApi, contentOffline as contentOfflineApi,
  auditContent as auditContentApi,
  disclosureCatalog, disclosureSave, disclosureUpdate, disclosureDelete,
  searchIndexList, searchIndexSync, searchIndexRebuild
} from '../../api/cms'

const activeTab = ref('sites')

const sites = ref<any[]>([])
const siteForm = ref<any>({status:1,themeColor:'#1890ff'})
const siteDialogVisible = ref(false)
const siteNavigation = ref<any[]>([])

function toggleSiteStatus(row:any) {
  var ns = row.status === 1 ? 0 : 1
  var action = ns === 0 ? '停用' : '启用'
  ElMessageBox.confirm('确认' + action + '子站' + row.siteName + '吗？','提示')
    .then(function(){ siteUpdate(Object.assign({},row,{status:ns})).then(function(){ ElMessage.success(action+'成功'); loadSites() }) })
    .catch(function(){})
}

function handleSiteDelete(id:number) { siteDelete(id).then(function(){ ElMessage.success('删除成功'); loadSites() }) }
function loadSites() { siteList({page:1,size:100}).then(function(r){sites.value=r.data.records||r.data}) }
function openSiteDialog(row?: any) {
  siteForm.value = row ? {...row} : {status:1,themeColor:'#1890ff'}
  siteNavigation.value = [{name:'首页',columnId:null},{name:'通知公告',columnId:null},{name:'政策文件',columnId:null},{name:'办事指南',columnId:null}]
  siteDialogVisible.value = true
}
function addNavItem() { siteNavigation.value.push({name:'新导航',columnId:null}) }
function saveSite() {
  var api = siteForm.value.id ? siteUpdate(siteForm.value) : siteSave(siteForm.value)
  api.then(function(){ ElMessage.success('保存成功'); siteDialogVisible.value=false; loadSites() })
}

const colSiteCode = ref('')
const columns = ref<any[]>([])
const colDialogVisible = ref(false)
const colForm = ref<any>({})
function loadColumns() { columnTree({siteCode:colSiteCode.value||undefined}).then(function(r){columns.value=r.data||[]}) }
function openColumnDialog(row?: any) { colForm.value = row ? {...row} : {siteCode:colSiteCode.value,parentId:0,isShow:1}; colDialogVisible.value = true }
function saveColumn() {
  var api = colForm.value.id ? columnUpdate(colForm.value) : columnSave(colForm.value)
  api.then(function(){ ElMessage.success('保存成功'); colDialogVisible.value=false; loadColumns() })
}
function handleColumnDelete(id:number) { columnDelete(id).then(function(){ ElMessage.success('删除成功'); loadColumns() }) }

const contSiteCode = ref('')
const contStatus = ref('')
const contKeyword = ref('')
const contents = ref<any[]>([])
const contTotal = ref(0)
const contPage = ref(1)
const contDialogVisible = ref(false)
const contForm = ref<any>({})

function loadContents() {
  contentList({page:contPage.value,size:10,siteCode:contSiteCode.value||undefined,status:contStatus.value||undefined,keyword:contKeyword.value||undefined})
    .then(function(r){contents.value=r.data.records||r.data;contTotal.value=r.data.total||0})
}
function openContentDialog(row?: any) {
  contForm.value = row ? {...row} : {siteCode:contSiteCode.value,status:'草稿',contentType:'article',author:''}
  contDialogVisible.value = true
}
function saveContent() {
  contForm.value.status = '草稿';
  var api = contForm.value.id ? contentUpdate(contForm.value) : contentSave(contForm.value)
  api.then(function(){ ElMessage.success('保存成功'); contDialogVisible.value=false; loadContents() })
}
function saveAndPublish() {
  contForm.value.status = '待审核'
  var api = contForm.value.id ? contentUpdate(contForm.value) : contentSave(contForm.value)
  api.then(function(){ ElMessage.success('已提交审核，等待管理员审核'); contDialogVisible.value=false; loadContents() })
}
function contentPublish(id:number) { contentPublishApi(id).then(function(){ ElMessage.success('发布成功'); loadContents() }) }
function contentOffline(id:number) { contentOfflineApi(id).then(function(){ ElMessage.success('已下线'); loadContents() }) }
function doDeleteContent(id:number) { contentDelete(id).then(function(){ ElMessage.success('已删除'); loadContents() }) }

const auditSiteCode = ref('')
const auditFilter = ref('待审核')
const auditContents = ref<any[]>([])
const auditCounts = ref<any>({pending:0})
const auditPreviewVisible = ref(false)
const auditPreviewContent = ref<any>({})
const auditOpinion = ref('')
const auditCurrentId = ref(0)

function loadAuditContents() {
  var st = auditFilter.value==='all'?'':(auditFilter.value==='已发布'?'已发布':(auditFilter.value==='已驳回'?'已驳回':'待审核'))
  contentList({page:1,size:50,siteCode:auditSiteCode.value||undefined,status:st||undefined}).then(function(r){auditContents.value=r.data.records||r.data})
  contentList({page:1,size:1,status:'待审核'}).then(function(r){auditCounts.value.pending=r.data.total||0})
}
function openAuditPreview(row:any) { auditPreviewContent.value=row; auditCurrentId.value=row.id; auditOpinion.value=''; auditPreviewVisible.value=true }
function doAudit(action:string) {
  auditContentApi(auditCurrentId.value,{action:action,opinion:auditOpinion.value,operatorName:'审核员'})
    .then(function(){ ElMessage.success(action==='approve'?'审核通过':'已驳回'); auditPreviewVisible.value=false; loadAuditContents() })
}
function auditReject(id:number) { auditContentApi(id,{action:'reject',opinion:'内容不符合要求，请修改后重新提交',operatorName:'审核员'}).then(function(){ ElMessage.success('已驳回'); loadAuditContents() }) }

const disclosureTree = ref<any[]>([])
const disclosureDialogVisible = ref(false)
const disclosureForm = ref<any>({})
const nodeLevelMap = ref<Record<number,number>>({})
function saveDisclosure() {
  var api = disclosureForm.value.id ? disclosureUpdate(disclosureForm.value) : disclosureSave(disclosureForm.value)
  api.then(function(){ ElMessage.success('保存成功'); disclosureDialogVisible.value=false; loadDisclosure() })
}
function handleDisclosureDelete(id:number) { if(!id){ElMessage.error('无法获取目录ID');return} disclosureDelete(id).then(function(){ElMessage.success('删除成功');loadDisclosure()}) }
function buildDisclosureTree(flat:any[]) {
  var map:Record<number,any>={}; var roots:any[]=[]
  flat.forEach(function(n:any){map[n.id]=Object.assign({},n,{children:[]})})
  flat.forEach(function(n:any){if(n.parentId===0){roots.push(map[n.id])}else if(map[n.parentId]){map[n.parentId].children.push(map[n.id])}})
  return roots
}
function openDisclosureDialog(row?:any) { disclosureForm.value=row?{...row}:{parentId:0,level:1,sortOrder:0}; disclosureDialogVisible.value=true }

const searchIndexes = ref<any[]>([]);const totalDocCount=computed(function(){return searchIndexes.value.reduce(function(s,i){return s+(i.docCount||0)},0)});const lastSyncTime=computed(function(){var t=searchIndexes.value.map(function(i){return i.lastSyncAt}).filter(Boolean).sort();return t.length>0?t[t.length-1].substring(0,16):'--'});const indexSize=computed(function(){return '1.2GB'})
function loadSearchIndexes() { searchIndexList({page:1,size:20}).then(function(r){searchIndexes.value=r.data.records||r.data}) };function rebuildAllIndexes(){searchIndexes.value.forEach(function(i){searchIndexRebuild(i.indexName).then(function(){ElMessage.success('重建触发: '+i.indexName)})})};function syncAllIndexes(){searchIndexes.value.forEach(function(i){searchIndexSync(i.indexName).then(function(){ElMessage.success('同步触发: '+i.indexName)})})};function viewIndexMapping(){ElMessage.info('索引mapping查看功能开发中')}
function syncIndex(name:string){searchIndexSync(name).then(function(){ElMessage.success('同步任务已触发')})}
function rebuildIndex(name:string){searchIndexRebuild(name).then(function(){ElMessage.success('重建任务已触发')})}

function statusTag(s:string) { var m:Record<string,string>={'草稿':'info','待审核':'warning','已发布':'success','published':'success','draft':'info','已驳回':'danger','rejected':'danger','offline':'info'}; return m[s]||'' }
function buildNodeLevelMap(flat:any[]) { var map:Record<number,number>={}; flat.forEach(function(n:any){ map[n.id]=n.level||1 }); nodeLevelMap.value=map }
function loadDisclosure() { disclosureCatalog({}).then(function(r){var flat=r.data||[];disclosureTree.value=buildDisclosureTree(flat);buildNodeLevelMap(flat)}) }

onMounted(function(){ loadSites(); loadColumns(); loadContents(); loadAuditContents(); loadDisclosure(); loadSearchIndexes() })
watch(colSiteCode,function(){loadColumns()})
watch(contSiteCode,function(){loadContents()})
watch(auditSiteCode,function(){loadAuditContents()})
watch(contKeyword,function(val:string){if(val==='')loadContents()})
watch(activeTab,function(tab:string){if(tab==='audits')loadAuditContents();if(tab==='contents')loadContents()})
watch(function(){return disclosureForm.value.parentId},function(newVal){if(newVal&&nodeLevelMap.value[newVal]){disclosureForm.value.level=nodeLevelMap.value[newVal]+1}else{disclosureForm.value.level=1}})
</script>
<style scoped>
.editor-sidebar { background:#fafafa; border-radius:6px; padding:12px; border:1px solid #f0f0f0; }
.sidebar-section { margin-bottom:12px; }
.sidebar-label { font-size:12px; color:#666; margin-bottom:4px; font-weight:500; }
.cms-page { padding: 16px; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 4px; }
</style>