<script setup>
import { ArrowUpRight, ArrowRight, Heart, Sparkles, MessageCircle, ShieldCheck, Zap, Command, Sprout } from 'lucide-vue-next'
import { apps } from '../data'
</script>

<template>
  <header class="topbar"><div class="breadcrumb">工作空间<span>/</span><strong>应用广场</strong></div><span class="topbar-caption"><span class="status-dot" />为你的每一种可能</span></header>
  <div class="home-content">
    <section class="welcome">
      <div class="eyebrow"><span class="little-line" /> A LITTLE INTELLIGENCE, A LOT OF POSSIBILITY</div>
      <h1>你好，探索者<span class="greeting-spark">✳</span><br>今天，想让什么<span class="green-text">变得更好？</span></h1>
      <p>聊聊心事，探索灵感，或是完成一个了不起的想法。<br class="mobile-break">你的 AI 伙伴，随时在这里。</p>
      <div class="welcome-art" aria-hidden="true"><div class="orbit orbit-one"></div><div class="orbit orbit-two"></div><div class="art-tile tile-main"><Sparkles :size="44" :stroke-width="1.2" /></div><div class="art-tile tile-heart"><Heart :size="29" :stroke-width="1.5" /></div><div class="art-tile tile-chat"><MessageCircle :size="26" :stroke-width="1.5" /></div><span class="art-star star-one">✦</span><span class="art-star star-two">✧</span><span class="art-dot" /></div>
    </section>
    <section class="apps-section">
      <div class="section-heading"><div><h2>发现你的 AI 伙伴 <span>02</span></h2><p>专注不同领域，陪你解决每一个具体的问题。</p></div><span class="section-meta">为你精选 <span>↙</span></span></div>
      <div class="app-grid">
        <RouterLink v-for="(app, key) in apps" :key="key" :to="`/chat/${key}`" class="app-card" :class="key">
          <div class="card-top"><span class="app-icon"><Heart v-if="key === 'love'" :size="27" :stroke-width="1.6" /><Sparkles v-else :size="27" :stroke-width="1.6" /></span><span class="app-category">{{ key === 'love' ? '你的情感陪伴' : '你的行动搭档' }}<span class="category-dot" /></span></div>
          <div class="card-art" aria-hidden="true"><template v-if="key === 'love'"><div class="love-orbit"></div><Heart class="big-heart" :size="103" :stroke-width="0.8" /><span class="mini-spark">✧</span></template><template v-else><div class="agent-orbit"></div><Command class="big-command" :size="90" :stroke-width="0.8" /><span class="mini-spark">✦</span></template></div>
          <div class="card-copy"><span class="card-english">{{ app.english }}</span><h3>{{ app.name }}</h3><p>{{ app.description }}</p><div class="tags"><span v-for="tag in app.tags" :key="tag">{{ tag }}</span></div></div>
          <div class="card-footer"><span>开始对话 <ArrowRight :size="16" /></span><span class="round-arrow"><ArrowUpRight :size="19" /></span></div>
        </RouterLink>
      </div>
    </section>
    <section class="possibility-banner"><span class="banner-icon"><Sprout :size="24" :stroke-width="1.5" /></span><div><h3>不止于回答，更是一起找到答案。</h3><p>用一点 AI 的力量，给生活多一点可能。</p></div><span class="banner-decoration" aria-hidden="true">✳</span></section>
    <div class="feature-row"><span><MessageCircle :size="15" />自然对话，即时回应</span><span><Zap :size="15" />专属场景，更懂你的需求</span><span><ShieldCheck :size="15" />独立会话，安心探索</span></div>
    <footer class="page-footer"><span>© {{ new Date().getFullYear() }} YU AI. Made for your possibilities.</span><span>由 AI 驱动 · 为你而来 <span class="footer-spark">✦</span></span></footer>
  </div>
</template>
