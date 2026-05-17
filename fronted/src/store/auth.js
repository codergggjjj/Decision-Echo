import { defineStore } from 'pinia'
import { getStoredUser, getToken, removeStoredUser, removeToken, setStoredUser, setToken } from '../utils/token'
import { getCurrentUser, login as loginApi, logout as logoutApi } from '../api/auth'

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
    async loadCurrentUser() {
      const user = await getCurrentUser()
      this.user = user
      setStoredUser(user)
      return user
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
    }
  }
})
