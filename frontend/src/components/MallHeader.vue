<script setup>
import { computed, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import MallLanguageDropdown from './MallLanguageDropdown.vue'
import { useUiLang } from '@/composables/useUiLang.js'
import { useMultiDictionary } from '@/composables/useMultiDictionary.js'
import { pageDictFallback } from '@/utils/pageDictionaryFallback.js'

const query = ref('')
const searchScope = ref('all')
const emit = defineEmits(['search'])
const router = useRouter()

const { uiLang } = useUiLang()
const { t } = useMultiDictionary(['page_mall'], uiLang)

function tx(key) {
  return t('page_mall', key, pageDictFallback('page_mall', key, uiLang.value))
}

function onSearch() {
  emit('search', query.value)
}

const role = computed(() => localStorage.getItem('role') || '')
const canAccessProfile = computed(() => ['super', 'manager', 'reviewer', 'merchant', 'user'].includes(role.value))
const isLoggedIn = computed(() => Boolean(localStorage.getItem('satoken')))
const displayName = computed(() => {
  const nickname = (localStorage.getItem('nickname') || '').trim()
  if (nickname) {
    return nickname
  }
  const username = (localStorage.getItem('username') || '').trim()
  if (username) {
    return username
  }
  return t('page_mall', 'header_default_user', pageDictFallback('page_mall', 'header_default_user', uiLang.value))
})

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
</script>

<template>
  <div class="mall-header">
    <div class="top-bar">
      <div class="container top-inner">
        <nav class="top-links" aria-label="快捷链接">
          <MallLanguageDropdown />
          <div v-if="isLoggedIn" class="user-menu">
            <button type="button" class="user-trigger" @click="router.push({ name: 'profile' })">
              {{ tx('header_guest_hi') }}{{ displayName }}
            </button>
            <div class="user-dropdown">
              <button type="button" class="dropdown-item" @click="router.push({ name: 'profile' })">
                {{ tx('header_profile') }}
              </button>
              <button type="button" class="dropdown-item danger" @click="logout">
                {{ tx('header_logout') }}
              </button>
            </div>
          </div>
          <RouterLink v-else to="/login">{{ tx('header_guest_hi') }}{{ tx('header_login_link') }}</RouterLink>
          <RouterLink v-if="!isLoggedIn" to="/register">{{ tx('header_register_link') }}</RouterLink>
          <a href="#">{{ tx('header_returns') }}</a>
        </nav>
      </div>
    </div>

    <div class="main-bar">
      <div class="container main-inner">
        <RouterLink class="logo" to="/" aria-label="GlobalMall 首页">
          <span class="logo-mark">GM</span>
          <span class="logo-text">Global<span class="accent">Mall</span></span>
        </RouterLink>

        <form class="search" @submit.prevent="onSearch">
          <select v-model="searchScope" class="search-scope" :aria-label="tx('header_search_placeholder')">
            <option value="all">{{ tx('header_scope_all') }}</option>
            <option value="digital">{{ tx('header_scope_digital') }}</option>
            <option value="home">{{ tx('header_scope_home') }}</option>
            <option value="fashion">{{ tx('header_scope_fashion') }}</option>
          </select>
          <input
            v-model="query"
            type="search"
            class="search-input"
            :placeholder="tx('header_search_placeholder')"
            autocomplete="off"
          />
          <button type="submit" class="search-btn" :aria-label="tx('header_search_placeholder')">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20l-4.35-4.35" />
            </svg>
          </button>
        </form>

        <div class="main-actions">
          <a class="cart" href="#" :aria-label="tx('header_cart')">
            <span class="cart-count">0</span>
            <svg width="36" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
              <path d="M6 6h15l-1.5 9h-12z" />
              <circle cx="9" cy="20" r="1.5" fill="currentColor" stroke="none" />
              <circle cx="18" cy="20" r="1.5" fill="currentColor" stroke="none" />
              <path d="M6 6 5 3H2" />
            </svg>
            <span class="cart-label">{{ tx('header_cart') }}</span>
          </a>
        </div>
      </div>
    </div>

    <div class="sub-bar">
      <div class="container sub-inner">
        <button type="button" class="all-dept" aria-expanded="false">
          <span class="hamburger" />
          {{ tx('header_all_cats') }}
        </button>
        <nav class="sub-links" aria-label="商品分类与频道">
          <a href="#">{{ tx('header_sub_today') }}</a>
          <a href="#">{{ tx('header_sub_service') }}</a>
          <a href="#">{{ tx('header_sub_gift_list') }}</a>
          <a href="#">{{ tx('header_sub_giftcard') }}</a>
          <a href="#">{{ tx('header_sub_sell') }}</a>
        </nav>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mall-header {
  position: sticky;
  top: 0;
  z-index: 50;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.top-bar {
  background: var(--mall-black);
  color: var(--mall-text);
  font-size: 0.8rem;
  border-bottom: 1px solid var(--mall-border);
}

.top-inner {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-height: 38px;
}

.top-links {
  display: flex;
  align-items: center;
  gap: 18px;
}

.top-links a {
  padding: 4px 0;
  color: var(--mall-text-muted);
  text-decoration: none;
}

.user-menu {
  position: relative;
  padding-bottom: 8px;
  margin-bottom: -8px;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  height: 28px;
  border: none;
  background: transparent;
  padding: 4px 0;
  color: var(--mall-text-muted);
  font: inherit;
  font-size: 0.8rem;
  line-height: 1;
  cursor: pointer;
}

.user-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  min-width: 120px;
  padding: 6px;
  border-radius: 8px;
  border: 1px solid var(--mall-border);
  background: var(--mall-surface-elevated);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.35);
  display: none;
  z-index: 80;
}

.user-menu:hover .user-dropdown {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dropdown-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 34px;
  border: none;
  background: transparent;
  color: var(--mall-text-muted);
  text-align: left;
  line-height: 34px;
  padding: 0 8px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  margin: 0;
  appearance: none;
  -webkit-appearance: none;
}

.dropdown-item:hover {
  background: rgba(255, 138, 0, 0.12);
  color: var(--mall-orange-bright);
}

.dropdown-item.danger:hover {
  color: #fca5a5;
}

.top-links a:hover {
  color: var(--mall-orange-bright);
}

.main-bar {
  background: var(--mall-black-soft);
  padding: 10px 0 14px;
}

.main-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.logo {
  display: flex;
  align-items: baseline;
  gap: 8px;
  flex-shrink: 0;
  padding: 4px 8px 4px 0;
  text-decoration: none;
  color: inherit;
}

.logo-mark {
  width: 42px;
  height: 42px;
  background: linear-gradient(145deg, var(--mall-orange) 0%, var(--mall-orange-dim) 100%);
  color: var(--mall-black);
  font-weight: 800;
  font-size: 0.85rem;
  letter-spacing: -0.03em;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.logo-text {
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.logo-text .accent {
  color: var(--mall-orange);
}

.search {
  flex: 1;
  display: flex;
  min-width: 200px;
  max-width: 720px;
  height: 42px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 0 0 2px var(--mall-orange-glow);
}

.search-scope {
  min-width: 72px;
  width: auto;
  border: none;
  background: var(--mall-surface-elevated);
  color: var(--mall-text);
  font-size: 0.75rem;
  padding: 0 34px 0 10px;
  border-right: 1px solid var(--mall-border);
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' fill='none' stroke='%23f4f4f5' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M2.5 4.5L6 8l3.5-3.5'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-size: 12px 12px;
  background-position: right 14px center;
}

.search-input {
  flex: 1;
  border: none;
  padding: 0 14px;
  font-size: 0.95rem;
  background: #fff;
  color: #111;
}

.search-input::placeholder {
  color: #6b7280;
}

.search-btn {
  width: 52px;
  border: none;
  background: linear-gradient(180deg, var(--mall-orange-bright) 0%, var(--mall-orange) 55%, var(--mall-orange-dim) 100%);
  color: var(--mall-black);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  filter: brightness(1.08);
}

.main-actions {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.cart {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 10px;
  color: var(--mall-text);
}

.cart:hover {
  outline: 1px solid var(--mall-text-muted);
  border-radius: 2px;
}

.cart-count {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(2px);
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  font-size: 0.75rem;
  font-weight: 700;
  background: var(--mall-orange);
  color: var(--mall-black);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-label {
  font-size: 0.8rem;
  font-weight: 700;
  margin-top: -4px;
}

.sub-bar {
  background: var(--mall-surface);
  border-top: 1px solid var(--mall-border);
  border-bottom: 1px solid var(--mall-border);
}

.sub-inner {
  display: flex;
  align-items: center;
  gap: 4px;
  min-height: 40px;
  overflow-x: auto;
  scrollbar-width: none;
}

.sub-inner::-webkit-scrollbar {
  display: none;
}

.all-dept {
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: transparent;
  color: var(--mall-text);
  font-weight: 600;
  padding: 8px 12px;
  border-radius: 2px;
  white-space: nowrap;
}

.all-dept:hover {
  outline: 1px solid var(--mall-text-muted);
}

.hamburger {
  width: 16px;
  height: 12px;
  background: linear-gradient(
    var(--mall-text) 0 2px,
    transparent 0
  );
  background-size: 100% 5px;
}

.sub-links {
  display: flex;
  gap: 4px;
  padding-left: 8px;
}

.sub-links a {
  padding: 8px 12px;
  font-size: 0.875rem;
  color: var(--mall-text-muted);
  white-space: nowrap;
  border-radius: 2px;
}

.sub-links a:hover {
  color: var(--mall-orange-bright);
}

@media (max-width: 768px) {
  .top-links {
    display: none;
  }

  .search {
    order: 3;
    flex: 1 1 100%;
    max-width: none;
  }

  .main-inner {
    justify-content: space-between;
  }

  .main-actions {
    margin-left: 0;
  }
}
</style>
