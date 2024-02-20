<template>
  <!--提现记录-->
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <!-- <el-form-item label="代付渠道">
            <el-select v-model="formInline.channel_id" placeholder="请选择代付渠道" clearable >
              <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="三方订单号">
            <el-input v-model="formInline.merchant_serial" placeholder="请输入三方订单号"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="formInline.state">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="自动提现失败" value="0">自动提现失败</el-option>
              <el-option label="手动提现失败" value="1">手动提现失败</el-option>
              <el-option label="成功" value="2">成功</el-option>
              <el-option label="人工审核中" value="3">人工审核中</el-option>
              <el-option label="自动出款中" value="4">自动出款中</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="">
            <el-date-picker v-model="times" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" :default-time="defaultTime" @change="setSearchTime"> </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" @click="exportTable"> 导出 </el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" @click="isShowTable"> 下载表格 </el-button>
            
          </el-form-item>
        </el-form>
      </div>
      <div class="content-list">
        <el-table :data="list" :summary-method="getSummaries" show-summary element-loading-text="Loading" border fit highlight-current-row>
          <el-table-column label="提现ID" align="center">
            <template #default="scope">
              <span>{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="商户ID" align="center">
            <template #default="scope">
              <span>{{ scope.row.merchant_id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="第三方订单号" align="center">
            <template #default="scope">
              <span>{{ scope.row.merchant_serial }}</span>
            </template>
          </el-table-column>
          <el-table-column label="提现类型" align="center">
            <template #default="scope">
              {{ txType(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="提现金额" align="center" prop="apply_amount">
            <template #default="scope">
              {{ toFixedNReport(scope.row.apply_amount) }}
            </template>
          </el-table-column>
          <el-table-column label="到账金额" align="center" prop="actually_amount">
            <template #default="scope">
              <span>
                {{ toFixedNReport(scope.row.actually_amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="手续费" align="center">
            <template #default="scope">
              {{ toFixedNReport(scope.row.fee) }}
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center">
            <template #default="scope">
              {{ dateFormat(scope.row.created_at * 1000) }}
            </template>
          </el-table-column>
          <el-table-column label="创建IP" align="center">
            <template #default="scope">
              {{ long2ip(scope.row.created_ip) }}
            </template>
          </el-table-column>
          <el-table-column label="银行信息" align="left" width="200px">
            <template #default="scope">
              <p>名称：{{ scope.row.bank_type_name }}</p>
              <p>卡号：{{ scope.row.bank_card }}</p>
              <p>姓名：{{ scope.row.bank_name }}</p>
              <p>开户行：{{ scope.row.bank_opening }}</p>
            </template>
          </el-table-column>
          <!-- <el-table-column label="开户人" align="center">
            <template #default="scope">
              {{ scope.row.bank_name }}
            </template>
          </el-table-column>
          <el-table-column label="卡号" align="center">
            <template #default="scope">
              {{ scope.row.bank_card }}
            </template>
          </el-table-column>
          <el-table-column label="开户行" align="center">
            <template #default="scope">
              {{ scope.row.bank_opening }}
            </template>
          </el-table-column> -->
          <el-table-column label="回调地址" align="center">
            <template #default="scope">
              {{ scope.row.callback_url }}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.state == 2 ? 'green' : 'red' }">
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
        <el-table :data="wthdrawalList" border fit style="width: 100%">
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
// import { channelMenu } from '../../http/apis/channel';
import { addMerchant as addMerchantFetch, getMerchantList, exportOrderTable,editMerchant, deleteMerchant, withdrawList, getWthdrawalTable } from '../../http/apis/merchant';
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio, ElMessage, ElMessageBox } from 'element-plus';
// import { getMerchantList, addMerchant, editMerchant } from '@/http/apis/merchant';
import { generateCode } from '../../utils/generateCode';
import { empty, long2ip, dateFormat, getSummaries, toFixedNReport } from '../../utils/common';
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
      formInline: {
        state: '',
        name: ''
      },
      isEdit: true,
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

      isShowDialog: false,
      isShowDownloadDialog: false,
      wthdrawalList: []
    });

    // // 代付渠道列表
    // const channelMenuList = ref([]);
    // channelMenu().then(res => {
    //   if (res.status) {
    //     if (Array.isArray(res.data)) {
    //       channelMenuList.value = res.data;
    //     } else {
    //       channelMenuList.value = [];
    //     }
    //   }
    // });

    // 代付列表转文字
    // const channelIdToText = pid => {
    //   const obj = channelMenuList.value.find(item => item.id == pid);
    //   return obj ? obj.name : '-';
    // };

    const getList = () => {
      console.log(state.formInline);
      withdrawList(
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

    const addMerchant = () => {
      state.isEdit = false;
      state.isShowDialog = true;
    };

    const addForm = ref(null);

    const handleEdit = (index, row) => {
      state.isEdit = true;
      Object.assign(state.defaultForm, row);
      console.log(state.defaultForm);
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

    const stateToText = row => {
      if (row.state == 0) {
        return '自动提现失败';
      }
      if (row.state == 1) {
        return '手动提现失败';
      }
      if (row.state == 2) {
        return '成功';
      }
      if (row.state == 3) {
        return '人工审核中';
      }
      if (row.state == 4) {
        return '出款中';
      }
    };

    const txType = row => {
      if (row.flags == 1) {
        return '单条手动';
      }
      if (row.flags == 2) {
        return '批量手动';
      }
      if (row.flags == 3) {
        return 'API';
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
      getWthdrawalTable({ flag: 2 }).then(res => {
        if (res.status) {
          if (Array.isArray(res.data)) {
            state.wthdrawalList = res.data || [];
          }
        }
      });
      state.isShowDownloadDialog = true;
    };

    const token = store.getters.userInfo.token;

    const exportTable = () => {
      if (state.formInline.st && state.formInline.et) {
        exportOrderTable({
          ...state.formInline
        }).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            let path = window.location.origin + '/merchant/excel/download?path=' + res.data + '&t=' + token;
            window.open(path);
          }
        });
      } else {
        ElMessage.warning('请选择开始时间和结束时间！！');
      }
    };

    return {
      ...toRefs(state),
      txType,
      stateToText,
      rules,
      addForm,
      setSearchTime,
      isShowTable,
      addMerchant,
      // channelIdToText,
      onCancel,
      long2ip,
      onSubmit,
      handleEdit,
      toFixedNReport,
      delMerchant,
      handleSizeChange,
      handleCurrentChange,
      // channelMenuList,
      dateFormat,
      generate,
      getSummaries,
      search,
      token,
      exportTable
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