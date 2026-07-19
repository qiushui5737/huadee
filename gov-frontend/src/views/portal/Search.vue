<template>
  <section class="search-page">
    <div class="search-band">
      <div class="search-shell">
        <div class="eyebrow">统一检索</div>
        <h1>政务智能搜索</h1>
        <div class="search-box">
          <el-input
            v-model="keyword"
            size="large"
            placeholder="搜索政策、办事事项、公开信息"
            clearable
            @keyup.enter="runSearch(1)"
          >
            <template #prefix><el-icon><SearchIcon /></el-icon></template>
          </el-input>
          <el-button size="large" type="primary" :loading="loading" @click="runSearch(1)">
            搜索
          </el-button>
        </div>
        <div class="hot-row">
          <el-tag
            v-for="word in hotWords"
            :key="word"
            effect="plain"
            @click="pickWord(word)"
          >
            {{ word }}
          </el-tag>
        </div>
      </div>
    </div>

    <div class="content-shell">
      <aside class="filter-panel">
        <div class="panel-title">检索模式</div>
        <el-segmented v-model="mode" :options="modeOptions" block @change="runSearch(1)" />
        <div class="panel-title compact">结果类型</div>
        <el-checkbox-group v-model="checkedTypes">
          <el-checkbox label="知识库" />
          <el-checkbox label="信息公开" />
          <el-checkbox label="办事服务" />
        </el-checkbox-group>
        <div class="metric-box">
          <span>结果数</span>
          <strong>{{ total }}</strong>
        </div>
      </aside>

      <main class="result-panel">
        <div class="result-head">
          <div>
            <h2>{{ keyword ? `“${keyword}”的搜索结果` : '推荐搜索内容' }}</h2>
            <p>聚合知识库、CMS 内容和办事事项，支持关键词与语义检索。</p>
          </div>
          <el-button :icon="Refresh" circle @click="runSearch(page)" />
        </div>

        <el-skeleton v-if="loading" :rows="6" animated />
        <el-empty v-else-if="filteredRecords.length === 0" description="暂无匹配结果" />

        <div v-else class="result-list">
          <article v-for="item in filteredRecords" :key="`${item.type}-${item.id}`" class="result-item">
            <div class="item-main">
              <div class="item-title-row">
                <el-tag :type="tagType(item.type)" size="small">{{ item.type }}</el-tag>
                <h3>{{ item.title }}</h3>
              </div>
              <p class="summary" v-html="item.highlight || item.summary" />
              <div class="meta-row">
                <span><el-icon><Collection /></el-icon>{{ item.source || item.deptCode || '平台知识库' }}</span>
                <span><el-icon><LinkIcon /></el-icon>{{ item.matchMode === 'semantic' ? '语义匹配' : '关键词匹配' }}</span>
              </div>
            </div>
            <div class="score-box">
              <strong>{{ Math.round(item.score || 0) }}</strong>
              <span>相关度</span>
              <el-button link type="primary" @click="go(item.url)">查看</el-button>
            </div>
          </article>
        </div>

        <el-pagination
          v-if="total > size"
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="runSearch"
        />
      </main>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Collection, Link as LinkIcon, Refresh, Search as SearchIcon } from '@element-plus/icons-vue'
import { hotKeywords, search, semanticSearch } from '@/api/ai'

const route = useRoute()
const router = useRouter()
const keyword = ref(String(route.query.q || ''))
const hotWords = ref<string[]>([])
const records = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = 8
const mode = ref('keyword')
const checkedTypes = ref(['知识库', '信息公开', '办事服务'])
const modeOptions = [
  { label: '关键词', value: 'keyword' },
  { label: '语义', value: 'semantic' }
]

const filteredRecords = computed(() => records.value.filter((item) => checkedTypes.value.includes(item.type)))

watch(() => route.query.q, (value) => {
  keyword.value = String(value || '')
  runSearch(1)
})

onMounted(async () => {
  const res: any = await hotKeywords()
  hotWords.value = res.data || []
  if (keyword.value) {
    runSearch(1)
  } else {
    keyword.value = hotWords.value[0] || ''
    runSearch(1)
  }
})

async function runSearch(nextPage = 1) {
  if (!keyword.value.trim()) return
  loading.value = true
  page.value = nextPage
  try {
    const api = mode.value === 'semantic' ? semanticSearch : search
    const res: any = await api(keyword.value.trim(), page.value, size)
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function pickWord(word: string) {
  keyword.value = word
  router.replace({ path: '/search', query: { q: word } })
  runSearch(1)
}

function go(url: string) {
  router.push(url || '/')
}

function tagType(type: string) {
  if (type === '办事服务') return 'success'
  if (type === '信息公开') return 'warning'
  return 'primary'
}
</script>

<style scoped>
.search-page { min-height: calc(100vh - 132px); background: #f5f7fb; }
.search-band { background: linear-gradient(135deg, #0f5fb8 0%, #0a7f83 100%); color: #fff; }
.search-shell { max-width: 1120px; margin: 0 auto; padding: 42px 20px 36px; }
.eyebrow { font-size: 13px; opacity: .82; margin-bottom: 8px; }
h1 { margin: 0 0 22px; font-size: 34px; font-weight: 700; letter-spacing: 0; }
.search-box { display: grid; grid-template-columns: minmax(0, 1fr) 108px; gap: 12px; max-width: 820px; }
.hot-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
.hot-row .el-tag { cursor: pointer; background: rgba(255,255,255,.14); color: #fff; border-color: rgba(255,255,255,.28); }
.content-shell { max-width: 1120px; margin: 0 auto; padding: 22px 20px 34px; display: grid; grid-template-columns: 238px minmax(0,1fr); gap: 18px; }
.filter-panel, .result-panel { background: #fff; border: 1px solid #e6eaf0; border-radius: 8px; }
.filter-panel { padding: 18px; align-self: start; }
.panel-title { font-size: 14px; font-weight: 700; color: #1f2a3d; margin-bottom: 12px; }
.panel-title.compact { margin-top: 22px; }
.metric-box { margin-top: 22px; padding: 14px; background: #f6f9fc; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }
.metric-box span { color: #6b7280; }
.metric-box strong { font-size: 28px; color: #0f5fb8; }
.result-panel { padding: 18px; }
.result-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 14px; }
.result-head h2 { margin: 0; font-size: 20px; color: #1f2a3d; }
.result-head p { margin: 6px 0 0; color: #6b7280; font-size: 13px; }
.result-list { display: grid; gap: 12px; }
.result-item { display: grid; grid-template-columns: minmax(0,1fr) 92px; gap: 16px; padding: 16px; border: 1px solid #e7ebf2; border-radius: 8px; }
.result-item:hover { border-color: #8db8e8; background: #fbfdff; }
.item-title-row { display: flex; gap: 10px; align-items: center; }
.item-title-row h3 { margin: 0; font-size: 17px; color: #172033; }
.summary { margin: 10px 0; color: #4b5563; line-height: 1.7; }
.summary :deep(mark) { background: #fff1a8; padding: 0 2px; }
.meta-row { display: flex; flex-wrap: wrap; gap: 16px; color: #667085; font-size: 13px; }
.meta-row span { display: inline-flex; align-items: center; gap: 5px; }
.score-box { border-left: 1px solid #edf0f5; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; }
.score-box strong { color: #0a7f83; font-size: 28px; }
.score-box span { color: #667085; font-size: 12px; }
.el-pagination { margin-top: 18px; justify-content: flex-end; }
@media (max-width: 820px) {
  .content-shell { grid-template-columns: 1fr; }
  .search-box { grid-template-columns: 1fr; }
  .result-item { grid-template-columns: 1fr; }
  .score-box { border-left: 0; border-top: 1px solid #edf0f5; padding-top: 10px; flex-direction: row; justify-content: space-between; }
}
</style>
