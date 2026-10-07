import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: () => {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return userInfo.role === 'ADMIN' ? '/dashboard' : '/browse'
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer', roles: ['ADMIN'] }
      },
      {
        path: 'books',
        name: 'Books',
        component: () => import('@/views/Books.vue'),
        meta: { title: '图书管理', icon: 'Reading', roles: ['ADMIN'] }
      },
      {
        path: 'borrow',
        name: 'Borrow',
        component: () => import('@/views/Borrow.vue'),
        meta: { title: '借阅管理', icon: 'Tickets', roles: ['ADMIN'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '读者管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/Categories.vue'),
        meta: { title: '分类管理', icon: 'Files', roles: ['ADMIN'] }
      },
      {
        path: 'browse',
        name: 'BookBrowse',
        component: () => import('@/views/BookBrowse.vue'),
        meta: { title: '图书借阅', icon: 'Reading', roles: ['ADMIN', 'READER'] }
      },
      {
        path: 'browse/:id',
        name: 'BookDetail',
        component: () => import('@/views/BookDetail.vue'),
        meta: { title: '图书详情', icon: 'Reading', roles: ['ADMIN', 'READER'], hidden: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '我的借阅', icon: 'Tickets', roles: ['ADMIN', 'READER'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '个人信息', icon: 'User', roles: ['ADMIN', 'READER'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next(userInfo.role === 'ADMIN' ? '/dashboard' : '/browse')
  } else if (to.meta.roles && !to.meta.roles.includes(userInfo.role)) {
    next(userInfo.role === 'ADMIN' ? '/dashboard' : '/browse')
  } else {
    next()
  }
})

export default router
