<template>
  <AccessibilityToolbar />
  <div id="app-main" tabindex="-1">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AccessibilityToolbar from '@/components/AccessibilityToolbar.vue'
import { useAccessibilityStore } from '@/stores/accessibility'

const accessibility = useAccessibilityStore()
onMounted(accessibility.initialize)
</script>

<style>
body {
  margin: 0;
  font-family: "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
}
</style>
