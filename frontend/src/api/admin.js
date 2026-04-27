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

export async function fetchManagerPortalGoods({ pageNum = 1, pageSize = 10, category = 1 } = {}) {
  const res = await fetch(
    buildUrl('/admin/manager/getPortalGoods', {
      pageNum,
      pageSize,
      category,
    }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function fetchManagerPortalGoodsById(goodsId) {
  const res = await fetch(
    buildUrl('/admin/manager/getPortalGoodsById', {
      goodsId,
    }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function fetchManagerGoodsTypes({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(
    buildUrl('/admin/manager/getGoodsType', {
      pageNum,
      pageSize,
    }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function addManagerGoodsType(payload) {
  const res = await fetch(buildUrl('/admin/manager/addGoodsType'), {
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

export async function updateManagerGoodsType(payload) {
  const res = await fetch(buildUrl('/admin/manager/updateGoodsType'), {
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

export async function deleteManagerGoodsType(typeId) {
  const res = await fetch(
    buildUrl('/admin/manager/deleteGoodsType', {
      typeId,
    }),
    {
      method: 'POST',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function fetchManagerBrands({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(
    buildUrl('/admin/manager/getBrand', {
      pageNum,
      pageSize,
    }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function addManagerBrand(payload) {
  const res = await fetch(buildUrl('/admin/manager/addBrand'), {
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

export async function updateManagerBrand(payload) {
  const res = await fetch(buildUrl('/admin/manager/updateBrand'), {
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

export async function deleteManagerBrand(id) {
  const res = await fetch(
    buildUrl('/admin/manager/deleteBrand', {
      id,
    }),
    {
      method: 'POST',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function fetchReviewerRegisterApplications({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(
    buildUrl('/admin/reviewer/fetchRegisterApplication', {
      pageNum,
      pageSize,
    }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

async function postReviewerRegisterDecision(path, payload) {
  const res = await fetch(buildUrl(path), {
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

export function approveRegisterApplication(payload) {
  return postReviewerRegisterDecision('/admin/reviewer/accessRegister', payload)
}

export function rejectRegisterApplication(payload) {
  return postReviewerRegisterDecision('/admin/reviewer/rejectRegister', payload)
}

export async function fetchMerchantGoodsApplications({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(
    buildUrl('/admin/reviewer/getGoodsApplication', {
      pageNum,
      pageSize,
    }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function fetchMerchantOwnGoodsApplications({ pageNum = 1, pageSize = 10, merchantId } = {}) {
  const res = await fetch(
    buildUrl('/admin/merchant/getGoodsApplication', {
      pageNum,
      pageSize,
      merchantId,
    }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function approveGoodsApplication(applyId) {
  const res = await fetch(buildUrl('/admin/reviewer/accessGoodsApply', { applyId }), {
    method: 'POST',
    headers: {
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

export async function rejectGoodsApplication({ applyId, remark = '审核退回' } = {}) {
  const res = await fetch(buildUrl('/admin/reviewer/rejectGoodsApply', { applyId, remark }), {
    method: 'POST',
    headers: {
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

export async function fetchSuperUserList({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(
    buildUrl('/admin/super/fetchSysUserInfo', { pageNum, pageSize }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function fetchSuperUserByType({ pageNum = 1, pageSize = 10, userType } = {}) {
  const res = await fetch(
    buildUrl('/admin/super/fetchSysUserByUserType', { pageNum, pageSize, userType }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function fetchSuperUserById(userId) {
  const res = await fetch(
    buildUrl('/admin/super/fetchSysUserByUserId', { userId }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function updateSuperUser(payload) {
  const res = await fetch(buildUrl('/admin/super/updateSysUserInfo'), {
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

export async function deleteSuperUser(userId) {
  const res = await fetch(
    buildUrl('/admin/super/deleteSysUser', { userId }),
    {
      method: 'POST',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

/** super：发起秒杀活动主信息（mall-admin → mall-portal） */
export async function launchSeckillActivity(payload) {
  const res = await fetch(buildUrl('/admin/super/seckill/launch'), {
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

export async function fetchSuperDictionaryList({ pageNum = 1, pageSize = 10 } = {}) {
  const res = await fetch(
    buildUrl('/admin/sys/super/getDictionary', { pageNum, pageSize }),
    {
      method: 'GET',
      headers: {
        ...buildAuthHeader(),
      },
    },
  )
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function addSuperDictionary(payload) {
  const rows = Array.isArray(payload) ? payload : [payload]
  const res = await fetch(buildUrl('/admin/sys/super/addDictionary'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...buildAuthHeader(),
    },
    body: JSON.stringify(rows),
  })
  let data = {}
  try {
    data = await res.json()
  } catch {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, data }
}

export async function updateSuperDictionary(payload) {
  const res = await fetch(buildUrl('/admin/sys/super/updateDictionary'), {
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

export async function deleteSuperDictionary(payload) {
  const res = await fetch(buildUrl('/admin/sys/super/deleteDictionary'), {
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
