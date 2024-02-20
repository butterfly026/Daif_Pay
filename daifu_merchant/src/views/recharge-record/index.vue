<template>
  <div>
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>发起单笔提现</span>
        </div>
      </template>
      <div style="padding: 20px; width: 600px; margin: 0 auto">
        <el-form ref="formInlineRef" :model="formInline" class="demo-form-inline" label-width="auto" :rules="rules">
          <el-form-item label="账户金额">
            <el-input size="small" :model-value="toFixedNReport(formInline.balance)" placeholder="" autocomplete="off" disabled></el-input>
          </el-form-item>
          <el-form-item label="提现金额" prop="amount">
            <el-input size="small" v-model.trim="formInline.amount" type="number" autocomplete="off" placeholder="请输入提现金额"></el-input>
          </el-form-item>
          <el-form-item label="收款银行" prop="bank_name">
            <el-select v-model="formInline.bank_name" filterable clearable placeholder="请选择收款银行" @click="getBankList">
              <el-option v-for="(cItem, index) in bankList" :key="index" :label="cItem" :value="cItem"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="收款姓名" prop="bank_user">
            <el-input size="small" v-model.trim="formInline.bank_user" autocomplete="off" placeholder="请输入收款姓名"></el-input>
          </el-form-item>
          <el-form-item label="收款卡号" prop="bank_card">
            <el-input size="small" v-model.trim="formInline.bank_card" autocomplete="off" type="text" placeholder="请输入收款卡号"></el-input>
          </el-form-item>
          <el-form-item label="开户行">
            <el-input size="small" v-model.trim="formInline.bank_open" autocomplete="off" placeholder="请输入开户行"></el-input>
          </el-form-item>
          <el-form-item label="动态密码" prop="code">
            <el-input size="small" v-model.trim="formInline.code" autocomplete="off" type="number" placeholder="请输入动态密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="insert">提交</el-button>
            <el-button size="small" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>
<script>
import { defineComponent, reactive, ref, toRefs } from 'vue';
import { getBalance, getBankName, singleWithdrawal } from '../../http/apis/withdrawal';
import { toFixedNReport } from '../../utils/common';
import { ElMessage } from 'element-plus';

export default defineComponent({
  setup() {
    const formInlineRef = ref();
    const state = reactive({
      formInline: {}
    });
    const bankList = ref([]);

    // 获取用户余额信息
    const getBalanceHanlder = () => {
      getBalance().then(res => {
        state.formInline.balance = res.data || 0;
      });
    };

    // 获取银行名称列表
    const getBankList = () => {
      bankList.value = [];
      getBankName().then(res => {
        if (res.status) {
          Object.keys(res.data).forEach(key => {
            bankList.value.push(key);
          });
        } else {
          ElMessage.error('获取银行列表信息失败，请刷新页面');
        }
      });
    };

    getBalanceHanlder();

    // 提交提现申请
    const insert = () => {
      formInlineRef.value.validate(valid => {
        if (valid) {
          if (parseFloat(state.formInline.amount) > parseFloat(state.formInline.balance)) {
            ElMessage.error('提现金额不能大于账户金额');
            return;
          }
          if (parseFloat(state.formInline.amount) < 1) {
            ElMessage.error('提现金额不能小于1');
            return;
          }
          if (parseFloat(state.formInline.amount) > 50000) {
            ElMessage.error('提现金额不能大于50000');
            return;
          }
          const params = {
            ...state.formInline
          };
          delete params.balance;
          singleWithdrawal(params).then(res => {
            if (res.status) {
              ElMessage.success('操作成功');
              reset();
              state.formInline.bank_open = '';
              getBalanceHanlder();
            }
          });
        } else {
          return false;
        }
      });
    };

    // 重置表单
    const reset = () => {
      formInlineRef.value.resetFields();
      state.formInline.bank_open = '';
    };

    const rules = {
      amount: [
        {
          required: true,
          message: '请输入提现金额，金额为1-50000之间',
          trigger: 'blur'
        }
      ],
      bank_name: [
        {
          required: true,
          message: '请选择收款银行',
          trigger: 'blur'
        }
      ],
      bank_user: [
        {
          required: true,
          message: '请输入收款姓名',
          trigger: 'blur'
        }
      ],
      bank_card: [
        {
          required: true,
          message: '请输入收款卡号',
          trigger: 'blur'
        }
      ],
      bank_open: [
        {
          required: true,
          message: '请输入开户行',
          trigger: 'blur'
        }
      ],
      code: [
        {
          required: true,
          message: '请输入动态密码',
          trigger: 'blur'
        }
      ]
    };
    return {
      ...toRefs(state),
      formInlineRef,
      insert,
      rules,
      reset,
      bankList,
      getBankList,
      toFixedNReport
    };
  }
});
</script>

<style scoped lang="less">
</style>