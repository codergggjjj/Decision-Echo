import { API_PATHS } from '../constants/apis'
import request from '../utils/request'

export function getDecisionDashboard() {
  return request.get(API_PATHS.decisionDashboard)
}

export function createDecision(data) {
  return request.post(API_PATHS.decisions, data)
}

export function reviewDecision(id, data) {
  return request.put(`${API_PATHS.decisions}/${id}/review`, data)
}
