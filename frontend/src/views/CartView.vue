<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createOrderFromCart, deleteUserCartItem, fetchUserCartList } from '@/api/portal'
import { useUiLang } from '@/composables/useUiLang.js'
import { useMultiDictionary } from '@/composables/useMultiDictionary.js'
import { pageDictFallback } from '@/utils/pageDictionaryFallback.js'

const route = useRoute()
const router = useRouter()
const { uiLang } = useUiLang()
const { t } = useMultiDictionary(['page_mall'], uiLang)

function tx(key) {
  return t('page_mall', key, pageDictFallback('page_mall', key, uiLang.value))
}

const loading = ref(true)
const error = ref('')
const items = ref([])
const deletingId = ref('')
const creatingOrderId = ref('')

function rowImageSrc(picture) {
  const pic = String(picture || '').trim()
  if (!pic) return ''
  if (/^https?:\/\//i.test(pic) || /^data:/i.test(pic)) return pic
  return `/src/assets/picture/${encodeURIComponent(pic)}`
}

function rowPriceNum(p) {
  const n = Number(p)
  return Number.isFinite(n) ? n : 0
}

function rowQty(q) {
  const n = Number(q)
  return Number.isFinite(n) && n > 0 ? n : 0
}

function lineSubtotal(row) {
  return rowPriceNum(row.price) * rowQty(row.quantity)
}

const totalAmount = computed(() =>
  items.value.reduce((sum, row) => sum + lineSubtotal(row), 0),
)

async function load() {
  const userId = (localStorage.getItem('userId') || '').trim()
  const role = (localStorage.getItem('role') || '').trim()
  const token = (localStorage.getItem('satoken') || '').trim()

  loading.value = true
  error.value = ''
  items.value = []

  if (!token || !userId) {
    loading.value = false
    error.value = 'need_login'
    return
  }
  if (role && role !== 'user') {
    loading.value = false
    error.value = 'wrong_role'
    return
  }

  try {
    const { ok, data } = await fetchUserCartList()
    if (!ok || data?.code !== 200) {
      error.value = data?.message || 'load_fail'
      return
    }
    items.value = Array.isArray(data?.data) ? data.data : []
  } catch {
    error.value = 'network'
  } finally {
    loading.value = false
  }
}

async function removeItem(row) {
  const id = row?.id != null ? String(row.id) : ''
  if (!id) return
  deletingId.value = id
  try {
    const { ok, data } = await deleteUserCartItem(id)
    if (!ok || data?.code !== 200) {
      error.value = data?.message || 'delete_fail'
      return
    }
    await load()
  } catch {
    error.value = 'network'
  } finally {
    deletingId.value = ''
  }
}

async function goPay(row) {
  const id = row?.id
  if (id == null) return
  const userPhone = String(localStorage.getItem('phone') || '').trim()
  const city = String(localStorage.getItem('city') || '').trim()
  if (!userPhone || !city) {
    error.value = tx('cart_need_contact')
    return
  }
  creatingOrderId.value = String(id)
  error.value = ''
  try {
    const { ok, data } = await createOrderFromCart({ cartId: id, userPhone, city })
    if (!ok || data?.code !== 200) {
      error.value = data?.message || tx('cart_create_order_fail')
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
    error.value = tx('cart_network_retry')
  } finally {
    creatingOrderId.value = ''
  }
}

watch(
  () => route.fullPath,
  () => {
    load()
  },
  { immediate: true },
)
</script>

<template>
  <main class="cart-page">
    <div class="container">
      <nav class="breadcrumb">
        <button type="button" class="link" @click="router.push({ name: 'home' })">{{ tx('cart_back_home') }}</button>
        <span class="sep">›</span>
        <span class="current">{{ tx('cart_title') }}</span>
      </nav>

      <h1 class="page-title">{{ tx('cart_title') }}</h1>

      <p v-if="loading" class="status">{{ tx('cart_loading') }}</p>

      <template v-else-if="error === 'need_login'">
        <p class="status">{{ tx('cart_login_hint') }}</p>
        <button type="button" class="btn primary" @click="router.push({ name: 'login' })">
          {{ tx('cart_go_login') }}
        </button>
      </template>

      <p v-else-if="error === 'wrong_role'" class="status error">{{ tx('cart_wrong_role') }}</p>

      <p v-else-if="error" class="status error">
        {{
          error === 'network'
            ? tx('mall_hot_request_fail')
            : error === 'load_fail'
              ? tx('cart_load_fail')
              : error
        }}
      </p>

      <template v-else-if="items.length === 0">
        <p class="status">{{ tx('cart_empty') }}</p>
        <button type="button" class="btn ghost" @click="router.push({ name: 'home' })">
          {{ tx('cart_back_home') }}
        </button>
      </template>

      <div v-else class="cart-card">
        <div class="table-head" role="row">
          <span class="col-goods">{{ tx('cart_col_goods') }}</span>
          <span class="col-price">{{ tx('cart_col_price') }}</span>
          <span class="col-qty">{{ tx('cart_col_qty') }}</span>
          <span class="col-sub">{{ tx('cart_col_subtotal') }}</span>
        </div>
        <ul class="rows">
          <li v-for="(row, i) in items" :key="row.id != null ? String(row.id) : 'r-' + i" class="row">
            <div class="col-goods cell">
              <button type="button" class="thumb-wrap" @click="router.push({ name: 'goods-detail', params: { goodsId: String(row.goodsId || '') } })">
                <img v-if="rowImageSrc(row.picture)" :src="rowImageSrc(row.picture)" :alt="row.skuName" class="thumb" />
                <span v-else class="thumb-ph" />
              </button>
              <div class="goods-text">
                <button type="button" class="name-link" @click="router.push({ name: 'goods-detail', params: { goodsId: String(row.goodsId || '') } })">
                  {{ row.skuName || row.goodsId }}
                </button>
                <p v-if="row.goodsId" class="meta">{{ tx('cart_goods_id_prefix') }}{{ row.goodsId }}</p>
                <button
                  type="button"
                  class="pay-btn"
                  :disabled="creatingOrderId === String(row.id)"
                  @click="goPay(row)"
                >
                  {{ creatingOrderId === String(row.id) ? tx('cart_creating_order') : tx('profile_go_pay') }}
                </button>
                <button
                  type="button"
                  class="remove-btn"
                  :disabled="deletingId === String(row.id)"
                  @click="removeItem(row)"
                >
                  {{ deletingId === String(row.id) ? tx('cart_deleting') : tx('cart_delete') }}
                </button>
              </div>
            </div>
            <div class="col-price cell">¥{{ rowPriceNum(row.price).toFixed(2) }}</div>
            <div class="col-qty cell">{{ rowQty(row.quantity) }}</div>
            <div class="col-sub cell">¥{{ lineSubtotal(row).toFixed(2) }}</div>
          </li>
        </ul>
        <div class="footer-bar">
          <span class="total-label">{{ tx('cart_total_label') }}</span>
          <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.cart-page {
  flex: 1;
  padding: 20px 0 48px;
  background: var(--mall-black-soft, #09090b);
}

.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 16px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: var(--mall-text-muted);
  margin-bottom: 16px;
}

.breadcrumb .link {
  border: none;
  background: none;
  padding: 0;
  color: var(--mall-orange);
  font: inherit;
  cursor: pointer;
}

.breadcrumb .link:hover {
  text-decoration: underline;
}

.breadcrumb .sep {
  color: #52525b;
}

.breadcrumb .current {
  color: var(--mall-text-muted);
}

.page-title {
  margin: 0 0 20px;
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--mall-text, #fafafa);
}

.status {
  margin: 0 0 12px;
  font-size: 0.95rem;
  color: var(--mall-text-muted);
}

.status.error {
  color: #f87171;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  border: none;
}

.btn.primary {
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
  color: var(--mall-black);
}

.btn.ghost {
  margin-top: 8px;
  background: transparent;
  color: var(--mall-orange);
  border: 1px solid var(--mall-border);
}

.cart-card {
  background: var(--mall-surface-elevated, #16161b);
  border: 1px solid var(--mall-border, #2c2c34);
  border-radius: 12px;
  overflow: hidden;
}

.table-head {
  display: grid;
  grid-template-columns: 1fr 100px 72px 100px;
  gap: 12px;
  padding: 12px 16px;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--mall-text-muted);
  border-bottom: 1px solid var(--mall-border, #2c2c34);
}

.rows {
  list-style: none;
  margin: 0;
  padding: 0;
}

.row {
  display: grid;
  grid-template-columns: 1fr 100px 72px 100px;
  gap: 12px;
  padding: 14px 16px;
  align-items: center;
  border-bottom: 1px solid var(--mall-border, #2c2c34);
}

.row:last-child {
  border-bottom: none;
}

.cell {
  font-size: 0.9rem;
  color: var(--mall-text, #e4e4e7);
}

.col-goods.cell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.thumb-wrap {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  padding: 0;
  border: 1px solid var(--mall-border);
  border-radius: 8px;
  overflow: hidden;
  background: #111117;
  cursor: pointer;
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-ph {
  display: block;
  width: 100%;
  height: 100%;
  background: linear-gradient(160deg, #3d3530 0%, #1a1512 100%);
}

.goods-text {
  min-width: 0;
}

.name-link {
  display: block;
  width: 100%;
  text-align: left;
  border: none;
  background: none;
  padding: 0;
  font: inherit;
  font-weight: 600;
  color: var(--mall-text);
  cursor: pointer;
  line-height: 1.35;
}

.name-link:hover {
  color: var(--mall-orange-bright);
}

.meta {
  margin: 4px 0 0;
  font-size: 0.78rem;
  color: var(--mall-text-muted);
}

.remove-btn {
  margin-top: 8px;
  border: 1px solid rgba(248, 113, 113, 0.6);
  background: transparent;
  color: #fca5a5;
  border-radius: 999px;
  font-size: 0.75rem;
  padding: 4px 10px;
}

.pay-btn {
  margin-top: 8px;
  margin-right: 8px;
  border: 1px solid rgba(34, 197, 94, 0.6);
  background: transparent;
  color: #86efac;
  border-radius: 999px;
  font-size: 0.75rem;
  padding: 4px 10px;
}

.col-price,
.col-qty,
.col-sub {
  text-align: right;
}

.col-price.cell,
.col-qty.cell,
.col-sub.cell {
  font-variant-numeric: tabular-nums;
}

.col-sub.cell {
  font-weight: 700;
  color: var(--mall-orange-bright);
}

.footer-bar {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid var(--mall-border, #2c2c34);
  background: rgba(0, 0, 0, 0.2);
}

.total-label {
  font-size: 0.9rem;
  color: var(--mall-text-muted);
}

.total-amount {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--mall-orange-bright);
}

@media (max-width: 640px) {
  .cart-card {
    overflow-x: auto;
  }

  .table-head,
  .row {
    min-width: 520px;
  }
}
</style>
