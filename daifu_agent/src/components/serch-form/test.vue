<template>
  <el-form :inline="true" ref="validateForm" :rules="rules" :model="formInline" class="demo-form-inline" v-bind="$attrs">
    <el-form-item v-for="(item, i) in FormDataList" :key="i" :label="item.label" :prop="item.model">
      <DataAmount v-if="item.type === 'doubleInput'" v-model:min="formInline[item.model]" v-model:max="formInline[item.max]"> </DataAmount>
      <CompositeCompontent
        v-model="formInline[item.model]"
        v-model:compostiteType="item.selectType"
        :type="item.selectType"
        v-if="item.type === 'composite'"
        @enterhanlder="submitForm"
        :selectvalue="item.modelName"
        :options="item.options"
      >
        <template #prepend>
          <el-select v-model="item.modelName" placeholder="请选择" :style="{ width: (item.width ? item.width : 220) + 'px' }">
            <el-option v-for="cItem in item.options" :key="cItem.value" :label="cItem.label" :value="cItem.value"></el-option>
          </el-select>
        </template>
      </CompositeCompontent>
      <!-- INPUT -->
      <el-input
        v-if="item.type === undefined || item.type === 'input'"
        :clearable="true"
        v-model.trim="formInline[item.model]"
        @keyup.enter="submitForm"
        :placeholder="item.placeholder ? item.placeholder : '请输入' + item.label"
      ></el-input>
      <!-- SELECT -->
      <el-select
        v-loading="item.loading"
        :disabled="item.loading"
        element-loading-spinner="el-icon-loading"
        :clearable="true"
        :multiple="item.multiple"
        v-if="item.type === 'select'"
        v-model="formInline[item.model]"
        :placeholder="item.placeholder ? item.placeholder : '请选择' + item.label"
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
        :type="item.type"
        unlink-panels
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        :shortcuts="shortcuts"
      >
      </el-date-picker>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm">查询</el-button>
      <el-button @click="resetForm">重置</el-button>
      <slot name="csv"></slot>
    </el-form-item>
  </el-form>
</template>

<script lang="ts">
import { defineComponent, ref, PropType, nextTick, onMounted } from 'vue';
import { FormData, RefObjectKeyRulesType, OptionsType } from './types';
import { ElForm } from 'element-plus';
import CompositeCompontent from './composite.vue';
import useShortcuts from './useShortcuts';
import _ from 'lodash';
import dayjs from 'dayjs';
import DataAmount from '../data-amount/index.vue';

export default defineComponent({
  name: 'bgSerch',
  components: {
    CompositeCompontent,
    DataAmount
  },
  props: {
    FormDataList: {
      type: Array as PropType<FormData[]>,
      default: []
    },
    firstload: {
      type: Boolean as PropType<boolean>,
      default: true
    }
  },
  emits: ['onSubmit'],
  setup(props, ctx) {
    const formInline = ref<RefObjectKeyRulesType>({});
    const validateForm = ref(ElForm);
    const rules = ref<RefObjectKeyRulesType>({});
    const datearr = ref<string[]>(['datetimerange', 'daterange']);
    const { shortcuts } = useShortcuts();
    const empty = {
      isEmpty(obj: any) {
        if (typeof obj === 'undefined' || obj === null || obj === '') {
          return true;
        } else {
          return false;
        }
      },
      preProcessData(formData: any) {
        /* 删除空值 */
        Object.keys(formData).forEach(item => {
          if (this.isEmpty(formData[item])) {
            delete formData[item];
          }
        });
        return formData;
      }
    };
    const axiosFn = (url: string, bool: boolean) => {
      return new Promise((resolve, reject) => {
        resolve(true);
      });
    };
    props.FormDataList.forEach((item: any) => {
      if (datearr.value.includes(item.type) && !item.default) {
        if (item.type === 'daterange') {
          item.default = [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')];
        } else {
          item.default = [dayjs().format('YYYY-MM-DD 00:00:00'), dayjs().format('YYYY-MM-DD 23:59:59')];
        }
      }
      formInline.value[item.model] = item.default ? item.default : '';
      // 添加rules
      if (item.rules) {
        rules.value[item.model] = item.rules;
      }
    });
    props.FormDataList.forEach(async (item: FormData) => {
      if (item.type === 'select' && item.url && Array.isArray(item.options) && item.options.length <= 0) {
        item.loading = true;
        item.options = (await axiosFn(item.url as string, true)) as OptionsType[];
        item.loading = false;
      }
    });
    const resetForm = () => {
      nextTick(() => {
        (validateForm.value as typeof ElForm).resetFields();
      });
    };
    const submitForm = () => {
      validateForm.value.validate((valid: any) => {
        if (valid) {
          const obj: RefObjectKeyRulesType = _.cloneDeep(formInline);
          props.FormDataList.forEach(item => {
            if (item.type === 'composite') {
              if (item.selectType === 'input') {
                obj.value[item.valueModel] = obj.value[item.model];
              }
              if (!item.modelNameArray) {
                item.modelNameArray = ['start_time', 'end_time'];
              }
              // 是时间选择器
              obj.value[item.modelNameArray[0]] = dayjs(obj.value[item.model][0]).format('YYYY-MM-DD HH:mm:ss');
              obj.value[item.modelNameArray[1]] = dayjs(obj.value[item.model][1]).format('YYYY-MM-DD HH:mm:ss');
              obj.value[item.model] = item.modelName;
            }
            // 对时间做操作
            if (item.type && datearr.value.includes(item.type)) {
              if (!item.modelNameArray) {
                item.modelNameArray = ['start_time', 'end_time'];
              }
              obj.value[item.model][0] = dayjs(obj.value[item.model][0]).format('YYYY-MM-DD HH:mm:ss');
              obj.value[item.model][1] = dayjs(obj.value[item.model][1]).format('YYYY-MM-DD HH:mm:ss');
              obj.value[item.modelNameArray[0]] = obj.value[item.model][0];
              obj.value[item.modelNameArray[1]] = obj.value[item.model][1];
              delete obj.value[item.model];
            }
            if (item.multiple) {
              obj.value[item.model] = obj.value[item.model].toString();
            }
          });
          // 删除空的obj
          ctx.emit('onSubmit', empty.preProcessData(obj.value));
          console.log('onSubmit');
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    };

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
      datearr
    };
  }
});
</script>

<style></style>
