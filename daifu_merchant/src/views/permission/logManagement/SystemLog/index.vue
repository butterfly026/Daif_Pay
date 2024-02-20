<template>
  <div class="container">
    <!-- <el-dialog :title="t('routes.system.request_response_params')" v-model="dialogTableVisible" width="800px">
      <div class="request-cont clearfix">
        <div>
          <p>{{t('routes.system.request_params')}}</p>
          <pre>{{responseData.requestD}}</pre>
        </div>
        <div>
          <p>{{t('routes.system.response_params')}}</p>
          <pre>{{responseData.responseD}}</pre>
        </div>
      </div>
      <div class="empty"></div>
      <template #footer>
        <span class="dialog-footer">
          <div class="btn">
            <el-button type="primary" @click="dialogTableVisible = false">{{t('global.sure')}}</el-button>
          </div>
        </span>
      </template>
    </el-dialog> -->
    <el-form :inline="true" ref="elForm" :model="formInline" class="demo-form-inline">
      <el-form-item :label="t('global.operating_time')" prop="value1" :rules="[{ required: true, message: ' ' }]">
        <el-date-picker v-model="formInline.value1" type="datetimerange" unlink-panels :range-separator="t('global.zhi')" :start-placeholder="t('global.start_date')" :end-placeholder="t('global.end_date')" :shortcuts="shortcuts" @change="dataChange($event)">
        </el-date-picker>
      </el-form-item>
      <el-form-item :label="t('global.operating_people')" prop="name" :rules="[{pattern: user_name,message: ' ',trigger: ['blur', 'change']}]">
        <el-input v-model="formInline.name" style="width: 280px" :placeholder="t('routes.system.please_input_opeartor_account')"></el-input>
      </el-form-item>
      <el-form-item :label="t('routes.system.operating_menu')">
        <el-cascader v-model="formInline.title" :options="dialogRoleAuthTree" style="width: 260px" clearable filterable :show-all-levels="false" @change="menuChange"></el-cascader>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">{{t('global.query')}}</el-button>
        <el-button @click="resetForm">{{t('global.reset')}}</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column type="index" width="80" :label="t('global.sort')" align="center">
      </el-table-column>
      <el-table-column prop="name" :label="t('global.operating_people')" align="center">
      </el-table-column>
      <el-table-column prop="title" :label="t('routes.system.menu_name')" align="center">
      </el-table-column>
      <el-table-column prop="content" :label="t('routes.system.operating_content')" align="center">
      </el-table-column>
      <!-- <el-table-column prop="city" :label="t('routes.system.response_msg')" align="center">
        <template v-slot="scope">
          <el-button type="text" @click="queryRequest(scope.row)">{{ scope.row.city }}</el-button>
        </template>
      </el-table-column> -->
      <el-table-column prop="ip" :label="t('routes.system.IP_addr')" align="center">
        <template #default="{ row }">
          <p>{{ long2ip(row.ip) }}</p>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" :label="t('routes.system.operating_time')" align="center" width="150">
        <template #default="{row}">
          {{ $filters.format(row.created_at,false) }}
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-show="total > 0" :total="total" v-model:page="pageQuery.page" @pagination="pagination" v-model:limit="pageQuery.page_size" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, unref } from 'vue';
import Pagination from '@/components/pagination/index.vue';
import usePaginationHanlder from '@/components/pagination/pagination';
import { RefObjectKeyRulesType } from '@/components/serch-form/types';
import { useI18n } from 'vue-i18n';
import { ElForm } from 'element-plus';
import { getPrivList, getSystemList } from '@/http/modules/permission';
import { PriveListItem } from '@/views/permission/roleModules/types';
import { ResponseGroupUserItem } from '@/views/permission/rolePermission/types';
import { listToTree as listToTreeOfRole } from '@/views/permission/roleModules/tree';
import dayjs from 'dayjs';
import _ from 'lodash';
import useShortcuts from '@/components/serch-form/useShortcuts';
import { user_name, user_name_prompt } from '@/utils/expressions';
import long2ip from '@/utils/long2ip';
import { empty } from '@/components/serch-form/empty';

export default defineComponent({
  name: 'SystemLogs',
  components: {
    Pagination
  },
  setup() {
    const { t } = useI18n({ useScope: 'global' });
    const elForm = ref<typeof ElForm | null>(null);
    const pageQuery = ref<RefObjectKeyRulesType>({});
    const listQuery = ref<RefObjectKeyRulesType>({});
    const { shortcuts } = useShortcuts();
    const defaultTime = ref([dayjs().format('YYYY-MM-DD 00:00:00'), dayjs().format('YYYY-MM-DD 23:59:59')]);
    const formInline = ref<any>({
      value1: defaultTime.value,
      title: [''],
      title_name: ''
    });
    // const responseData = ref({
    //   requestD: 'ty: 0',
    //   responseD: { id: 28, level: 3, level_name: 'SVIP3', draw_num: 50, draw_max: 200, daily_draw_max: 10000, members: 3 }
    // });
    const dialogTableVisible = ref<boolean>(false);
    const dialogRoleAuthTree = reactive<any>([]);
    const { total, pagination } = usePaginationHanlder({
      pageQuery: pageQuery.value,
      hander: getList
    });
    getList();
    getPrivList().then((res: any) => {
      if (res.status) {
        const data: ResponseGroupUserItem[] = res.data;
        const root = null;
        let dialogTreeData: PriveListItem[] = [];
        let temp: any;
        data.forEach((item: any) => {
          temp = {
            id: item.id,
            value: item.name,
            module: item.name,
            name: item.name,
            state: item.state,
            label: item.name,
            docId: `preid${item.id}`,
            pid: parseInt(item.pid, 10)
          };
          if (item.state === 1) {
            dialogTreeData.push(Object.assign({}, temp));
          }
        });

        dialogTreeData = listToTreeOfRole(dialogTreeData, 0);
        const result = filter(dialogTreeData);
        dialogRoleAuthTree.push(...result);
        dialogRoleAuthTree.unshift({
          value: '',
          label: t('global.checkall')
        });
      }
    });
    function filter(arr: any) {
      const result = _.cloneDeepWith(arr);
      for (const firstLayer of result) {
        if (firstLayer.children) {
          for (const secondLayer of firstLayer.children) {
            delete secondLayer.children;
          }
        }
      }
      return result;
    }
    const tableData = ref([]);
    function getList() {
      const obj = _.cloneDeep(formInline.value);
      obj.start_time = obj.value1[0];
      obj.end_time = obj.value1[1];
      getSystemList(
        empty.preProcessData({
          ...unref(pageQuery.value),
          name: obj.name,
          start_time: obj.start_time,
          end_time: obj.end_time,
          title: obj.title_name
        })
      ).then((res: any) => {
        tableData.value = [];
        if (res.status) {
          if (pageQuery.value.page.toString() === '1') {
            total.value = res.data.t;
          }
          if (res.data.d) {
            tableData.value = res.data.d;
          }
        }
      });
    }
    const onSubmit = (data: any) => {
      pageQuery.value.page = 1;
      if (elForm.value) {
        elForm.value.validate((valid: boolean) => {
          if (valid) {
            getList();
          } else {
            console.log('error submit!!');
            return false;
          }
        });
      }
    };
    // const queryRequest = () => {
    //   dialogTableVisible.value = true;
    // };
    const resetForm = () => {
      formInline.value.value1 = defaultTime.value;
      formInline.value.name = '';
      formInline.value.title = [''];
    };
    const menuChange = (name: any) => {
      console.log(name);
      if (name) {
        formInline.value.title_name = name.join('-');
      } else {
        formInline.value.title_name = '';
      }
    };
    const dataChange = (e: any) => {
      if (e) {
        // formInline[m]
        const start = dayjs(e[0]).format('HH:mm:ss');
        const end = dayjs(e[1]).format('HH:mm:ss');
        // 说明都是为00:00:00
        console.log(start + '||' + end);
        if (start === end && start === '00:00:00') {
          formInline.value.value1 = [dayjs(e[0]).format('YYYY-MM-DD 00:00:00'), dayjs(e[1]).format('YYYY-MM-DD 23:59:59')];
        }
        const obj = _.cloneDeep(formInline.value);
        obj.start_time = obj.value1[0];
        obj.end_time = obj.value1[1];
        delete obj.value1;
        listQuery.value = obj;
      }
    };
    return {
      formInline,
      elForm,
      tableData,
      // queryRequest,
      dialogTableVisible,
      defaultTime,
      listQuery,
      total,
      pagination,
      pageQuery,
      onSubmit,
      dataChange,
      t,
      resetForm,
      shortcuts,
      dialogRoleAuthTree,
      user_name,
      // responseData,
      menuChange,
      long2ip,
      user_name_prompt
    };
  }
});
</script>
<style lang="scss" scoped>
.container {
  padding: 20px 20px 20px 20px;
  .blue {
    color: #409eff;
    cursor: pointer;
  }
  .request-cont {
    margin-bottom: 20px;
    > div {
      min-height: 400px;
      border: 1px solid #f1f1f1;
      > p {
        height: 50px;
        line-height: 50px;
        padding-left: 20px;
        font-size: 18px;
        color: black;
        border-bottom: 1px solid #f1f1f1;
      }
    }
    > div:nth-of-type(1) {
      width: 38%;
      float: left;
      margin-bottom: 20px;
    }
    > div:nth-of-type(2) {
      width: 58%;
      float: right;
      margin-bottom: 20px;
    }
    .clearfix:after {
      content: '';
      display: block;
      height: 0;
      clear: both;
      visibility: hidden;
    }
  }
}
</style>
