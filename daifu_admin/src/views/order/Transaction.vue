<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="请选择商户">
            <el-select v-model="formInline.merchant_id" placeholder="请选择商户" filterable clearable>
              <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="订单ID">
            <el-input v-model="formInline.order_id" placeholder="请输入订单ID"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="formInline.cash_type">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="代充手续费" value="3">代充手续费</el-option>
              <el-option label="代付手续费" value="4">代付手续费</el-option>
              <el-option label="usdt充值" value="5">usdt充值</el-option>
              <el-option label="银行卡充值" value="6">银行卡充值</el-option>
              <el-option label="余额划转" value="7">余额划转</el-option>
              <el-option label="增加余额" value="9">增加余额</el-option>
              <el-option label="商户冲正" value="10">商户冲正</el-option>
              <el-option label="扣减余额" value="12">扣减余额</el-option>
            </el-select>
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

          <el-table-column label="商户名" align="center">
            <template #default="scope">
              {{ channelIdToText(scope.row.merchant_id) }}
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
          <el-table-column label="备注" align="center">
            <template #default="scope">
              <span style="font-weight: bold" v-html="scope.row.remark ? scope.row.remark : ''"></span>
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

    <div class="add-dialog">
      <el-dialog v-model="isShowDialog" :title="`${!isEdit ? '添加' : '编辑'}商户后台会员`" center>
        <div>
          <el-form :model="defaultForm" :rules="rules" ref="addForm">
            <el-form-item label="商户" prop="merchant_id" label-width="140px">
              <el-select v-model="defaultForm.merchant_id" placeholder="请选择商户">
                <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="会员名" prop="username" label-width="140px">
              <el-input v-model="defaultForm.username" maxlength="20" autocomplete="off" placeholder="请输入会员名"></el-input>
            </el-form-item>
            <template v-if="isRules">
              <el-form-item label="密码" label-width="140px">
                <el-input v-model="defaultForm.password" autofocus maxlength="20" autocomplete="off" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item label="确认密码" label-width="140px">
                <el-input v-model="defaultForm.password2" maxlength="20" autocomplete="off" placeholder="请确认密码"></el-input>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="密码" :prop="'password'" label-width="140px">
                <el-input v-model="defaultForm.password" autofocus maxlength="20" autocomplete="off" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item label="确认密码" :prop="'password2'" label-width="140px">
                <el-input v-model="defaultForm.password2" maxlength="20" autocomplete="off" placeholder="请确认密码"></el-input>
              </el-form-item>
            </template>
            <el-form-item v-if="!isEdit" label="登录密钥" prop="google" label-width="140px">
              <div class="login-key">
                <el-input v-model="defaultForm.google" disabled maxlength="20" autocomplete="off" placeholder="点击生成登录密钥"></el-input><el-button @click="generate">生成密钥</el-button>
              </div>
            </el-form-item>
            <el-form-item label="商户状态" label-width="140px">
              <el-radio-group v-model="defaultForm.state">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="onCancel">取消</el-button>
            <el-button type="primary" @click="onSubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
    <el-dialog v-model="isShowDownloadDialog" title="表格下载" center width="560px">
      <div class="download-table">
        <el-table :data="transactionList" border fit style="width: 100%">
          <el-table-column type="index" align="center" width="50" label="序号" />
          <el-table-column label="日期" align="center">
            <template #default="scope">
              <span>
                {{ dateFormat(scope.row.created_at * 1000) }}
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
import { reactive, toRefs, ref, computed } from 'vue';
import { merchantMenu } from '../../http/apis/channel';
import { addMerchantMember, transfromWithdrawList, editMerchantMember, deleteMerchantMember, getRechargeTable } from '../../http/apis/merchant';
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio, ElMessage, ElMessageBox } from 'element-plus';
// import { getMerchantList, addMerchant, editMerchant } from '@/http/apis/merchant';
import { generateCode } from '../../utils/generateCode';
import { empty, dateFormat, long2ip, getSummaries, toFixedNReport } from '../../utils/common';
import { user_name, password, user_name_prompt, password2Msg } from '../../utils/expressions';
import store from '../../store';

export default {
  name: 'MerchantList',
  components: { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio },
  setup() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
      } else {
        if (state.defaultForm.password2 !== '') {
          addForm.value.validateField('password2');
        }
        callback();
      }
    };
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入确认密码'));
      } else if (value !== state.defaultForm.password) {
        callback(new Error('密码不正确'));
      } else {
        callback();
      }
    };

    const generate = () => {
      state.defaultForm.google = generateCode();
    };

    // 是否验证密码
    const isRules = computed(() => {
      // 编辑，并且密码为空，返回true
      return state.isEdit && !state.defaultForm.password && !state.defaultForm.password2;
    });

    const state = reactive({
      query: '',
      formInline: {
        state: '',
        name: ''
      },
      isEdit: true,
      defaultForm: {
        state: 1
      },
      currentPage: 1,
      total: 0,
      page_size: 50,
      list: [],

      isShowDialog: false,
      isShowDownloadDialog: false,
      times: [],
      defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)],
      transactionList: []
    });

    // 代付渠道列表
    const channelMenuList = ref([]);
    const channelName = ref(null);
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

    const getList = () => {
      transfromWithdrawList(
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

    const rules = reactive({
      merchant_id: [
        {
          required: true,
          message: '请选择代付渠道',
          trigger: 'blur'
        }
      ],
      username: [
        {
          required: true,
          message: '请输入用户名称',
          trigger: 'blur'
        },
        {
          pattern: user_name,
          message: user_name_prompt
        }
      ],
      google: [
        {
          required: true,
          message: '请先生成密钥',
          trigger: 'blur'
        }
      ],
      password: [
        {
          required: true,
          message: '请输入密码',
          trigger: 'blur'
        },
        {
          pattern: password,
          message: password2Msg
        },
        { validator: validatePass, trigger: 'blur' }
      ],
      password2: [
        {
          required: true,
          message: '请输入确认密码',
          trigger: 'blur'
        },
        {
          pattern: password,
          message: password2Msg
        },
        { validator: validatePass2, trigger: 'blur' }
      ]
    });
    // 搜索
    const searchData = () => {
      // console.log(state.query);
    };

    const addMerchant = () => {
      state.isEdit = false;
      state.defaultForm = {};
      state.isShowDialog = true;
    };

    const addForm = ref(null);

    const handleEdit = (index, row) => {
      state.isEdit = true;
      state.defaultForm.password = '';
      state.defaultForm.password2 = '';
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
        deleteMerchantMember({
          uid: row.uid
        }).then(res => {
          if (res.status) {
            ElMessage.success('删除成功');
            getList();
          }
        });
      });
    };

    const search = () => {
      state.formInline.username = decodeURIComponent(state.formInline.username || '');
      state.currentPage = 1;
      getList();
    };

    search();

    const onSubmit = () => {
      addForm.value.validate(valid => {
        if (valid) {
          let params = {};
          if (state.isEdit) {
            Object.assign(params, {
              uid: state.defaultForm.uid,
              username: state.defaultForm.username,
              password: state.defaultForm.password,
              state: state.defaultForm.state
            });
          } else {
            Object.assign(params, {
              ...state.defaultForm
            });
          }

          delete params.password2;
          [addMerchantMember, editMerchantMember][state.isEdit ? 1 : 0](params).then(res => {
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
      state.defaultForm = {};
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
            state.transactionList = res.data || [];
          }
        }
      });
      state.isShowDownloadDialog = true;
    };

    const token = store.getters.userInfo.token;

    return {
      ...toRefs(state),
      setSearchTime,
      isShowTable,
      token,
      rules,
      textColor,
      addForm,
      searchData,
      addMerchant,
      channelIdToText,
      onCancel,
      onSubmit,
      handleEdit,
      delMerchant,
      handleSizeChange,
      handleCurrentChange,
      channelMenuList,
      channelName,
      generate,
      search,
      dateFormat,
      toFixedNReport,
      long2ip,
      flagsToText,
      isRules,
      getSummaries
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