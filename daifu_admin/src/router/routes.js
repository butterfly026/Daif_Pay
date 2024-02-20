export const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/layout',
    name: 'index',
    component: () => import('../views/layout/Index.vue'),
    redirect: '/index/pay',
    children: [
      /**二级路由*/
      {
        path: '/pay',
        name: 'pay',
        redirect: '/pay',
        component: () => import('../views/pay/Pay.vue'),
        children: [],
      },
      {
        path: '/:catchAll(.*)',
        redirect: '/index'
      }
    ]
  },
  {
    path: '/:catchAll(.*)',
    redirect: '/card'
  }
]
