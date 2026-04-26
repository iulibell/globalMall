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
  if (!token) {
    return {}
  }
  const normalizedToken = String(token).replace(/^(Bearer|barrer)\s+/i, '').trim()
  if (!normalizedToken) {
    return {}
  }
  const auth = `Bearer ${normalizedToken}`
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

export async function fetchGoodsDetailBatch(goodsIds = []) {
  const res = await fetch(buildUrl('/portal/getGoodsDetailBatch'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(Array.isArray(goodsIds) ? goodsIds : []),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 商品类型列表（需 manager 或 merchant 登录态，用于商家申请上架等） */
export async function fetchGoodsTypes(params = {}) {
  const res = await fetch(buildUrl('/portal/getGoodsType', params), {
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

export async function fetchHotGoods(params = {}) {
  const res = await fetch(buildUrl('/portal/hotGoods', params), {
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

/** 用户修改资料（需 user 登录态） */
export async function userUpdateInfo(payload) {
  const body = { ...payload }
  if (!body.password || String(body.password).trim() === '') {
    delete body.password
  }
  const res = await fetch(buildUrl('/portal/user/updateInfo'), {
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

/** 商家可选仓库列表（logi-wms，需 merchant 登录态；申请上架选仓用） */
export async function fetchAvailableWarehouses(params = {}) {
  const res = await fetch(buildUrl('/portal/merchant/getAvailableWarehouse', params), {
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
export async function merchantApplyForOffShelf(goodsId, city) {
  const res = await fetch(buildUrl('/portal/merchant/applyForOffShelf', { goodsId, city }), {
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

/** 商家下架申请列表（需 merchant 登录态） */
export async function fetchMerchantOffShelfList({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(buildUrl('/portal/merchant/getOffShelfList', { pageNum, pageSize }), {
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

/** 商家支付下架费用（需 merchant 登录态） */
export async function merchantPayForOffShelf(payload) {
  const res = await fetch(buildUrl('/portal/merchant/payForOffShelf'), {
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

/** 商家报名秒杀活动（需 merchant 登录态） */
export async function merchantApplySeckillActivity(payload) {
  const res = await fetch(buildUrl('/portal/merchant/seckill/apply'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
    body: JSON.stringify(payload || {}),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 商家查询我的秒杀活动 */
export async function fetchMerchantSeckillActivities({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(buildUrl('/portal/merchant/seckill/myList', { pageNum, pageSize }), {
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

/** 用户购物车列表（需 user 登录态，userId 由后端登录态确定） */
export async function fetchUserCartList() {
  const res = await fetch(buildUrl('/portal/user/cart/list'), {
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

/** 用户删除购物车商品（需 user 登录态） */
export async function deleteUserCartItem(id) {
  const res = await fetch(buildUrl('/portal/user/cart/delete', { id }), {
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

/** 用户订单列表（需 user 登录态） */
export async function fetchUserOrderList({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(buildUrl('/portal/user/order/list', { pageNum, pageSize }), {
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

/** 用户查询订单支付截止信息（需 user 登录态） */
export async function fetchUserOrderPayDeadline(orderId) {
  const res = await fetch(buildUrl('/portal/user/order/payDeadline', { orderId }), {
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

/** 用户支付订单（需 user 登录态） */
export async function payUserOrder(orderId) {
  const res = await fetch(buildUrl('/portal/user/order/pay', { orderId }), {
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

/** 用户取消订单（需 user 登录态） */
export async function cancelUserOrder(orderId) {
  const res = await fetch(buildUrl('/portal/user/order/cancel', { orderId }), {
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

/** 从购物车创建订单（需 user 登录态） */
export async function createOrderFromCart(payload) {
  const res = await fetch(buildUrl('/portal/user/order/createFromCart'), {
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

/** 商品详情页直接创建订单（需 user 登录态） */
export async function createOrderDirect(payload) {
  const res = await fetch(buildUrl('/portal/user/order/createDirect'), {
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

/** 超管/经理：秒杀活动列表 */
export async function fetchSeckillActivitiesForManager({ pageNum = 1, pageSize = 10, status } = {}) {
  const res = await fetch(buildUrl('/portal/manager/seckill/list', { pageNum, pageSize, status }), {
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

/** super：发起秒杀活动主信息 */
export async function launchSeckillActivity(payload) {
  const res = await fetch(buildUrl('/portal/manager/seckill/launch'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
    body: JSON.stringify(payload || {}),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 审核员：审核秒杀活动 */
export async function reviewSeckillActivity(payload) {
  const res = await fetch(buildUrl('/portal/manager/seckill/review'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
    body: JSON.stringify(payload || {}),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** 审核员：取消秒杀活动 */
export async function cancelSeckillActivity(activityCode, reviewRemark = '') {
  const res = await fetch(buildUrl('/portal/manager/seckill/cancel', { activityCode, reviewRemark }), {
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
