<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <!-- <el-form-item label="订单ID">
            <el-input v-model="formInline.order_id" placeholder="请输入订单ID" clearable></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="formInline.cash_type">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="API提现" value="1">API提现</el-option>
              <el-option label="手动提现" value="2">手动提现</el-option>
              <el-option label="代充手续费" value="3">代充手续费</el-option>
              <el-option label="代付手续费" value="4">代付手续费</el-option>
              <el-option label="usdt充值" value="5">usdt充值</el-option>
              <el-option label="银行卡充值" value="6">银行卡充值</el-option>
              <el-option label="余额划转" value="7">余额划转</el-option>
              <el-option label="手动代付失败返款" value="8">手动代付失败返款</el-option>
              <el-option label="增加余额" value="9">增加余额</el-option>
              <el-option label="商户冲正" value="10">商户冲正</el-option>
              <el-option label="代付手续费返款" value="11">代付手续费返款</el-option>
              <el-option label="扣减余额" value="12">扣减余额</el-option>
            </el-select>
          </el-form-item> -->
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
        <el-table :data="list" element-loading-text="Loading" :summary-method="getSummaries" show-summary border fit highlight-current-row>
          <el-table-column label="账变ID" align="center">
            <template #default="scope">
              <span>{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="订单ID" align="center">
            <template #default="scope">
              <span>{{ scope.row.order_id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="账变时间" align="center">
            <template #default="scope">
              <span>{{ dateFormat((scope.row.created_at / 1000).toFixed(0), false) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" align="center">
            <template #default="scope">
              {{ flagsToText(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="账变前金额" align="center">
            <template #default="scope">
              <span style="font-weight: bold">{{ toFixedNReport(scope.row.before) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" align="center" prop="amount">
            <template #default="scope">
              <span :style="{ color: textColor(scope.row.amount) }" style="font-weight: bold">
                {{ toFixedNReport(scope.row.amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="账变后金额" align="center">
            <template #default="scope">
              <span style="font-weight: bold">{{ toFixedNReport(scope.row.after) }}</span>
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
                {{ dateFormat(scope.row.created_at, false, 'YYYY-MM-DD') }}
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
import { transactionList } from '../../http/apis/vendor';
import { getRechargeTable } from '../../http/apis/merchant';
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio, ElMessage, ElMessageBox } from 'element-plus';
import { generateCode } from '../../utils/generateCode';
import { empty, dateFormat, getSummaries, toFixedNReport } from '../../utils/common';
import store from '../../store';

export default {
  name: 'MerchantList',
  components: { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio },
  setup() {
    const generate = () => {
      state.defaultForm.ppk = generateCode();
    };
    const state = reactive({
      query: '',
      formInline: {},
      isEdit: true,
      currentPage: 1,
      total: 0,
      page_size: 50,
      list: [],
      isShowDialog: false,
      isShowDownloadDialog: false,
      times: [],
      defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)],
      rechargeList: []
    });

    const getList = () => {
      transactionList(
        empty.preProcessData({
          ...state.formInline,
          page: state.currentPage,
          page_size: state.page_size
        })
      ).then(res => {
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

    // 搜索
    const search = () => {
      console.log(state.formInline);
      state.currentPage = 1;
      getList();
    };

    const handleSizeChange = val => {
      state.page_size = val;
      state.currentPage = 1;
      getList();
    };
    const handleCurrentChange = val => {
      state.currentPage = val;
      getList();
    };

    const flagsToText = row => {
      if (row.cash_type == 1) {
        return 'API提现';
      }
      if (row.cash_type == 2) {
        return '手动提现';
      }
      if (row.cash_type == 3) {
        return '代充手续费';
      }
      if (row.cash_type == 4) {
        return '代付手续费';
      }
      if (row.cash_type == 5) {
        return 'usdt充值';
      }
      if (row.cash_type == 6) {
        return '银行卡充值';
      }
      if (row.cash_type == 7) {
        return '余额划转';
      }
      if (row.cash_type == 8) {
        return '手动代付失败返款';
      }
      if (row.cash_type == 9) {
        return '增加余额';
      }
      if (row.cash_type == 10) {
        return '商户冲正';
      }
      if (row.cash_type == 11) {
        return '代付手续费返款';
      }
      if (row.cash_type == 12) {
        return '扣减余额';
      }
    };

    const textColor = value => {
      if (value >= 0) {
        return 'green';
      } else {
        return 'red';
      }
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
      getRechargeTable({ flag: 3 }).then(res => {
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
      isShowTable,
      flagsToText,
      getSummaries,
      textColor,
      handleSizeChange,
      toFixedNReport,
      handleCurrentChange,
      generate,
      search,
      dateFormat,
      setSearchTime,
      token
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
</style>