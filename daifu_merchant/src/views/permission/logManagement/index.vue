<template>
  <div>
    <!-- <BMenu :name-list="nameList" /> -->
    系统日志
  </div>
</template>

<script>
import { defineComponent } from 'vue';
// import BMenu from '@/components/b-menu/index.vue';
// import SystemLog from './SystemLog/index.vue';
// import LoginLog from './LoginLog/index.vue';
// import { useI18n } from 'vue-i18n';

export default defineComponent({
  name: 'logManage',
  components: {
    // BMenu,
    // SystemLog,
    // LoginLog
  },
  setup() {
    // const { t } = useI18n({ useScope: 'global' });
    // const nameList = [
    //   {
    //     name: 'SystemLog',
    //     text: t('routes.system.system_log'),
    //     components: SystemLog
    //   },
    //   {
    //     name: 'LoginLog',
    //     text: t('routes.system.login_log'),
    //     components: LoginLog
    //   }
    // ];
    // return {
    //   nameList
    // };
  }
});
</script>
<style lang="less" scoped>
</style>
