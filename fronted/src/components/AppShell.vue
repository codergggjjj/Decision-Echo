<template>
  <main class="dashboard-page reference-dashboard content-first-dashboard" :class="pageClass">
    <nav class="reference-topbar" aria-label="顶部导航">
      <button type="button" class="reference-brand compact-brand" @click="goDashboard">决策回声</button>
      <div class="reference-top-search" role="search">
        <span class="reference-search-icon" aria-hidden="true"></span>
        <el-input
          v-model.trim="searchProxy"
          clearable
          placeholder="搜索决策、标签..."
          @keyup.enter="emitSearch"
          @clear="emitSearch"
        />
      </div>
      <div class="reference-top-actions">
        <button type="button" class="reference-avatar-button" aria-label="进入个人中心" title="进入个人主页" @click="goProfile">
          <img v-if="avatarUrl" :src="avatarUrl" alt="用户头像" />
          <span v-else>{{ avatarInitial }}</span>
        </button>
        <button type="button" class="reference-icon-button reference-logout-button danger" aria-label="退出登录" title="退出登录" @click="handleLogout">
          <span class="reference-logout-icon" aria-hidden="true"></span>
        </button>
      </div>
    </nav>

    <aside class="reference-sidebar" aria-label="主导航">
      <div class="reference-sidebar-title">
        <div class="reference-logo-mark" aria-hidden="true"></div>
        <div>
          <h2>决策回声</h2>
          <p>个人决策回测器</p>
        </div>
      </div>
      <nav class="reference-side-links">
        <button type="button" :class="{ active: active === 'dashboard' }" @click="goDashboard">
          <span>首</span>
          首页
        </button>
        <button type="button" :class="{ active: active === 'analysis' }" @click="goAnalysis">
          <span>统</span>
          统计
        </button>
        <button type="button" :class="{ active: active === 'profile' }" @click="goProfile">
          <span>我</span>
          我的
        </button>
      </nav>
      <button type="button" class="reference-side-create" @click="emitCreate">
        <span>+</span>
        记录新决定
      </button>
    </aside>

    <section class="reference-shell">
      <slot />
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const props = defineProps({
  active: {
    type: String,
    default: 'dashboard'
  },
  searchValue: {
    type: String,
    default: ''
  },
  pageClass: {
    type: [String, Array, Object],
    default: ''
  }
})

const emit = defineEmits(['update:searchValue', 'search', 'create'])
const router = useRouter()
const authStore = useAuthStore()

const searchProxy = computed({
  get: () => props.searchValue,
  set: (value) => emit('update:searchValue', value)
})
const displayName = computed(() => authStore.user?.nickname || authStore.user?.username || '朋友')
const avatarUrl = computed(() => authStore.user?.avatarUrl || '')
const avatarInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())

function emitSearch() {
  emit('search')
}

function emitCreate() {
  emit('create')
}

function goDashboard() {
  router.push('/dashboard')
}

function goAnalysis() {
  router.push('/analysis')
}

function goProfile() {
  router.push('/profile')
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>
