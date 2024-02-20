import { useRoute, useRouter } from 'vue-router';

// 路由返回
export default function useBackRouter(defaultPath = 'home') {
  const router = useRouter();
  const route = useRoute();
  const { query, path } = router.currentRoute.value;
  const key = `${path}-from-route`;
  const firstRoute = sessionStorage.getItem(key);
  // 记录首次 进入页面路由
  if (query.from !== firstRoute && query.from !== undefined) {
    sessionStorage.setItem(key, String(query.from));
  }
  function goBack() {
    const fromPath = sessionStorage.getItem(key);
    // 如果已存第一次访问路由跳转对应路由
    const toPath = fromPath || firstRoute || defaultPath;
    router.push(`/${toPath}`);
  }
  const goTo = (p, q) => {
    q ? router.push({ path: p, query: { from: route.path.replace('/', ''), ...q } }): router.push({ path: p })
  };
  return {
    goBack,
    goTo
  };
}
