<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchManagerPortalGoods,
  fetchManagerPortalGoodsById,
  fetchManagerGoodsTypes,
  addManagerGoodsType,
  updateManagerGoodsType,
  deleteManagerGoodsType,
  fetchManagerBrands,
  addManagerBrand,
  updateManagerBrand,
  deleteManagerBrand,
  approveRegisterApplication,
  approveGoodsApplication,
  deleteSuperUser,
  fetchSuperUserById,
  fetchSuperUserByType,
  fetchSuperUserList,
  fetchSuperDictionaryList,
  addSuperDictionary,
  updateSuperDictionary,
  deleteSuperDictionary,
  launchSeckillActivity,
  fetchMerchantOwnGoodsApplications,
  fetchMerchantGoodsApplications,
  fetchReviewerRegisterApplications,
  rejectGoodsApplication,
  rejectRegisterApplication,
  updateSuperUser,
} from '@/api/admin'
import {
  cancelUserOrder,
  confirmMerchantOrderReceived,
  confirmUserOrderReceived,
  createOrderFromCart,
  deleteUserCartItem,
  fetchMerchantSeckillActivities,
  fetchMerchantPortalGoods,
  fetchMerchantOffShelfList,
  merchantApplySeckillActivity,
  fetchUserOrderList,
  fetchUserCartList,
  userUpdateInfo,
  merchantApplyForOffShelf,
  merchantUpdateInfo,
} from '@/api/portal'
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
const S_DICT = 's_dict'
const S_ACTIVITY = 's_activity'
const SUPER_SECKILL_PLAN_STORAGE_KEY = 'super-seckill-plan'
const MG_GOODS = 'mg_goods'
const MG_QUERY = 'mg_query'
const MG_TYPE = 'mg_type'
const MG_BRAND = 'mg_brand'
const M_OVER = 'm_overview'
const M_APPLY_SHELF = 'm_apply_shelf'
const M_APPLY_LIST = 'm_apply_list'
const M_MY_GOODS = 'm_my_goods'
const M_OFF_SHELF = 'm_off_shelf'
const M_ACTIVITY_APPLY = 'm_activity_apply'
const U_HOME = 'u_home'
const U_ORDER = 'u_order'
const U_CART = 'u_cart'

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
  { key: S_DICT, label: txp('profile_super_tab_dict') },
  { key: S_ACTIVITY, label: txp('profile_super_tab_launch_activity') },
])

const managerTabs = computed(() => [
  { key: S_HOME, label: txp('profile_tab_home') },
  { key: MG_GOODS, label: txp('profile_manager_tab_goods_list') },
  { key: MG_QUERY, label: txp('profile_manager_tab_goods_query') },
  { key: MG_TYPE, label: txp('profile_manager_tab_type_list') },
  { key: MG_BRAND, label: txp('profile_manager_tab_brand_list') },
])

const adminSidebarItems = computed(() => {
  if (role.value === 'reviewer') return reviewerTabs.value
  if (role.value === 'super') return superTabs.value
  if (role.value === 'manager') return managerTabs.value
  return []
})

const merchantTabs = computed(() => [
  { key: M_OVER, label: txp('profile_merchant_overview') },
  { key: M_APPLY_SHELF, label: txp('profile_apply_shelf_title') },
  { key: M_ACTIVITY_APPLY, label: txp('profile_merchant_activity_apply') },
  { key: M_OFF_SHELF, label: txp('profile_off_shelf_title') },
  { key: M_APPLY_LIST, label: txp('profile_merchant_apply_list') },
  { key: M_MY_GOODS, label: txp('profile_merchant_my_goods') },
])

const userTabs = computed(() => [
  { key: U_HOME, label: txp('profile_tab_home') },
  { key: U_ORDER, label: txp('profile_user_tab_my_orders') },
  { key: U_CART, label: txp('profile_user_tab_my_cart') },
])

const reviewerActiveTab = ref(R_HOME)
const superActiveTab = ref(S_HOME)
const managerActiveTab = ref(S_HOME)
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
const superDictLoading = ref(false)
const superDictError = ref('')
const superDictList = ref([])
const superDictPageNum = ref(1)
const superDictPageSize = ref(10)
const superDictDialogVisible = ref(false)
const superDictSubmitting = ref(false)
const superDictDialogMode = ref('add')
const superDictDeleteDialogVisible = ref(false)
const superDictDeleteSubmitting = ref(false)
const superDictDialogError = ref('')
const superDictDeleteError = ref('')
const superSeckillTip = ref('')
const superDictForm = ref({
  dictType: '',
  dictValue: '',
  dictName: '',
  lang: '1',
  sort: 0,
  status: 1,
})
const superDictDeleteTarget = ref(null)
const superSeckillForm = ref({
  activityName: '',
  startTime: '',
  endTime: '',
})
const superSeckillStartInputRef = ref(null)
const superSeckillEndInputRef = ref(null)
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
const userActiveTab = ref(U_HOME)

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
const offShelfConfirmVisible = ref(false)
const offShelfConfirmLoading = ref(false)
const offShelfConfirmError = ref('')
const offShelfConfirmSnapshot = ref({ goodsId: '', city: '', row: null })
const offShelfListLoading = ref(false)
const offShelfListError = ref('')
const offShelfList = ref([])
const offShelfPageNum = ref(1)
const offShelfPageSize = ref(10)
const merchantApplyListLoading = ref(false)
const merchantApplyListError = ref('')
const merchantApplyList = ref([])
const merchantApplyListPageNum = ref(1)
const merchantApplyListPageSize = ref(10)
const merchantSeckillSubmitting = ref(false)
const merchantSeckillTip = ref('')
const merchantSeckillError = ref('')
const merchantSeckillListLoading = ref(false)
const merchantSeckillListError = ref('')
const merchantSeckillList = ref([])
const merchantSeckillPageNum = ref(1)
const merchantSeckillPageSize = ref(10)
const merchantSeckillForm = ref({
  goodsId: '',
  seckillPrice: '',
  totalStock: 1,
  perUserLimit: 1,
  remark: '',
})
const merchantGoodsListLoading = ref(false)
const merchantGoodsListError = ref('')
const merchantGoodsList = ref([])
const merchantGoodsPageNum = ref(1)
const merchantGoodsPageSize = ref(10)
const userCartLoading = ref(false)
const userCartError = ref('')
const userCartList = ref([])
const deletingCartId = ref('')
const creatingCartOrderId = ref('')
const userOrderLoading = ref(false)
const userOrderError = ref('')
const userOrderTip = ref('')
const userOrderList = ref([])
const userOrderPageNum = ref(1)
const userOrderPageSize = ref(10)
const cancellingOrderId = ref('')
const confirmingReceiveId = ref('')
const managerGoodsLoading = ref(false)
const managerGoodsError = ref('')
const managerGoodsList = ref([])
const managerGoodsPageNum = ref(1)
const managerGoodsPageSize = ref(10)
const managerGoodsCategory = ref('1')
const managerQueryGoodsId = ref('')
const managerQueryLoading = ref(false)
const managerQueryError = ref('')
const managerQueryResult = ref(null)
const managerTypeLoading = ref(false)
const managerTypeError = ref('')
const managerTypeList = ref([])
const managerTypePageNum = ref(1)
const managerTypePageSize = ref(10)
const managerBrandLoading = ref(false)
const managerBrandError = ref('')
const managerBrandList = ref([])
const managerBrandPageNum = ref(1)
const managerBrandPageSize = ref(10)
const managerCrudDialogVisible = ref(false)
const managerCrudSubmitting = ref(false)
const managerCrudError = ref('')
const managerCrudKind = ref('type')
const managerCrudMode = ref('add')
const managerCrudForm = ref({
  typeId: null,
  typeName: '',
  id: null,
  brandId: '',
  brandName: '',
})

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

function syncMallProfileForm() {
  if (!['merchant', 'user'].includes(role.value)) return
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

async function submitMallProfile() {
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
    const updater = role.value === 'merchant' ? merchantUpdateInfo : userUpdateInfo
    const { ok, data } = await updater(payload)
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

const offShelfConfirmImageSrc = computed(() => {
  const pic = String(offShelfConfirmSnapshot.value.row?.picture || '').trim()
  if (!pic) return ''
  if (/^https?:\/\//i.test(pic) || /^data:/i.test(pic)) return pic
  return `/src/assets/picture/${encodeURIComponent(pic)}`
})

async function resolveOffShelfGoodsRow(goodsId) {
  const gid = String(goodsId || '').trim()
  if (!gid) return null
  const local = merchantGoodsList.value.find((r) => String(r?.goodsId || '').trim() === gid)
  if (local) return local
  const merchantId = (userId.value || '').trim()
  if (!merchantId) return null
  try {
    const { ok, data } = await fetchMerchantPortalGoods({
      pageNum: 1,
      pageSize: 200,
      merchantId,
    })
    if (!ok || data?.code !== 200) return null
    const list = Array.isArray(data?.data) ? data.data : []
    return list.find((r) => String(r?.goodsId || '').trim() === gid) || null
  } catch {
    return null
  }
}

function closeOffShelfConfirm() {
  offShelfConfirmVisible.value = false
  offShelfConfirmError.value = ''
}

async function onOffShelfFormSubmit() {
  const goodsId = offShelfGoodsId.value.trim()
  const city = String(mallSessionCity.value || merchantProfileForm.value.city || '').trim()
  if (!goodsId) {
    offShelfError.value = txp('profile_off_shelf_need_id')
    return
  }
  if (!city) {
    offShelfError.value = txp('profile_off_shelf_city_required')
    return
  }
  offShelfError.value = ''
  offShelfConfirmError.value = ''
  offShelfConfirmLoading.value = true
  try {
    const row = await resolveOffShelfGoodsRow(goodsId)
    offShelfConfirmSnapshot.value = { goodsId, city, row }
    offShelfConfirmVisible.value = true
  } finally {
    offShelfConfirmLoading.value = false
  }
}

async function submitOffShelfApply() {
  const { goodsId, city } = offShelfConfirmSnapshot.value
  if (!goodsId || !city) {
    closeOffShelfConfirm()
    return
  }
  offShelfSubmitting.value = true
  offShelfTip.value = ''
  offShelfError.value = ''
  offShelfConfirmError.value = ''
  try {
    const { ok, data } = await merchantApplyForOffShelf(goodsId, city)
    if (!ok || data?.code !== 200) {
      offShelfConfirmError.value = data?.message || txp('profile_off_shelf_fail')
      return
    }
    offShelfTip.value = typeof data?.data === 'string' ? data.data : data?.message || txp('profile_off_shelf_ok')
    offShelfGoodsId.value = ''
    closeOffShelfConfirm()
    await loadMerchantOffShelfList()
  } catch {
    offShelfConfirmError.value = txp('profile_net_portal')
  } finally {
    offShelfSubmitting.value = false
  }
}

function offShelfStatusText(status) {
  const s = Number(status)
  if (s === 0) return txp('reg_pending')
  if (s === 1) return txp('pay_pending')
  if (s === 2) return txp('pay_paid')
  if (s === 3) return txp('pay_timeout')
  if (s === 4) return txp('profile_off_shelf_done')
  return txp('status_unknown')
}

function canPayOffShelf(row) {
  const fee = Number(row?.fee ?? Number.NaN)
  return Number(row?.status) === 1 && Number.isFinite(fee) && fee > 0
}

function canViewOffShelfLogistic(row) {
  return Number(row?.status) >= 2 && String(row?.transportOrderId || '').trim() !== ''
}

/** 下架申请「操作」列：不可支付时的说明（勿用 profile_reviewed，易与审核通过混淆） */
function offShelfActionPlaceholder(row) {
  const s = Number(row?.status)
  if (s === 0) return txp('profile_off_shelf_op_wait_reviewer')
  if (s === 1) {
    const fee = Number(row?.fee ?? Number.NaN)
    if (!Number.isFinite(fee) || fee <= 0) return txp('profile_off_shelf_op_wait_fee')
  }
  if (s === 2) return txp('pay_paid')
  if (s === 3) return txp('pay_timeout')
  if (s === 4) return txp('profile_off_shelf_done')
  return txp('profile_empty_dash')
}

async function loadMerchantOffShelfList() {
  offShelfListLoading.value = true
  offShelfListError.value = ''
  try {
    const { ok, data } = await fetchMerchantOffShelfList({
      pageNum: offShelfPageNum.value,
      pageSize: offShelfPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      offShelfList.value = []
      offShelfListError.value = data?.message || txp('profile_off_shelf_list_fail')
      return
    }
    offShelfList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    offShelfList.value = []
    offShelfListError.value = txp('profile_net_portal')
  } finally {
    offShelfListLoading.value = false
  }
}

async function payOffShelfRow(row) {
  const offShelfId = row?.id
  if (offShelfId == null) return
  const fee = Number(row?.fee ?? Number.NaN)
  if (!Number.isFinite(fee) || fee <= 0 || Number(row?.status) !== 1) {
    offShelfListError.value = txp('profile_off_shelf_fee_invalid')
    return
  }
  sessionStorage.setItem('merchant_off_shelf_pay_row', JSON.stringify(row))
  router.push({
    name: 'merchant-off-shelf-pay',
    query: { offShelfId: String(offShelfId) },
  })
}

function setMerchantTab(key) {
  merchantActiveTab.value = key
  if (key === M_ACTIVITY_APPLY) {
    loadMerchantSeckillActivities()
    return
  }
  if (key === M_APPLY_LIST) {
    loadMerchantApplyList()
    return
  }
  if (key === M_MY_GOODS) {
    loadMerchantGoodsList()
  }
}

function seckillStatusText(v) {
  const s = Number(v)
  if (s === 1) return '待审核'
  if (s === 2) return '未过审'
  if (s === 3) return '已过审'
  return txp('status_unknown')
}

function datetimeLocalToIso(v) {
  const raw = String(v || '').trim()
  if (!raw) return ''
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return ''
  return d.toISOString()
}

function getSuperSeckillPlanWindow() {
  try {
    const raw = window.localStorage.getItem(SUPER_SECKILL_PLAN_STORAGE_KEY)
    if (!raw) return { activityName: '', startTime: '', endTime: '' }
    const plan = JSON.parse(raw)
    return {
      activityName: String(plan?.activityName || '').trim(),
      startTime: datetimeLocalToIso(plan?.startTime),
      endTime: datetimeLocalToIso(plan?.endTime),
    }
  } catch {
    return { activityName: '', startTime: '', endTime: '' }
  }
}

async function submitMerchantSeckillApply() {
  const planWindow = getSuperSeckillPlanWindow()
  const payload = {
    goodsId: String(merchantSeckillForm.value.goodsId || '').trim(),
    activityName: planWindow.activityName,
    seckillPrice: Number(merchantSeckillForm.value.seckillPrice),
    totalStock: Number(merchantSeckillForm.value.totalStock),
    perUserLimit: Number(merchantSeckillForm.value.perUserLimit),
    startTime: planWindow.startTime,
    endTime: planWindow.endTime,
    remark: String(merchantSeckillForm.value.remark || '').trim(),
  }
  if (!payload.activityName || !payload.startTime || !payload.endTime) {
    merchantSeckillError.value = txp('profile_merchant_activity_window_missing')
    return
  }
  if (!payload.goodsId || !payload.startTime || !payload.endTime
      || !Number.isFinite(payload.seckillPrice) || payload.seckillPrice <= 0
      || !Number.isFinite(payload.totalStock) || payload.totalStock <= 0
      || !Number.isFinite(payload.perUserLimit) || payload.perUserLimit <= 0) {
    merchantSeckillError.value = txp('profile_merchant_activity_required')
    return
  }
  if (new Date(payload.startTime).getTime() >= new Date(payload.endTime).getTime()) {
    merchantSeckillError.value = txp('profile_super_activity_time_invalid')
    return
  }
  const nowTs = Date.now()
  const startTs = new Date(payload.startTime).getTime()
  const endTs = new Date(payload.endTime).getTime()
  if (nowTs < startTs || nowTs >= endTs) {
    merchantSeckillError.value = txp('profile_merchant_activity_not_in_window')
    return
  }
  merchantSeckillSubmitting.value = true
  merchantSeckillTip.value = ''
  merchantSeckillError.value = ''
  try {
    const { ok, data } = await merchantApplySeckillActivity(payload)
    if (!ok || data?.code !== 200) {
      merchantSeckillError.value = data?.message || txp('profile_merchant_activity_apply_fail')
      return
    }
    merchantSeckillTip.value = data?.message || txp('profile_merchant_activity_apply_ok')
    merchantSeckillForm.value = {
      goodsId: '',
      seckillPrice: '',
      totalStock: 1,
      perUserLimit: 1,
      remark: '',
    }
    await loadMerchantSeckillActivities()
  } catch {
    merchantSeckillError.value = txp('profile_net_portal')
  } finally {
    merchantSeckillSubmitting.value = false
  }
}

async function loadMerchantSeckillActivities() {
  merchantSeckillListLoading.value = true
  merchantSeckillListError.value = ''
  try {
    const { ok, data } = await fetchMerchantSeckillActivities({
      pageNum: merchantSeckillPageNum.value,
      pageSize: merchantSeckillPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      merchantSeckillList.value = []
      merchantSeckillListError.value = data?.message || txp('profile_merchant_activity_list_fail')
      return
    }
    merchantSeckillList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    merchantSeckillList.value = []
    merchantSeckillListError.value = txp('profile_net_portal')
  } finally {
    merchantSeckillListLoading.value = false
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

function canViewUserOrderLogistic(row) {
  return String(row?.transportOrderId || '').trim() !== ''
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
  const s = Number(v)
  if (s === 1) return txp('portal_on_shelf')
  if (s === 2) return txp('portal_removed')
  return txp('portal_off_shelf')
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

function goodsTypeText(v) {
  const raw = String(v ?? '').trim()
  if (!raw) return txp('profile_empty_dash')
  const matched = managerTypeList.value.find((item) => String(item?.typeId) === raw)
  if (matched?.typeName) return matched.typeName
  const asNum = Number(raw)
  if (!Number.isNaN(asNum)) return txp('profile_empty_dash')
  return raw
}

async function ensureManagerTypeLookup() {
  if (managerTypeList.value.length > 0) return
  try {
    const { ok, data } = await fetchManagerGoodsTypes({ pageNum: 1, pageSize: 200 })
    if (ok && data?.code === 200 && Array.isArray(data?.data)) {
      managerTypeList.value = data.data
    }
  } catch {
    // ignore: fallback handled by goodsTypeText
  }
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

async function loadUserCartList() {
  userCartLoading.value = true
  userCartError.value = ''
  try {
    const { ok, data } = await fetchUserCartList()
    if (!ok || data?.code !== 200) {
      userCartList.value = []
      userCartError.value = data?.message || txp('profile_user_cart_load_fail')
      return
    }
    userCartList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    userCartList.value = []
    userCartError.value = txp('profile_net_portal')
  } finally {
    userCartLoading.value = false
  }
}

async function removeUserCartRow(row) {
  const id = row?.id != null ? String(row.id) : ''
  if (!id) return
  deletingCartId.value = id
  userCartError.value = ''
  try {
    const { ok, data } = await deleteUserCartItem(id)
    if (!ok || data?.code !== 200) {
      userCartError.value = data?.message || txp('profile_user_cart_delete_fail')
      return
    }
    await loadUserCartList()
  } catch {
    userCartError.value = txp('profile_net_portal')
  } finally {
    deletingCartId.value = ''
  }
}

function setUserTab(key) {
  userActiveTab.value = key
  if (key === U_ORDER) {
    loadUserOrderList()
    return
  }
  if (key === U_CART) {
    loadUserCartList()
  }
}

async function createOrderAndGoPay(row) {
  const cartId = row?.id
  if (cartId == null) return
  const userPhone = String(localStorage.getItem('phone') || '').trim()
  const city = String(localStorage.getItem('city') || '').trim()
  if (!userPhone || !city) {
    userCartError.value = txp('profile_user_order_need_contact')
    return
  }
  creatingCartOrderId.value = String(cartId)
  userCartError.value = ''
  try {
    const { ok, data } = await createOrderFromCart({ cartId, userPhone, city })
    if (!ok || data?.code !== 200) {
      userCartError.value = data?.message || txp('profile_user_order_create_fail')
      return
    }
    const payload = data?.data || {}
    router.push({
      name: 'order-pay',
      query: {
        orderId: payload.orderId || '',
        expireAt: payload.expireAt || '',
      },
    })
  } catch {
    userCartError.value = txp('profile_net_portal')
  } finally {
    creatingCartOrderId.value = ''
  }
}

function orderStatusText(status) {
  const s = Number(status)
  if (s === 0) return txp('pay_pending')
  if (s === 2) return txp('pay_pending')
  if (s === 3) return txp('pay_pending')
  if (s === 4) return txp('pay_timeout')
  if (s === 5) return txp('pay_paid')
  return txp('status_unknown')
}

async function loadUserOrderList() {
  userOrderLoading.value = true
  userOrderError.value = ''
  userOrderTip.value = ''
  try {
    const { ok, data } = await fetchUserOrderList({
      pageNum: userOrderPageNum.value,
      pageSize: userOrderPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      userOrderList.value = []
      userOrderError.value = data?.message || txp('profile_user_order_load_fail')
      return
    }
    userOrderList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    userOrderList.value = []
    userOrderError.value = txp('profile_net_portal')
  } finally {
    userOrderLoading.value = false
  }
}

function canPayOrder(row) {
  const s = Number(row?.status)
  return s === 0 || s === 2 || s === 3
}

function canCancelOrder(row) {
  const s = Number(row?.status)
  return s === 2 || s === 3
}

function canConfirmReceiveOrder(row) {
  const transportOrderId = String(row?.transportOrderId || '').trim()
  if (!transportOrderId) return false
  const s = Number(row?.status)
  return s === 5
}

async function payOrderRow(row) {
  const orderId = String(row?.orderId || '').trim()
  if (!orderId) return
  const rawCreateTime = row?.createTime
  const createAtMs = rawCreateTime ? new Date(rawCreateTime).getTime() : Number.NaN
  const hasCreateTime = Number.isFinite(createAtMs)
  const expireAt = hasCreateTime ? createAtMs + 30 * 60 * 1000 : ''
  router.push({
    name: 'order-pay',
    query: {
      orderId,
      expireAt,
    },
  })
}

async function cancelOrderRow(row) {
  const orderId = String(row?.orderId || '').trim()
  if (!orderId) return
  cancellingOrderId.value = orderId
  userOrderError.value = ''
  userOrderTip.value = ''
  try {
    const { ok, data } = await cancelUserOrder(orderId)
    if (!ok || data?.code !== 200) {
      userOrderError.value = data?.message || txp('profile_user_order_cancel_fail')
      return
    }
    userOrderTip.value = data?.message || txp('profile_user_order_cancel_ok')
    await loadUserOrderList()
  } catch {
    userOrderError.value = txp('profile_net_portal')
  } finally {
    cancellingOrderId.value = ''
  }
}

async function confirmUserReceiveRow(row) {
  const transportOrderId = String(row?.transportOrderId || '').trim()
  if (!transportOrderId) return
  confirmingReceiveId.value = transportOrderId
  userOrderError.value = ''
  userOrderTip.value = ''
  try {
    const { ok, data } = await confirmUserOrderReceived(transportOrderId)
    if (!ok || data?.code !== 200 || data?.data === false) {
      userOrderError.value = data?.message || txp('profile_user_order_receive_fail')
      return
    }
    userOrderTip.value = data?.message || txp('profile_user_order_receive_ok')
  } catch {
    userOrderError.value = txp('profile_net_portal')
  } finally {
    confirmingReceiveId.value = ''
  }
}

async function confirmMerchantReceiveRow(row) {
  const transportOrderId = String(row?.transportOrderId || '').trim()
  if (!transportOrderId) return
  merchantApplyListError.value = ''
  try {
    const { ok, data } = await confirmMerchantOrderReceived(transportOrderId)
    if (!ok || data?.code !== 200 || data?.data === false) {
      merchantApplyListError.value = data?.message || txp('profile_merchant_order_receive_fail')
      return
    }
  } catch {
    merchantApplyListError.value = txp('profile_net_portal')
  }
}

function canConfirmMerchantReceive(row) {
  return canViewLogistic(row)
}

function canConfirmMerchantOffShelfReceive(row) {
  return canViewOffShelfLogistic(row)
}

async function loadManagerGoodsList() {
  managerGoodsLoading.value = true
  managerGoodsError.value = ''
  try {
    await ensureManagerTypeLookup()
    const category = Number(managerGoodsCategory.value)
    const { ok, data } = await fetchManagerPortalGoods({
      pageNum: managerGoodsPageNum.value,
      pageSize: managerGoodsPageSize.value,
      category: Number.isNaN(category) ? 1 : category,
    })
    if (!ok || data?.code !== 200) {
      managerGoodsList.value = []
      managerGoodsError.value = data?.message || txp('profile_manager_goods_list_fail')
      return
    }
    managerGoodsList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    managerGoodsList.value = []
    managerGoodsError.value = txp('profile_net_admin')
  } finally {
    managerGoodsLoading.value = false
  }
}

async function loadManagerGoodsById() {
  const goodsId = managerQueryGoodsId.value.trim()
  if (!goodsId) {
    managerQueryError.value = txp('profile_manager_goods_query_need_id')
    managerQueryResult.value = null
    return
  }
  managerQueryLoading.value = true
  managerQueryError.value = ''
  try {
    await ensureManagerTypeLookup()
    const { ok, data } = await fetchManagerPortalGoodsById(goodsId)
    if (!ok || data?.code !== 200) {
      managerQueryResult.value = null
      managerQueryError.value = data?.message || txp('profile_manager_goods_query_fail')
      return
    }
    managerQueryResult.value = data?.data || null
    if (!managerQueryResult.value) {
      managerQueryError.value = txp('profile_manager_goods_query_empty')
    }
  } catch {
    managerQueryResult.value = null
    managerQueryError.value = txp('profile_net_admin')
  } finally {
    managerQueryLoading.value = false
  }
}

async function loadManagerTypeList() {
  managerTypeLoading.value = true
  managerTypeError.value = ''
  try {
    const { ok, data } = await fetchManagerGoodsTypes({
      pageNum: managerTypePageNum.value,
      pageSize: managerTypePageSize.value,
    })
    if (!ok || data?.code !== 200) {
      managerTypeList.value = []
      managerTypeError.value = data?.message || txp('profile_manager_type_list_fail')
      return
    }
    managerTypeList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    managerTypeList.value = []
    managerTypeError.value = txp('profile_net_admin')
  } finally {
    managerTypeLoading.value = false
  }
}

function openManagerTypeDialog(row = null) {
  managerCrudDialogVisible.value = true
  managerCrudError.value = ''
  managerCrudKind.value = 'type'
  managerCrudMode.value = row ? 'edit' : 'add'
  managerCrudForm.value = {
    typeId: row?.typeId ?? null,
    typeName: row?.typeName ?? '',
    id: null,
    brandId: '',
    brandName: '',
  }
}

async function removeManagerType(row) {
  if (!window.confirm(txp('profile_manager_type_confirm_delete'))) return
  const { ok, data } = await deleteManagerGoodsType(row?.typeId)
  if (!ok || data?.code !== 200) {
    managerTypeError.value = data?.message || txp('profile_manager_type_delete_fail')
    return
  }
  await loadManagerTypeList()
}

async function loadManagerBrandList() {
  managerBrandLoading.value = true
  managerBrandError.value = ''
  try {
    const { ok, data } = await fetchManagerBrands({
      pageNum: managerBrandPageNum.value,
      pageSize: managerBrandPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      managerBrandList.value = []
      managerBrandError.value = data?.message || txp('profile_manager_brand_list_fail')
      return
    }
    managerBrandList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    managerBrandList.value = []
    managerBrandError.value = txp('profile_net_admin')
  } finally {
    managerBrandLoading.value = false
  }
}

function openManagerBrandDialog(row = null) {
  managerCrudDialogVisible.value = true
  managerCrudError.value = ''
  managerCrudKind.value = 'brand'
  managerCrudMode.value = row ? 'edit' : 'add'
  managerCrudForm.value = {
    typeId: null,
    typeName: '',
    id: row?.id ?? null,
    brandId: row?.brandId ?? '',
    brandName: row?.brandName ?? '',
  }
}

function closeManagerCrudDialog() {
  managerCrudDialogVisible.value = false
  managerCrudSubmitting.value = false
  managerCrudError.value = ''
}

async function submitManagerCrudDialog() {
  managerCrudSubmitting.value = true
  managerCrudError.value = ''
  try {
    if (managerCrudKind.value === 'type') {
      const typeName = String(managerCrudForm.value.typeName || '').trim()
      if (!typeName) {
        managerCrudError.value = txp('profile_manager_type_need_name')
        return
      }
      const apiCall = managerCrudMode.value === 'edit'
        ? updateManagerGoodsType({ typeId: managerCrudForm.value.typeId, typeName })
        : addManagerGoodsType({ typeName })
      const { ok, data } = await apiCall
      if (!ok || data?.code !== 200) {
        managerCrudError.value = data?.message || txp(
          managerCrudMode.value === 'edit'
            ? 'profile_manager_type_update_fail'
            : 'profile_manager_type_add_fail',
        )
        return
      }
      await loadManagerTypeList()
      closeManagerCrudDialog()
      return
    }
    const brandId = String(managerCrudForm.value.brandId || '').trim()
    const brandName = String(managerCrudForm.value.brandName || '').trim()
    if (!brandId) {
      managerCrudError.value = txp('profile_manager_brand_need_id')
      return
    }
    if (!brandName) {
      managerCrudError.value = txp('profile_manager_brand_need_name')
      return
    }
    const apiCall = managerCrudMode.value === 'edit'
      ? updateManagerBrand({ id: managerCrudForm.value.id, brandId, brandName })
      : addManagerBrand({ brandId, brandName })
    const { ok, data } = await apiCall
    if (!ok || data?.code !== 200) {
      managerCrudError.value = data?.message || txp(
        managerCrudMode.value === 'edit'
          ? 'profile_manager_brand_update_fail'
          : 'profile_manager_brand_add_fail',
      )
      return
    }
    await loadManagerBrandList()
    closeManagerCrudDialog()
  } finally {
    managerCrudSubmitting.value = false
  }
}

async function removeManagerBrand(row) {
  if (!window.confirm(txp('profile_manager_brand_confirm_delete'))) return
  const { ok, data } = await deleteManagerBrand(row?.id)
  if (!ok || data?.code !== 200) {
    managerBrandError.value = data?.message || txp('profile_manager_brand_delete_fail')
    return
  }
  await loadManagerBrandList()
}

function isAdminTabActive(key) {
  if (role.value === 'reviewer') {
    return reviewerActiveTab.value === key
  }
  if (role.value === 'super') {
    return superActiveTab.value === key
  }
  if (role.value === 'manager') {
    return managerActiveTab.value === key
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
    return
  }
  if (role.value === 'manager') {
    managerActiveTab.value = key
  }
}

const showAdminHome = computed(() => {
  if (role.value === 'reviewer') return reviewerActiveTab.value === R_HOME
  if (role.value === 'super') return superActiveTab.value === S_HOME
  if (role.value === 'manager') return managerActiveTab.value === S_HOME
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

async function loadSuperDictionaryList() {
  superDictLoading.value = true
  superDictError.value = ''
  try {
    const { ok, data } = await fetchSuperDictionaryList({
      pageNum: superDictPageNum.value,
      pageSize: superDictPageSize.value,
    })
    if (!ok || data?.code !== 200) {
      superDictList.value = []
      superDictError.value = data?.message || txp('profile_super_dict_list_fail')
      return
    }
    superDictList.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    superDictList.value = []
    superDictError.value = txp('profile_net_admin')
  } finally {
    superDictLoading.value = false
  }
}

function openSuperDictDialog(row = null) {
  superDictDialogVisible.value = true
  superDictDialogError.value = ''
  superDictDialogMode.value = row ? 'edit' : 'add'
  superDictForm.value = {
    dictType: row?.dictType || '',
    dictValue: row?.dictValue || '',
    dictName: row?.dictName || '',
    lang: String(row?.lang || '1'),
    sort: Number(row?.sort ?? 0),
    status: Number(row?.status ?? 1),
  }
}

function closeSuperDictDialog() {
  superDictDialogVisible.value = false
  superDictDialogError.value = ''
}

async function submitSuperDictDialog() {
  const payload = {
    dictType: String(superDictForm.value.dictType || '').trim(),
    dictValue: String(superDictForm.value.dictValue || '').trim(),
    dictName: String(superDictForm.value.dictName || '').trim(),
    lang: String(superDictForm.value.lang || '').trim() || '1',
  }
  if (!payload.dictType || !payload.dictValue || !payload.dictName) {
    superDictDialogError.value = txp('profile_super_dict_required')
    return
  }
  superDictSubmitting.value = true
  superDictDialogError.value = ''
  try {
    if (superDictDialogMode.value === 'add') {
      const { ok, data } = await addSuperDictionary({
        ...payload,
        sort: Number(superDictForm.value.sort ?? 0),
        status: Number(superDictForm.value.status ?? 1),
      })
      if (!ok || data?.code !== 200) {
        superDictDialogError.value = data?.message || txp('profile_super_dict_add_fail')
        return
      }
    } else {
      const { ok, data } = await updateSuperDictionary(payload)
      if (!ok || data?.code !== 200) {
        superDictDialogError.value = data?.message || txp('profile_super_dict_update_fail')
        return
      }
    }
    closeSuperDictDialog()
    await loadSuperDictionaryList()
  } finally {
    superDictSubmitting.value = false
  }
}

function openSuperDictDeleteDialog(row) {
  superDictDeleteDialogVisible.value = true
  superDictDeleteError.value = ''
  superDictDeleteTarget.value = row
}

function closeSuperDictDeleteDialog() {
  superDictDeleteDialogVisible.value = false
  superDictDeleteError.value = ''
  superDictDeleteTarget.value = null
}

async function confirmSuperDictDelete() {
  if (!superDictDeleteTarget.value) return
  superDictDeleteSubmitting.value = true
  superDictDeleteError.value = ''
  try {
    const row = superDictDeleteTarget.value
    const { ok, data } = await deleteSuperDictionary({
      dictType: row.dictType,
      dictName: row.dictName,
      dictValue: row.dictValue,
      lang: row.lang,
    })
    if (!ok || data?.code !== 200) {
      superDictDeleteError.value = data?.message || txp('profile_super_dict_delete_fail')
      return
    }
    closeSuperDictDeleteDialog()
    await loadSuperDictionaryList()
  } finally {
    superDictDeleteSubmitting.value = false
  }
}

function loadSuperSeckillPlan() {
  try {
    const raw = window.localStorage.getItem(SUPER_SECKILL_PLAN_STORAGE_KEY)
    if (!raw) return
    const plan = JSON.parse(raw)
    superSeckillForm.value = {
      activityName: String(plan?.activityName || ''),
      startTime: String(plan?.startTime || ''),
      endTime: String(plan?.endTime || ''),
    }
  } catch {
    // ignore parse/storage errors, keep defaults
  }
}

function openSuperSeckillPicker(kind) {
  const target = kind === 'start' ? superSeckillStartInputRef.value : superSeckillEndInputRef.value
  if (!target || typeof target.showPicker !== 'function') return
  try {
    target.showPicker()
  } catch {
    // ignore browsers that block programmatic picker open
  }
}

async function saveSuperSeckillPlan() {
  const payload = {
    activityName: String(superSeckillForm.value.activityName || '').trim(),
    startTime: String(superSeckillForm.value.startTime || '').trim(),
    endTime: String(superSeckillForm.value.endTime || '').trim(),
  }
  if (!payload.activityName || !payload.startTime || !payload.endTime) {
    superSeckillTip.value = txp('profile_super_activity_required')
    return
  }
  if (new Date(payload.startTime).getTime() >= new Date(payload.endTime).getTime()) {
    superSeckillTip.value = txp('profile_super_activity_time_invalid')
    return
  }
  try {
    const { ok, data } = await launchSeckillActivity(payload)
    if (!ok || data?.code !== 200) {
      superSeckillTip.value = data?.message || txp('profile_action_fail')
      return
    }
    window.localStorage.setItem(SUPER_SECKILL_PLAN_STORAGE_KEY, JSON.stringify(payload))
    superSeckillTip.value = txp('profile_super_activity_saved')
  } catch {
    superSeckillTip.value = txp('profile_net_portal')
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
  } else if (superActiveTab.value === S_DICT) {
    await loadSuperDictionaryList()
  } else if (superActiveTab.value === S_ACTIVITY) {
    loadSuperSeckillPlan()
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
    } else if (tab === S_DICT) {
      loadSuperDictionaryList()
    } else if (tab === S_ACTIVITY) {
      loadSuperSeckillPlan()
    }
  },
)

watch(
  () => managerActiveTab.value,
  (tab) => {
    if (role.value === 'manager' && tab === MG_GOODS) {
      loadManagerGoodsList()
    }
    if (role.value === 'manager' && tab === MG_QUERY) {
      managerQueryError.value = ''
      managerQueryResult.value = null
    }
    if (role.value === 'manager' && tab === MG_TYPE) {
      loadManagerTypeList()
    }
    if (role.value === 'manager' && tab === MG_BRAND) {
      loadManagerBrandList()
    }
  },
)

watch(
  () => [role.value, token.value],
  ([r, t]) => {
    if (!t || !['merchant', 'user'].includes(r)) return
    refreshMallSessionFromStorage()
    syncMallProfileForm()
  },
  { immediate: true },
)

watch(
  () => route.fullPath,
  () => {
    if (route.name !== 'profile' || !token.value) return
    if (!['merchant', 'user'].includes(role.value)) return
    refreshMallSessionFromStorage()
    syncMallProfileForm()
  },
)

onMounted(() => {
  if (!token.value || !['merchant', 'user'].includes(role.value)) return
  refreshMallSessionFromStorage()
  syncMallProfileForm()
  if (role.value === 'user' && route.query.tab === 'orders') {
    userActiveTab.value = U_ORDER
    loadUserOrderList()
  }
})

watch(
  () => merchantActiveTab.value,
  (tab) => {
    if (role.value === 'merchant' && tab === M_ACTIVITY_APPLY) {
      loadMerchantSeckillActivities()
      return
    }
    if (role.value === 'merchant' && tab === M_APPLY_LIST) {
      loadMerchantApplyList()
      return
    }
    if (role.value === 'merchant' && tab === M_OFF_SHELF) {
      loadMerchantOffShelfList()
      return
    }
    if (role.value === 'merchant' && tab === M_MY_GOODS) {
      loadMerchantGoodsList()
    }
  },
)

watch(
  () => userActiveTab.value,
  (tab) => {
    if (role.value === 'user' && tab === U_ORDER) {
      loadUserOrderList()
      return
    }
    if (role.value === 'user' && tab === U_CART) {
      loadUserCartList()
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
          <div v-else class="table-wrap dict-table-wrap">
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

        <section v-if="role === 'super' && superActiveTab === S_DICT" class="panel">
          <div class="panel-head">
            <h3>{{ txp('profile_super_tab_dict') }}</h3>
            <button class="btn primary" @click="openSuperDictDialog()">{{ txp('profile_super_add_dict') }}</button>
          </div>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="superDictPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="superDictPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadSuperDictionaryList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="superDictLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="superDictError" class="panel-tip error">{{ superDictError }}</p>
          <p v-else-if="superDictList.length === 0" class="panel-tip">{{ txp('profile_super_dict_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_dict_type') }}</th>
                  <th>{{ txp('profile_dict_value') }}</th>
                  <th>{{ txp('profile_dict_name') }}</th>
                  <th>{{ txp('profile_dict_lang') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in superDictList" :key="`${row.dictType}-${row.dictValue}-${row.lang}`">
                  <td>{{ row.dictType || txp('profile_empty_dash') }}</td>
                  <td>{{ row.dictValue || txp('profile_empty_dash') }}</td>
                  <td>{{ row.dictName || txp('profile_empty_dash') }}</td>
                  <td>{{ row.lang || txp('profile_empty_dash') }}</td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openSuperDictDialog(row)">{{ txp('profile_btn_update') }}</button>
                    <button class="op-btn reject" @click="openSuperDictDeleteDialog(row)">{{ txp('profile_btn_delete') }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'super' && superActiveTab === S_ACTIVITY" class="panel">
          <h3>{{ txp('profile_super_tab_launch_activity') }}</h3>
          <p class="panel-tip">{{ txp('profile_super_launch_activity_hint') }}</p>
          <p v-if="superSeckillTip" class="merchant-edit-msg">{{ superSeckillTip }}</p>
          <p class="panel-tip">{{ txp('profile_super_launch_activity_flow') }}</p>
          <form class="merchant-edit-form" @submit.prevent="saveSuperSeckillPlan">
            <label class="merchant-field">
              <span>{{ txp('profile_super_activity_name') }}</span>
              <input v-model="superSeckillForm.activityName" type="text" class="merchant-input" autocomplete="off" />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_super_activity_start') }}</span>
              <input
                ref="superSeckillStartInputRef"
                v-model="superSeckillForm.startTime"
                type="datetime-local"
                class="merchant-input"
                @click="openSuperSeckillPicker('start')"
                @focus="openSuperSeckillPicker('start')"
              />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_super_activity_end') }}</span>
              <input
                ref="superSeckillEndInputRef"
                v-model="superSeckillForm.endTime"
                type="datetime-local"
                class="merchant-input"
                @click="openSuperSeckillPicker('end')"
                @focus="openSuperSeckillPicker('end')"
              />
            </label>
            <div class="merchant-edit-actions">
              <button type="submit" class="btn primary">{{ txp('profile_super_save_activity_plan') }}</button>
            </div>
          </form>
        </section>

        <section v-if="role === 'manager' && managerActiveTab === MG_GOODS" class="panel">
          <h3>{{ txp('profile_manager_tab_goods_list') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_col_category') }}
              <select v-model="managerGoodsCategory" class="mini-input">
                <option value="1">{{ txp('category_special') }}</option>
                <option value="0">{{ txp('category_normal') }}</option>
              </select>
            </label>
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="managerGoodsPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="managerGoodsPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadManagerGoodsList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="managerGoodsLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="managerGoodsError" class="panel-tip error">{{ managerGoodsError }}</p>
          <p v-else-if="managerGoodsList.length === 0" class="panel-tip">{{ txp('profile_manager_goods_list_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_merchant_id') }}</th>
                  <th>{{ txp('profile_goods_id_col') }}</th>
                  <th>{{ txp('profile_goods_name') }}</th>
                  <th>{{ txp('profile_col_category') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_col_goods_type') }}</th>
                  <th>{{ txp('profile_col_description') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in managerGoodsList" :key="row.goodsId">
                  <td>{{ row.merchantId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.goodsId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.skuName || txp('profile_empty_dash') }}</td>
                  <td>{{ categoryText(row.category) }}</td>
                  <td>{{ row.price ?? txp('profile_empty_dash') }}</td>
                  <td>{{ goodsTypeText(row.type) }}</td>
                  <td>{{ row.description || txp('profile_empty_dash') }}</td>
                  <td class="op-cell">
                    <button class="op-btn info" @click="goGoodsDetail(row)">{{ txp('profile_go_detail') }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'manager' && managerActiveTab === MG_QUERY" class="panel">
          <h3>{{ txp('profile_manager_tab_goods_query') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_goods_id_col') }}
              <input v-model="managerQueryGoodsId" type="text" class="mini-input goods-id-input" />
            </label>
            <button class="btn ghost" @click="loadManagerGoodsById">{{ txp('profile_query') }}</button>
          </div>
          <p v-if="managerQueryLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="managerQueryError" class="panel-tip error">{{ managerQueryError }}</p>
          <div v-else-if="managerQueryResult" class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_merchant_id') }}</th>
                  <th>{{ txp('profile_goods_id_col') }}</th>
                  <th>{{ txp('profile_goods_name') }}</th>
                  <th>{{ txp('profile_col_category') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_col_goods_type') }}</th>
                  <th>{{ txp('profile_col_description') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ managerQueryResult.merchantId || txp('profile_empty_dash') }}</td>
                  <td>{{ managerQueryResult.goodsId || txp('profile_empty_dash') }}</td>
                  <td>{{ managerQueryResult.skuName || txp('profile_empty_dash') }}</td>
                  <td>{{ categoryText(managerQueryResult.category) }}</td>
                  <td>{{ managerQueryResult.price ?? txp('profile_empty_dash') }}</td>
                  <td>{{ goodsTypeText(managerQueryResult.type) }}</td>
                  <td>{{ managerQueryResult.description || txp('profile_empty_dash') }}</td>
                  <td class="op-cell">
                    <button class="op-btn info" @click="goGoodsDetail(managerQueryResult)">{{ txp('profile_go_detail') }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'manager' && managerActiveTab === MG_TYPE" class="panel">
          <div class="panel-head">
            <h3>{{ txp('profile_manager_tab_type_list') }}</h3>
            <button class="btn primary" @click="openManagerTypeDialog()">{{ txp('profile_manager_add_type') }}</button>
          </div>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="managerTypePageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="managerTypePageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadManagerTypeList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="managerTypeLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="managerTypeError" class="panel-tip error">{{ managerTypeError }}</p>
          <p v-else-if="managerTypeList.length === 0" class="panel-tip">{{ txp('profile_manager_type_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_type_id_col') }}</th>
                  <th>{{ txp('profile_type_name_col') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in managerTypeList" :key="row.typeId">
                  <td>{{ row.typeId ?? txp('profile_empty_dash') }}</td>
                  <td>{{ row.typeName || txp('profile_empty_dash') }}</td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openManagerTypeDialog(row)">{{ txp('profile_btn_update') }}</button>
                    <button class="op-btn reject" @click="removeManagerType(row)">{{ txp('profile_btn_delete') }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'manager' && managerActiveTab === MG_BRAND" class="panel">
          <div class="panel-head">
            <h3>{{ txp('profile_manager_tab_brand_list') }}</h3>
            <button class="btn primary" @click="openManagerBrandDialog()">{{ txp('profile_manager_add_brand') }}</button>
          </div>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="managerBrandPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="managerBrandPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadManagerBrandList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="managerBrandLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="managerBrandError" class="panel-tip error">{{ managerBrandError }}</p>
          <p v-else-if="managerBrandList.length === 0" class="panel-tip">{{ txp('profile_manager_brand_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_brand_id_col') }}</th>
                  <th>{{ txp('profile_brand_name_col') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in managerBrandList" :key="row.id">
                  <td>{{ row.brandId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.brandName || txp('profile_empty_dash') }}</td>
                  <td class="op-cell">
                    <button class="op-btn pass" @click="openManagerBrandDialog(row)">{{ txp('profile_btn_update') }}</button>
                    <button class="op-btn reject" @click="removeManagerBrand(row)">{{ txp('profile_btn_delete') }}</button>
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
        <div v-if="role === 'user'" class="merchant-side-tabs">
          <button
            v-for="item in userTabs"
            :key="item.key"
            type="button"
            class="btn ghost side-btn merchant-side-tab"
            :class="{ active: userActiveTab === item.key }"
            @click="setUserTab(item.key)"
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
          <form class="merchant-edit-form" @submit.prevent="submitMallProfile">
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

        <section v-if="role === 'user' && userActiveTab === U_HOME" class="amazon-panel merchant-edit">
          <h3>{{ txp('profile_edit_title') }}</h3>
          <p class="merchant-edit-hint">{{ txp('profile_edit_hint') }}</p>
          <p v-if="merchantProfileTip" class="merchant-edit-msg">{{ merchantProfileTip }}</p>
          <p v-if="merchantProfileError" class="merchant-edit-msg error">{{ merchantProfileError }}</p>
          <form class="merchant-edit-form" @submit.prevent="submitMallProfile">
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

        <section v-if="role === 'merchant' && merchantActiveTab === M_APPLY_SHELF" class="amazon-panel merchant-apply-entry">
          <h3>{{ txp('profile_apply_shelf_title') }}</h3>
          <p class="merchant-edit-hint">{{ txp('profile_apply_shelf_hint') }}</p>
          <button type="button" class="btn primary merchant-apply-btn" @click="router.push({ name: 'merchant-apply-goods' })">
            {{ txp('profile_apply_shelf_btn') }}
          </button>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === M_ACTIVITY_APPLY" class="amazon-panel merchant-apply-entry">
          <h3>{{ txp('profile_merchant_activity_apply') }}</h3>
          <p class="merchant-edit-hint">{{ txp('profile_merchant_activity_hint') }}</p>
          <p v-if="merchantSeckillTip" class="merchant-edit-msg">{{ merchantSeckillTip }}</p>
          <p v-if="merchantSeckillError" class="merchant-edit-msg error">{{ merchantSeckillError }}</p>
          <form class="merchant-edit-form" @submit.prevent="submitMerchantSeckillApply">
            <label class="merchant-field">
              <span>{{ txp('profile_field_goods_id') }}</span>
              <input v-model="merchantSeckillForm.goodsId" type="text" class="merchant-input" autocomplete="off" />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_price') }}</span>
              <input v-model="merchantSeckillForm.seckillPrice" type="number" min="0.01" step="0.01" class="merchant-input" />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_merchant_activity_stock') }}</span>
              <input v-model.number="merchantSeckillForm.totalStock" type="number" min="1" class="merchant-input" />
            </label>
            <label class="merchant-field">
              <span>{{ txp('profile_merchant_activity_limit') }}</span>
              <input v-model.number="merchantSeckillForm.perUserLimit" type="number" min="1" class="merchant-input" />
            </label>
            <label class="merchant-field" style="grid-column: 1 / -1;">
              <span>{{ txp('profile_col_description') }}</span>
              <input v-model="merchantSeckillForm.remark" type="text" class="merchant-input" autocomplete="off" />
            </label>
            <div class="merchant-edit-actions" style="grid-column: 1 / -1;">
              <button type="submit" class="btn primary" :disabled="merchantSeckillSubmitting">
                {{ merchantSeckillSubmitting ? txp('profile_submitting') : txp('profile_merchant_activity_apply_submit') }}
              </button>
            </div>
          </form>

          <div class="register-filter merchant-activity-toolbar">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="merchantSeckillPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="merchantSeckillPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadMerchantSeckillActivities">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="merchantSeckillListLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="merchantSeckillListError" class="panel-tip error">{{ merchantSeckillListError }}</p>
          <p v-else-if="merchantSeckillList.length === 0" class="panel-tip">{{ txp('profile_merchant_activity_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>活动编码</th>
                  <th>{{ txp('profile_field_goods_id') }}</th>
                  <th>{{ txp('profile_super_activity_name') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_status') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in merchantSeckillList" :key="row.activityCode || row.id">
                  <td>{{ row.activityCode || txp('profile_empty_dash') }}</td>
                  <td>{{ row.goodsId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.activityName || txp('profile_empty_dash') }}</td>
                  <td>{{ row.seckillPrice ?? txp('profile_empty_dash') }}</td>
                  <td>{{ seckillStatusText(row.status) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'merchant' && merchantActiveTab === M_OFF_SHELF" class="amazon-panel merchant-off-shelf-entry">
          <h3>{{ txp('profile_off_shelf_title') }}</h3>
          <p class="merchant-edit-hint">{{ txp('profile_off_shelf_hint') }}</p>
          <p v-if="offShelfTip" class="merchant-edit-msg">{{ offShelfTip }}</p>
          <p v-if="offShelfError" class="merchant-edit-msg error">{{ offShelfError }}</p>
          <form class="merchant-edit-form merchant-off-shelf-form" @submit.prevent="onOffShelfFormSubmit">
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
              <button type="submit" class="btn primary" :disabled="offShelfSubmitting || offShelfConfirmLoading">
                {{
                  offShelfConfirmLoading
                    ? txp('profile_loading')
                    : offShelfSubmitting
                      ? txp('profile_submitting')
                      : txp('profile_off_shelf_submit')
                }}
              </button>
            </div>
          </form>
          <div class="register-filter off-shelf-list-toolbar">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="offShelfPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="offShelfPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadMerchantOffShelfList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="offShelfListLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="offShelfListError" class="panel-tip error">{{ offShelfListError }}</p>
          <p v-else-if="offShelfList.length === 0" class="panel-tip">{{ txp('profile_off_shelf_list_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_off_shelf_id_col') }}</th>
                  <th>{{ txp('profile_goods_id_col') }}</th>
                  <th>{{ txp('profile_transport_order_id_col') }}</th>
                  <th>{{ txp('profile_field_city') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_status') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in offShelfList" :key="row.id">
                  <td>{{ row.id ?? txp('profile_empty_dash') }}</td>
                  <td>{{ row.goodsId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.transportOrderId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.city || txp('profile_empty_dash') }}</td>
                  <td>{{ row.fee ?? txp('profile_empty_dash') }}</td>
                  <td>{{ offShelfStatusText(row.status) }}</td>
                  <td class="op-cell">
                    <button
                      v-if="canPayOffShelf(row)"
                      class="op-btn pass"
                      @click="payOffShelfRow(row)"
                    >
                      {{ txp('profile_go_pay') }}
                    </button>
                    <button v-else-if="canViewOffShelfLogistic(row)" class="op-btn info" @click="viewLogistic(row)">
                      {{ txp('profile_view_logistic') }}
                    </button>
                    <button v-if="canConfirmMerchantOffShelfReceive(row)" class="op-btn pass" @click="confirmMerchantReceiveRow(row)">
                      {{ txp('profile_confirm_receive') }}
                    </button>
                    <span v-if="!canPayOffShelf(row) && !canViewOffShelfLogistic(row) && !canConfirmMerchantOffShelfReceive(row)" class="reviewed-tag">
                      {{ offShelfActionPlaceholder(row) }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
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
                    <button v-if="canConfirmMerchantReceive(row)" class="op-btn pass" @click="confirmMerchantReceiveRow(row)">{{ txp('profile_confirm_receive') }}</button>
                    <span v-if="!canPayApply(row) && !canViewLogistic(row) && !canConfirmMerchantReceive(row)" class="reviewed-tag">
                      {{ txp('profile_empty_dash') }}
                    </span>
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

        <section v-if="role === 'user' && userActiveTab === U_CART" class="amazon-panel">
          <div class="panel-head">
            <h3>{{ txp('profile_user_tab_my_cart') }}</h3>
            <div class="head-actions">
              <button class="btn ghost" @click="router.push({ name: 'cart' })">{{ txp('profile_user_go_cart_page') }}</button>
              <button class="btn primary" @click="setUserTab(U_ORDER)">{{ txp('profile_go_pay') }}</button>
            </div>
          </div>
          <p v-if="userCartLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="userCartError" class="panel-tip error">{{ userCartError }}</p>
          <p v-else-if="userCartList.length === 0" class="panel-tip">{{ txp('profile_user_cart_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_goods_id_col') }}</th>
                  <th>{{ txp('profile_goods_name') }}</th>
                  <th>{{ txp('profile_sku_code') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_col_quantity') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in userCartList" :key="row.id || `${row.goodsId}-${row.skuCode || ''}`">
                  <td>{{ row.goodsId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.skuName || txp('profile_empty_dash') }}</td>
                  <td>{{ row.skuCode || txp('profile_empty_dash') }}</td>
                  <td>{{ row.price ?? txp('profile_empty_dash') }}</td>
                  <td>{{ row.quantity ?? txp('profile_empty_dash') }}</td>
                  <td class="op-cell">
                    <button
                      class="op-btn pass"
                      :disabled="creatingCartOrderId === String(row.id)"
                      @click="createOrderAndGoPay(row)"
                    >
                      {{ creatingCartOrderId === String(row.id) ? txp('profile_submitting') : txp('profile_go_pay') }}
                    </button>
                    <button class="op-btn info" @click="goGoodsDetail(row)">{{ txp('profile_go_detail') }}</button>
                    <button
                      class="op-btn reject"
                      :disabled="deletingCartId === String(row.id)"
                      @click="removeUserCartRow(row)"
                    >
                      {{ txp('profile_btn_delete') }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="role === 'user' && userActiveTab === U_ORDER" class="amazon-panel">
          <h3>{{ txp('profile_user_tab_my_orders') }}</h3>
          <div class="register-filter">
            <label>
              {{ txp('profile_page') }}
              <input v-model.number="userOrderPageNum" type="number" min="1" class="mini-input" />
            </label>
            <label>
              {{ txp('profile_page_size') }}
              <input v-model.number="userOrderPageSize" type="number" min="1" class="mini-input" />
            </label>
            <button class="btn ghost" @click="loadUserOrderList">{{ txp('profile_refresh') }}</button>
          </div>
          <p v-if="userOrderTip" class="panel-tip">{{ userOrderTip }}</p>
          <p v-if="userOrderLoading" class="panel-tip">{{ txp('profile_loading') }}</p>
          <p v-else-if="userOrderError" class="panel-tip error">{{ userOrderError }}</p>
          <p v-else-if="userOrderList.length === 0" class="panel-tip">{{ txp('profile_user_order_empty') }}</p>
          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>{{ txp('profile_order_id_col') }}</th>
                  <th>{{ txp('profile_transport_order_id_col') }}</th>
                  <th>{{ txp('profile_goods_name') }}</th>
                  <th>{{ txp('profile_price') }}</th>
                  <th>{{ txp('profile_col_quantity') }}</th>
                  <th>{{ txp('profile_col_pay_status') }}</th>
                  <th>{{ txp('profile_col_actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in userOrderList" :key="row.orderId || row.id">
                  <td>{{ row.orderId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.transportOrderId || txp('profile_empty_dash') }}</td>
                  <td>{{ row.skuName || txp('profile_empty_dash') }}</td>
                  <td>{{ row.price ?? txp('profile_empty_dash') }}</td>
                  <td>{{ row.quantity ?? txp('profile_empty_dash') }}</td>
                  <td>{{ orderStatusText(row.status) }}</td>
                  <td class="op-cell">
                    <button
                      v-if="canPayOrder(row)"
                      class="op-btn pass"
                      @click="payOrderRow(row)"
                    >
                      {{ txp('profile_go_pay') }}
                    </button>
                    <button
                      v-if="canConfirmReceiveOrder(row)"
                      class="op-btn pass"
                      :disabled="confirmingReceiveId === String(row.transportOrderId || '').trim()"
                      @click="confirmUserReceiveRow(row)"
                    >
                      {{ txp('profile_confirm_receive') }}
                    </button>
                    <button
                      v-if="canCancelOrder(row)"
                      class="op-btn reject"
                      :disabled="cancellingOrderId === row.orderId"
                      @click="cancelOrderRow(row)"
                    >
                      {{ txp('profile_cancel') }}
                    </button>
                    <button
                      v-if="canViewUserOrderLogistic(row)"
                      class="op-btn info"
                      @click="viewLogistic(row)"
                    >
                      {{ txp('profile_view_logistic') }}
                    </button>
                    <button class="op-btn info" @click="goGoodsDetail(row)">{{ txp('profile_go_detail') }}</button>
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

    <div v-if="offShelfConfirmVisible" class="dialog-mask" @click.self="closeOffShelfConfirm">
      <div class="dialog-card off-shelf-confirm-card">
        <h3 class="dialog-title">{{ txp('profile_off_shelf_confirm_title') }}</h3>
        <p class="dialog-text">{{ txp('profile_off_shelf_confirm_hint') }}</p>
        <p v-if="offShelfConfirmError" class="panel-tip error">{{ offShelfConfirmError }}</p>
        <div v-if="offShelfConfirmSnapshot.row" class="off-shelf-confirm-layout">
          <div v-if="offShelfConfirmImageSrc" class="off-shelf-confirm-thumb">
            <img :src="offShelfConfirmImageSrc" :alt="String(offShelfConfirmSnapshot.row.skuName || '')" />
          </div>
          <div class="off-shelf-confirm-fields">
            <p>
              <span class="off-shelf-confirm-label">{{ txp('profile_field_goods_id') }}</span>
              {{ offShelfConfirmSnapshot.row.goodsId || txp('profile_empty_dash') }}
            </p>
            <p>
              <span class="off-shelf-confirm-label">{{ txp('profile_goods_name') }}</span>
              {{ offShelfConfirmSnapshot.row.skuName || txp('profile_empty_dash') }}
            </p>
            <p>
              <span class="off-shelf-confirm-label">{{ txp('profile_sku_code') }}</span>
              {{ offShelfConfirmSnapshot.row.skuCode || txp('profile_empty_dash') }}
            </p>
            <p>
              <span class="off-shelf-confirm-label">{{ txp('profile_price') }}</span>
              {{ offShelfConfirmSnapshot.row.price ?? txp('profile_empty_dash') }}
            </p>
            <p>
              <span class="off-shelf-confirm-label">{{ txp('profile_col_category') }}</span>
              {{ categoryText(offShelfConfirmSnapshot.row.category) }}
            </p>
            <p>
              <span class="off-shelf-confirm-label">{{ txp('profile_status') }}</span>
              {{ portalGoodsStatusText(offShelfConfirmSnapshot.row.status) }}
            </p>
          </div>
        </div>
        <div v-else class="off-shelf-confirm-minimal">
          <p class="dialog-text">{{ txp('profile_off_shelf_confirm_no_detail') }}</p>
          <p class="off-shelf-confirm-line">
            <span class="off-shelf-confirm-label">{{ txp('profile_field_goods_id') }}</span>
            {{ offShelfConfirmSnapshot.goodsId }}
          </p>
        </div>
        <div class="dialog-actions">
          <button type="button" class="btn ghost" :disabled="offShelfSubmitting" @click="closeOffShelfConfirm">
            {{ txp('profile_off_shelf_confirm_cancel') }}
          </button>
          <button type="button" class="btn primary" :disabled="offShelfSubmitting" @click="submitOffShelfApply">
            {{ offShelfSubmitting ? txp('profile_submitting') : txp('profile_off_shelf_confirm_ok') }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="managerCrudDialogVisible" class="dialog-mask" @click.self="closeManagerCrudDialog">
      <div class="dialog-card">
        <h3 class="dialog-title">
          {{
            managerCrudKind === 'type'
              ? (managerCrudMode === 'edit' ? txp('profile_manager_type_edit_title') : txp('profile_manager_type_add_title'))
              : (managerCrudMode === 'edit' ? txp('profile_manager_brand_edit_title') : txp('profile_manager_brand_add_title'))
          }}
        </h3>
        <p v-if="managerCrudError" class="panel-tip error">{{ managerCrudError }}</p>
        <div v-if="managerCrudKind === 'type'" class="dialog-grid">
          <label>{{ txp('profile_type_name_col') }}<input v-model="managerCrudForm.typeName" class="dialog-input" /></label>
        </div>
        <div v-else class="dialog-grid">
          <label>{{ txp('profile_brand_id_col') }}
            <input v-model="managerCrudForm.brandId" class="dialog-input" :disabled="managerCrudMode === 'edit'" />
          </label>
          <label>{{ txp('profile_brand_name_col') }}<input v-model="managerCrudForm.brandName" class="dialog-input" /></label>
        </div>
        <div class="dialog-actions">
          <button class="btn ghost" @click="closeManagerCrudDialog">{{ txp('profile_cancel') }}</button>
          <button class="btn primary" :disabled="managerCrudSubmitting" @click="submitManagerCrudDialog">
            {{ managerCrudSubmitting ? txp('profile_submitting') : txp('profile_save') }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="superDictDialogVisible" class="dialog-mask" @click.self="closeSuperDictDialog">
      <div class="dialog-card">
        <h3 class="dialog-title">
          {{ superDictDialogMode === 'edit' ? txp('profile_super_dict_edit_title') : txp('profile_super_dict_add_title') }}
        </h3>
        <p v-if="superDictDialogError" class="panel-tip error">{{ superDictDialogError }}</p>
        <div class="dialog-grid">
          <label>{{ txp('profile_dict_type') }}<input v-model="superDictForm.dictType" class="dialog-input" :disabled="superDictDialogMode === 'edit'" /></label>
          <label>{{ txp('profile_dict_value') }}<input v-model="superDictForm.dictValue" class="dialog-input" :disabled="superDictDialogMode === 'edit'" /></label>
          <label>{{ txp('profile_dict_name') }}<input v-model="superDictForm.dictName" class="dialog-input" /></label>
          <label>{{ txp('profile_dict_lang') }}
            <select v-model="superDictForm.lang" class="dialog-input">
              <option value="1">1</option>
              <option value="2">2</option>
              <option value="3">3</option>
            </select>
          </label>
          <label v-if="superDictDialogMode === 'add'">{{ txp('profile_dict_sort') }}<input v-model.number="superDictForm.sort" type="number" class="dialog-input" /></label>
          <label v-if="superDictDialogMode === 'add'">{{ txp('profile_status') }}
            <select v-model.number="superDictForm.status" class="dialog-input">
              <option :value="1">{{ txp('user_enabled') }}</option>
              <option :value="0">{{ txp('user_disabled') }}</option>
            </select>
          </label>
        </div>
        <div class="dialog-actions">
          <button class="btn ghost" @click="closeSuperDictDialog">{{ txp('profile_cancel') }}</button>
          <button class="btn primary" :disabled="superDictSubmitting" @click="submitSuperDictDialog">
            {{ superDictSubmitting ? txp('profile_submitting') : txp('profile_save') }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="superDictDeleteDialogVisible" class="dialog-mask" @click.self="closeSuperDictDeleteDialog">
      <div class="dialog-card delete-card">
        <h3 class="dialog-title">{{ txp('profile_super_dict_delete_title') }}</h3>
        <p class="dialog-text">
          {{ txp('profile_super_dict_delete_confirm') }}
          <strong>{{ superDictDeleteTarget?.dictType }}/{{ superDictDeleteTarget?.dictValue }}/{{ superDictDeleteTarget?.lang }}</strong>
        </p>
        <p v-if="superDictDeleteError" class="panel-tip error">{{ superDictDeleteError }}</p>
        <div class="dialog-actions">
          <button class="btn ghost" @click="closeSuperDictDeleteDialog">{{ txp('profile_cancel') }}</button>
          <button class="btn danger" :disabled="superDictDeleteSubmitting" @click="confirmSuperDictDelete">
            {{ txp('profile_confirm_delete') }}
          </button>
        </div>
      </div>
    </div>

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

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.goods-id-input {
  width: 220px;
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
  max-height: 420px;
  overflow-y: auto;
  overflow-x: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 170, 26, 0.85) rgba(255, 255, 255, 0.08);
}

/* 字典列表使用独立滚动区域，避免依赖页面主滚动条 */
.dict-table-wrap {
  max-height: 520px;
}

.table-wrap::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.table-wrap::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 999px;
}

.table-wrap::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(255, 196, 85, 0.95), rgba(255, 140, 0, 0.9));
  border-radius: 999px;
  border: 2px solid rgba(22, 22, 30, 0.9);
}

.table-wrap::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(255, 211, 122, 0.98), rgba(255, 153, 51, 0.95));
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

.merchant-off-shelf-form .merchant-edit-actions {
  margin-bottom: 4px;
}

.merchant-off-shelf-entry .off-shelf-list-toolbar {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #2a2a34;
}

.merchant-activity-toolbar {
  margin-top: 20px;
  padding-top: 14px;
  border-top: 1px solid #2a2a34;
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

.off-shelf-confirm-card {
  width: min(640px, 100%);
}

.off-shelf-confirm-layout {
  display: flex;
  gap: 16px;
  margin-top: 14px;
  align-items: flex-start;
}

.off-shelf-confirm-thumb img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #3a3a44;
}

.off-shelf-confirm-fields {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 14px;
  font-size: 13px;
  color: var(--mall-text);
}

.off-shelf-confirm-fields p,
.off-shelf-confirm-line {
  margin: 0;
}

.off-shelf-confirm-label {
  display: inline-block;
  margin-right: 6px;
  color: var(--mall-text-muted);
  font-size: 12px;
}

.off-shelf-confirm-minimal {
  margin-top: 12px;
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
