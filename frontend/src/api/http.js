/**
 * 与 globalLogistic 前端一致：相对路径请求网关代理的 mall-admin / mall-portal 等。
 * 鉴权头与后端 sa-token.token-prefix 对齐，统一发送 `Bearer <token>`。
 */
function satokenHeaderValue(raw) {
  if (raw == null || typeof raw !== 'string') return ''
  const t = raw.trim()
  if (!t) return ''
  const token = t.replace(/^(Bearer|barrer)\s+/i, '').trim()
  if (!token) return ''
  return `Bearer ${token}`
}

function buildHeaders({ auth }) {
  const headers = { Accept: 'application/json' }
  if (auth && typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('satoken') || ''
    const v = satokenHeaderValue(token)
    if (v) {
      headers.Authorization = v
      headers.satoken = v
    }
  }
  return headers
}

async function parseJsonResponse(res) {
  const text = await res.text()
  let json
  try {
    json = text ? JSON.parse(text) : {}
  } catch {
    throw new Error('服务器返回非 JSON')
  }
  return json
}

/**
 * @param {string} path 以 / 开头
 * @param {{ auth?: boolean }} [options]
 */
export async function getJson(path, { auth = false } = {}) {
  const headers = buildHeaders({ auth })
  const base = import.meta.env.VITE_API_BASE || ''
  const res = await fetch(`${base}${path}`, { method: 'GET', headers })
  return parseJsonResponse(res)
}

/**
 * @param {string} path
 * @param {object} [body]
 * @param {{ auth?: boolean }} [options]
 */
export async function postJson(path, body = {}, { auth = true } = {}) {
  const headers = {
    ...buildHeaders({ auth }),
    'Content-Type': 'application/json',
  }
  const base = import.meta.env.VITE_API_BASE || ''
  const res = await fetch(`${base}${path}`, {
    method: 'POST',
    headers,
    body: JSON.stringify(body ?? {}),
  })
  const json = await parseJsonResponse(res)
  if (!res.ok) {
    const msg =
      json && typeof json.message === 'string' && json.message.trim()
        ? json.message.trim()
        : `http_${res.status}`
    throw new Error(msg)
  }
  return json
}
