<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="请选择商户">
            <el-select v-model="formInline.merchant_id" placeholder="请选择商户" clearable filterable>
              <el-option :label="item.name" :value="item.id" v-for="(item, index) in channelMenuList" :key="index">{{ item.name }}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="formInline.state">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="审核中" value="2">审核中</el-option>
              <el-option label="支付中" value="3">支付中</el-option>
              <el-option label="成功" value="1">成功</el-option>
              <el-option label="失败" value="0">失败</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="充值类型">
            <el-select v-model="formInline.flags">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="余额划转" value="1">余额划转</el-option>
              <el-option label="银行卡转账" value="2">银行卡转账</el-option>
              <el-option label="USDT" value="3">USDT</el-option>
              <el-option label="通道自带充值" value="4">通道自带充值</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="">
            <el-date-picker v-model="times" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" :default-time="defaultTime" @change="setSearchTime"> </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" @click="isShowTable"> 下载表格 </el-button>
          </el-form-item>
          <el-form-item style="float: right">
            <div style="display: flex">
              <div>{{ countdown >= 0 ? countdown : 0 }}S 后刷新&nbsp;|&nbsp;</div>
              <div><span style="cursor: pointer" @click="runTime" v-if="isRunTime">开启</span><span style="cursor: pointer" @click="stopTime" v-if="!isRunTime">暂停</span></div>
            </div>
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
          <el-table-column label="商户名" align="center">
            <template #default="scope">
              {{ channelIdToText(scope.row.merchant_id) }}
            </template>
          </el-table-column>
          <el-table-column label="充值金额" align="center" prop="amount">
            <template #default="scope">
              {{ toFixedNReport(scope.row.amount) }}
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
          <el-table-column label="审核时间" align="center">
            <template #default="scope">
              <span>
                {{ dateFormat(scope.row.review_at * 1000) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="审核人" align="center">
            <template #default="scope">
              {{ scope.row.review_by_name }}
            </template>
          </el-table-column>
          <el-table-column label="商户备注" align="center">
            <template #default="scope">
              <span v-html="scope.row.merchant_remark"></span>
            </template>
          </el-table-column>
          <el-table-column label="审核备注" align="center">
            <template #default="scope">
              <span v-html="scope.row.review_remark"></span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.state == 1 ? 'green' : 'red' }">
                {{ stateToText(scope.row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="200">
            <template #default="scope">
              <el-button v-if="scope.row.state == 2" type="primary" @click="upperPoints(scope.row)">上分</el-button>
              <el-button v-if="scope.row.state == 2" type="danger" @click="refuse(scope.row)">拒绝</el-button>
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
    <el-dialog v-model="dialogState" :title="operateState == 1 ? '上分' : '拒绝'" width="30%">
      <el-input type="textarea" v-model.trim="remars" placeholder="请输入备注" maxlength="256" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogState = false">关闭</el-button>
          <el-button type="primary" @click="sure">确定</el-button>
        </span>
      </template>
    </el-dialog>

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
              <a :href="'/admin/excel/redirect?id=' + scope.row.id + '&t=' + token" class="table-down" target="_blank">下载</a>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, toRefs, ref, onUnmounted } from 'vue';
import { addMerchant as addMerchantFetch, editMerchant, deleteMerchant, merchantDepositList, depositReject, depositResolve, getRechargeTable } from '../../http/apis/merchant';
import { ElMessage, ElMessageBox } from 'element-plus';
import { generateCode } from '../../utils/generateCode';
import { empty, dateFormat, toFixedNReport,getSummaries } from '../../utils/common';
import { merchantMenu } from '../../http/apis/channel';
import store from '../../store';

export default {
  name: 'MerchantList',
  setup() {
    onUnmounted(() => {
      clearInterval(timer.value);
    });
    const generate = () => {
      state.defaultForm.ppk = generateCode();
    };
    const state = reactive({
      query: '',
      formInline: {},
      isEdit: true,
      remars: '',
      defaultForm: {
        channel_id: '', // 代付渠道ID
        name: '', // 商户名称
        ppk: '', // 密钥
        withdraw_fee: '', // 费率
        m_single_withdraw: 1, // 手动单条代付
        m_batch_withdraw: 1, // 批量手动代付
        api_withdraw: 1, // api自动代付
        state: 1, // 状态
        remarks: '' // 备注
      },
      currentPage: 1,
      total: 0,
      page_size: 10,
      list: [],
      times: [],
      defaultTime: [new Date(2021, 1, 1, 0, 0, 0), new Date(2021, 2, 1, 23, 59, 59)],
      dialogState: false,
      isShowDialog: false,
      isShowDownloadDialog: false,
      rechargeList: [],
      countdown: 30,
      isRunTime: true
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
        state.list = [];
        if (state.currentPage == 1) {
          state.total = Number(res.data.total) || 0;
        }
        if (res.status) {
          state.countdown = 30;
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
      // console.log(state.query);
    };

    const addMerchant = () => {
      state.isEdit = false;
      state.isShowDialog = true;
    };

    const addForm = ref(null);

    const handleEdit = (index, row) => {
      state.isEdit = true;
      Object.assign(state.defaultForm, row);
      // console.log(state.defaultForm);
      state.isShowDialog = true;
    };

    const delMerchant = row => {
      ElMessageBox.confirm('你确定要删除吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMerchant({
          id: row.id
        }).then(res => {
          if (res.status) {
            ElMessage.success('删除成功');
            getList();
          }
        });
      });
    };

    const search = () => {
      state.formInline.name = decodeURIComponent(state.formInline.name || '');
      state.currentPage = 1;
      getList();
    };

    const onSubmit = () => {
      addForm.value.validate(valid => {
        if (valid) {
          [addMerchantFetch, editMerchant][state.isEdit ? 1 : 0](state.defaultForm).then(res => {
            if (res.status) {
              ElMessage.success(state.isEdit ? '编辑成功' : '新增成功');
              getList();
              onCancel();
            }
          });
        }
      });
    };

    const onCancel = () => {
      addForm.value && addForm.value.resetFields();
      state.isShowDialog = false;
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

    const operateState = ref(1); // 1:上分，2：拒绝
    let operateObj = {};

    // 上分
    const upperPoints = row => {
      state.remars = '';
      operateObj = row;
      operateState.value = 1;
      state.dialogState = true;
    };

    // 代付渠道列表
    const channelMenuList = ref([]);
    merchantMenu().then(res => {
      if (res.status) {
        if (Array.isArray(res.data)) {
          channelMenuList.value = res.data;
        } else {
          channelMenuList.value = [];
        }
      }
    });

    // 代付列表转文字
    const channelIdToText = pid => {
      const obj = channelMenuList.value.find(item => item.id == pid);
      return obj ? obj.name : '-';
    };

    // 拒绝
    const refuse = row => {
      state.remars = '';
      operateObj = row;
      operateState.value = 2;
      state.dialogState = true;
    };

    const close = () => {
      state.remars = '';
      state.dialogState = false;
    };

    const sure = () => {
      //  上分
      if (operateState.value == 1) {
        depositResolve({
          id: operateObj.id,
          remark: state.remars
        }).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            getList();
            close();
          }
        });
      } else {
        // 拒绝
        depositReject({
          id: operateObj.id,
          remark: state.remars
        }).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            getList();
            close();
          }
        });
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

    const timer = ref(null);

    const runTime = () => {
      state.isRunTime = false;
      timer.value = setInterval(() => {
        state.countdown -= 1;
        if (state.countdown == 0) {
          getList();
        }
      }, 1000);
    };

    const stopTime = () => {
      state.isRunTime = true;
      clearInterval(timer.value);
    };

    return {
      ...toRefs(state),
      setSearchTime,
      isShowTable,
      token,
      upperPoints,
      refuse,
      rules,
      sure,
      operateState,
      addForm,
      searchData,
      addMerchant,
      onCancel,
      toFixedNReport,
      channelIdToText,
      onSubmit,
      handleEdit,
      delMerchant,
      handleSizeChange,
      handleCurrentChange,
      channelMenuList,
      getSummaries,
      dateFormat,
      stateToText,
      generate,
      search,
      flagsToText,
      runTime,
      stopTime
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
.download-table {
  max-height: 450px;
  overflow-y: auto;
}
</style>