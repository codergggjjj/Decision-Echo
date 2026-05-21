import request from '../utils/request'
import { API_PATHS } from '../constants/apis'

export function getSatisfactionPie(params) {
  return request.get(API_PATHS.analysisSatisfactionPie, { params })
}

export function getTrendLine(params) {
  return request.get(API_PATHS.analysisTrendLine, { params })
}
