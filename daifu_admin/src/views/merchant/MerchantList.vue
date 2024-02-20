<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="代付渠道">
            <el-select v-model="formInline.channel_id" placeholder="请选择代付渠道" clearable>
              <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="代理员">
            <el-select v-model="formInline.agent_id" placeholder="请选择代理员" clearable>
              <el-option v-for="(item, index) in agentMenuList" :label="item.username" :value="item.uid" :key="index"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="商户名">
            <el-input v-model="formInline.name" placeholder="请输入商户名"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="formInline.state">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="正常" value="1">正常</el-option>
              <el-option label="关闭" value="0">关闭</el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="addMerchant"> 添加商户 </el-button>
          </el-form-item>
        </el-form>
        <!-- <el-input placeholder="请输入商户账号" v-model="query" style="width: 280px; margin-left: 0" class="filter-item" />
        <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-search" @click="searchData"> 搜索 </el-button>
        <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="addMerchant"> 添加商户 </el-button> -->
      </div>
      <div class="content-list">
        <el-table :data="list" element-loading-text="Loading" :summary-method="getSummaries" show-summary border fit highlight-current-row>
          <el-table-column label="商户ID" align="center">
            <template #default="scope">
              <span>{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="商户名称" align="center">
            <template #default="scope">
              <span>{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="代理员" align="center">
            <template #default="scope">
              {{ agentIdToText(scope.row.agent_id) }}
            </template>
          </el-table-column>
          <el-table-column label="代理佣金" align="center">
            <template #default="scope">
              {{ scope.row.agent_ratio ? toFixedNReport(scope.row.agent_ratio) + '%' : 0 }}
            </template>
          </el-table-column>
          <el-table-column label="商户余额" align="center" prop="balance">
            <template #default="scope">
              <span>{{ toFixedNReport(scope.row.balance) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="代付渠道" align="center">
            <template #default="scope">
              {{ channelIdToText(scope.row.channel_id) }}
            </template>
          </el-table-column>
          <el-table-column label="单笔提现手续费" align="center">
            <template #default="scope">
              {{ scope.row.withdraw_fee ? toFixedNReport(scope.row.withdraw_fee) + '%' : 0 }}
            </template>
          </el-table-column>
          <el-table-column label="单笔提现比例" align="center">
            <template #default="scope">
              {{ scope.row.withdraw_scale }}
            </template>
          </el-table-column>
          <el-table-column label="银行代充手续费" align="center">
            <template #default="scope">
              {{ scope.row.deposit_bank_fee ? toFixedNReport(scope.row.deposit_bank_fee) + '%' : 0 }}
            </template>
          </el-table-column>
          <el-table-column label="自动代付限额" align="center">
            <template #default="scope">
              {{ toFixedNReport(scope.row.confirm) }}
            </template>
          </el-table-column>
          <el-table-column label="最小出款限额" align="center">
            <template #default="scope">
              {{ toFixedNReport(scope.row.min_limit) }}
            </template>
          </el-table-column>
          <el-table-column label="最大出款限额" align="center">
            <template #default="scope">
              {{ toFixedNReport(scope.row.max_limit) }}
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center">
            <template #default="scope">
              {{ dateFormat(scope.row.created_at * 1000) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.state == 1 ? 'green' : 'red' }">
                {{ scope.row.state == 1 ? '正常' : '关闭' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="备注" align="center">
            <template #default="scope">
              <span v-html="scope.row.remarks"></span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="280">
            <template #default="scope">
              <el-button type="primary" size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
              <el-button type="primary" size="mini" @click="showChangeDialog(scope.$index, scope.row)">余额调整</el-button>
              <el-button size="mini" type="danger" @click="delMerchant(scope.row)">删除</el-button>
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
      <el-dialog v-model="isShowDialog" :title="`${!isEdit ? '添加' : '编辑'}商户`" center>
        <div>
          <el-form :model="defaultForm" :rules="rules" ref="addForm">
            <el-form-item label="代付渠道" prop="channel_id" label-width="140px">
              <el-select v-model="defaultForm.channel_id" placeholder="请选择渠道">
                <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="商户名称" prop="name" label-width="140px">
              <el-input v-model="defaultForm.name" maxlength="20" autocomplete="off" placeholder="请输入商户名称"></el-input>
            </el-form-item>
            <div class="fee-form-item">
              <el-form-item label="代理员" prop="agent_id" label-width="140px">
                <el-select v-model="defaultForm.agent_id" placeholder="请选择渠道">
                  <el-option v-for="(item, index) in agentMenuList" :label="item.username" :value="item.uid" :key="index"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="代理佣金" prop="agent_ratio" label-width="140px">
                <el-input v-model="defaultForm.agent_ratio" type="number" maxlength="50" autocomplete="off" placeholder="请输入单笔提现费率"
                  ><template #suffix><i style="margin-right: 10px">%</i></template></el-input
                >
              </el-form-item>
            </div>

            <template v-if="isEdit">
              <el-form-item label="密钥" label-width="140px">
                <div class="login-key">
                  <el-input v-model="defaultForm.ppk" disabled maxlength="20" autocomplete="off" placeholder="请生成密钥"></el-input><el-button @click="generate">生成密钥</el-button>
                </div>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="密钥" prop="ppk" label-width="140px">
                <div class="login-key">
                  <el-input v-model="defaultForm.ppk" disabled maxlength="20" autocomplete="off" placeholder="请生成密钥"></el-input><el-button @click="generate">生成密钥</el-button>
                </div>
              </el-form-item>
            </template>

            <div class="fee-form-item">
              <el-form-item label="单笔提现费率" prop="withdraw_fee" label-width="140px">
                <el-input v-model="defaultForm.withdraw_fee" type="number" maxlength="50" autocomplete="off" placeholder="请输入单笔提现费率"
                  ><template #suffix><i style="margin-right: 10px">%</i></template></el-input
                >
              </el-form-item>
              <el-form-item label="单笔提现手续费" prop="withdraw_scale" label-width="140px">
                <el-input v-model="defaultForm.withdraw_scale" type="number" maxlength="50" autocomplete="off" placeholder="请输入单笔提现手续费"></el-input>
              </el-form-item>
            </div>

            <el-form-item label="银行代充手续费" prop="deposit_bank_fee" label-width="140px">
              <el-input v-model="defaultForm.deposit_bank_fee" type="number" maxlength="50" autocomplete="off" placeholder="请输入银行代充手续费"
                ><template #suffix><i style="margin-right: 10px">%</i></template></el-input
              >
            </el-form-item>

            <el-form-item label="自动代付最大限额" prop="confirm" label-width="140px">
              <el-input v-model="defaultForm.confirm" type="number" maxlength="50" autocomplete="off" placeholder="请输入自动代付最大限额"></el-input>
            </el-form-item>

            <div class="fee-form-item">
              <el-form-item label="最小出款限额" prop="min_limit" label-width="140px">
                <el-input v-model="defaultForm.min_limit" type="number" maxlength="50" autocomplete="off" placeholder="请输入最小出款限额"></el-input>
              </el-form-item>
              <el-form-item label="最大出款限额" prop="max_limit" label-width="140px">
                <el-input v-model="defaultForm.max_limit" type="number" maxlength="50" autocomplete="off" placeholder="请输入最大出款限额"></el-input>
              </el-form-item>
            </div>

            <el-form-item label="手动单笔代付" label-width="140px" prop="m_single_withdraw">
              <el-radio-group v-model="defaultForm.m_single_withdraw">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="手动批量代付" label-width="140px" prop="m_batch_withdraw">
              <el-radio-group v-model="defaultForm.m_batch_withdraw">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="API自动代付" label-width="140px" prop="api_withdraw">
              <el-radio-group v-model="defaultForm.api_withdraw">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="商户状态" label-width="140px" prop="state">
              <el-radio-group v-model="defaultForm.state">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" label-width="140px" prop="remarks">
              <el-input v-model="defaultForm.remarks" type="textarea"></el-input>
            </el-form-item>
            <el-form-item label="动态验证" label-width="140px" prop="code">
              <el-input v-model.trim="defaultForm.code" placeholder="请输入动态验证码" type="number" autocomplete="off"></el-input>
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

      <el-dialog v-model="isShowChange" title="商户余额调整" center width="20%">
        <el-form ref="modifyParms" :model="modifyForm" class="demo-form-inline" label-width="auto" :rules="rules">
          <el-form-item label="商户" prop="name">
            <el-input size="small" v-model.trim="modifyForm.name" autocomplete="off" disabled style="max-width: 216px"></el-input>
          </el-form-item>
          <el-form-item label="商户余额" prop="balance">
            <el-input size="small" :model-value="toFixedNReport(modifyForm.balance)" autocomplete="off" disabled style="max-width: 216px"></el-input>
          </el-form-item>
          <el-form-item label="账变类型" prop="ct">
            <el-select v-model="modifyForm.ct" placeholder="请选择账变类型">
              <el-option v-for="(item, index) in typeList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="调整余额" prop="amount">
            <el-input size="small" v-model.trim="modifyForm.amount" type="number" autocomplete="off" style="max-width: 216px"></el-input>
          </el-form-item>
          <el-form-item label="备注">
            <el-input size="small" v-model.trim="modifyForm.remark" type="textarea" style="max-width: 216px"></el-input>
          </el-form-item>
          <el-form-item label="动态验证" prop="code">
            <el-input size="small" v-model.trim="modifyForm.code" placeholder="请输入动态验证码" type="number" style="max-width: 216px"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelModify">取消</el-button>
            <el-button type="primary" @click="submitModify">确定</el-button>
          </span>
        </template>
      </el-dialog>
      <!-- 成功添加提示 -->
      <el-dialog v-model="isShowSuccessInfo" title="操作成功" center width="25%" :show-close="false" :close-on-press-escape="false" :close-on-click-modal="false">
        <div>
          <p>商户名称：{{ defaultForm.name }}</p>
          <p>商户ID：{{ isEdit ? defaultForm.id : merchantID }}</p>
          <p>单笔提现费率：{{ defaultForm.withdraw_fee + '%' }}</p>
          <p>单笔提现手续费：{{ defaultForm.withdraw_scale }}</p>
          <p>代理佣金手续费：{{ defaultForm.withdraw_scale }}</p>
          <p>银行代充手续费：{{ defaultForm.deposit_bank_fee + '%' }}</p>
          <p>自动代付最大限额：{{ defaultForm.confirm }}</p>
          <p>最小出款限额{{ defaultForm.min_limit }}</p>
          <p>最大出款限额{{ defaultForm.max_limit }}</p>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button type="primary" @click="closeSuccessInfo">好的</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, ref } from 'vue';
import { channelMenu } from '../../http/apis/channel';
import { addMerchant as addMerchantFetch, getMerchantList, editMerchant, deleteMerchant, changeMerchantMoney, getAccountType, agentMenu } from '../../http/apis/merchant';
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio, ElMessage, ElMessageBox } from 'element-plus';
// import { getMerchantList, addMerchant, editMerchant } from '@/http/apis/merchant';
import { generateCode } from '../../utils/generateCode';
import { empty, dateFormat, toFixedNReport, getSummaries } from '../../utils/common';
import { teny2, noNegative, teny3 } from '../../utils/expressions';

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
        agent_id: '',
        agent_radtio: '',
        ppk: '', // 密钥
        withdraw_fee: '', // 费率
        m_single_withdraw: 1, // 手动单条代付
        m_batch_withdraw: 1, // 批量手动代付
        api_withdraw: 1, // api自动代付
        confirm: 0, //自动代付最大限额
        min_limit: 100, //最小出款限额
        max_limit: 50000, //最大出款限额
        state: 1, // 状态
        remarks: '' // 备注
      },
      currentPage: 1,
      total: 0,
      page_size: 10,
      list: [],
      merchantID: '',

      isShowDialog: false,
      isShowChange: false,
      isShowSuccessInfo: false
    });

    const modifyForm = reactive({
      id: '',
      amount: '',
      ct: '',
      name: '',
      balance: '',
      remark: '',
      code: ''
    });

    const modifyParms = ref(null);

    const typeList = ref([]);

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

    const agentMenuList = ref([]);
    agentMenu().then(res => {
      if (res.status) {
        if (Array.isArray(res.data)) {
          agentMenuList.value = res.data;
        } else {
          agentMenuList.value = [];
        }
      }
    });

    // 代付列表转文字
    const channelIdToText = pid => {
      const obj = channelMenuList.value.find(item => item.id == pid);
      return obj ? obj.name : '-';
    };

    // 代理员列表转文字
    const agentIdToText = pid => {
      const obj = agentMenuList.value.find(item => item.uid == pid);
      return obj ? obj.username : '-';
    };

    const getList = () => {
      getMerchantList(
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
      ],
      withdraw_scale: [
        {
          required: true,
          message: '请输入单笔提现比例',
          trigger: 'blur'
        },
        {
          pattern: teny2,
          message: '请输入最多保留三位小数的正数',
          trigger: 'blur'
        }
      ],
      deposit_bank_fee: [
        {
          required: true,
          message: '请输入银行代充手续费',
          trigger: 'blur'
        },
        {
          pattern: teny2,
          message: '请输入最多保留三位小数的正数',
          trigger: 'blur'
        }
      ],
      cashtype: [
        {
          required: true,
          message: '请选择调整类型',
          trigger: 'blur'
        }
      ],
      code: [
        {
          required: true,
          message: '请输入动态验证码',
          trigger: 'blur'
        }
      ],
      amount: [
        {
          required: true,
          message: '请输入要调整的余额',
          trigger: 'blur'
        },
        {
          pattern: teny3,
          message: '请输入最多保留三位小数的正数',
          trigger: 'blur'
        }
      ],
      confirm: [
        {
          required: true,
          message: '请输入自动代付最大限额',
          trigger: 'blur'
        },
        {
          pattern: noNegative,
          message: '请输入正整数',
          trigger: 'blur'
        }
      ],
      min_limit: [
        {
          required: true,
          message: '请输入最小出款限额',
          trigger: 'blur'
        },
        {
          pattern: noNegative,
          message: '请输入正整数',
          trigger: 'blur'
        }
      ],
      max_limit: [
        {
          required: true,
          message: '请输入最大出款限额',
          trigger: 'blur'
        },
        {
          pattern: noNegative,
          message: '请输入正整数',
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
      state.defaultForm = {
        channel_id: '', // 代付渠道ID
        merchant_id: '',
        name: '', // 商户名称
        ppk: '', // 密钥
        withdraw_fee: '', // 费率
        m_single_withdraw: 1, // 手动单条代付
        m_batch_withdraw: 1, // 批量手动代付
        api_withdraw: 1, // api自动代付
        confirm: 0, //自动代付最大限额
        min_limit: 100, //最小出款限额
        max_limit: 50000, //最大出款限额
        state: 1, // 状态
        remarks: '', // 备注
        agent_country: "IPA",
        agent_ratio: '',
      };
    };

    const addForm = ref(null);

    const handleEdit = (index, row) => {
      addForm.value && addForm.value.resetFields();
      state.defaultForm.ppk = '';
      state.isEdit = true;
      Object.assign(state.defaultForm, row);
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

    const closeSuccessInfo = () => {
      state.isShowSuccessInfo = false;
      getList();
      onCancel();
    };

    const onSubmit = () => {
      if (state.defaultForm.min_limit >= state.defaultForm.max_limit) {
        ElMessage.error('最小出款金额不能大于或等于最大出款金额！');
        return false;
      }
      addForm.value.validate(valid => {
        if (valid) {
          [addMerchantFetch, editMerchant][state.isEdit ? 1 : 0](state.defaultForm).then(res => {
            if (res.status) {
              if (!state.isEdit) {
                state.merchantID = res.data;
                state.isShowSuccessInfo = true;
                state.isShowDialog = false;
              } else {
                ElMessage.success('编辑成功');
                getList();
                addForm.value && addForm.value.resetFields();
                state.isShowDialog = false;
              }
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

    // eslint-disable-next-line no-unused-vars
    const showChangeDialog = (index, row) => {
      state.isShowChange = true;
      modifyParms.value && modifyParms.value.resetFields();
      getAccountTypeList();
      modifyForm.id = row.id;
      modifyForm.name = row.name;
      modifyForm.balance = row.balance;
      modifyForm.remark = '';
    };

    const getAccountTypeList = () => {
      getAccountType().then(res => {
        if (res.status) {
          typeList.value = res.data;
        } else {
          return false;
        }
      });
    };

    const cancelModify = () => {
      state.isShowChange = false;
      modifyParms.value.resetFields();
    };

    const submitModify = () => {
      modifyParms.value.validate(valid => {
        if (valid) {
          const params = {
            ...modifyForm
          };
          delete params.name;
          delete params.balance;
          // console.log(params);
          changeMerchantMoney(params).then(res => {
            // console.log(res);
            if (res.status) {
              ElMessage.success('操作成功');
              state.isShowChange = false;
              modifyParms.value.resetFields();
              getList();
            } else {
              return false;
            }
          });
        }
      });
    };

    return {
      ...toRefs(state),
      rules,
      addForm,
      typeList,
      searchData,
      addMerchant,
      channelIdToText,
      agentIdToText,
      onCancel,
      onSubmit,
      closeSuccessInfo,
      handleEdit,
      delMerchant,
      handleSizeChange,
      handleCurrentChange,
      channelMenuList,
      agentMenuList,
      generate,
      getSummaries,
      search,
      dateFormat,
      toFixedNReport,
      showChangeDialog,
      modifyForm,
      modifyParms,
      cancelModify,
      submitModify,
      getAccountTypeList
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
.fee-form-item {
  display: flex;
}
</style>
