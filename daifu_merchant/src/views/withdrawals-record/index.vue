<template>
  <Content :key="key" :reset="reset" />
</template>
<script>
import { defineComponent, ref } from 'vue';
import Content from './content.vue'

export default defineComponent({
  components: {Content},
  setup() {
    const key = ref(1);
    const reset = () => {
      key.value++;
    }
    return {
      reset,
      key
    };
  }
});
</script>

<style scoped lang="less">
.upload-box {
  display: flex;
  .upload-box-left {
    width: 13%;
    display: flex;
    justify-items: center;
    align-items: center;
    padding-left: 20px;
    font-size: 18px;
    font-weight: 600;
  }
  .upload-box-right {
    .shili {
      margin: 8px 0;
    }
    .drop {
      margin-top: 10px;
    }
  }
}
</style>