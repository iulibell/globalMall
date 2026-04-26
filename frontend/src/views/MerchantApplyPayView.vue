<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { merchantCancelGoodsApply, merchantPayForInbound } from '@/api/portal'

const router = useRouter()
const route = useRoute()

const applying = ref(false)
const canceling = ref(false)
const showCancelConfirm = ref(false)
const tip = ref('')
const error = ref('')
const apply = ref(null)

const token = computed(() => localStorage.getItem('satoken') || '')
const role = computed(() => localStorage.getItem('role') || '')
const isMerchant = computed(() => role.value === 'merchant' && Boolean(token.value))

const payable = computed(() => {
  const row = apply.value
  if (!row) return false
  return Number(row.mallStatus) === 1 && Number(row.logisticStatus) === 1 && Number(row.isPay) === 0
})

function statusText(v) {
  const s = Number(v)
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === 2) return '已退回'
  if (s === 3) return '已取消'
  return '未知'
}

function payStatusText(v) {
  const s = Number(v)
  if (s === 0) return '待支付'
  if (s === 1) return '已支付'
  if (s === 2) return '超时未支付'
  return '未知'
}

function loadApplyFromCache() {
  const applyId = String(route.query.applyId || '').trim()
  const raw = sessionStorage.getItem('merchant_apply_pay_row')
  if (!applyId || !raw) return
  try {
    const row = JSON.parse(raw)
    if (String(row?.applyId || '').trim() === applyId) {
      apply.value = row
    }
  } catch {
    /* ignore */
  }
}

async function submitPay() {
  const applyId = String(apply.value?.applyId || '').trim()
  if (!applyId) {
    error.value = '缺少申请单号，请返回申请列表重试'
    return
  }
  applying.value = true
  tip.value = ''
  error.value = ''
  try {
    const { ok, data } = await merchantPayForInbound(applyId)
    if (!ok || data?.code !== 200) {
      error.value = data?.message || '支付失败'
      return
    }
    tip.value = typeof data?.data === 'string' ? data.data : data?.message || '支付成功'
    if (apply.value) {
      apply.value = { ...apply.value, isPay: 1 }
      sessionStorage.setItem('merchant_apply_pay_row', JSON.stringify(apply.value))
    }
    await router.push({ name: 'profile' })
  } catch {
    error.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    applying.value = false
  }
}

async function submitCancel() {
  const applyId = String(apply.value?.applyId || '').trim()
  if (!applyId) {
    error.value = '缺少申请单号，请返回申请列表重试'
    return
  }
  canceling.value = true
  tip.value = ''
  error.value = ''
  try {
    const { ok, data } = await merchantCancelGoodsApply(applyId)
    if (!ok || data?.code !== 200) {
      error.value = data?.message || '取消失败'
      return
    }
    tip.value = typeof data?.data === 'string' ? data.data : data?.message || '申请已取消'
    if (apply.value) {
      apply.value = { ...apply.value, mallStatus: 3 }
      sessionStorage.setItem('merchant_apply_pay_row', JSON.stringify(apply.value))
    }
  } catch {
    error.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    canceling.value = false
  }
}

function openCancelConfirm() {
  showCancelConfirm.value = true
}

function closeCancelConfirm() {
  if (canceling.value) return
  showCancelConfirm.value = false
}

async function confirmCancelApply() {
  await submitCancel()
  if (!error.value) {
    showCancelConfirm.value = false
  }
}

onMounted(() => {
  loadApplyFromCache()
})
</script>

<template>
  <div class="pay-page">
    <section v-if="!token" class="forbidden-card">
      <h1>请先登录</h1>
      <p>支付申请单需要商家账号登录。</p>
      <div class="action-row">
        <button type="button" class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
        <button type="button" class="btn primary" @click="router.push({ name: 'login' })">去登录</button>
      </div>
    </section>

    <section v-else-if="!isMerchant" class="forbidden-card">
      <h1>仅限商家</h1>
      <p>当前账号不是商家角色，无法支付申请单。</p>
      <div class="action-row">
        <button type="button" class="btn ghost" @click="router.push({ name: 'profile' })">返回个人中心</button>
      </div>
    </section>

    <section v-else-if="!apply" class="forbidden-card">
      <h1>缺少申请单信息</h1>
      <p>请从“个人中心 - 申请列表”点击“前往支付”进入本页。</p>
      <div class="action-row">
        <button type="button" class="btn primary" @click="router.push({ name: 'profile' })">返回个人中心</button>
      </div>
    </section>

    <main v-else class="pay-main">
      <header class="pay-head">
        <h1>申请单支付</h1>
        <button type="button" class="back-btn" @click="router.push({ name: 'profile' })">返回申请列表</button>
      </header>
      <section class="panel">
        <p v-if="tip" class="msg">{{ tip }}</p>
        <p v-if="error" class="msg error">{{ error }}</p>

        <div class="detail-grid">
          <p><span>申请ID：</span>{{ apply.applyId }}</p>
          <p><span>商品名称：</span>{{ apply.skuName }}</p>
          <p><span>价格：</span>{{ apply.price }}</p>
          <p><span>申请数量：</span>{{ apply.applyQuantity ?? '-' }}</p>
          <p><span>商家城市：</span>{{ apply.city }}</p>
          <p><span>仓库城市：</span>{{ apply.warehouseCity }}</p>
          <p><span>Mall审核：</span>{{ statusText(apply.mallStatus) }}</p>
          <p><span>Logistic审核：</span>{{ statusText(apply.logisticStatus) }}</p>
          <p><span>支付状态：</span>{{ payStatusText(apply.isPay) }}</p>
          <p><span>费用：</span>{{ apply.fee ?? '-' }}</p>
        </div>

        <div class="actions">
          <button type="button" class="pay-btn" :disabled="applying || canceling || !payable" @click="submitPay">
            <span class="pay-btn-ico" aria-hidden="true">¥</span>
            <span class="pay-btn-text">
              <span class="pay-btn-title">{{ applying ? '支付中…' : '确认支付' }}</span>
              <span class="pay-btn-sub">{{ payable ? '立即完成上架费用支付' : '当前状态不可支付' }}</span>
            </span>
          </button>
          <button type="button" class="cancel-btn" :disabled="applying || canceling || !payable" @click="openCancelConfirm">
            {{ canceling ? '取消中…' : '取消申请' }}
          </button>
        </div>
      </section>
    </main>

    <div v-if="showCancelConfirm" class="confirm-mask" @click.self="closeCancelConfirm">
      <section class="confirm-dialog" role="dialog" aria-modal="true" aria-label="取消申请确认">
        <h3>确认取消申请？</h3>
        <p>取消后该申请单将变为“已取消”，且不可再次支付。</p>
        <div class="confirm-actions">
          <button type="button" class="confirm-btn ghost" :disabled="canceling" @click="closeCancelConfirm">再想想</button>
          <button type="button" class="confirm-btn danger" :disabled="canceling" @click="confirmCancelApply">
            {{ canceling ? '取消中…' : '确认取消' }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.pay-page { min-height: 100vh; background: radial-gradient(circle at 20% -10%, rgba(255, 138, 0, 0.18), transparent 42%), #09090b; padding: 24px; }
.forbidden-card { max-width: 720px; margin: 120px auto 0; background: #16161b; border: 1px solid var(--mall-border); border-radius: 14px; padding: 28px; }
.action-row { display: flex; gap: 12px; margin-top: 20px; }
.pay-main { max-width: 920px; margin: 0 auto; }
.pay-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.back-btn {
  height: 40px;
  padding: 0 16px;
  border-radius: 10px;
  border: 1px solid rgba(126, 152, 186, 0.45);
  background: linear-gradient(180deg, rgba(34, 45, 66, 0.78), rgba(21, 28, 43, 0.9));
  color: #d9e6f7;
  font-size: 0.88rem;
  font-weight: 700;
  letter-spacing: 0.01em;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.28);
  transition: transform 0.12s ease, box-shadow 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}
.back-btn:hover {
  transform: translateY(-1px);
  border-color: rgba(163, 191, 228, 0.66);
  color: #f3f8ff;
  box-shadow: 0 10px 24px rgba(20, 35, 58, 0.45);
}
.back-btn:active {
  transform: translateY(0);
}
.panel { padding: 20px; border-radius: 12px; border: 1px solid #343444; background: linear-gradient(180deg, #1b1b23 0%, #191921 100%); }
.msg { margin: 0 0 12px; color: #86efac; }
.msg.error { color: #f87171; }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px 16px; color: var(--mall-text); }
.detail-grid p { margin: 0; padding: 8px 10px; border-radius: 8px; background: #12121a; border: 1px solid #30303a; }
.detail-grid span { color: var(--mall-text-muted); margin-right: 6px; }
.actions { margin-top: 16px; }
.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.pay-btn {
  height: 54px;
  min-width: 260px;
  padding: 0 18px;
  border-radius: 12px;
  border: 1px solid rgba(255, 167, 38, 0.85);
  background:
    radial-gradient(circle at 18% 20%, rgba(255, 235, 180, 0.45), transparent 35%),
    linear-gradient(180deg, #ffc977 0%, #ff9a1a 54%, #ef7f00 100%);
  color: #1c1c22;
  font-weight: 800;
  font-size: 0.95rem;
  cursor: pointer;
  box-shadow: 0 12px 28px rgba(255, 138, 0, 0.38), inset 0 -1px 0 rgba(0, 0, 0, 0.16);
  transition: transform 0.12s ease, box-shadow 0.22s ease, filter 0.2s ease, opacity 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.pay-btn:hover:not(:disabled) {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 0 14px 30px rgba(255, 138, 0, 0.5), 0 0 0 3px rgba(255, 167, 38, 0.18);
  filter: saturate(1.08);
}
.pay-btn:active:not(:disabled) {
  transform: translateY(0);
}
.pay-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: inset 0 -1px 0 rgba(0, 0, 0, 0.08);
}
.pay-btn-ico {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.34);
  border: 1px solid rgba(255, 255, 255, 0.45);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  font-weight: 900;
}
.pay-btn-text {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.05;
}
.pay-btn-title {
  font-size: 0.95rem;
}
.pay-btn-sub {
  margin-top: 2px;
  font-size: 0.68rem;
  font-weight: 700;
  color: rgba(28, 28, 34, 0.78);
}
.cancel-btn {
  height: 54px;
  min-width: 120px;
  padding: 0 18px;
  border-radius: 12px;
  border: 1px solid rgba(248, 113, 113, 0.65);
  background: linear-gradient(180deg, rgba(248, 113, 113, 0.22), rgba(127, 29, 29, 0.35));
  color: #fecaca;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}
.cancel-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(127, 29, 29, 0.35);
}
.cancel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.confirm-mask {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.68);
  backdrop-filter: blur(3px);
  display: grid;
  place-items: center;
  z-index: 80;
}
.confirm-dialog {
  width: min(92vw, 420px);
  border-radius: 14px;
  padding: 18px 18px 16px;
  border: 1px solid rgba(71, 85, 105, 0.55);
  background: linear-gradient(180deg, #151a25 0%, #0f1320 100%);
  box-shadow: 0 20px 48px rgba(2, 6, 23, 0.52);
}
.confirm-dialog h3 {
  margin: 0;
  color: #f8fafc;
  font-size: 1.06rem;
}
.confirm-dialog p {
  margin: 10px 0 0;
  color: #cbd5e1;
  font-size: 0.9rem;
  line-height: 1.45;
}
.confirm-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.confirm-btn {
  height: 38px;
  padding: 0 14px;
  border-radius: 9px;
  font-size: 0.86rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.12s ease, opacity 0.2s ease, box-shadow 0.2s ease;
}
.confirm-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.confirm-btn.ghost {
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: rgba(30, 41, 59, 0.55);
  color: #e2e8f0;
}
.confirm-btn.ghost:hover:not(:disabled) {
  transform: translateY(-1px);
}
.confirm-btn.danger {
  border: 1px solid rgba(248, 113, 113, 0.7);
  background: linear-gradient(180deg, rgba(248, 113, 113, 0.3), rgba(185, 28, 28, 0.42));
  color: #fee2e2;
}
.confirm-btn.danger:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(127, 29, 29, 0.34);
}
</style>
