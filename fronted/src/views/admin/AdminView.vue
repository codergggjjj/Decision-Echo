<template>
  <main class="admin-page">
    <header class="admin-header">
      <div>
        <span>后台管理</span>
        <h1>全站数据总览</h1>
        <p>查看用户与决策记录的基础运营数据。</p>
      </div>
      <button type="button" class="admin-logout-button" @click="handleLogout">退出登录</button>
    </header>

    <section class="admin-stats" v-loading="loading.stats">
      <article v-for="item in statCards" :key="item.label" class="admin-stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </section>

    <section class="admin-panel">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="用户列表" name="users">
          <div class="admin-toolbar">
            <el-input v-model="userKeyword" placeholder="搜索用户名或昵称" clearable @keyup.enter="loadUsers" />
            <el-button type="primary" @click="loadUsers">搜索</el-button>
          </div>
          <el-table :data="users" v-loading="loading.users" class="admin-table">
            <el-table-column prop="id" label="用户ID" width="90" />
            <el-table-column prop="username" label="用户名" min-width="130" />
            <el-table-column prop="nickname" label="昵称" min-width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="角色" width="110">
              <template #default="{ row }">
                <el-tag :type="row.role === 'admin' ? 'warning' : 'info'">
                  {{ row.role === 'admin' ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" min-width="170">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="决策列表" name="decisions">
          <div class="admin-toolbar">
            <el-input v-model="decisionKeyword" placeholder="搜索标题或用户名" clearable @keyup.enter="loadDecisions" />
            <el-select v-model="decisionStatus" placeholder="全部状态" clearable>
              <el-option label="待回测" value="pending" />
              <el-option label="已回测" value="reviewed" />
            </el-select>
            <el-button type="primary" @click="loadDecisions">搜索</el-button>
          </div>
          <el-table :data="decisions" v-loading="loading.decisions" class="admin-table">
            <el-table-column prop="id" label="决策ID" width="90" />
            <el-table-column prop="username" label="用户" min-width="120" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="tags" label="标签" min-width="120" show-overflow-tooltip />
            <el-table-column label="回测状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 'reviewed' ? 'success' : 'info'">
                  {{ row.status === 'reviewed' ? '已回测' : '待回测' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="170">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
  </main>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminDecisions, getAdminStats, getAdminUsers, resetAdminUserPassword } from '../../api/admin'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('users')
const userKeyword = ref('')
const decisionKeyword = ref('')
const decisionStatus = ref('')
const users = ref([])
const decisions = ref([])
const stats = reactive({
  userTotal: 0,
  decisionTotal: 0,
  reviewedTotal: 0,
  pendingTotal: 0
})
const loading = reactive({
  stats: false,
  users: false,
  decisions: false
})

const statCards = computed(() => [
  { label: '用户总数', value: stats.userTotal },
  { label: '决策总数', value: stats.decisionTotal },
  { label: '已回测数量', value: stats.reviewedTotal },
  { label: '待回测数量', value: stats.pendingTotal }
])

function formatDate(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

async function loadStats() {
  loading.stats = true
  try {
    Object.assign(stats, await getAdminStats())
  } finally {
    loading.stats = false
  }
}

async function loadUsers() {
  loading.users = true
  try {
    users.value = await getAdminUsers({
      keyword: userKeyword.value || undefined,
      limit: 50
    })
  } finally {
    loading.users = false
  }
}

async function handleResetPassword(row) {
  await ElMessageBox.confirm(
    `确定将用户「${row.username}」的密码重置为初始密码 123456 吗？`,
    '重置密码',
    {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  await resetAdminUserPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

async function loadDecisions() {
  loading.decisions = true
  try {
    decisions.value = await getAdminDecisions({
      keyword: decisionKeyword.value || undefined,
      status: decisionStatus.value || undefined,
      limit: 50
    })
  } finally {
    loading.decisions = false
  }
}

function handleTabChange(name) {
  if (name === 'users' && users.value.length === 0) {
    loadUsers()
  }
  if (name === 'decisions' && decisions.value.length === 0) {
    loadDecisions()
  }
}

onMounted(() => {
  loadStats()
  loadUsers()
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 32px;
  background: linear-gradient(180deg, #fff8fb 0%, #f7fbff 100%);
  color: #243047;
}

.admin-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  max-width: 1180px;
  margin: 0 auto 24px;
}

.admin-header span {
  color: #fb7185;
  font-weight: 700;
}

.admin-header h1 {
  margin: 8px 0;
  font-size: 34px;
}

.admin-header p {
  margin: 0;
  color: #64748b;
}

.admin-logout-button {
  border: 0;
  border-radius: 18px;
  padding: 12px 18px;
  color: #ffffff;
  background: #fb7185;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(251, 113, 133, 0.22);
}

.admin-stats,
.admin-panel {
  max-width: 1180px;
  margin: 0 auto;
}

.admin-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.admin-stat-card,
.admin-panel {
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 36px rgba(148, 163, 184, 0.14);
}

.admin-stat-card {
  padding: 20px;
}

.admin-stat-card span {
  color: #64748b;
  font-size: 14px;
}

.admin-stat-card strong {
  display: block;
  margin-top: 8px;
  font-size: 32px;
  color: #0f172a;
}

.admin-panel {
  padding: 18px;
}

.admin-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.admin-toolbar .el-input {
  max-width: 260px;
}

.admin-toolbar .el-select {
  max-width: 160px;
}

.admin-table {
  width: 100%;
}

@media (max-width: 860px) {
  .admin-page {
    padding: 20px 14px;
  }

  .admin-header,
  .admin-toolbar {
    flex-direction: column;
  }

  .admin-header h1 {
    font-size: 28px;
  }

  .admin-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-toolbar .el-input,
  .admin-toolbar .el-select {
    max-width: none;
    width: 100%;
  }
}

@media (max-width: 520px) {
  .admin-stats {
    grid-template-columns: 1fr;
  }
}
</style>
