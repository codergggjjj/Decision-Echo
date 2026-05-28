<template>
  <AppShell v-model:search-value="shellSearch" active="goals" page-class="goal-shell-page" @search="goList" @create="showCreateDecisionHint">
    <div class="goal-detail-page">
      <button type="button" class="goal-back-button" @click="goList">返回长期目标</button>

      <div v-if="loading" class="goal-detail-state">正在加载目标详情...</div>
      <div v-else-if="errorMessage" class="goal-detail-state error">
        <strong>目标详情加载失败</strong>
        <p>{{ errorMessage }}</p>
        <button type="button" @click="loadDetail">重新加载</button>
      </div>

      <template v-else-if="goal">
        <section class="goal-detail-hero">
          <div class="goal-detail-title">
            <div class="goal-detail-badges">
              <span class="detail-badge status">{{ statusText(goal.status) }}</span>
              <span class="detail-badge priority">{{ priorityText(goal.priority) }}</span>
              <span class="detail-badge category">{{ goal.category || '未分类' }}</span>
            </div>
            <h1>{{ goal.title || '未命名目标' }}</h1>
            <p>{{ goal.description || '暂未填写目标描述。' }}</p>
          </div>
          <button type="button" class="goal-primary-button" @click="showCreateDecisionHint">
            <span>+</span>
            为此目标创建决策
          </button>
        </section>

        <section class="goal-detail-main">
          <article class="goal-overview-card">
            <div class="overview-head">
              <div>
                <span>目标进度</span>
                <strong>{{ normalizedProgress(goal.progress) }}%</strong>
              </div>
              <em>{{ daysLeftText(goal.targetDate) }}</em>
            </div>
            <div class="detail-progress-track">
              <i :style="{ width: `${normalizedProgress(goal.progress)}%` }"></i>
            </div>
            <dl class="goal-meta-grid">
              <div>
                <dt>预期完成日期</dt>
                <dd>{{ formatDate(goal.targetDate) }}</dd>
              </div>
              <div>
                <dt>衡量方式</dt>
                <dd>{{ goal.measurement || '暂未填写' }}</dd>
              </div>
              <div>
                <dt>目标分类</dt>
                <dd>{{ goal.category || '未分类' }}</dd>
              </div>
              <div>
                <dt>当前状态</dt>
                <dd>{{ statusText(goal.status) }}</dd>
              </div>
            </dl>
          </article>

          <section class="detail-stats" aria-label="目标关联统计">
            <article v-for="item in statCards" :key="item.label" class="detail-stat-card" :class="item.tone">
              <span>{{ item.icon }}</span>
              <div>
                <small>{{ item.label }}</small>
                <strong>{{ item.value }}</strong>
              </div>
            </article>
          </section>
        </section>

        <section class="linked-decisions-card">
          <div class="linked-head">
            <div>
              <span>关联的决策</span>
              <h2>围绕此目标留下的选择</h2>
            </div>
            <strong>{{ decisions.length }} 条</strong>
          </div>

          <div v-if="decisions.length === 0" class="linked-empty">
            <strong>暂无关联决策</strong>
            <p>从决策表单接入目标后，这里会展示所有关联到该目标的决策。</p>
          </div>
          <div v-else class="linked-list">
            <article v-for="decision in decisions" :key="decision.id || decision.title" class="linked-item">
              <div>
                <span :class="['decision-status', decisionStatusClass(decision)]">{{ decisionStatusText(decision) }}</span>
                <h3>{{ decision.title || '未命名决策' }}</h3>
                <p>最终选择：{{ decision.finalChoice || decision.selectedOption || '暂未记录' }}</p>
              </div>
              <time>{{ formatDate(decision.createTime || decision.createdAt) }}</time>
            </article>
          </div>
        </section>
      </template>
    </div>
  </AppShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppShell from '../../components/AppShell.vue'
import { getGoalDetail } from '../../api/goal'

const route = useRoute()
const router = useRouter()
const shellSearch = ref('')
const loading = ref(false)
const errorMessage = ref('')
const goal = ref(null)
const stats = ref({})
const decisions = ref([])

const statCards = computed(() => [
  { label: '关联决策总数', value: numberStat('decisionCount'), icon: '⌁', tone: 'pink' },
  { label: '已复盘决策', value: numberStat('reviewedCount'), icon: '✓', tone: 'blue' },
  { label: '待回看数量', value: numberStat('pendingReviewCount'), icon: '◷', tone: 'yellow' },
  { label: '满意数量', value: numberStat('satisfiedCount'), icon: '☆', tone: 'gray' }
])

async function loadDetail() {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await getGoalDetail(route.params.id)
    const normalized = normalizeDetail(data)
    goal.value = normalized.goal
    stats.value = normalized.stats
    decisions.value = normalized.decisions
  } catch (error) {
    goal.value = null
    stats.value = {}
    decisions.value = []
    errorMessage.value = error.message || '请稍后重试。'
  } finally {
    loading.value = false
  }
}

function normalizeDetail(data) {
  const rootGoal = data?.goal || data || null
  return {
    goal: rootGoal,
    stats: data?.stats || rootGoal?.stats || {},
    decisions: normalizeDecisionList(data?.decisions || rootGoal?.decisions)
  }
}

function normalizeDecisionList(data) {
  if (Array.isArray(data)) {
    return data
  }
  if (Array.isArray(data?.records)) {
    return data.records
  }
  if (Array.isArray(data?.list)) {
    return data.list
  }
  return []
}

function numberStat(key) {
  return Number(stats.value?.[key] ?? goal.value?.[key] ?? 0) || 0
}

function goList() {
  router.push('/goals')
}

function showCreateDecisionHint() {
  ElMessage.info('从目标创建决策的入口已预留，待决策表单接入目标后启用。')
}

function normalizedProgress(value) {
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return 0
  }
  return Math.min(100, Math.max(0, Math.round(numberValue)))
}

function statusText(status) {
  return {
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    ABANDONED: '已放弃'
  }[status] || status || '进行中'
}

function priorityText(priority) {
  return {
    HIGH: '高优先级',
    MEDIUM: '中优先级',
    LOW: '低优先级'
  }[priority] || priority || '中优先级'
}

function decisionStatusText(decision) {
  return decision.status === 'reviewed' || decision.status === 'REVIEWED' ? '已复盘' : '待回看'
}

function decisionStatusClass(decision) {
  return decision.status === 'reviewed' || decision.status === 'REVIEWED' ? 'done' : 'todo'
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

onMounted(loadDetail)
</script>

<style scoped>
.goal-detail-page {
  width: min(100%, 1180px);
  margin: 0 auto;
  color: #191c1e;
}

.goal-back-button {
  margin-bottom: 18px;
  padding: 9px 18px;
  border: 1px solid rgba(217, 192, 199, 0.86);
  border-radius: 999px;
  background: #ffffff;
  color: #544248;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 850;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
}

.goal-back-button:hover {
  color: #9e3a68;
  transform: translateY(-1px);
}

.goal-detail-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
  padding: 24px;
  border: 1px solid rgba(217, 192, 199, 0.38);
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
}

.goal-detail-title {
  min-width: 0;
}

.goal-detail-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.detail-badge {
  display: inline-flex;
  min-height: 26px;
  align-items: center;
  padding: 0 11px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 850;
}

.detail-badge.status {
  background: #d4e3ff;
  color: #001c39;
}

.detail-badge.priority {
  background: #ffd9e4;
  color: #3e0021;
}

.detail-badge.category {
  background: #ffdf9f;
  color: #261a00;
}

.goal-detail-hero h1 {
  margin: 0;
  color: #191c1e;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.25;
  letter-spacing: 0;
}

.goal-detail-hero p {
  max-width: 720px;
  margin: 10px 0 0;
  color: #544248;
  font-size: 15px;
  line-height: 1.7;
}

.goal-primary-button {
  display: inline-flex;
  height: 48px;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
  padding: 0 22px;
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
}

.goal-primary-button:hover {
  background: #802150;
  box-shadow: 0 8px 24px rgba(158, 58, 104, 0.28);
  transform: translateY(-3px);
}

.goal-detail-main {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.7fr);
  gap: 24px;
  margin-bottom: 24px;
}

.goal-overview-card,
.linked-decisions-card,
.goal-detail-state {
  border: 1px solid rgba(217, 192, 199, 0.38);
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
}

.goal-overview-card {
  padding: 24px;
}

.overview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.overview-head span {
  display: block;
  color: #544248;
  font-size: 12px;
  font-weight: 850;
}

.overview-head strong {
  display: block;
  margin-top: 4px;
  color: #191c1e;
  font-size: 44px;
  font-weight: 900;
  line-height: 1;
}

.overview-head em {
  padding: 8px 14px;
  border-radius: 999px;
  background: #f2f4f6;
  color: #544248;
  font-style: normal;
  font-size: 12px;
  font-weight: 850;
}

.detail-progress-track {
  width: 100%;
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: #e6e8ea;
}

.detail-progress-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #9e3a68, #64a8fe);
}

.goal-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 24px 0 0;
}

.goal-meta-grid div {
  min-width: 0;
  padding: 16px;
  border-radius: 14px;
  background: #f7f9fb;
}

.goal-meta-grid dt {
  margin: 0 0 6px;
  color: #544248;
  font-size: 11px;
  font-weight: 850;
}

.goal-meta-grid dd {
  margin: 0;
  color: #191c1e;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.45;
}

.detail-stats {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.detail-stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgba(217, 192, 199, 0.38);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
}

.detail-stat-card > span {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 999px;
  background: #ffd9e4;
  color: #3e0021;
  font-size: 22px;
  font-weight: 900;
}

.detail-stat-card.blue > span {
  background: #d4e3ff;
  color: #001c39;
}

.detail-stat-card.yellow > span {
  background: #ffdf9f;
  color: #261a00;
}

.detail-stat-card.gray > span {
  background: #e6e8ea;
  color: #544248;
}

.detail-stat-card small {
  display: block;
  color: #544248;
  font-size: 11px;
  font-weight: 850;
}

.detail-stat-card strong {
  display: block;
  margin-top: 4px;
  color: #191c1e;
  font-size: 24px;
  font-weight: 900;
}

.linked-decisions-card {
  padding: 22px;
}

.linked-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.linked-head span {
  color: #9e3a68;
  font-size: 12px;
  font-weight: 900;
}

.linked-head h2 {
  margin: 4px 0 0;
  color: #191c1e;
  font-size: 20px;
  font-weight: 900;
}

.linked-head strong {
  padding: 8px 14px;
  border-radius: 999px;
  background: #f2f4f6;
  color: #544248;
  font-size: 12px;
  font-weight: 850;
}

.linked-empty,
.goal-detail-state {
  padding: 28px;
  color: #544248;
  text-align: center;
}

.linked-empty strong,
.goal-detail-state strong {
  display: block;
  margin-bottom: 8px;
  color: #191c1e;
  font-size: 18px;
}

.linked-empty p,
.goal-detail-state p {
  margin: 0;
}

.goal-detail-state.error {
  border-color: rgba(186, 26, 26, 0.2);
}

.goal-detail-state.error button {
  margin-top: 16px;
  padding: 9px 18px;
  border: 0;
  border-radius: 999px;
  background: #9e3a68;
  color: #ffffff;
  cursor: pointer;
  font-weight: 800;
}

.linked-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.linked-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  background: #f7f9fb;
}

.decision-status {
  display: inline-flex;
  margin-bottom: 8px;
  padding: 5px 10px;
  border-radius: 999px;
  background: #ffdf9f;
  color: #261a00;
  font-size: 11px;
  font-weight: 850;
}

.decision-status.done {
  background: #d4e3ff;
  color: #001c39;
}

.linked-item h3 {
  margin: 0;
  color: #191c1e;
  font-size: 16px;
  font-weight: 900;
}

.linked-item p {
  margin: 6px 0 0;
  color: #544248;
  font-size: 13px;
}

.linked-item time {
  flex: 0 0 auto;
  color: #544248;
  font-size: 12px;
  font-weight: 750;
}

@media (max-width: 980px) {
  .goal-detail-main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .goal-detail-hero,
  .overview-head,
  .linked-item {
    flex-direction: column;
  }

  .goal-primary-button {
    justify-content: center;
    width: 100%;
  }

  .goal-meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
