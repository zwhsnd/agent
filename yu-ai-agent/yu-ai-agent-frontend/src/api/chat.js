import axios from 'axios'
import { createSseParser } from './sse'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api',
  adapter: 'fetch',
})

export async function streamChat({ app, message, chatId, signal, onChunk }) {
  const response = await api.get(app === 'love' ? '/ai/love_app/chat/sse' : '/ai/manus/chat', {
    params: app === 'love' ? { message, chatId } : { message },
    responseType: 'stream', signal, headers: { Accept: 'text/event-stream' },
  })
  if (!String(response.headers['content-type']).includes('text/event-stream')) {
    await response.data.cancel()
    throw new Error('服务未返回 SSE 数据，请检查后端接口。')
  }
  const reader = response.data.getReader()
  const decoder = new TextDecoder()
  let doneSignal = false
  const parser = createSseParser(({ event, data }) => {
    if (doneSignal) return
    if (data === '[DONE]' || event === 'done' || event === 'complete') { doneSignal = true; return }
    if (event === 'error') throw new Error(data || '生成失败，请稍后重试。')
    onChunk(data)
  })
  try {
    while (!doneSignal) {
      const { value, done } = await reader.read()
      if (done) { parser.push(decoder.decode()); parser.finish(); break }
      parser.push(decoder.decode(value, { stream: true }))
    }
  } finally {
    await reader.cancel().catch(() => {})
    reader.releaseLock()
  }
}
