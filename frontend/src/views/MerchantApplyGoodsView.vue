<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchAvailableWarehouses, fetchGoodsTypes, merchantApplyGoods } from '@/api/portal'

const router = useRouter()
const route = useRoute()
const pictureInputRef = ref(null)
const pictureFileName = ref('')

/** 与登录/个人中心保存同步，避免 localStorage 非响应式导致回个人中心改资料后不刷新 */
const merchantPhone = ref('')
const merchantCity = ref('')

function syncMerchantAccountFromStorage() {
  merchantPhone.value = (localStorage.getItem('phone') || '').trim()
  merchantCity.value = (localStorage.getItem('city') || '').trim()
}

const token = computed(() => localStorage.getItem('satoken') || '')
const role = computed(() => localStorage.getItem('role') || '')
const userId = computed(() => (localStorage.getItem('userId') || '').trim())

const isMerchant = computed(() => role.value === 'merchant' && Boolean(token.value))

const profileIncomplete = computed(
  () => isMerchant.value && !merchantCity.value,
)

const form = ref({
  typeId: '',
  skuName: '',
  price: '',
  applyQuantity: '',
  picture: '',
  /** 选中的仓库 id（来自 getAvailableWarehouse），提交时映射为 warehouseCity */
  warehouseId: '',
  description: '',
})
const submitting = ref(false)
const tip = ref('')
const error = ref('')
const goodsTypes = ref([])
const typesLoading = ref(false)
const warehouses = ref([])
const warehousesLoading = ref(false)

async function loadGoodsTypes() {
  if (!isMerchant.value || !token.value) {
    goodsTypes.value = []
    return
  }
  typesLoading.value = true
  try {
    const { ok, data } = await fetchGoodsTypes({ pageNum: 1, pageSize: 200 })
    if (ok && data?.code === 200 && Array.isArray(data.data)) {
      goodsTypes.value = data.data
    } else {
      goodsTypes.value = []
    }
  } catch {
    goodsTypes.value = []
  } finally {
    typesLoading.value = false
  }
}

async function loadWarehouses() {
  if (!isMerchant.value || !token.value) {
    warehouses.value = []
    return
  }
  warehousesLoading.value = true
  try {
    const { ok, data } = await fetchAvailableWarehouses({ pageNum: 1, pageSize: 200 })
    if (ok && data?.code === 200 && Array.isArray(data.data)) {
      warehouses.value = data.data
    } else {
      warehouses.value = []
    }
  } catch {
    warehouses.value = []
  } finally {
    warehousesLoading.value = false
  }
}

function warehouseLabel(w) {
  const city = (w?.city || '').trim()
  const addr = (w?.address || '').trim()
  const id = w?.warehouseId != null ? String(w.warehouseId) : ''
  if (city && addr) return `${city} — ${addr}`
  if (city) return city
  if (addr) return addr
  return id ? `仓库 #${id}` : '—'
}

function warehouseCityForSubmit() {
  const id = (form.value.warehouseId || '').trim()
  if (!id) return ''
  const w = warehouses.value.find((x) => String(x?.warehouseId) === id)
  if (!w) return ''
  const city = (w.city || '').trim()
  if (city) return city
  const country = (w.country || '').trim()
  if (country) return country
  const addr = (w.address || '').trim()
  return addr.slice(0, 20) || ''
}

function readPictureFile(file, inputEl) {
  if (!file) {
    pictureFileName.value = ''
    return
  }
  if (!file.type.startsWith('image/')) {
    error.value = '请选择图片文件（PNG、JPG、WebP 等）'
    return
  }
  error.value = ''
  pictureFileName.value = file.name
  const reader = new FileReader()
  reader.onload = () => {
    form.value = { ...form.value, picture: reader.result }
    if (inputEl) inputEl.value = ''
  }
  reader.readAsDataURL(file)
}

function onPictureFile(ev) {
  const file = ev.target?.files?.[0]
  readPictureFile(file, ev.target)
}

function onPictureDrop(ev) {
  const file = ev.dataTransfer?.files?.[0]
  readPictureFile(file, pictureInputRef.value)
}

function triggerPicturePick() {
  pictureInputRef.value?.click()
}

onMounted(() => {
  syncMerchantAccountFromStorage()
  loadGoodsTypes()
  loadWarehouses()
})

watch(
  () => route.fullPath,
  () => {
    if (route.name === 'merchant-apply-goods') {
      syncMerchantAccountFromStorage()
      loadGoodsTypes()
      loadWarehouses()
    }
  },
)

watch([isMerchant, token], () => {
  loadGoodsTypes()
  loadWarehouses()
})

async function submit() {
  const uid = userId.value
  if (!uid) {
    error.value = '缺少用户 ID，请重新登录后再试'
    return
  }
  syncMerchantAccountFromStorage()
  if (!merchantCity.value) {
    error.value = '请先在个人中心填写「所在城市」后再申请上架'
    return
  }
  if (!merchantPhone.value) {
    error.value = '请先在个人中心填写「手机号」后再申请上架'
    return
  }
  const typeIdNum = Number(form.value.typeId)
  const skuName = form.value.skuName.trim()
  const picture = form.value.picture.trim()
  const warehouseCity = warehouseCityForSubmit().trim()
  const priceNum = Number(form.value.price)
  const applyQuantityNum = Number(form.value.applyQuantity)
  if (
    !Number.isInteger(typeIdNum) ||
    typeIdNum < 1 ||
    !skuName ||
    !picture ||
    !(form.value.warehouseId || '').trim() ||
    !warehouseCity ||
    !Number.isFinite(priceNum) ||
    !Number.isInteger(applyQuantityNum) ||
    applyQuantityNum < 1
  ) {
    error.value =
      '请选择商品类型与入库仓库，并填写商品名称、价格、申请数量、商品图片（申请数量需为正整数）'
    return
  }
  submitting.value = true
  tip.value = ''
  error.value = ''
  try {
    const payload = {
      userId: uid,
      merchantId: uid,
      typeId: typeIdNum,
      skuName,
      merchantPhone: merchantPhone.value,
      price: priceNum,
      picture,
      city: merchantCity.value,
      warehouseCity,
      applyQuantity: applyQuantityNum,
      isPay: 0,
      description: form.value.description.trim() || undefined,
    }
    const { ok, data } = await merchantApplyGoods(payload)
    if (!ok || data?.code !== 200) {
      error.value = data?.message || '申请失败'
      return
    }
    tip.value = typeof data?.data === 'string' ? data.data : data?.message || '申请成功'
    error.value = ''
    form.value = {
      typeId: '',
      skuName: '',
      price: '',
      applyQuantity: '',
      picture: '',
      warehouseId: '',
      description: '',
    }
    pictureFileName.value = ''
  } catch {
    error.value = '网络异常，请确认 mall-portal 已启动'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="apply-page">
    <section v-if="!token" class="forbidden-card">
      <h1>请先登录</h1>
      <p>申请上架需要商家账号登录。</p>
      <div class="action-row">
        <button type="button" class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
        <button type="button" class="btn primary" @click="router.push({ name: 'login' })">去登录</button>
      </div>
    </section>

    <section v-else-if="!isMerchant" class="forbidden-card">
      <h1>仅限商家</h1>
      <p>当前账号不是商家角色，无法使用申请上架。</p>
      <div class="action-row">
        <button type="button" class="btn ghost" @click="router.push({ name: 'profile' })">返回个人中心</button>
        <button type="button" class="btn primary" @click="router.push({ name: 'home' })">返回首页</button>
      </div>
    </section>

    <main v-else class="apply-main">
      <header class="apply-head">
        <div>
          <p class="eyebrow">MERCHANT</p>
          <h1>申请上架</h1>
          <p class="sub">提交后进入审核流程。商品图可上传本地图片（data URL）或填写已有文件名。</p>
        </div>
        <div class="head-actions">
          <button type="button" class="btn ghost" @click="router.push({ name: 'profile' })">返回个人中心</button>
          <button type="button" class="btn ghost" @click="router.push({ name: 'home' })">返回首页</button>
        </div>
      </header>

      <section class="panel">
        <div v-if="profileIncomplete" class="profile-warn" role="alert">
          <p class="profile-warn-title">请先完善资料</p>
          <p class="profile-warn-text">申请上架需要使用账号中的「所在城市」。当前未填写，请先到个人中心保存「所在城市」（与手机号一并维护）。</p>
          <button type="button" class="btn primary" @click="router.push({ name: 'profile' })">去个人中心完善信息</button>
        </div>

        <div v-else class="account-strip">
          <div class="account-item">
            <span class="account-label">商家手机号</span>
            <span class="account-value">{{ merchantPhone || '—' }}</span>
          </div>
          <div class="account-item">
            <span class="account-label">商家所在城市</span>
            <span class="account-value">{{ merchantCity || '—' }}</span>
          </div>
        </div>

        <p v-if="tip" class="msg">{{ tip }}</p>
        <p v-if="error" class="msg error">{{ error }}</p>
        <form class="apply-form" @submit.prevent="submit">
          <label class="field field-full">
            <span>商品类型</span>
            <select v-model="form.typeId" class="input select-input" required>
              <option value="" disabled>请选择类型</option>
              <option v-for="t in goodsTypes" :key="t.typeId" :value="String(t.typeId)">
                {{ t.typeName }}
              </option>
            </select>
            <p v-if="typesLoading" class="field-hint">正在加载类型列表…</p>
            <p v-else-if="isMerchant && goodsTypes.length === 0" class="field-hint warn">
              暂无可用类型，请联系管理员在后台维护「商品类型」后再申请。
            </p>
          </label>
          <label class="field">
            <span>商品名称</span>
            <input v-model="form.skuName" type="text" class="input" autocomplete="off" />
          </label>
          <label class="field">
            <span>价格（元）</span>
            <input v-model="form.price" type="number" step="0.01" min="0" class="input" />
          </label>
          <label class="field">
            <span>申请数量</span>
            <input v-model="form.applyQuantity" type="number" step="1" min="1" class="input" />
          </label>
          <label class="field field-full">
            <span>入库仓库</span>
            <select v-model="form.warehouseId" class="input select-input" required>
              <option value="" disabled>请选择仓库（来自物流系统）</option>
              <option v-for="w in warehouses" :key="w.warehouseId" :value="String(w.warehouseId)">
                {{ warehouseLabel(w) }}
              </option>
            </select>
            <p v-if="warehousesLoading" class="field-hint">正在加载可选仓库…</p>
            <p v-else-if="isMerchant && warehouses.length === 0" class="field-hint warn">
              暂无可选仓库，请确认 logi-wms 已启动且存在可用仓库。
            </p>
          </label>
          <label class="field field-full">
            <span>商品描述（选填）</span>
            <textarea v-model="form.description" class="textarea" rows="2" />
          </label>
          <label class="field field-full">
            <span>商品图片（data URL 或文件名）</span>
            <textarea v-model="form.picture" class="textarea" rows="3" placeholder="上传图片后将自动填入，或直接输入文件名" />
          </label>
          <div class="field field-full file-block">
            <span>本地图片</span>
            <div
              class="file-drop"
              role="button"
              tabindex="0"
              @click="triggerPicturePick"
              @keydown.enter.prevent="triggerPicturePick"
              @keydown.space.prevent="triggerPicturePick"
              @dragover.prevent
              @drop.prevent="onPictureDrop"
            >
              <input
                ref="pictureInputRef"
                type="file"
                class="file-input-native"
                accept="image/*"
                @change="onPictureFile"
              />
              <div class="file-drop-icon" aria-hidden="true" />
              <p class="file-drop-title">点击选择或拖拽图片到此处</p>
              <p class="file-drop-hint">PNG、JPG、WebP，单张不超过约 5MB（与后端限制一致）</p>
              <span class="file-pick-chip">选择图片</span>
            </div>
            <p v-if="pictureFileName" class="file-picked">
              <span class="file-picked-label">已选择</span>
              <span class="file-picked-name">{{ pictureFileName }}</span>
            </p>
          </div>
          <div class="actions field-full">
            <button type="submit" class="btn primary" :disabled="submitting || profileIncomplete">
              {{ submitting ? '提交中…' : '提交申请' }}
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<style scoped>
.apply-page {
  min-height: 100vh;
  background: radial-gradient(circle at 20% -10%, rgba(255, 138, 0, 0.18), transparent 42%), #09090b;
  padding: 24px;
}

.forbidden-card {
  max-width: 640px;
  margin: 80px auto 0;
  background: #16161b;
  border: 1px solid var(--mall-border);
  border-radius: 14px;
  padding: 28px;
}

.forbidden-card h1 {
  margin: 0 0 12px;
  color: var(--mall-text);
}

.forbidden-card p {
  margin: 0;
  color: var(--mall-text-muted);
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.apply-main {
  max-width: 880px;
  margin: 0 auto;
}

.apply-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(255, 167, 38, 0.65);
}

.apply-head h1 {
  margin: 0;
  font-size: 1.75rem;
  color: var(--mall-text);
}

.sub {
  margin: 10px 0 0;
  font-size: 0.88rem;
  color: var(--mall-text-muted);
  max-width: 520px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.panel {
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #343444;
  background: linear-gradient(180deg, #1b1b23 0%, #191921 100%);
}

.msg {
  margin: 0 0 12px;
  font-size: 0.88rem;
  color: #86efac;
}

.msg.error {
  color: #f87171;
}

.profile-warn {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid rgba(248, 113, 113, 0.45);
  background: rgba(248, 113, 113, 0.08);
}

.profile-warn-title {
  margin: 0 0 8px;
  font-size: 0.95rem;
  font-weight: 700;
  color: #fecaca;
}

.profile-warn-text {
  margin: 0 0 12px;
  font-size: 0.82rem;
  line-height: 1.5;
  color: var(--mall-text-muted);
}

.profile-warn .btn {
  margin-top: 4px;
}

.account-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #3a3a44;
  background: #12121a;
}

.account-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 140px;
}

.account-label {
  font-size: 0.72rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--mall-text-muted);
}

.account-value {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--mall-text);
}

.apply-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.8rem;
  color: var(--mall-text-muted);
}

.field-full {
  grid-column: 1 / -1;
}

.input {
  height: 40px;
  border-radius: 8px;
  border: 1px solid #3a3a44;
  background: #0f0f15;
  color: var(--mall-text);
  padding: 0 12px;
  font-size: 0.9rem;
}

.select-input {
  cursor: pointer;
  appearance: auto;
}

.field-hint {
  margin: 4px 0 0;
  font-size: 0.75rem;
  color: var(--mall-text-muted);
}

.field-hint.warn {
  color: #fbbf24;
}

.textarea {
  width: 100%;
  min-height: 72px;
  border-radius: 8px;
  border: 1px solid #3a3a44;
  background: #0f0f15;
  color: var(--mall-text);
  padding: 10px 12px;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
}

.file-block {
  gap: 10px;
}

.file-input-native {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

.file-drop {
  position: relative;
  border-radius: 12px;
  border: 1px dashed rgba(255, 138, 0, 0.45);
  background: linear-gradient(165deg, rgba(255, 138, 0, 0.08) 0%, #12121a 55%, #0f0f14 100%);
  padding: 22px 20px 20px;
  text-align: center;
  cursor: pointer;
  transition:
    border-color 0.2s,
    box-shadow 0.2s,
    background 0.2s;
}

.file-drop:hover,
.file-drop:focus-within {
  border-color: rgba(255, 167, 38, 0.85);
  box-shadow: 0 0 0 1px rgba(255, 138, 0, 0.25), 0 12px 32px rgba(0, 0, 0, 0.35);
}

.file-drop:focus-visible {
  outline: 2px solid var(--mall-orange-bright);
  outline-offset: 2px;
}

.file-drop-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  border-radius: 14px;
  border: 1px solid rgba(255, 138, 0, 0.45);
  background:
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ffb347' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='3' y='6' width='18' height='12' rx='2'/%3E%3Cpath d='M8 6V5h8v1'/%3E%3Ccircle cx='12' cy='12' r='3'/%3E%3C/svg%3E")
      center / 28px no-repeat,
    linear-gradient(145deg, rgba(255, 138, 0, 0.22), rgba(18, 18, 24, 0.96));
}

.file-drop-title {
  margin: 0 0 6px;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--mall-text);
}

.file-drop-hint {
  margin: 0 0 14px;
  font-size: 0.78rem;
  color: var(--mall-text-muted);
  line-height: 1.45;
}

.file-pick-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 18px;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 700;
  color: #18181b;
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
  border: none;
  pointer-events: none;
  box-shadow: 0 4px 14px rgba(255, 138, 0, 0.35);
}

.file-picked {
  margin: 10px 0 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 0.82rem;
}

.file-picked-label {
  color: var(--mall-text-muted);
}

.file-picked-name {
  padding: 4px 10px;
  border-radius: 8px;
  background: #1a1a22;
  border: 1px solid #3f3f4a;
  color: #fde68a;
  font-weight: 600;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions {
  margin-top: 4px;
}

.btn {
  border: none;
  border-radius: 999px;
  padding: 10px 20px;
  font-weight: 700;
  font-size: 0.88rem;
  cursor: pointer;
}

.btn.primary {
  color: #18181b;
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
}

.btn.primary:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn.ghost {
  color: var(--mall-text);
  background: transparent;
  border: 1px solid #3a3a44;
}

@media (max-width: 720px) {
  .apply-form {
    grid-template-columns: 1fr;
  }
}
</style>
