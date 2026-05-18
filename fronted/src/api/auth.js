import { API_PATHS } from '../constants/apis'
import request from '../utils/request'

export function getCaptcha() {
  return request.get(API_PATHS.captcha)
}

export function login(data) {
  return request.post(API_PATHS.login, data)
}

export function register(data) {
  return request.post(API_PATHS.register, data)
}

export function getCurrentUser() {
  return request.get(API_PATHS.me)
}

export function changePassword(data) {
  return request.put(API_PATHS.changePassword, data)
}

export function logout() {
  return request.post(API_PATHS.logout)
}
