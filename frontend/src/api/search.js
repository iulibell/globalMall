function toAbsoluteUrl(path, params = {}) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = new URL(normalizedPath, window.location.origin)
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  return url.toString()
}

export async function searchGoods({ query = '', page = 0, size = 12 } = {}) {
  const res = await fetch(toAbsoluteUrl('/search/goods', { query, page, size }), {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore parse error */
  }
  return { ok: res.ok, status: res.status, data }
}
