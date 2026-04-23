<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

const query = ref('')
const emit = defineEmits(['search'])

function onSearch() {
  emit('search', query.value)
}
</script>

<template>
  <div class="mall-header">
    <div class="top-bar">
      <div class="container top-inner">
        <nav class="top-links" aria-label="快捷链接">
          <a href="#">简体中文</a>
          <RouterLink to="/login">您好，请登录</RouterLink>
          <RouterLink to="/register">免费注册</RouterLink>
          <a href="#">退换货与订单</a>
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
          <select class="search-scope" aria-label="搜索范围">
            <option>全部</option>
            <option>数码</option>
            <option>家居</option>
            <option>服饰</option>
          </select>
          <input
            v-model="query"
            type="search"
            class="search-input"
            placeholder="搜索 GlobalMall 商品"
            autocomplete="off"
          />
          <button type="submit" class="search-btn" aria-label="搜索">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20l-4.35-4.35" />
            </svg>
          </button>
        </form>

        <div class="main-actions">
          <a class="cart" href="#" aria-label="购物车">
            <span class="cart-count">0</span>
            <svg width="36" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
              <path d="M6 6h15l-1.5 9h-12z" />
              <circle cx="9" cy="20" r="1.5" fill="currentColor" stroke="none" />
              <circle cx="18" cy="20" r="1.5" fill="currentColor" stroke="none" />
              <path d="M6 6 5 3H2" />
            </svg>
            <span class="cart-label">购物车</span>
          </a>
        </div>
      </div>
    </div>

    <div class="sub-bar">
      <div class="container sub-inner">
        <button type="button" class="all-dept" aria-expanded="false">
          <span class="hamburger" />
          全部分类
        </button>
        <nav class="sub-links" aria-label="商品分类与频道">
          <a href="#">今日特惠</a>
          <a href="#">客户服务</a>
          <a href="#">礼品清单</a>
          <a href="#">礼品卡</a>
          <a href="#">我要开店</a>
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
  gap: 18px;
}

.top-links a {
  padding: 4px 0;
  color: var(--mall-text-muted);
  text-decoration: none;
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
  padding: 0 6px;
  border-right: 1px solid var(--mall-border);
  cursor: pointer;
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
