import { API_PATHS } from '../constants/apis'
import request from '../utils/request'

const SATISFACTION_LABELS = ['满意', '一般', '后悔']

export async function getSatisfactionPie() {
  const dashboard = await request.get(API_PATHS.decisionDashboard)
  const satisfaction = dashboard?.summary?.satisfaction || {}
  const items = SATISFACTION_LABELS.map((label) => ({
    name: label,
    value: satisfaction[label] || 0
  }))

  return {
    total: items.reduce((sum, item) => sum + item.value, 0),
    items
  }
}
