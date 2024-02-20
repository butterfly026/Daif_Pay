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
    path: '/channel',
    name: 'Channel',
    redirect: '/channel/list',
    component: () => import('../views/layout/default/index.vue'),
    meta: {
      "title": "上游渠道管理",
      icon: 'el-icon-s-operation',
    },
    // redirect: '/pay',
    children: [
      /**二级路由*/
      {
        path: 'list',
        name: 'list',
        component: () => import('../views/channel/index.vue'),
        meta: {
          "title": "渠道管理"
        },
      },
    ]
  },
  {
    path: '/card',
    name: 'Card',
    redirect: '/card/merchant-list',
    component: () => import('../views/layout/default/index.vue'),
    meta: {
      "title": "商户管理",
      icon: 'el-icon-folder',
    },
    // redirect: '/pay',
    children: [
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
        path: 'merchant-admin-list',
        name: 'MerchantAdminList',
        component: () => import('../views/merchant/MerchantAdminList.vue'),
        meta: {
          "title": "商户管理员列表"
        },
      },
      {
        path: 'merchant-agent-list',
        name: 'MerchantAgentList',
        component: () => import('../views/merchant/MerchantAgentList.vue'),
        meta: {
          "title": "商户代理后台"
        },
      },
      {
        path: 'white-list',
        name: 'Whitelist',
        component: () => import('../views/merchant/Whitelist.vue'),
        meta: {
          "title": "IP白名单(API)"
        },
      },
      {
        path: 'merchant-white-list',
        name: 'MerchantWhitelist',
        component: () => import('../views/merchant/MerchantWhitelist.vue'),
        meta: {
          "title": "IP白名单(商户后台)"
        },
      }
    ]
  },
  {
    path: '/order',
    name: 'Order',
    redirect: '/order/order',
    component: () => import('../views/layout/default/index.vue'),
    meta: {
      "title": "订单管理",
      icon: 'el-icon-s-order',
    },
    // redirect: '/pay',
    children: [
      /**二级路由*/
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/order/Index.vue'),
        meta: {
          "title": "商户提现审核"
        },
      },
      {
        path: 'review',
        name: 'Review',
        component: () => import('../views/order/RechargeReview.vue'),
        meta: {
          "title": "商户充值审核"
        },
      },
      {
        path: 'transaction',
        name: 'Transaction',
        component: () => import('../views/order/Transaction.vue'),
        meta: {
          "title": "商户账变记录"
        },
      }
    ]
  },
  {
    path: '/permission',
    name: 'Permission',
    component: () => import('../views/layout/default/index.vue'),
    // redirect: '/permission/roleModule',
    meta: {
      icon: 'el-icon-s-platform',
      title: '系统管理'
    },
    children: [
      {
        path: 'systemAccount',
        name: 'SystemAccount',
        component: () => import(/* webpackChunkName: "permission-system-account" */ '@/views/permission/systemAccount/index.vue'),
        meta: {
          title: '管理员列表',
          noCache: true
        }
      },
      {
        path: 'rolePermission',
        name: 'RolePermission',
        component: () => import(/* webpackChunkName: "permission-role-permision" */ '@/views/permission/rolePermission/index.vue'),
        meta: {
          title: '用户组列表'
        }
      },
      // // {
      // //   path: 'roleModules',
      // //   name: 'RoleModules',
      // //   component: () => import(/* webpackChunkName: "permission-role-module" */ '@/views/permission/roleModules/index.vue'),
      // //   meta: {
      // //     name: 'routes.system_manage_children.roleModules'
      // //   }
      // // },
      // {
      //   path: 'accessAuthorization',
      //   name: 'AccessAuthorization',
      //   component: () => import(/* webpackChunkName: "permission-access-authorization" */ '@/views/permission/accessAuthorization/index.vue'),
      //   meta: {
      //     name: 'routes.system_manage_children.accessAuthorization'
      //   }
      // },
      {
        path: 'logManagement',
        name: 'LogManagement',
        component: () => import(/* webpackChunkName: "permission-log-management" */ '@/views/permission/logManagement/index.vue'),
        meta: {
           title: '系统日志'
        } 
      },
      // {
      //   path: 'systemMessage',
      //   name: 'SystemMessage',
      //   component: () => import(/* webpackChunkName: "permission-system-message" */ '@/views/permission/systemMessage/index.vue'),
      //   meta: {
      //     name: 'routes.system_manage_children.systemMessage'
      //   }
      // },
      // {
      //   path: 'clientLimit',
      //   name: 'ClientLimit',
      //   component: () => import(/* webpackChunkName: "permission-client-limit" */ '@/views/permission/clientLimit/index.vue'),
      //   meta: {
      //     name: 'routes.system_manage_children.clientLimit'
      //   }
      // }
    ]
  }
  // {
  //   path: '/system',
  //   name: 'System',
  //   redirect: '/system/count',
  //   component: () => import('../views/layout/default/index.vue'),
  //   meta: {
  //     "title": "系统管理"
  //   },
  //   // redirect: '/pay',
  //   children: [
  //     /**二级路由*/
  //     {
  //       path: 'count',
  //       name: 'Count',
  //       component: () => import('../views/pay/Pay.vue'),
  //       meta: {
  //         "title": "系统管理"
  //       },
  //     },
  //   ]
  // }
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
  } else if (to.path !== '/login' && store.state.userInfo.login) {
    next()
  } else {
    console.error(123)
    next()
  }
});

export default router;
