<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { register, sendRegisterCaptcha } from '@/api/systemAuth'
import MallLanguageDropdown from '@/components/MallLanguageDropdown.vue'

const username = ref('')
const password = ref('')
const nickname = ref('')
const userType = ref('4')
const phone = ref('')
const verifyCode = ref('')
const submitting = ref(false)
const captchaLoading = ref(false)
const countdown = ref(0)
const errorMsg = ref('')
const infoMsg = ref('')
const registerSuccess = ref(false)

const userTypes = [
  { value: '1', label: '超级管理员' },
  { value: '2', label: '管理员' },
  { value: '3', label: '商家' },
  { value: '4', label: '普通用户' },
  { value: '5', label: '审核员' },
]

let timer
function startCooldown(sec) {
  countdown.value = sec
  clearInterval(timer)
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function onSendCaptcha() {
  errorMsg.value = ''
  infoMsg.value = ''
  if (!/^1\d{10}$/.test(phone.value.trim())) {
    errorMsg.value = '请输入 11 位中国大陆手机号'
    return
  }
  captchaLoading.value = true
  try {
    const { data } = await sendRegisterCaptcha({ phone: phone.value.trim() })
    if (data?.code === 200) {
      infoMsg.value =
        data?.data?.debugCode != null
          ? `验证码已生成（调试）：${data.data.debugCode}`
          : '验证码已发送（若未接短信，请查看后端日志或打开 debug-return）'
      startCooldown(60)
      return
    }
    errorMsg.value = data?.message || '发送失败'
    if (data?.message === 'register_captcha_rate_limit') {
      startCooldown(60)
    }
  } catch {
    errorMsg.value = '网络异常，请确认 gl-system 已启动'
  } finally {
    captchaLoading.value = false
  }
}

async function onSubmit() {
  errorMsg.value = ''
  infoMsg.value = ''
  registerSuccess.value = false
  submitting.value = true
  try {
    const { data } = await register({
      username: username.value.trim(),
      password: password.value,
      nickname: nickname.value.trim() || undefined,
      userType: userType.value,
      phone: phone.value.trim(),
      verifyCode: verifyCode.value.trim(),
    })
    if (data?.code === 200) {
      const msg = typeof data.data === 'string' ? data.data : data.message
      infoMsg.value = msg || '提交成功'
      registerSuccess.value = true
      return
    }
    errorMsg.value = data?.message || '注册失败'
  } catch {
    errorMsg.value = '网络异常，请确认 gl-system 已启动'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-topbar">
        <MallLanguageDropdown />
      </div>
      <RouterLink to="/" class="brand">
        <span class="logo-mark">GM</span>
        <span class="logo-text">Global<span class="accent">Mall</span></span>
      </RouterLink>
      <h1 class="title">注册申请</h1>
      <p class="hint">提交后进入管理端待审核；需先获取短信验证码（本地可调 app.register.captcha.debug-return）。</p>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="label">用户名</span>
          <input v-model="username" type="text" autocomplete="username" required class="input" />
        </label>
        <label class="field">
          <span class="label">密码</span>
          <input v-model="password" type="password" autocomplete="new-password" required class="input" />
        </label>
        <label class="field">
          <span class="label">昵称（可选）</span>
          <input v-model="nickname" type="text" autocomplete="nickname" class="input" />
        </label>
        <label class="field">
          <span class="label">申请身份</span>
          <select v-model="userType" class="input select" required>
            <option v-for="u in userTypes" :key="u.value" :value="u.value">{{ u.label }}</option>
          </select>
        </label>
        <label class="field">
          <span class="label">手机号</span>
          <input v-model="phone" type="tel" autocomplete="tel" maxlength="11" required class="input" />
        </label>
        <div class="field captcha-row">
          <label class="grow">
            <span class="label">验证码</span>
            <input v-model="verifyCode" type="text" maxlength="6" inputmode="numeric" required class="input" />
          </label>
          <button
            type="button"
            class="captcha-btn"
            :disabled="captchaLoading || countdown > 0"
            @click="onSendCaptcha"
          >
            {{ countdown > 0 ? `${countdown}s` : captchaLoading ? '发送中…' : '获取验证码' }}
          </button>
        </div>

        <p v-if="errorMsg" class="error" role="alert">{{ errorMsg }}</p>
        <p v-if="infoMsg" class="info" role="status">{{ infoMsg }}</p>

        <div v-if="registerSuccess" class="success-actions">
          <RouterLink to="/" class="success-btn secondary">回到首页</RouterLink>
          <RouterLink to="/login" class="success-btn primary">去登录</RouterLink>
        </div>

        <button type="submit" class="submit" :disabled="submitting">
          {{ submitting ? '提交中…' : '提交注册申请' }}
        </button>
      </form>

      <p class="footer-link">
        已有账号？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background: radial-gradient(ellipse 80% 60% at 50% 0%, rgba(255, 138, 0, 0.12), transparent 55%),
    linear-gradient(180deg, #0a0a0b 0%, #0d0d10 100%);
}

.auth-card {
  width: 100%;
  max-width: 440px;
  padding: 32px 28px 28px;
  background: var(--mall-surface-elevated);
  border: 1px solid var(--mall-border);
  border-radius: 12px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.45);
}

.auth-topbar {
  display: flex;
  justify-content: flex-end;
  margin: -8px -6px 6px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  text-decoration: none;
}

.logo-mark {
  width: 40px;
  height: 40px;
  background: linear-gradient(145deg, var(--mall-orange) 0%, var(--mall-orange-dim) 100%);
  color: var(--mall-black);
  font-weight: 800;
  font-size: 0.8rem;
  letter-spacing: -0.03em;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.logo-text {
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--mall-text);
  letter-spacing: -0.02em;
}

.accent {
  color: var(--mall-orange);
}

.title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--mall-text);
}

.hint {
  margin: 0 0 24px;
  font-size: 0.85rem;
  color: var(--mall-text-muted);
  line-height: 1.45;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.captcha-row {
  flex-direction: row;
  align-items: flex-end;
  gap: 10px;
}

.grow {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--mall-text-muted);
}

.input {
  height: 44px;
  padding: 0 14px;
  border: 1px solid var(--mall-border);
  border-radius: 6px;
  background: var(--mall-surface);
  color: var(--mall-text);
  font-size: 0.95rem;
}

.input:focus {
  outline: none;
  border-color: var(--mall-orange);
  box-shadow: 0 0 0 2px var(--mall-orange-glow);
}

.select {
  cursor: pointer;
  padding-right: 42px;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' fill='none' stroke='%23f4f4f5' stroke-width='1.8' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M2.5 4.5L6 8l3.5-3.5'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-size: 12px 12px;
  background-position: right 16px center;
}

.captcha-btn {
  flex-shrink: 0;
  height: 44px;
  padding: 0 14px;
  border: 1px solid var(--mall-border);
  border-radius: 6px;
  background: var(--mall-surface);
  color: var(--mall-orange-bright);
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  white-space: nowrap;
}

.captcha-btn:hover:not(:disabled) {
  border-color: var(--mall-orange);
  background: rgba(255, 138, 0, 0.08);
}

.captcha-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error {
  margin: 0;
  font-size: 0.85rem;
  color: #f87171;
}

.info {
  margin: 0;
  font-size: 0.85rem;
  color: #86efac;
}

.success-actions {
  display: flex;
  gap: 10px;
  margin-top: 2px;
}

.success-btn {
  flex: 1;
  height: 40px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.86rem;
  font-weight: 700;
  text-decoration: none;
}

.success-btn.primary {
  color: var(--mall-black);
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
}

.success-btn.secondary {
  color: var(--mall-text);
  border: 1px solid var(--mall-border);
  background: var(--mall-surface);
}

.submit {
  margin-top: 8px;
  height: 46px;
  border: none;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--mall-black);
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
  cursor: pointer;
}

.submit:hover:not(:disabled) {
  filter: brightness(1.06);
}

.submit:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.footer-link {
  margin: 22px 0 0;
  text-align: center;
  font-size: 0.88rem;
  color: var(--mall-text-muted);
}

.footer-link a {
  color: var(--mall-orange);
  font-weight: 600;
  text-decoration: none;
}

.footer-link a:hover {
  text-decoration: underline;
}
</style>
