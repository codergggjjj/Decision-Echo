<template>
  <main class="dashboard-page youth-dashboard content-first-dashboard" v-loading="loading">
    <header class="memory-header">
      <div>
        <h1>决策回声</h1>
        <p>{{ greeting }}，先看过去留下的记录，再决定要不要补一条新的。</p>
      </div>
      <div class="memory-actions">
        <nav class="top-view-nav" aria-label="页面切换">
          <button type="button" class="active" @click="goDashboard">决策记录</button>
          <button type="button" @click="goAnalysis">图表分析</button>
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

    <section class="memory-metrics" aria-label="决策统计">
      <article v-for="item in overviewCards" :key="item.label" class="metric-chip" :class="`tone-${item.tone}`">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </section>

    <section class="memory-layout">
      <section class="history-stage">
        <div class="section-title memory-title">
          <div>
            <span>我的回看流</span>
            <h2>过去的记录</h2>
          </div>
        </div>

        <section class="decision-search-panel">
          <div class="decision-search-main">
            <label class="decision-search-field">
              <span>决策名</span>
              <el-input
                v-model.trim="searchForm.keyword"
                clearable
                placeholder="搜决策名，比如 周末课程"
                @keyup.enter="executeDecisionSearch"
              />
            </label>
            <label class="decision-search-field compact">
              <span>标签</span>
              <el-select v-model="searchForm.tag" clearable placeholder="全部标签">
                <el-option v-for="item in tagOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </label>
          </div>
          <div class="decision-search-actions">
            <div class="decision-status-filter" aria-label="决策状态筛选">
              <button
                v-for="item in statusFilterOptions"
                :key="item.value"
                type="button"
                :class="{ active: searchForm.status === item.value }"
                @click="selectDecisionStatus(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
            <div class="decision-search-buttons">
              <el-button :loading="isSearching" class="primary-action search-submit" type="primary" @click="executeDecisionSearch">查询</el-button>
              <el-button :disabled="(!hasSearchFilters && !isSearchMode) || isSearching" @click="resetDecisionSearch">清空</el-button>
            </div>
          </div>
        </section>

        <div v-if="recentDecisions.length === 0" class="empty-state youth-empty">
          <strong>{{ isSearchMode ? '没有找到匹配的决策' : '还没有决策记录' }}</strong>
          <p>{{ isSearchMode ? '换个关键词、标签或状态再试试。' : '从一个小选择开始，比如要不要买某样东西、报名某个课程。' }}</p>
        </div>

        <div v-else class="memory-feed">
          <article
            v-for="decision in pagedRecentDecisions"
            :key="decision.id"
            class="memory-item clickable"
            :class="statusClass(decision.status)"
            tabindex="0"
            @click="openDetail(decision)"
            @keydown.enter.self="openDetail(decision)"
          >
            <div class="memory-date">
              <span>{{ formatDate(decision.reviewTime).slice(5, 10) }}</span>
              <small>{{ urgencyText(decision.urgency) }}</small>
            </div>
            <div class="memory-body">
              <div class="memory-head">
                <span :class="['status-pill', statusClass(decision.status)]">{{ statusText(decision.status) }}</span>
                <i>{{ decision.tags || '未分类' }}</i>
                <button
                  v-if="decision.status !== 'reviewed'"
                  type="button"
                  class="featured-review"
                  @click.stop="openReview(decision)"
                >
                  现在回看
                </button>
              </div>
              <h3>{{ decision.title }}</h3>
              <p>{{ decision.context || decision.reason || '这条记录还没有补充更多内容。' }}</p>
              <div v-if="optionSummary(decision.options).selected" class="selected-option-preview">
                <span>最终选择</span>
                <strong>{{ optionSummary(decision.options).selected }}</strong>
                <i v-if="optionSummary(decision.options).selectedStrategy">{{ optionSummary(decision.options).selectedStrategy }}</i>
              </div>
              <div v-if="optionSummary(decision.options).items.length" class="option-mini-tree">
                <div
                  v-for="item in optionSummary(decision.options).items"
                  :key="item.id || item.title"
                  class="option-mini-item"
                  :class="{ selected: item.title === optionSummary(decision.options).selected }"
                >
                  <span>
                    {{ item.title }}
                    <i v-if="item.strategy">{{ item.strategy }}</i>
                  </span>
                  <small v-if="item.children?.length">{{ item.children.map((child) => child.title).join(' / ') }}</small>
                </div>
              </div>
              <div class="memory-foot">
                <span>回看时间 {{ formatDate(decision.reviewTime) }}</span>
                <div class="memory-foot-actions">
                  <button type="button" class="inline-detail" @click.stop="openDetail(decision)">查看详情</button>
                  <button type="button" class="inline-delete" :disabled="deletingDecisionId === decision.id" @click.stop="confirmDelete(decision)">删除</button>
                </div>
              </div>
            </div>
          </article>
          <el-pagination
            v-if="recentDecisions.length > decisionPageSize"
            v-model:current-page="decisionPage"
            :page-size="decisionPageSize"
            :total="recentDecisions.length"
            background
            class="decision-pagination"
            layout="prev, pager, next"
          />
        </div>
      </section>

      <aside class="review-dock">
        <section class="dock-panel highlight-review">
          <div class="section-title memory-title">
            <div>
              <span>待处理</span>
              <h2>待回看</h2>
            </div>
            <strong>{{ pendingReview.length }}</strong>
          </div>

          <div v-if="pendingReview.length === 0" class="mini-empty">没有积压，状态很轻。</div>
          <div v-else class="review-stack">
            <article v-for="decision in pendingReview" :key="decision.id" class="review-ticket">
              <span>{{ formatDate(decision.reviewTime) }}</span>
              <h3>{{ decision.title }}</h3>
              <button type="button" @click="openReview(decision)">补回测</button>
            </article>
          </div>
        </section>

        <section class="dock-panel">
          <div class="section-title memory-title">
            <div>
              <span>复盘心情</span>
              <h2>回测结果</h2>
            </div>
          </div>
          <div class="satisfaction-stack compact">
            <div v-for="item in satisfactionBars" :key="item.label" class="mood-row" :class="`mood-${item.key}`">
              <div>
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
              <div class="mood-track"><i :style="{ width: item.width }"></i></div>
            </div>
          </div>
        </section>
      </aside>
    </section>

    <button type="button" class="floating-create" @click="openCreateDialog">
      <span>记录新决定</span>
      <strong>+</strong>
    </button>

    <el-dialog v-model="createDialogVisible" align-center class="youth-dialog create-decision-dialog" draggable title="记录新决定" width="760px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" class="create-decision-form" label-position="top">
        <section class="create-form-section section-basic">
          <div class="create-section-heading">
            <span>01</span>
            <div>
              <h3>基础信息</h3>
              <p>先把这次选择的基本语境留下来。</p>
            </div>
          </div>
          <el-form-item label="决策标题" prop="title">
            <el-input v-model.trim="createForm.title" placeholder="例如：是否报名周末课程" />
          </el-form-item>
          <el-form-item label="背景">
            <el-input v-model.trim="createForm.context" type="textarea" :rows="3" placeholder="可选：这件事的背景是什么？" />
          </el-form-item>
          <div class="form-grid">
            <el-form-item label="标签">
              <el-select v-model="createForm.tags" clearable placeholder="选择标签">
                <el-option v-for="item in tagOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="心情">
              <el-select v-model="createForm.mood" clearable placeholder="选择心情">
                <el-option v-for="item in moodOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </div>
        </section>

        <section class="create-form-section section-options">
          <div class="create-section-heading">
            <span>02</span>
            <div>
              <h3>候选方案</h3>
              <p>至少保留两个方案，并标记这次最终选择哪一个。</p>
            </div>
          </div>
          <div class="option-builder">
            <article
              v-for="(option, optionIndex) in optionTree"
              :key="option.id"
              class="option-editor-card"
              :class="{ selected: selectedOptionId === option.id }"
            >
              <div class="option-editor-head">
                <div class="option-card-title">
                  <span class="option-index">方案 {{ optionIndex + 1 }}</span>
                  <label class="option-choice">
                    <input v-model="selectedOptionId" type="radio" :value="option.id" />
                    <span>最终选择</span>
                  </label>
                </div>
                <button type="button" class="text-action danger" @click="removeOption(option.id)">删除方案</button>
              </div>
              <div class="option-main-row">
                <el-input v-model.trim="option.title" :placeholder="`候选方案 ${optionIndex + 1}`" />
                <el-select v-model="option.strategy" class="option-strategy-select" placeholder="方案标签">
                  <el-option v-for="item in optionStrategyOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </div>
              <div class="option-children">
                <div v-for="child in option.children" :key="child.id" class="option-child-row">
                  <el-input v-model.trim="child.title" placeholder="补充点 / 理由" />
                  <button type="button" class="text-action" @click="removeOptionChild(option.id, child.id)">删除</button>
                </div>
                <button type="button" class="add-child-button" @click="addOptionChild(option.id)">添加补充点</button>
              </div>
            </article>
            <button type="button" class="add-option-button" @click="addOption">添加候选方案</button>
          </div>
        </section>

        <section class="create-form-section section-review">
          <div class="create-section-heading">
            <span>03</span>
            <div>
              <h3>原因与回看</h3>
              <p>写下现在的判断，并给未来一个回看时间。</p>
            </div>
          </div>
          <el-form-item label="选择原因" prop="reason">
            <el-input v-model.trim="createForm.reason" type="textarea" :rows="3" placeholder="为什么现在倾向这样选？" />
          </el-form-item>
          <div class="form-grid">
            <el-form-item label="紧急度" prop="urgency">
              <el-select v-model="createForm.urgency">
                <el-option label="低" :value="1" />
                <el-option label="中" :value="2" />
                <el-option label="高" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="回看时间" prop="reviewTime">
              <el-date-picker
                v-model="createForm.reviewTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="选择回看时间"
              />
            </el-form-item>
          </div>
        </section>

        <section class="ai-advice-card create-ai-advice-card">
          <div>
            <span>AI 决策建议</span>
            <p>填完决策信息后，可以先生成一份建议，再决定是否保存。</p>
          </div>
          <el-button
            v-if="!createAdviceResult"
            :loading="createAdviceLoading"
            class="primary-action ai-advice-button"
            type="primary"
            @click="handleGenerateCreateAdvice"
          >
            生成 AI 建议
          </el-button>
          <p v-if="createAdviceError" class="ai-advice-error">{{ createAdviceError }}</p>
          <div v-if="createAdviceResult" class="ai-advice-result">
            <p class="ai-advice-overall">{{ createAdviceResult.overallAdvice || '暂无整体建议' }}</p>
            <div v-if="createAdviceResult.options?.length" class="ai-option-advice-list">
              <article v-for="option in createAdviceResult.options || []" :key="option.name" class="ai-option-advice">
                <h4>{{ option.name || '未命名方案' }}</h4>
                <div class="ai-option-advice-grid">
                  <section>
                    <span>优点</span>
                    <ul>
                      <li v-for="item in adviceItems(option.pros)" :key="item">{{ item }}</li>
                    </ul>
                  </section>
                  <section>
                    <span>缺点</span>
                    <ul>
                      <li v-for="item in adviceItems(option.cons)" :key="item">{{ item }}</li>
                    </ul>
                  </section>
                  <section>
                    <span>风险</span>
                    <ul>
                      <li v-for="item in adviceItems(option.risks)" :key="item">{{ item }}</li>
                    </ul>
                  </section>
                </div>
                <p><strong>适合情况：</strong>{{ option.bestFor || '暂无' }}</p>
                <p><strong>补充建议：</strong>{{ option.suggestion || '暂无' }}</p>
              </article>
            </div>
            <p v-else class="ai-advice-empty">暂无候选方案分析</p>
            <p class="ai-advice-reminder">{{ createAdviceResult.reminder || '最终选择前，可以再检查一次关键信息。' }}</p>
          </div>
        </section>
      </el-form>
      <template #footer>
        <div class="create-dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button class="primary-action dialog-primary" type="primary" :loading="creating" @click="submitCreate">保存决策</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" align-center class="youth-dialog decision-detail-dialog" title="Decision Details" width="640px">
      <div v-if="detailLoading" class="detail-loading">正在加载...</div>
      <div v-else-if="decisionDetail" class="decision-detail-content">
        <section class="detail-title-block">
          <div>
            <span :class="['status-pill', statusClass(decisionDetail.status)]">{{ statusText(decisionDetail.status) }}</span>
            <small>{{ formatDate(decisionDetail.createTime) }}</small>
          </div>
          <h3>{{ decisionDetail.title }}</h3>
        </section>

        <section class="detail-section">
          <span>背景</span>
          <p>{{ decisionDetail.context || '未填写背景' }}</p>
        </section>

        <section class="detail-section">
          <span>候选方案</span>
          <div v-if="decisionDetail.options?.length" class="detail-option-list">
            <article v-for="item in decisionDetail.options" :key="item.id || item.title" class="detail-option-item" :class="{ selected: item.title === decisionDetail.finalChoice }">
              <strong>
                {{ item.title }}
                <i v-if="item.strategy">{{ item.strategy }}</i>
              </strong>
              <small v-if="item.children?.length">{{ item.children.map((child) => child.title).join(' / ') }}</small>
            </article>
          </div>
          <p v-else>未记录候选方案</p>
        </section>

        <section class="detail-section">
          <span>最终选择</span>
          <p>{{ decisionDetail.finalChoice || '旧记录未标记最终选择' }}</p>
        </section>

        <section class="detail-section">
          <span>选择原因</span>
          <p>{{ decisionDetail.reason || '未填写选择原因' }}</p>
        </section>

        <section v-if="decisionDetail.status === 'reviewed'" class="detail-section">
          <span>回测结果</span>
          <p>{{ decisionDetail.satisfaction || '未记录满意度' }}</p>
          <small v-if="decisionDetail.feedback" class="detail-feedback">{{ decisionDetail.feedback }}</small>
        </section>

        <section class="ai-advice-card detail-ai-advice-card">
          <div>
            <span>AI 复盘建议</span>
            <p>基于这条决策的背景和候选方案，生成一份利弊分析。</p>
          </div>
          <el-button
            v-if="!detailAdviceResult"
            :loading="detailAdviceLoading"
            class="primary-action ai-advice-button"
            type="primary"
            @click="handleGenerateDetailAdvice"
          >
            生成 AI 建议
          </el-button>
          <p v-if="detailAdviceError" class="ai-advice-error">{{ detailAdviceError }}</p>
          <div v-if="detailAdviceResult" class="ai-advice-result">
            <p class="ai-advice-overall">{{ detailAdviceResult.summary || '暂无决策概括' }}</p>
            <div class="ai-review-advice-grid">
              <section>
                <span>影响因素</span>
                <p>{{ detailAdviceResult.factors || '暂无' }}</p>
              </section>
              <section>
                <span>风险问题</span>
                <p>{{ detailAdviceResult.risks || '暂无' }}</p>
              </section>
            </div>
            <section class="ai-review-improvements">
              <span>改进建议</span>
              <ul>
                <li v-for="item in adviceItems(detailAdviceResult.improvements)" :key="item">{{ item }}</li>
              </ul>
            </section>
            <p class="ai-advice-reminder">{{ detailAdviceResult.nextReminder || '下次遇到类似决策时，可以先检查关键信息是否充分。' }}</p>
          </div>
        </section>

      </div>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" align-center class="youth-dialog review-decision-dialog" title="补回测" width="560px">
      <div v-if="activeDecision" class="review-dialog-content">
        <section class="review-target-card">
          <div class="review-target-meta">
            <span>{{ formatDate(activeDecision.reviewTime) }}</span>
            <i>{{ urgencyText(activeDecision.urgency) }}</i>
          </div>
          <h3>{{ activeDecision.title }}</h3>
          <p>{{ activeDecision.context || activeDecision.reason || '回看当时的选择，再补上今天的结果。' }}</p>
          <div v-if="optionSummary(activeDecision.options).selected" class="review-selected-choice">
            <span>当时选择</span>
            <strong>{{ optionSummary(activeDecision.options).selected }}</strong>
          </div>
        </section>

        <el-form :model="reviewForm" label-position="top" class="review-form">
          <el-form-item label="这次结果感觉如何？">
            <div class="review-mood-grid">
              <button
                v-for="item in reviewSatisfactionOptions"
                :key="item.value"
                type="button"
                class="review-mood-card"
                :class="[{ active: reviewForm.satisfaction === item.value }, `tone-${item.tone}`]"
                @click="reviewForm.satisfaction = item.value"
              >
                <span>{{ item.icon }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.hint }}</small>
              </button>
            </div>
          </el-form-item>
          <el-form-item label="复盘反馈">
            <el-input
              v-model.trim="reviewForm.feedback"
              type="textarea"
              :rows="5"
              maxlength="300"
              show-word-limit
              placeholder="写下实际结果、和预期的偏差、下次可以怎么判断。"
            />
          </el-form-item>
          <el-form-item v-if="reviewForm.satisfaction === '后悔'" label="更好的选择">
            <el-select v-model="reviewForm.betterChoice" placeholder="从候选方案中重新选择">
              <el-option
                v-for="item in reviewBetterChoiceOptions"
                :key="item.id || item.title"
                :label="item.strategy ? `${item.title}（${item.strategy}）` : item.title"
                :value="item.title"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <section class="ai-advice-card review-ai-advice-card">
          <div>
            <span>AI 回测建议</span>
            <p>回看前可以重新分析当时的候选方案，辅助整理复盘思路。</p>
          </div>
          <el-button
            v-if="!reviewAdviceResult"
            :loading="reviewAdviceLoading"
            class="primary-action ai-advice-button"
            type="primary"
            @click="handleGenerateReviewAdvice"
          >
            生成 AI 建议
          </el-button>
          <p v-if="reviewAdviceError" class="ai-advice-error">{{ reviewAdviceError }}</p>
          <div v-if="reviewAdviceResult" class="ai-advice-result">
            <p class="ai-advice-overall">{{ reviewAdviceResult.summary || '暂无决策概括' }}</p>
            <div class="ai-review-advice-grid">
              <section>
                <span>影响因素</span>
                <p>{{ reviewAdviceResult.factors || '暂无' }}</p>
              </section>
              <section>
                <span>风险问题</span>
                <p>{{ reviewAdviceResult.risks || '暂无' }}</p>
              </section>
            </div>
            <section class="ai-review-improvements">
              <span>改进建议</span>
              <ul>
                <li v-for="item in adviceItems(reviewAdviceResult.improvements)" :key="item">{{ item }}</li>
              </ul>
            </section>
            <p class="ai-advice-reminder">{{ reviewAdviceResult.nextReminder || '下次遇到类似决策时，可以先检查关键信息是否充分。' }}</p>
          </div>
        </section>
      </div>
      <template #footer>
        <div class="review-dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button class="primary-action dialog-primary" type="primary" :loading="reviewing" @click="submitReview">保存回测</el-button>
        </div>
      </template>
    </el-dialog>

  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { createDecision, deleteDecision, generateDecisionAdvice, getDecisionDashboard, getDecisionDetail, reviewDecision, searchDecisions } from '../../api/decision'

const router = useRouter()
const authStore = useAuthStore()
const createFormRef = ref()
const loading = ref(false)
const creating = ref(false)
const reviewing = ref(false)
const isSearching = ref(false)
const isSearchMode = ref(false)
const detailLoading = ref(false)
const createAdviceLoading = ref(false)
const detailAdviceLoading = ref(false)
const reviewAdviceLoading = ref(false)
const deletingDecisionId = ref(null)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const activeDecision = ref(null)
const decisionDetail = ref(null)
const createAdviceResult = ref(null)
const detailAdviceResult = ref(null)
const reviewAdviceResult = ref(null)
const createAdviceError = ref('')
const detailAdviceError = ref('')
const reviewAdviceError = ref('')
const dashboard = ref({ summary: { total: 0, pending: 0, reviewed: 0, satisfaction: {} }, recent: [], pendingReview: [] })
const searchResults = ref([])
const decisionPage = ref(1)
const decisionPageSize = 10
const tagOptions = ['学习', '消费', '工作', '生活', '健康']
const moodOptions = ['平静', '焦虑', '纠结', '兴奋', '冲动']
const statusFilterOptions = [
  { label: '全部', value: '' },
  { label: '待回看', value: 'pending' },
  { label: '已复盘', value: 'reviewed' }
]
const optionStrategyOptions = ['保守', '平衡', '激进']
const optionTree = ref(createDefaultOptionTree())
const selectedOptionId = ref(optionTree.value[0].id)

const createForm = reactive({
  title: '',
  context: '',
  reason: '',
  tags: '',
  mood: '',
  urgency: 2,
  reviewTime: ''
})

const reviewForm = reactive({
  satisfaction: '满意',
  feedback: '',
  betterChoice: ''
})

const searchForm = reactive({
  keyword: '',
  tag: '',
  status: ''
})

const reviewSatisfactionOptions = [
  { value: '满意', icon: '很值', hint: '结果比预期更好', tone: 'good' },
  { value: '一般', icon: '还行', hint: '基本符合预期', tone: 'normal' },
  { value: '后悔', icon: '踩坑', hint: '下次需要调整', tone: 'bad' }
]

const createRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '选择原因不能为空', trigger: 'blur' }],
  reviewTime: [{ required: true, message: '回看时间不能为空', trigger: 'change' }]
}

const displayName = computed(() => authStore.user?.nickname || authStore.user?.username || '朋友')
const avatarUrl = computed(() => authStore.user?.avatarUrl || '')
const avatarInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 11) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
const summary = computed(() => dashboard.value.summary || { total: 0, pending: 0, reviewed: 0, satisfaction: {} })
const hasSearchFilters = computed(() => Boolean(searchForm.keyword || searchForm.tag || searchForm.status))
const recentDecisions = computed(() => (isSearchMode.value ? searchResults.value : dashboard.value.recent || []))
const pagedRecentDecisions = computed(() => {
  const start = (decisionPage.value - 1) * decisionPageSize
  return recentDecisions.value.slice(start, start + decisionPageSize)
})
const reviewBetterChoiceOptions = computed(() => {
  if (!activeDecision.value) {
    return []
  }
  return optionSummary(activeDecision.value.options).items
})
const pendingReview = computed(() => dashboard.value.pendingReview || [])
const satisfactionRate = computed(() => {
  const reviewed = summary.value.reviewed || 0
  const satisfied = summary.value.satisfaction?.['满意'] || 0
  return reviewed === 0 ? '0%' : `${Math.round((satisfied / reviewed) * 100)}%`
})
const overviewCards = computed(() => [
  { label: '全部记录', value: summary.value.total, note: '已经写下的选择', tone: 'mint' },
  { label: '待回看', value: summary.value.pending, note: '到点需要复盘', tone: 'yellow' },
  { label: '已复盘', value: summary.value.reviewed, note: '有了结果反馈', tone: 'coral' },
  { label: '满意率', value: satisfactionRate.value, note: '满意 / 已复盘', tone: 'ink' }
])
const satisfactionBars = computed(() => {
  const items = [
    { label: '满意', key: 'good' },
    { label: '一般', key: 'normal' },
    { label: '后悔', key: 'bad' }
  ]
  const satisfaction = summary.value.satisfaction || {}
  const max = Math.max(...items.map((item) => satisfaction[item.label] || 0), 1)
  return items.map((item) => {
    const value = satisfaction[item.label] || 0
    return { ...item, value, width: `${Math.max(8, Math.round((value / max) * 100))}%` }
  })
})

async function loadDashboard() {
  loading.value = true
  try {
    dashboard.value = await getDecisionDashboard()
    if (!isSearchMode.value) {
      searchResults.value = []
    }
    decisionPage.value = 1
  } catch (error) {
    if (error.response?.status === 401) {
      router.push('/login')
      return
    }
    throw error
  } finally {
    loading.value = false
  }
}

async function executeDecisionSearch() {
  if (!hasSearchFilters.value) {
    isSearchMode.value = false
    searchResults.value = []
    await loadDashboard()
    return
  }
  isSearching.value = true
  try {
    searchResults.value = await searchDecisions({
      keyword: searchForm.keyword || undefined,
      tag: searchForm.tag || undefined,
      status: searchForm.status || undefined,
      limit: 50
    })
    isSearchMode.value = true
    decisionPage.value = 1
  } finally {
    isSearching.value = false
  }
}

async function selectDecisionStatus(status) {
  searchForm.status = status
  await executeDecisionSearch()
}

async function resetDecisionSearch() {
  searchForm.keyword = ''
  searchForm.tag = ''
  searchForm.status = ''
  isSearchMode.value = false
  searchResults.value = []
  decisionPage.value = 1
  await loadDashboard()
}

async function refreshDecisionData() {
  await loadDashboard()
  if (isSearchMode.value) {
    await executeDecisionSearch()
  }
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

function goProfile() {
  router.push('/profile')
}

function goDashboard() {
  router.push('/dashboard')
}

function goAnalysis() {
  router.push('/analysis')
}

function openCreateDialog() {
  createAdviceResult.value = null
  createAdviceError.value = ''
  createDialogVisible.value = true
}

async function openDetail(decision) {
  detailDialogVisible.value = true
  detailLoading.value = true
  decisionDetail.value = null
  detailAdviceResult.value = null
  detailAdviceError.value = ''
  try {
    decisionDetail.value = await getDecisionDetail(decision.id)
  } finally {
    detailLoading.value = false
  }
}

async function handleGenerateDetailAdvice() {
  if (!decisionDetail.value) {
    return
  }
  detailAdviceLoading.value = true
  detailAdviceError.value = ''
  try {
    const data = await generateDecisionAdvice(buildAdvicePayload(decisionDetail.value, {
      satisfaction: decisionDetail.value.satisfaction || '',
      feedback: decisionDetail.value.feedback || ''
    }))
    detailAdviceResult.value = normalizeAdviceResult(data)
  } catch (error) {
    detailAdviceError.value = 'AI 建议生成失败，请稍后重试。'
  } finally {
    detailAdviceLoading.value = false
  }
}

async function handleGenerateCreateAdvice() {
  await createFormRef.value.validate()
  if (!validateOptionTree()) {
    return
  }
  createAdviceLoading.value = true
  createAdviceError.value = ''
  try {
    const data = await generateDecisionAdvice({ ...createForm, mode: 'create', context: createForm.context || '', options: buildOptionsPayload() })
    createAdviceResult.value = normalizeAdviceResult(data)
  } catch (error) {
    createAdviceError.value = 'AI 建议生成失败，请稍后重试。'
  } finally {
    createAdviceLoading.value = false
  }
}

async function submitCreate() {
  await createFormRef.value.validate()
  if (!validateOptionTree()) {
    return
  }
  creating.value = true
  try {
    await createDecision({ ...createForm, context: createForm.context || '', options: buildOptionsPayload() })
    ElMessage.success('决策已保存')
    resetCreateForm()
    createAdviceResult.value = null
    createAdviceError.value = ''
    createDialogVisible.value = false
    await refreshDecisionData()
  } finally {
    creating.value = false
  }
}

function openReview(decision) {
  activeDecision.value = decision
  reviewForm.satisfaction = '满意'
  reviewForm.feedback = ''
  reviewForm.betterChoice = ''
  reviewAdviceResult.value = null
  reviewAdviceError.value = ''
  reviewDialogVisible.value = true
}

async function confirmDelete(decision) {
  try {
    await ElMessageBox.confirm(`确定删除“${decision.title}”吗？删除后不会再出现在列表和统计中。`, '删除决策', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  deletingDecisionId.value = decision.id
  try {
    await deleteDecision(decision.id)
    ElMessage.success('决策已删除')
    if (decisionDetail.value?.id === decision.id) {
      detailDialogVisible.value = false
      decisionDetail.value = null
    }
    await refreshDecisionData()
  } finally {
    deletingDecisionId.value = null
  }
}

async function submitReview() {
  if (!reviewForm.feedback) {
    ElMessage.warning('请填写复盘反馈')
    return
  }
  if (reviewForm.satisfaction === '后悔' && !reviewForm.betterChoice) {
    ElMessage.warning('请填写你认为更好的选择')
    return
  }
  reviewing.value = true
  try {
    await reviewDecision(activeDecision.value.id, {
      satisfaction: reviewForm.satisfaction,
      feedback: reviewForm.feedback,
      betterChoice: reviewForm.satisfaction === '后悔' ? reviewForm.betterChoice : undefined
    })
    ElMessage.success('回测已保存')
    reviewDialogVisible.value = false
    await refreshDecisionData()
  } finally {
    reviewing.value = false
  }
}

async function handleGenerateReviewAdvice() {
  if (!activeDecision.value) {
    return
  }
  reviewAdviceLoading.value = true
  reviewAdviceError.value = ''
  try {
    const data = await generateDecisionAdvice(buildAdvicePayload(activeDecision.value, {
      satisfaction: reviewForm.satisfaction,
      feedback: reviewForm.feedback
    }))
    reviewAdviceResult.value = normalizeAdviceResult(data)
  } catch (error) {
    reviewAdviceError.value = 'AI 建议生成失败，请稍后重试。'
  } finally {
    reviewAdviceLoading.value = false
  }
}

function resetCreateForm() {
  Object.assign(createForm, {
      title: '',
      context: '',
      reason: '',
      tags: '',
    mood: '',
    urgency: 2,
      reviewTime: ''
  })
  resetOptionTree()
}

function formatDate(value) {
  if (!value) return '-'
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0] = value
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

function urgencyText(value) {
  return { 1: '低', 2: '中', 3: '高' }[value] || '中'
}

function statusText(status) {
  return status === 'reviewed' ? '已复盘' : '待回看'
}

function statusClass(status) {
  return status === 'reviewed' ? 'done' : 'todo'
}

function createOptionNode(title = '', children = [], strategy = '平衡') {
  return {
    id: `opt_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    title,
    strategy,
    children
  }
}

function createDefaultOptionTree() {
  return [createOptionNode(''), createOptionNode('')]
}

function resetOptionTree() {
  optionTree.value = createDefaultOptionTree()
  selectedOptionId.value = optionTree.value[0].id
}

function addOption() {
  optionTree.value.push(createOptionNode(''))
}

function removeOption(id) {
  if (optionTree.value.length <= 2) {
    ElMessage.warning('至少保留两个候选方案')
    return
  }
  optionTree.value = optionTree.value.filter((option) => option.id !== id)
  if (selectedOptionId.value === id) {
    selectedOptionId.value = optionTree.value[0]?.id || ''
  }
}

function addOptionChild(optionId) {
  const option = optionTree.value.find((item) => item.id === optionId)
  if (option) {
    option.children.push(createOptionNode(''))
  }
}

function removeOptionChild(optionId, childId) {
  const option = optionTree.value.find((item) => item.id === optionId)
  if (option) {
    option.children = option.children.filter((child) => child.id !== childId)
  }
}

function validateOptionTree() {
  const topLevel = optionTree.value.map((option) => ({ ...option, title: option.title.trim() }))
  if (topLevel.length < 2) {
    ElMessage.warning('至少添加两个候选方案')
    return false
  }
  if (topLevel.some((option) => !option.title)) {
    ElMessage.warning('候选方案标题不能为空')
    return false
  }
  if (!topLevel.some((option) => option.id === selectedOptionId.value)) {
    ElMessage.warning('请选择最终方案')
    return false
  }
  return true
}

function buildOptionsPayload() {
  const items = optionTree.value.map((option) => ({
    id: option.id,
    title: option.title.trim(),
    strategy: option.strategy,
    children: option.children
      .map((child) => ({ id: child.id, title: child.title.trim() }))
      .filter((child) => child.title)
  }))
  return JSON.stringify({ version: 1, selectedId: selectedOptionId.value, items })
}

function optionSummary(rawOptions) {
  if (!rawOptions) {
    return { selected: '', items: [] }
  }
  try {
    const parsed = JSON.parse(rawOptions)
    if (parsed?.version === 1 && Array.isArray(parsed.items)) {
      const selected = parsed.items.find((item) => item.id === parsed.selectedId)
      return {
        selected: selected?.title || '',
        selectedStrategy: selected?.strategy || '',
        items: parsed.items.map((item) => ({
          id: item.id,
          title: item.title,
          strategy: item.strategy || '',
          children: Array.isArray(item.children) ? item.children : []
        }))
      }
    }
  } catch (error) {
    // Legacy records used comma-separated option text.
  }
  const items = String(rawOptions)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((title, index) => ({ id: `legacy_${index}`, title, strategy: '', children: [] }))
  return { selected: items[0]?.title || '', selectedStrategy: '', items }
}

function adviceItems(items) {
  return Array.isArray(items) && items.length > 0 ? items : ['暂无']
}

function buildAdvicePayload(decision, reviewInfo = {}) {
  const summary = optionSummary(decision.options)
  return {
    decisionId: decision.id || null,
    mode: 'review',
    title: decision.title || '未填写标题',
    context: decision.context || '',
    reason: decision.reason || '未填写选择原因',
    tags: decision.tags || '',
    mood: decision.mood || '',
    urgency: decision.urgency || 2,
    reviewTime: normalizeReviewTimeValue(decision.reviewTime) || new Date().toISOString().slice(0, 19),
    selectedOption: decision.finalChoice || summary.selected || '',
    satisfaction: reviewInfo.satisfaction || decision.satisfaction || '',
    feedback: reviewInfo.feedback || decision.feedback || '',
    historySummary: buildHistorySummary(),
    options: buildAdviceOptions(decision)
  }
}

function buildHistorySummary() {
  const satisfaction = summary.value.satisfaction || {}
  const total = summary.value.reviewed || 0
  if (!total) {
    return '暂无已复盘记录'
  }
  return `已复盘 ${total} 条；满意 ${satisfaction['满意'] || 0} 条，一般 ${satisfaction['一般'] || 0} 条，后悔 ${satisfaction['后悔'] || 0} 条。`
}

function buildAdviceOptions(decision) {
  if (typeof decision.options === 'string' && decision.options.trim()) {
    return decision.options
  }
  if (Array.isArray(decision.options) && decision.options.length > 0) {
    return JSON.stringify({
      selectedId: decision.options.find((item) => item.title === decision.finalChoice)?.id || '',
      items: decision.options
    })
  }
  if (decision.finalChoice) {
    return decision.finalChoice
  }
  return '未填写候选方案'
}

function normalizeReviewTimeValue(value) {
  if (!value) {
    return ''
  }
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value
    return `${year}-${padDatePart(month)}-${padDatePart(day)}T${padDatePart(hour)}:${padDatePart(minute)}:${padDatePart(second)}`
  }
  if (typeof value === 'string') {
    return value.includes(' ') ? value.replace(' ', 'T') : value
  }
  return ''
}

function padDatePart(value) {
  return String(value).padStart(2, '0')
}

function normalizeAdviceResult(data) {
  if (!data) {
    return null
  }
  if (data.overallAdvice || data.options || data.reminder) {
    return data
  }
  if (typeof data.advice === 'string' && data.advice.trim()) {
    const cleaned = data.advice.trim().replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/, '')
    try {
      const parsed = JSON.parse(cleaned)
      if (parsed && typeof parsed === 'object') {
        return parsed
      }
    } catch (error) {
      return {
        overallAdvice: data.advice,
        options: [],
        reminder: '最终选择前，可以再检查一次关键信息。'
      }
    }
  }
  return null
}

onMounted(async () => {
  try {
    await Promise.all([loadDashboard(), authStore.loadCurrentUser()])
  } catch (error) {
    if (error.response?.status === 401) {
      router.push('/login')
    }
  }
})
</script>
