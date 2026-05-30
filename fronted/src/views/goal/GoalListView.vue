<template>
  <AppShell v-model:search-value="keyword" active="goals" page-class="goal-shell-page" @search="loadGoals" @create="openCreateDialog">
    <div class="goal-page">
      <header class="goal-hero">
        <div>
          <h1>长期目标</h1>
          <p>让每一次决策都更靠近长期方向</p>
        </div>
        <button type="button" class="goal-primary-button" @click="openCreateDialog">
          <span>+</span>
          新建目标
        </button>
      </header>

      <section class="goal-stats" aria-label="目标统计">
        <article v-for="item in statCards" :key="item.label" class="goal-stat-card" :class="item.tone">
          <div class="goal-stat-icon" aria-hidden="true">{{ item.icon }}</div>
          <div>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </article>
      </section>

      <section class="goal-filter-card" aria-label="目标筛选">
        <div class="goal-filter-pills">
          <button
            v-for="item in statusFilters"
            :key="item.value"
            type="button"
            :class="{ active: selectedStatus === item.value }"
            @click="selectStatus(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
        <div class="goal-filter-actions" @click.stop>
          <div class="goal-filter-menu" :class="{ open: categoryMenuOpen }">
            <button type="button" class="goal-filter-trigger" @click="toggleCategoryMenu">
              {{ categoryFilterLabel }}
              <span>⌄</span>
            </button>
            <div v-if="categoryMenuOpen" class="goal-filter-dropdown">
              <button type="button" :class="{ active: selectedCategory === '' }" @click="selectCategory('')">全部分类</button>
              <button
                v-for="category in categoryOptions"
                :key="category"
                type="button"
                :class="{ active: selectedCategory === category }"
                @click="selectCategory(category)"
              >
                {{ category }}
              </button>
            </div>
          </div>
          <div class="goal-filter-menu" :class="{ open: priorityMenuOpen }">
            <button type="button" class="goal-filter-trigger" @click="togglePriorityMenu">
              {{ priorityFilterLabel }}
              <span>⌄</span>
            </button>
            <div v-if="priorityMenuOpen" class="goal-filter-dropdown">
              <button
                v-for="item in priorityFilters"
                :key="item.value"
                type="button"
                :class="{ active: selectedPriority === item.value }"
                @click="selectPriority(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <div v-if="loading" class="goal-state-card">正在加载长期目标...</div>
      <div v-else-if="errorMessage" class="goal-state-card error">
        <strong>长期目标加载失败</strong>
        <p>{{ errorMessage }}</p>
        <button type="button" @click="loadGoals">重新加载</button>
      </div>
      <div v-else-if="filteredGoals.length === 0" class="goal-state-card">
        <strong>{{ hasActiveFilters ? '没有找到匹配的目标' : '还没有长期目标' }}</strong>
        <p>{{ hasActiveFilters ? '换个关键词、状态、分类或优先级再试试。' : '后端目标接口返回数据后，这里会展示你的长期目标。' }}</p>
      </div>

      <section v-else class="goal-grid" aria-label="长期目标列表">
        <article
          v-for="goal in filteredGoals"
          :key="goal.id"
          class="goal-card"
          role="button"
          tabindex="0"
          @click="openGoal(goal)"
          @keydown.enter="openGoal(goal)"
        >
          <div class="goal-card-head">
            <div class="goal-badges">
              <span class="goal-badge priority" :class="priorityClass(goal.priority)">{{ priorityText(goal.priority) }}</span>
              <span class="goal-badge category" :class="categoryClass(goal.category)">{{ goal.category || '未分类' }}</span>
            </div>
            <div class="goal-actions" @click.stop>
              <button type="button" title="编辑" @click="openEditDialog(goal)">✎</button>
              <button type="button" title="删除" @click="showReadonlyHint">⌫</button>
            </div>
          </div>

          <h2>{{ goal.title || '未命名目标' }}</h2>
          <p>{{ goal.description || '暂未填写目标描述。' }}</p>

          <div class="goal-card-bottom">
            <div class="goal-progress-meta">
              <span>进度 ({{ normalizedProgress(goal.progress) }}%)</span>
              <span>{{ daysLeftText(goal.targetDate) }}</span>
            </div>
            <div class="goal-progress-track">
              <i :class="progressClass(goal)" :style="{ width: `${normalizedProgress(goal.progress)}%` }"></i>
            </div>
            <div class="goal-card-foot">
              <span class="goal-decision-link">
                <span aria-hidden="true">↗</span>
                {{ decisionCount(goal) }}个决策
              </span>
              <span>{{ formatDate(goal.targetDate) }} 截止</span>
            </div>
          </div>
        </article>
      </section>
    </div>
    <GoalCreateDialog
      v-model:visible="createDialogVisible"
      :mode="editingGoal ? 'edit' : 'create'"
      :goal="editingGoal"
      @success="handleCreateSuccess"
    />
  </AppShell>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppShell from '../../components/AppShell.vue'
import GoalCreateDialog from './components/GoalCreateDialog.vue'
import { getGoals } from '../../api/goal'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const goals = ref([])
const keyword = ref('')
const selectedStatus = ref('')
const selectedCategory = ref('')
const selectedPriority = ref('')
const categoryMenuOpen = ref(false)
const priorityMenuOpen = ref(false)
const createDialogVisible = ref(false)
const editingGoal = ref(null)

const statusFilters = [
  { label: '全部', value: '' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已放弃', value: 'ABANDONED' }
]

const priorityFilters = [
  { label: '全部优先级', shortLabel: '优先级', value: '' },
  { label: '高优先级', shortLabel: '高优先级', value: 'HIGH' },
  { label: '中优先级', shortLabel: '中优先级', value: 'MEDIUM' },
  { label: '低优先级', shortLabel: '低优先级', value: 'LOW' }
]

const categoryOptions = computed(() => {
  return Array.from(new Set(goals.value.map((goal) => goal.category).filter(Boolean))).sort((a, b) => String(a).localeCompare(String(b), 'zh-Hans-CN'))
})

const categoryFilterLabel = computed(() => selectedCategory.value || '分类')

const priorityFilterLabel = computed(() => {
  return priorityFilters.find((item) => item.value === selectedPriority.value)?.shortLabel || '优先级'
})

const hasActiveFilters = computed(() => Boolean(keyword.value.trim() || selectedStatus.value || selectedCategory.value || selectedPriority.value))

const filteredGoals = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return goals.value.filter((goal) => {
    const matchesText = !text || [goal.title, goal.description, goal.category]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(text))
    const matchesCategory = !selectedCategory.value || goal.category === selectedCategory.value
    const matchesPriority = !selectedPriority.value || goal.priority === selectedPriority.value
    return matchesText && matchesCategory && matchesPriority
  })
})

const allGoalStats = computed(() => ({
  progressing: goals.value.filter((goal) => goal.status === 'IN_PROGRESS').length,
  completed: goals.value.filter((goal) => goal.status === 'COMPLETED').length,
  abandoned: goals.value.filter((goal) => goal.status === 'ABANDONED').length,
  decisions: goals.value.reduce((sum, goal) => sum + decisionCount(goal), 0)
}))

const statCards = computed(() => [
  { label: '进行中目标', value: allGoalStats.value.progressing, icon: '↗', tone: 'pink' },
  { label: '已完成目标', value: allGoalStats.value.completed, icon: '✓', tone: 'blue' },
  { label: '已放弃目标', value: allGoalStats.value.abandoned, icon: '×', tone: 'gray' },
  { label: '关联决策总数', value: allGoalStats.value.decisions, icon: '⌁', tone: 'yellow' }
])

async function loadGoals() {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await getGoals({ status: selectedStatus.value || undefined })
    goals.value = normalizeGoalList(data)
  } catch (error) {
    goals.value = []
    errorMessage.value = error.message || '请稍后重试。'
  } finally {
    loading.value = false
  }
}

async function selectStatus(status) {
  selectedStatus.value = status
  await loadGoals()
}

function toggleCategoryMenu() {
  categoryMenuOpen.value = !categoryMenuOpen.value
  priorityMenuOpen.value = false
}

function togglePriorityMenu() {
  priorityMenuOpen.value = !priorityMenuOpen.value
  categoryMenuOpen.value = false
}

function selectCategory(category) {
  selectedCategory.value = category
  categoryMenuOpen.value = false
}

function selectPriority(priority) {
  selectedPriority.value = priority
  priorityMenuOpen.value = false
}

function closeFilterMenus() {
  categoryMenuOpen.value = false
  priorityMenuOpen.value = false
}

function normalizeGoalList(data) {
  if (Array.isArray(data)) {
    return data
  }
  if (Array.isArray(data?.records)) {
    return data.records
  }
  if (Array.isArray(data?.list)) {
    return data.list
  }
  if (Array.isArray(data?.items)) {
    return data.items
  }
  return []
}

function openGoal(goal) {
  if (!goal.id) {
    return
  }
  router.push(`/goals/${goal.id}`)
}

function openCreateDialog() {
  editingGoal.value = null
  createDialogVisible.value = true
}

async function handleCreateSuccess() {
  editingGoal.value = null
  await loadGoals()
}

function openEditDialog(goal) {
  editingGoal.value = goal
  createDialogVisible.value = true
}

function showReadonlyHint() {
  ElMessage.info('编辑和删除入口已预留，本次先展示目标页面。')
}

function normalizedProgress(value) {
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return 0
  }
  return Math.min(100, Math.max(0, Math.round(numberValue)))
}

function decisionCount(goal) {
  return Number(goal.decisionCount ?? goal.decisionsCount ?? goal.decisions ?? 0) || 0
}

function priorityText(priority) {
  return {
    HIGH: '高优先级',
    MEDIUM: '中优先级',
    LOW: '低优先级'
  }[priority] || priority || '中优先级'
}

function priorityClass(priority) {
  return priority === 'HIGH' ? 'high' : ''
}

function categoryClass(category) {
  if (category === '学习') return 'study'
  if (category === '健康') return 'health'
  if (category === '财务') return 'finance'
  return ''
}

function progressClass(goal) {
  if (goal.status === 'COMPLETED') return 'done'
  if (goal.priority === 'HIGH') return 'primary'
  if (goal.category === '健康') return 'secondary'
  return 'tertiary'
}

function formatDate(value) {
  if (!value) {
    return '-'
  }
  if (Array.isArray(value)) {
    const [year, month, day] = value
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  return String(value).replace('T', ' ').slice(0, 10)
}

function daysLeftText(value) {
  if (!value) {
    return '未设截止'
  }
  const target = new Date(formatDate(value))
  if (Number.isNaN(target.getTime())) {
    return '未设截止'
  }
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const diff = Math.ceil((target.getTime() - today.getTime()) / 86400000)
  if (diff < 0) return `已超 ${Math.abs(diff)} 天`
  if (diff === 0) return '今天截止'
  return `剩余 ${diff} 天`
}

onMounted(() => {
  document.addEventListener('click', closeFilterMenus)
  loadGoals()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeFilterMenus)
})
</script>

<style scoped>
.goal-page {
  width: min(100%, 1180px);
  margin: 0 auto;
  color: #191c1e;
}

.goal-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.goal-hero h1 {
  margin: 0;
  color: #191c1e;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.25;
  letter-spacing: 0;
}

.goal-hero p {
  margin: 8px 0 0;
  color: #544248;
  font-size: 16px;
  font-weight: 500;
}

.goal-primary-button {
  display: inline-flex;
  height: 48px;
  align-items: center;
  gap: 8px;
  padding: 0 24px;
  border: 0;
  border-radius: 999px;
  background: #9e3a68;
  color: #ffffff;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 850;
  box-shadow: 0 4px 20px rgba(158, 58, 104, 0.22);
  transition: transform 160ms ease, box-shadow 160ms ease, background 160ms ease;
}

.goal-primary-button span {
  display: inline-grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 18px;
  line-height: 1;
}

.goal-primary-button:hover {
  background: #802150;
  box-shadow: 0 8px 24px rgba(158, 58, 104, 0.28);
  transform: translateY(-3px);
}

.goal-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.goal-stat-card {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgba(217, 192, 199, 0.42);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
}

.goal-stat-icon {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 999px;
  background: #ffd9e4;
  color: #3e0021;
  font-size: 22px;
  font-weight: 900;
}

.goal-stat-card.blue .goal-stat-icon {
  background: #d4e3ff;
  color: #001c39;
}

.goal-stat-card.gray .goal-stat-icon {
  background: #e6e8ea;
  color: #544248;
}

.goal-stat-card.yellow .goal-stat-icon {
  background: #ffdf9f;
  color: #261a00;
}

.goal-stat-card span {
  display: block;
  color: #544248;
  font-size: 11px;
  font-weight: 800;
}

.goal-stat-card strong {
  display: block;
  margin-top: 5px;
  color: #191c1e;
  font-size: 24px;
  font-weight: 900;
}

.goal-filter-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 24px;
  padding: 10px;
  border: 1px solid rgba(217, 192, 199, 0.34);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
}

.goal-filter-pills,
.goal-filter-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.goal-filter-actions {
  align-items: center;
  justify-content: flex-end;
}

.goal-filter-card button {
  min-height: 38px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: #f2f4f6;
  color: #544248;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 850;
  transition: transform 160ms ease, background 160ms ease, color 160ms ease, box-shadow 160ms ease;
}

.goal-filter-card button:hover {
  background: #eceef0;
  color: #9e3a68;
}

.goal-filter-card button.active {
  background: #9e3a68;
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(158, 58, 104, 0.18);
}

.goal-filter-actions button {
  border: 1px solid rgba(217, 192, 199, 0.86);
  background: #ffffff;
}

.goal-filter-menu {
  position: relative;
  display: inline-flex;
  justify-content: center;
}

.goal-filter-trigger {
  display: inline-flex;
  width: 112px;
  min-height: 38px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 14px;
  border: 1px solid rgba(217, 192, 199, 0.86);
  border-radius: 999px;
  background: #ffffff;
  color: #544248;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 850;
  white-space: nowrap;
  transition: background 160ms ease, color 160ms ease, box-shadow 160ms ease;
}

.goal-filter-trigger span {
  color: currentColor;
  font-size: 12px;
}

.goal-filter-trigger:hover,
.goal-filter-menu.open .goal-filter-trigger {
  background: #eceef0;
  color: #9e3a68;
}

.goal-filter-trigger:focus-visible {
  box-shadow: 0 0 0 3px rgba(158, 58, 104, 0.12);
  outline: none;
}

.goal-filter-dropdown {
  position: absolute;
  z-index: 20;
  top: calc(100% + 8px);
  left: 50%;
  display: grid;
  min-width: 148px;
  gap: 4px;
  padding: 8px;
  border: 1px solid rgba(217, 192, 199, 0.68);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 34px rgba(25, 28, 30, 0.12);
  backdrop-filter: blur(12px);
  transform: translateX(-50%);
}

.goal-filter-dropdown button {
  display: flex;
  min-height: 34px;
  align-items: center;
  justify-content: flex-start;
  padding: 0 12px;
  border: 0;
  background: transparent;
  color: #544248;
  text-align: left;
}

.goal-filter-dropdown button:hover {
  background: #f7f9fb;
  color: #9e3a68;
}

.goal-filter-dropdown button.active {
  background: #ffd9e4;
  color: #3e0021;
  box-shadow: none;
}

.goal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.goal-card {
  position: relative;
  display: flex;
  min-height: 292px;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
  padding: 18px;
  border: 1px solid rgba(217, 192, 199, 0.38);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.goal-card:hover,
.goal-card:focus-visible {
  border-color: rgba(158, 58, 104, 0.24);
  box-shadow: 0 8px 30px rgba(25, 28, 30, 0.08);
  outline: none;
  transform: translateY(-4px);
}

.goal-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.goal-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.goal-badge {
  display: inline-flex;
  min-height: 24px;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 850;
}

.goal-badge.priority {
  background: #e6e8ea;
  color: #191c1e;
}

.goal-badge.priority.high {
  background: #ffd9e4;
  color: #3e0021;
}

.goal-badge.category {
  background: #e0e3e5;
  color: #544248;
}

.goal-badge.category.study {
  background: #d4e3ff;
  color: #001c39;
}

.goal-badge.category.health {
  background: #ffdf9f;
  color: #261a00;
}

.goal-badge.category.finance {
  background: #f2f4f6;
  color: #544248;
}

.goal-actions {
  display: flex;
  gap: 4px;
  padding: 4px;
  border-radius: 999px;
  background: #ffffff;
  box-shadow: 0 4px 14px rgba(25, 28, 30, 0.08);
  opacity: 0;
  transition: opacity 160ms ease;
}

.goal-card:hover .goal-actions,
.goal-card:focus-visible .goal-actions {
  opacity: 1;
}

.goal-actions button {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #544248;
  cursor: pointer;
  font: inherit;
  font-size: 15px;
}

.goal-actions button:hover {
  background: #f2f4f6;
  color: #9e3a68;
}

.goal-card h2 {
  margin: 4px 0 0;
  color: #191c1e;
  font-size: 20px;
  font-weight: 850;
  line-height: 1.32;
}

.goal-card p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #544248;
  font-size: 14px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.goal-card-bottom {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
}

.goal-progress-meta,
.goal-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #544248;
  font-size: 11px;
  font-weight: 750;
}

.goal-progress-track {
  width: 100%;
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: #e6e8ea;
}

.goal-progress-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
  transition: width 240ms ease;
}

.goal-progress-track i.primary {
  background: #9e3a68;
}

.goal-progress-track i.secondary {
  background: #0060ac;
}

.goal-progress-track i.tertiary {
  background: #795900;
}

.goal-progress-track i.done {
  background: #64a8fe;
}

.goal-card-foot {
  margin-top: 8px;
  padding-top: 14px;
  border-top: 1px solid #e0e3e5;
}

.goal-decision-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0060ac;
}

.goal-state-card {
  padding: 28px;
  border: 1px solid rgba(217, 192, 199, 0.38);
  border-radius: 16px;
  background: #ffffff;
  color: #544248;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
  text-align: center;
}

.goal-state-card strong {
  display: block;
  margin-bottom: 8px;
  color: #191c1e;
  font-size: 18px;
}

.goal-state-card p {
  margin: 0;
}

.goal-state-card.error {
  border-color: rgba(186, 26, 26, 0.2);
}

.goal-state-card.error button {
  margin-top: 16px;
  padding: 9px 18px;
  border: 0;
  border-radius: 999px;
  background: #9e3a68;
  color: #ffffff;
  cursor: pointer;
  font-weight: 800;
}

@media (max-width: 1100px) {
  .goal-stats,
  .goal-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .goal-hero,
  .goal-filter-card {
    align-items: stretch;
    flex-direction: column;
  }

  .goal-primary-button,
  .goal-filter-actions button,
  .goal-filter-menu,
  .goal-filter-trigger {
    justify-content: center;
    width: 100%;
    max-width: none;
  }

  .goal-filter-dropdown {
    left: 0;
    width: 100%;
    transform: none;
  }

  .goal-stats,
  .goal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
