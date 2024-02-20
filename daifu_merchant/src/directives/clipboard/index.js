import { Directive, DirectiveBinding } from 'vue';
import { ElMessage } from 'element-plus';

function copyText(text) {
  if (!text) return;
  const input = document.getElementById('clipboard-ym');
  if (input) {
    input.value = text;
    input.select();
    if (document.execCommand('copy')) {
      ElMessage.success({
        message: "复制成功",
        type: 'success'
      });
    } else {
      ElMessage.error("复制失败");
    }
  }
}

export const clipboard = {
  updated(el, binding) {
    el.selectValue = binding.value;
  },
  beforeMount(el, binding) {
    el.clickHanlder = () => {
      copyText(el.selectValue);
    };
  },
  mounted(el, binding) {
    el.selectValue = binding.value;
    el.addEventListener('click', el.clickHanlder);
  },
  unmounted(el, binding) {
    el.removeEventListener('click', el.clickHanlder);
  }
};
