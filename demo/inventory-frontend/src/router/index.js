import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('@/views/LayoutView.vue'),
    redirect: '/products',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/ProductView.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/AdminUsers.vue'),
        meta: { title: '用户管理', admin: true }
      },
      {
        path: 'admin/records',
        name: 'AdminRecords',
        component: () => import('@/views/AdminRecords.vue'),
        meta: { title: '出入库记录', admin: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.guest && auth.isLoggedIn) return next('/')
  if (!to.meta.guest && !auth.isLoggedIn) return next('/login')
  if (to.meta.admin && !auth.isAdmin) return next('/products')
  next()
})

export default router
