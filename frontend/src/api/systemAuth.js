/**
 * 对接 gl-system：LoginController、RegisterController（前缀 /system）
 * 开发环境由 Vite 代理到 127.0.0.1:8205；生产可改为网关地址并配置 CORS。
 */
async function postJson(path, body) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = new URL(normalizedPath, window.location.origin).toString()
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/**
 * @param {{ username: string, password: string, requiredRoleKey: string }} payload
 */
export function login(payload) {
  return postJson('/system/login', payload)
}

/**
 * @param {{ username: string, password: string, nickname?: string, userType: string, phone: string, verifyCode: string }} payload
 */
export function register(payload) {
  return postJson('/system/register', payload)
}

/**
 * @param {{ phone: string }} payload
 */
export function sendRegisterCaptcha(payload) {
  return postJson('/system/register/sendCaptcha', payload)
}
