import { API_PATHS } from '../constants/apis'
import request from '../utils/request'

export function getAdminStats() {
  return request.get(API_PATHS.adminStats)
}

export function getAdminUsers(params) {
  return request.get(API_PATHS.adminUsers, { params })
}

export function getAdminDecisions(params) {
  return request.get(API_PATHS.adminDecisions, { params })
}
