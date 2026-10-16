import test from 'node:test'
import assert from 'node:assert/strict'
import { createSseParser } from './sse.js'

test('parses chunked and multiline SSE events', () => {
  const events = []
  const parser = createSseParser(event => events.push(event))
  parser.push('data: 你')
  parser.push('好\r\ndata: 世界\r')
  parser.push('\n\r\nevent: done\ndata: ok\n\n')
  parser.finish()
  assert.deepEqual(events, [{ event: 'message', data: '你好\n世界' }, { event: 'done', data: 'ok' }])
})

test('ignores comments and flushes the final event', () => {
  const events = []
  const parser = createSseParser(event => events.push(event))
  parser.push(': ping\ndata: final')
  parser.finish()
  assert.deepEqual(events, [{ event: 'message', data: 'final' }])
})
