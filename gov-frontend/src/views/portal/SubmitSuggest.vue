<template>
  <div class="suggest-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">我要建议</h1>
      <p class="page-desc">接受自然人、法人和其他组织对全省各政府部门改进办事服务效能、服务方式及四川政务服务网建设的意见和建议。</p>
      <a href="javascript:void(0)" class="query-link" @click="showQuery">回复查询 &gt;&gt;</a>
    </div>

    <!-- 提示信息 -->
    <div class="info-section">
      <p class="info-title">尊敬的用户：</p>
      <p class="info-text">您好！感谢您给我们提出宝贵的建议。我们会进行严格保密。您的个人信息绝不会向外公开，请根据您的实际情况或内心真实想法如实填写。</p>
      <p class="info-text">如需查询建议回复情况，您可以注册成为四川政务服务网用户，然后登录四川政务服务网，在用户中心进行查看，也可以通过右上角的回复查询输入查询码查询。</p>
      <p class="info-note">（注：<span class="required-mark">*</span>为必填项）</p>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="suggest-form">
        <!-- 建议标题 -->
        <el-form-item label="建议标题：" prop="title" required>
          <el-input v-model="form.title" placeholder="请输入您的建议标题，最多不超过50个字" maxlength="50" show-word-limit />
        </el-form-item>

        <!-- 建议类型 -->
        <el-form-item label="建议类型：" prop="type" required>
          <el-radio-group v-model="form.type">
            <el-radio value="网站建议">网站建议</el-radio>
            <el-radio value="部门建议">部门建议</el-radio>
            <el-radio value="我要纠错">我要纠错</el-radio>
            <el-radio value="助企惠企服务专区建议">助企惠企服务专区建议</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 姓名和身份证 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="建议人姓名" prop="name" required>
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号：" prop="idCard" required>
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 邮箱和电话 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电子邮箱：">
              <el-input v-model="form.email" placeholder="请输入您的电子邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话：" prop="phone" required>
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 联系地址 -->
        <el-form-item label="联系地址：" prop="address" required>
          <el-input v-model="form.address" placeholder="请输入联系地址" />
        </el-form-item>

        <!-- 事件发生地 -->
        <el-form-item label="事件发生地：" prop="location" required>
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

        <!-- 建议内容 -->
        <el-form-item label="建议内容：" prop="content" required>
          <div class="content-header">
            <span class="char-count">{{ form.content.length }}/1000</span>
          </div>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入您的建议内容，1000字以内"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <!-- 是否愿意公开 -->
        <el-form-item label="是否愿意公开：" prop="isPublic" required>
          <div class="radio-with-tip">
            <el-radio-group v-model="form.isPublic">
              <el-radio :value="true">是</el-radio>
              <el-radio :value="false">否</el-radio>
            </el-radio-group>
            <span class="tip-text">（选择公开，将会在四川政务服务网上展示您的建议信息）</span>
          </div>
        </el-form-item>

        <!-- 是否保密 -->
        <el-form-item label="是否保密：" prop="isSecret" required>
          <el-radio-group v-model="form.isSecret">
            <el-radio :value="true">是</el-radio>
            <el-radio :value="false">否</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <div class="submit-wrapper">
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              提交建议
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadUserFile } from 'element-plus'
import { submitMessage } from '@/api/interaction'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const fileList = ref<UploadUserFile[]>([])

// 城市数据
const cities = ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市', '内江市', '乐山市', '南充市', '眉山市', '宜宾市', '广安市', '达州市', '雅安市', '巴中市', '资阳市', '阿坝州', '甘孜州', '凉山州']
const districts = ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '龙泉驿区', '青白江区', '新都区', '温江区', '双流区', '郫都区', '新津区']

// 表单数据
const form = reactive({
  title: '',
  type: '网站建议',
  name: '',
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

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入建议标题', trigger: 'blur' },
    { max: 50, message: '标题最多50个字', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择建议类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入建议人姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入联系地址', trigger: 'blur' }],
  location: [
    {
      required: true,
      validator: (_rule: any, value: any, callback: any) => {
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
    { required: true, message: '请输入建议内容', trigger: 'blur' },
    { max: 1000, message: '内容最多1000字', trigger: 'blur' }
  ],
  isPublic: [{ required: true, message: '请选择是否愿意公开', trigger: 'change' }],
  isSecret: [{ required: true, message: '请选择是否保密', trigger: 'change' }]
}

// 查询回复
const showQuery = () => {
  ElMessage.info('回复查询功能开发中')
}

// 提交建议
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
    const res: any = await submitMessage({
      title: form.title,
      content: form.content,
      contactName: form.name,
      type: form.type,
      targetDept: form.city
    })

    if (res.code === 200) {
      ElMessage.success('建议提交成功！')
      router.push('/interaction')
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
.suggest-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  min-height: 100vh;
}

// 页面标题
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

// 提示信息
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

// 表单容器
.form-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 32px 40px;

  .suggest-form {
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: #303133;

      &::before {
        content: '*';
        color: #f56c6c;
        margin-right: 4px;
      }

      // 非必填项不显示星号
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

// 上传提示
.upload-tip {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

// 内容头部
.content-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;

  .char-count {
    font-size: 13px;
    color: #909399;
  }
}

// 单选按钮带提示
.radio-with-tip {
  display: flex;
  align-items: center;
  gap: 12px;

  .tip-text {
    font-size: 13px;
    color: #909399;
  }
}

// 提交按钮
.submit-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;

  :deep(.el-button) {
    min-width: 120px;
  }
}
</style>
