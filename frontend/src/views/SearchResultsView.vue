<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { searchGoods } from '@/api/search'

const route = useRoute()
const searching = ref(false)
const searchError = ref('')
const searchResults = ref([])

const searchQuery = computed(() => String(route.query.q || '').trim())

function toCard(item, i) {
  const picture = String(item?.picture || '').trim()
  const imageUrl = (/^https?:\/\//i.test(picture) || /^data:/i.test(picture))
    ? picture
    : (picture ? `/src/assets/picture/${encodeURIComponent(picture)}` : '')
  return {
    goodsId: item.goodsId != null ? String(item.goodsId) : '',
    title: item.skuName || item.description || `商品 #${item.goodsId || i + 1}`,
    price: Number(item.price || 0).toFixed(2),
    rating: 4.2,
    reviews: 0,
    badge: item.type || '',
    imageUrl,
    imageTone: ['a', 'b', 'c'][Math.abs((item.category ?? i) % 3)],
  }
}

async function fetchSearchResults(query) {
  const q = (query || '').trim()
  if (!q) {
    searchResults.value = []
    searchError.value = ''
    return
  }
  searching.value = true
  searchError.value = ''
  try {
    const { ok, data } = await searchGoods({ query: q, page: 0, size: 12 })
    if (!ok || data?.code !== 200) {
      searchError.value = data?.message || '搜索服务不可用，请稍后重试'
      searchResults.value = []
      return
    }
    const items = Array.isArray(data?.data?.items) ? data.data.items : []
    searchResults.value = items.map(toCard)
  } catch {
    searchError.value = '搜索请求失败，请确认 gl-search 已启动'
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

watch(
  () => searchQuery.value,
  (value) => {
    fetchSearchResults(value)
  },
  { immediate: true },
)
</script>

<template>
  <main class="search-page container">
    <section class="block">
      <div class="block-head">
        <h2>搜索结果</h2>
        <span class="result-tip">关键词：{{ searchQuery || '（空）' }}</span>
      </div>
      <p v-if="searching" class="status">正在搜索商品...</p>
      <p v-else-if="searchError" class="status error">{{ searchError }}</p>
      <p v-else-if="!searchQuery" class="status">请输入关键词进行搜索</p>
      <p v-else-if="searchResults.length === 0" class="status">暂无匹配商品</p>
      <div v-else class="grid">
        <ProductCard v-for="(p, i) in searchResults" :key="'s-' + i" v-bind="p" />
      </div>
    </section>
  </main>
</template>

<style scoped>
.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.search-page {
  padding: 24px 16px 40px;
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

.result-tip {
  font-size: 0.88rem;
  color: var(--mall-text-muted);
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
