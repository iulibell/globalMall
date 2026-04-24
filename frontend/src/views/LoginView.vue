<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { login } from '@/api/systemAuth'

const router = useRouter()

const username = ref('')
const password = ref('')
const requiredRoleKey = ref('user')
const submitting = ref(false)
const errorMsg = ref('')

const roles = [
  { value: 'super', label: '超级管理员' },
  { value: 'manager', label: '管理员' },
  { value: 'merchant', label: '商家' },
  { value: 'user', label: '普通用户' },
  { value: 'reviewer', label: '审核员' },
]

async function onSubmit() {
  errorMsg.value = ''
  submitting.value = true
  try {
    const { ok, data } = await login({
      username: username.value.trim(),
      password: password.value,
      requiredRoleKey: requiredRoleKey.value,
    })
    if (data?.code === 200 && data?.data?.token) {
      localStorage.setItem('satoken', data.data.token)
      if (data.data.tokenHead) {
        localStorage.setItem('tokenHead', data.data.tokenHead)
      }
      localStorage.setItem('role', data.data.role || requiredRoleKey.value)
      localStorage.setItem('username', data.data.username || username.value.trim())
      localStorage.setItem('nickname', data.data.nickname || '')
      if (data.data.userId) {
        localStorage.setItem('userId', data.data.userId)
      }
      if (data.data.phone != null && String(data.data.phone).trim() !== '') {
        localStorage.setItem('phone', String(data.data.phone).trim())
      } else {
        localStorage.removeItem('phone')
      }
      if (data.data.city != null && String(data.data.city).trim() !== '') {
        localStorage.setItem('city', String(data.data.city).trim())
      } else {
        localStorage.removeItem('city')
      }
      await router.push({ name: 'home' })
      return
    }
    errorMsg.value = data?.message || (ok ? '登录失败' : `请求失败 (${data?.code ?? '?'})`)
  } catch (e) {
    errorMsg.value = '网络异常，请确认 gl-system 已启动（默认 8205）'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <RouterLink to="/" class="brand">
        <span class="logo-mark">GM</span>
        <span class="logo-text">Global<span class="accent">Mall</span></span>
      </RouterLink>
      <h1 class="title">登录</h1>
      <p class="hint">使用 gl-system 账号；身份需与库中 userType 一致。</p>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="label">用户名</span>
          <input v-model="username" type="text" autocomplete="username" required class="input" />
        </label>
        <label class="field">
          <span class="label">密码</span>
          <input v-model="password" type="password" autocomplete="current-password" required class="input" />
        </label>
        <label class="field">
          <span class="label">登录身份</span>
          <select v-model="requiredRoleKey" class="input select" required>
            <option v-for="r in roles" :key="r.value" :value="r.value">{{ r.label }}（{{ r.value }}）</option>
          </select>
        </label>

        <p v-if="errorMsg" class="error" role="alert">{{ errorMsg }}</p>

        <button type="submit" class="submit" :disabled="submitting">
          {{ submitting ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="footer-link">
        还没有账号？
        <RouterLink to="/register">去注册</RouterLink>
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
  max-width: 420px;
  padding: 32px 28px 28px;
  background: var(--mall-surface-elevated);
  border: 1px solid var(--mall-border);
  border-radius: 12px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.45);
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
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
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

.error {
  margin: 0;
  font-size: 0.85rem;
  color: #f87171;
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
