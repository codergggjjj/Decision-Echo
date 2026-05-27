<template>
  <main class="analysis-page">
    <header class="analysis-header">
      <div>
        <span>图表分析</span>
        <h1>决策结果分析</h1>
        <p>先从已回测决策的满意度分布开始，后续图表会继续补齐。</p>
      </div>
      <div class="memory-actions analysis-actions">
        <nav class="top-view-nav" aria-label="页面切换">
          <button type="button" @click="goDashboard">决策记录</button>
          <button type="button" class="active" @click="goAnalysis">图表分析</button>
        </nav>
        <div class="avatar-menu">
          <button type="button" class="avatar-nav-button" aria-label="进入个人中心" title="进入个人主页" @click="goProfile">
            <span class="avatar-frame">
              <img v-if="avatarUrl" :src="avatarUrl" alt="用户头像" />
              <span v-else>{{ avatarInitial }}</span>
            </span>
            <span class="avatar-chevron" aria-hidden="true"></span>
          </button>
          <button type="button" class="top-icon-button logout-icon-button" aria-label="退出登录" title="退出登录" @click="handleLogout">
            <span class="logout-icon" aria-hidden="true"></span>
          </button>
        </div>
      </div>
    </header>

    <section class="analysis-grid">
      <article class="analysis-card pie-card" v-loading="loading">
        <div class="card-title">
          <div>
            <span>满意度分布</span>
            <h2>回测结果占比</h2>
          </div>
          <strong>{{ total }} 条</strong>
        </div>

        <div class="analysis-filters" aria-label="满意度筛选">
          <el-select v-model="filters.tag" placeholder="全部标签" clearable @change="loadSatisfactionPie">
            <el-option v-for="item in tagOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.mood" placeholder="全部心情" clearable @change="loadSatisfactionPie">
            <el-option v-for="item in moodOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>

        <div v-if="isEmpty" class="analysis-empty">
          <strong>暂无已回测结果</strong>
          <p>当前筛选下暂无结果，换个标签或心情再看看。</p>
        </div>
        <div v-show="!isEmpty" ref="chartRef" class="pie-chart" aria-label="满意度分布饼图"></div>
      </article>

      <article class="analysis-card trend-card" v-loading="trendLoading">
        <div class="trend-title">
          <div>
            <span>趋势变化</span>
            <h2>{{ trendTitle }}</h2>
          </div>
          <strong>{{ trendTotal }} 条</strong>
        </div>
        <div class="trend-controls" aria-label="趋势筛选">
          <button type="button" :class="{ active: trendMode === 'year' }" @click="showYearTrend">全年</button>
          <el-select v-model="selectedMonth" placeholder="选择月份" @change="showMonthTrend">
            <el-option v-for="item in monthOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div v-if="isTrendEmpty" class="trend-empty">
          <strong>暂无决策记录</strong>
          <p>{{ trendEmptyText }}</p>
        </div>
        <div v-show="!isTrendEmpty" ref="trendChartRef" class="trend-chart" aria-label="每月创建决策数量折线图"></div>
      </article>

      <article class="analysis-card mood-card" v-loading="moodLoading">
        <div class="mood-title">
          <div>
            <span>情绪关联</span>
            <h2>情绪满意度图</h2>
          </div>
          <strong>{{ moodTotal }} 条</strong>
        </div>
        <div v-if="moodTotal === 0" class="mood-empty">
          <strong>暂无情绪回测结果</strong>
          <p>完成带有心情和满意度的回测后，这里会展示关联分布。</p>
        </div>
        <div v-else class="mood-bars" aria-label="情绪满意度统计">
          <div v-for="row in moodSatisfactionRows" :key="row.mood" class="mood-bar-row">
            <strong class="mood-name">{{ row.mood }}</strong>
            <div class="mood-result-list">
              <div v-for="item in row.items" :key="item.label" class="mood-result" :class="`tone-${item.key}`">
                <span>{{ item.label }}</span>
                <i :style="{ '--bar-width': item.width }"></i>
                <em>{{ item.value }}</em>
              </div>
            </div>
          </div>
        </div>
      </article>

      <article class="analysis-card tag-card" v-loading="tagLoading">
        <div class="tag-title">
          <div>
            <span>标签偏好</span>
            <h2>标签分布图</h2>
          </div>
          <strong>{{ tagTotal }} 次</strong>
        </div>
        <div v-if="isTagEmpty" class="tag-empty">
          <strong>暂无标签数据</strong>
          <p>记录决策标签后，这里会展示各标签出现次数。</p>
        </div>
        <div v-show="!isTagEmpty" ref="tagChartRef" class="tag-chart" aria-label="标签分布柱状图"></div>
      </article>
    </section>
  </main>
</template>

<script setup>
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMoodSatisfaction, getSatisfactionPie, getTagBar, getTrendLine } from '../../api/analysis'
import { useAuthStore } from '../../store/auth'

use([PieChart, LineChart, BarChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const router = useRouter()
const authStore = useAuthStore()
const chartRef = ref(null)
const trendChartRef = ref(null)
const tagChartRef = ref(null)
const loading = ref(false)
const trendLoading = ref(false)
const moodLoading = ref(false)
const tagLoading = ref(false)
const pieData = ref([])
const trendData = ref({ labels: [], counts: [], granularity: 'month', selectedMonth: '' })
const moodData = ref({ total: 0, items: [] })
const tagData = ref([])
const filters = ref({
  tag: '',
  mood: ''
})
const selectedMonth = ref('')
let chartInstance = null
let trendChartInstance = null
let tagChartInstance = null

const tagOptions = ['学习', '消费', '工作', '生活', '健康']
const moodOptions = ['平静', '焦虑', '纠结', '兴奋', '冲动']
const satisfactionOptions = [
  { label: '满意', key: 'good' },
  { label: '一般', key: 'normal' },
  { label: '后悔', key: 'bad' }
]

const total = computed(() => pieData.value.reduce((sum, item) => sum + item.value, 0))
const isEmpty = computed(() => total.value === 0)
const trendTotal = computed(() => trendData.value.counts.reduce((sum, value) => sum + value, 0))
const moodTotal = computed(() => moodData.value.total || 0)
const tagTotal = computed(() => tagData.value.reduce((sum, item) => sum + item.value, 0))
const isTrendEmpty = computed(() => trendTotal.value === 0)
const isTagEmpty = computed(() => tagTotal.value === 0)
const trendMode = computed(() => trendData.value.granularity === 'day' ? 'month' : 'year')
const trendTitle = computed(() => trendMode.value === 'month' ? `${trendData.value.selectedMonth} 每日趋势` : '趋势折线图')
const trendEmptyText = computed(() => trendMode.value === 'month' ? '当前月份暂无决策记录。' : '创建决策后，这里会展示每月新增数量。')
const monthOptions = computed(() => trendData.value.granularity === 'month' ? trendData.value.labels : yearMonthOptions())
const displayName = computed(() => authStore.user?.nickname || authStore.user?.username || '朋友')
const avatarUrl = computed(() => authStore.user?.avatarUrl || '')
const avatarInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())
const moodSatisfactionRows = computed(() => {
  const max = Math.max(
    ...moodData.value.items.flatMap((row) => satisfactionOptions.map((item) => row.satisfaction?.[item.label] || 0)),
    1
  )
  return moodData.value.items.map((row) => ({
    mood: row.mood,
    items: satisfactionOptions.map((item) => {
      const value = row.satisfaction?.[item.label] || 0
      return {
        ...item,
        value,
        width: `${Math.max(value === 0 ? 0 : 14, Math.round((value / max) * 100))}%`
      }
    })
  }))
})

const chartOptions = computed(() => ({
  color: ['#7edbf1', '#ff9ac2', '#ffd6e7'],
  tooltip: {
    trigger: 'item',
    formatter: '{b}<br/>{c} 条 ({d}%)'
  },
  legend: {
    bottom: 0,
    left: 'center',
    itemWidth: 10,
    itemHeight: 10,
    textStyle: {
      color: '#52606d',
      fontSize: 13
    }
  },
  series: [
    {
      name: '满意度',
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      label: {
        formatter: '{b}\n{d}%',
        color: '#1f2933',
        fontSize: 13,
        fontWeight: 700
      },
      labelLine: {
        length: 12,
        length2: 8
      },
      data: pieData.value
    }
  ]
}))

const trendOptions = computed(() => ({
  color: ['#ff76a7'],
  grid: {
    top: 28,
    right: 14,
    bottom: 30,
    left: 34
  },
  tooltip: {
    trigger: 'axis',
    formatter(params) {
      const point = params?.[0]
      return point ? `${point.axisValue}<br/>${point.data} 条` : ''
    }
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendData.value.labels,
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#d7dee8' } },
    axisLabel: { color: '#64748b', fontSize: 12 }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisLabel: { color: '#64748b', fontSize: 12 },
    splitLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.16)', type: 'dashed' } }
  },
  series: [
    {
      name: '创建数量',
      type: 'line',
      smooth: true,
      symbolSize: 8,
      lineStyle: { width: 3 },
      areaStyle: { color: 'rgba(255, 118, 167, 0.16)' },
      data: trendData.value.counts
    }
  ]
}))

const tagOptionsConfig = computed(() => ({
  color: ['#7edbf1'],
  grid: {
    top: 24,
    right: 16,
    bottom: 34,
    left: 40
  },
  tooltip: {
    trigger: 'axis',
    formatter(params) {
      const point = params?.[0]
      return point ? `${point.name}<br/>${point.data} 次` : ''
    }
  },
  xAxis: {
    type: 'category',
    data: tagData.value.map((item) => item.name),
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#d7dee8' } },
    axisLabel: { color: '#64748b', fontSize: 12 }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisLabel: { color: '#64748b', fontSize: 12 },
    splitLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.16)', type: 'dashed' } }
  },
  series: [
    {
      name: '标签次数',
      type: 'bar',
      barMaxWidth: 42,
      itemStyle: { borderRadius: [12, 12, 4, 4] },
      data: tagData.value.map((item) => item.value)
    }
  ]
}))

async function loadSatisfactionPie() {
  loading.value = true
  try {
    const result = await getSatisfactionPie({
      tag: filters.value.tag || undefined,
      mood: filters.value.mood || undefined
    })
    pieData.value = result.items
  } finally {
    loading.value = false
  }
}

async function loadTrendLine() {
  trendLoading.value = true
  try {
    const result = await getTrendLine({
      month: selectedMonth.value || undefined
    })
    trendData.value = {
      labels: result.labels || [],
      counts: result.counts || [],
      granularity: result.granularity || 'month',
      selectedMonth: result.selectedMonth || ''
    }
  } catch (error) {
    trendData.value = { labels: [], counts: [], granularity: 'month', selectedMonth: '' }
  } finally {
    trendLoading.value = false
  }
}

async function loadMoodSatisfaction() {
  moodLoading.value = true
  try {
    moodData.value = await getMoodSatisfaction()
  } catch (error) {
    moodData.value = { total: 0, items: [] }
  } finally {
    moodLoading.value = false
  }
}

async function loadTagBar() {
  tagLoading.value = true
  try {
    const result = await getTagBar()
    tagData.value = result.items || []
  } catch (error) {
    tagData.value = []
  } finally {
    tagLoading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || isEmpty.value) {
    chartInstance?.clear()
    return
  }
  if (!chartInstance) {
    chartInstance = init(chartRef.value)
  }
  chartInstance.setOption(chartOptions.value)
}

function renderTrendChart() {
  if (!trendChartRef.value || isTrendEmpty.value) {
    trendChartInstance?.clear()
    return
  }
  if (!trendChartInstance) {
    trendChartInstance = init(trendChartRef.value)
    trendChartInstance.on('click', (params) => {
      if (trendData.value.granularity !== 'month' || !params?.name) {
        return
      }
      selectedMonth.value = params.name
      loadTrendLine()
    })
  }
  trendChartInstance.setOption(trendOptions.value)
}

function renderTagChart() {
  if (!tagChartRef.value || isTagEmpty.value) {
    tagChartInstance?.clear()
    return
  }
  if (!tagChartInstance) {
    tagChartInstance = init(tagChartRef.value)
  }
  tagChartInstance.setOption(tagOptionsConfig.value)
}

function showYearTrend() {
  selectedMonth.value = ''
  loadTrendLine()
}

function showMonthTrend() {
  loadTrendLine()
}

function yearMonthOptions() {
  const year = new Date().getFullYear()
  return Array.from({ length: 12 }, (_, index) => `${year}-${String(index + 1).padStart(2, '0')}`)
}

function resizeChart() {
  chartInstance?.resize()
  trendChartInstance?.resize()
  tagChartInstance?.resize()
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

watch(pieData, async () => {
  await nextTick()
  renderChart()
})

watch(trendData, async () => {
  await nextTick()
  renderTrendChart()
})

watch(tagData, async () => {
  await nextTick()
  renderTagChart()
})

onMounted(async () => {
  await Promise.allSettled([loadSatisfactionPie(), loadTrendLine(), loadMoodSatisfaction(), loadTagBar(), authStore.loadCurrentUser().catch(() => null)])
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
  trendChartInstance?.dispose()
  tagChartInstance?.dispose()
  chartInstance = null
  trendChartInstance = null
  tagChartInstance = null
})
</script>

<style scoped>
.analysis-page {
  --analysis-glass-border: rgba(255, 255, 255, 0.72);
  --analysis-glass-shadow: 0 24px 60px rgba(54, 73, 100, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.74);
  --analysis-soft-shadow: 0 16px 34px rgba(55, 70, 96, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.7);
  --analysis-text: #111923;
  --analysis-muted: rgba(38, 52, 68, 0.74);
  min-height: 100vh;
  overflow-x: hidden;
  padding: 28px clamp(16px, 4vw, 48px) 56px;
  background:
    radial-gradient(circle at 8% 8%, rgba(255, 154, 194, 0.28), transparent 28%),
    radial-gradient(circle at 92% 0, rgba(126, 219, 241, 0.28), transparent 32%),
    linear-gradient(135deg, #f8fbff 0%, #eefaff 48%, #fff6fb 100%);
  color: var(--analysis-text);
}

.analysis-header,
.analysis-grid {
  width: min(1120px, 100%);
  margin: 0 auto;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  padding: 6px 0 24px;
}

.analysis-header > div:first-child > span,
.card-title span,
.placeholder-card span {
  color: #ff6f91;
  font-size: 13px;
  font-weight: 900;
}

.analysis-header h1 {
  margin: 8px 0 0;
  font-size: clamp(32px, 5vw, 52px);
  line-height: 1.08;
  letter-spacing: 0;
  text-shadow: 0 14px 34px rgba(33, 44, 66, 0.12);
}

.analysis-header p,
.placeholder-card p,
.analysis-empty p {
  margin: 8px 0 0;
  color: var(--analysis-muted);
  line-height: 1.7;
}

.analysis-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  padding-top: 14px;
}

.top-view-nav,
.avatar-nav-button,
.top-icon-button,
.analysis-card,
.mood-bar-row {
  border: 1px solid var(--analysis-glass-border);
  background:
    linear-gradient(132deg, rgba(255, 255, 255, 0.54), rgba(215, 248, 255, 0.38) 46%, rgba(255, 223, 239, 0.36)),
    rgba(255, 255, 255, 0.3);
  box-shadow: var(--analysis-glass-shadow);
  backdrop-filter: blur(22px) saturate(1.18);
  -webkit-backdrop-filter: blur(22px) saturate(1.18);
}

.top-view-nav {
  display: inline-flex;
  min-height: 48px;
  gap: 4px;
  align-items: center;
  padding: 5px;
  border-radius: 999px;
}

.top-view-nav button,
.trend-controls button {
  min-height: 38px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: rgba(34, 47, 61, 0.72);
  cursor: pointer;
  font: inherit;
  font-weight: 800;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease, color 180ms ease;
}

.top-view-nav button {
  padding: 0 18px;
}

.top-view-nav button:hover,
.top-view-nav button:focus-visible,
.trend-controls button:hover,
.trend-controls button:focus-visible {
  outline: none;
  border-color: rgba(255, 255, 255, 0.9);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.68), rgba(214, 249, 255, 0.48)),
    rgba(255, 255, 255, 0.34);
  box-shadow: 0 12px 26px rgba(54, 73, 100, 0.14), inset 0 1px 0 rgba(255, 255, 255, 0.82);
  transform: translateY(-1px);
}

.top-view-nav button:active,
.trend-controls button:active {
  box-shadow: inset 0 2px 8px rgba(61, 89, 115, 0.1);
  transform: translateY(1px) scale(0.98);
}

.top-view-nav button.active,
.trend-controls button.active {
  border-color: rgba(255, 255, 255, 0.78);
  background:
    linear-gradient(135deg, rgba(255, 118, 167, 0.96), rgba(255, 154, 194, 0.9) 46%, rgba(126, 219, 241, 0.82)),
    rgba(255, 255, 255, 0.34);
  color: #ffffff;
  box-shadow:
    0 18px 36px rgba(251, 114, 153, 0.24),
    0 8px 18px rgba(0, 174, 236, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.55);
}

.avatar-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 0;
  padding-bottom: 0;
}

.avatar-nav-button {
  display: inline-flex;
  width: auto;
  min-width: 72px;
  height: 44px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  overflow: visible;
  padding: 4px 10px 4px 5px;
  border-radius: 999px;
  color: #172636;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease;
}

.avatar-frame {
  display: inline-grid;
  width: 36px;
  height: 36px;
  place-items: center;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(218, 249, 255, 0.74), rgba(255, 223, 239, 0.56));
  color: #0b8ec2;
  font-weight: 900;
  box-shadow: 0 8px 18px rgba(40, 64, 88, 0.14);
}

.avatar-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-chevron {
  width: 7px;
  height: 7px;
  border-right: 2px solid rgba(23, 38, 54, 0.72);
  border-bottom: 2px solid rgba(23, 38, 54, 0.72);
  transform: translateY(-2px) rotate(45deg);
}

.top-icon-button {
  display: inline-grid;
  width: 42px;
  height: 42px;
  place-items: center;
  padding: 0;
  border-radius: 13px;
  color: #bf3e52;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease;
}

.avatar-nav-button:hover,
.avatar-nav-button:focus-visible,
.top-icon-button:hover,
.top-icon-button:focus-visible {
  outline: none;
  border-color: rgba(255, 255, 255, 0.9);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.68), rgba(214, 249, 255, 0.48)),
    rgba(255, 255, 255, 0.34);
  box-shadow: 0 18px 38px rgba(54, 73, 100, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.82);
  transform: translateY(-1px);
}

.top-icon-button:hover,
.top-icon-button:focus-visible {
  border-color: rgba(255, 146, 158, 0.42);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.68), rgba(255, 224, 229, 0.48)),
    rgba(255, 244, 246, 0.46);
  color: #aa3045;
}

.avatar-nav-button:active,
.top-icon-button:active {
  transform: translateY(1px) scale(0.98);
}

.logout-icon {
  position: relative;
  width: 17px;
  height: 17px;
  border: 2px solid currentColor;
  border-right: 0;
  border-radius: 4px 0 0 4px;
}

.logout-icon::before {
  position: absolute;
  top: 50%;
  right: -8px;
  width: 12px;
  height: 2px;
  border-radius: 999px;
  background: currentColor;
  content: "";
  transform: translateY(-50%);
}

.logout-icon::after {
  position: absolute;
  top: 50%;
  right: -9px;
  width: 7px;
  height: 7px;
  border-top: 2px solid currentColor;
  border-right: 2px solid currentColor;
  content: "";
  transform: translateY(-50%) rotate(45deg);
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  align-items: stretch;
}

.analysis-card {
  border-radius: 18px;
  background:
    linear-gradient(140deg, rgba(255, 255, 255, 0.48), rgba(211, 246, 255, 0.32) 45%, rgba(255, 224, 239, 0.34)),
    rgba(255, 255, 255, 0.22);
}

.pie-card {
  min-height: 520px;
  padding: clamp(20px, 3vw, 28px);
}

.card-title {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
}

.card-title h2,
.placeholder-card h2,
.trend-title h2,
.mood-title h2,
.tag-title h2 {
  margin: 6px 0 0;
  color: var(--analysis-text);
  font-size: 24px;
  line-height: 1.2;
  letter-spacing: 0;
}

.card-title strong,
.trend-title strong,
.mood-title strong,
.tag-title strong {
  flex: 0 0 auto;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(206, 246, 255, 0.84), rgba(255, 255, 255, 0.62));
  color: #008fc6;
  font-size: 14px;
  box-shadow: 0 10px 24px rgba(65, 82, 106, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.trend-title,
.tag-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.trend-title span,
.mood-title span,
.tag-title span {
  color: #ff6f91;
  font-size: 13px;
  font-weight: 900;
}

.trend-controls {
  display: grid;
  grid-template-columns: 92px minmax(180px, 260px);
  gap: 12px;
  align-items: center;
  margin-top: 14px;
}

.trend-controls :deep(.el-select__wrapper) {
  min-height: 42px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 0 0 1px rgba(160, 211, 230, 0.34) inset;
}

.analysis-filters {
  display: grid;
  grid-template-columns: repeat(2, minmax(160px, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.analysis-filters :deep(.el-select__wrapper) {
  min-height: 42px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 0 0 1px rgba(160, 211, 230, 0.34) inset;
}

.pie-chart {
  width: 100%;
  height: 420px;
  margin-top: 18px;
}

.analysis-empty {
  display: grid;
  min-height: 380px;
  place-content: center;
  padding: 28px;
  border: 1px dashed rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.46), rgba(218, 249, 255, 0.28)),
    rgba(255, 255, 255, 0.18);
  text-align: center;
}

.analysis-empty strong {
  color: #171d24;
  font-size: 22px;
}

.trend-card {
  min-height: 320px;
  padding: 22px;
}

.trend-chart {
  width: 100%;
  height: 230px;
  margin-top: 16px;
}

.trend-empty {
  display: grid;
  min-height: 230px;
  place-content: center;
  border: 1px dashed rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.46), rgba(255, 225, 240, 0.28), rgba(218, 249, 255, 0.34)),
    rgba(255, 255, 255, 0.18);
  text-align: center;
}

.trend-empty strong {
  color: #171d24;
  font-size: 18px;
}

.trend-empty p {
  margin: 8px 0 0;
  color: #52606d;
  line-height: 1.6;
}

.tag-card {
  min-height: 320px;
  padding: 22px;
}

.tag-chart {
  width: 100%;
  height: 240px;
  margin-top: 16px;
}

.tag-empty {
  display: grid;
  min-height: 240px;
  place-content: center;
  border: 1px dashed rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.46), rgba(255, 225, 240, 0.28), rgba(218, 249, 255, 0.34)),
    rgba(255, 255, 255, 0.18);
  text-align: center;
}

.tag-empty strong {
  color: #171d24;
  font-size: 18px;
}

.tag-empty p {
  margin: 8px 0 0;
  color: #52606d;
  line-height: 1.6;
}

.mood-card {
  min-height: 320px;
  padding: 22px;
}

.mood-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.mood-bars {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.mood-bar-row {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  padding: 12px;
  border-radius: 14px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.46), rgba(218, 249, 255, 0.3)),
    rgba(255, 255, 255, 0.18);
  box-shadow: var(--analysis-soft-shadow);
}

.mood-name {
  color: #171d24;
  font-size: 15px;
}

.mood-result-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.mood-result {
  display: grid;
  grid-template-columns: 34px minmax(46px, 1fr) 22px;
  gap: 7px;
  align-items: center;
  min-width: 0;
  color: #52606d;
  font-size: 13px;
  font-weight: 800;
}

.mood-result i {
  display: block;
  height: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(222, 232, 238, 0.82);
}

.mood-result i::before {
  display: block;
  width: var(--bar-width);
  height: 100%;
  min-width: 0;
  border-radius: inherit;
  content: "";
}

.mood-result.tone-good i::before {
  background: #7edbf1;
}

.mood-result.tone-normal i::before {
  background: #ff9ac2;
}

.mood-result.tone-bad i::before {
  background: #ffb4c8;
}

.mood-result em {
  color: #171d24;
  font-style: normal;
  text-align: right;
}

.mood-empty {
  display: grid;
  min-height: 220px;
  place-content: center;
  border: 1px dashed rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.46), rgba(255, 225, 240, 0.28), rgba(218, 249, 255, 0.34)),
    rgba(255, 255, 255, 0.18);
  text-align: center;
}

.mood-empty strong {
  color: #171d24;
  font-size: 18px;
}

.mood-empty p {
  margin: 8px 0 0;
  color: #52606d;
  line-height: 1.6;
}

.placeholder-card {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  min-height: 150px;
  padding: 20px;
}

.placeholder-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 18px;
  background: #fff0b8;
  color: #171d24;
  font-size: 24px;
  font-weight: 900;
}

@media (max-width: 860px) {
  .analysis-header {
    display: grid;
  }

  .analysis-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .top-view-nav {
    flex: 1 1 auto;
  }

  .pie-card {
    min-height: 460px;
  }
}

@media (max-width: 560px) {
  .analysis-page {
    padding: 18px 12px 36px;
  }

  .analysis-header {
    gap: 14px;
  }

  .analysis-actions {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 10px;
    align-items: center;
  }

  .top-view-nav {
    width: 100%;
    min-width: 0;
  }

  .top-view-nav button {
    flex: 1 1 0;
    min-width: 0;
    padding: 0 10px;
  }

  .card-title {
    display: grid;
  }

  .analysis-filters {
    grid-template-columns: 1fr;
  }

  .trend-controls {
    grid-template-columns: 1fr;
  }

  .mood-bar-row,
  .mood-result-list {
    grid-template-columns: 1fr;
  }

  .mood-result {
    grid-template-columns: 34px minmax(0, 1fr) 22px;
  }

  .pie-chart {
    height: 340px;
  }

  .placeholder-card {
    grid-template-columns: 1fr;
  }
}
</style>
