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
            <img v-if="avatarUrl" :src="avatarUrl" alt="用户头像" />
            <span v-else>{{ avatarInitial }}</span>
          </button>
          <div class="avatar-dropdown">
            <button type="button" @click="goProfile">个人中心</button>
            <button type="button" @click="handleLogout">退出登录</button>
          </div>
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

      <article v-for="item in placeholderCards" :key="item.title" class="analysis-card placeholder-card">
        <div class="placeholder-icon">{{ item.icon }}</div>
        <div>
          <span>{{ item.kicker }}</span>
          <h2>{{ item.title }}</h2>
          <p>图表区域已预留，后续版本接入数据。</p>
        </div>
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
import { getSatisfactionPie, getTagBar, getTrendLine } from '../../api/analysis'
import { useAuthStore } from '../../store/auth'

use([PieChart, LineChart, BarChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const router = useRouter()
const authStore = useAuthStore()
const chartRef = ref(null)
const trendChartRef = ref(null)
const tagChartRef = ref(null)
const loading = ref(false)
const trendLoading = ref(false)
const tagLoading = ref(false)
const pieData = ref([])
const trendData = ref({ labels: [], counts: [], granularity: 'month', selectedMonth: '' })
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
const placeholderCards = [
  { kicker: '情绪关联', title: '情绪满意度图', icon: '♡' }
]

const total = computed(() => pieData.value.reduce((sum, item) => sum + item.value, 0))
const isEmpty = computed(() => total.value === 0)
const trendTotal = computed(() => trendData.value.counts.reduce((sum, value) => sum + value, 0))
const isTrendEmpty = computed(() => trendTotal.value === 0)
const tagTotal = computed(() => tagData.value.reduce((sum, item) => sum + item.value, 0))
const isTagEmpty = computed(() => tagTotal.value === 0)
const trendMode = computed(() => trendData.value.granularity === 'day' ? 'month' : 'year')
const trendTitle = computed(() => trendMode.value === 'month' ? `${trendData.value.selectedMonth} 每日趋势` : '趋势折线图')
const trendEmptyText = computed(() => trendMode.value === 'month' ? '当前月份暂无决策记录。' : '创建决策后，这里会展示每月新增数量。')
const monthOptions = computed(() => trendData.value.granularity === 'month' ? trendData.value.labels : yearMonthOptions())
const displayName = computed(() => authStore.user?.nickname || authStore.user?.username || '朋友')
const avatarUrl = computed(() => authStore.user?.avatarUrl || '')
const avatarInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())

const chartOptions = computed(() => ({
  color: ['#69d9b4', '#ffd166', '#ff8ba7'],
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
  color: ['#6c8cff'],
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
      areaStyle: { color: 'rgba(108, 140, 255, 0.14)' },
      data: trendData.value.counts
    }
  ]
}))

const tagOptionsConfig = computed(() => ({
  color: ['#ff9f7a'],
  grid: {
    top: 24,
    right: 16,
    bottom: 34,
    left: 40
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
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
  await Promise.allSettled([loadSatisfactionPie(), loadTrendLine(), loadTagBar(), authStore.loadCurrentUser().catch(() => null)])
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
  min-height: 100vh;
  overflow-x: hidden;
  padding: 28px clamp(16px, 4vw, 48px) 56px;
  background:
    radial-gradient(circle at top left, rgba(105, 217, 180, 0.2), transparent 30%),
    linear-gradient(135deg, #fffaf2 0%, #f7fbff 52%, #fff6fa 100%);
  color: #11161c;
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
}

.analysis-header p,
.placeholder-card p,
.analysis-empty p {
  margin: 8px 0 0;
  color: #52606d;
  line-height: 1.7;
}

.analysis-actions {
  flex: 0 0 auto;
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 18px;
  align-items: stretch;
}

.analysis-card {
  border: 1px solid rgba(23, 29, 36, 0.08);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 44px rgba(23, 29, 36, 0.08);
}

.pie-card {
  min-height: 520px;
  padding: clamp(18px, 3vw, 28px);
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
.tag-title h2 {
  margin: 6px 0 0;
  color: #171d24;
  font-size: 24px;
  line-height: 1.2;
  letter-spacing: 0;
}

.card-title strong {
  flex: 0 0 auto;
  padding: 8px 12px;
  border-radius: 999px;
  background: #e9fff7;
  color: #0f8f6b;
  font-size: 14px;
}

.trend-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.trend-title span,
.tag-title span {
  color: #ff6f91;
  font-size: 13px;
  font-weight: 900;
}

.trend-title strong,
.tag-title strong {
  flex: 0 0 auto;
  padding: 7px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f63d8;
  font-size: 13px;
}

.tag-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.trend-controls {
  display: grid;
  grid-template-columns: 92px minmax(180px, 260px);
  gap: 12px;
  align-items: center;
  margin-top: 14px;
}

.trend-controls button {
  min-height: 42px;
  border: 1px solid rgba(23, 29, 36, 0.12);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  color: #52606d;
  cursor: pointer;
  font: inherit;
  font-weight: 800;
}

.trend-controls button.active {
  border-color: #6c8cff;
  background: #eef2ff;
  color: #4f63d8;
}

.trend-controls :deep(.el-select__wrapper) {
  min-height: 42px;
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(23, 29, 36, 0.08) inset;
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
  box-shadow: 0 0 0 1px rgba(23, 29, 36, 0.08) inset;
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
  text-align: center;
}

.analysis-empty strong {
  color: #171d24;
  font-size: 22px;
}

.trend-card {
  min-height: 320px;
  padding: 20px;
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
  padding: 20px;
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
    justify-content: flex-start;
  }

  .pie-card {
    min-height: 460px;
  }
}

@media (max-width: 560px) {
  .analysis-page {
    padding: 18px 12px 36px;
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

  .pie-chart {
    height: 340px;
  }

  .placeholder-card {
    grid-template-columns: 1fr;
  }
}
</style>
