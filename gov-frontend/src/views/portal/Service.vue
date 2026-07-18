<template>
  <div class="service-page">
    <div class="page-banner">
      <div class="banner-content">
        <h1>政务办事服务平台</h1>
        <p>便捷高效的一站式政务服务，让群众少跑腿、好办事</p>
      </div>
    </div>

    <div class="page-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="service-tabs">
        <el-tab-pane label="事项目录" name="catalog">
          <div class="catalog-section">
            <div class="search-area">
              <el-input v-model="keyword" placeholder="搜索事项名称或描述" size="large">
                <template #append>
                  <el-button type="primary" size="large" @click="loadItems"><el-icon><Search /></el-icon></el-button>
                </template>
              </el-input>
              <div class="category-tags">
                <el-tag 
                  v-for="cat in categoriesList" 
                  :key="cat.code"
                  :type="selectedCategory === cat.code ? 'primary' : ''"
                  @click="selectedCategory = selectedCategory === cat.code ? '' : cat.code; loadItems()"
                  class="category-tag"
                >
                  {{ cat.name }}
                </el-tag>
              </div>
            </div>

            <div class="items-grid">
              <el-card 
                v-for="item in itemsList" 
                :key="item.id" 
                class="item-card"
                @click="viewItem(item)"
              >
                <div class="card-header">
                  <span class="item-code">{{ item.itemCode }}</span>
                  <el-tag size="small" type="success">在线办理</el-tag>
                </div>
                <h3 class="item-name">{{ item.itemName }}</h3>
                <p class="item-desc">{{ item.description }}</p>
                <div class="card-footer">
                  <span class="category-label">{{ getCategoryName(item.category) }}</span>
                  <div class="card-actions">
                    <el-button type="text" size="small" @click.stop="viewItem(item)">查看详情</el-button>
                    <el-button type="primary" size="small" @click.stop="applyItem(item)">在线申报</el-button>
                  </div>
                </div>
              </el-card>
            </div>

            <el-pagination 
              v-if="total > 0" 
              :total="total" 
              :page-size="pageSize" 
              v-model:current-page="currentPage" 
              @current-change="loadItems" 
              class="pagination"
              layout="prev, pager, next, jumper, ->, total"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="在线申报" name="apply">
          <div class="apply-section">
            <div v-if="currentItem" class="apply-content">
              <div class="apply-header">
                <el-icon class="back-icon" @click="cancelApply"><ArrowLeft /></el-icon>
                <div class="header-info">
                  <h3>{{ currentItem.itemName }}</h3>
                  <p>{{ currentItem.itemCode }}</p>
                </div>
              </div>

              <el-card class="form-card">
                <el-form :model="formData" label-width="120px" class="apply-form">
                  <div v-for="field in formFields" :key="field.key" class="form-row">
                    <el-form-item :label="field.label" :required="field.required">
                      <el-input 
                        v-model="formData[field.key]" 
                        v-if="field.type === 'input'" 
                        :placeholder="`请输入${field.label}`"
                        size="large"
                      />
                      <el-input 
                        v-model="formData[field.key]" 
                        v-else-if="field.type === 'textarea'" 
                        type="textarea" 
                        :placeholder="`请输入${field.label}`" 
                        :rows="4"
                        size="large"
                      />
                    </el-form-item>
                  </div>
                  <el-form-item class="form-actions">
                    <el-button size="large" @click="cancelApply">取消</el-button>
                    <el-button type="primary" size="large" @click="doSubmitForm">提交申请</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </div>
            <div v-else class="empty-state">
              <el-empty description="请先选择事项" :image-size="120" />
              <el-button type="primary" @click="activeTab = 'catalog'" style="margin-top:20px;">去选择事项</el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="进度查询" name="progress">
          <div class="progress-section">
            <div class="search-box">
              <el-input v-model="acceptNo" placeholder="请输入受理号查询办件进度" size="large" style="width:400px;">
                <template #append>
                  <el-button type="primary" size="large" @click="queryProgress"><el-icon><Search /></el-icon></el-button>
                </template>
              </el-input>
            </div>

            <div v-if="progressSteps.length > 0" class="progress-content">
              <div class="progress-header">
                <h3>办件进度</h3>
                <div class="progress-actions">
                  <span class="accept-no">受理号：{{ acceptNo }}</span>
                  <el-button type="primary" size="small" @click="showInquiryDialog = true">
                    <el-icon :size="16"><Message /></el-icon> 询问完成时间
                  </el-button>
                </div>
              </div>

              <el-steps :active="activeStep" align-center class="progress-steps">
                <el-step 
                  v-for="step in progressSteps" 
                  :key="step.step" 
                  :title="step.title" 
                  :description="step.desc"
                />
              </el-steps>

              <el-card class="detail-card">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="当前状态">
                    <el-tag :type="getStatusType(currentRecord.status)">{{ currentRecord.status }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="申请人">{{ currentRecord.userName }}</el-descriptions-item>
                  <el-descriptions-item label="事项名称">{{ currentRecord.itemName }}</el-descriptions-item>
                  <el-descriptions-item label="提交时间">{{ currentRecord.submitTime }}</el-descriptions-item>
                  <el-descriptions-item label="办结时间">{{ currentRecord.finishTime || '-' }}</el-descriptions-item>
                </el-descriptions>
              </el-card>

              <el-card v-if="inquiriesList.length > 0" class="inquiry-card">
                <div class="card-title">
                  <el-icon :size="18"><Message /></el-icon>
                  <h3>询问记录</h3>
                </div>
                <div class="inquiry-list">
                  <div v-for="inquiry in inquiriesList" :key="inquiry.id" class="inquiry-item">
                    <div class="inquiry-content">
                      <span class="inquiry-label">问：</span>
                      <span>{{ inquiry.content }}</span>
                    </div>
                    <div v-if="inquiry.reply" class="inquiry-reply">
                      <span class="reply-label">答：</span>
                      <span>{{ inquiry.reply }}</span>
                    </div>
                    <div class="inquiry-time">{{ inquiry.createTime }}</div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="缴费证照" name="payment">
          <div class="payment-section">
            <div class="payment-header">
              <h3>我的缴费证照</h3>
              <el-button type="primary" @click="loadPaymentRecords">
                <el-icon :size="16"><Refresh /></el-icon> 刷新
              </el-button>
            </div>

            <div v-if="paymentRecordsList.length > 0" class="payment-list">
              <el-card v-for="record in paymentRecordsList" :key="record.acceptNo" class="payment-list-card">
                <div class="payment-card-header">
                  <div>
                    <span class="payment-item-name">{{ record.itemName }}</span>
                    <span class="payment-accept-no">受理号：{{ record.acceptNo }}</span>
                  </div>
                  <el-tag :type="record.payStatus === '已支付' ? 'success' : 'warning'">
                    {{ record.payStatus }}
                  </el-tag>
                </div>
                <div class="payment-card-body">
                  <div class="payment-info">
                    <span class="payment-label">缴费金额</span>
                    <span class="payment-amount">¥{{ record.payAmount || 0 }}</span>
                  </div>
                  <div class="payment-info">
                    <span class="payment-label">支付时间</span>
                    <span>{{ record.payTime ? formatDate(record.payTime) : '-' }}</span>
                  </div>
                  <div class="payment-info">
                    <span class="payment-label">证照状态</span>
                    <el-tag :type="record.licenseStatus === '已发放' ? 'success' : record.licenseStatus === '待领取' ? 'primary' : ''">
                      {{ record.licenseStatus || '办理中' }}
                    </el-tag>
                  </div>
                </div>
                <div class="payment-card-footer">
                  <el-button 
                    v-if="record.payStatus !== '已支付'" 
                    type="primary" 
                    size="small" 
                    @click="doPayment(record.acceptNo)"
                  >
                    <el-icon :size="14"><CreditCard /></el-icon> 立即支付
                  </el-button>
                  <el-button 
                    v-if="record.licenseStatus === '已发放'" 
                    type="success" 
                    size="small" 
                    @click="doDownload(record.acceptNo)"
                  >
                    <el-icon :size="14"><Download /></el-icon> 下载证照
                  </el-button>
                </div>
              </el-card>
            </div>

            <div v-else class="empty-state">
              <el-empty description="暂无缴费证照记录" :image-size="120" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="detailDialogVisible" title="事项详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="事项编码" :span="2">{{ currentItem?.itemCode }}</el-descriptions-item>
        <el-descriptions-item label="事项名称">{{ currentItem?.itemName }}</el-descriptions-item>
        <el-descriptions-item label="所属分类">{{ getCategoryName(currentItem?.category) }}</el-descriptions-item>
        <el-descriptions-item label="承办部门">{{ getDeptName(currentItem?.deptCode) }}</el-descriptions-item>
        <el-descriptions-item label="事项描述" :span="2">{{ currentItem?.description }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="applyItem(currentItem)">在线申报</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showInquiryDialog" title="询问完成时间" width="500px">
      <el-form :model="inquiryForm" label-width="80px">
        <el-form-item label="询问内容">
          <el-input v-model="inquiryForm.content" type="textarea" :rows="4" placeholder="请输入您的问题，例如：请问什么时候能办理完成？" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showInquiryDialog = false">取消</el-button>
        <el-button type="primary" @click="doInquiry">
          <el-icon :size="16"><ChatRound /></el-icon> 发送询问
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowLeft, Wallet, CreditCard, Document, Download, Clock, Message, Refresh, ChatRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { categories, items, itemDetail, formSchema, submitForm, formProgress, recordDetail, calculatePayment, pay, getLicense, downloadLicense, paymentRecords, inquiryProgress, getInquiries } from '@/api/service'

const activeTab = ref('catalog')
const keyword = ref('')
const selectedCategory = ref('')
const categoriesList = ref<any[]>([])
const itemsList = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

const currentItem = ref<any>(null)
const formFields = ref<any[]>([])
const formData = reactive<any>({})
const detailDialogVisible = ref(false)

const acceptNo = ref('')
const progressSteps = ref<any[]>([])
const activeStep = ref(0)
const currentRecord = reactive<any>({})

const payAcceptNo = ref('')
const paymentInfo = ref<any>(null)
const licenseInfo = ref<any>(null)
const paymentRecordsList = ref<any[]>([])

const showInquiryDialog = ref(false)
const inquiryForm = reactive({ content: '' })
const inquiriesList = ref<any[]>([])

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
const getStatusType = (status: string) => {
  switch (status) {
    case '提交申请': return 'warning'
    case '审核中': return ''
    case '已审批': return 'primary'
    case '已办结': return 'success'
    case '已驳回': return 'danger'
    default: return ''
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

const loadCategories = async () => {
  const res = await categories()
  categoriesList.value = res.data || []
}

const loadItems = async () => {
  const params = {
    category: selectedCategory.value || undefined,
    keyword: keyword.value || undefined,
    page: currentPage.value,
    size: pageSize.value
  }
  const res = await items(params)
  itemsList.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const router = useRouter()

const viewItem = async (item: any) => {
  const res = await itemDetail(item.id)
  currentItem.value = res.data
  detailDialogVisible.value = true
}

const applyItem = async (item: any) => {
  detailDialogVisible.value = false
  router.push(`/service/apply?itemId=${item.id}`)
}

const cancelApply = () => {
  currentItem.value = null
  formFields.value = []
  Object.keys(formData).forEach(key => delete formData[key])
  activeTab.value = 'catalog'
}

const doSubmitForm = async () => {
  if (!currentItem.value) {
    ElMessage.warning('请先选择事项')
    return
  }
  formData.itemId = currentItem.value.id
  const res = await submitForm(formData)
  if (res.code === 200) {
    ElMessage.success(res.message)
    cancelApply()
    activeTab.value = 'progress'
    acceptNo.value = res.data.acceptNo
    queryProgress()
  }
}

const queryProgress = async () => {
  if (!acceptNo.value) {
    ElMessage.warning('请输入受理号')
    return
  }
  const [progressRes, detailRes, inquiriesRes] = await Promise.all([
    formProgress(acceptNo.value),
    recordDetail(acceptNo.value),
    getInquiries(acceptNo.value)
  ])
  progressSteps.value = progressRes.data || []
  Object.assign(currentRecord, detailRes.data || {})
  inquiriesList.value = inquiriesRes.data || []
  
  const completedSteps = progressSteps.value.filter(s => s.completed).length
  activeStep.value = completedSteps > 0 ? completedSteps - 1 : 0
}

const queryPayment = async () => {
  if (!payAcceptNo.value) {
    ElMessage.warning('请输入受理号')
    return
  }
  const payRes = await calculatePayment(payAcceptNo.value)
  paymentInfo.value = payRes.data
  
  try {
    const licenseRes = await getLicense(payAcceptNo.value)
    licenseInfo.value = licenseRes.data
  } catch (e) {
    licenseInfo.value = null
  }
}

const loadPaymentRecords = async () => {
  const res = await paymentRecords()
  paymentRecordsList.value = res.data || []
}

const doPayment = async (acceptNo: string) => {
  const payRes = await calculatePayment(acceptNo)
  const paymentData = payRes.data
  if (!paymentData || paymentData.payStatus === '已支付') {
    ElMessage.warning('该办件已支付或不存在')
    return
  }
  
  const res = await pay({
    acceptNo: paymentData.acceptNo,
    amount: paymentData.amount
  })
  if (res.code === 200) {
    ElMessage.success(res.message)
    loadPaymentRecords()
  }
}

const doDownload = async (acceptNo: string) => {
  const res = await downloadLicense(acceptNo)
  if (res.code === 200) {
    ElMessage.success(res.data.message)
  }
}

const doInquiry = async () => {
  if (!inquiryForm.content.trim()) {
    ElMessage.warning('请输入询问内容')
    return
  }
  await inquiryProgress(acceptNo.value, inquiryForm.content)
  ElMessage.success('询问已发送')
  showInquiryDialog.value = false
  inquiryForm.content = ''
  queryProgress()
}

const handleTabChange = (tabName: string) => {
  if (tabName === 'catalog') {
    loadItems()
  } else if (tabName === 'payment') {
    loadPaymentRecords()
  }
}

onMounted(() => {
  loadCategories()
  loadItems()
})
</script>

<style scoped>
.service-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

.page-banner {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 60px 0;
  text-align: center;
  color: #fff;
}

.banner-content h1 {
  font-size: 36px;
  margin: 0 0 12px 0;
  font-weight: bold;
}

.banner-content p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.service-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.service-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 30px;
}

.catalog-section {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.search-area {
  margin-bottom: 24px;
}

.search-area :deep(.el-input) {
  max-width: 500px;
  margin-bottom: 16px;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.category-tag:hover {
  transform: scale(1.05);
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.item-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-code {
  font-size: 12px;
  color: #999;
  font-family: monospace;
}

.item-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #303133;
}

.item-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.category-label {
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  padding: 4px 12px;
  border-radius: 4px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 30px;
  text-align: right;
}

.apply-section {
  animation: fadeIn 0.3s ease;
}

.apply-content {
  max-width: 700px;
  margin: 0 auto;
}

.apply-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.back-icon {
  font-size: 24px;
  cursor: pointer;
  margin-right: 16px;
  color: #606266;
}

.back-icon:hover {
  color: #1890ff;
}

.header-info h3 {
  font-size: 20px;
  margin: 0 0 4px 0;
}

.header-info p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.form-card {
  border-radius: 12px;
}

.apply-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-row {
  animation: slideIn 0.3s ease backwards;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.progress-section {
  animation: fadeIn 0.3s ease;
}

.search-box {
  text-align: center;
  margin-bottom: 30px;
}

.progress-content {
  max-width: 800px;
  margin: 0 auto;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.progress-header h3 {
  font-size: 18px;
  margin: 0;
}

.accept-no {
  font-size: 14px;
  color: #1890ff;
  font-family: monospace;
}

.progress-steps {
  margin-bottom: 30px;
}

.detail-card {
  border-radius: 12px;
}

.payment-section {
  animation: fadeIn 0.3s ease;
}

.payment-content {
  max-width: 600px;
  margin: 0 auto;
}

.payment-card, .license-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.card-title h3 {
  font-size: 16px;
  margin: 0 0 0 8px;
}

.amount {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
}

.pay-btn, .download-btn {
  width: 100%;
  margin-top: 20px;
}

.hint-card {
  background: #f0f9ff;
  border-color: #b3d8ff;
}

.hint-content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.hint-icon {
  font-size: 24px;
  color: #1890ff;
  margin-right: 12px;
}

.hint-content p {
  margin: 0;
  color: #606266;
}
</style>