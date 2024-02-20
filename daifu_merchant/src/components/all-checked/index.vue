<template>
  <div :class="{ displayline: $attrs.isline }">
    <!-- {{ checkAll }}
    {{ isIndeterminate }} -->
    <el-checkbox :indeterminate="isIndeterminate" :disabled="$attrs.disabled" v-model="checkAll" @change="handleCheckAllChange">
      全部
    </el-checkbox>
    <el-checkbox-group v-model="checkedCities" :disabled="$attrs.disabled" @change="handleCheckedCitiesChange">
      <el-checkbox v-for="city in cities" :label="city.value" :key="city.value">{{ city.label }}</el-checkbox>
    </el-checkbox-group>
  </div>
</template>

<script >
import { computed, defineComponent, PropType, ref, watch } from 'vue';

export default defineComponent({
  name: 'AllChecked',
  props: {
    selectOptionsProp: {
      // 所有选择项目
      type: Array,
      default: []
    },
    modelValue: {
      // 选中的设备
      type: [Array, String],
      default: []
    },
    valueDefaultType: {
      // 默认双向绑定的类型
      type: String,
      default: 'string'
    }
  },
  emits: ['update:modelValue'],
  setup(props, ctx) {
    // checkedCities
    const selectOptions = computed(() =>
      props.selectOptionsProp.map(it => {
        const obj = Object.assign({}, it, { value: String(it.value) });
        return obj;
      })
    );
    const { t } = useI18n({ useScope: 'global' });
    const mvComputed = computed<any>({
      get: () => {
        if (props.modelValue) {
          if (typeof props.modelValue === 'string') {
            return props.modelValue.split(',').filter((t) => t);
            // .map((t: any) => {
            //   return Number.isNaN(Number(t)) ? t : Number(t);
            // });
          } else {
            return props.modelValue;
          }
        } else {
          return [];
        }
      },
      set: val => ctx.emit('update:modelValue', val.toString())
    });
    const checkAll = ref<boolean>(false);
    const checkedCities = ref();

    const cities = ref<any>(selectOptions);
    const isIndeterminate = ref(true);
    watch(
      () => mvComputed.value,
      n => {
        checkedCities.value = mvComputed.value;
        checkAll.value = mvComputed.value.length === cities.value.length;
        if (mvComputed.value.length === cities.value.length && mvComputed.value.length) {
          isIndeterminate.value = false;
        }
        // if (mvComputed.value.length === cities.value.length) {
        //   console.error(true);
        // }
      },
      {
        immediate: true,
        deep: true
      }
    );

    function handleCheckAllChange(val) {
      checkedCities.value = val ? cities.value.map((p) => p.value) : [];
      isIndeterminate.value = false;
      setCheckedCitids();
    }
    function handleCheckedCitiesChange(value) {
      let checkedCount = value.length;
      // 上面使用watch给checkAll赋值，先注释掉，有问题解开
      // checkAll.value = checkedCount === cities.value.length;
      isIndeterminate.value = checkedCount > 0 && checkedCount < cities.value.length;
      setCheckedCitids();
    }
    function setCheckedCitids() {
      mvComputed.value = checkedCities.value;
    }
    return {
      checkAll,
      checkedCities,
      cities,
      isIndeterminate,
      handleCheckAllChange,
      handleCheckedCitiesChange,
      selectOptions
    };
  }
});
</script>
<style lang="scss" scoped>
.displayline {
  display: flex;
  ::v-deep(.el-checkbox) {
    margin-right: 20px;
  }
}
</style>
