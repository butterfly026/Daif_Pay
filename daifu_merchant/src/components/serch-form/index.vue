<template>
  <el-form :inline="true" ref="validateForm" :rules="rules" :model="formInline" class="demo-form-inline" v-bind="$attrs">
    <el-form-item v-for="(item, i) in FormDataList" :key="i" :label="t(item.label ? item.label : '')" :prop="item.model">
      <AllChecked v-if="item.type === 'allchecked'" :selectOptionsProp="item.options" v-model="formInline[item.model]" :isline="true" />
      <DataAmount v-if="item.type === 'doubleInput'" v-model:min="formInline[item.model]" v-model:max="formInline[item.max]"> </DataAmount>
      <CompositeCompontent
        v-model="formInline[item.model]"
        v-model:compostiteType="item.selectType"
        :type="item.selectType"
        :model="item.model"
        v-if="item.type === 'composite'"
        @enterhanlder="submitForm(true)"
        @change="datechange"
        :selectvalue="item.modelName"
        :options="item.options"
      >
        <template #prepend>
          <el-select v-model="item.modelName" :style="{ width: (item.width ? item.width : 220) + 'px' }">
            <el-option v-for="cItem in item.options" :key="cItem.value" :label="cItem.label" :value="cItem.value"></el-option>
          </el-select>
        </template>
      </CompositeCompontent>
      <!-- INPUT -->
      <el-input
        v-if="item.type === undefined || item.type === 'input'"
        :clearable="true"
        v-model="formInline[item.model]"
        @keyup.enter="submitForm(true)"
        :placeholder="item.placeholder ? item.placeholder : '' + item.label"
        :style="`${item.width ? 'width: ' + item.width : ''}`"
      ></el-input>
      <!-- SELECT -->
      <el-select
        v-loading="item.loading"
        :disabled="item.disabled"
        element-loading-spinner="el-icon-loading"
        :clearable="true"
        :multiple="item.multiple"
        v-if="item.type === 'select'"
        v-model="formInline[item.model]"
        :placeholder="item.placeholder ? t(item.placeholder || '') : t(item.label || '')"
      >
        <el-option v-for="(selectItem, selectIndex) in item.options" :key="selectIndex" :label="selectItem.label" :value="selectItem.value"> </el-option>
      </el-select>
      <!-- RADIO -->
      <el-radio-group v-model="formInline[item.model]" v-if="item.type === 'radio'" :label="item.label">
        <el-radio v-for="(radioItem, radioIndex) in item.options" :key="radioIndex" :label="radioItem.value">
          {{ radioItem.label }}
        </el-radio>
      </el-radio-group>
      <!-- DATE PICKER -->
      <el-date-picker
        v-if="datearr.includes(item.type)"
        v-model="formInline[item.model]"
        @change="datechange($event, item.model)"
        :type="item.type"
        unlink-panels
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        :shortcuts="shortcuts"
      >
      </el-date-picker>
      <!-- DATE -->
      <el-date-picker
        v-if="date_list.includes(item.type)"
        v-model="formInline[item.model]"
        :type="item.type"
        :placeholder="item.placeholder"
        :default-value="new Date()"
      >
      </el-date-picker>
      <!-- MONTH -->
      <el-date-picker v-if="item.type === 'month'" v-model="formInline[item.model]" :type="item.type" :placeholder="item.placeholder"> </el-date-picker>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm(true)">查询</el-button>
      <el-button @click="resetForm">重置</el-button>
      <slot name="add"></slot>
      <slot name="csv"></slot>
    </el-form-item>
  </el-form>
</template>

<script>
import { defineComponent, ref, PropType, nextTick, onMounted, computed, watch } from 'vue';
import { ElForm } from 'element-plus';
import CompositeCompontent from './composite.vue';
import useShortcuts from './useShortcuts';
import _ from 'lodash';
import dayjs from 'dayjs';
import DataAmount from '../data-amount/index.vue';
import AllChecked from '../all-checked/index.vue';

const empty = {
  isEmpty(obj) {
    if (typeof obj === 'undefined' || obj === null || obj === '') {
      return true;
    } else {
      return false;
    }
  },
  preProcessData(formData) {
    /* 删除空值 */
    Object.keys(formData).forEach(item => {
      if (this.isEmpty(formData[item])) {
        delete formData[item];
      }
    });
    return formData;
  }
};

export default defineComponent({
  name: 'bgSerch',
  components: {
    CompositeCompontent,
    DataAmount,
    AllChecked
  },
  props: {
    FormDataList: {
      type: Int8Array,
      default: []
    },
    firstload: {
      type: Boolean,
      default: true
    },
    modelValue: {
      type: Object,
      default: {}
    },
    isCycle: {
      type: Boolean,
      default: false
    },
    jvbObject: {
      // 如果监听数据变化，这个就是变化后的obj
      type: Object,
      default: {}
    },
    isFor: {
      // 是否需要监听数据变化
      type: Boolean,
      default: false
    }
  },
  emits: ['onSubmit', 'update:modelValue', 'update:jvbObject'],
  setup(props, ctx) {
    const formInline = ref<any>({});

    const validateForm = ref(ElForm);
    const rules = ref<RefObjectKeyRulesType>({});
    const datearr = ref(['datetimerange', 'daterange']);
    const date_list = ref(['date']);
    const { shortcuts } = useShortcuts();
    const jvbObjectComputed = computed({
      get: () => props.jvbObject,
      set: value => {
        ctx.emit('update:jvbObject', value);
      }
    });

    // 时间改变事件
    const datechange = (e, m) => {
      if (e) {
        // formInline[m]
        const start = dayjs(e[0]).format('HH:mm:ss');
        const end = dayjs(e[1]).format('HH:mm:ss');
        // 说明都是为00:00:00
        if (start === end && start === '00:00:00') {
          formInline.value[m] = [dayjs(e[0]).format('YYYY-MM-DD 00:00:00'), dayjs(e[1]).format('YYYY-MM-DD 23:59:59')];
        }
      }
    };

    const modelObjectv = computed({
      get: () => props.modelValue,
      set: value => ctx.emit('update:modelValue', value)
    });

    const axiosFn = (url, bool) => {
      return new Promise((resolve, reject) => {
        resolve(true);
      });
    };
    props.FormDataList.forEach((item) => {
      if (datearr.value.includes(item.type) && !item.default) {
        if (item.type === 'daterange') {
          if (item.default !== null) {
            item.default = [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')];
          } else {
            item.default = [];
          }
        } else {
          if (item.default !== null) {
            item.default = [dayjs().format('YYYY-MM-DD 00:00:00'), dayjs().format('YYYY-MM-DD 23:59:59')];
          } else {
            item.default = [];
          }
        }
      }

      // 判断单个的时间选择
      if (item.type === 'date') {
        item.default = dayjs().format('YYYY-MM-DD');
      }
      // 判断单个的时间选择
      if (item.type === 'month') {
        item.default = dayjs().format('YYYY-MM');
      }
      formInline.value[item.model] = item.default ? item.default : '';
      // 添加rules
      if (item.rules) {
        rules.value[item.model] = item.rules;
      }
    });
    props.FormDataList.forEach(async (item) => {
      if (item.type === 'select' && item.url && Array.isArray(item.options) && item.options.length <= 0) {
        item.loading = true;
        item.options = (await axiosFn(item.url, true));
        item.loading = false;
      }
    });
    const resetForm = () => {
      nextTick(() => {
        (validateForm.value).resetFields();
      });
    };

    const submitForm = (isSerch = false) => {
      validateForm.value.validate((valid) => {
        if (valid) {
          // 判断是否存在noSubmit的class，有就不提交
          try {
            if (document.querySelector('.demo-form-inline').querySelector('.noSubmit')) {
              return;
            }
          } catch (e) {
            console.error(e);
          }
          const obj = _.cloneDeep(formInline);
          props.FormDataList.forEach(item => {
            if (item.type === 'composite') {
              if (item.selectType === 'input') {
                obj.value[item.valueModel] = obj.value[item.model];
                obj.value[item.model] = item.modelName;
              } else {
                if (!item.modelNameArray) {
                  item.modelNameArray = ['start_time', 'end_time'];
                }
                if (obj.value[item.model] && obj.value[item.model].length) {
                  // 是时间选择器

                  obj.value[item.modelNameArray[0]] = dayjs(obj.value[item.model][0]).format('YYYY-MM-DD HH:mm:ss');
                  obj.value[item.modelNameArray[1]] = dayjs(obj.value[item.model][1]).format('YYYY-MM-DD HH:mm:ss');
                  obj.value[item.model] = item.modelName;
                } else {
                  obj.value[item.modelNameArray[0]] = '';
                  obj.value[item.modelNameArray[1]] = '';
                  obj.value[item.model] = item.modelName;
                }
              }
            } else {
              // 对时间做操作
              if (item.type && datearr.value.includes(item.type)) {
                if (!item.modelNameArray) {
                  item.modelNameArray = ['start_time', 'end_time'];
                }

                if (obj.value[item.model] && obj.value[item.model].length) {
                  if (item.decideyear === true && item.type === 'daterange') {
                    obj.value[item.model][0] = dayjs(obj.value[item.model][0]).format('YYYY-MM-DD');
                    obj.value[item.model][1] = dayjs(obj.value[item.model][1]).format('YYYY-MM-DD');
                  } else {
                    obj.value[item.model][0] = dayjs(obj.value[item.model][0]).format(
                      item.type === 'datetimerange' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD 00:00:00'
                    );
                    obj.value[item.model][1] = dayjs(obj.value[item.model][1]).format(
                      item.type === 'datetimerange' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD 23:59:59'
                    );
                  }
                  obj.value[item.modelNameArray[0]] = obj.value[item.model][0];
                  obj.value[item.modelNameArray[1]] = obj.value[item.model][1];
                  delete obj.value[item.model];
                } else {
                  obj.value[item.modelNameArray[0]] = '';
                  obj.value[item.modelNameArray[1]] = '';
                  delete obj.value[item.model];
                }
              }

              if (item.type && date_list.value.includes(item.type)) {
                obj.value[item.model] = dayjs(obj.value[item.model]).format('YYYY-MM-DD');
              }

              if (item.type && item.type === 'month') {
                obj.value[item.model] = dayjs(obj.value[item.model]).format('YYYY-MM');
              }
            }

            if (item.multiple) {
              obj.value[item.model] = obj.value[item.model].toString();
            }
          });
          // 如果是点击的查询，直接搜索数据
          if (isSerch) {
            ctx.emit('onSubmit', empty.preProcessData(obj.value));
            return;
          }
          // 删除空的obj
          if (!props.isFor) {
            ctx.emit('onSubmit', empty.preProcessData(obj.value));
            console.log('onSubmit');
          } else {
            ctx.emit('update:jvbObject', empty.preProcessData(obj.value));
          }
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    };

    if (props.isFor) {
      watch(
        () => formInline.value,
        () => {
          submitForm();
        },
        {
          deep: true
        }
      );
    }

    if (props.isCycle) {
      watch(
        () => formInline.value,
        () => {
          modelObjectv.value = formInline.value;
        },
        {
          deep: true,
          immediate: true
        }
      );
      watch(
        () => props.modelValue,
        () => {
          if (modelObjectv.value) {
            if (typeof modelObjectv.value === 'object' && Object.keys(modelObjectv.value).length !== 0) {
              Object.assign(formInline.value, modelObjectv.value);
            }
          }
        },
        {
          deep: true
        }
      );
    }

    /**
     * 组件外设置值，isSubmit是否设置后提交刷新
     */
    function setSerchFormData(key, val, isSubmit = false) {
      formInline.value[key] = val;
      if (isSubmit) {
        submitForm(true);
      }
    }

    onMounted(() => {
      if (props.firstload) {
        submitForm();
      }
    });

    return {
      validateForm,
      resetForm,
      shortcuts,
      formInline,
      rules,
      submitForm,
      datearr,
      datechange,
      setSerchFormData,
      date_list,
    };
  }
});
</script>

<style></style>
