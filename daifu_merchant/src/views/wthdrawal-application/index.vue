<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <!-- <el-form-item label="充值ID">
            <el-input v-model="formInline.id" placeholder="请输入充值ID" />
          </el-form-item> -->

          <el-form-item label="状态">
            <el-select v-model="formInline.state">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="审核中" value="2">审核中</el-option>
              <el-option label="支付中" value="3">支付中</el-option>
              <el-option label="成功" value="1">成功</el-option>
              <el-option label="失败" value="0">失败</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="金额">
            <el-input style="width: 100px" v-model="formInline.amount_min" placeholder="最小金额" />
          </el-form-item>
          <el-form-item label="">
            <el-input style="width: 100px" v-model="formInline.amount_max" placeholder="最大金额" />
          </el-form-item>
          <el-form-item label="">
            <el-date-picker v-model="times" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" :default-time="defaultTime" @change="setSearchTime"> </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" @click="isShowTable"> 下载表格 </el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="content-list">
        <el-table :data="list" :summary-method="getSummaries" show-summary element-loading-text="Loading" border fit highlight-current-row>
          <el-table-column label="充值ID" align="center">
            <template #default="scope">
              <span>{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="充值类型" align="center">
            <template #default="scope">
              <span>{{ flagsToText(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="充值金额" align="center" prop="amount">
            <template #default="scope">
              {{ toFixedNReport(scope.row.amount) }}
            </template>
          </el-table-column>
          <el-table-column label="备注" align="center">
            <template #default="scope">
              {{ scope.row.review_remark }}
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center">
            <template #default="scope">
              <span>
                {{ dateFormat(scope.row.created_at * 1000) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建人" align="center">
            <template #default="scope">
              {{ scope.row.created_by_name }}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.state == 1 ? 'green' : 'red' }">
                {{ stateToText(scope.row) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-pagination
        v-model:currentPage="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="page_size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
      </el-pagination>
    </el-card>

    <el-dialog v-model="isShowDownloadDialog" title="表格下载" center width="560px">
      <div class="download-table">
        <el-table :data="rechargeList" border fit style="width: 100%">
          <el-table-column type="index" align="center" width="50" label="序号" />
          <el-table-column label="日期" align="center">
            <template #default="scope">
              <span>
                {{ dateFormat(scope.row.created_at,false,'YYYY-MM-DD') }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120">
            <template #default="scope">
             <a :href="'/merchant/excel/redirect?id=' + scope.row.id + '&t=' + token" class="table-down" target="_blank">下载</a>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, toRefs, ref } from 'vue';
import { merchantDepositList, getRechargeTable } from '../../http/apis/merchant';
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElMessage } from 'element-plus';
// import { getMerchantList, addMerchant, editMerchant } from '@/http/apis/merchant';
// import { generateCode } from '../../utils/generateCode';
import { empty, dateFormat, toFixedNReport } from '../../utils/common';
import store from '../../store';

export default {
  name: 'MerchantList',
  components: { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem },
  setup() {
    // const generate = () => {
    //   state.defaultForm.ppk = generateCode();
    // };
    const state = reactive({
      query: '',
      formInline: {
        state: '',
        name: '',
        startTime: '',
        endTime: ''
      },
      // isEdit: true,
      // defaultForm: {
      //   channel_id: '', // 代付渠道ID
      //   name: '', // 商户名称
      //   ppk: '', // 密钥
      //   withdraw_fee: '', // 费率
      //   m_single_withdraw: 1, // 手动单条代付
      //   m_batch_withdraw: 1, // 批量手动代付
      //   api_withdraw: 1, // api自动代付
      //   state: 1, // 状态
      //   remarks: '' // 备注
      // },
      currentPage: 1,
      total: 0,
      page_size: 10,
      list: [],
      times: [],
      defaultTime: [new Date(2023, 1, 1, 0, 0, 0), new Date(2023, 2, 1, 23, 59, 59)],

      isShowDialog: false,
      isShowDownloadDialog: false,
      rechargeList: []
    });

    const stateToText = row => {
      if (row.state == 0) {
        return '失败';
      }
      if (row.state == 1) {
        return '成功';
      }
      if (row.state == 2) {
        return '审核中';
      }
      if (row.state == 3) {
        return '支付中';
      }
    };

    const flagsToText = row => {
      if (row.flags == 1) {
        return '余额划转';
      }
      if (row.flags == 2) {
        return '银行卡转账';
      }
      if (row.flags == 3) {
        return 'USDT';
      }
      if (row.flags == 4) {
        return '通道自带充值';
      }
    };

    const getList = () => {
      if (state.formInline.amount_min && !state.formInline.amount_max) {
        ElMessage.error('请输入最大金额');
        return;
      }
      if (!state.formInline.amount_min && state.formInline.amount_max) {
        ElMessage.error('请输入最小金额');
        return;
      }
      // console.log(state.formInline);
      merchantDepositList(
        empty.preProcessData({
          ...state.formInline,
          page: state.currentPage,
          page_size: state.page_size
        })
      ).then(res => {
        console.log(res);
        state.list = [];
        if (state.currentPage == 1) {
          state.total = Number(res.data.total) || 0;
        }
        if (res.status) {
          if (Array.isArray(res.data.d)) {
            state.list = res.data.d || [];
          }
        }
      });
    };
    getList();

    const rules = reactive({
      channel_id: [
        {
          required: true,
          message: '请选择代付渠道',
          trigger: 'blur'
        }
      ],
      name: [
        {
          required: true,
          message: '请输入商户名称',
          trigger: 'blur'
        }
      ],
      ppk: [
        {
          required: true,
          message: '请先生成密钥',
          trigger: 'blur'
        }
      ],
      withdraw_fee: [
        {
          required: true,
          message: '请输入代付费率',
          trigger: 'blur'
        }
      ]
    });
    // 搜索
    const searchData = () => {
      console.log(state.query);
    };

    // const addMerchant = () => {
    //   state.isEdit = false;
    //   state.isShowDialog = true;
    // };

    const addForm = ref(null);

    // const handleEdit = (index, row) => {
    //   state.isEdit = true;
    //   Object.assign(state.defaultForm, row);
    //   console.log(state.defaultForm);
    //   state.isShowDialog = true;
    // };

    // const delMerchant = row => {
    //   ElMessageBox.confirm('你确定要删除吗？', '提示', {
    //     confirmButtonText: '确定',
    //     cancelButtonText: '取消',
    //     type: 'warning'
    //   }).then(() => {
    //     deleteMerchant({
    //       id: row.id
    //     }).then(res => {
    //       if (res.status) {
    //         ElMessage.success('删除成功');
    //         getList();
    //       }
    //     });
    //   });
    // };

    const search = () => {
      state.formInline.name = decodeURIComponent(state.formInline.name || '');
      state.currentPage = 1;
      getList();
    };

    // const onSubmit = () => {
    //   addForm.value.validate(valid => {
    //     if (valid) {
    //       [addMerchantFetch, editMerchant][state.isEdit ? 1 : 0](state.defaultForm).then(res => {
    //         if (res.status) {
    //           ElMessage.success(state.isEdit ? '编辑成功' : '新增成功');
    //           getList();
    //           onCancel();
    //         }
    //       });
    //     }
    //   });
    // };

    // const onCancel = () => {
    //   addForm.value && addForm.value.resetFields();
    //   state.isShowDialog = false;
    // };

    const handleSizeChange = val => {
      state.page_size = val;
      state.currentPage = 1;
      getList();
    };

    const handleCurrentChange = val => {
      state.currentPage = val;
      getList();
    };

    const setSearchTime = () => {
      if (state.times != null) {
        state.formInline.st = dateFormat(state.times[0]);
        state.formInline.et = dateFormat(state.times[1]);
      } else {
        state.formInline.st = '';
        state.formInline.et = '';
      }
    };

    const isShowTable = () => {
      getRechargeTable({ flag: 1 }).then(res => {
        if (res.status) {
          if (Array.isArray(res.data)) {
            state.rechargeList = res.data || [];
          }
        }
      });
      state.isShowDownloadDialog = true;
    };

    const token = store.getters.userInfo.token;

    return {
      ...toRefs(state),
      rules,
      addForm,
      setSearchTime,
      isShowTable,
      token,
      searchData,
      // addMerchant,
      // onCancel,
      // onSubmit,
      // handleEdit,
      // delMerchant,
      handleSizeChange,
      handleCurrentChange,
      dateFormat,
      stateToText,
      toFixedNReport,
      // generate,
      search,
      flagsToText
    };
  }
};
</script>
<style lang="less" scoped>
.app-container {
  .content-list {
    margin-top: 20px;
    margin-bottom: 20px;
  }
}
.login-key {
  display: flex;
}
.download-table{
  max-height: 450px;
  overflow-y: auto;

}
</style>