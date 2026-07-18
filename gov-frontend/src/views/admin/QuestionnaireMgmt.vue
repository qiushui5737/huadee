<template>
  <div class="questionnaire-mgmt-page">
    <div class="page-header">
      <div class="header-title">
        <h2>问卷调查管理</h2>
        <p>管理问卷调查内容和查看调查结果</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新建问卷
      </el-button>
    </div>

    <div class="page-content">
      <el-card shadow="hover" class="content-card">
        <el-table :data="questionnaires" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="问卷标题" min-width="200" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
                {{ scope.row.status === 'active' ? '发布中' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalCount" label="参与人数" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const questionnaires = ref([
  { id: 1, title: '政务服务满意度调查', status: 'active', totalCount: 1256, createTime: '2026-07-15 10:30:00' },
  { id: 2, title: '城市建设意见征集', status: 'active', totalCount: 892, createTime: '2026-07-14 09:15:00' },
  { id: 3, title: '教育改革民意调查', status: 'draft', totalCount: 0, createTime: '2026-07-16 14:20:00' }
])

const handleAdd = () => {
  ElMessage.info('新建问卷功能开发中')
}

const handleView = (row: any) => {
  ElMessage.info(`查看问卷: ${row.title}`)
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑问卷: ${row.title}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该问卷吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    questionnaires.value = questionnaires.value.filter(q => q.id !== row.id)
    ElMessage.success('删除成功')
  })
}
</script>

<style scoped>
.questionnaire-mgmt-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-title h2 {
  font-size: 24px;
  margin: 0 0 8px 0;
}

.header-title p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.page-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.content-card {
  border-radius: 8px;
}
</style>