<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MallHeader from './components/MallHeader.vue'
import { useUiLang } from '@/composables/useUiLang.js'
import { useMultiDictionary } from '@/composables/useMultiDictionary.js'
import { pageDictFallback } from '@/utils/pageDictionaryFallback.js'

const route = useRoute()
const router = useRouter()
const lastQuery = ref('')

const { uiLang } = useUiLang()
const { t } = useMultiDictionary(['page_mall'], uiLang)

function tx(key) {
  return t('page_mall', key, pageDictFallback('page_mall', key, uiLang.value))
}

const showMallHeader = computed(() => route.meta.layout === 'mall')
const showSearchToast = computed(() => route.name === 'search' && lastQuery.value)

function onSearch(q) {
  const query = (q || '').trim()
  lastQuery.value = query
  router.push({ name: 'search', query: query ? { q: query } : {} })
}
</script>

<template>
  <div class="app-shell">
    <MallHeader v-if="showMallHeader" @search="onSearch" />
    <p v-if="showSearchToast" class="search-toast" role="status">
      <span class="container search-toast-inner">
        <span class="search-toast-text">
          {{ tx('mall_search_toast_prefix') }}<strong>{{ lastQuery }}</strong>{{ tx('mall_search_toast_suffix') }}
        </span>
      </span>
    </p>
    <router-view />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.search-toast {
  margin: 0;
  font-size: 0.85rem;
  color: var(--mall-text-muted);
  background: rgba(255, 138, 0, 0.08);
  border-bottom: 1px solid var(--mall-border);
}

.search-toast-inner {
  display: block;
  padding-top: 10px;
  padding-bottom: 10px;
}

.search-toast-text {
  display: block;
  padding-left: 12px;
}

.search-toast strong {
  color: var(--mall-orange-bright);
}
</style>
