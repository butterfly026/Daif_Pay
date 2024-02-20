<template>
  <div class="container">
    <el-dialog :title="t('global.increment')" v-model="dialog.addClientDailog" width="510px">
      <el-form ref="form" :model="formData" label-width="auto">
        <el-form-item :label="t('routes.system.IP_addr')+':'">
          <el-input type="textarea" v-model="formData.a" maxlength="500" show-word-limit :autosize="{ minRows: 4, maxRows: 4 }"></el-input>
          <p>{{t('routes.system.input_tips')}}</p>
        </el-form-item>
        <el-form-item label="">
          <el-checkbox v-model="formData.b">{{t('routes.system.same_insert_other_client')}}</el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.addClientDailog = false">{{t('global.cancel')}}</el-button>
          <el-button type="primary" @click="dialog.addClientDailog = false">{{t('global.submit')}}</el-button>
        </span>
      </template>
    </el-dialog>
    <page-table :FormDataList="FormDataList" ref="pageTable" url="" :listHanlder="currentHanlder" :showsummary="false" :special_params="{ uid }">
      <el-table-column type="index" :label="t('routes.system.sort_number')" align="center" width="50"></el-table-column>
      <el-table-column align="center" prop="a" label="IP"> </el-table-column>
      <el-table-column align="center" prop="b" :label="t('routes.system.operating_time')">
        <template #default="scope">
          {{ $filters.format(scope.row.b) }}
        </template>
      </el-table-column>
      <el-table-column align="center" prop="c" :label="t('global.operating_people')"> </el-table-column>
    </page-table>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, inject } from 'vue';
import useSearchPlaceholder from './search';
import PageTable from '@/components/page-table/index.vue';
import { ElTable } from 'element-plus';
import { useI18n } from 'vue-i18n';

export default defineComponent({
  name: 'queryWhiteIps',
  components: { PageTable },
  setup() {
    const { FormDataList } = useSearchPlaceholder();
    const { t } = useI18n({ useScope: 'global' });
    const uid = inject('') || '';
    const pageTable = ref<typeof ElTable | null>(null);
    const dialog = ref({
      addClientDailog: true
    });
    const formData = ref({});
    const currentHanlder = (tableData: any) => {
      return tableData.map((item: any) => {
        return {
          ...item
        };
      });
    };
    return {
      FormDataList,
      formData,
      currentHanlder,
      uid,
      t,
      pageTable,
      dialog
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  padding: 20px 20px 20px 20px;
}
</style>
