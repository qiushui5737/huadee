<template>
  <div class="complaint-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">我要投诉</h1>
      <p class="page-desc">接受自然人、法人和其他组织对全省各政府部门办理行政权力及公共服务事项时工作效能、工作态度的投诉。</p>
      <a href="javascript:void(0)" class="query-link" @click="showQuery">回复查询 &gt;&gt;</a>
    </div>

    <!-- 提示信息 -->
    <div class="info-section">
      <p class="info-title">尊敬的用户：</p>
      <p class="info-text">您好！我们会对你的个人信息进行严格保密。您的个人信息绝不会向外公开，请根据您的实际情况或内心真实想法如实填写。</p>
      <p class="info-text">如需查询投诉回复情况，您可以注册成为四川政务服务网用户，然后登录四川政务服务网，在用户中心进行查看，也可以通过右上角的回复查询输入查询码查询。</p>
      <p class="info-note">（注：<span class="required-mark">*</span>为必填项）</p>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="complaint-form">
        <!-- 投诉标题 -->
        <el-form-item label="*投诉标题：" prop="title">
          <el-input v-model="form.title" placeholder="请输入您的投诉标题，最多不超过50个字" maxlength="50" show-word-limit />
        </el-form-item>

        <!-- 投诉单位（三级层级） -->
        <el-form-item label="*投诉单位：" prop="complaintUnitL1">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="form.complaintUnitL1" placeholder="请选择" style="width: 100%" @change="onUnitL1Change">
                <el-option v-for="item in unitL1Options" :key="item" :label="item" :value="item" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.complaintUnitL2" placeholder="请选择" style="width: 100%" @change="onUnitL2Change" :disabled="!form.complaintUnitL1">
                <el-option v-for="item in unitL2Options" :key="item" :label="item" :value="item" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.complaintUnitL3" placeholder="请选择" style="width: 100%" :disabled="!form.complaintUnitL2">
                <el-option v-for="item in unitL3Options" :key="item" :label="item" :value="item" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <!-- 问题类型（三级层级） -->
        <el-form-item label="问题类型：">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="form.problemTypeL1" placeholder="请选择" style="width: 100%" @change="onTypeL1Change">
                <el-option v-for="item in typeL1Options" :key="item" :label="item" :value="item" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.problemTypeL2" placeholder="请选择" style="width: 100%" @change="onTypeL2Change" :disabled="!form.problemTypeL1">
                <el-option v-for="item in typeL2Options" :key="item" :label="item" :value="item" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.problemTypeL3" placeholder="请选择" style="width: 100%" :disabled="!form.problemTypeL2">
                <el-option v-for="item in typeL3Options" :key="item" :label="item" :value="item" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <!-- 姓名和身份证 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="*投诉人姓名：" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="*身份证号：" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 邮箱和电话 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="*联系电话：" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱：">
              <el-input v-model="form.email" placeholder="请输入您的电子邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 联系地址 -->
        <el-form-item label="*联系地址：" prop="address">
          <el-input v-model="form.address" placeholder="请输入联系地址" />
        </el-form-item>

        <!-- 事件发生地 -->
        <el-form-item label="*事件发生地：" prop="city">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="form.province" placeholder="请选择" style="width: 100%">
                <el-option label="四川省" value="四川省" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.city" placeholder="请选择" style="width: 100%">
                <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.district" placeholder="请选择" style="width: 100%">
                <el-option v-for="district in districts" :key="district" :label="district" :value="district" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <!-- 事件发生具体地址 -->
        <el-form-item label="事件发生具体地址：">
          <el-input v-model="form.detailAddress" placeholder="请输入地址，最多不超过50个字" maxlength="50" show-word-limit />
        </el-form-item>

        <!-- 附件上传 -->
        <el-form-item label="附件上传：">
          <div class="upload-tip">（支持DOC、DOCX、XLS、XLSX、JPG、TXT、PNG格式文件）</div>
          <el-upload
            v-model:file-list="fileList"
            :auto-upload="false"
            :limit="5"
            accept=".doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.txt"
            multiple
          >
            <el-button type="primary">附件上传</el-button>
          </el-upload>
        </el-form-item>

        <!-- 投诉内容 -->
        <el-form-item label="*投诉内容：" prop="content">
          <div class="content-header">
            <span class="char-count">{{ form.content.length }}/1000</span>
          </div>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入您的投诉内容，1000字以内"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <!-- 是否愿意公开 -->
        <el-form-item label="*是否愿意公开：" prop="isPublic">
          <div class="radio-with-tip">
            <el-radio-group v-model="form.isPublic">
              <el-radio :value="true">是</el-radio>
              <el-radio :value="false">否</el-radio>
            </el-radio-group>
            <span class="tip-text">（选择公开，将会在四川政务服务网上展示您的投诉信息）</span>
          </div>
        </el-form-item>

        <!-- 是否保密 -->
        <el-form-item label="*是否保密：" prop="isSecret">
          <el-radio-group v-model="form.isSecret">
            <el-radio :value="true">是</el-radio>
            <el-radio :value="false">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <div class="submit-wrapper">
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              提交问题
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 进度查询弹窗 -->
    <el-dialog v-model="queryVisible" title="投诉进度查询" width="550px">
      <div style="margin-bottom: 16px; display: flex; gap: 10px;">
        <el-input v-model="queryComplaintNo" placeholder="请输入投诉单号" @keyup.enter="handleQuery" />
        <el-button type="primary" :loading="queryLoading" @click="handleQuery">查询</el-button>
      </div>
      <div v-if="queryResult" class="query-result">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投诉单号">{{ queryResult.complaintNo }}</el-descriptions-item>
          <el-descriptions-item label="投诉标题">{{ queryResult.title }}</el-descriptions-item>
          <el-descriptions-item label="投诉单位">{{ [queryResult.complaintUnitL1, queryResult.complaintUnitL2, queryResult.complaintUnitL3].filter(Boolean).join(' / ') }}</el-descriptions-item>
          <el-descriptions-item label="问题类型">{{ [queryResult.problemTypeL1, queryResult.problemTypeL2, queryResult.problemTypeL3].filter(Boolean).join(' / ') }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(queryResult.status)">{{ queryResult.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ queryResult.createTime }}</el-descriptions-item>
          <el-descriptions-item label="答复期限">{{ queryResult.deadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答复内容" v-if="queryResult.replyContent">
            <div style="white-space: pre-wrap;">{{ queryResult.replyContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="答复时间" v-if="queryResult.replyTime">{{ queryResult.replyTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadUserFile } from 'element-plus'
import { submitComplaint, complaintProgress } from '@/api/interaction'

const formRef = ref()
const submitting = ref(false)
const fileList = ref<UploadUserFile[]>([])

// ========== 投诉单位层级数据 ==========
const unitData: Record<string, Record<string, string[]>> = {
  '成都市': {
    '市政府办公厅': ['综合处', '督查室', '信息处'],
    '市教育局': ['基础教育处', '高等教育处', '职业教育处'],
    '市公安局': ['治安支队', '交警支队', '刑侦支队'],
    '市卫健委': ['医政处', '疾控处', '基层卫生处'],
    '市住建局': ['房产处', '城建处', '住房保障处'],
    '市人社局': ['就业处', '社保处', '人才处']
  },
  '自贡市': {
    '市政府办公厅': ['综合处', '督查室'],
    '市教育局': ['基础教育处', '高等教育处'],
    '市公安局': ['治安支队', '交警支队']
  },
  '攀枝花市': {
    '市政府办公厅': ['综合处'],
    '市教育局': ['基础教育处'],
    '市卫健委': ['医政处']
  },
  '泸州市': {
    '市政府办公厅': ['综合处', '督查室'],
    '市教育局': ['基础教育处', '职业教育处'],
    '市公安局': ['治安支队']
  },
  '德阳市': {
    '市政府办公厅': ['综合处', '督查室'],
    '市教育局': ['基础教育处'],
    '市住建局': ['房产处', '城建处']
  },
  '绵阳市': {
    '市政府办公厅': ['综合处', '督查室', '信息处'],
    '市教育局': ['基础教育处', '高等教育处'],
    '市公安局': ['治安支队', '交警支队', '刑侦支队'],
    '市卫健委': ['医政处', '疾控处']
  },
  '广元市': { '市政府办公厅': ['综合处'], '市教育局': ['基础教育处'] },
  '遂宁市': { '市政府办公厅': ['综合处'], '市公安局': ['治安支队'] },
  '内江市': { '市政府办公厅': ['综合处'], '市教育局': ['基础教育处'] },
  '乐山市': { '市政府办公厅': ['综合处', '督查室'], '市卫健委': ['医政处'] },
  '南充市': { '市政府办公厅': ['综合处'], '市教育局': ['基础教育处'] },
  '眉山市': { '市政府办公厅': ['综合处'], '市公安局': ['治安支队'] },
  '宜宾市': { '市政府办公厅': ['综合处', '督查室'], '市教育局': ['基础教育处'] },
  '广安市': { '市政府办公厅': ['综合处'], '市住建局': ['房产处'] },
  '达州市': { '市政府办公厅': ['综合处'], '市教育局': ['基础教育处'] },
  '雅安市': { '市政府办公厅': ['综合处'], '市卫健委': ['医政处'] },
  '巴中市': { '市政府办公厅': ['综合处'], '市教育局': ['基础教育处'] },
  '资阳市': { '市政府办公厅': ['综合处'], '市公安局': ['治安支队'] },
  '阿坝州': { '州政府办公厅': ['综合处'], '州教育局': ['基础教育处'] },
  '甘孜州': { '州政府办公厅': ['综合处'], '州卫健委': ['医政处'] },
  '凉山州': { '州政府办公厅': ['综合处'], '州教育局': ['基础教育处'] }
}

// ========== 问题类型层级数据 ==========
const typeData: Record<string, Record<string, string[]>> = {
  '公安、司法': {
    '法律服务': ['法律援助', '公证', '律师'],
    '治安管理': ['户籍管理', '出入境管理', '交通管理'],
    '刑事侦查': ['案件受理', '案件办理', '案件监督']
  },
  '教育': {
    '学前教育': ['幼儿园管理', '幼教质量'],
    '基础教育': ['义务教育', '高中教育', '特殊教育'],
    '高等教育': ['高校管理', '学历认证', '招生就业'],
    '职业教育': ['中职教育', '技能培训', '职业资格']
  },
  '卫生健康': {
    '医疗服务': ['医院管理', '医疗质量', '医患关系'],
    '公共卫生': ['疾病防控', '卫生监督', '食品安全'],
    '医疗保障': ['医保报销', '医保政策', '异地就医']
  },
  '住房保障': {
    '房地产管理': ['商品房销售', '物业管理', '房屋租赁'],
    '保障性住房': ['公租房', '廉租房', '经济适用房'],
    '城乡建设': ['城市规划', '市政建设', '环境保护']
  },
  '人力资源和社会保障': {
    '就业服务': ['就业登记', '职业介绍', '创业扶持'],
    '社会保险': ['养老保险', '医疗保险', '工伤保险', '失业保险'],
    '劳动关系': ['劳动合同', '工资支付', '劳动监察']
  },
  '民政': {
    '社会救助': ['低保', '特困供养', '临时救助'],
    '养老服务': ['养老机构', '居家养老', '社区养老'],
    '婚姻登记': ['结婚登记', '离婚登记', '补领证件']
  },
  '市场监管': {
    '工商管理': ['企业登记', '商标管理', '广告监管'],
    '质量监督': ['产品质量', '计量标准', '特种设备'],
    '食品药品': ['食品安全', '药品监管', '餐饮管理']
  },
  '交通运输': {
    '道路运输': ['客运管理', '货运管理', '出租车管理'],
    '交通建设': ['公路建设', '桥梁管理', '交通规划']
  }
}

// ========== 层级选择逻辑 ==========
const unitL1Options = Object.keys(unitData)
const unitL2Options = computed(() => form.complaintUnitL1 ? Object.keys(unitData[form.complaintUnitL1] || {}) : [])
const unitL3Options = computed(() => {
  if (form.complaintUnitL1 && form.complaintUnitL2) {
    return unitData[form.complaintUnitL1]?.[form.complaintUnitL2] || []
  }
  return []
})

const typeL1Options = Object.keys(typeData)
const typeL2Options = computed(() => form.problemTypeL1 ? Object.keys(typeData[form.problemTypeL1] || {}) : [])
const typeL3Options = computed(() => {
  if (form.problemTypeL1 && form.problemTypeL2) {
    return typeData[form.problemTypeL1]?.[form.problemTypeL2] || []
  }
  return []
})

const onUnitL1Change = () => { form.complaintUnitL2 = ''; form.complaintUnitL3 = '' }
const onUnitL2Change = () => { form.complaintUnitL3 = '' }
const onTypeL1Change = () => { form.problemTypeL2 = ''; form.problemTypeL3 = '' }
const onTypeL2Change = () => { form.problemTypeL3 = '' }

// ========== 城市数据 ==========
const cities = ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市', '内江市', '乐山市', '南充市', '眉山市', '宜宾市', '广安市', '达州市', '雅安市', '巴中市', '资阳市', '阿坝州', '甘孜州', '凉山州']
const districts = ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '龙泉驿区', '青白江区', '新都区', '温江区', '双流区', '郫都区', '新津区']

// ========== 表单数据 ==========
const form = reactive({
  title: '',
  complaintUnitL1: '',
  complaintUnitL2: '',
  complaintUnitL3: '',
  problemTypeL1: '',
  problemTypeL2: '',
  problemTypeL3: '',
  realName: '',
  idCard: '',
  email: '',
  phone: '',
  address: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  content: '',
  isPublic: true,
  isSecret: false
})

// ========== 表单验证 ==========
const rules = {
  title: [
    { required: true, message: '请输入投诉标题', trigger: 'blur' },
    { max: 50, message: '标题最多50个字', trigger: 'blur' }
  ],
  complaintUnitL1: [{ required: true, message: '请选择投诉单位', trigger: 'change' }],
  realName: [{ required: true, message: '请输入投诉人姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入联系地址', trigger: 'blur' }],
  city: [
    {
      required: true,
      validator: (_rule: any, _value: any, callback: any) => {
        if (!form.province || !form.city || !form.district) {
          callback(new Error('请选择完整的事件发生地'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  content: [
    { required: true, message: '请输入投诉内容', trigger: 'blur' },
    { max: 1000, message: '内容最多1000字', trigger: 'blur' }
  ],
  isPublic: [{ required: true, message: '请选择是否愿意公开', trigger: 'change' }],
  isSecret: [{ required: true, message: '请选择是否保密', trigger: 'change' }]
}

// ========== 进度查询 ==========
const queryVisible = ref(false)
const queryComplaintNo = ref('')
const queryResult = ref<any>(null)
const queryLoading = ref(false)

const showQuery = () => {
  queryVisible.value = true
  queryResult.value = null
  queryComplaintNo.value = ''
}

const handleQuery = async () => {
  if (!queryComplaintNo.value.trim()) {
    ElMessage.warning('请输入投诉单号')
    return
  }
  queryLoading.value = true
  try {
    const res: any = await complaintProgress(queryComplaintNo.value.trim())
    if (res.code === 200) {
      queryResult.value = res.data
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询失败，请检查单号是否正确')
  } finally {
    queryLoading.value = false
  }
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '待受理': 'warning', '处理中': '', '已答复': 'success', '已办结': 'info' }
  return map[status] || ''
}

// ========== 提交投诉 ==========
const handleSubmit = async () => {
  await formRef.value.validate()

  try {
    await ElMessageBox.confirm(
      '提交后不可修改，请确认信息填写正确。',
      '确认提交',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '再检查一下',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    const res: any = await submitComplaint({
      title: form.title,
      complaintUnitL1: form.complaintUnitL1,
      complaintUnitL2: form.complaintUnitL2,
      complaintUnitL3: form.complaintUnitL3,
      problemTypeL1: form.problemTypeL1,
      problemTypeL2: form.problemTypeL2,
      problemTypeL3: form.problemTypeL3,
      realName: form.realName,
      idCard: form.idCard,
      email: form.email,
      phone: form.phone,
      address: form.address,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress,
      content: form.content,
      isPublic: form.isPublic,
      isSecret: form.isSecret
    })

    if (res.code === 200) {
      ElMessageBox.alert(
        `您的投诉已提交成功！<br/>投诉单号：<strong>${res.data.complaintNo}</strong><br/>答复期限：${res.data.deadline || '-'}<br/><br/>请牢记投诉单号，用于查询投诉进度。`,
        '提交成功',
        { dangerouslyUseHTMLString: true, confirmButtonText: '知道了' }
      )
      formRef.value.resetFields()
      form.content = ''
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.complaint-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  min-height: 100vh;
}

.page-header {
  border-bottom: 2px solid #1e5799;
  padding-bottom: 16px;
  margin-bottom: 24px;
  position: relative;

  .page-title {
    font-size: 28px;
    color: #1e5799;
    margin: 0 0 8px 0;
    font-weight: 600;
  }

  .page-desc {
    font-size: 14px;
    color: #606266;
    margin: 0;
    line-height: 1.6;
  }

  .query-link {
    position: absolute;
    right: 0;
    top: 0;
    color: #409eff;
    font-size: 14px;
    text-decoration: none;

    &:hover {
      color: #66b1ff;
      text-decoration: underline;
    }
  }
}

.info-section {
  background: #f5f7fa;
  padding: 20px 24px;
  border-radius: 4px;
  margin-bottom: 24px;

  .info-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px 0;
  }

  .info-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    margin: 0 0 8px 0;
  }

  .info-note {
    font-size: 14px;
    color: #f56c6c;
    margin: 12px 0 0 0;

    .required-mark {
      color: #f56c6c;
    }
  }
}

.form-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 32px 40px;

  .complaint-form {
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: #303133;

      &::before {
        content: '*';
        color: #f56c6c;
        margin-right: 4px;
      }

      &[for="problemTypeL1"],
      &[for="email"],
      &[for="detailAddress"] {
        &::before {
          content: '';
          margin-right: 0;
        }
      }
    }

    :deep(.el-form-item__content) {
      line-height: 32px;
    }

    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;

      &:hover {
        box-shadow: 0 0 0 1px #c0c4cc inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px #409eff inset;
      }
    }

    :deep(.el-textarea__inner) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;

      &:hover {
        box-shadow: 0 0 0 1px #c0c4cc inset;
      }

      &:focus {
        box-shadow: 0 0 0 1px #409eff inset;
      }
    }
  }
}

.upload-tip {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.content-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;

  .char-count {
    font-size: 13px;
    color: #909399;
  }
}

.radio-with-tip {
  display: flex;
  align-items: center;
  gap: 12px;

  .tip-text {
    font-size: 13px;
    color: #909399;
  }
}

.submit-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;

  :deep(.el-button) {
    min-width: 120px;
  }
}
</style>
