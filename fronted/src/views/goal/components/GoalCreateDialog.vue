<template>
  <el-dialog
    v-model="visible"
    class="goal-create-dialog"
    width="min(92vw, 760px)"
    :show-close="false"
    append-to-body
    destroy-on-close
  >
    <div class="goal-create-panel">
      <div class="goal-create-glow" aria-hidden="true"></div>
      <div class="goal-create-content">
        <header class="goal-create-header">
          <div>
            <h2>新建长期目标</h2>
            <p>把长期方向写清楚，让后续每一次决策都有参照。</p>
          </div>
          <span class="goal-create-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" role="img">
              <path d="M6 3.75c0-.41.34-.75.75-.75h8.4c.28 0 .54.16.67.41l.9 1.8h2.53c.41 0 .75.34.75.75v8.5c0 .41-.34.75-.75.75h-8.4a.75.75 0 0 1-.67-.41l-.9-1.8H7.5v7.25a.75.75 0 0 1-1.5 0V3.75Zm1.5.75v7h2.25c.28 0 .54.16.67.41l.9 1.8h7.18v-7h-2.25a.75.75 0 0 1-.67-.41l-.9-1.8H7.5Z" />
            </svg>
          </span>
        </header>

        <form class="goal-create-form" @submit.prevent="submit">
          <label class="goal-field">
            <span>目标标题</span>
            <input v-model.trim="form.title" type="text" maxlength="100" placeholder="例如：考取高级工程师证书" />
          </label>

          <label class="goal-field">
            <span>目标描述</span>
            <textarea v-model.trim="form.description" rows="3" placeholder="描述你想达成的结果，以及为什么它重要。"></textarea>
          </label>

          <div class="goal-form-grid">
            <label class="goal-field">
              <span>分类</span>
              <select v-model="form.category">
                <option value="">选择分类</option>
                <option value="学习">学习</option>
                <option value="工作">工作</option>
                <option value="健康">健康</option>
                <option value="财务">财务</option>
                <option value="生活">生活</option>
              </select>
            </label>

            <label class="goal-field">
              <span>优先级</span>
              <select v-model="form.priority">
                <option value="HIGH">高优先级</option>
                <option value="MEDIUM">中优先级</option>
                <option value="LOW">低优先级</option>
              </select>
            </label>
          </div>

          <div class="goal-form-grid">
            <label class="goal-field">
              <span>预期完成日期</span>
              <input v-model="form.targetDate" type="date" />
            </label>

            <label class="goal-field">
              <span>衡量方式</span>
              <input v-model.trim="form.measurement" type="text" maxlength="255" placeholder="例如：考试分数达到 90 分以上" />
            </label>
          </div>

          <label class="goal-field">
            <span>标签</span>
            <input v-model.trim="tagText" type="text" placeholder="用逗号或空格分隔，例如：学习 证书 职业发展" />
          </label>

          <div class="goal-progress-field">
            <div>
              <span>初始进度</span>
              <strong>{{ form.progress }}%</strong>
            </div>
            <input v-model.number="form.progress" type="range" min="0" max="100" />
          </div>

          <div class="goal-status-field">
            <span>当前状态</span>
            <div class="goal-status-pills">
              <label v-for="item in statusOptions" :key="item.value" :class="['goal-status-pill', item.tone, { active: form.status === item.value }]">
                <input v-model="form.status" type="radio" name="goal-status" :value="item.value" />
                {{ item.label }}
              </label>
            </div>
          </div>

          <footer class="goal-create-footer">
            <button type="button" class="goal-cancel-button" :disabled="submitting" @click="close">取消</button>
            <button type="submit" class="goal-submit-button" :disabled="submitting">
              {{ submitting ? '保存中...' : '保存目标' }}
            </button>
          </footer>
        </form>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createGoal } from '../../../api/goal'

const visible = defineModel('visible', { type: Boolean, default: false })
const emit = defineEmits(['success'])

const statusOptions = [
  { value: 'IN_PROGRESS', label: '进行中', tone: 'pink' },
  { value: 'COMPLETED', label: '已完成', tone: 'blue' },
  { value: 'ABANDONED', label: '已放弃', tone: 'gray' }
]

const initialForm = () => ({
  title: '',
  description: '',
  category: '',
  priority: 'MEDIUM',
  status: 'IN_PROGRESS',
  targetDate: '',
  measurement: '',
  progress: 0
})

const form = reactive(initialForm())
const tagText = ref('')
const submitting = ref(false)

watch(visible, (value) => {
  if (value) {
    resetForm()
  }
})

function resetForm() {
  Object.assign(form, initialForm())
  tagText.value = ''
  submitting.value = false
}

function close() {
  if (submitting.value) {
    return
  }
  visible.value = false
}

function parseTags(value) {
  if (!value) {
    return []
  }
  return Array.from(
    new Set(
      value
        .split(/[\s,，、]+/)
        .map((item) => item.trim())
        .filter(Boolean)
    )
  )
}

async function submit() {
  if (!form.title) {
    ElMessage.warning('请先填写目标标题')
    return
  }
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      description: form.description || null,
      category: form.category || null,
      priority: form.priority,
      status: form.status,
      targetDate: form.targetDate || null,
      measurement: form.measurement || null,
      progress: Number(form.progress) || 0,
      tags: parseTags(tagText.value)
    }
    const goal = await createGoal(payload)
    ElMessage.success('目标创建成功')
    visible.value = false
    emit('success', goal)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
:deep(.goal-create-dialog) {
  --el-dialog-padding-primary: 0;
  border-radius: 22px;
  background: transparent;
  box-shadow: none;
}

:deep(.goal-create-dialog .el-dialog__header) {
  display: none;
}

:deep(.goal-create-dialog .el-dialog__body) {
  padding: 0;
}

.goal-create-panel {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 54px rgba(25, 28, 30, 0.14);
}

.goal-create-glow {
  position: absolute;
  top: -120px;
  right: -120px;
  width: 280px;
  height: 280px;
  border-radius: 999px;
  background: rgba(255, 217, 228, 0.72);
  filter: blur(26px);
  pointer-events: none;
}

.goal-create-content {
  position: relative;
  z-index: 1;
  padding: 30px;
}

.goal-create-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(217, 192, 199, 0.36);
}

.goal-create-header h2 {
  margin: 0;
  color: #191c1e;
  font-size: 24px;
  font-weight: 900;
  line-height: 1.25;
  letter-spacing: 0;
}

.goal-create-header p {
  margin: 8px 0 0;
  color: #544248;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
}

.goal-create-icon {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 999px;
  background: #f47fb0;
  color: #701343;
}

.goal-create-icon svg {
  width: 26px;
  height: 26px;
  fill: currentColor;
}

.goal-create-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.goal-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.goal-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 7px;
}

.goal-field span,
.goal-progress-field span,
.goal-status-field > span {
  color: #544248;
  font-size: 12px;
  font-weight: 850;
}

.goal-field input,
.goal-field textarea,
.goal-field select {
  width: 100%;
  min-width: 0;
  border: 1px solid rgba(217, 192, 199, 0.56);
  border-radius: 10px;
  background: #f7f9fb;
  color: #191c1e;
  font: inherit;
  font-size: 14px;
  line-height: 1.35;
  outline: none;
  transition: border-color 160ms ease, box-shadow 160ms ease, background 160ms ease;
}

.goal-field input,
.goal-field select {
  height: 42px;
  padding: 0 14px;
}

.goal-field textarea {
  resize: none;
  padding: 12px 14px;
}

.goal-field input:focus,
.goal-field textarea:focus,
.goal-field select:focus {
  border-color: #9e3a68;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(158, 58, 104, 0.12);
}

.goal-progress-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goal-progress-field > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.goal-progress-field strong {
  color: #9e3a68;
  font-size: 13px;
  font-weight: 900;
}

.goal-progress-field input {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  accent-color: #9e3a68;
  cursor: pointer;
}

.goal-status-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goal-status-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.goal-status-pill {
  display: inline-flex;
  min-height: 34px;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border: 1px solid rgba(217, 192, 199, 0.72);
  border-radius: 999px;
  color: #544248;
  cursor: pointer;
  font-size: 13px;
  font-weight: 850;
  transition: transform 160ms ease, box-shadow 160ms ease, background 160ms ease, border-color 160ms ease, color 160ms ease;
}

.goal-status-pill input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.goal-status-pill:hover {
  transform: translateY(-1px);
}

.goal-status-pill.active {
  border-color: transparent;
  box-shadow: 0 8px 18px rgba(25, 28, 30, 0.08);
}

.goal-status-pill.pink.active {
  background: #f47fb0;
  color: #701343;
}

.goal-status-pill.blue.active {
  background: #d4e3ff;
  color: #001c39;
}

.goal-status-pill.gray.active {
  background: #e6e8ea;
  color: #544248;
}

.goal-create-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
  padding-top: 20px;
  border-top: 1px solid rgba(217, 192, 199, 0.36);
}

.goal-cancel-button,
.goal-submit-button {
  min-height: 42px;
  padding: 0 22px;
  border-radius: 999px;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 900;
  transition: transform 160ms ease, box-shadow 160ms ease, background 160ms ease, border-color 160ms ease;
}

.goal-cancel-button {
  border: 1px solid rgba(217, 192, 199, 0.88);
  background: #ffffff;
  color: #9e3a68;
}

.goal-submit-button {
  border: 0;
  background: #9e3a68;
  color: #ffffff;
  box-shadow: 0 4px 14px rgba(158, 58, 104, 0.28);
}

.goal-cancel-button:hover,
.goal-submit-button:hover {
  transform: translateY(-2px);
}

.goal-submit-button:hover {
  background: #802150;
}

.goal-cancel-button:disabled,
.goal-submit-button:disabled {
  cursor: not-allowed;
  opacity: 0.72;
  transform: none;
}

@media (max-width: 720px) {
  .goal-create-content {
    padding: 22px;
  }

  .goal-create-header,
  .goal-form-grid,
  .goal-create-footer {
    grid-template-columns: 1fr;
  }

  .goal-form-grid {
    display: flex;
    flex-direction: column;
  }

  .goal-create-footer {
    flex-direction: column-reverse;
  }

  .goal-cancel-button,
  .goal-submit-button {
    width: 100%;
  }
}
</style>
