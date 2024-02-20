<template>
  <div class="container">
    <el-dialog :title="t('global.edit')" v-model="addClientDailog" width="410px">
      <div class="empty"></div>
      <el-form ref="form" :model="formData" label-width="auto">
        <el-form-item :label="t('routes.system.IP_addr')">
          <el-input v-model="formData.a" :placeholder="t('routes.system.IP_addr')+':'"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addClientDailog = false">{{ t('global.cancel') }}</el-button>
          <el-button type="primary" @click="addClientDailog = false">{{ t('global.sure') }}</el-button>
        </span>
      </template>
    </el-dialog>
    <page-table :FormDataList="FormDataList" ref="pageTable" url="" :listHanlder="currentHanlder" :showsummary="false"
                :special_params="{ uid }">
      <template #batch>
        <div>
          <el-button type="primary" @click="addIp">{{ t('global.increment') }}</el-button>
          <el-button>{{ t('routes.system.batch_del') }}</el-button>
        </div>
      </template>
      <el-table-column type="selection" :label="t('routes.system.sort_number')" align="center" width="50"
                       @selection-change="selectChange" highlight-current-row></el-table-column>
      <el-table-column type="index" :label="t('routes.system.sort_number')" align="center" width="50"></el-table-column>
      <el-table-column align="center" prop="a" label="IP"></el-table-column>
      <el-table-column align="center" prop="b" :label="t('routes.system.operating_time')">
        <template #default="scope">
          {{ $filters.format(scope.row.b) }}
        </template>
      </el-table-column>
      <el-table-column align="center" prop="c" :label="t('global.operating_people')"></el-table-column>
      <el-table-column align="center" prop="c" :label="t('global.operating')">
        <template #default="scope">
          <el-button type="text" @click="editBlack(scope.row)">{{ t('global.edit') }}</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="text" @click="deleteBlack(scope.row)">{{ t('global.delete') }}</el-button>
        </template>
      </el-table-column>
    </page-table>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, inject } from 'vue';
import useSearchPlaceholder from './searchBlack';
import PageTable from '@/components/page-table/index.vue';
import { useI18n } from 'vue-i18n';
import { ElTable, ElMessageBox, ElMessage } from 'element-plus';

export default defineComponent({
  name: 'queryBlackIps',
  components: {PageTable},
  setup() {
    const { t } = useI18n({ useScope: 'global' });
    const {FormDataList} = useSearchPlaceholder();
    const uid = inject('') || '';
    const pageTable = ref<typeof ElTable | null>(null);
    const formData = ref({});
    const addClientDailog = ref<boolean>(false);
    const checkData = ref([]);
    const currentHanlder = (tableData: any) => {
      return tableData.map((item: any) => {
        return {
          ...item
        };
      });
    };
    const addIp = () => {
      addClientDailog.value = true;
    };
    const selectChange = (row: any) => {
      if (row) {
        checkData.value = row.map((el: any) => {
          return {
            uid: el.id
          };
        });
      }
    };
    const editBlack = () => {
      addClientDailog.value = true;
    };
    const deleteBlack = () => {
      ElMessageBox.confirm(t('routes.system.sure_dele_tips'), t('global.warning'), {
        confirmButtonText: t('global.sure'),
        cancelButtonText: t('global.cancel'),
        type: 'warning'
      }).then(() => {
        ElMessage.success({
          message: t('global.delete_suc'),
          type: 'success'
        });
      });
    };
    return {
      FormDataList,
      currentHanlder,
      uid,
      checkData,
      editBlack,
      deleteBlack,
      selectChange,
      addIp,
      pageTable,
      addClientDailog,
      formData,
      t
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  padding: 20px 20px 20px 20px;

  .empty {
    height: 20px;
  }
}
</style>
