<template>
  <div class="container">
    <page-table :FormDataList="FormDataList" ref="pageTable" url="merchant/sys/log/login/list" :listHanlder="currentHanlder" :showsummary="false">
      <el-table-column type="index" :label="t('global.sort')" align="center" width="50"></el-table-column>
      <el-table-column align="center" prop="name" :label="t('global.operating_people')">
      </el-table-column>
      <el-table-column align="center" prop="flag" :label="t('global.type')" width="180">
        <template #default="{row}">
          {{loginTypeTotext(row.flag)}}
        </template>
      </el-table-column>
      <el-table-column align="center" prop="ip" :label="t('routes.system.IP_addr')">
        <template #default="{ row }">
          <p>{{ long2ip(row.ip) }}</p>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="device" :label="t('routes.system.device_num')">
      </el-table-column>
      <el-table-column align="center" prop="created_at" :label="t('routes.system.operating_time')">
        <template #default="{row}">
          {{ $filters.format(row.created_at,false) }}
        </template>
      </el-table-column>
    </page-table>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, unref, inject, reactive, toRefs } from 'vue';
import useSearchPlaceholder from './search';
import PageTable from '@/components/page-table/index.vue';
import { ElTable, ElMessage, ElMessageBox, ElForm } from 'element-plus';
import { useI18n } from 'vue-i18n';
import long2ip from '@/utils/long2ip';
import { loginTypeTotext } from '@/utils/selectOptions';

export default defineComponent({
  name: 'LoginLogs',
  components: { PageTable },
  setup() {
    const { t } = useI18n({ useScope: 'global' });
    const { FormDataList } = useSearchPlaceholder(t);
    const pageTable = ref<typeof ElTable | null>(null);
    const elForm = ref<typeof ElForm | null>(null);
    const currentHanlder = (tableData: any) => {
      return tableData.map((item: any) => {
        return {
          ...item
        };
      });
    };
    return {
      elForm,
      FormDataList,
      currentHanlder,
      pageTable,
      close,
      long2ip,
      loginTypeTotext,
      t
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  padding: 20px 20px 20px 20px;
  .blue-cursor {
    color: #409eff;
    cursor: pointer;
  }
  .input-width {
    .span {
      padding: 0 15px;
    }
  }
}
</style>
