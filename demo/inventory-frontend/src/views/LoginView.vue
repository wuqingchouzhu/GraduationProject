<template>
  <div class="login-wrapper" @keyup.enter="handleLogin">
    <div class="login-card">
      <!-- Logo / Branding -->
      <div class="logo-section">
        <h1 class="logo-title">进销存管理系统</h1>
        <p class="logo-subtitle">Inventory Management System</p>
      </div>

      <!-- Login Form -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item class="btn-item">
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loginLoading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Footer -->
      <p class="login-footer">© 2024 进销存管理系统</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref(null)
const loginLoading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度为 2-32 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 64, message: '密码长度至少为 4 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loginLoading.value = true
  try {
    await auth.login({
      username: form.username,
      password: form.password
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (err) {
    // 拦截器已弹窗提示的异常（BizException / 网络错误），无需重复提示
    // 此处仅兜底非标准异常
    if (!err?.response?.data?.message && err?.message !== '网络错误') {
      ElMessage.error(err?.message || '登录失败，请检查用户名和密码')
    }
  } finally {
    loginLoading.value = false
  }
}
</script>

<style scoped>
/* ====== Full-screen Gradient Background ====== */
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

/* Decorative ambient glow behind the card */
.login-wrapper::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(15, 52, 96, 0.3) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

/* ====== Login Card ====== */
.login-card {
  width: 420px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 0 120px rgba(15, 52, 96, 0.15);
  padding: 40px;
  position: relative;
  z-index: 1;
  animation: cardEnter 0.6s ease-out;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ====== Logo Section ====== */
.logo-section {
  text-align: center;
  margin-bottom: 36px;
}

.logo-title {
  font-size: 26px;
  font-weight: 700;
  color: #0f3460;
  letter-spacing: 4px;
  margin: 0 0 8px 0;
}

.logo-subtitle {
  font-size: 13px;
  font-weight: 400;
  color: #8892b0;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  margin: 0;
}

/* ====== Form ====== */
:deep(.el-form-item) {
  margin-bottom: 22px;
}

/* Custom label style */
:deep(.el-form-item__label) {
  font-size: 13px;
  color: #4a5568;
  font-weight: 500;
  padding-bottom: 4px;
}

/* Input styling */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e0 inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #0f3460 inset, 0 0 0 3px rgba(15, 52, 96, 0.1);
}

/* Input prefix icon color */
:deep(.el-input__prefix .el-icon) {
  color: #a0aec0;
}

:deep(.el-input.is-focus .el-input__prefix .el-icon) {
  color: #0f3460;
}

/* ====== Button ====== */
.btn-item {
  margin-bottom: 16px !important;
}

.btn-item :deep(.el-form-item__content) {
  justify-content: stretch;
}

.login-btn {
  width: 100%;
  height: 46px;
  background: #0f3460;
  border-color: #0f3460;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: #1a4a7a;
  border-color: #1a4a7a;
  box-shadow: 0 6px 20px rgba(15, 52, 96, 0.4);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.is-loading {
  letter-spacing: 4px;
}

/* ====== Footer ====== */
.login-footer {
  text-align: center;
  font-size: 12px;
  color: #cbd5e0;
  margin: 8px 0 0 0;
  letter-spacing: 0.5px;
}
</style>
