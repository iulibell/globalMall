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
import MallLanguageDropdown from '@/components/MallLanguageDropdown.vue'
import { useUiLang } from '@/composables/useUiLang.js'
import { useMultiDictionary } from '@/composables/useMultiDictionary.js'
import { pageDictFallback } from '@/utils/pageDictionaryFallback.js'

const R_HOME = 'r_home'
const R_REG = 'r_register'
const R_GOODS = 'r_goods_apply'
const S_HOME = 's_home'
const S_USERS = 's_users'
const S_TYPE = 's_type'
const S_ID = 's_id'
const M_OVER = 'm_overview'
const M_APPLY_LIST = 'm_apply_list'
const M_MY_GOODS = 'm_my_goods'

const router = useRouter()
const route = useRoute()

const { uiLang } = useUiLang()
const { t } = useMultiDictionary(['page_mall', 'page_mall_profile'], uiLang)

function txp(key) {
  return t('page_mall_profile', key, pageDictFallback('page_mall_profile', key, uiLang.value))
}

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

const displayName = computed(() => {
  const nn = (nickname.value || username.value || '').trim()
  if (nn) return nn
  return txp('profile_display_unnamed')
})

const roleLabel = computed(() => {
  const map = {
    super: 'role_super',
    manager: 'role_manager',
    reviewer: 'role_reviewer',
    merchant: 'role_merchant',
    user: 'role_user',
  }
  const dk = map[role.value]
  if (dk) {
    return t('page_mall', dk, pageDictFallback('page_mall', dk, uiLang.value))
  }
  return (role.value || '').trim() || txp('profile_role_not_logged')
})

const reviewerTabs = computed(() => [
  { key: R_HOME, label: txp('profile_tab_home') },
  { key: R_REG, label: txp('profile_tab_register') },
  { key: R_GOODS, label: txp('profile_tab_goods_apply') },
])

const superTabs = computed(() => [
  { key: S_HOME, label: txp('profile_tab_home') },
  { key: S_USERS, label: txp('profile_super_tab_users') },
  { key: S_TYPE, label: txp('profile_super_tab_type') },
  { key: S_ID, label: txp('profile_super_tab_id') },
])

const adminSidebarItems = computed(() => {
  if (role.value === 'reviewer') return reviewerTabs.value
  if (role.value === 'super') return superTabs.value
  return []
})

const merchantTabs = computed(() => [
  { key: M_OVER, label: txp('profile_merchant_overview') },
  { key: M_APPLY_LIST, label: txp('profile_merchant_apply_list') },
  { key: M_MY_GOODS, label: txp('profile_merchant_my_goods') },
])

const reviewerActiveTab = ref(R_HOME)
const superActiveTab = ref(S_HOME)
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
const merchantActiveTab = ref(M_OVER)

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
    merchantProfileError.value = txp('profile_err_missing_uid')
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
      merchantProfileError.value = data?.message || txp('profile_save_failed')
      return
    }
    merchantProfileTip.value =
      typeof data?.data === 'string' ? data.data : data?.message || txp('profile_save_ok')
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
    merchantProfileError.value = txp('profile_net_portal')
  } finally {
    merchantProfileSubmitting.value = false
  }
}

async function submitOffShelfApply() {
  const goodsId = offShelfGoodsId.value.trim()
  if (!goodsId) {
    offShelfError.value = txp('profile_off_shelf_need_id')
    return
  }
  offShelfSubmitting.value = true
  offShelfTip.value = ''
  offShelfError.value = ''
  try {
    const { ok, data } = await merchantApplyForOffShelf(goodsId)
    if (!ok || data?.code !== 200) {
      offShelfError.value = data?.message || txp('profile_off_shelf_fail')
      return
    }
    offShelfTip.value = typeof data?.data === 'string' ? data.data : data?.message || txp('profile_off_shelf_ok')
    offShelfGoodsId.value = ''
  } catch {
    offShelfError.value = txp('profile_net_portal')
  } finally {
    offShelfSubmitting.value = false
  }
}

function setMerchantTab(key) {
  merchantActiveTab.value = key
  if (key === M_APPLY_LIST) {
    loadMerchantApplyList()
    return
  }
  if (key === M_MY_GOODS) {
    loadMerchantGoodsList()
  }
}

function payStatusText(v) {
  const s = Number(v)
  if (s === 0) return txp('pay_pending')
  if (s === 1) return txp('pay_paid')
  if (s === 2) return txp('pay_timeout')
  return txp('pay_unknown')
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
      merchantApplyListError.value = data?.message || txp('profile_apply_list_fail')
      return
    }
    merchantApplyList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    merchantApplyList.value = []
    merchantApplyListError.value = txp('profile_net_admin')
  } finally {
    merchantApplyListLoading.value = false
  }
}

function portalGoodsStatusText(v) {
  return Number(v) === 1 ? txp('portal_on_shelf') : txp('portal_off_shelf')
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
  return Number(v) === 1 ? txp('category_special') : txp('category_normal')
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
      merchantGoodsListError.value = data?.message || txp('profile_goods_list_fail')
      return
    }
    merchantGoodsList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    merchantGoodsList.value = []
    merchantGoodsListError.value = txp('profile_net_portal')
  } finally {
    merchantGoodsListLoading.value = false
  }
}

function isAdminTabActive(key) {
  if (role.value === 'reviewer') {
    return reviewerActiveTab.value === key
  }
  if (role.value === 'super') {
    return superActiveTab.value === key
  }
  return false
}

function setAdminTab(key) {
  if (role.value === 'reviewer') {
    reviewerActiveTab.value = key
    return
  }
  if (role.value === 'super') {
    superActiveTab.value = key
  }
}

const showAdminHome = computed(() => {
  if (role.value === 'reviewer') return reviewerActiveTab.value === R_HOME
  if (role.value === 'super') return superActiveTab.value === S_HOME
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
      registerAppsError.value = data?.message || txp('profile_reg_list_fail')
      return
    }
    registerApps.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    registerApps.value = []
    registerAppsError.value = txp('profile_net_admin')
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
  return txp('reg_done')
}

function registerStatusText(status) {
  const s = Number(status)
  if (s === 0) return txp('reg_pending')
  if (s === 1) return txp('reg_done')
  if (s === 2) return txp('reg_rejected')
  return txp('status_unknown')
}

async function approveRow(row) {
  actingPhone.value = row.phone
  try {
    const { ok, data } = await approveRegisterApplication(toRegisterPayload(row))
    if (!ok || data?.code !== 200) {
      registerAppsError.value = data?.message || txp('profile_action_fail')
      return
    }
    await loadRegisterApplications()
  } catch {
    registerAppsError.value = `${txp('profile_action_fail')}，${txp('profile_try_later')}`
  } finally {
    actingPhone.value = ''
  }
}

async function rejectRow(row) {
  actingPhone.value = row.phone
  try {
    const { ok, data } = await rejectRegisterApplication(toRegisterPayload(row))
    if (!ok || data?.code !== 200) {
      registerAppsError.value = data?.message || txp('profile_action_fail')
      return
    }
    await loadRegisterApplications()
  } catch {
    registerAppsError.value = `${txp('profile_action_fail')}，${txp('profile_try_later')}`
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
      goodsApplyError.value = data?.message || txp('profile_goods_apply_fail')
      return
    }
    goodsApplyList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    goodsApplyList.value = []
    goodsApplyError.value = txp('profile_net_admin')
  } finally {
    goodsApplyLoading.value = false
  }
}

function canOperateGoodsApply(row) {
  return Number(row?.mallStatus) === 0
}

function mallGoodsApplyStatusText(status) {
  const s = Number(status)
  if (s === 0) return txp('mall_apply_pending')
  if (s === 1) return txp('mall_apply_passed')
  if (s === 2) return txp('mall_apply_rejected')
  if (s === 3) return txp('mall_apply_cancelled')
  return txp('status_unknown')
}

function logisticGoodsApplyStatusText(status) {
  const s = Number(status)
  if (s === 0) return txp('log_apply_pending')
  if (s === 1) return txp('log_apply_passed')
  if (s === 2) return txp('log_apply_rejected')
  return txp('status_unknown')
}

function userTypeText(v) {
  const s = String(v ?? '').trim()
  if (s === '1') return txp('ut_super')
  if (s === '2') return txp('ut_manager')
  if (s === '3') return txp('ut_merchant')
  if (s === '4') return txp('ut_user')
  if (s === '5') return txp('ut_reviewer')
  return s || txp('profile_empty_dash')
}

function userStatusText(v) {
  return Number(v) === 0 ? txp('user_disabled') : txp('user_enabled')
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
      superError.value = data?.message || txp('profile_super_list_fail')
      return
    }
    superUsers.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    superUsers.value = []
    superError.value = txp('profile_net_admin')
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
      superError.value = data?.message || txp('profile_super_type_fail')
      return
    }
    superUsers.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    superUsers.value = []
    superError.value = txp('profile_net_admin')
  } finally {
    superLoading.value = false
  }
}

async function loadSuperUserById() {
  if (!superQueryUserId.value.trim()) {
    superError.value = txp('profile_super_id_empty')
    superUsers.value = []
    return
  }
  superLoading.value = true
  superError.value = ''
  try {
    const { ok, data } = await fetchSuperUserById(superQueryUserId.value.trim())
    if (!ok || data?.code !== 200) {
      superUsers.value = []
      superError.value = data?.message || txp('profile_super_id_fail')
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
    superError.value = txp('profile_net_admin')
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
      superError.value = data?.message || txp('profile_update_fail')
      return
    }
    editDialogVisible.value = false
    await reloadSuperCurrentTab()
  } catch {
    superError.value = `${txp('profile_update_fail')}，${txp('profile_try_later')}`
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
      superError.value = data?.message || txp('profile_delete_fail')
      return
    }
    deleteDialogVisible.value = false
    deleteTarget.value = null
    await reloadSuperCurrentTab()
  } catch {
    superError.value = `${txp('profile_delete_fail')}，${txp('profile_try_later')}`
  } finally {
    deleteSubmitting.value = false
  }
}

async function reloadSuperCurrentTab() {
  if (superActiveTab.value === S_USERS) {
    await loadSuperUsers()
  } else if (superActiveTab.value === S_TYPE) {
    await loadSuperUsersByType()
  } else if (superActiveTab.value === S_ID) {
    await loadSuperUserById()
  }
}

async function approveGoodsRow(row) {
  actingApplyId.value = row.applyId
  try {
    const { ok, data } = await approveGoodsApplication(row.applyId)
    if (!ok || data?.code !== 200) {
      goodsApplyError.value = data?.message || txp('profile_action_fail')
      return
    }
    await loadGoodsApplications()
  } catch {
    goodsApplyError.value = `${txp('profile_action_fail')}，${txp('profile_try_later')}`
  } finally {
    actingApplyId.value = ''
  }
}

async function rejectGoodsRow(row) {
  actingApplyId.value = row.applyId
  try {
    const { ok, data } = await rejectGoodsApplication({
      applyId: row.applyId,
      remark: row.remark || txp('goods_apply_reject_remark'),
    })
    if (!ok || data?.code !== 200) {
      goodsApplyError.value = data?.message || txp('profile_action_fail')
      return
    }
    await loadGoodsApplications()
  } catch {
    goodsApplyError.value = `${txp('profile_action_fail')}，${txp('profile_try_later')}`
  } finally {
    actingApplyId.value = ''
  }
}

watch(
  () => reviewerActiveTab.value,
  (tab) => {
    if (role.value === 'reviewer' && tab === R_REG) {
      loadRegisterApplications()
    }
    if (role.value === 'reviewer' && tab === R_GOODS) {
      loadGoodsApplications()
    }
  },
)

watch(
  () => superActiveTab.value,
  (tab) => {
    if (role.value !== 'super') return
    if (tab === S_USERS) {
      loadSuperUsers()
    } else if (tab === S_TYPE) {
      loadSuperUsersByType()
    } else if (tab === S_ID) {
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
    if (role.value === 'merchant' && tab === M_APPLY_LIST) {
      loadMerchantApplyList()
      return
    }
    if (role.value === 'merchant' && tab === M_MY_GOODS) {
      loadMerchantGoodsList()
    }
  },
)
</script>

<template>
  <div class="profile-page">
    <section v-if="!token" class="forbidden-card">
      <h1>{{ txp('profile_need_login_title') }}</h1>
      <p>{{ txp('profile_need_login_desc') }}</p>
      <div class="action-row">
        <MallLanguageDropdown />
        <button class="btn ghost" @click="router.push({ name: 'home' })">{{ txp('profile_back_home') }}</button>
        <button class="btn primary" @click="goLogin">{{ txp('profile_go_login') }}</button>
      </div>
    </section>

    <div v-else-if="isAdminCenterRole" class="profile-layout admin-layout">
      <aside class="sidebar">
        <div class="sidebar-head">
          <p class="eyebrow">{{ txp('profile_globalmall_center') }}</p>
          <h2>{{ txp('profile_center_title') }}</h2>
          <p class="welcome">{{ txp('profile_sidebar_welcome') }}{{ displayName }}</p>
        </div>
        <nav v-if="adminSidebarItems.length" class="menu">
          <button
            v-for="item in adminSidebarItems"
            :key="item.key"
            type="button"
            class="menu-item"
            :class="{ active: isAdminTabActive(item.key) }"
            @click="setAdminTab(item.key)"
          >
            {{ item.label }}
          </button>
        </nav>
      </aside>

      <main class="content">
        <header class="content-head">
          <div>
            <p class="role-tag">{{ roleLabel }}</p>
            <h1>{{ txp('profile_account_home') }}</h1>
          </div>
          <div class="head-actions">
            <MallLanguageDropdown />
            <button class="btn ghost" @click="router.push({ name: 'home' })">{{ txp('profile_back_home') }}</button>
            <button class="btn ghost" @click="logout">{{ txp('profile_logout') }}</button>
          </div>
        </header>

        <section v-if="showAdminHome" class="cards">
          <article class="card">
            <p class="card-label">{{ txp('profile_col_nickname') }}</p>
            <p class="card-value">{{ displayName }}</p>
          </article>
          <article class="card">
            <p class="card-label">{{ txp('profile_col_username') }}</p>
            <p class="card-value">{{ username || txp('profile_empty_dash') }}</p>
          </article>
          <article class="card">
            <p class="card-label">{{ txp('profile_user_id_col') }}</p>
            <p class="card-value">{{ userId || txp('profile_empty_dash') }}</p>
          </article>
        </section>

        <section v-if="showAdminHome" class="panel">
          <h3>{{ txp('profile_workbench_title') }}</h3>
          <p>{{ txp('profile_workbench_desc') }}</p>
        </section>

        <section v-if="role === 'reviewer' && reviewerActiveTab === R_REG" class="panel">
          <h3>{{ txp('profile_tab_register') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="registerPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="registerPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadRegisterApplications">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="registerAppsLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="registerAppsError" class="panel-tip error">{{ registerAppsError }}</p>
          <p v-else-if="registerApps.length === 0" class="panel-tip">{{ txp('profile_reg_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_col_username') }}</th>
                  <th>{{ txp('profile_col_nickname') }}</th>
                  <th>{{ txp('profile_col_role_type') }}</th>
                  <th>{{ txp('profile_col_phone') }}</th>
                  <th>{{ txp('profile_col_mall_audit') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in registerApps" :key="row.id">
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || txp('profile_empty_dash') }}</td>
                  <td>{{ userTypeText(row.userType) }}</td>
                  <td>{{ row.phone }}</td>
                  <td>{{ registerStatusText(row.status) }}</td>
                  <td class="op-cell">
                    <template v-if="isPendingRegister(row)">
                      <button class="op-btn pass" :disabled="actingPhone === row.phone" @click="approveRow(row)">{{ txp('profile_pass') }}</button>
                      <button class="op-btn reject" :disabled="actingPhone === row.phone" @click="rejectRow(row)">{{ txp('profile_reject') }}</button>
                    </template>
                    <span v-else class="reviewed-tag">{{ reviewedText(row) }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'reviewer' && reviewerActiveTab === R_GOODS" class="panel">
          <h3>{{ txp('profile_tab_goods_apply') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="goodsApplyPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="goodsApplyPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadGoodsApplications">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="goodsApplyLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="goodsApplyError" class="panel-tip error">{{ goodsApplyError }}</p>
          <p v-else-if="goodsApplyList.length === 0" class="panel-tip">{{ txp('profile_goods_apply_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_apply_id') }}</th>
                  <th>{{ txp('profile_merchant_id') }}</th>
                  <th>{{ txp('profile_goods_name') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_col_mall_audit') }}</th>
                  <th>{{ txp('profile_col_logistic_audit') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
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
                      <button class="op-btn pass" :disabled="actingApplyId === row.applyId" @click="approveGoodsRow(row)">{{ txp('profile_pass') }}</button>
                      <button class="op-btn reject" :disabled="actingApplyId === row.applyId" @click="rejectGoodsRow(row)">{{ txp('profile_reject') }}</button>
                    </template>
                    <span v-else class="reviewed-tag">{{ txp('profile_reviewed') }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'super' && superActiveTab === S_USERS" class="panel">
          <h3>{{ txp('profile_super_tab_users') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="superPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="superPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadSuperUsers">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="superLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="superError" class="panel-tip error">{{ superError }}</p>
          <p v-else-if="superUsers.length === 0" class="panel-tip">{{ txp('profile_user_list_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_user_id_col') }}</th>
                  <th>{{ txp('profile_col_username') }}</th>
                  <th>{{ txp('profile_col_nickname') }}</th>
                  <th>{{ txp('profile_col_role_type') }}</th>
                  <th>{{ txp('profile_status') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in superUsers" :key="row.userId">
                  <td>{{ row.userId }}</td>
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || txp('profile_empty_dash') }}</td>
                  <td>{{ userTypeText(row.userType) }}</td>
                  <td>
                    <span class="status-tag" :class="{ disabled: Number(row.status) === 0 }">{{ userStatusText(row.status) }}</span>
                  </td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openEditDialog(row)">{{ txp('profile_btn_update') }}</button>
                    <button class="op-btn reject" @click="openDeleteDialog(row)">{{ txp('profile_btn_delete') }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'super' && superActiveTab === S_TYPE" class="panel">
          <h3>{{ txp('profile_super_tab_type') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_super_query_type') }}
              <select v-model="superQueryType" class="mini-input">
                <option value="1">{{ txp('ut_super') }}</option>
                <option value="2">{{ txp('ut_manager') }}</option>
                <option value="3">{{ txp('ut_merchant') }}</option>
                <option value="4">{{ txp('ut_user') }}</option>
                <option value="5">{{ txp('ut_reviewer') }}</option>
              </select>
            </label>
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="superTypePageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="superTypePageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadSuperUsersByType">{{ txp('profile_query') }}</button>
          </div>
          <p v-if="superLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="superError" class="panel-tip error">{{ superError }}</p>
          <p v-else-if="superUsers.length === 0" class="panel-tip">{{ txp('profile_no_match') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_user_id_col') }}</th>
                  <th>{{ txp('profile_col_username') }}</th>
                  <th>{{ txp('profile_col_nickname') }}</th>
                  <th>{{ txp('profile_col_role_type') }}</th>
                  <th>{{ txp('profile_status') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in superUsers" :key="row.userId">
                  <td>{{ row.userId }}</td>
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || txp('profile_empty_dash') }}</td>
                  <td>{{ userTypeText(row.userType) }}</td>
                  <td>
                    <span class="status-tag" :class="{ disabled: Number(row.status) === 0 }">{{ userStatusText(row.status) }}</span>
                  </td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openEditDialog(row)">{{ txp('profile_btn_update') }}</button>
                    <button class="op-btn reject" @click="openDeleteDialog(row)">{{ txp('profile_btn_delete') }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'super' && superActiveTab === S_ID" class="panel">
          <h3>{{ txp('profile_super_tab_id') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_super_query_user_id') }}
              <input v-model="superQueryUserId" type="text" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadSuperUserById">{{ txp('profile_query') }}</button>
          </div>
          <p v-if="superLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="superError" class="panel-tip error">{{ superError }}</p>
          <p v-else-if="superUsers.length === 0" class="panel-tip">{{ txp('profile_user_not_found') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_user_id_col') }}</th>
                  <th>{{ txp('profile_col_username') }}</th>
                  <th>{{ txp('profile_col_nickname') }}</th>
                  <th>{{ txp('profile_col_role_type') }}</th>
                  <th>{{ txp('profile_status') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in superUsers" :key="row.userId">
                  <td>{{ row.userId }}</td>
                  <td>{{ row.username }}</td>
                  <td>{{ row.nickname || txp('profile_empty_dash') }}</td>
                  <td>{{ userTypeText(row.userType) }}</td>
                  <td>
                    <span class="status-tag" :class="{ disabled: Number(row.status) === 0 }">{{ userStatusText(row.status) }}</span>
                  </td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openEditDialog(row)">{{ txp('profile_btn_update') }}</button>
                    <button class="op-btn reject" @click="openDeleteDialog(row)">{{ txp('profile_btn_delete') }}</button>
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
        <h2>{{ txp('profile_your_account') }}</h2>
        <p class="welcome">{{ txp('profile_hi') }}{{ displayName }}</p>
        <p class="sub">{{ txp('profile_role_prefix') }}{{ roleLabel }}</p>
        <button class="btn ghost side-btn" @click="router.push({ name: 'home' })">{{ txp('profile_continue_shop') }}</button>
        <div v-if="role === 'merchant'" class="merchant-side-tabs">
          <button
            v-for="item in merchantTabs"
            :key="item.key"
            type="button"
            class="btn ghost side-btn merchant-side-tab"
            :class="{ active: merchantActiveTab === item.key }"
            @click="setMerchantTab(item.key)"
          >
            {{ item.label }}
          </button>
        </div>
      </aside>

      <main class="amazon-main">
        <header class="amazon-head">
          <div>
            <p class="amazon-eyebrow">{{ txp('profile_main_eyebrow') }}</p>
            <h1>{{ txp('profile_main_title') }}</h1>
          </div>
          <div class="head-actions">
            <MallLanguageDropdown />
            <button class="btn ghost" @click="router.push({ name: 'home' })">{{ txp('profile_back_home') }}</button>
            <button class="btn ghost" @click="logout">{{ txp('profile_logout') }}</button>
          </div>
        </header>

        <section class="amazon-grid">
          <article class="amazon-card">
            <h3>{{ txp('profile_card_info') }}</h3>
            <p>{{ txp('profile_lbl_username') }}{{ username || txp('profile_empty_dash') }}</p>
            <p>{{ txp('profile_lbl_user_id') }}{{ userId || txp('profile_empty_dash') }}</p>
            <p>{{ txp('profile_lbl_phone') }}{{ mallSessionPhone || txp('profile_em_dash') }}</p>
            <p v-if="mallSessionCity">{{ txp('profile_lbl_city') }}{{ mallSessionCity }}</p>
          </article>
          <article class="amazon-card">
            <h3>{{ txp('profile_card_security') }}</h3>
            <p>{{ txp('profile_card_security_desc') }}</p>
          </article>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === M_OVER" class="amazon-panel merchant-edit">
          <h3>{{ txp('profile_edit_title') }}</h3>
          <p class="merchant-edit-hint">{{ txp('profile_edit_hint') }}</p>
          <p v-if="merchantProfileTip" class="merchant-edit-msg">{{ merchantProfileTip }}</p>
          <p v-if="merchantProfileError" class="merchant-edit-msg error">{{ merchantProfileError }}</p>
          <form class="merchant-edit-form" @submit.prevent="submitMerchantProfile">
            <label class="merchant-field">
              <span>{{ txp('profile_field_uid') }}</span>
              <input type="text" class="merchant-input" :value="userId || txp('profile_empty_dash')" disabled />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_field_nickname') }}</span>
              <input v-model="merchantProfileForm.nickname" type="text" class="merchant-input" autocomplete="nickname" />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_field_phone') }}</span>
              <input v-model="merchantProfileForm.phone" type="text" class="merchant-input" autocomplete="tel" />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_field_city') }}</span>
              <input v-model="merchantProfileForm.city" type="text" class="merchant-input" autocomplete="address-level2" />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_field_new_pw') }}</span>
              <input
                v-model="merchantProfileForm.password"
                type="password"
                class="merchant-input"
                autocomplete="new-password"
                :placeholder="txp('profile_pw_placeholder')"
              />
            </label>
            <div class="merchant-edit-actions">
              <button type="submit" class="btn primary" :disabled="merchantProfileSubmitting">
                {{ merchantProfileSubmitting ? txp('profile_saving') : txp('profile_save_btn') }}
              </button>
            </div>
          </form>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === M_OVER" class="amazon-panel merchant-apply-entry">
          <h3>{{ txp('profile_apply_shelf_title') }}</h3>
          <p class="merchant-edit-hint">{{ txp('profile_apply_shelf_hint') }}</p>
          <button type="button" class="btn primary merchant-apply-btn" @click="router.push({ name: 'merchant-apply-goods' })">
            {{ txp('profile_apply_shelf_btn') }}
          </button>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === M_OVER" class="amazon-panel merchant-off-shelf-entry">
          <h3>{{ txp('profile_off_shelf_title') }}</h3>
          <p class="merchant-edit-hint">{{ txp('profile_off_shelf_hint') }}</p>
          <p v-if="offShelfTip" class="merchant-edit-msg">{{ offShelfTip }}</p>
          <p v-if="offShelfError" class="merchant-edit-msg error">{{ offShelfError }}</p>
          <form class="merchant-edit-form merchant-off-shelf-form" @submit.prevent="submitOffShelfApply">
            <label class="merchant-field">
              <span>{{ txp('profile_field_goods_id') }}</span>
              <input
                v-model="offShelfGoodsId"
                type="text"
                class="merchant-input"
                autocomplete="off"
                :placeholder="txp('profile_off_shelf_ph')"
              />
            </label>
            <div class="merchant-edit-actions">
              <button type="submit" class="btn primary" :disabled="offShelfSubmitting">
                {{ offShelfSubmitting ? txp('profile_submitting') : txp('profile_off_shelf_submit') }}
              </button>
            </div>
          </form>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === M_APPLY_LIST" class="amazon-panel">
          <h3>{{ txp('profile_apply_list_title') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="merchantApplyListPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="merchantApplyListPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadMerchantApplyList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="merchantApplyListLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="merchantApplyListError" class="panel-tip error">{{ merchantApplyListError }}</p>
          <p v-else-if="merchantApplyList.length === 0" class="panel-tip">{{ txp('profile_merchant_apply_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_apply_id') }}</th>
                  <th>{{ txp('profile_goods_name') }}</th>
                  <th>{{ txp('profile_col_mall_audit') }}</th>
                  <th>{{ txp('profile_col_logistic_audit') }}</th>
                  <th>{{ txp('profile_col_pay_status') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
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
                    <button v-if="canPayApply(row)" class="op-btn pass" @click="goPayApply(row)">{{ txp('profile_go_pay') }}</button>
                    <button v-else-if="canViewLogistic(row)" class="op-btn info" @click="viewLogistic(row)">{{ txp('profile_view_logistic') }}</button>
                    <span v-else class="reviewed-tag">{{ txp('profile_empty_dash') }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === M_MY_GOODS" class="amazon-panel">
          <h3>{{ txp('profile_my_goods_title') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="merchantGoodsPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="merchantGoodsPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadMerchantGoodsList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="merchantGoodsListLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="merchantGoodsListError" class="panel-tip error">{{ merchantGoodsListError }}</p>
          <p v-else-if="merchantGoodsList.length === 0" class="panel-tip">{{ txp('profile_merchant_goods_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_goods_id_col') }}</th>
                  <th>{{ txp('profile_goods_name') }}</th>
                  <th>{{ txp('profile_sku_code') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_col_category') }}</th>
                  <th>{{ txp('profile_status') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in merchantGoodsList" :key="row.id || row.goodsId">
                  <td>{{ row.goodsId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.skuName || txp('profile_empty_dash') }}</td>
                  <td>{{ row.skuCode || txp('profile_empty_dash') }}</td>
                  <td>{{ row.price ?? txp('profile_empty_dash') }}</td>
                  <td>{{ categoryText(row.category) }}</td>
                  <td>{{ portalGoodsStatusText(row.status) }}</td>
                  <td class="op-cell">
                    <button v-if="canViewGoodsDetail(row)" class="op-btn info" @click="goGoodsDetail(row)">{{ txp('profile_go_detail') }}</button>
                    <span v-else class="reviewed-tag">{{ txp('profile_empty_dash') }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>

    <section v-else class="forbidden-card">
      <h1>{{ txp('profile_unsupported_title') }}</h1>
      <p>{{ txp('profile_unsupported_desc') }}{{ roleLabel }}</p>
      <div class="action-row">
        <MallLanguageDropdown />
        <button class="btn ghost" @click="router.push({ name: 'home' })">{{ txp('profile_back_home') }}</button>
        <button class="btn primary" @click="goLogin">{{ txp('profile_switch_account') }}</button>
      </div>
    </section>

    <div v-if="editDialogVisible" class="dialog-mask" @click.self="editDialogVisible = false">
      <div class="dialog-card">
        <h3 class="dialog-title">{{ txp('profile_update_user') }}</h3>
        <div class="dialog-grid">
          <label>{{ txp('profile_user_id_col') }}<input v-model="editForm.userId" class="dialog-input" disabled /></label>
          <label>{{ txp('profile_col_username') }}<input v-model="editForm.username" class="dialog-input" /></label>
          <label>{{ txp('profile_col_nickname') }}<input v-model="editForm.nickname" class="dialog-input" /></label>
          <label>{{ txp('profile_super_query_type') }}
            <select v-model="editForm.userType" class="dialog-input">
              <option value="1">{{ txp('ut_super') }}</option>
              <option value="2">{{ txp('ut_manager') }}</option>
              <option value="3">{{ txp('ut_merchant') }}</option>
              <option value="4">{{ txp('ut_user') }}</option>
              <option value="5">{{ txp('ut_reviewer') }}</option>
            </select>
          </label>
          <label>{{ txp('profile_status') }}
            <select v-model="editForm.status" class="dialog-input">
              <option value="1">{{ txp('user_enabled') }}</option>
              <option value="0">{{ txp('user_disabled') }}</option>
            </select>
          </label>
        </div>
        <div class="dialog-actions">
          <button class="btn ghost" @click="editDialogVisible = false">{{ txp('profile_cancel') }}</button>
          <button class="btn primary" :disabled="editSubmitting" @click="submitEdit">{{ txp('profile_save') }}</button>
        </div>
      </div>
    </div>

    <div v-if="deleteDialogVisible" class="dialog-mask" @click.self="deleteDialogVisible = false">
      <div class="dialog-card delete-card">
        <h3 class="dialog-title">{{ txp('profile_delete_user') }}</h3>
        <p class="dialog-text">
          {{ txp('profile_delete_confirm') }}<strong>{{ deleteTarget?.username }}</strong>（{{ deleteTarget?.userId }}）
        </p>
        <div class="dialog-actions">
          <button class="btn ghost" @click="deleteDialogVisible = false">{{ txp('profile_cancel') }}</button>
          <button class="btn danger" :disabled="deleteSubmitting" @click="confirmDelete">{{ txp('profile_confirm_delete') }}</button>
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
  align-items: center;
  flex-wrap: wrap;
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
  align-items: center;
  flex-wrap: wrap;
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

/* 与下方 merchant 侧栏 Tab 同宽（侧栏内纵向 flex 子项会 stretch，此按钮在 tabs 容器外需单独拉满） */
.amazon-side > .side-btn {
  display: block;
  width: 100%;
  box-sizing: border-box;
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
