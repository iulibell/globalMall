function buildUrl(path, params = {}) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = new URL(normalizedPath, window.location.origin)
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  return url.toString()
}

function buildAuthHeader() {
  const token = localStorage.getItem('satoken') || ''
  const tokenHead = (localStorage.getItem('tokenHead') || 'Bearer ').trim()
  if (!token) {
    return {}
  }
  const auth =
    token.startsWith(`${tokenHead} `) || token.startsWith(`${tokenHead}`)
      ? token
      : `${tokenHead} ${token}`.trim()
  return {
    Authorization: auth,
    satoken: auth,
  }
}

export async function fetchGoodsDetail(goodsId) {
  const res = await fetch(buildUrl('/portal/getGoodsDetail', { goodsId }), {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 商家修改资料（需 merchant 登录态） */
export async function merchantUpdateInfo(payload) {
  const body = { ...payload }
  if (!body.password || String(body.password).trim() === '') {
    delete body.password
  }
  const res = await fetch(buildUrl('/portal/merchant/updateInfo'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
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

/** 商家上架申请（需 merchant 登录态） */
export async function merchantApplyGoods(payload) {
  const res = await fetch(buildUrl('/portal/merchant/applyGoods'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
    body: JSON.stringify(payload),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 商家下架申请（需 merchant 登录态） */
export async function merchantApplyForOffShelf(goodsId) {
  const res = await fetch(buildUrl('/portal/merchant/applyForOffShelf', { goodsId }), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 商家支付上架入库费用（需 merchant 登录态） */
export async function merchantPayForInbound(applyId) {
  const res = await fetch(buildUrl('/portal/merchant/payForInbound', { applyId }), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 商家取消待支付的上架申请（将 mall_status 置为 3） */
export async function merchantCancelGoodsApply(applyId) {
  const res = await fetch(buildUrl('/portal/merchant/cancelGoodsApply', { applyId }), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 商家查询我的商品列表（需 merchant 登录态） */
export async function fetchMerchantPortalGoods(params = {}) {
  const res = await fetch(buildUrl('/portal/merchant/getPortalGoods', params), {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 用户加入购物车（需 user 登录态） */
export async function addUserCart(payload) {
  const res = await fetch(buildUrl('/portal/user/cart/add'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
    body: JSON.stringify(payload),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}
