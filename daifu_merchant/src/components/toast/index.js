import { defineComponent, isVNode, createVNode, render } from 'vue';
import toastComponent from './instance.vue';

const ToastConstructor = defineComponent(toastComponent);

let instance;
let instances = [];
let isMultiple = false; // 是否同时存在多个
let zIndex = 3000;
const Toast = options => {
  if (window === undefined) return;

  if (typeof options === 'string') {
    options = {
      message: options
    };
  }
  mergeOptions(options);

  // 筛选出未关闭的toast, 已关闭的从dom移除
  instances = instances.filter(item => {
    const { show } = item.component.proxy;
    return show;
  });

  // 清空显示
  if (instances.length && !isMultiple) {
    instances.forEach(item => {
      item.component.proxy.close();
    });
  }
  // 创建新的toast
  instance = createVNode(ToastConstructor, options, isVNode(options.message) ? { default: () => options.message } : null);

  const container = document.createElement('div');
  render(instance, container);
  instance.close = () => {
    instance.component.proxy.close();
  };

  instances.push(instance);
  return instance;
};
Toast.multiple = value => {
  isMultiple = value;
};

const mergeOptions = options => {
  zIndex++;
  return {
    ...options,
    zIndex
  };
};

export default Toast;
