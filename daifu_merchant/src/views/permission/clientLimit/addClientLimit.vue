<template>
  <div class="container">
    <el-form ref="elForm" :model="formData" size="medium" label-width="auto">
      <el-form-item :label="$t('routes.system.client')+':'">
        <el-checkbox-group v-model="formData.type">
          <el-checkbox v-for="item in addClientType" :key="item.value" :label="item.label" :value="item.value"></el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item :label="$t('routes.system.area')+':'">
        <label class="label">{{$t('routes.system.china')}}</label>
        <el-select v-model="formData.b" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.taiwan')}}</label
        ><el-select v-model="formData.c" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.hongkong')}}</label
        ><el-select v-model="formData.d" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.singapore')}}</label
        ><el-select v-model="formData.e" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.america')}}</label
        ><el-select v-model="formData.f" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.korea')}}</label
        ><el-select v-model="formData.g" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.japan')}}</label
        ><el-select v-model="formData.h" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.philippines')}}</label
        ><el-select v-model="formData.i" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.malaysia')}}</label
        ><el-select v-model="formData.j" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.cambodia')}}</label
        ><el-select v-model="formData.k" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
        <label class="label">{{$t('routes.system.other')}}</label
        ><el-select v-model="formData.l" :placeholder="$t('global.please_choose')">
          <el-option v-for="item in clientLimitType" :key="item.value" :label="item.label" :value="item.value"> </el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('routes.system.white_order')+':'">
        <el-input v-model="formData.m" type="textarea" :placeholder="$t('routes.system.input_more_ip')" :autosize="{ minRows: 4, maxRows: 4 }"></el-input>
      </el-form-item>
      <el-form-item :label="$t('routes.system.black_order')+':'">
        <el-input v-model="formData.n" type="textarea" :placeholder="$t('routes.system.input_more_ip')" :autosize="{ minRows: 4, maxRows: 4 }"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>
<script lang="ts">
import { defineComponent, ref } from 'vue';
import { clientLimitType, addClientType } from '@/utils/selectOptions';
export default defineComponent({
  name: 'addClientLimits',
  components: {},
  setup() {
    const formData = ref({
      type: []
    });
    return {
      formData,
      clientLimitType,
      addClientType
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  .el-select {
    width: 100px;
    margin-bottom: 30px;
  }
  .label {
    display: inline-block;
    width:75px;
    margin: 0 8px;
    line-height: 1;
  }
}
</style>
