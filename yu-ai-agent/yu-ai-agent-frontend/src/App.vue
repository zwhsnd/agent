<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Grid2X2, Heart, Sparkles, ArrowUpRight, PanelLeftClose, Menu, X, Leaf } from 'lucide-vue-next'
const route = useRoute()
const mobileOpen = ref(false)
const current = computed(() => route.params.app)
</script>

<template>
  <div class="app-shell">
    <button class="mobile-menu icon-button" aria-label="打开导航" @click="mobileOpen = !mobileOpen"><Menu :size="21" /></button>
    <div v-if="mobileOpen" class="sidebar-scrim" @click="mobileOpen = false"></div>
    <aside class="sidebar" :class="{ 'is-open': mobileOpen }">
      <RouterLink to="/" class="brand" @click="mobileOpen = false"><span class="brand-mark"><Sparkles :size="23" /></span><span>YU<span class="brand-light"> AI</span><small>YOUR AI COMPANION</small></span></RouterLink>
      <button class="mobile-close icon-button" aria-label="关闭导航" @click="mobileOpen = false"><X :size="20" /></button>
      <div class="workspace-label">个人工作空间 <span>FREE</span></div>
      <nav aria-label="主导航">
        <RouterLink to="/" class="nav-item" :class="{ active: route.path === '/' }" @click="mobileOpen = false"><Grid2X2 :size="18" />应用广场<span class="nav-dot" v-if="route.path === '/'" /></RouterLink>
        <p class="nav-label">我的 AI 应用</p>
        <RouterLink to="/chat/love" class="nav-item" :class="{ active: current === 'love' }" @click="mobileOpen = false"><Heart :size="18" />AI 恋爱大师<span class="tiny-badge pink">LOVE</span></RouterLink>
        <RouterLink to="/chat/manus" class="nav-item" :class="{ active: current === 'manus' }" @click="mobileOpen = false"><Sparkles :size="18" />AI 超级智能体</RouterLink>
      </nav>
      <div class="sidebar-bottom">
        <div class="sidebar-note"><Leaf :size="22" /><h3>让灵感，生长为可能。</h3><p>每一次对话，都是新的开始。</p><RouterLink to="/chat/manus">开启你的探索 <ArrowUpRight :size="14" /></RouterLink></div>
        <div class="profile"><span class="user-avatar">Y</span><div><strong>探索者</strong><small>你的专属 AI 空间</small></div><span class="profile-status" title="个人工作空间" /></div>
        <div class="sidebar-footer"><span>YU AI WORKSPACE</span><PanelLeftClose :size="14" /></div>
      </div>
    </aside>
    <main class="main-panel"><RouterView :key="route.path" /></main>
  </div>
</template>
