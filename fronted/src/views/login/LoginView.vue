<template>
  <main class="login-page">
    <section class="login-hero" aria-label="应用介绍">
      <div class="hero-content">
        <div class="brand-chip">
          <span class="brand-mark">决</span>
          <span>个人决策回测器</span>
        </div>
        <h1>把今天的想法，留给之后的自己看。</h1>
        <p>不需要复杂表格。写下选择、原因和约定的回看时间，等结果出现时，再认真复盘一次。</p>
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

    <section class="login-panel" aria-label="登录表单">
      <div class="panel-heading">
        <h2>欢迎回来</h2>
        <p>使用账号、密码和验证码登录系统。</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" autocomplete="username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            autocomplete="current-password"
            placeholder="请输入密码"
            show-password
            size="large"
            type="password"
          />
        </el-form-item>
        <el-form-item label="数字验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model.trim="form.captchaCode" maxlength="4" placeholder="4位数字" size="large" />
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

      <div class="demo-account">
        <span>测试账号</span>
        <code>test_user / Test@123456</code>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '../../api/auth'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const captchaImage = ref('')
const captchaLoading = ref(false)
const submitting = ref(false)

const form = reactive({
  username: 'test_user',
  password: 'Test@123456',
  captchaId: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
  captchaCode: [
    { required: true, message: '验证码不能为空', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '验证码应为4位数字', trigger: 'blur' }
  ]
}

async function refreshCaptcha() {
  captchaLoading.value = true
  try {
    const data = await getCaptcha()
    form.captchaId = data.captchaId
    form.captchaCode = ''
    captchaImage.value = data.imageBase64
  } finally {
    captchaLoading.value = false
  }
}

async function submitLogin() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await authStore.login({ ...form })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    await refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(refreshCaptcha)
</script>
