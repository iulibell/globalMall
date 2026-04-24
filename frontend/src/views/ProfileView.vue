<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  approveRegisterApplication,
  approveGoodsApplication,
  deleteSuperUser,
  fetchSuperUserById,
  fetchSuperUserByType,
  fetchSuperUserList,
  fetchMerchantOwnGoodsApplications,
  fetchMerchantGoodsApplications,
  fetchReviewerRegisterApplications,
  rejectGoodsApplication,
  rejectRegisterApplication,
  updateSuperUser,
} from '@/api/admin'
import { fetchMerchantPortalGoods, merchantApplyForOffShelf, merchantUpdateInfo } from '@/api/portal'

const router = useRouter()
const route = useRoute()

/** 与登录/保存写入的 localStorage 同步，供商城侧账户卡片展示（localStorage 非响应式，用 ref 承接） */
const mallSessionPhone = ref('')
const mallSessionCity = ref('')

function refreshMallSessionFromStorage() {
  mallSessionPhone.value = (localStorage.getItem('phone') || '').trim()
  mallSessionCity.value = (localStorage.getItem('city') || '').trim()
}
const token = computed(() => localStorage.getItem('satoken') || '')
const role = computed(() => localStorage.getItem('role') || '')
const username = computed(() => localStorage.getItem('username') || '')
const nickname = computed(() => localStorage.getItem('nickname') || '')
const userId = computed(() => localStorage.getItem('userId') || '')

const isAdminCenterRole = computed(() => ['super', 'manager', 'reviewer'].includes(role.value))
const isMallCenterRole = computed(() => ['merchant', 'user'].includes(role.value))

const displayName = computed(() => nickname.value || username.value || '未命名用户')

const roleLabelMap = {
  super: '超级管理员',
  manager: '管理员',
  reviewer: '审核员',
  merchant: '商家',
  user: '普通用户',
}

const roleLabel = computed(() => roleLabelMap[role.value] || role.value || '未登录')

const reviewerSidebarItems = computed(() => (role.value === 'reviewer' ? ['首页', '注册申请', '上架申请'] : []))
const superSidebarItems = computed(() => (role.value === 'super' ? ['首页', '用户列表', '身份查询', 'id查询'] : []))
const adminSidebarItems = computed(() => {
  if (role.value === 'reviewer') return reviewerSidebarItems.value
  if (role.value === 'super') return superSidebarItems.value
  return []
})
const reviewerActiveTab = ref('首页')
const superActiveTab = ref('首页')
const registerAppsLoading = ref(false)
const registerAppsError = ref('')
const registerApps = ref([])
const registerPageNum = ref(1)
const registerPageSize = ref(10)
const actingPhone = ref('')
const goodsApplyLoading = ref(false)
const goodsApplyError = ref('')
const goodsApplyList = ref([])
const goodsApplyPageNum = ref(1)
const goodsApplyPageSize = ref(10)
const actingApplyId = ref('')
const superLoading = ref(false)
const superError = ref('')
const superUsers = ref([])
const superPageNum = ref(1)
const superPageSize = ref(10)
const superTypePageNum = ref(1)
const superTypePageSize = ref(10)
const superQueryType = ref('1')
const superQueryUserId = ref('')
const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editForm = ref({
  userId: '',
  username: '',
  nickname: '',
  userType: '4',
  status: '1',
})
const deleteDialogVisible = ref(false)
const deleteSubmitting = ref(false)
const deleteTarget = ref(null)
const merchantActiveTab = ref('账户概览')

const merchantProfileForm = ref({
  nickname: '',
  phone: '',
  city: '',
  password: '',
})
const merchantProfileSubmitting = ref(false)
const merchantProfileTip = ref('')
const merchantProfileError = ref('')
const offShelfGoodsId = ref('')
const offShelfSubmitting = ref(false)
const offShelfTip = ref('')
const offShelfError = ref('')
const merchantApplyListLoading = ref(false)
const merchantApplyListError = ref('')
const merchantApplyList = ref([])
const merchantApplyListPageNum = ref(1)
const merchantApplyListPageSize = ref(10)
const merchantGoodsListLoading = ref(false)
const merchantGoodsListError = ref('')
const merchantGoodsList = ref([])
const merchantGoodsPageNum = ref(1)
const merchantGoodsPageSize = ref(10)

function goLogin() {
  router.push({ name: 'login' })
}

function logout() {
  localStorage.removeItem('satoken')
  localStorage.removeItem('tokenHead')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userId')
  localStorage.removeItem('phone')
  localStorage.removeItem('city')
  router.push({ name: 'login' })
}

function syncMerchantProfileForm() {
  if (role.value !== 'merchant') return
  refreshMallSessionFromStorage()
  merchantProfileForm.value = {
    nickname: localStorage.getItem('nickname') || '',
    phone: mallSessionPhone.value,
    city: mallSessionCity.value,
    password: '',
  }
  merchantProfileTip.value = ''
  merchantProfileError.value = ''
}

async function submitMerchantProfile() {
  const uid = (userId.value || '').trim()
  if (!uid) {
    merchantProfileError.value = '缺少用户 ID，请重新登录后再试'
    return
  }
  merchantProfileSubmitting.value = true
  merchantProfileTip.value = ''
  merchantProfileError.value = ''
  try {
    const payload = {
      userId: uid,
      nickname: merchantProfileForm.value.nickname.trim(),
      phone: merchantProfileForm.value.phone.trim(),
      city: merchantProfileForm.value.city.trim(),
    }
    if (merchantProfileForm.value.password.trim()) {
      payload.password = merchantProfileForm.value.password.trim()
    }
    const { ok, data } = await merchantUpdateInfo(payload)
    if (!ok || data?.code !== 200) {
      merchantProfileError.value = data?.message || '保存失败'
      return
    }
    merchantProfileTip.value =
      typeof data?.data === 'string' ? data.data : data?.message || '修改成功'
    localStorage.setItem('nickname', payload.nickname)
    if (payload.phone != null && String(payload.phone).trim() !== '') {
      localStorage.setItem('phone', String(payload.phone).trim())
    } else {
      localStorage.removeItem('phone')
    }
    if (payload.city != null && String(payload.city).trim() !== '') {
      localStorage.setItem('city', String(payload.city).trim())
    } else {
      localStorage.removeItem('city')
    }
    merchantProfileForm.value.password = ''
    refreshMallSessionFromStorage()
  } catch {
    merchantProfileError.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    merchantProfileSubmitting.value = false
  }
}

async function submitOffShelfApply() {
  const goodsId = offShelfGoodsId.value.trim()
  if (!goodsId) {
    offShelfError.value = '请输入要下架的商品ID'
    return
  }
  offShelfSubmitting.value = true
  offShelfTip.value = ''
  offShelfError.value = ''
  try {
    const { ok, data } = await merchantApplyForOffShelf(goodsId)
    if (!ok || data?.code !== 200) {
      offShelfError.value = data?.message || '下架申请提交失败'
      return
    }
    offShelfTip.value = typeof data?.data === 'string' ? data.data : data?.message || '下架申请已提交'
    offShelfGoodsId.value = ''
  } catch {
    offShelfError.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    offShelfSubmitting.value = false
  }
}

const merchantSidebarItems = ['账户概览', '申请列表', '我的商品']

function setMerchantTab(item) {
  merchantActiveTab.value = item
  if (item === '申请列表') {
    loadMerchantApplyList()
    return
  }
  if (item === '我的商品') {
    loadMerchantGoodsList()
  }
}

function payStatusText(v) {
  const s = Number(v)
  if (s === 0) return '待支付'
  if (s === 1) return '已支付'
  if (s === 2) return '超时未支付'
  return '未知'
}

function canPayApply(row) {
  return Number(row?.mallStatus) === 1 && Number(row?.logisticStatus) === 1 && Number(row?.isPay) === 0
}

function canViewLogistic(row) {
  return Number(row?.isPay) === 1
    && String(row?.transportOrderId || '').trim() !== ''
}

function viewLogistic(row) {
  const transportOrderId = String(row?.transportOrderId || '').trim()
  if (!transportOrderId) return
  const target = `http://localhost:5174/?transportOrderId=${encodeURIComponent(transportOrderId)}`
  window.open(target, '_blank', 'noopener')
}

function goPayApply(row) {
  if (!row?.applyId) return
  sessionStorage.setItem('merchant_apply_pay_row', JSON.stringify(row))
  router.push({
    name: 'merchant-apply-pay',
    query: { applyId: row.applyId },
  })
}

async function loadMerchantApplyList() {
  const merchantId = (userId.value || '').trim()
  if (!merchantId) return
  merchantApplyListLoading.value = true
  merchantApplyListError.value = ''
  try {
    const { ok, data } = await fetchMerchantOwnGoodsApplications({
      pageNum: merchantApplyListPageNum.value,
      pageSize: merchantApplyListPageSize.value,
      merchantId,
    })
    if (!ok || data?.code !== 200) {
      merchantApplyList.value = []
      merchantApplyListError.value = data?.message || '获取申请列表失败'
      return
    }
    merchantApplyList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    merchantApplyList.value = []
    merchantApplyListError.value = '网络异常，请确认 mall-admin 已启动'
  } finally {
    merchantApplyListLoading.value = false
  }
}

function portalGoodsStatusText(v) {
  return Number(v) === 1 ? '已上架' : '未上架'
}

function canViewGoodsDetail(row) {
  return Number(row?.status) === 1 && String(row?.goodsId || '').trim() !== ''
}

function goGoodsDetail(row) {
  const goodsId = String(row?.goodsId || '').trim()
  if (!goodsId) return
  router.push({ name: 'goods-detail', params: { goodsId } })
}

function categoryText(v) {
  return Number(v) === 1 ? '特殊商品' : '普通商品'
}

async function loadMerchantGoodsList() {
  const merchantId = (userId.value || '').trim()
  if (!merchantId) return
  merchantGoodsListLoading.value = true
  merchantGoodsListError.value = ''
  try {
    const { ok, data } = await fetchMerchantPortalGoods({
      pageNum: merchantGoodsPageNum.value,
      pageSize: merchantGoodsPageSize.value,
      merchantId,
    })
    if (!ok || data?.code !== 200) {
      merchantGoodsList.value = []
      merchantGoodsListError.value = data?.message || '获取我的商品失败'
      return
    }
    merchantGoodsList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    merchantGoodsList.value = []
    merchantGoodsListError.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    merchantGoodsListLoading.value = false
  }
}

function isAdminTabActive(item) {
  if (role.value === 'reviewer') {
    return reviewerActiveTab.value === item
  }
  if (role.value === 'super') {
    return superActiveTab.value === item
  }
  return false
}

function setAdminTab(item) {
  if (role.value === 'reviewer') {
    reviewerActiveTab.value = item
    return
  }
  if (role.value === 'super') {
    superActiveTab.value = item
  }
}

const showAdminHome = computed(() => {
  if (role.value === 'reviewer') return reviewerActiveTab.value === '首页'
  if (role.value === 'super') return superActiveTab.value === '首页'
  return true
})

async function loadRegisterApplications() {
  registerAppsLoading.value = true
  registerAppsError.value = ''
  try {
    const { ok, data } = await fetchReviewerRegisterApplications({
      pageNum: registerPageNum.value,
      pageSize: registerPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      registerApps.value = []
      registerAppsError.value = data?.message || '获取注册申请失败'
      return
    }
    registerApps.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    registerApps.value = []
    registerAppsError.value = '网络异常，请确认 mall-admin 已启动'
  } finally {
    registerAppsLoading.value = false
  }
}

function toRegisterPayload(row) {
  return {
    username: row.username || '',
    password: row.password || '',
    nickname: row.nickname || '',
    userType: row.userType == null ? '' : String(row.userType),
    phone: row.phone || '',
  }
}

function isPendingRegister(row) {
  return Number(row?.status) === 0
}

function reviewedText(row) {
  const status = Number(row?.status)
  if (status === 0) {
    return ''
  }
  return '已审核'
}

function registerStatusText(status) {
  const s = Number(status)
  if (s === 0) return '待审核'
  if (s === 1) return '已审核'
  if (s === 2) return '已退回'
  return '未知'
}

async function approveRow(row) {
  actingPhone.value = row.phone
  try {
    const { ok, data } = await approveRegisterApplication(toRegisterPayload(row))
    if (!ok || data?.code !== 200) {
      registerAppsError.value = data?.message || '通过失败'
      return
    }
    await loadRegisterApplications()
  } catch {
    registerAppsError.value = '通过失败，请稍后重试'
  } finally {
    actingPhone.value = ''
  }
}

async function rejectRow(row) {
  actingPhone.value = row.phone
  try {
    const { ok, data } = await rejectRegisterApplication(toRegisterPayload(row))
    if (!ok || data?.code !== 200) {
      registerAppsError.value = data?.message || '退回失败'
      return
    }
    await loadRegisterApplications()
  } catch {
    registerAppsError.value = '退回失败，请稍后重试'
  } finally {
    actingPhone.value = ''
  }
}

async function loadGoodsApplications() {
  goodsApplyLoading.value = true
  goodsApplyError.value = ''
  try {
    const { ok, data } = await fetchMerchantGoodsApplications({
      pageNum: goodsApplyPageNum.value,
      pageSize: goodsApplyPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      goodsApplyList.value = []
      goodsApplyError.value = data?.message || '获取上架申请失败'
      return
    }
    goodsApplyList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    goodsApplyList.value = []
    goodsApplyError.value = '网络异常，请确认 mall-admin 已启动'
  } finally {
    goodsApplyLoading.value = false
  }
}

function canOperateGoodsApply(row) {
  return Number(row?.mallStatus) === 0
}

function mallGoodsApplyStatusText(status) {
  const s = Number(status)
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === 2) return '已退回'
  if (s === 3) return '已取消'
  return '未知'
}

function logisticGoodsApplyStatusText(status) {
  const s = Number(status)
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === 2) return '已退回'
  return '未知'
}

function userTypeText(v) {
  const s = String(v ?? '').trim()
  if (s === '1') return '超级管理员'
  if (s === '2') return '管理员'
  if (s === '3') return '商家'
  if (s === '4') return '普通用户'
  if (s === '5') return '审核员'
  return s || '-'
}

function userStatusText(v) {
  return Number(v) === 0 ? '禁用' : '启用'
}


async function loadSuperUsers() {
  superLoading.value = true
  superError.value = ''
  try {
    const { ok, data } = await fetchSuperUserList({
      pageNum: superPageNum.value,
      pageSize: superPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      superUsers.value = []
      superError.value = data?.message || '获取用户列表失败'
      return
    }
    superUsers.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    superUsers.value = []
    superError.value = '网络异常，请确认 mall-admin 已启动'
  } finally {
    superLoading.value = false
  }
}

async function loadSuperUsersByType() {
  superLoading.value = true
  superError.value = ''
  try {
    const { ok, data } = await fetchSuperUserByType({
      pageNum: superTypePageNum.value,
      pageSize: superTypePageSize.value,
      userType: superQueryType.value,
    })
    if (!ok || data?.code !== 200) {
      superUsers.value = []
      superError.value = data?.message || '身份查询失败'
      return
    }
    superUsers.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    superUsers.value = []
    superError.value = '网络异常，请确认 mall-admin 已启动'
  } finally {
    superLoading.value = false
  }
}

async function loadSuperUserById() {
  if (!superQueryUserId.value.trim()) {
    superError.value = '请输入用户ID'
    superUsers.value = []
    return
  }
  superLoading.value = true
  superError.value = ''
  try {
    const { ok, data } = await fetchSuperUserById(superQueryUserId.value.trim())
    if (!ok || data?.code !== 200) {
      superUsers.value = []
      superError.value = data?.message || 'ID查询失败'
      return
    }
    if (Array.isArray(data?.data)) {
      superUsers.value = data.data
    } else if (data?.data) {
      superUsers.value = [data.data]
    } else {
      superUsers.value = []
    }
  } catch {
    superUsers.value = []
    superError.value = '网络异常，请确认 mall-admin 已启动'
  } finally {
    superLoading.value = false
  }
}

function openEditDialog(row) {
  editForm.value = {
    userId: row.userId == null ? '' : String(row.userId),
    username: row.username || '',
    nickname: row.nickname || '',
    userType: row.userType == null ? '4' : String(row.userType),
    status: row.status == null ? '1' : String(row.status),
  }
  editDialogVisible.value = true
}

async function submitEdit() {
  editSubmitting.value = true
  superError.value = ''
  try {
    const { ok, data } = await updateSuperUser({
      userId: Number(editForm.value.userId),
      username: editForm.value.username.trim(),
      nickname: editForm.value.nickname.trim(),
      userType: editForm.value.userType,
      status: Number(editForm.value.status),
    })
    if (!ok || data?.code !== 200) {
      superError.value = data?.message || '更新失败'
      return
    }
    editDialogVisible.value = false
    await reloadSuperCurrentTab()
  } catch {
    superError.value = '更新失败，请稍后重试'
  } finally {
    editSubmitting.value = false
  }
}


function openDeleteDialog(row) {
  deleteTarget.value = row
  deleteDialogVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleteSubmitting.value = true
  superError.value = ''
  try {
    const { ok, data } = await deleteSuperUser(deleteTarget.value.userId)
    if (!ok || data?.code !== 200) {
      superError.value = data?.message || '删除失败'
      return
    }
    deleteDialogVisible.value = false
    deleteTarget.value = null
    await reloadSuperCurrentTab()
  } catch {
    superError.value = '删除失败，请稍后重试'
  } finally {
    deleteSubmitting.value = false
  }
}

async function reloadSuperCurrentTab() {
  if (superActiveTab.value === '用户列表') {
    await loadSuperUsers()
  } else if (superActiveTab.value === '身份查询') {
    await loadSuperUsersByType()
  } else if (superActiveTab.value === 'id查询') {
    await loadSuperUserById()
  }
}

async function approveGoodsRow(row) {
  actingApplyId.value = row.applyId
  try {
    const { ok, data } = await approveGoodsApplication(row.applyId)
    if (!ok || data?.code !== 200) {
      goodsApplyError.value = data?.message || '通过失败'
      return
    }
    await loadGoodsApplications()
  } catch {
    goodsApplyError.value = '通过失败，请稍后重试'
  } finally {
    actingApplyId.value = ''
  }
}

async function rejectGoodsRow(row) {
  actingApplyId.value = row.applyId
  try {
    const { ok, data } = await rejectGoodsApplication({
      applyId: row.applyId,
      remark: row.remark || '审核退回',
    })
    if (!ok || data?.code !== 200) {
      goodsApplyError.value = data?.message || '退回失败'
      return
    }
    await loadGoodsApplications()
  } catch {
    goodsApplyError.value = '退回失败，请稍后重试'
  } finally {
    actingApplyId.value = ''
  }
}

watch(
  () => reviewerActiveTab.value,
  (tab) => {
    if (role.value === 'reviewer' && tab === '注册申请') {
      loadRegisterApplications()
    }
    if (role.value === 'reviewer' && tab === '上架申请') {
      loadGoodsApplications()
    }
  },
)

watch(
  () => superActiveTab.value,
  (tab) => {
    if (role.value !== 'super') return
    if (tab === '用户列表') {
      loadSuperUsers()
    } else if (tab === '身份查询') {
      loadSuperUsersByType()
    } else if (tab === 'id查询') {
      superUsers.value = []
      superError.value = ''
    }
  },
)

watch(
  () => [role.value, token.value],
  ([r, t]) => {
    if (!t || !['merchant', 'user'].includes(r)) return
    refreshMallSessionFromStorage()
    if (r === 'merchant') {
      syncMerchantProfileForm()
    }
  },
  { immediate: true },
)

watch(
  () => route.fullPath,
  () => {
    if (route.name !== 'profile' || !token.value) return
    if (!['merchant', 'user'].includes(role.value)) return
    refreshMallSessionFromStorage()
    if (role.value === 'merchant') {
      syncMerchantProfileForm()
    }
  },
)

onMounted(() => {
  if (!token.value || !['merchant', 'user'].includes(role.value)) return
  refreshMallSessionFromStorage()
  if (role.value === 'merchant') {
    syncMerchantProfileForm()
  }
})

watch(
  () => merchantActiveTab.value,
  (tab) => {
    if (role.value === 'merchant' && tab === '申请列表') {
      loadMerchantApplyList()
      return
    }
    if (role.value === 'merchant' && tab === '我的商品') {
      loadMerchantGoodsList()
    }
  },
)
</script>

<template>
  <div class="profile-page">
    <section v-if="!token" class="forbidden-card">
      <h1>请先登录后查看个人主页</h1>
      <p>当前未检测到登录态，请先完成登录。</p>
      <div class="action-row">
        <button class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
        <button class="btn primary" @click="goLogin">去登录</button>
      </div>
    </section>

    <div v-else-if="isAdminCenterRole" class="profile-layout admin-layout">
      <aside class="sidebar">
        <div class="sidebar-head">
          <p class="eyebrow">GLOBALMALL CENTER</p>
          <h2>个人中心</h2>
          <p class="welcome">欢迎你，{{ displayName }}</p>
        </div>
        <nav v-if="adminSidebarItems.length" class="menu">
          <button
            v-for="item in adminSidebarItems"
            :key="item"
            type="button"
            class="menu-item"
            :class="{ active: isAdminTabActive(item) }"
            @click="setAdminTab(item)"
          >
            {{ item }}
          </button>
        </nav>
      </aside>

      <main class="content">
        <header class="content-head">
          <div>
            <p class="role-tag">{{ roleLabel }}</p>
            <h1>账户主页</h1>
          </div>
          <div class="head-actions">
            <button class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
            <button class="btn ghost" @click="logout">退出登录</button>
          </div>
        </header>

        <section v-if="showAdminHome" class="cards">
          <article class="card">
            <p class="card-label">昵称</p>
            <p class="card-value">{{ displayName }}</p>
          </article>
          <article class="card">
            <p class="card-label">用户名</p>
            <p class="card-value">{{ username || '-' }}</p>
          </article>
          <article class="card">
            <p class="card-label">用户ID</p>
            <p class="card-value">{{ userId || '-' }}</p>
          </article>
        </section>

        <section v-if="showAdminHome" class="panel">
          <h3>工作台</h3>
          <p>该区域保留 logistic 风格布局，适配 `super`、`manager`、`reviewer` 管理角色。</p>
        </section>

        <section v-if="role === 'reviewer' && reviewerActiveTab === '注册申请'" class="panel">
          <h3>注册申请</h3>
          <div class="register-filter">
            <label>
              页码
              <input v-model.number="registerPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              条数
              <input v-model.number="registerPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadRegisterApplications">刷新</button>
          </div>
          <p v-if="registerAppsLoading" class="panel-tip">加载中...</p>
          <p v-else-if="registerAppsError" class="panel-tip error">{{ registerAppsError }}</p>
          <p v-else-if="registerApps.length === 0" class="panel-tip">暂无注册申请</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>身份</th>
                  <th>手机号</th>
                  <th>Mall审核</th>
                  <th>Logistic审核</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in registerApps" :key="row.id">
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || '-' }}</td>
                  <td>{{ row.userType }}</td>
                  <td>{{ row.phone }}</td>
                  <td>{{ registerStatusText(row.status) }}</td>
                  <td class="op-cell">
                    <template v-if="isPendingRegister(row)">
                      <button class="op-btn pass" :disabled="actingPhone === row.phone" @click="approveRow(row)">通过</button>
                      <button class="op-btn reject" :disabled="actingPhone === row.phone" @click="rejectRow(row)">退回</button>
                    </template>
                    <span v-else class="reviewed-tag">{{ reviewedText(row) }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'reviewer' && reviewerActiveTab === '上架申请'" class="panel">
          <h3>上架申请</h3>
          <div class="register-filter">
            <label>
              页码
              <input v-model.number="goodsApplyPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              条数
              <input v-model.number="goodsApplyPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadGoodsApplications">刷新</button>
          </div>
          <p v-if="goodsApplyLoading" class="panel-tip">加载中...</p>
          <p v-else-if="goodsApplyError" class="panel-tip error">{{ goodsApplyError }}</p>
          <p v-else-if="goodsApplyList.length === 0" class="panel-tip">暂无上架申请</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>申请ID</th>
                  <th>商家ID</th>
                  <th>商品名</th>
                  <th>价格</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in goodsApplyList" :key="row.id || row.applyId">
                  <td>{{ row.applyId }}</td>
                  <td>{{ row.merchantId }}</td>
                  <td>{{ row.skuName }}</td>
                  <td>{{ row.price }}</td>
                  <td>{{ mallGoodsApplyStatusText(row.mallStatus) }}</td>
                  <td>{{ logisticGoodsApplyStatusText(row.logisticStatus) }}</td>
                  <td class="op-cell">
                    <template v-if="canOperateGoodsApply(row)">
                      <button class="op-btn pass" :disabled="actingApplyId === row.applyId" @click="approveGoodsRow(row)">通过</button>
                      <button class="op-btn reject" :disabled="actingApplyId === row.applyId" @click="rejectGoodsRow(row)">退回</button>
                    </template>
                    <span v-else class="reviewed-tag">已审核</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'super' && superActiveTab === '用户列表'" class="panel">
          <h3>用户列表</h3>
          <div class="register-filter">
            <label>
              页码
              <input v-model.number="superPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              条数
              <input v-model.number="superPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadSuperUsers">刷新</button>
          </div>
          <p v-if="superLoading" class="panel-tip">加载中...</p>
          <p v-else-if="superError" class="panel-tip error">{{ superError }}</p>
          <p v-else-if="superUsers.length === 0" class="panel-tip">暂无用户数据</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>身份</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in superUsers" :key="row.userId">
                  <td>{{ row.userId }}</td>
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || '-' }}</td>
                  <td>{{ userTypeText(row.userType) }}</td>
                  <td>
                    <span class="status-tag" :class="{ disabled: Number(row.status) === 0 }">{{ userStatusText(row.status) }}</span>
                  </td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openEditDialog(row)">更新</button>
                    <button class="op-btn reject" @click="openDeleteDialog(row)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'super' && superActiveTab === '身份查询'" class="panel">
          <h3>身份查询</h3>
          <div class="register-filter">
            <label>
              身份
              <select v-model="superQueryType" class="mini-input">
                <option value="1">超级管理员</option>
                <option value="2">管理员</option>
                <option value="3">商家</option>
                <option value="4">普通用户</option>
                <option value="5">审核员</option>
              </select>
            </label>
            <label>
              页码
              <input v-model.number="superTypePageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              条数
              <input v-model.number="superTypePageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadSuperUsersByType">查询</button>
          </div>
          <p v-if="superLoading" class="panel-tip">加载中...</p>
          <p v-else-if="superError" class="panel-tip error">{{ superError }}</p>
          <p v-else-if="superUsers.length === 0" class="panel-tip">暂无匹配数据</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>身份</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in superUsers" :key="row.userId">
                  <td>{{ row.userId }}</td>
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || '-' }}</td>
                  <td>{{ userTypeText(row.userType) }}</td>
                  <td>
                    <span class="status-tag" :class="{ disabled: Number(row.status) === 0 }">{{ userStatusText(row.status) }}</span>
                  </td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openEditDialog(row)">更新</button>
                    <button class="op-btn reject" @click="openDeleteDialog(row)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'super' && superActiveTab === 'id查询'" class="panel">
          <h3>ID查询</h3>
          <div class="register-filter">
            <label>
              用户ID
              <input v-model="superQueryUserId" type="text" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadSuperUserById">查询</button>
          </div>
          <p v-if="superLoading" class="panel-tip">加载中...</p>
          <p v-else-if="superError" class="panel-tip error">{{ superError }}</p>
          <p v-else-if="superUsers.length === 0" class="panel-tip">未找到该用户</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>身份</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in superUsers" :key="row.userId">
                  <td>{{ row.userId }}</td>
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || '-' }}</td>
                  <td>{{ userTypeText(row.userType) }}</td>
                  <td>
                    <span class="status-tag" :class="{ disabled: Number(row.status) === 0 }">{{ userStatusText(row.status) }}</span>
                  </td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openEditDialog(row)">更新</button>
                    <button class="op-btn reject" @click="openDeleteDialog(row)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

      </main>
    </div>

    <div v-else-if="isMallCenterRole" class="profile-layout mall-layout">
      <aside class="amazon-side">
        <h2>你的账户</h2>
        <p class="welcome">Hi，{{ displayName }}</p>
        <p class="sub">角色：{{ roleLabel }}</p>
        <button class="btn ghost side-btn" @click="router.push({ name: 'home' })">继续逛商城</button>
        <div v-if="role === 'merchant'" class="merchant-side-tabs">
          <button
            v-for="item in merchantSidebarItems"
            :key="item"
            type="button"
            class="btn ghost side-btn merchant-side-tab"
            :class="{ active: merchantActiveTab === item }"
            @click="setMerchantTab(item)"
          >
            {{ item }}
          </button>
        </div>
      </aside>

      <main class="amazon-main">
        <header class="amazon-head">
          <div>
            <p class="amazon-eyebrow">GLOBALMALL ACCOUNT</p>
            <h1>Amazon 风个人主页</h1>
          </div>
          <div class="head-actions">
            <button class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
            <button class="btn ghost" @click="logout">退出登录</button>
          </div>
        </header>

        <section class="amazon-grid">
          <article class="amazon-card">
            <h3>账户信息</h3>
            <p>用户名：{{ username || '-' }}</p>
            <p>用户ID：{{ userId || '-' }}</p>
            <p>手机号：{{ mallSessionPhone || '—' }}</p>
            <p v-if="mallSessionCity">所在城市：{{ mallSessionCity }}</p>
          </article>
          <article class="amazon-card">
            <h3>安全与支付</h3>
            <p>管理登录信息、支付方式与隐私偏好。</p>
          </article>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === '账户概览'" class="amazon-panel merchant-edit">
          <h3>修改信息</h3>
          <p class="merchant-edit-hint">修改后将同步到系统账号资料（需商家登录态）。</p>
          <p v-if="merchantProfileTip" class="merchant-edit-msg">{{ merchantProfileTip }}</p>
          <p v-if="merchantProfileError" class="merchant-edit-msg error">{{ merchantProfileError }}</p>
          <form class="merchant-edit-form" @submit.prevent="submitMerchantProfile">
            <label class="merchant-field">
              <span>用户 ID</span>
              <input type="text" class="merchant-input" :value="userId || '-'" disabled />
            </label>
            <label class="merchant-field">
              <span>昵称</span>
              <input v-model="merchantProfileForm.nickname" type="text" class="merchant-input" autocomplete="nickname" />
            </label>
            <label class="merchant-field">
              <span>手机号</span>
              <input v-model="merchantProfileForm.phone" type="text" class="merchant-input" autocomplete="tel" />
            </label>
            <label class="merchant-field">
              <span>所在城市</span>
              <input v-model="merchantProfileForm.city" type="text" class="merchant-input" autocomplete="address-level2" />
            </label>
            <label class="merchant-field">
              <span>新密码</span>
              <input
                v-model="merchantProfileForm.password"
                type="password"
                class="merchant-input"
                autocomplete="new-password"
                placeholder="不修改请留空"
              />
            </label>
            <div class="merchant-edit-actions">
              <button type="submit" class="btn primary" :disabled="merchantProfileSubmitting">
                {{ merchantProfileSubmitting ? '保存中…' : '保存修改' }}
              </button>
            </div>
          </form>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === '账户概览'" class="amazon-panel merchant-apply-entry">
          <h3>申请上架</h3>
          <p class="merchant-edit-hint">填写商品资料并提交审核，请在专用页面完成操作。</p>
          <button type="button" class="btn primary merchant-apply-btn" @click="router.push({ name: 'merchant-apply-goods' })">
            前往申请上架
          </button>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === '账户概览'" class="amazon-panel merchant-off-shelf-entry">
          <h3>下架申请</h3>
          <p class="merchant-edit-hint">输入商品ID提交下架申请，提交后请在时效内完成支付。</p>
          <p v-if="offShelfTip" class="merchant-edit-msg">{{ offShelfTip }}</p>
          <p v-if="offShelfError" class="merchant-edit-msg error">{{ offShelfError }}</p>
          <form class="merchant-edit-form merchant-off-shelf-form" @submit.prevent="submitOffShelfApply">
            <label class="merchant-field">
              <span>商品ID</span>
              <input
                v-model="offShelfGoodsId"
                type="text"
                class="merchant-input"
                autocomplete="off"
                placeholder="请输入需要下架的商品ID"
              />
            </label>
            <div class="merchant-edit-actions">
              <button type="submit" class="btn primary" :disabled="offShelfSubmitting">
                {{ offShelfSubmitting ? '提交中…' : '提交下架申请' }}
              </button>
            </div>
          </form>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === '申请列表'" class="amazon-panel">
          <h3>申请列表</h3>
          <div class="register-filter">
            <label>
              页码
              <input v-model.number="merchantApplyListPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              条数
              <input v-model.number="merchantApplyListPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadMerchantApplyList">刷新</button>
          </div>
          <p v-if="merchantApplyListLoading" class="panel-tip">加载中...</p>
          <p v-else-if="merchantApplyListError" class="panel-tip error">{{ merchantApplyListError }}</p>
          <p v-else-if="merchantApplyList.length === 0" class="panel-tip">暂无申请记录</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>申请ID</th>
                  <th>商品名</th>
                  <th>Mall审核</th>
                  <th>Logistic审核</th>
                  <th>支付状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in merchantApplyList" :key="row.id || row.applyId">
                  <td>{{ row.applyId }}</td>
                  <td>{{ row.skuName }}</td>
                  <td>{{ mallGoodsApplyStatusText(row.mallStatus) }}</td>
                  <td>{{ logisticGoodsApplyStatusText(row.logisticStatus) }}</td>
                  <td>{{ payStatusText(row.isPay) }}</td>
                  <td class="op-cell">
                    <button v-if="canPayApply(row)" class="op-btn pass" @click="goPayApply(row)">前往支付</button>
                    <button v-else-if="canViewLogistic(row)" class="op-btn info" @click="viewLogistic(row)">查看物流</button>
                    <span v-else class="reviewed-tag">-</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === '我的商品'" class="amazon-panel">
          <h3>我的商品</h3>
          <div class="register-filter">
            <label>
              页码
              <input v-model.number="merchantGoodsPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              条数
              <input v-model.number="merchantGoodsPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadMerchantGoodsList">刷新</button>
          </div>
          <p v-if="merchantGoodsListLoading" class="panel-tip">加载中...</p>
          <p v-else-if="merchantGoodsListError" class="panel-tip error">{{ merchantGoodsListError }}</p>
          <p v-else-if="merchantGoodsList.length === 0" class="panel-tip">暂无商品记录</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>商品ID</th>
                  <th>商品名</th>
                  <th>SKU编码</th>
                  <th>价格</th>
                  <th>分类</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in merchantGoodsList" :key="row.id || row.goodsId">
                  <td>{{ row.goodsId || '-' }}</td>
                  <td>{{ row.skuName || '-' }}</td>
                  <td>{{ row.skuCode || '-' }}</td>
                  <td>{{ row.price ?? '-' }}</td>
                  <td>{{ categoryText(row.category) }}</td>
                  <td>{{ portalGoodsStatusText(row.status) }}</td>
                  <td class="op-cell">
                    <button v-if="canViewGoodsDetail(row)" class="op-btn info" @click="goGoodsDetail(row)">前往详情</button>
                    <span v-else class="reviewed-tag">-</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>

    <section v-else class="forbidden-card">
      <h1>当前角色暂无个人中心模板</h1>
      <p>当前登录角色为：{{ roleLabel }}。如需使用请切换为已支持角色。</p>
      <div class="action-row">
        <button class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
        <button class="btn primary" @click="goLogin">切换账号</button>
      </div>
    </section>

    <div v-if="editDialogVisible" class="dialog-mask" @click.self="editDialogVisible = false">
      <div class="dialog-card">
        <h3 class="dialog-title">更新用户</h3>
        <div class="dialog-grid">
          <label>用户ID<input v-model="editForm.userId" class="dialog-input" disabled /></label>
          <label>用户名<input v-model="editForm.username" class="dialog-input" /></label>
          <label>昵称<input v-model="editForm.nickname" class="dialog-input" /></label>
          <label>身份
            <select v-model="editForm.userType" class="dialog-input">
              <option value="1">超级管理员</option>
              <option value="2">管理员</option>
              <option value="3">商家</option>
              <option value="4">普通用户</option>
              <option value="5">审核员</option>
            </select>
          </label>
          <label>状态
            <select v-model="editForm.status" class="dialog-input">
              <option value="1">启用</option>
              <option value="0">禁用</option>
            </select>
          </label>
        </div>
        <div class="dialog-actions">
          <button class="btn ghost" @click="editDialogVisible = false">取消</button>
          <button class="btn primary" :disabled="editSubmitting" @click="submitEdit">保存</button>
        </div>
      </div>
    </div>

    <div v-if="deleteDialogVisible" class="dialog-mask" @click.self="deleteDialogVisible = false">
      <div class="dialog-card delete-card">
        <h3 class="dialog-title">确认删除用户</h3>
        <p class="dialog-text">删除后无法恢复，请确认是否删除用户：<strong>{{ deleteTarget?.username }}</strong>（{{ deleteTarget?.userId }}）</p>
        <div class="dialog-actions">
          <button class="btn ghost" @click="deleteDialogVisible = false">取消</button>
          <button class="btn danger" :disabled="deleteSubmitting" @click="confirmDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: radial-gradient(circle at 20% -10%, rgba(255, 138, 0, 0.18), transparent 42%), #09090b;
  padding: 24px;
}

.forbidden-card {
  max-width: 720px;
  margin: 120px auto 0;
  background: #16161b;
  border: 1px solid var(--mall-border);
  border-radius: 14px;
  padding: 28px;
}

.forbidden-card h1 {
  margin: 0 0 12px;
  color: var(--mall-text);
}

.forbidden-card p {
  margin: 0;
  color: var(--mall-text-muted);
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.profile-layout {
  display: flex;
  min-height: calc(100vh - 48px);
  border: 1px solid #2c2c34;
  border-radius: 16px;
  overflow: hidden;
}

.admin-layout {
  background: linear-gradient(160deg, #0d0d11 0%, #131318 55%, #0f0f13 100%);
}

.sidebar {
  width: 260px;
  flex-shrink: 0;
  border-right: 1px solid #2f2f39;
  background: linear-gradient(180deg, #101015 0%, #15151b 100%);
  padding: 24px 14px;
}

.sidebar-head {
  padding: 0 10px 20px;
  border-bottom: 1px solid #2f2f39;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(255, 167, 38, 0.65);
}

.sidebar-head h2 {
  margin: 0;
  font-size: 22px;
}

.welcome {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--mall-text-muted);
}

.menu {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.menu-item {
  width: 100%;
  border: 1px solid rgba(255, 138, 0, 0.35);
  border-radius: 8px;
  background: rgba(255, 138, 0, 0.1);
  color: #ffd8a6;
  padding: 10px 12px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
}

.menu-item.active {
  background: rgba(255, 138, 0, 0.2);
  border-color: rgba(255, 138, 0, 0.55);
  color: #ffe5c5;
}


.content {
  flex: 1;
  padding: 24px 30px;
}

.content-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.content-head h1 {
  margin: 8px 0 0;
  font-size: 30px;
}

.role-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 138, 0, 0.14);
  border: 1px solid rgba(255, 138, 0, 0.45);
  color: var(--mall-orange-bright);
  font-size: 12px;
  margin: 0;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.card {
  background: #17171d;
  border: 1px solid #2d2d36;
  border-radius: 12px;
  padding: 16px;
}

.card-label {
  margin: 0;
  color: var(--mall-text-muted);
  font-size: 13px;
}

.card-value {
  margin: 10px 0 0;
  font-size: 18px;
  font-weight: 600;
}

.panel {
  margin-top: 14px;
  background: #17171d;
  border: 1px solid #2d2d36;
  border-radius: 12px;
  padding: 18px;
}

.panel h3 {
  margin: 0;
}

.panel p {
  margin: 10px 0 0;
  color: var(--mall-text-muted);
}

.register-filter {
  display: flex;
  gap: 10px;
  align-items: end;
  flex-wrap: wrap;
}

.register-filter label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: var(--mall-text-muted);
}

.mini-input {
  width: 92px;
  height: 34px;
  border: 1px solid #3a3a44;
  border-radius: 8px;
  background: #111117;
  color: var(--mall-text);
  padding: 0 10px;
}

select.mini-input {
  width: 138px;
  padding-right: 34px;
  background-position: right 12px center;
}

.panel-tip {
  margin-top: 12px;
}

.panel-tip.error {
  color: #f87171;
}

.table-wrap {
  margin-top: 12px;
  overflow: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}

.table th,
.table td {
  border-bottom: 1px solid #2d2d36;
  text-align: left;
  padding: 10px 8px;
  font-size: 13px;
}

.table th {
  color: var(--mall-orange-bright);
  font-weight: 700;
}

.op-cell {
  display: flex;
  gap: 8px;
}

.op-btn {
  border: 1px solid #3a3a44;
  background: #121219;
  color: var(--mall-text);
  border-radius: 999px;
  font-size: 12px;
  padding: 4px 10px;
}

.op-btn.pass {
  border-color: rgba(34, 197, 94, 0.6);
  color: #86efac;
}

.op-btn.reject {
  border-color: rgba(248, 113, 113, 0.6);
  color: #fca5a5;
}

.op-btn.info {
  border-color: rgba(125, 211, 252, 0.6);
  color: #bae6fd;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  border: 1px solid rgba(34, 197, 94, 0.55);
  background: rgba(34, 197, 94, 0.15);
  color: #86efac;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
}

.status-tag.disabled {
  border-color: rgba(248, 113, 113, 0.55);
  background: rgba(248, 113, 113, 0.15);
  color: #fca5a5;
}

.reviewed-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #52525b;
  background: #27272a;
  color: #d4d4d8;
  font-size: 12px;
  font-weight: 700;
}

.mall-layout {
  background: linear-gradient(180deg, #111117 0%, #17171e 100%);
}

.amazon-side {
  width: 280px;
  flex-shrink: 0;
  background: #121219;
  border-right: 1px solid #2f2f39;
  padding: 24px 20px;
}

.amazon-side h2 {
  margin: 0;
  font-size: 26px;
  color: #fff;
}

.amazon-side .welcome {
  margin-top: 14px;
}

.amazon-side .sub {
  margin: 6px 0 0;
  color: var(--mall-orange-bright);
}

.side-btn {
  margin-top: 16px;
}

.amazon-main {
  flex: 1;
  padding: 24px 30px;
}

.amazon-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.amazon-eyebrow {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(255, 167, 38, 0.75);
}

.amazon-head h1 {
  margin: 8px 0 0;
  font-size: 30px;
}

.amazon-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.amazon-card {
  background: linear-gradient(180deg, #1b1b23 0%, #191921 100%);
  border: 1px solid #343444;
  border-radius: 12px;
  padding: 16px;
}

.amazon-card h3 {
  margin: 0 0 10px;
  color: var(--mall-orange-bright);
}

.amazon-panel {
  margin-top: 18px;
  padding: 18px;
  border-radius: 12px;
  border: 1px solid #343444;
  background: linear-gradient(180deg, #1b1b23 0%, #191921 100%);
}

.amazon-panel h3 {
  margin: 0 0 8px;
  color: var(--mall-orange-bright);
}

.merchant-edit-hint {
  margin: 0 0 12px;
  font-size: 0.85rem;
  color: var(--mall-text-muted);
}

.merchant-edit-msg {
  margin: 0 0 10px;
  font-size: 0.85rem;
  color: #86efac;
}

.merchant-edit-msg.error {
  color: #f87171;
}

.merchant-edit-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.merchant-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.8rem;
  color: var(--mall-text-muted);
}

.merchant-input {
  height: 40px;
  border-radius: 8px;
  border: 1px solid #3a3a44;
  background: #0f0f15;
  color: var(--mall-text);
  padding: 0 12px;
  font-size: 0.9rem;
}

.merchant-input:disabled {
  opacity: 0.65;
}

.merchant-edit-actions {
  grid-column: 1 / -1;
  margin-top: 4px;
}

.merchant-apply-btn {
  margin-top: 4px;
}

.merchant-side-tabs {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.merchant-side-tab.active {
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
  border-color: rgba(255, 167, 38, 0.9);
  color: #1f1f25;
}

.merchant-off-shelf-form {
  grid-template-columns: minmax(240px, 420px);
}

.amazon-card p {
  color: #dedee6;
}

.btn {
  border: none;
  border-radius: 999px;
  padding: 10px 16px;
  font-weight: 700;
}

.btn.primary {
  color: #18181b;
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
}

.btn.ghost {
  color: var(--mall-text);
  background: transparent;
  border: 1px solid #3a3a44;
}

.btn.danger {
  color: #fff;
  background: linear-gradient(180deg, #ef4444, #dc2626);
}

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(5, 5, 8, 0.7);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 90;
  padding: 16px;
}

.dialog-card {
  width: min(520px, 100%);
  border-radius: 14px;
  border: 1px solid #3a3a44;
  background: linear-gradient(180deg, #17171f 0%, #111118 100%);
  box-shadow: 0 20px 56px rgba(0, 0, 0, 0.45);
  padding: 18px;
}

.delete-card {
  border-color: rgba(239, 68, 68, 0.5);
}

.dialog-title {
  margin: 0;
  color: #ffe2bc;
}

.dialog-text {
  margin: 12px 0 0;
  color: #d4d4dc;
}

.dialog-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.dialog-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: var(--mall-text-muted);
}

.dialog-input {
  height: 38px;
  border-radius: 8px;
  border: 1px solid #3a3a44;
  background: #0f0f15;
  color: var(--mall-text);
  padding: 0 10px;
}

select.dialog-input {
  width: 100%;
  padding-right: 36px;
  background-position: right 12px center;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .profile-layout {
    flex-direction: column;
  }

  .sidebar,
  .amazon-side {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #2f2f39;
  }

  .cards,
  .amazon-grid {
    grid-template-columns: 1fr;
  }

  .merchant-edit-form {
    grid-template-columns: 1fr;
  }

  .dialog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
