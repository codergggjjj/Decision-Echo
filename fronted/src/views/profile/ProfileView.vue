<template>
  <main class="profile-page">
    <header class="profile-header">
      <button type="button" class="profile-back" @click="goDashboard">返回主页</button>
      <button type="button" class="profile-logout" @click="handleLogout">退出</button>
    </header>

    <section class="profile-hero">
      <div class="profile-avatar-wrap">
        <img v-if="avatarUrl" :src="avatarUrl" alt="用户头像" />
        <span v-else>{{ avatarInitial }}</span>
      </div>
      <div class="profile-title">
        <h1>{{ displayName }}</h1>
        <p>@{{ user?.username || '-' }}</p>
      </div>
      <div class="profile-actions">
        <button type="button" class="profile-primary-action" @click="openEditDialog">编辑个人资料</button>
        <button type="button" class="profile-secondary-action" @click="openPasswordDialog">修改密码</button>
      </div>
    </section>

    <section class="profile-info-grid" aria-label="用户基本信息">
      <article class="profile-info-card">
        <span>用户名</span>
        <strong>{{ user?.username || '-' }}</strong>
      </article>
      <article class="profile-info-card">
        <span>昵称</span>
        <strong>{{ user?.nickname || '-' }}</strong>
      </article>
      <article class="profile-info-card">
        <span>注册日期</span>
        <strong>{{ formatDate(user?.createTime) }}</strong>
      </article>
      <article class="profile-info-card">
        <span>头像状态</span>
        <strong>{{ avatarUrl ? '已设置' : '默认头像' }}</strong>
      </article>
    </section>

    <section class="profile-note">
      <h2>账号小记</h2>
      <p>头像和昵称会显示在你的决策回看页面，修改密码后需要重新登录。</p>
    </section>

    <el-dialog v-model="editDialogVisible" class="youth-dialog profile-edit-dialog" title="编辑个人资料" width="560px">
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top">
        <section class="profile-edit-card">
          <div class="profile-upload-row">
            <div class="profile-edit-avatar">
              <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" alt="头像预览" />
              <span v-else>{{ profileInitial }}</span>
            </div>
            <div>
              <input ref="avatarInputRef" class="profile-file-input" type="file" accept="image/*" @change="handleAvatarChange" />
              <button type="button" class="profile-secondary-action compact" :disabled="uploadingAvatar" @click="avatarInputRef?.click()">
                {{ uploadingAvatar ? '上传中...' : '上传头像' }}
              </button>
              <p>支持 JPG、PNG、GIF、WebP，大小不超过 2MB。</p>
            </div>
          </div>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model.trim="profileForm.nickname" maxlength="50" show-word-limit placeholder="请输入昵称" />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <div class="profile-dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button class="primary-action dialog-primary" type="primary" :loading="savingProfile" @click="submitProfile">保存资料</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" class="youth-dialog password-dialog" title="修改密码" width="520px">
      <el-form ref="passwordFormRef" class="password-form" :model="passwordForm" :rules="passwordRules" label-position="top">
        <section class="password-card">
          <div class="password-card-title">
            <span>账号安全</span>
            <p>修改成功后会退出登录，请使用新密码重新进入。</p>
          </div>
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" autocomplete="current-password" placeholder="请输入当前密码" show-password type="password" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" autocomplete="new-password" placeholder="8-32 位，至少包含字母和数字" show-password type="password" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" autocomplete="new-password" placeholder="再次输入新密码" show-password type="password" />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <div class="password-dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button class="primary-action dialog-primary" type="primary" :loading="changingPassword" @click="submitPasswordChange">保存并重新登录</el-button>
        </div>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const authStore = useAuthStore()
const profileFormRef = ref()
const passwordFormRef = ref()
const avatarInputRef = ref()
const editDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const savingProfile = ref(false)
const uploadingAvatar = ref(false)
const changingPassword = ref(false)

const user = computed(() => authStore.user || {})
const displayName = computed(() => user.value.nickname || user.value.username || '朋友')
const avatarUrl = computed(() => user.value.avatarUrl || '')
const avatarInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())
const profileInitial = computed(() => (profileForm.nickname || displayName.value).slice(0, 1).toUpperCase())

const profileForm = reactive({
  nickname: '',
  avatarUrl: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  nickname: [{ required: true, message: '昵称不能为空', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/, message: '新密码需要 8-32 位且至少包含字母和数字', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value && value === passwordForm.oldPassword) {
          callback(new Error('新密码不能和旧密码相同'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function openEditDialog() {
  profileForm.nickname = user.value.nickname || user.value.username || ''
  profileForm.avatarUrl = user.value.avatarUrl || ''
  editDialogVisible.value = true
}

function openPasswordDialog() {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  passwordFormRef.value?.clearValidate()
  passwordDialogVisible.value = true
}

async function handleAvatarChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('只能上传图片文件')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像文件不能超过 2MB')
    return
  }
  uploadingAvatar.value = true
  try {
    const data = await authStore.uploadAvatar(file)
    profileForm.avatarUrl = data.avatarUrl
    ElMessage.success('头像已上传')
  } finally {
    uploadingAvatar.value = false
  }
}

async function submitProfile() {
  await profileFormRef.value.validate()
  savingProfile.value = true
  try {
    await authStore.updateProfile({ ...profileForm })
    ElMessage.success('资料已更新')
    editDialogVisible.value = false
  } finally {
    savingProfile.value = false
  }
}

async function submitPasswordChange() {
  await passwordFormRef.value.validate()
  changingPassword.value = true
  try {
    await authStore.changePassword({ ...passwordForm })
    ElMessage.success('密码已更新，请重新登录')
    passwordDialogVisible.value = false
    router.push('/login')
  } finally {
    changingPassword.value = false
  }
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

function goDashboard() {
  router.push('/dashboard')
}

function formatDate(value) {
  if (!value) return '-'
  if (Array.isArray(value)) {
    const [year, month, day] = value
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  return String(value).replace('T', ' ').slice(0, 10)
}

onMounted(() => {
  authStore.loadCurrentUser().catch(() => null)
})
</script>
