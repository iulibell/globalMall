import { getJson } from '@/api/http.js'

/**
 * 对应 mall-admin {@code GET /admin/sys/getDictionary?dictType=...}（公开，无需登录）
 * @param {string} dictType
 */
export function fetchDictionary(dictType) {
  const q = new URLSearchParams({ dictType })
  return getJson(`/admin/sys/getDictionary?${q.toString()}`, { auth: false })
}
