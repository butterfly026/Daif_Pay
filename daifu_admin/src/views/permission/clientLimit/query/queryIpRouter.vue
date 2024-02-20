<template>
  <div class="container">
    <el-dialog :title="title" v-model="dialogComputed" width="900px">
      <BMenu :name-list="nameList" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogComputed = false">{{$t('global.cancel')}}</el-button>
          <el-button type="primary" @click="dialogComputed = false">{{$t('global.sure')}}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import BMenu from '@/components/b-menu/index.vue';
import queryWhiteIp from './queryWhiteIp.vue';
import queryBlackIp from './queryBlackIp.vue';
import queryAddr from './queryAddr.vue';
import { useI18n } from 'vue-i18n';
export default defineComponent({
  name: 'VipConfigs',
  components: {
    BMenu,
    queryWhiteIp,
    queryAddr,
    queryBlackIp
  },
  props: {
    modelValue: Boolean,
    title: String
  },
  emits: ['update:modelValue', 'change'],
  setup(props, context) {
    const { t } = useI18n({ useScope: 'global' });
    const dialogComputed = computed({
      get: () => props.modelValue,
      set: value => context.emit('update:modelValue', value)
    });
    const nameList = [
      {
        name: 'queryAddr',
        text: t('routes.system.area'),
        components: queryAddr
      },
      {
        name: 'queryWhiteIp',
        text: t('routes.system.white_order'),
        components: queryWhiteIp
      },
      {
        name: 'queryBlackIp',
        text: t('routes.system.black_order'),
        components: queryBlackIp
      }
    ];
    return {
      nameList,
      dialogComputed
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  ::v-deep(.el-dialog__body) {
    padding: 0 20px;
  }
}
</style>
