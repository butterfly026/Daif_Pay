<template>
  <!-- <ElConfigProvider :locale="zhCn"> -->
    <router-view></router-view>
  <!-- </ElConfigProvider> -->
</template>
<script>
import { onMounted, ref } from 'vue';
import Event from '@/event';
import { useRouter, useRoute } from 'vue-router';
import Toast from '@/components/toast';
//import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

export default {
  components: {
  //  ElConfigProvider
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const loading = ref(false);

    const handlerLoading = state => {
      loading.value = state.loading;
    };

    const handlerToast = state => {
      if (state && state.data) {
        if (state.data === 'token') Toast('登录已过期');
        else if (state.data === 502) Toast('网络异常');
        else if (state.data === 'priv') Toast('当前账号没有权限访问！');
        else Toast(state.data);
      }
    };

    const handlerToken = state => {
      if (state.login === false) {
        router.replace({ path: '/login', redirect: route.path });
      }
    };

    onMounted(() => {
      Event.on('loading', handlerLoading);
      Event.on('toast', handlerToast);
      Event.on('login', handlerToken);
    });

    return {
      loading,
      zhCn
    };
  }
};
</script>
<style type="text/css" lang="less" src='./assets/css/index.less'></style>
