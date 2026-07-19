<template>
  <div class="service-mgmt-page">
    <div class="page-header">
      <div class="header-title">
        <h2>服务事项管理</h2>
        <p>管理本部门的办事服务事项，支持添加、修改、删除操作</p>
      </div>
      <el-button type="primary" @click="addDialogVisible = true">
        <el-icon><Plus /></el-icon> 添加服务
      </el-button>
    </div>

    <div class="page-content">
      <div class="filter-bar">
        <el-select v-model="categoryFilter" placeholder="全部分类" style="width:150px;" @change="loadItems">
          <el-option label="全部" value="" />
          <el-option v-for="cat in categoriesList" :key="cat.code" :label="cat.name" :value="cat.code" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索事项名称" style="width:250px; margin-left:10px;" @keyup.enter="loadItems">
          <template #append>
            <el-button @click="loadItems"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="loadItems">查询</el-button>
      </div>

      <el-card class="item-card">
        <el-table :data="itemsList" border :loading="loading">
          <el-table-column prop="itemCode" label="事项编码" width="150" />
          <el-table-column prop="itemName" label="事项名称" min-width="180" />
          <el-table-column prop="category" label="分类" width="100">
            <template #default="scope">
              <el-tag size="small">{{ getCategoryName(scope.row.category) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deptCode" label="承办部门" width="100">
            <template #default="scope">
              <el-tag size="small">{{ getDeptName(scope.row.deptCode) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200">
            <template #default="scope">
              <span :title="scope.row.description">{{ scope.row.description?.length > 50 ? scope.row.description.substring(0, 50) + '...' : scope.row.description }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="缴费金额" width="110" align="center">
            <template #default="scope">
              <span v-if="scope.row.price && scope.row.price > 0" style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.price }}</span>
              <el-tag v-else size="small" type="success">免费</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? '上线' : '下线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewItem(scope.row)">详情</el-button>
              <el-button size="small" @click="editItem(scope.row)">修改</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="detailDialogVisible" title="事项详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="事项编码" :span="2">{{ detailItem.itemCode }}</el-descriptions-item>
        <el-descriptions-item label="事项名称">{{ detailItem.itemName }}</el-descriptions-item>
        <el-descriptions-item label="所属分类">{{ getCategoryName(detailItem.category) }}</el-descriptions-item>
        <el-descriptions-item label="承办部门">{{ getDeptName(detailItem.deptCode) }}</el-descriptions-item>
        <el-descriptions-item label="事项描述" :span="2">{{ detailItem.description }}</el-descriptions-item>
        <el-descriptions-item label="缴费金额">
          <span v-if="detailItem.price && detailItem.price > 0" style="color: #f56c6c; font-weight: bold;">¥{{ detailItem.price }}</span>
          <span v-else>免费办理</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请材料" :span="2">
          <div v-if="detailItem.formSchema" class="material-list">
            <div v-for="(material, index) in parseFormSchema(detailItem.formSchema)" :key="index" class="material-item">
              <el-icon><Document /></el-icon>
              <span>{{ material }}</span>
            </div>
            <div v-if="parseFormSchema(detailItem.formSchema).length === 0" class="empty-material">
              暂无申请材料要求
            </div>
          </div>
          <span v-else class="empty-material">暂无申请材料要求</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailItem.status === 1 ? 'success' : 'danger'">{{ detailItem.status === 1 ? '上线' : '下线' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailItem.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editItem(detailItem)">修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addDialogVisible" :title="isEdit ? '修改服务' : '添加服务'" width="650px">
      <el-form :model="formData" label-width="100px" class="service-form">
        <el-form-item label="事项编码" required>
          <el-input v-model="formData.itemCode" :disabled="isEdit" placeholder="请输入事项编码" />
        </el-form-item>
        <el-form-item label="事项名称" required>
          <el-input v-model="formData.itemName" placeholder="请输入事项名称" />
        </el-form-item>
        <el-form-item label="所属分类" required>
          <el-select v-model="formData.category" placeholder="请选择分类">
            <el-option v-for="cat in categoriesList" :key="cat.code" :label="cat.name" :value="cat.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="承办部门">
          <el-select v-model="formData.deptCode" :disabled="!isAdmin" placeholder="请选择部门">
            <el-option v-for="dept in deptList" :key="dept.code" :label="dept.name" :value="dept.code" />
          </el-select>
          <span v-if="!isAdmin" class="form-tip">当前用户只能创建本部门的服务</span>
        </el-form-item>
        <el-form-item label="事项描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入事项描述" />
        </el-form-item>
        <el-form-item label="缴费金额">
          <el-input-number v-model="formData.price" :min="0" :precision="2" :step="10" style="width: 200px;" />
          <span style="margin-left: 8px; color: #999; font-size: 12px;">元（设为0表示免费办理）</span>
        </el-form-item>
        <el-form-item label="申请材料">
          <div class="material-input">
            <el-input v-model="newMaterial" placeholder="输入材料名称，回车添加" @keyup.enter="addMaterial" />
            <el-button type="primary" size="small" @click="addMaterial">添加</el-button>
          </div>
          <div v-if="formData.materials.length > 0" class="material-tags">
            <el-tag v-for="(material, index) in formData.materials" :key="index" closable @close="removeMaterial(index)">
              {{ material }}
            </el-tag>
          </div>
          <span class="form-tip">按回车或点击添加按钮添加材料，点击标签删除</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
          <span>{{ formData.status === 1 ? '上线' : '下线' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEdit ? '确认修改' : '确认添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Search, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categories, myItems, createItem, updateItem, deleteItem } from '@/api/service'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const itemsList = ref<any[]>([])
const categoriesList = ref<any[]>([])
const categoryFilter = ref('')
const keyword = ref('')

const detailDialogVisible = ref(false)
const detailItem = reactive<any>({})

const addDialogVisible = ref(false)
const isEdit = ref(false)
const formData = reactive({
  itemCode: '',
  itemName: '',
  category: '',
  deptCode: '',
  description: '',
  materials: [] as string[],
  price: 0,
  status: 1
})

const newMaterial = ref('')

const isAdmin = computed(() => userStore.roles?.includes('ADMIN'))

const deptList = [
  { code: 'EDU', name: '教育局' },
  { code: 'HOU', name: '住建局' },
  { code: 'MED', name: '医保局' },
  { code: 'EMP', name: '人社局' },
  { code: 'SOC', name: '社保局' },
  { code: 'TRA', name: '交通局' },
  { code: 'TAX', name: '税务局' },
  { code: 'POL', name: '公安局' }
]

const categoryMap: Record<string, string> = {
  'education': '教育服务',
  'housing': '住房保障',
  'health': '医疗卫生',
  'employment': '就业创业',
  'social': '社会保障',
  'traffic': '交通出行',
  'tax': '税费办理',
  'certificate': '证件办理'
}

const deptMap: Record<string, string> = {
  'POL': '公安局',
  'SOC': '社保局',
  'MED': '医保局',
  'EDU': '教育局',
  'HOU': '住建局',
  'EMP': '人社局',
  'TRA': '交通局',
  'TAX': '税务局'
}

const getCategoryName = (code: string) => categoryMap[code] || code
const getDeptName = (code: string) => deptMap[code] || code

const parseFormSchema = (formSchema: string) => {
  if (!formSchema) return []
  try {
    const schema = JSON.parse(formSchema)
    if (schema.materials && Array.isArray(schema.materials)) {
      return schema.materials
    }
  } catch {
    return []
  }
  return []
}

const loadCategories = async () => {
  const res = await categories()
  categoriesList.value = res.data || []
}

const loadItems = async () => {
  loading.value = true
  const res = await myItems()
  let items = res.data || []
  
  if (categoryFilter.value) {
    items = items.filter((item: any) => item.category === categoryFilter.value)
  }
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    items = items.filter((item: any) => 
      item.itemName.toLowerCase().includes(kw) ||
      item.itemCode.toLowerCase().includes(kw)
    )
  }
  
  itemsList.value = items
  loading.value = false
}

const viewItem = (item: any) => {
  Object.assign(detailItem, item)
  detailDialogVisible.value = true
}

const editItem = (item: any) => {
  isEdit.value = true
  formData.itemCode = item.itemCode
  formData.itemName = item.itemName
  formData.category = item.category
  formData.deptCode = item.deptCode
  formData.description = item.description
  formData.materials = parseFormSchema(item.formSchema)
  formData.price = item.price != null ? Number(item.price) : 0
  formData.status = item.status
  addDialogVisible.value = true
}

const closeDialog = () => {
  addDialogVisible.value = false
  isEdit.value = false
  formData.itemCode = ''
  formData.itemName = ''
  formData.category = ''
  formData.deptCode = ''
  formData.description = ''
  formData.materials = []
  formData.price = 0
  formData.status = 1
  newMaterial.value = ''
}

const addMaterial = () => {
  if (newMaterial.value.trim()) {
    formData.materials.push(newMaterial.value.trim())
    newMaterial.value = ''
  }
}

const removeMaterial = (index: number) => {
  formData.materials.splice(index, 1)
}

const submitForm = async () => {
  if (!formData.itemCode) {
    ElMessage.warning('请输入事项编码')
    return
  }
  if (!formData.itemName) {
    ElMessage.warning('请输入事项名称')
    return
  }
  if (!formData.category) {
    ElMessage.warning('请选择分类')
    return
  }
  
  const body: any = {
    itemCode: formData.itemCode,
    itemName: formData.itemName,
    category: formData.category,
    description: formData.description,
    formSchema: JSON.stringify({ materials: formData.materials }),
    price: formData.price,
    status: formData.status
  }
  
  if (isAdmin && formData.deptCode) {
    body.deptCode = formData.deptCode
  }
  
  let res
  if (isEdit.value) {
    res = await updateItem(detailItem.id, body)
  } else {
    res = await createItem(body)
  }
  
  if (res.code === 200) {
    ElMessage.success(res.message)
    closeDialog()
    loadItems()
  }
}

const handleDelete = (item: any) => {
  ElMessageBox.confirm(
    `确定要删除服务「${item.itemName}」吗？`,
    '删除确认',
    { type: 'warning' }
  ).then(async () => {
    const res = await deleteItem(item.id)
    if (res.code === 200) {
      ElMessage.success(res.message)
      loadItems()
    }
  }).catch(() => {})
}

onMounted(() => {
  loadCategories()
  loadItems()
})
</script>

<style scoped>
.service-mgmt-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-title h2 {
  font-size: 24px;
  margin: 0 0 8px 0;
}

.header-title p {
  font-size: 14px;
  opacity: 0.85;
  margin: 0;
}

.page-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar :deep(.el-button) {
  margin-left: 10px;
}

.item-card {
  border-radius: 8px;
}

.item-card :deep(.el-table) {
  border-radius: 8px;
}

.material-list {
  max-height: 200px;
  overflow-y: auto;
}

.material-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.material-item :deep(.el-icon) {
  color: #1890ff;
}

.empty-material {
  color: #999;
  font-style: italic;
}

.service-form {
  padding: 10px;
}

.service-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.material-input {
  display: flex;
  gap: 8px;
}

.material-tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.form-tip {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
