import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import LoginView from '../views/login/LoginView.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', component: LoginView },
    { path: '/dashboard', component: DashboardView, meta: { requiresAuth: true } }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return '/login'
  }
  if (to.path === '/login' && authStore.isLoggedIn) {
    return '/dashboard'
  }
  return true
})

export default router
