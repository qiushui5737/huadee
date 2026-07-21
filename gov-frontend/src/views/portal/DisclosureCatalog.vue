<template>
  <div style="background:#f5f7fa;min-height:100vh;padding:24px 40px;font-family:'Microsoft YaHei',sans-serif;">
    <h1 style="font-size:22px;font-weight:700;color:#1a1a1a;margin:0 0 16px 0;">信息公开目录 - {{ siteName || '政府部门' }}</h1>
    
    <div id="two-panel" style="display:flex;flex-direction:row;gap:16px;">
      <!-- LEFT: tree panel -->
      <div style="width:280px;flex-shrink:0;background:#fff;border-radius:8px;border:1px solid #d0d5dd;overflow:hidden;">
        <div style="padding:12px 16px;font-size:15px;font-weight:600;border-bottom:1px solid #eee;background:#f9fafb;">目录导航</div>
        <div style="padding:8px 12px;border-bottom:1px solid #eee;">
          <el-input v-model="filterText" placeholder="目录导航" size="small" clearable />
        </div>
        <div style="padding:4px 0;max-height:600px;overflow-y:auto;">
          <el-tree
            :data="treeData"
            :props="{children:'children',label:'name'}"
            node-key="id"
            :filter-node-method="filterNode"
            highlight-current
            @node-click="handleClick"
            default-expand-all
          >
            <template #default="{node,data}">
              <span style="font-size:13px;color:#333;">
                <span v-if="data.children&&data.children.length>0" style="font-size:10px;color:#999;width:14px;display:inline-block;">{{ node.expanded?'▼':'▶' }}</span>
                <span v-else style="display:inline-block;width:14px;"></span>
                <span v-if="data.description" style="font-size:13px;">📄</span>
                <span v-else style="font-size:13px;color:#999;">📁</span>
                {{ data.name }}
              </span>
            </template>
          </el-tree>
        </div>
      </div>
      
      <!-- RIGHT: content panel (always renders) -->
      <div style="flex:1;min-width:300px;background:#fff;border-radius:8px;border:1px solid #d0d5dd;padding:24px;min-height:300px;">
        <div v-if="currentNode">
          <div style="font-size:13px;color:#999;padding-bottom:12px;border-bottom:1px solid #eee;margin-bottom:16px;">
            当前位置：信息公开目录 &gt; <span style="color:#1890ff;">{{ currentNode.name }}</span>
          </div>
          <h2 style="font-size:20px;font-weight:700;color:#1a1a1a;margin:0 0 12px;">{{ currentNode.name }}</h2>
          <div style="display:flex;gap:20px;font-size:13px;color:#909399;margin-bottom:12px;flex-wrap:wrap;">
            <span>编码：{{ currentNode.code || '-' }}</span>
            <span>发布部门：{{ currentNode.deptCode || '政府部门' }}</span>
            <span>更新日期：{{ currentNode.updatedAt ? currentNode.updatedAt.substring(0,10) : '2026-07-17' }}</span>
          </div>
          <hr style="border:none;border-top:1px solid #eee;margin:12px 0;">
          <p style="font-size:15px;line-height:1.9;color:#333;min-height:80px;">{{ currentNode.description || '暂无目录内容' }}</p>
          <div style="margin-top:20px;padding-top:16px;border-top:1px solid #eee;display:flex;gap:8px;">
            <el-button size="small" @click="ElMessage.success('下载功能开发中')">📥 下载</el-button>
            <el-button size="small" @click="doPrint">🖨️ 打印</el-button>
          </div>
        </div>
        <div v-if="!currentNode" style="text-align:center;padding:80px 0;color:#bbb;font-size:14px;">
          请选择节点
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { disclosureCatalog, siteGetByCode } from '../../api/cms'

const route = useRoute()
const filterText = ref('')
const treeData = ref<any[]>([])
const currentNode = ref<any>(null)
const siteName = ref('')
function doPrint() { window.print() }
function handleClick(d){currentNode.value=d;siteName.value='['+d.name+']'};function filterNode(v,d){if(!v)return true;return d.name.toLowerCase().includes(v.toLowerCase())};
watch(filterText, function(val) { 
  // Filter handled inline in el-tree's filter-node-method
})

onMounted(function() {
  var sc = route.query.site as string
  if (sc) { siteGetByCode(sc).then(function(r) { siteName.value = r.data?.siteName || '' }) }
  disclosureCatalog({}).then(function(r) {
    var flat = r.data || []
    var map:any = {}; var roots:any = []
    flat.forEach(function(n:any){map[n.id]=Object.assign({},n,{children:[]})})
    flat.forEach(function(n:any){if(n.parentId===0){roots.push(map[n.id])}else if(map[n.parentId]){map[n.parentId].children.push(map[n.id])}})
    treeData.value = roots
  })
})
</script>