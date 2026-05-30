<template>
  <el-dialog v-model="dialogVisible" align-center class="youth-dialog create-decision-dialog" draggable title="记录新决定" width="760px">
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
        <el-form-item label="纳入长期目标">
          <el-select
            v-model="createForm.goalIds"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="选择这条决策要纳入的长期目标"
          >
            <el-option v-for="goal in mergedGoalOptions" :key="goal.id" :label="goal.title || '未命名目标'" :value="goal.id" />
          </el-select>
        </el-form-item>
        <div class="goal-recommend-card">
          <div>
            <span>根据标签推荐</span>
            <p>{{ goalRecommendHint }}</p>
          </div>
          <button type="button" :disabled="recommendedGoals.length === 0" @click="addRecommendedGoals">
            一键加入推荐目标
          </button>
          <div v-if="recommendedGoals.length" class="goal-recommend-list">
            <button
              v-for="goal in recommendedGoals"
              :key="goal.id"
              type="button"
              :class="{ selected: createForm.goalIds.includes(goal.id) }"
              @click="toggleGoal(goal.id)"
            >
              <strong>{{ goal.title || '未命名目标' }}</strong>
              <small>{{ goal.category || '未分类' }} · {{ normalizedProgress(goal.progress) }}%</small>
            </button>
          </div>
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="primary-action dialog-primary" type="primary" :loading="creating" @click="submitCreate">保存决策</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createDecision, generateDecisionAdvice } from '../../../api/decision'
import { getGoals, recommendGoals } from '../../../api/goal'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  initialGoalId: {
    type: [Number, String],
    default: null
  },
  initialGoal: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success'])

const createFormRef = ref()
const creating = ref(false)
const createAdviceLoading = ref(false)
const createAdviceResult = ref(null)
const createAdviceError = ref('')
const goalOptions = ref([])
const recommendedGoals = ref([])
const recommendingGoals = ref(false)
const optionTree = ref(createDefaultOptionTree())
const selectedOptionId = ref(optionTree.value[0].id)

const tagOptions = ['学习', '消费', '工作', '生活', '健康']
const moodOptions = ['平静', '焦虑', '纠结', '兴奋', '冲动']
const optionStrategyOptions = ['保守', '平衡', '激进']

const createForm = reactive({
  title: '',
  context: '',
  reason: '',
  tags: '',
  goalIds: [],
  mood: '',
  urgency: 2,
  reviewTime: ''
})

const createRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '选择原因不能为空', trigger: 'blur' }],
  reviewTime: [{ required: true, message: '回看时间不能为空', trigger: 'change' }]
}

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const initialGoalIdValue = computed(() => normalizeGoalId(props.initialGoalId || props.initialGoal?.id))

const mergedGoalOptions = computed(() => {
  const merged = new Map()
  if (props.initialGoal?.id) {
    merged.set(normalizeGoalId(props.initialGoal.id), {
      ...props.initialGoal,
      id: normalizeGoalId(props.initialGoal.id)
    })
  }
  for (const goal of recommendedGoals.value) {
    if (goal?.id) merged.set(normalizeGoalId(goal.id), { ...goal, id: normalizeGoalId(goal.id) })
  }
  for (const goal of goalOptions.value) {
    if (goal?.id) merged.set(normalizeGoalId(goal.id), { ...goal, id: normalizeGoalId(goal.id) })
  }
  return Array.from(merged.values())
})

const goalRecommendHint = computed(() => {
  if (!createForm.tags) {
    return '选择标签后，系统会自动推荐匹配的进行中目标。'
  }
  if (recommendingGoals.value) {
    return '正在根据标签匹配长期目标...'
  }
  if (recommendedGoals.value.length === 0) {
    return '暂未匹配到推荐目标，可以从下方手动选择。'
  }
  return `已推荐 ${recommendedGoals.value.length} 个目标，可一键加入本次决策。`
})

watch(() => props.visible, async (visible) => {
  if (!visible) {
    return
  }
  prepareCreateDialog()
  await nextTick()
  loadGoalOptions()
  loadRecommendedGoals()
})

watch(() => createForm.tags, () => {
  if (dialogVisible.value) {
    loadRecommendedGoals()
  }
})

function prepareCreateDialog() {
  resetCreateForm()
  createAdviceResult.value = null
  createAdviceError.value = ''
  const goalId = initialGoalIdValue.value
  createForm.goalIds = goalId ? [goalId] : []
  createFormRef.value?.clearValidate?.()
}

async function handleGenerateCreateAdvice() {
  await createFormRef.value.validate()
  if (!validateOptionTree()) {
    return
  }
  createAdviceLoading.value = true
  createAdviceError.value = ''
  try {
    const data = await generateDecisionAdvice({
      ...createForm,
      mode: 'create',
      context: createForm.context || '',
      options: buildOptionsPayload()
    })
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
    await createDecision({
      ...createForm,
      context: createForm.context || '',
      options: buildOptionsPayload()
    })
    ElMessage.success('决策已保存')
    dialogVisible.value = false
    emit('success')
    resetCreateForm()
  } finally {
    creating.value = false
  }
}

function resetCreateForm() {
  Object.assign(createForm, {
    title: '',
    context: '',
    reason: '',
    tags: '',
    goalIds: [],
    mood: '',
    urgency: 2,
    reviewTime: ''
  })
  resetOptionTree()
  recommendedGoals.value = []
}

async function loadGoalOptions() {
  try {
    goalOptions.value = normalizeGoalList(await getGoals({ status: 'IN_PROGRESS' }))
  } catch (error) {
    goalOptions.value = []
  }
}

async function loadRecommendedGoals() {
  const tags = splitCreateTags(createForm.tags)
  if (tags.length === 0) {
    recommendedGoals.value = []
    return
  }
  recommendingGoals.value = true
  try {
    recommendedGoals.value = normalizeGoalList(await recommendGoals(tags))
  } catch (error) {
    recommendedGoals.value = []
  } finally {
    recommendingGoals.value = false
  }
}

function addRecommendedGoals() {
  const ids = recommendedGoals.value.map((goal) => normalizeGoalId(goal.id)).filter(Boolean)
  createForm.goalIds = Array.from(new Set([...createForm.goalIds, ...ids]))
  if (ids.length > 0) {
    ElMessage.success('已加入推荐目标')
  }
}

function toggleGoal(goalId) {
  const normalizedId = normalizeGoalId(goalId)
  if (!normalizedId) {
    return
  }
  if (createForm.goalIds.includes(normalizedId)) {
    createForm.goalIds = createForm.goalIds.filter((id) => id !== normalizedId)
    return
  }
  createForm.goalIds = [...createForm.goalIds, normalizedId]
}

function splitCreateTags(value) {
  if (!value) {
    return []
  }
  return String(value)
    .split(/[\s,，、]+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function normalizeGoalList(data) {
  const list = Array.isArray(data)
    ? data
    : Array.isArray(data?.records)
      ? data.records
      : Array.isArray(data?.list)
        ? data.list
        : Array.isArray(data?.items)
          ? data.items
          : []
  return list.map((goal) => ({ ...goal, id: normalizeGoalId(goal.id) })).filter((goal) => goal.id)
}

function normalizedProgress(value) {
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return 0
  }
  return Math.min(100, Math.max(0, Math.round(numberValue)))
}

function normalizeGoalId(value) {
  if (value === null || value === undefined || value === '') {
    return null
  }
  const numberValue = Number(value)
  return Number.isNaN(numberValue) ? value : numberValue
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

function adviceItems(items) {
  return Array.isArray(items) && items.length > 0 ? items : ['暂无']
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
</script>
