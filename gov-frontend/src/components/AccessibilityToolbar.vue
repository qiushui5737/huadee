<template>
  <a class="skip-link" href="#app-main">跳到主要内容</a>
  <button
    class="a11y-launcher"
    type="button"
    :aria-expanded="store.toolbarOpen"
    aria-controls="accessibility-toolbar"
    title="无障碍服务"
    @click="store.toolbarOpen = !store.toolbarOpen"
  >
    <el-icon><SetUp /></el-icon><span>无障碍</span>
  </button>

  <section v-show="store.toolbarOpen" id="accessibility-toolbar" class="a11y-toolbar" aria-label="无障碍工具栏">
    <div class="toolbar-inner">
      <strong>无障碍服务</strong>
      <div class="tool-group language-control" aria-label="语言">
        <button :class="{ active: store.language === 'zh-CN' }" type="button" @click="store.setLanguage('zh-CN')">简体</button>
        <button :class="{ active: store.language === 'zh-TW' }" type="button" @click="store.setLanguage('zh-TW')">繁體</button>
      </div>
      <div class="tool-group font-control" aria-label="文字大小">
        <button v-for="size in fontSizes" :key="size.value" :class="{ active: store.fontScale === size.value }" type="button" @click="store.setFontScale(size.value)">{{ size.label }}</button>
      </div>
      <button class="tool-button" :class="{ active: store.highContrast }" type="button" :aria-pressed="store.highContrast" @click="store.toggleHighContrast">
        <el-icon><Sunny /></el-icon><span>高对比度</span>
      </button>
      <button class="tool-button" :class="{ active: store.highlightLinks }" type="button" :aria-pressed="store.highlightLinks" @click="store.toggleHighlightLinks">
        <el-icon><Link /></el-icon><span>强调链接</span>
      </button>
      <button class="tool-button" :class="{ active: store.reduceMotion }" type="button" :aria-pressed="store.reduceMotion" @click="store.toggleReduceMotion">
        <el-icon><VideoPause /></el-icon><span>减少动画</span>
      </button>
      <button class="tool-button" type="button" @click="toggleReading">
        <el-icon><component :is="store.reading ? VideoPause : Headset" /></el-icon><span>{{ store.reading ? '停止朗读' : '朗读页面' }}</span>
      </button>
      <button class="tool-button" type="button" @click="store.reset"><el-icon><RefreshLeft /></el-icon><span>恢复默认</span></button>
      <button class="close-button" type="button" aria-label="关闭无障碍工具栏" title="关闭" @click="store.toolbarOpen = false"><el-icon><Close /></el-icon></button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Headset, Link, Close, RefreshLeft, SetUp, Sunny, VideoPause } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAccessibilityStore } from '@/stores/accessibility'

const store = useAccessibilityStore()
const fontSizes = [
  { label: '标准', value: 100 as const },
  { label: '大字', value: 120 as const },
  { label: '超大', value: 140 as const }
]
function toggleReading() {
  if (store.reading) return store.stopReading()
  if (!store.startReading()) ElMessage.warning('当前浏览器不支持页面朗读')
}
</script>
