import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import LoginView from '../views/login/LoginView.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import ProfileView from '../views/profile/ProfileView.vue'
import AnalysisView from '../views/analysis/AnalysisView.vue'
import GoalListView from '../views/goal/GoalListView.vue'
import GoalDetailView from '../views/goal/GoalDetailView.vue'
import ErrorView from '../views/error/ErrorView.vue'
import AdminView from '../views/admin/AdminView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', component: LoginView },
    { path: '/dashboard', component: DashboardView, meta: { requiresAuth: true } },
    { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
    { path: '/analysis', component: AnalysisView, meta: { requiresAuth: true } },
    { path: '/goals', component: GoalListView, meta: { requiresAuth: true } },
    { path: '/goals/:id', component: GoalDetailView, meta: { requiresAuth: true } },
    { path: '/admin', component: AdminView, meta: { requiresAuth: true, requiresAdmin: true } },
    {
      path: '/403',
      component: ErrorView,
      props: {
        code: '403',
        kicker: '访问受限',
        title: '你暂时没有权限访问这里',
        description: '这个入口先为权限场景预留，回到首页继续记录和回看你的决策。',
        tone: 'yellow'
      }
    },
    {
      path: '/404',
      component: ErrorView,
      props: {
        code: '404',
        kicker: '找不到页面',
        title: '页面走丢了',
        description: '可能是链接写错了，或者这个页面已经被移动。先回到首页继续看看吧。',
        tone: 'blue'
      }
    },
    {
      path: '/500',
      component: ErrorView,
      props: {
        code: '500',
        kicker: '系统异常',
        title: '系统开小差了',
        description: '页面暂时没有顺利加载，可以重新尝试，或者先返回首页。',
        tone: 'pink',
        showRetry: true
      }
    },
    { path: '/:pathMatch(.*)*', redirect: '/404' }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return '/login'
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return '/403'
  }
  if (authStore.isAdmin && to.meta.requiresAuth && !to.meta.requiresAdmin) {
    return '/admin'
  }
  if (to.path === '/login' && authStore.isLoggedIn) {
    return authStore.isAdmin ? '/admin' : '/dashboard'
  }
  return true
})

export default router
