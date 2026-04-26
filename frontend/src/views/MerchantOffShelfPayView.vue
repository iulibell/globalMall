<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { merchantPayForOffShelf } from '@/api/portal'

const router = useRouter()
const route = useRoute()

const submitting = ref(false)
const tip = ref('')
const error = ref('')
const row = ref(null)
const userPhone = ref('')

const token = computed(() => localStorage.getItem('satoken') || '')
const role = computed(() => localStorage.getItem('role') || '')
const merchantPhone = computed(() => String(localStorage.getItem('phone') || '').trim())
const city = computed(() => String(localStorage.getItem('city') || row.value?.city || '').trim())
const isMerchant = computed(() => role.value === 'merchant' && Boolean(token.value))

const payable = computed(() => {
  const fee = Number(row.value?.fee ?? Number.NaN)
  return Number(row.value?.status) === 1 && Number.isFinite(fee) && fee > 0
})

function statusText(v) {
  const s = Number(v)
  if (s === 0) return '待审核'
  if (s === 1) return '待支付'
  if (s === 2) return '已支付'
  if (s === 3) return '超时未支付'
  if (s === 4) return '下架完成'
  return '未知'
}

function loadRowFromCache() {
  const offShelfId = String(route.query.offShelfId || '').trim()
  const raw = sessionStorage.getItem('merchant_off_shelf_pay_row')
  if (!offShelfId || !raw) return
  try {
    const parsed = JSON.parse(raw)
    if (String(parsed?.id || '').trim() === offShelfId) {
      row.value = parsed
    }
  } catch {
    // ignore
  }
}

async function submitPay() {
  const offShelfId = row.value?.id
  if (offShelfId == null) {
    error.value = '缺少下架申请信息，请返回列表重试'
    return
  }
  if (!String(userPhone.value || '').trim()) {
    error.value = '收件人手机号不能为空'
    return
  }
  if (!merchantPhone.value) {
    error.value = '商家手机号为空，请先在个人中心完善'
    return
  }
  if (!city.value) {
    error.value = '城市为空，请先在个人中心完善'
    return
  }
  const fee = Number(row.value?.fee ?? 0)
  if (!Number.isFinite(fee) || fee <= 0) {
    error.value = '费用未核定或不合法，请待物流仓管在 WMS 核定费用后刷新'
    return
  }
  submitting.value = true
  tip.value = ''
  error.value = ''
  try {
    const { ok, data } = await merchantPayForOffShelf({
      offShelfId,
      userPhone: String(userPhone.value).trim(),
      merchantPhone: merchantPhone.value,
      city: city.value,
      fee,
    })
    if (!ok || data?.code !== 200) {
      error.value = data?.message || '支付失败'
      return
    }
    // 后端若误用 success(文案) 会把文案放在 data，message 仍为 operation_success
    const topMsg = data?.message
    const payloadStr = typeof data?.data === 'string' ? String(data.data).trim() : ''
    tip.value =
      topMsg && topMsg !== 'operation_success'
        ? topMsg
        : payloadStr || '支付成功'
    if (row.value) {
      row.value = { ...row.value, status: 2 }
      sessionStorage.setItem('merchant_off_shelf_pay_row', JSON.stringify(row.value))
    }
    await router.push({ name: 'profile' })
  } catch {
    error.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadRowFromCache()
  userPhone.value = merchantPhone.value
})
</script>

<template>
  <div class="pay-page">
    <section v-if="!token" class="forbidden-card">
      <h1>请先登录</h1>
      <p>下架申请支付需要商家账号登录。</p>
      <div class="action-row">
        <button type="button" class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
        <button type="button" class="btn primary" @click="router.push({ name: 'login' })">去登录</button>
      </div>
    </section>

    <section v-else-if="!isMerchant" class="forbidden-card">
      <h1>仅限商家</h1>
      <p>当前账号不是商家角色，无法支付下架申请。</p>
      <div class="action-row">
        <button type="button" class="btn ghost" @click="router.push({ name: 'profile' })">返回个人中心</button>
      </div>
    </section>

    <section v-else-if="!row" class="forbidden-card">
      <h1>缺少申请信息</h1>
      <p>请从“个人中心 - 下架申请”点击“前往支付”进入本页。</p>
      <div class="action-row">
        <button type="button" class="btn primary" @click="router.push({ name: 'profile' })">返回个人中心</button>
      </div>
    </section>

    <main v-else class="pay-main">
      <header class="pay-head">
        <h1>下架申请支付</h1>
        <button type="button" class="back-btn" @click="router.push({ name: 'profile' })">返回下架申请</button>
      </header>

      <section class="panel">
        <p v-if="tip" class="msg">{{ tip }}</p>
        <p v-if="error" class="msg error">{{ error }}</p>

        <div class="detail-grid">
          <p><span>申请ID：</span>{{ row.id }}</p>
          <p><span>商品ID：</span>{{ row.goodsId }}</p>
          <p><span>城市：</span>{{ row.city || '-' }}</p>
          <p><span>费用：</span>{{ row.fee ?? '-' }}</p>
          <p><span>状态：</span>{{ statusText(row.status) }}</p>
        </div>

        <label class="field">
          <span>收件人手机号</span>
          <input v-model="userPhone" type="text" class="input" />
        </label>

        <div class="actions">
          <button type="button" class="pay-btn" :disabled="submitting || !payable" @click="submitPay">
            {{ submitting ? '支付中...' : '确认支付' }}
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.pay-page { min-height: 100vh; background: #09090b; padding: 24px; }
.forbidden-card { max-width: 720px; margin: 120px auto 0; background: #16161b; border: 1px solid var(--mall-border); border-radius: 14px; padding: 28px; }
.action-row { display: flex; gap: 12px; margin-top: 20px; }
.pay-main { max-width: 920px; margin: 0 auto; }
.pay-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.back-btn { height: 40px; padding: 0 16px; border-radius: 10px; border: 1px solid var(--mall-border); background: #1b1b23; color: var(--mall-text); }
.panel { padding: 20px; border-radius: 12px; border: 1px solid #343444; background: #191921; }
.msg { margin: 0 0 12px; color: #86efac; }
.msg.error { color: #f87171; }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px 16px; color: var(--mall-text); }
.detail-grid p { margin: 0; padding: 8px 10px; border-radius: 8px; background: #12121a; border: 1px solid #30303a; }
.detail-grid span { color: var(--mall-text-muted); margin-right: 6px; }
.field { margin-top: 14px; display: flex; flex-direction: column; gap: 6px; }
.input { height: 40px; border: 1px solid #3a3a44; border-radius: 8px; background: #111117; color: var(--mall-text); padding: 0 12px; }
.actions { margin-top: 14px; }
.pay-btn { height: 42px; border: 0; border-radius: 999px; padding: 0 20px; background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange)); color: var(--mall-black); font-weight: 700; }
</style>
