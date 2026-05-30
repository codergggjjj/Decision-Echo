<template>
  <AppShell v-model:search-value="shellSearch" active="goals" page-class="goal-shell-page" @search="goList" @create="showCreateDecisionHint">
    <div class="goal-detail-page">
      <div v-if="loading" class="goal-detail-state">正在加载目标详情...</div>
      <div v-else-if="errorMessage" class="goal-detail-state error">
        <strong>目标详情加载失败</strong>
        <p>{{ errorMessage }}</p>
        <button type="button" @click="loadDetail">重新加载</button>
      </div>

      <template v-else-if="goal">
        <section class="goal-detail-header">
          <button type="button" class="goal-back-button" @click="goList">
            <span aria-hidden="true">‹</span>
            返回长期目标
          </button>

          <div class="goal-heading-row">
            <div class="goal-detail-title">
              <div class="goal-detail-badges">
                <span :class="['detail-badge', 'status', statusClass(goal.status)]">{{ statusText(goal.status) }}</span>
                <span :class="['detail-badge', 'priority', priorityClass(goal.priority)]">{{ priorityText(goal.priority) }}</span>
                <span class="detail-badge category">{{ goal.category || '未分类' }}</span>
              </div>
              <h1>{{ goal.title || '未命名目标' }}</h1>
            </div>
            <button type="button" class="goal-primary-button" @click="showCreateDecisionHint">
              <span>+</span>
              为此目标创建决策
            </button>
          </div>
        </section>

        <section class="goal-bento-grid">
          <div class="goal-left-column">
            <article class="goal-glass-card goal-overview-card">
              <div class="section-title-row">
                <div>
                  <span>GOAL OVERVIEW</span>
                  <h2>目标概览</h2>
                </div>
                <em>{{ daysLeftText(goal.targetDate) }}</em>
              </div>

              <p class="goal-description">{{ goal.description || '暂未填写目标描述。' }}</p>

              <div class="progress-panel">
                <div class="overview-head">
                  <div>
                    <span>整体进度</span>
                    <strong>{{ normalizedProgress(goal.progress) }}%</strong>
                  </div>
                  <small>{{ statusText(goal.status) }}</small>
                </div>
                <div class="detail-progress-track">
                  <i :style="{ width: `${normalizedProgress(goal.progress)}%` }"></i>
                </div>
              </div>

              <dl class="goal-meta-strip">
                <div class="goal-meta-item deadline">
                  <span class="meta-icon" aria-hidden="true">▣</span>
                  <dt>DEADLINE</dt>
                  <dd>{{ formatDate(goal.targetDate) }}</dd>
                </div>
                <div class="goal-meta-item measurement">
                  <span class="meta-icon" aria-hidden="true">▤</span>
                  <dt>MEASUREMENT</dt>
                  <dd>{{ goal.measurement || '暂未填写' }}</dd>
                </div>
              </dl>
            </article>

            <section class="goal-glass-card linked-decisions-card">
              <div class="section-title-row">
                <div>
                  <span>RELATED DECISIONS</span>
                  <h2>关联的决策</h2>
                </div>
                <em>{{ decisions.length }} 条</em>
              </div>

              <div v-if="decisions.length === 0" class="linked-empty">
                <strong>暂无关联决策</strong>
                <p>从决策表单接入目标后，这里会展示所有关联到该目标的决策。</p>
              </div>
              <div v-else class="linked-list">
                <article v-for="decision in decisions" :key="decision.id || decision.title" class="linked-item">
                  <span class="decision-icon" aria-hidden="true">✓</span>
                  <div class="linked-item-body">
                    <div class="linked-item-title-row">
                      <h3>{{ decision.title || '未命名决策' }}</h3>
                      <span :class="['decision-status', decisionStatusClass(decision)]">{{ decisionStatusText(decision) }}</span>
                    </div>
                    <p>最终选择：{{ decision.finalChoice || decision.selectedOption || '暂未记录' }}</p>
                  </div>
                  <time>{{ formatDate(decision.createTime || decision.createdAt) }}</time>
                </article>
              </div>
            </section>
          </div>

          <aside class="goal-side-column">
            <article class="goal-glass-card alignment-card">
              <div class="section-title-row compact">
                <div>
                  <span>SATISFACTION</span>
                  <h2>决策满意度</h2>
                </div>
              </div>
              <div class="progress-gauge" :style="{ '--gauge-angle': `${satisfactionRate * 3.6}deg` }">
                <div>
                  <strong>{{ satisfactionRate }}%</strong>
                  <small>{{ satisfactionLabel }}</small>
                </div>
              </div>
              <p>{{ satisfactionDescription }}</p>
            </article>

            <section class="goal-glass-card status-card" aria-label="目标关联统计">
              <div class="section-title-row compact">
                <div>
                  <span>DECISION STATUS</span>
                  <h2>决策状态</h2>
                </div>
              </div>
              <div class="detail-stats">
                <article v-for="item in statCards" :key="item.label" class="detail-stat-card" :class="item.tone">
                  <span>{{ item.icon }}</span>
                  <div>
                    <small>{{ item.label }}</small>
                    <strong>{{ item.value }}</strong>
                  </div>
                </article>
              </div>
            </section>

            <section class="goal-glass-card activity-card">
              <div class="section-title-row compact">
                <div>
                  <span>ACTIVITY</span>
                  <h2>最近动态</h2>
                </div>
              </div>
              <div v-if="recentActivities.length === 0" class="activity-empty">暂无可展示的关联动态</div>
              <ol v-else class="activity-list">
                <li v-for="activity in recentActivities" :key="activity.key">
                  <span aria-hidden="true"></span>
                  <div>
                    <strong>{{ activity.title }}</strong>
                    <p>{{ activity.description }}</p>
                    <time>{{ activity.date }}</time>
                  </div>
                </li>
              </ol>
            </section>
          </aside>
        </section>
      </template>

      <CreateDecisionDialog
        v-model:visible="createDecisionDialogVisible"
        :initial-goal-id="goal?.id || route.params.id"
        :initial-goal="goal"
        @success="handleDecisionCreated"
      />
    </div>
  </AppShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppShell from '../../components/AppShell.vue'
import { getGoalDetail } from '../../api/goal'
import CreateDecisionDialog from './components/CreateDecisionDialog.vue'

const route = useRoute()
const router = useRouter()
const shellSearch = ref('')
const loading = ref(false)
const errorMessage = ref('')
const goal = ref(null)
const stats = ref({})
const decisions = ref([])
const createDecisionDialogVisible = ref(false)

const statCards = computed(() => [
  { label: '关联决策总数', value: numberStat('decisionCount'), icon: '⌁', tone: 'pink' },
  { label: '已复盘决策', value: numberStat('reviewedCount'), icon: '✓', tone: 'blue' },
  { label: '待回看数量', value: numberStat('pendingReviewCount'), icon: '◷', tone: 'yellow' },
  { label: '满意数量', value: numberStat('satisfiedCount'), icon: '☆', tone: 'gray' }
])

const satisfactionRate = computed(() => {
  const reviewed = numberStat('reviewedCount')
  const satisfied = numberStat('satisfiedCount')
  return reviewed === 0 ? 0 : Math.round((satisfied / reviewed) * 100)
})

const satisfactionLabel = computed(() => {
  const reviewed = numberStat('reviewedCount')
  const satisfied = numberStat('satisfiedCount')
  return reviewed === 0 ? '暂无复盘' : `满意 ${satisfied}/${reviewed}`
})

const satisfactionDescription = computed(() => {
  const reviewed = numberStat('reviewedCount')
  if (reviewed === 0) {
    return '该目标下还没有已复盘决策，完成回看后会自动计算满意度。'
  }
  return '基于该目标下已复盘决策中标记为满意的数量计算。'
})

const recentActivities = computed(() => decisions.value
  .map((decision, index) => ({
    key: decision.id || `${decision.title || 'decision'}-${index}`,
    title: decision.title || '未命名决策',
    description: `最终选择：${decision.finalChoice || decision.selectedOption || '暂未记录'}`,
    date: formatDate(decision.createTime || decision.createdAt),
    time: parseDateTime(decision.createTime || decision.createdAt)
  }))
  .sort((a, b) => b.time - a.time)
  .slice(0, 4))

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
  createDecisionDialogVisible.value = true
}

async function handleDecisionCreated() {
  await loadDetail()
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

function statusClass(status) {
  return {
    IN_PROGRESS: 'is-active',
    COMPLETED: 'is-done',
    ABANDONED: 'is-muted'
  }[status] || 'is-active'
}

function priorityClass(priority) {
  return {
    HIGH: 'is-high',
    MEDIUM: 'is-medium',
    LOW: 'is-low'
  }[priority] || 'is-medium'
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

function parseDateTime(value) {
  if (!value) {
    return 0
  }
  const time = new Date(Array.isArray(value) ? formatDate(value) : String(value).replace(' ', 'T')).getTime()
  return Number.isNaN(time) ? 0 : time
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

.goal-detail-header {
  margin-bottom: 24px;
}

.goal-back-button {
  display: inline-flex;
  min-height: 40px;
  align-items: center;
  gap: 7px;
  margin-bottom: 18px;
  padding: 0 16px 0 12px;
  border: 1px solid rgba(217, 192, 199, 0.72);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
  color: #544248;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 850;
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
  transition: transform 160ms ease, color 160ms ease, box-shadow 160ms ease;
}

.goal-back-button span {
  font-size: 25px;
  line-height: 1;
}

.goal-back-button:hover {
  color: #9e3a68;
  box-shadow: 0 8px 24px rgba(158, 58, 104, 0.14);
  transform: translateY(-2px);
}

.goal-heading-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.goal-detail-title {
  min-width: 0;
}

.goal-detail-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.goal-detail-title h1 {
  margin: 0;
  color: #191c1e;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.25;
  letter-spacing: 0;
}

.detail-badge {
  display: inline-flex;
  min-height: 30px;
  align-items: center;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.detail-badge.status.is-active {
  background: #d4e3ff;
  color: #001c39;
}

.detail-badge.status.is-done {
  background: #d7f3e2;
  color: #0e5c35;
}

.detail-badge.status.is-muted {
  background: #e6e8ea;
  color: #544248;
}

.detail-badge.priority.is-high {
  background: #ffd9e4;
  color: #3e0021;
}

.detail-badge.priority.is-medium {
  background: #ffdf9f;
  color: #4d3800;
}

.detail-badge.priority.is-low {
  background: #f2f4f6;
  color: #544248;
}

.detail-badge.category {
  background: rgba(255, 255, 255, 0.78);
  border-color: rgba(217, 192, 199, 0.72);
  color: #544248;
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

.goal-bento-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(300px, 0.9fr);
  gap: 24px;
}

.goal-left-column,
.goal-side-column {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 24px;
}

.goal-glass-card,
.goal-detail-state {
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(25, 28, 30, 0.05);
}

.goal-overview-card {
  padding: 24px;
}

.section-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.section-title-row span {
  display: block;
  margin-bottom: 5px;
  color: #9e3a68;
  font-size: 11px;
  font-weight: 950;
  letter-spacing: 0.08em;
}

.section-title-row h2 {
  margin: 0;
  color: #191c1e;
  font-size: 20px;
  font-weight: 950;
  line-height: 1.25;
}

.section-title-row em {
  flex: 0 0 auto;
  padding: 8px 14px;
  border-radius: 999px;
  background: #f2f4f6;
  color: #544248;
  font-style: normal;
  font-size: 12px;
  font-weight: 850;
}

.section-title-row.compact {
  margin-bottom: 16px;
}

.goal-description {
  margin: 0 0 22px;
  color: #544248;
  font-size: 16px;
  line-height: 1.75;
}

.progress-panel {
  padding: 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 217, 228, 0.55), rgba(212, 227, 255, 0.48));
}

.overview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
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

.overview-head small {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
  color: #9e3a68;
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

.goal-meta-strip {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin: 22px 0 0;
}

.goal-meta-item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 4px 14px;
  align-items: center;
  min-width: 0;
  padding: 16px;
  border: 1px solid rgba(217, 192, 199, 0.34);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.goal-meta-item dt,
.goal-meta-item dd {
  grid-column: 2;
}

.goal-meta-item dt {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #6b5a62;
  font-weight: 900;
  letter-spacing: 0;
}

.goal-meta-item .meta-icon {
  display: grid;
  width: 38px;
  height: 38px;
  grid-column: 1;
  grid-row: 1 / span 2;
  place-items: center;
  border-radius: 11px;
  font-size: 17px;
}

.goal-meta-item dt {
  color: #6b5a62;
  font-size: 11px;
  font-weight: 950;
  letter-spacing: 0.04em;
}

.goal-meta-item.deadline .meta-icon {
  background: #ffdf9f;
  color: #795900;
}

.goal-meta-item.measurement .meta-icon {
  background: #d4e3ff;
  color: #0060ac;
}

.goal-meta-item dd {
  margin: 0;
  overflow-wrap: anywhere;
  color: #191c1e;
  font-size: 17px;
  font-weight: 900;
  line-height: 1.35;
}

.alignment-card,
.status-card,
.activity-card,
.linked-decisions-card {
  padding: 22px;
}

.alignment-card {
  overflow: hidden;
}

.progress-gauge {
  display: grid;
  width: 182px;
  height: 182px;
  place-items: center;
  margin: 4px auto 18px;
  border-radius: 50%;
  background:
    radial-gradient(circle at center, #ffffff 0 58%, transparent 59%),
    conic-gradient(#9e3a68 0 var(--gauge-angle), #e6e8ea var(--gauge-angle) 360deg);
  box-shadow: inset 0 0 0 1px rgba(217, 192, 199, 0.42), 0 12px 30px rgba(158, 58, 104, 0.12);
}

.progress-gauge div {
  display: grid;
  width: 128px;
  height: 128px;
  place-items: center;
  align-content: center;
  border-radius: 50%;
  background: #ffffff;
}

.progress-gauge strong {
  color: #191c1e;
  font-size: 34px;
  font-weight: 950;
  line-height: 1;
}

.progress-gauge small {
  margin-top: 8px;
  color: #544248;
  font-size: 12px;
  font-weight: 850;
}

.alignment-card p {
  margin: 0;
  color: #6b5a62;
  font-size: 13px;
  line-height: 1.65;
  text-align: center;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding: 15px;
  border: 1px solid rgba(217, 192, 199, 0.38);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
}

.detail-stat-card > span {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 999px;
  background: #ffd9e4;
  color: #3e0021;
  font-size: 18px;
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
  line-height: 1.25;
}

.detail-stat-card strong {
  display: block;
  margin-top: 4px;
  color: #191c1e;
  font-size: 24px;
  font-weight: 900;
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
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border: 1px solid rgba(217, 192, 199, 0.26);
  border-radius: 14px;
  background: rgba(247, 249, 251, 0.82);
  transition: transform 160ms ease, box-shadow 160ms ease, background 160ms ease;
}

.linked-item:hover {
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(25, 28, 30, 0.07);
  transform: translateY(-2px);
}

.decision-icon {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 13px;
  background: #ffd9e4;
  color: #9e3a68;
  font-size: 18px;
  font-weight: 950;
}

.linked-item-body {
  min-width: 0;
}

.linked-item-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.decision-status {
  display: inline-flex;
  flex: 0 0 auto;
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

.activity-empty {
  padding: 18px;
  border-radius: 14px;
  background: rgba(247, 249, 251, 0.82);
  color: #6b5a62;
  font-size: 13px;
  text-align: center;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.activity-list li {
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr);
  gap: 12px;
}

.activity-list li > span {
  position: relative;
  display: block;
  width: 11px;
  height: 11px;
  margin-top: 5px;
  border-radius: 999px;
  background: #9e3a68;
  box-shadow: 0 0 0 5px #ffd9e4;
}

.activity-list strong {
  display: block;
  color: #191c1e;
  font-size: 14px;
  font-weight: 900;
}

.activity-list p {
  margin: 4px 0 3px;
  color: #544248;
  font-size: 13px;
  line-height: 1.5;
}

.activity-list time {
  color: #877278;
  font-size: 12px;
  font-weight: 750;
}

@media (max-width: 980px) {
  .goal-bento-grid {
    grid-template-columns: 1fr;
  }

  .goal-side-column {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .activity-card {
    grid-column: 1 / -1;
  }
}

@media (max-width: 760px) {
  .goal-heading-row,
  .overview-head,
  .linked-item-title-row {
    flex-direction: column;
    align-items: stretch;
  }

  .goal-primary-button {
    justify-content: center;
    width: 100%;
  }

  .goal-meta-strip {
    grid-template-columns: 1fr;
  }

  .goal-side-column,
  .detail-stats {
    grid-template-columns: 1fr;
  }

  .linked-item {
    grid-template-columns: 40px minmax(0, 1fr);
  }

  .linked-item time {
    grid-column: 2;
  }
}
</style>
