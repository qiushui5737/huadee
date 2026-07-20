<template>
  <div class="dept-page">
    <div class="page-header">
      <h1 class="page-title">部门集约化 - 子站切换与信息公开</h1>
      <p class="page-subtitle">选择部门子站查看信息公开目录、通知公告、政策文件等</p>
    </div>

    <el-row :gutter="20">
      <el-col v-for="site in sites" :key="site.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="site-card" @click="goDept(site)">
          <div class="site-card-inner">
            <div class="site-icon">🏛</div>
            <div class="site-name">{{ site.siteName }}</div>
            <div class="site-code">{{ site.siteCode }}</div>
          </div>
          <div class="site-links">
            <router-link :to="'/disclosure-catalog?site=' + site.siteCode">信息公开目录</router-link>
            <el-divider direction="vertical" />
            <router-link :to="'/dept?site=' + site.siteCode">查看详情</router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { siteList } from '../../api/cms'

const router = useRouter()
const sites = ref<any[]>([])

function goDept(site: any) {
  router.push('/dept?site=' + site.siteCode)
}

onMounted(function() {
  siteList({page:1, size:100}).then(function(r) {
    sites.value = r.data.records || r.data || []
  })
})
</script>

<style scoped>
.dept-page { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
.page-subtitle { font-size: 14px; color: #909399; margin-bottom: 24px; }
.site-card { margin-bottom: 20px; cursor: pointer; text-align: center; }
.site-card-inner { padding: 16px 0; }
.site-icon { font-size: 48px; margin-bottom: 8px; }
.site-name { font-size: 18px; font-weight: 600; }
.site-code { font-size: 12px; color: #999; margin-top: 4px; }
.site-links { padding: 8px 0 0; border-top: 1px solid #f0f0f0; font-size: 13px; }
.site-links a { color: #1890ff; text-decoration: none; }
</style>
