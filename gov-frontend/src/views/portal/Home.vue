<template>
  <div style="padding:20px;">
    <el-card>
      <h1>遂宁市政务智能搜索</h1>
      <el-input v-model="keyword" placeholder="搜索政策、服务、部门..." style="width:500px;">
        <template #append><el-button @click="onSearch">搜索</el-button></template>
      </el-input>
    </el-card>
    <el-card style="margin-top:16px;">
      <h2>部门集约化</h2>
      <el-row :gutter="16">
        <el-col :span="6" v-for="s in sites" :key="s.siteCode">
          <el-card><div>{{ s.siteName }}</div></el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { sites as getSites } from "@/api/cms"
const keyword = ref("")
const sites = ref<any[]>([])
onMounted(() => { getSites().then((res:any) => sites.value = res.data || []) })
function onSearch() {}
</script>