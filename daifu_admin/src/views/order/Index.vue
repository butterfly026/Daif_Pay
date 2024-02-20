<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="请选择商户">
            <el-select v-model="formInline.merchant_id" placeholder="请选择商户" clearable filterable style="width: 136px">
              <el-option :label="item.name" :value="item.id" v-for="(item, index) in allMenuList" :key="index">{{ item.name }}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="请选择上游渠道">
            <el-select v-model="formInline.channel_id" placeholder="请选择渠道" clearable filterable style="width: 136px">
              <el-option :label="item.name" :value="item.id" v-for="(item, index) in channelMenuList" :key="index">{{ item.name }}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="提现ID">
            <el-input v-model="formInline.id" placeholder="请输入提现ID" clearable></el-input>
          </el-form-item>
          <el-form-item label="三方订单号">
            <el-input v-model="formInline.merchant_serial" placeholder="请输入三方订单号" clearable></el-input>
          </el-form-item>
          <el-form-item label="提现类型">
            <el-select v-model="formInline.flags" style="width: 136px">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="单条手动" value="1">单条手动</el-option>
              <el-option label="批量手动" value="2">批量手动</el-option>
              <el-option label="API" value="3">API</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="formInline.state" style="width: 136px">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="自动提现失败" value="0">自动提现失败</el-option>
              <el-option label="手动提现失败" value="1">手动提现失败</el-option>
              <el-option label="成功" value="2">成功</el-option>
              <el-option label="人工审核中" value="3">人工审核中</el-option>
              <el-option label="自动出款中" value="4">自动出款中</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="客户名称">
            <el-input v-model="formInline.bank_name" placeholder="请输入客户名称" style="width: 136px" clearable></el-input>
          </el-form-item>
          <el-form-item label="">
            <el-date-picker v-model="times" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" :default-time="defaultTime" @change="setSearchTime"> </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" @click="exportTable"> 导出 </el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" @click="isShowTable"> 每日报表 </el-button>
          </el-form-item>
          <el-form-item>
            <div style="display: flex">
              <div style="font-size: small;margin-right: 10px;">订单总数</div>
              <div style="color: blue; margin-right: 10px; ">{{ total }}</div>
              <div style="font-size: small;margin-right: 10px;">总金额</div>
              <div v-if="total>0" style="margin-right: 10px;color: red;">  {{ toFixedNReport(all_money) }}</div>
              <div v-else style="margin-right: 10px;color: red;">  0</div>
              <div style="font-size: small;margin-right: 10px;">成功总金额</div>
              <div v-if="total>0" style="margin-right: 10px;color: blue;">{{ toFixedNReport(success_money) }}</div>
              <div v-else style="margin-right: 10px;color: blue;">0</div>
              <div style="font-size: small;margin-right: 10px;">失败总金额</div>
              <div v-if="total>0" style="color: blue;">{{ toFixedNReport(failed_money) }}</div>
              <div v-else style="color: blue;">0</div>
            </div>
          </el-form-item>
          <el-form-item style="float: right">
            <div style="display: flex">
              <div>{{ countdown >= 0 ? countdown : 0 }}S 后刷新&nbsp;|&nbsp;</div>
              <div>
                <span style="cursor: pointer" @click="runTime" v-if="isRunTime">开启</span>
                <span style="cursor: pointer" @click="stopTime" v-if="!isRunTime">暂停</span>
              </div>
            </div>
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
          <el-table-column label="商户名" align="center">
            <template #default="scope">
              <span>{{ renderMerName(scope.row.merchant_id) }}</span>
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
          <el-table-column label="银行信息" align="left" width="210px">
            <template #default="scope">
              <p>名称：{{ scope.row.bank_type_name }}</p>
              <p>卡号：{{ scope.row.bank_card }}</p>
              <p>姓名：{{ scope.row.bank_name }}</p>
              <p>开户行：{{ scope.row.bank_opening }}</p>
            </template>
          </el-table-column>
          <el-table-column label="回调地址" align="center" width="128px">
            <template #default="scope">
              {{ scope.row.callback_url }}
            </template>
          </el-table-column>
          <el-table-column label="出款上游" align="center">
            <template #default="scope">
              {{ channelIdToText(scope.row.channel_id) }}
            </template>
          </el-table-column>
          <el-table-column label="回调状态" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.notify_state == 0 ? 'red' : 'green' }">
                {{ scope.row.notify_state == 0 ? '失败' : scope.row.notify_state == 1 ? '成功' : '--' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="提现状态" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.state == 2 ? 'green' : 'red' }">
                {{ stateToText(scope.row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="回调反馈" align="center">
            <template #default="scope">
              {{ scope.row.err }}
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="220">
            <template #default="scope">
              <el-button @click="resolveReview(scope.row)" v-if="scope.row.state == 3" style="background-color: purple; color: #fff">出款</el-button>
              <el-button @click="notifyReview(scope.row)" v-if="scope.row.flags == 3 && scope.row.state != 3 && scope.row.state != 4" type="success">回调</el-button>
              <el-button @click="successReview(scope.row)" v-if="scope.row.state == 4" type="primary">成功</el-button>
              <el-button @click="delReview(scope.row)" v-if="scope.row.state == 3 || scope.row.state == 4" type="warning">拒绝</el-button>
              <el-button @click="queryOrder(scope.row)" style="background-color: #ec4859; color: #fff">查询</el-button>
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

      <el-dialog v-model="isShowChannel" title="选择出款渠道" center width="20%">
        <el-form ref="modifyParms" :model="channelForm" class="demo-form-inline" label-width="auto" :rules="rules">
          <el-form-item label="代付渠道" prop="channel_id">
            <el-select v-model="channelForm.channel_id" placeholder="请选择渠道">
              <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="动态验证" prop="code">
            <el-input v-model.trim="channelForm.code" placeholder="请输入动态验证码" type="number" autocomplete="off" style="max-width: 220px"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelChannel">取消</el-button>
            <el-button type="primary" @click="submitChannel">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <el-dialog v-model="isShowDownloadDialog" title="表格下载" center width="560px">
        <div class="download-table">
          <el-table :data="wthdrawalList" border fit style="width: 100%">
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

      <el-dialog v-model="isDelReview" :title="isSuccessOrder ? '确定成功这笔订单？' : '确定拒绝这笔订单？'" center width="20%">
        <el-form v-if="!isSuccessOrder" ref="modifyOrderForm" :model="operationForm" class="demo-form-inline" label-width="auto" :rules="rules">
          <el-form-item label="动态验证" prop="code">
            <el-input v-model.trim="operationForm.code" placeholder="请输入动态验证码" type="number" autocomplete="off" style="max-width: 220px"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelOperation">取消</el-button>
            <el-button type="primary" @click="submitOperation">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { reactive, toRefs, ref, onUnmounted } from 'vue';
import { channelMenu, merchantMenu, withdrawReject, withdrawApprove, withdrawNotify, withdrawSuccess, withdrawQuery } from '../../http/apis/channel';
import { addMerchant as addMerchantFetch, getMerchantMemberList, editMerchant, deleteMerchant, withdrawList, getWthdrawalTable, exportOrderTable } from '../../http/apis/merchant';
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElMessage, ElMessageBox } from 'element-plus';
// import { getMerchantList, addMerchant, editMerchant } from '@/http/apis/merchant';
import { generateCode } from '../../utils/generateCode';
import { empty, long2ip, dateFormat, getSummaries, toFixedNReport } from '../../utils/common';
import store from '../../store';

export default {
  name: 'MerchantList',
  components: { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem },
  setup() {
    onUnmounted(() => {
      clearInterval(timer.value);
    });

    const generate = () => {
      state.defaultForm.ppk = generateCode();
    };
    const state = reactive({
      query: '',
      formInline: {
        merchant_id: '',
        channel_id: '',
        id: '',
        merchant_serial: '',
        flags: '',
        state: '',
        bank_name: '',
        st: '',
        et: ''
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
      all_money: 0,
      success_money: 0,
      failed_money: 0,
      times: [],
      defaultTime: [new Date(2023, 1, 1, 0, 0, 0), new Date(2023, 2, 1, 23, 59, 59)],
      channelForm: {
        channel_id: '',
        id: ''
      },

      isShowDialog: false,
      isShowChannel: false,
      isShowDownloadDialog: false,
      wthdrawalList: [],
      countdown: 30,
      isRunTime: true,
      isDelReview: false,
      isSuccessOrder: false,
      operationForm: {}
    });

    const modifyParms = ref(null);

    // 代付渠道列表
    const channelMenuList = ref([]);
    channelMenu().then(res => {
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

    // 代付渠道列表
    const allMenuList = ref([]);
    merchantMenu().then(res => {
      if (res.status) {
        if (Array.isArray(res.data)) {
          allMenuList.value = res.data;
        } else {
          allMenuList.value = [];
        }
      }
    });

    const getList = () => {
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
          state.countdown = 30;
          if (Array.isArray(res.data.d)) {
            state.list = res.data.d || [];
          }
          state.all_money = res.data.summary.all_money;
          state.failed_money = res.data.summary.failed_money;
          state.success_money = res.data.summary.success_money;

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
      ],
      code: [
        {
          required: true,
          message: '请输入动态验证码',
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

    const merRenderList = ref([]);
    const getMerRenderList = () => {
      getMerchantMemberList(
        empty.preProcessData({
          ...state.formInline,
          page: state.currentPage,
          page_size: 200
        })
      ).then(res => {
        merRenderList.value = res.data.d || [];
      });
    };
    getMerRenderList();
    const renderMerName = id => {
      const obj = allMenuList.value.find(item => item.id == id);
      if (obj) {
        return obj.name;
      } else {
        return '-';
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

    const modifyOrderForm = ref(null);

    const cancelOperation = () => {
      modifyOrderForm.value && modifyOrderForm.value.resetFields();
      state.isDelReview = false;
    };

    const submitOperation = () => {
      if (state.isSuccessOrder) {
        let params = {};
        if (state.isSuccessOrder) {
          Object.assign(params, state.operationForm);
        } else {
          Object.assign(params, state.operationForm);
          delete params.channel_id;
        }
        withdrawSuccess(params).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            cancelOperation();
            getList();
          }
        });
      } else {
        modifyOrderForm.value.validate(valid => {
          if (valid) {
            let params = {};
            if (state.isSuccessOrder) {
              Object.assign(params, state.operationForm);
            } else {
              Object.assign(params, state.operationForm);
              delete params.channel_id;
            }
            withdrawReject(params).then(res => {
              if (res.status) {
                ElMessage.success('操作成功');
                cancelOperation();
                getList();
              }
            });
          }
        });
      }
    };

    const delReview = row => {
      state.operationForm = {};
      state.isSuccessOrder = false;
      state.isDelReview = true;
      state.operationForm.id = row.id;
    };

    const successReview = row => {
      state.operationForm = {};
      state.isSuccessOrder = true;
      state.isDelReview = true;
      state.operationForm.id = row.id;
      state.operationForm.channel_id = row.channel_id;
    };

    const resolveReview = row => {
      state.isShowChannel = true;
      state.channelForm.id = row.id;
    };

    const submitChannel = () => {
      withdrawApprove(state.channelForm).then(res => {
        if (res.status) {
          ElMessage.success('操作成功');
          getList();
          modifyParms.value.resetFields();
          state.isShowChannel = false;
        } else {
          modifyParms.value.resetFields();
          state.isShowChannel = false;
          return false;
        }
      });
    };

    const cancelChannel = () => {
      modifyParms.value.resetFields();
      state.isShowChannel = false;
    };

    const notifyReview = row => {
      ElMessageBox.confirm('你确定要回调吗？', '提示', {
        confirmButtonText: '回调',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        withdrawNotify({
          id: row.id
        }).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            getList();
          }
        });
      });
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

    const result_msg = ref(null);
    const queryOrder = row => {
        result_msg.value = '';
        withdrawQuery({ id: row.id, channel_id: row.channel_id }).then(res => {
          if(res.status) {
            ElMessage.warning(res.data);
            getList();
          }
        });
    };

    const exportTable = () => {
      if (state.formInline.st && state.formInline.et) {
        exportOrderTable({
          ...state.formInline
        }).then(res => {
          if (res.status) {
            ElMessage.success('操作成功');
            let path = window.location.origin + '/admin/excel/download?path=' + res.data + '&t=' + token;
            window.open(path);
          }
        });
      } else {
        ElMessage.warning('请选择开始时间和结束时间！！');
      }
    };

    return {
      ...toRefs(state),
      setSearchTime,
      isShowTable,
      token,
      modifyParms,
      stateToText,
      txType,
      rules,
      addForm,
      searchData,
      addMerchant,
      renderMerName,
      resolveReview,
      channelIdToText,
      onCancel,
      long2ip,
      onSubmit,
      handleEdit,
      delMerchant,
      delReview,
      successReview,
      handleSizeChange,
      notifyReview,
      handleCurrentChange,
      channelMenuList,
      toFixedNReport,
      dateFormat,
      allMenuList,
      generate,
      getSummaries,
      search,
      submitChannel,
      cancelChannel,
      runTime,
      stopTime,
      modifyOrderForm,
      cancelOperation,
      submitOperation,
      queryOrder,
      result_msg,
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