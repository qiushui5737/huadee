<template>
  <div class="portal-layout">
    <header class="top-bar">
      <div class="logo">遂宁市政府网站集约化平台</div>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/search">智能搜索</router-link>
        <router-link to="/chat">智能问答</router-link>
        <router-link to="/service">办事服务</router-link>
        <router-link to="/interaction">政民互动</router-link>
        <router-link to="/disclosure">依申请公开</router-link>
        <router-link to="/dept">部门集约</router-link>
        <router-link to="/disclosure-catalog">信息公开目录</router-link>
      </nav>
    </header>
    <main><router-view /></main>
    <footer>© 2026 遂宁市人民政府 · 网站集约化平台 <a href="/admin/login">管理端入口</a></footer>
  </div>
</template>
<script setup lang="ts"></script>
<style scoped>
.top-bar { background:#003368; color:#fff; height:60px; display:flex; align-items:center; padding:0 24px; gap:24px; }
.top-bar .logo { font-size:18px; font-weight:bold; }
.top-bar nav a { color:#fff; margin:0 8px; text-decoration:none; }
.top-bar nav a.router-link-active { border-bottom:2px solid #ffd700; }
footer { background:#1a2a4a; color:rgba(255,255,255,.4); text-align:center; padding:24px; font-size:12px; }
footer a { color:rgba(255,255,255,.6); }
</style>
