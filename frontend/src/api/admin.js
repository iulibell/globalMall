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
  const auth = token.startsWith(`${tokenHead} `) || token.startsWith(`${tokenHead}`)
    ? token
    : `${tokenHead} ${token}`.trim()
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
