import { createRouter, createWebHistory } from 'vue-router';
import store from '../store';

export const routes = [
  {
    path: '/',
    redirect: '/card'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/Login.vue')
  }
]

export const authRoutes = [
  // {
  //   path: '/home',
  //   name: 'Home',
  //   redirect: "/home/count",
  //   component: () => import('../views/layout/default/index.vue'),
  //   meta: {
  //     "title": "首页管理",
  //     icon: 'el-icon-printer',
  //   },
  //   // redirect: '/pay',
  //   children: [
  //     /**二级路由*/
  //     {
  //       path: 'count',
  //       name: 'count',
  //       component: () => import('../views/pay/Pay.vue'),
  //       meta: {
  //         "title": "数据统计"
  //       },
  //     },
  //     {
  //       path: 'review',
  //       name: 'Review',
  //       component: () => import('../views/pay/Pay.vue'),
  //       meta: {
  //         "title": "体现订单审核"
  //       },
  //     },
  //     {
  //       path: 'circount',
  //       name: 'Circount',
  //       component: () => import('../views/pay/Pay.vue'),
  //       meta: {
  //         "title": "流通数据统计"
  //       },
  //     }
  //   ]
  // },
  {
    path: '/card',
    name: 'Card',
    redirect: '/card/dashboard',
    component: () => import('../views/layout/default/index.vue'),
    meta: {
      "title": "代理管理",
      icon: 'el-icon-folder',
    },
    // redirect: '/pay',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard'),
        meta: {
          "title": "代理信息"
        },
      },
      /**二级路由*/
      {
        path: 'merchant-list',
        name: 'MerchantList',
        component: () => import('../views/merchant/MerchantList.vue'),
        meta: {
          "title": "商户列表"
        },
      },
      {
        path: 'wthdrawalapplication',
        name: 'WithdrawalApplication',
        component: () => import('../views/wthdrawal-application/index.vue'),
        meta: {
          "title": "充值记录"
        },
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/order/Index.vue'),
        meta: {
          "title": "商户提现审核"
        },
      },
    ]
  },
  
]

const router = createRouter({
  history: createWebHistory('/'),
  routes: [...routes, ...authRoutes]
});

router.beforeEach((to, from, next) => {
  // console.log("----",to.path, )
  // if (to.path !== '/login' && !store.state.userInfo.login) {
  //   next('/login');
  // } else {
  //   next();
  // }

  if (to.path !== '/login') {
    const findObj = store.state.toolList.find(item => item['url'] == to.href);
    if (!findObj) {
      store.commit('setToolList', {
        name: to.meta.title || 'Not',
        url: to.href
      })
    }
  }

  // 如果去login，如果登录了，去home
  // 如果去login，没登录 next
  // 如果不去login，如果没登录，去login
  // 如果不去login，如果登录了，next
  if (to.path == '/login' && store.state.userInfo.login) {
    next('/card')
  } else if (to.path == '/login' && !store.state.userInfo.login) {
    next()
  } else if (to.path !== '/login' && !store.state.userInfo.login) {
    next('/login')
  } else if (to.path !== '/login' && !store.state.userInfo.login) {
    next()
  } else {
    console.error(123)
    next()
  }
});

export default router;
