import { getJson } from '@/api/http.js'

const dictCache = new Map()
const dictInFlight = new Map()

/**
 * 对应 mall-admin {@code GET /admin/sys/getDictionary?dictType=...}（公开，无需登录）
 * @param {string} dictType
 */
export function fetchDictionary(dictType) {
  if (dictCache.has(dictType)) {
    return Promise.resolve(dictCache.get(dictType))
  }
  if (dictInFlight.has(dictType)) {
    return dictInFlight.get(dictType)
  }
  const q = new URLSearchParams({ dictType })
  const task = getJson(`/admin/sys/getDictionary?${q.toString()}`, { auth: false })
    .then((res) => {
      if (res?.code === 200) {
        dictCache.set(dictType, res)
      }
      return res
    })
    .finally(() => {
      dictInFlight.delete(dictType)
    })
  dictInFlight.set(dictType, task)
  return task
}

export function preloadDictionaries(dictTypes = []) {
  const unique = Array.from(new Set((dictTypes || []).filter(Boolean)))
  return Promise.all(unique.map((dictType) => fetchDictionary(dictType)))
}
