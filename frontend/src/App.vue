<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import MallHeader from './components/MallHeader.vue'

const route = useRoute()
const lastQuery = ref('')

const showMallHeader = computed(() => route.meta.layout === 'mall')
const showSearchToast = computed(() => route.name === 'home' && lastQuery.value)

function onSearch(q) {
  lastQuery.value = (q || '').trim()
}
</script>

<template>
  <div class="app-shell">
    <MallHeader v-if="showMallHeader" @search="onSearch" />
    <p v-if="showSearchToast" class="search-toast container" role="status">
      正在展示与「<strong>{{ lastQuery }}</strong>」相关的搜索结果。
    </p>
    <router-view v-slot="{ Component, route: currentRoute }">
      <component
        :is="Component"
        v-bind="currentRoute.name === 'home' ? { searchQuery: lastQuery } : {}"
      />
    </router-view>
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
  padding: 10px 16px;
  font-size: 0.85rem;
  color: var(--mall-text-muted);
  background: rgba(255, 138, 0, 0.08);
  border-bottom: 1px solid var(--mall-border);
}

.search-toast strong {
  color: var(--mall-orange-bright);
}
</style>
