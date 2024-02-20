<template>
  <div class="doubleinput" :class="{ noSubmit: mc ? false : true }">
    <el-input placeholder="起" v-model.trim="min" @blur="change(0)" clearable> </el-input>
      至
    <el-input placeholder="止" v-model.trim="max" @blur="change(1)" clearable> </el-input>
  </div>
  <p class="notError" v-if="!mc">必须同时有值</p>
</template>

<script>
import { computed, defineComponent, watch } from 'vue';
export default defineComponent({
  name: 'DataAmount',
  props: {
    min: {
      type: [Number, String]
    },
    max: {
      type: [Number, String]
    }
  },
  emits: ['update:min', 'update:max'],
  setup(props, ctx) {
    const reg = /^\d+(\.\d{0,2})?$/;
    const min = computed({
      get: () => props.min,
      set: value => {
        ctx.emit('update:min', value);
      }
    });
    const mc = computed(() => {
      if ((!min.value && !max.value) || (min.value && max.value)) {
        return true;
      } else {
        return false;
      }
    });
    watch(
      () => min.value,
      n => {
        if (n == '') {
          max.value = '';
        }
      }
    );
    const max = computed({
      get: () => props.max,
      set: value => {
        ctx.emit('update:max', value);
      }
    });
    const change = (type) => {
      if (type) {
        const val = Number.parseFloat(max.value);
        if (Number.isNaN(val)) {
          max.value = '';
        } else {
          max.value = val.toFixed(0);
        }
      } else {
        const val = Number.parseFloat(min.value);
        if (Number.isNaN(val)) {
          min.value = '';
        } else {
          min.value = val.toFixed(0);
        }
      }
      // 判断max不能小于min
      if (Number(max.value) < Number(min.value)) {
        max.value = min.value;
      }
    };
    return {
      min,
      max,
      change,
      mc,
    };
  }
});
</script>
<style lang="less" scoped>
.doubleinput {
  display: flex;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  box-sizing: border-box;
}
.doubleinput ::v-deep(.el-input__inner) {
  border: none;
  width: 100px;
  text-align: center;
}
.noSubmit {
  border: 1px solid #f5222d;
}
.notError {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
}
</style>
