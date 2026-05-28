import { API_PATHS } from '../constants/apis'
import request from '../utils/request'

export function getGoals(params) {
  return request.get(API_PATHS.goals, { params })
}

export function getGoalDetail(id) {
  return request.get(`${API_PATHS.goals}/${id}`)
}

export function createGoal(data) {
  return request.post(API_PATHS.goals, data)
}

export function recommendGoals(tags) {
  return request.post(`${API_PATHS.goals}/recommend`, { tags })
}
