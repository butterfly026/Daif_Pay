<template>
  <div>
    <div style="padding: 20px; width: 800px">
      <el-form ref="formInlineRef" :model="formInline" class="demo-form-inline" label-width="auto" :rules="rules">
        <el-form-item label="充值方式" prop="flags" >
          <el-radio-group v-model="formInline.flags">
              <el-radio v-for="(radioItem, radioIndex) in options" :key="radioIndex" :label="radioItem.value">
            {{ radioItem.label }}
          </el-radio>
        </el-radio-group>
        </el-form-item>
        <el-form-item label="账户金额">
          <el-input size="small" :model-value="toFixedNReport(formInline.balance)" placeholder="" disabled></el-input>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount" required>
          <el-input size="small" v-model="formInline.amount" type="number" placeholder="请输入充值金额" ></el-input>
        </el-form-item>
        <el-form-item label="选择银行" v-if="(formInline.flags==4)">
          <el-select v-model="formInline.bankCode" style="width:100%">
            <el-option v-for="(bankInfo, bankKey) in banks" :key="bankKey" :label="bankInfo.label" :value="bankInfo.code">{{ bankInfo.label }}</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="银行账户" prop="cardNo" v-if="(formInline.flags==4)">
          <el-input size="small" v-model="formInline.cardNo" placeholder="请输入银行账户号(浦发银行和民生银行必填)"></el-input>
        </el-form-item>
        <el-form-item label="动态密码" prop="code" required>
          <el-input size="small" v-model="formInline.code" type="number" placeholder="请输入动态密码" ></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input size="small" v-model="formInline.remarks" placeholder="请输入备注" ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="insert">提交</el-button>
          <el-button size="small" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
import { defineComponent, reactive, ref, toRefs, ToRefs  } from 'vue'
import { getBalance, depositInsert, chkSelfcharge } from '../../http/apis/vendor'
import { ElMessage } from 'element-plus'
import { toFixedNReport } from '../../utils/common'

export default defineComponent({
  setup() {
    const formInlineRef = ref();
    const state = reactive({
      formInline: {
        balance: 0,
        flags: 1
      },
      options:[{
        label: '余额划转',
        value: 1
      },{
        label: '银行卡转账',
        value: 2
      },{
        label: 'USDT',
        value: 3
      }],
      banks:[{
        label: '农业银行',
        code: 'ABC'
      },{
        label: '中国银行',
        code: 'BOC'
      },{
        label: '建设银行',
        code: 'CCB'
      },{
        label: '交通银行',
        code: 'BCM'
      },{
        label: '邮储银行',
        code: 'PSBC'
      },{
        label: '招商银行',
        code: 'CMB'
      },{
        label: '民生银行',
        code: 'CMBC'
      },{
        label: '光大银行',
        code: 'CEB'
      },{
        label: '平安银行',
        code: 'PAB'
      },{
        label: '浦发银行',
        code: 'SPDB'
      },{
        label: '北京银行',
        code: 'BOB'
      }],
    })

    const getBalanceHanlder = () => {
      getBalance().then(res => {
        state.formInline.balance = res.data || 0;
      })
    }
    getBalanceHanlder();

    const chkSelfchargeHandler = () => {
      chkSelfcharge().then(res => {
        if ((res.status) && (res.data) && (state.options.length==3)){
          state.options.push({
            label: '发起通道充值',
            value: 4
          });
        }
      })
    }
    chkSelfchargeHandler();

    const insert = () => {
      formInlineRef.value.validate((valid) => {
        if (valid) {
          const params = {
            ...state.formInline
          }
          delete params.balance;
          depositInsert(params).then(res => {
            if (res.status) {
              ElMessage.success("操作成功");
              if (state.formInline.flags == 4){
                window.open(res.data);
              }
              reset();
              getBalanceHanlder();
            }
          })

        } else {
          return false
        }
      });
    }
    const reset = () => {
      formInlineRef.value.resetFields();
    }

    const rules = {
      amount: [{
          required: true,
          message: '请输入充值金额',
          trigger: 'blur',
      }],
      code: [{
          required: true,
          message: '请输入动态密码',
          trigger: 'blur',
      }],
    }

    return {
      ...toRefs(state),
      formInlineRef,
      insert,
      toFixedNReport,
      rules,
      reset,
    };
  }
})
</script>

<style scoped lang="less">
</style>