import { defineStore } from 'pinia'
import { getStoredUser, getToken, removeStoredUser, removeToken, setStoredUser, setToken } from '../utils/token'
import {
  changePassword as changePasswordApi,
  getCurrentUser,
  login as loginApi,
  logout as logoutApi,
  register as registerApi,
  updateProfile as updateProfileApi,
  uploadAvatar as uploadAvatarApi
} from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: getStoredUser()
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.token
      this.user = data.user
      setToken(data.token)
      setStoredUser(data.user)
      return data
    },
    async register(payload) {
      const data = await registerApi(payload)
      this.token = data.token
      this.user = data.user
      setToken(data.token)
      setStoredUser(data.user)
      return data
    },
    async loadCurrentUser() {
      const user = await getCurrentUser()
      this.user = user
      setStoredUser(user)
      return user
    },
    async updateProfile(payload) {
      const user = await updateProfileApi(payload)
      this.user = user
      setStoredUser(user)
      return user
    },
    async uploadAvatar(file) {
      return uploadAvatarApi(file)
    },
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.token = null
        this.user = null
        removeToken()
        removeStoredUser()
      }
    },
    async changePassword(payload) {
      await changePasswordApi(payload)
      this.token = null
      this.user = null
      removeToken()
      removeStoredUser()
    }
  }
})
