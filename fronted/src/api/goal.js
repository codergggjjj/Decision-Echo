import { API_PATHS } from '../constants/apis'
import request from '../utils/request'

export function getGoals(params) {
  return request.get(API_PATHS.goals, { params })
}

export function getGoalDetail(id) {
  return request.get(`${API_PATHS.goals}/${id}`)
}
