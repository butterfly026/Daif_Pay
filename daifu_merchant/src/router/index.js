import { createRouter, createWebHistory } from 'vue-router';
import store from '../store';

export const routes = [
  {
    path: '/',
    redirect: '/vendor/manufactureruserlist'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/Login.vue')
  }
]

export const authRoutes = [
    {
      path: '/vendor',
      name: 'Vendor',
      redirect: "/vendor/manufactureruserlist ",
      component: () => import('../views/layout/default/index.vue'),
      meta: {
        "title": "厂商管理",
        icon: 'el-icon-printer',
      },
      // redirect: '/pay',
      children: [
        /**二级路由*/

        {
          path: 'manufactureruserlist',
          name: 'ManufacturerUserList',
          component: () => import('../views/manufacturer-user-list/index.vue'),
          meta: {
            "title": "商户信息"
          },
        },
        {
          path: 'member',
          name: 'Member',
          component: () => import('../views/member/index.vue'),
          meta: {
            "title": "会员管理"
          },
        },
        {
          path: 'initiaterecharge',
          name: 'InitiateRecharge',
          component: () => import('../views/initiate-recharge/index.vue'),
          meta: {
            "title": "发起充值"
          },
        },
        {
          path: 'rechargerecord',
          name: 'RechargeRecord',
          component: () => import('../views/recharge-record/index.vue'),
          meta: {
            "title": "发起单笔提现"
          },
        },
        {
          path: 'withdrawalsrecord',
          name: 'WithdrawalsRecord',
          component: () => import('../views/withdrawals-record/index.vue'),
          meta: {
            "title": "发起批量提现"
          },
        },
        {
          path: 'fundingdetails',
          name: 'FundingDetails',
          component: () => import('../views/funding-details/index.vue'),
          meta: {
            "title": "提现记录"
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
          path: 'withdrawalapplication-details',
          name: 'WithdrawalApplicationDetails',
          component: () => import('../views/withdrawal-application-details/index.vue'),
          meta: {
            "title": "账变记录"
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
    next('/vendor/manufactureruserlist')
  } else if (to.path == '/login' && !store.state.userInfo.login) {
    next()
  } else if (to.path !== '/login' && !store.state.userInfo.login) {
    next('/login')
  } else if (to.path !== '/login' && store.state.userInfo.login) {
    next()
  } else {
    console.error(123)
    next()
  }
});

export default router;
