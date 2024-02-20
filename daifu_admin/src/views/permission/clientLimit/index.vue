<template>
  <div class="container">
    <el-dialog :title="$t('routes.system.add_client')" v-model="dialog.addClientDailog" width="710px">
      <addClientLimit />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.addClientDailog = false">{{$t('global.cancel')}}</el-button>
          <el-button type="primary" @click="dialog.addClientDailog = false">{{$t('global.sure')}}</el-button>
        </span>
      </template>
    </el-dialog>
    <queryIpRouter v-model="openWhiteDialog" :title="title" />
    <div class="add">
      <el-button type="primary" @click="openAddClientDailog">{{$t('global.increment')}}</el-button>
    </div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="date" :label="$t('routes.system.client')" align="center">
      </el-table-column>
      <el-table-column prop="name" :label="$t('routes.system.trust_zone')" align="center">
      </el-table-column>
      <el-table-column prop="province" :label="$t('routes.system.close_zone')" align="center">
      </el-table-column>
      <el-table-column prop="city" :label="$t('routes.system.white_ip')" align="center" sortable>
        <template #default="scope">
          <span class="blue" @click="openWhite(scope.row)">{{ scope.row.city }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="address" :label="$t('routes.system.black_ip')" align="center" sortable>
        <template #default="scope">
          <span class="blue" @click="openBlack(scope.row)">{{ scope.row.address }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="zip" :label="$t('global.operating')" align="center" width="150">
        <template #default="scope">
          <el-button type="text" @click="editBlack(scope.row)">{{$t('global.edit')}}</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="text">{{$t('global.delete')}}</el-button>
        </template>

      </el-table-column>
    </el-table>
    <Pagination v-show="total > 0" :total="total" v-model:page="pageQuery.page" @pagination="pagination" v-model:limit="pageQuery.page_size" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import Pagination from '@/components/pagination/index.vue';
import usePaginationHanlder from '@/components/pagination/pagination';
import { RefObjectKeyRulesType } from '@/components/serch-form/types';
import addClientLimit from './addClientLimit.vue';
import queryIpRouter from './query/queryIpRouter.vue';
import { useI18n } from 'vue-i18n';

export default defineComponent({
  name: 'clientLimits',
  components: {
    Pagination,
    addClientLimit,
    queryIpRouter
  },
  setup() {
    const pageQuery = ref<RefObjectKeyRulesType>({});
    const listQuery = ref<RefObjectKeyRulesType>({});
    const formInline = ref<any>({});
    const value1 = ref([new Date(), new Date()]);
    const dialog = ref({
      dialogTableVisible: false,
      addClientDailog: false
    });
    const { t } = useI18n({ useScope: 'global' });
    const title = ref();
    const { total, pagination } = usePaginationHanlder({
      pageQuery: pageQuery.value,
      hander: getList
    });
    const openWhiteDialog = ref<boolean>(false);
    const tableData = ref([
      {
        date: 'IOS棋牌APP',
        name: '中国大陆|中国香港|其他',
        province: '中国台湾|新加坡|美国|韩国|日本|菲律宾',
        city: '609条',
        address: '609条',
        zip: ''
      },
      {
        date: 'IOS棋牌APP',
        name: '中国大陆|中国香港|其他',
        province: '中国台湾|新加坡|美国|韩国|日本|菲律宾',
        city: '609条',
        address: '609条',
        zip: ''
      }
    ]);
    function getList() {
      console.log(123);
    }
    const onSubmit = (data: any) => {
      pageQuery.value.page = 1;
      listQuery.value = data;
      getList();
    };
    const openAddClientDailog = () => {
      dialog.value.addClientDailog = true;
    };
    const openWhite = (row: any) => {
      title.value = t('global.check') + '/PC';
      openWhiteDialog.value = true;
    };
    const openBlack = (row: any) => {
      title.value = t('global.check') + '/PC';
      openWhiteDialog.value = true;
    };
    const editBlack = () => {
      title.value = t('global.edit');
      openWhiteDialog.value = true;
    };
    return {
      formInline,
      value1,
      tableData,
      dialog,
      editBlack,
      openWhiteDialog,
      openWhite,
      openBlack,
      listQuery,
      title,
      total,
      pagination,
      pageQuery,
      onSubmit,
      openAddClientDailog
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  padding: 0 20px 20px 20px;
  .blue {
    color: #409eff;
    cursor: pointer;
  }
  .add {
    padding-bottom: 20px;
  }
}
</style>
