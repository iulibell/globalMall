<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchUserOrderPayDeadline, payUserOrder } from '@/api/portal'
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

const orderId = computed(() => String(route.query.orderId || '').trim())
const expireAtMs = ref(0)
const nowMs = ref(Date.now())
const paying = ref(false)
const tip = ref('')
const error = ref('')
let timer = null

const remainMs = computed(() => Math.max(0, expireAtMs.value - nowMs.value))
const hasExpireAt = computed(() => Number.isFinite(expireAtMs.value) && expireAtMs.value > 0)
const expired = computed(() => hasExpireAt.value && remainMs.value <= 0)

async function doPay() {
  if (!orderId.value || expired.value) return
  paying.value = true
  tip.value = ''
  error.value = ''
  try {
    const { ok, data } = await payUserOrder(orderId.value)
    if (!ok || data?.code !== 200) {
      error.value = data?.message || tx('pay_page_pay_fail')
      return
    }
    tip.value = data?.message || tx('pay_page_pay_ok')
    setTimeout(() => {
      router.push({ name: 'profile', query: { tab: 'orders' } })
    }, 500)
  } catch {
    error.value = tx('pay_page_network_error')
  } finally {
    paying.value = false
  }
}

async function syncDeadlineFromServer() {
  if (!orderId.value) return
  try {
    const { ok, data } = await fetchUserOrderPayDeadline(orderId.value)
    if (!ok || data?.code !== 200) return
    const serverExpireAt = Number(data?.data?.expireAt || 0)
    if (Number.isFinite(serverExpireAt) && serverExpireAt > 0) {
      expireAtMs.value = serverExpireAt
    }
  } catch {
    // ignore and keep fallback value from query
  }
}

onMounted(async () => {
  const queryExpireAt = Number(route.query.expireAt || 0)
  expireAtMs.value = Number.isFinite(queryExpireAt) && queryExpireAt > 0 ? queryExpireAt : 0
  nowMs.value = Date.now()
  timer = setInterval(() => {
    nowMs.value = Date.now()
  }, 1000)
  await syncDeadlineFromServer()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <main class="pay-page">
    <div class="container pay-card">
      <p class="eyebrow">GLOBALMALL CHECKOUT</p>
      <h1>{{ tx('pay_page_title') }}</h1>

      <div class="info-grid">
        <p class="label">{{ tx('pay_page_order_id') }}</p>
        <p class="value mono">{{ orderId || '-' }}</p>
      </div>

      <p class="hint">{{ tx('pay_page_hint_30min') }}</p>
      <p v-if="expired" class="hint error">{{ tx('pay_page_expired') }}</p>

      <p v-if="tip" class="tip">{{ tip }}</p>
      <p v-if="error" class="tip error">{{ error }}</p>

      <div class="actions">
        <button class="btn ghost" type="button" @click="router.push({ name: 'profile', query: { tab: 'orders' } })">
          {{ tx('pay_page_back_orders') }}
        </button>
        <button class="btn primary" type="button" :disabled="paying || expired || !orderId" @click="doPay">
          {{ paying ? tx('pay_page_paying') : tx('pay_page_confirm') }}
        </button>
      </div>
    </div>
  </main>
</template>

<style scoped>
.pay-page {
  min-height: calc(100vh - 110px);
  padding: 32px 16px 40px;
  background:
    radial-gradient(circle at 10% 0%, rgba(255, 163, 26, 0.12), transparent 45%),
    radial-gradient(circle at 90% 100%, rgba(251, 146, 60, 0.1), transparent 42%),
    var(--mall-black-soft, #09090b);
  display: flex;
  align-items: center;
}

.container {
  width: min(760px, 100%);
  margin: 0 auto;
}

.pay-card {
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #2f2f39;
  background: linear-gradient(180deg, #17171e 0%, #121218 100%);
  box-shadow: 0 20px 56px rgba(0, 0, 0, 0.35);
}

.eyebrow {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  color: rgba(255, 173, 102, 0.75);
}

h1 {
  margin: 8px 0 18px;
  font-size: 1.95rem;
}

.info-grid {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 8px 14px;
  padding: 14px;
  border: 1px solid #2d2d36;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.01);
}

.label {
  margin: 0;
  color: var(--mall-text-muted);
  font-size: 0.86rem;
}

.value {
  margin: 0;
  font-weight: 600;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  word-break: break-all;
}

.countdown {
  color: #86efac;
  font-size: 1.2rem;
  letter-spacing: 0.04em;
}

.hint {
  margin: 12px 2px 0;
  color: var(--mall-text-muted);
  font-size: 0.9rem;
}

.meta {
  color: var(--mall-text-muted);
}
.meta.error {
  color: #f87171;
}
.tip {
  margin: 12px 2px 0;
  color: #86efac;
}
.tip.error {
  color: #f87171;
}
.actions {
  margin-top: 20px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.btn {
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  font-weight: 700;
  cursor: pointer;
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

.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .pay-card {
    padding: 18px;
  }
  h1 {
    font-size: 1.45rem;
  }
  .info-grid {
    grid-template-columns: 1fr;
    gap: 4px;
  }
  .label {
    font-size: 0.78rem;
  }
}
</style>
