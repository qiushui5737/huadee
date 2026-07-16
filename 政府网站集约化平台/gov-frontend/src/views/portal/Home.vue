<template>
  <div style="padding:20px;">
    <el-card>
      <h1>政务智能搜索</h1>
      <el-input v-model="keyword" placeholder="搜索政策、服务、部门..." style="width:500px;">
        <template #append><el-button @click="onSearch">搜索</el-button></template>
      </el-input>
      <div style="margin-top:12px;">
        <el-tag v-for="w in hotWords" :key="w" @click="keyword=w;onSearch()" style="cursor:pointer;margin:0 4px;">{{ w }}</el-tag>
      </div>
    </el-card>
    <el-card style="margin-top:16px;">
      <h2>政务智能助手</h2>
      <el-button type="primary" @click="$router.push('/chat')">开始问答</el-button>
    </el-card>
    <el-card style="margin-top:16px;">
      <h2>部门集约化</h2>
      <el-row :gutter="16">
        <el-col :span="6" v-for="s in sites" :key="s.code">
          <el-card @click="$router.push('/dept')"><div>{{ s.name }}</div></el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { hotKeywords } from '@/api/ai'
import { sites as getSites } from '@/api/cms'

const router = useRouter()
const keyword = ref('')
const hotWords = ref<string[]>([])
const sites = ref<any[]>([])

onMounted(() => {
  hotKeywords().then((res:any) => hotWords.value = res.data || [])
  getSites().then((res:any) => sites.value = res.data || [])
})

function onSearch() {
  router.push({ path: '/search', query: { q: keyword.value } })
}
</script>
