/** Incremental SSE parser: supports split frames, CR/LF, comments and multiline data. */
export function createSseParser(onEvent) {
  let buffer = ''
  let data = []
  let event = ''
  function line(value) {
    if (value === '') {
      if (data.length) onEvent({ event: event || 'message', data: data.join('\n') })
      data = []
      event = ''
      return
    }
    if (value.startsWith(':')) return
    const colon = value.indexOf(':')
    const field = colon < 0 ? value : value.slice(0, colon)
    let content = colon < 0 ? '' : value.slice(colon + 1)
    if (content.startsWith(' ')) content = content.slice(1)
    if (field === 'data') data.push(content)
    if (field === 'event') event = content
  }
  return {
    push(chunk) {
      buffer += chunk
      let index = 0
      for (let i = 0; i < buffer.length; i++) {
        if (buffer[i] !== '\r' && buffer[i] !== '\n') continue
        if (buffer[i] === '\r' && i === buffer.length - 1) break
        line(buffer.slice(index, i))
        if (buffer[i] === '\r' && buffer[i + 1] === '\n') i++
        index = i + 1
      }
      buffer = buffer.slice(index)
    },
    finish() {
      if (buffer) line(buffer.replace(/\r$/, ''))
      line('')
      buffer = ''
    },
  }
}
