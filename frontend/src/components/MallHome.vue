<script setup>
import { ref } from 'vue'
import ProductCard from './ProductCard.vue'
import HeroCarousel from './HeroCarousel.vue'
import { fetchHotGoods } from '@/api/portal'
import { useUiLang } from '@/composables/useUiLang.js'
import { useMultiDictionary } from '@/composables/useMultiDictionary.js'
import { pageDictFallback } from '@/utils/pageDictionaryFallback.js'

const { uiLang } = useUiLang()
const { t } = useMultiDictionary(['page_mall'], uiLang)

function tx(key) {
  return t('page_mall', key, pageDictFallback('page_mall', key, uiLang.value))
}

const hotLoading = ref(false)
const hotError = ref('')
const hotGoods = ref([])

function toCard(item, i) {
  const picture = String(item?.picture || '').trim()
  const imageUrl = (/^https?:\/\//i.test(picture) || /^data:/i.test(picture))
    ? picture
    : (picture ? `/src/assets/picture/${encodeURIComponent(picture)}` : '')
  const goodsPrefix = t('page_mall', 'mall_goods_prefix', pageDictFallback('page_mall', 'mall_goods_prefix', uiLang.value))
  const pictureRaw = String(item?.picture || '').trim()
  return {
    goodsId: item.goodsId != null ? String(item.goodsId) : '',
    title: item.skuName || item.description || `${goodsPrefix}${item.goodsId || i + 1}`,
    price: Number(item.price || 0).toFixed(2),
    merchantId: item.merchantId != null ? String(item.merchantId).trim() : '',
    skuCode: item.skuCode != null ? String(item.skuCode).trim() : '',
    pictureForCart: pictureRaw,
    rating: 4.2,
    reviews: 0,
    badge: item.type || '',
    imageUrl,
    imageTone: ['a', 'b', 'c'][Math.abs((item.category ?? i) % 3)],
  }
}

async function fetchHomeHotGoods() {
  hotLoading.value = true
  hotError.value = ''
  try {
    const { ok, data } = await fetchHotGoods({ pageNum: 1, pageSize: 12 })
    if (!ok || data?.code !== 200) {
      hotError.value = data?.message || tx('mall_hot_load_fail')
      hotGoods.value = []
      return
    }
    const items = Array.isArray(data?.data) ? data.data : []
    hotGoods.value = items.map(toCard)
  } catch {
    hotError.value = tx('mall_hot_request_fail')
    hotGoods.value = []
  } finally {
    hotLoading.value = false
  }
}

fetchHomeHotGoods()
</script>

<template>
  <main class="home">
    <HeroCarousel />

    <section class="block container">
      <div class="block-head">
        <h2>{{ tx('mall_hot_title') }}</h2>
      </div>
      <p v-if="hotLoading" class="status">{{ tx('mall_hot_loading') }}</p>
      <p v-else-if="hotError" class="status error">{{ hotError }}</p>
      <p v-else-if="hotGoods.length === 0" class="status">{{ tx('mall_hot_empty') }}</p>
      <div v-else class="grid">
        <ProductCard v-for="(p, i) in hotGoods" :key="'h-' + i" v-bind="p" />
      </div>
    </section>

    <footer class="site-footer">
      <div class="container footer-inner">
        <p class="copyright">
          © {{ new Date().getFullYear() }} GlobalMall · {{ tx('mall_footer_rights') }}
        </p>
      </div>
    </footer>
  </main>
</template>

<style scoped>
.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.home {
  padding-bottom: 0;
}

.site-footer {
  margin-top: 8px;
  border-top: 1px solid var(--mall-border);
  background: var(--mall-black-soft);
}

.footer-inner {
  padding: 20px 16px 28px;
  text-align: center;
}

.copyright {
  margin: 0;
  font-size: 0.8rem;
  color: var(--mall-text-muted);
  line-height: 1.5;
}

.block {
  margin-top: 28px;
  margin-bottom: 40px;
}

.block-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.block-head h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
}

.status {
  margin: 0 0 12px;
  font-size: 0.9rem;
  color: var(--mall-text-muted);
}

.status.error {
  color: #f87171;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
