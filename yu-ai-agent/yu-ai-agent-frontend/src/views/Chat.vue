<script setup>
import { computed, ref, nextTick, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowUp, Heart, Sparkles, Plus, Square, Copy, Check, MessageCircle, RotateCcw } from 'lucide-vue-next'
import MarkdownIt from 'markdown-it'
import { apps } from '../data'
import { streamChat } from '../api/chat'

const route = useRoute()
const kind = route.params.app
const app = apps[kind]
const markdown = new MarkdownIt({ html: false, breaks: true, linkify: true })
const originalLink = markdown.renderer.rules.link_open || ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))
markdown.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return originalLink(tokens, idx, options, env, self)
}
const chatId = ref(crypto.randomUUID())
const messages = ref([])
const input = ref('')
const loading = ref(false)
const error = ref('')
const scroller = ref(null)
const textarea = ref(null)
const copied = ref(null)
let controller
let copyTimer
const canSend = computed(() => input.value.trim() && !loading.value)
const time = () => new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
async function scroll(force = false) {
  const el = scroller.value
  const nearBottom = el && el.scrollHeight - el.scrollTop - el.clientHeight < 160
  await nextTick()
  if (el && (force || nearBottom)) el.scrollTop = el.scrollHeight
}
function resize() {
  if (!textarea.value) return
  textarea.value.style.height = 'auto'
  textarea.value.style.height = Math.min(textarea.value.scrollHeight, 160) + 'px'
}
async function send(prompt) {
  const text = (prompt ?? input.value).trim()
  if (!text || loading.value) return
  error.value = ''
  input.value = ''
  nextTick(resize)
  messages.value.push({ id: crypto.randomUUID(), role: 'user', text, time: time() })
  const reply = { id: crypto.randomUUID(), role: 'assistant', text: '', time: time(), status: 'streaming' }
  messages.value.push(reply)
  const target = messages.value[messages.value.length - 1]
  loading.value = true
  controller = new AbortController()
  scroll(true)
  try {
    await streamChat({ app: kind, message: text, chatId: chatId.value, signal: controller.signal,
      onChunk(chunk) { target.text += (kind === 'manus' && target.text ? '\n\n' : '') + chunk; scroll() },
    })
    target.status = 'done'
    if (!target.text) { target.status = 'error'; error.value = '本次未收到回复，请重试。' }
  } catch (err) {
    if (controller.signal.aborted) target.status = 'stopped'
    else {
      target.status = 'error'
      error.value = err.response ? `请求失败（${err.response.status}），请检查后端服务。` : (err.message === 'Network Error' ? '无法连接后端，请确认 localhost:8123 服务已启动，并检查跨域配置。' : err.message || '连接中断，请重试。')
    }
  } finally {
    loading.value = false
    nextTick(() => textarea.value?.focus())
  }
}
function newChat() {
  if (loading.value) return
  messages.value = []
  chatId.value = crypto.randomUUID()
  error.value = ''
  input.value = ''
  nextTick(resize)
}
function retry() {
  const last = [...messages.value].reverse().find(m => m.role === 'user')
  if (last) send(last.text)
}
async function copy(message) {
  try { await navigator.clipboard.writeText(message.text); copied.value = message.id; clearTimeout(copyTimer); copyTimer = setTimeout(() => copied.value = null, 2000) }
  catch { error.value = '复制失败，请手动选择消息内容复制。' }
}
function keydown(event) {
  if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) { event.preventDefault(); send() }
}
onBeforeUnmount(() => { controller?.abort(); clearTimeout(copyTimer) })
</script>

<template>
  <div class="chat-page" :class="kind">
    <header class="topbar"><div class="breadcrumb"><RouterLink to="/">应用广场</RouterLink><span>/</span><strong>{{ app.name }}</strong></div><button class="new-chat" :disabled="loading" @click="newChat"><Plus :size="16" />新对话</button></header>
    <div class="chat-titlebar"><div class="chat-title"><span class="app-icon"><Heart v-if="kind === 'love'" :size="22" /><Sparkles v-else :size="22" /></span><div><h1>{{ app.name }}</h1><p>{{ kind === 'love' ? '懂你的心事，陪你靠近幸福' : '拆解目标，连接工具，付诸行动' }}</p></div></div><span class="session-id" :title="chatId">会话 {{ chatId.slice(0, 8) }}</span></div>
    <div ref="scroller" class="chat-scroll">
      <div class="conversation">
        <div v-if="!messages.length" class="chat-welcome"><span class="welcome-chat-icon"><Heart v-if="kind === 'love'" :size="36" :stroke-width="1.4" /><Sparkles v-else :size="36" :stroke-width="1.4" /></span><span class="eyebrow">{{ app.english }}</span><h2>{{ app.intro }}</h2><p>{{ app.greeting }}</p><div class="prompt-list"><button v-for="suggestion in app.suggestions" :key="suggestion" @click="send(suggestion)"><MessageCircle :size="16" />{{ suggestion }}<ArrowUp :size="15" /></button></div></div>
        <template v-else><div class="conversation-start">新的对话 · {{ kind === 'love' ? '每一份心事，都值得被认真倾听' : '每一个想法，都值得被付诸行动' }}</div><article v-for="message in messages" :key="message.id" class="message-row" :class="message.role"><span class="message-avatar"><template v-if="message.role === 'user'">Y</template><Heart v-else-if="kind === 'love'" :size="18" /><Sparkles v-else :size="18" /></span><div class="message-content"><div class="message-meta"><strong>{{ message.role === 'user' ? '你' : app.name }}</strong><time>{{ message.time }}</time></div><div class="message-bubble"><div v-if="message.role === 'user'" class="user-text">{{ message.text }}</div><div v-else-if="message.text" class="markdown-body" v-html="markdown.render(message.text)"></div><div v-else-if="message.status === 'streaming'" class="thinking"><span></span><span></span><span></span></div><span v-else class="empty-response">{{ message.status === 'stopped' ? '已停止生成' : '未收到回复' }}</span><span v-if="message.text && message.status === 'streaming'" class="stream-cursor" /></div><div v-if="message.role === 'assistant' && message.status !== 'streaming'" class="message-actions"><button v-if="message.text" :aria-label="copied === message.id ? '已复制' : '复制回复'" @click="copy(message)"><Check v-if="copied === message.id" :size="13" /><Copy v-else :size="13" />{{ copied === message.id ? '已复制' : '复制' }}</button><span v-if="message.status === 'stopped'">已停止</span><span v-if="message.status === 'error'">生成未完成</span></div></div></article></template>
      </div>
    </div>
    <div class="composer-area"><div v-if="error" class="error-banner" role="alert"><span>{{ error }}</span><button :disabled="loading" @click="retry"><RotateCcw :size="14" />重试</button></div><form class="composer" @submit.prevent="send()"><textarea ref="textarea" v-model="input" :placeholder="app.placeholder" aria-label="输入消息" rows="1" maxlength="10000" @keydown="keydown" @input="resize"></textarea><div class="composer-bottom"><span><span class="status-dot" />{{ loading ? '正在生成回复…' : '准备好，听你说' }}</span><button v-if="loading" type="button" class="send-button stop-button" aria-label="停止生成" title="停止生成" @click="controller?.abort()"><Square :size="16" fill="currentColor" /></button><button v-else class="send-button" type="submit" :disabled="!canSend" aria-label="发送消息"><ArrowUp :size="20" /></button></div></form><div class="composer-note"><span>{{ kind === 'manus' ? '每次任务独立执行 · ' : '' }}AI 内容仅供参考，请结合实际情况判断</span><span>Enter 发送 · Shift + Enter 换行</span></div></div>
  </div>
</template>
