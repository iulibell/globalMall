<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addUserCart, fetchGoodsDetail } from '@/api/portal'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const goods = ref(null)
const addingCart = ref(false)
const cartTip = ref('')
const cartError = ref('')

const goodsId = computed(() => String(route.params.goodsId || '').trim())

const priceText = computed(() => {
  const p = goods.value?.price
  if (p == null || p === '') return '-'
  const n = Number(p)
  return Number.isFinite(n) ? n.toFixed(2) : String(p)
})

const imageSrc = computed(() => {
  const pic = String(goods.value?.picture || '').trim()
  if (!pic) return ''
  if (/^https?:\/\//i.test(pic) || /^data:/i.test(pic)) return pic
  return `/src/assets/picture/${encodeURIComponent(pic)}`
})

async function load() {
  const id = goodsId.value
  if (!id) {
    loading.value = false
    error.value = '缺少商品 ID'
    goods.value = null
    return
  }
  loading.value = true
  error.value = ''
  goods.value = null
  cartTip.value = ''
  cartError.value = ''
  try {
    const { ok, data } = await fetchGoodsDetail(id)
    if (!ok || data?.code !== 200) {
      error.value = data?.message || '加载商品失败'
      return
    }
    const row = data?.data
    if (!row || !row.goodsId) {
      error.value = '未找到该商品'
      return
    }
    goods.value = row
  } catch {
    error.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    loading.value = false
  }
}

async function addToCart() {
  if (!goods.value) return
  const userId = (localStorage.getItem('userId') || '').trim()
  const role = (localStorage.getItem('role') || '').trim()
  if (!userId) {
    cartError.value = '请先登录后再加入购物车'
    return
  }
  if (role && role !== 'user') {
    cartError.value = '当前账号不是普通用户，暂不支持加入购物车'
    return
  }
  addingCart.value = true
  cartTip.value = ''
  cartError.value = ''
  try {
    const payload = {
      userId,
      merchantId: String(goods.value.merchantId || '').trim(),
      goodsId: String(goods.value.goodsId || '').trim(),
      skuCode: goods.value.skuCode || '',
      skuName: String(goods.value.skuName || '').trim(),
      picture: String(goods.value.picture || '').trim(),
      price: goods.value.price,
      quantity: 1,
      checked: 1,
    }
    const { ok, data } = await addUserCart(payload)
    if (!ok || data?.code !== 200) {
      cartError.value = data?.message || '加入购物车失败'
      return
    }
    cartTip.value = typeof data?.data === 'string' ? data.data : data?.message || '已加入购物车'
  } catch {
    cartError.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    addingCart.value = false
  }
}

watch(
  () => route.params.goodsId,
  () => {
    load()
  },
  { immediate: true },
)
</script>

<template>
  <main class="detail-page">
    <div class="container">
      <nav class="breadcrumb">
        <button type="button" class="link" @click="router.push({ name: 'home' })">首页</button>
        <span class="sep">›</span>
        <span class="current">商品详情</span>
      </nav>

      <p v-if="loading" class="status">加载中...</p>
      <p v-else-if="error" class="status error">{{ error }}</p>

      <article v-else-if="goods" class="detail-card">
        <div class="media">
          <img v-if="imageSrc" :src="imageSrc" :alt="goods.skuName" class="photo" />
          <div v-else class="photo-placeholder" role="img" :aria-label="goods.skuName" />
        </div>
        <div class="info">
          <p class="goods-id">商品编号：{{ goods.goodsId }}</p>
          <h1 class="title">{{ goods.skuName }}</h1>
          <p class="price-line">
            <span class="currency">¥</span><span class="amount">{{ priceText }}</span>
          </p>
          <dl class="meta">
            <div v-if="goods.type" class="row">
              <dt>类型</dt>
              <dd>{{ goods.type }}</dd>
            </div>
            <div v-if="goods.category != null" class="row">
              <dt>分类</dt>
              <dd>{{ goods.category }}</dd>
            </div>
            <div v-if="goods.merchantId" class="row">
              <dt>商家</dt>
              <dd>{{ goods.merchantId }}</dd>
            </div>
            <div v-if="goods.picture && !imageSrc" class="row">
              <dt>图片</dt>
              <dd class="muted">{{ goods.picture }}</dd>
            </div>
          </dl>
          <section v-if="goods.description" class="desc">
            <h2>商品介绍</h2>
            <p>{{ goods.description }}</p>
          </section>
          <p v-if="cartTip" class="cart-tip">{{ cartTip }}</p>
          <p v-if="cartError" class="cart-tip error">{{ cartError }}</p>
          <div class="actions">
            <button type="button" class="btn primary" :disabled="addingCart" @click="addToCart">
              {{ addingCart ? '加入中...' : '加入购物车' }}
            </button>
            <button type="button" class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
          </div>
        </div>
      </article>
    </div>
  </main>
</template>

<style scoped>
.detail-page {
  flex: 1;
  padding: 20px 0 48px;
  background: var(--mall-black-soft, #09090b);
}

.container {
  max-width: 1080px;
  margin: 0 auto;
  padding: 0 16px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: var(--mall-text-muted);
  margin-bottom: 20px;
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

.status {
  margin: 0;
  font-size: 0.95rem;
  color: var(--mall-text-muted);
}

.status.error {
  color: #f87171;
}

.detail-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr);
  gap: 28px;
  background: var(--mall-surface-elevated, #16161b);
  border: 1px solid var(--mall-border, #2c2c34);
  border-radius: 12px;
  padding: 24px;
}

.media {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--mall-border, #2c2c34);
  background: #111117;
}

.photo {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
}

.photo-placeholder {
  aspect-ratio: 1;
  background: linear-gradient(160deg, #3d3530 0%, #1a1512 100%);
}

.info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goods-id {
  margin: 0;
  font-size: 0.8rem;
  color: var(--mall-text-muted);
}

.title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.3;
  color: var(--mall-text, #fafafa);
}

.price-line {
  margin: 4px 0 0;
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--mall-orange-bright, #ff9f1a);
}

.price-line .currency {
  font-size: 1rem;
  margin-right: 2px;
}

.meta {
  margin: 8px 0 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta .row {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  font-size: 0.88rem;
}

.meta dt {
  margin: 0;
  color: var(--mall-text-muted);
}

.meta dd {
  margin: 0;
  color: var(--mall-text, #e4e4e7);
}

.meta .muted {
  color: var(--mall-text-muted);
  word-break: break-all;
}

.desc {
  margin-top: 8px;
}

.desc h2 {
  margin: 0 0 8px;
  font-size: 0.95rem;
  color: var(--mall-orange-bright);
}

.desc p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.55;
  color: var(--mall-text-muted);
}

.cart-tip {
  margin: 6px 0 0;
  font-size: 0.88rem;
  color: #86efac;
}

.cart-tip.error {
  color: #f87171;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: auto;
  padding-top: 16px;
}

.btn {
  border-radius: 999px;
  padding: 10px 20px;
  font-weight: 700;
  font-size: 0.88rem;
  border: none;
  cursor: pointer;
}

.btn.primary {
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
  color: #0a0a0b;
}

.btn.primary:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.btn.ghost {
  background: transparent;
  border: 1px solid #3a3a44;
  color: var(--mall-text);
}

@media (max-width: 768px) {
  .detail-card {
    grid-template-columns: 1fr;
  }
}
</style>
