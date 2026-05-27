<template>
  <main class="login-page auth-page">
    <section class="login-hero" aria-label="应用介绍">
      <div class="hero-content">
        <div class="brand-chip">
          <span>决策回声</span>
        </div>
        <h1>把今天的想法，留给之后的自己看。</h1>
        <p>记录选择、理由和回看时间。结果出现后再复盘一次，让下一次判断更稳一点。</p>
      </div>

      <div class="daily-card" aria-label="使用方式">
        <div class="daily-card-header">
          <span>今天可以记录</span>
          <strong>3 件小事</strong>
        </div>
        <ul class="daily-list">
          <li>
            <span class="daily-dot"></span>
            <div>
              <strong>为什么这么选</strong>
              <p>留下当时最真实的判断依据。</p>
            </div>
          </li>
          <li>
            <span class="daily-dot"></span>
            <div>
              <strong>什么时候再看</strong>
              <p>给决定一个合适的回看日期。</p>
            </div>
          </li>
          <li>
            <span class="daily-dot"></span>
            <div>
              <strong>后来感觉如何</strong>
              <p>满意、一般或后悔，都值得被看见。</p>
            </div>
          </li>
        </ul>
      </div>
    </section>

    <section class="login-panel" aria-label="账号表单">
      <div class="panel-heading">
        <h2>{{ isRegisterMode ? '创建账号' : '欢迎回来' }}</h2>
        <p>{{ isRegisterMode ? '注册后会自动进入首页，马上开始记录你的第一个决定。' : '使用账号、密码和验证码登录系统。' }}</p>
      </div>

      <div class="auth-mode-switch" role="tablist" aria-label="登录注册切换">
        <button type="button" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
        <button type="button" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
      </div>

      <el-form
        v-if="mode === 'login'"
        ref="loginFormRef"
        class="auth-form"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        @submit.prevent
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="loginForm.username" autocomplete="username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            autocomplete="current-password"
            placeholder="请输入密码"
            show-password
            size="large"
            type="password"
          />
        </el-form-item>
        <el-form-item label="数字验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model.trim="loginForm.captchaCode" maxlength="4" placeholder="4 位数字" size="large" />
            <button class="captcha-button" type="button" :disabled="captchaLoading" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="数字验证码" />
              <span v-else>刷新</span>
            </button>
          </div>
        </el-form-item>
        <el-button class="login-button" type="primary" :loading="submitting" size="large" @click="submitLogin">
          登录
        </el-button>
      </el-form>

      <el-form
        v-else
        ref="registerFormRef"
        class="auth-form"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        @submit.prevent
      >
        <div class="form-grid auth-grid">
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="registerForm.username" autocomplete="username" placeholder="4-50 位字母、数字或下划线" size="large" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model.trim="registerForm.nickname" autocomplete="nickname" placeholder="2-20 位昵称" size="large" />
          </el-form-item>
        </div>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            autocomplete="new-password"
            placeholder="8-32 位，至少包含字母和数字"
            show-password
            size="large"
            type="password"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            autocomplete="new-password"
            placeholder="再次输入密码"
            show-password
            size="large"
            type="password"
          />
        </el-form-item>
        <el-form-item label="数字验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model.trim="registerForm.captchaCode" maxlength="4" placeholder="4 位数字" size="large" />
            <button class="captcha-button" type="button" :disabled="captchaLoading" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="数字验证码" />
              <span v-else>刷新</span>
            </button>
          </div>
        </el-form-item>
        <el-button class="login-button" type="primary" :loading="submitting" size="large" @click="submitRegister">
          注册并进入
        </el-button>
      </el-form>

      <div class="demo-account">
        <span>测试账号</span>
        <code>test_user / Test@123456</code>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '../../api/auth'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref()
const registerFormRef = ref()
const captchaImage = ref('')
const captchaLoading = ref(false)
const submitting = ref(false)
const mode = ref('login')

const isRegisterMode = computed(() => mode.value === 'register')

const loginForm = reactive({
  username: 'test_user',
  password: 'Test@123456',
  captchaId: '',
  captchaCode: ''
})

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  captchaId: '',
  captchaCode: ''
})

const usernameRule = { pattern: /^[A-Za-z0-9_]{4,50}$/, message: '用户名只能包含 4-50 位字母、数字或下划线', trigger: 'blur' }
const passwordRule = { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/, message: '密码需要 8-32 位且至少包含字母和数字', trigger: 'blur' }
const captchaRule = { pattern: /^\d{4}$/, message: '验证码应为 4 位数字', trigger: 'blur' }

const loginRules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '验证码不能为空', trigger: 'blur' }, captchaRule]
}

const registerRules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }, usernameRule],
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度需要在 2-20 位之间', trigger: 'blur' }
  ],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }, passwordRule],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  captchaCode: [{ required: true, message: '验证码不能为空', trigger: 'blur' }, captchaRule]
}

function activeForm() {
  return mode.value === 'register' ? registerForm : loginForm
}

async function refreshCaptcha() {
  captchaLoading.value = true
  try {
    const data = await getCaptcha()
    const form = activeForm()
    form.captchaId = data.captchaId
    form.captchaCode = ''
    captchaImage.value = data.imageBase64
  } finally {
    captchaLoading.value = false
  }
}

async function switchMode(nextMode) {
  if (mode.value === nextMode) return
  mode.value = nextMode
  await refreshCaptcha()
}

async function submitLogin() {
  await loginFormRef.value.validate()
  submitting.value = true
  try {
    await authStore.login({ ...loginForm })
    ElMessage.success('登录成功')
    router.push(authStore.isAdmin ? '/admin' : '/dashboard')
  } catch (error) {
    await refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

async function submitRegister() {
  await registerFormRef.value.validate()
  submitting.value = true
  try {
    await authStore.register({ ...registerForm })
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch (error) {
    await refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(refreshCaptcha)
</script>
