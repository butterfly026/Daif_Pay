__webpack_public_path__ = ENV === 'dev' ? '/' : '/';
// console.log('__webpack_public_path__', __webpack_public_path__, ENV, VERSION);

import { createApp } from 'vue';
import router from '@/router/index';
import store from '@/store';
import App from '@/App.vue';
import * as directives from './directives/index';
import ElementPlus from 'element-plus';
import 'element-plus/lib/theme-chalk/index.css';
import VXETable from 'vxe-table';
import 'vxe-table/lib/style.css';


import * as utils from '@/utils/common';
const APP = createApp(App);

APP.use(ElementPlus, { size: 'small'});
APP.use(VXETable);

APP.config.globalProperties.$utils = utils;

// 注册自定义指令
// tslint:disable-next-line: no-shadowed-variable
Object.keys(directives).forEach(key => {
  APP.directive(key, (directives)[key]);
});

APP.use(store)
  .use(router)
  .mount('#app');
