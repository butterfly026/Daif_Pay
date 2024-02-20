<template>
  <div class="vip-container">
    <el-form ref="elForm" :model="formData" size="medium">
      <el-form-item>
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
    </el-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { clientLimitType } from '@/utils/selectOptions';

export default defineComponent({
  name: 'queryAddrs',
  setup() {
    const formData = ref({
      type: []
    });
    return {
      formData,
      clientLimitType
    };
  }
});
</script>
<style lang="scss" scoped>
.vip-container {
  padding: 20px 50px 0 50px;
  background: white;
  .empty {
    height: 20px;
  }
  a {
    text-decoration: none;
  }
  .el-select {
    width: 100px;
    margin-bottom: 30px;
  }
  .label {
    display: inline-block;
    width: 74px;
    margin: 0 8px;
    line-height: 1;
    text-align-last: justify;
  }
}
</style>
