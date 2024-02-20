<template>
  <!-- -->
  <div class="composite">
    <slot name="prepend"></slot>
    <el-input
      v-if="$attrs.type === 'input'"
      clearable
      @keyup.enter="$attrs.onEnterhanlder"
      :placeholder="getPlacehloder($attrs.selectvalue, $attrs.options)"
      v-model.trim="vals"
    ></el-input>
    <el-date-picker
      v-if="['datetimerange', 'daterange'].includes($attrs.type)"
      v-model="vals"
      :type="$attrs.type"
      @change="datechange($event, $attrs.model)"
      unlink-panels
      range-separator="至"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      :shortcuts="shortcuts"
    >
    </el-date-picker>
  </div>
  <!-- <abc type='input' v-model="formInline[item.model]"/> -->
</template>

<script>
import { defineComponent, ref, computed, PropType } from 'vue';
import useShortcuts, { timerChange } from './useShortcuts';
import dayjs from 'dayjs';
export default defineComponent({
  name: 'Composite',
  props: {
    modelValue: [String, Number, Array],
    compostiteType: {
      type: String,
      default: ''
    }
  },
  emits: {
    'update:modelValue': null,
    'update:enterhanlder': null,
    'update:compostiteType': null,
    change: null
  },
  components: {},
  setup(prop, ctx) {
    const datechange = (e, m) => {
      ctx.emit('change', e, m);
    };
    // const vals = ref(prop.modelValue);
    const datearr = ref(['datetimerange', 'daterange']);
    const vals = computed({
      get: () => prop.modelValue,
      set: value => ctx.emit('update:modelValue', value)
    });
    const compostiteType = computed({
      get: () => prop.compostiteType,
      set: value => ctx.emit('update:compostiteType', value)
    });

    if (compostiteType.value) {
      if (compostiteType.value && datearr.value.includes(compostiteType.value)) {
        if (compostiteType.value === 'daterange') {
          vals.value = [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')];
        } else {
          vals.value = [dayjs().format('YYYY-MM-DD 00:00:00'), dayjs().format('YYYY-MM-DD 23:59:59')];
        }
      }
    }

    function disabledDate(current) {
      // return current.getTime() < Date.now();
      const time = timerChange(30);
      return current.getTime() < time[0] || current.getTime() > time[1];
    }

    const getPlacehloder = (selectVal, options) => {
      const val = options.find(item => item.value === selectVal);
      if (val) {
        return val.placeholder;
      } else {
        return '请输入内容';
      }
    };

    const { shortcuts } = useShortcuts();
    return {
      vals,
      getPlacehloder,
      shortcuts,
      datechange,
      disabledDate,
    };
  }
});
</script>

<style scoped lang="scss">
.composite {
  display: flex;
}
</style>
