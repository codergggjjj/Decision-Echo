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

        <div v-if="isEmpty" class="analysis-empty">
          <strong>暂无已回测结果</strong>
          <p>完成决策回测后，这里会展示满意、一般、后悔的占比。</p>
        </div>
        <div v-show="!isEmpty" ref="chartRef" class="pie-chart" aria-label="满意度分布饼图"></div>
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
import { PieChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getSatisfactionPie } from '../../api/analysis'
import { useAuthStore } from '../../store/auth'

use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

const router = useRouter()
const authStore = useAuthStore()
const chartRef = ref(null)
const loading = ref(false)
const pieData = ref([])
let chartInstance = null

const placeholderCards = [
  { kicker: '趋势变化', title: '趋势折线图', icon: '↗' },
  { kicker: '标签偏好', title: '标签分布图', icon: '#' },
  { kicker: '情绪关联', title: '情绪满意度图', icon: '♡' }
]

const total = computed(() => pieData.value.reduce((sum, item) => sum + item.value, 0))
const isEmpty = computed(() => total.value === 0)
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

async function loadSatisfactionPie() {
  loading.value = true
  try {
    const result = await getSatisfactionPie()
    pieData.value = result.items
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || isEmpty.value) {
    return
  }
  if (!chartInstance) {
    chartInstance = init(chartRef.value)
  }
  chartInstance.setOption(chartOptions.value)
}

function resizeChart() {
  chartInstance?.resize()
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

onMounted(async () => {
  await Promise.all([loadSatisfactionPie(), authStore.loadCurrentUser().catch(() => null)])
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
  chartInstance = null
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
  grid-template-columns: minmax(0, 1.45fr) minmax(260px, 0.55fr);
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
  grid-row: span 3;
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
.placeholder-card h2 {
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

  .analysis-grid {
    grid-template-columns: 1fr;
  }

  .pie-card {
    grid-row: auto;
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

  .pie-chart {
    height: 340px;
  }

  .placeholder-card {
    grid-template-columns: 1fr;
  }
}
</style>
