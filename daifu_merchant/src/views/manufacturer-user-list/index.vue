<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商户信息</span>
        </div>
      </template>
      <div>
        <table class="table">
          <tbody>
            <tr>
              <td rowspan="9" class="text-center">
                <div><img src="../../assets/index.png" height="100" width="100" /></div>
                <el-button size="mini" type="success" style="margin-top: 12px" plain @click="setNewPwd">修改登录密码</el-button>
              </td>
            </tr>
            <tr>
              <td bgcolor="#f3f3f4" class="text-center">商户号</td>
              <td>{{ userInfo.merchant_id }}</td>
              <td bgcolor="#f3f3f4" class="text-center">今日充值金额</td>
              <td>{{ userInfo.deposit_sa }}</td>
              <td bgcolor="#f3f3f4" class="text-center">今日充值笔数</td>
              <td>{{ userInfo.deposit_sc }}</td>
            </tr>
            <tr>
              <td bgcolor="#f3f3f4" class="text-center">今日提现金额</td>
              <td>{{ userInfo.withdraw_sa }}</td>
              <td bgcolor="#f3f3f4" class="text-center">今日提现笔数</td>
              <td>{{ userInfo.withdraw_sc }}</td>
              <td bgcolor="#f3f3f4" class="text-center">商户可用余额</td>
              <td>{{ userInfo.balance }}</td>
            </tr>

            <tr>
              <td bgcolor="#f3f3f4" class="text-center">处理中的提现订单数</td>
              <td>{{ userInfo.withdraw_cc }}</td>
              <td bgcolor="#f3f3f4" class="text-center">最后登录ip</td>
              <td>{{ userInfo.login_ip }}</td>
              <td bgcolor="#f3f3f4" class="text-center">最后登录时间</td>
              <td>{{ userInfo.last_login_time ? format(userInfo.last_login_time*1000, 'yyyy-MM-dd hh:mm:ss') : '' }}</td>
            </tr>

            <tr>
              <td bgcolor="#f3f3f4" class="text-center">接口文档下载</td>
              <td>
                <a href="#" style="color: #337ab7">下载</a>
              </td>
              <td bgcolor="#f3f3f4" class="text-center">商户秘钥</td>
              <td>
                <span class="" v-if="showCode">{{ userKeyCode ? userKeyCode : '' }}</span>
                <el-button class="copy-btn" size="mini" type="success" plain style="margin-left: 10px" v-if="!showCode" @click="handleShowCode">查看秘钥</el-button>
              </td>
              <td bgcolor="#f3f3f4" class="text-center"></td>
              <td></td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
    <el-dialog v-model="showDialog" title="修改密码" center width="25%">
      <el-form ref="formInlineRef" :model="formInline" class="demo-form-inline" label-width="auto" :rules="rules">
        <el-form-item label="原密码" prop="old_password">
          <el-input size="small" v-model.trim="formInline.old_password" autocomplete="off" type="password" placeholder="请输入原密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="new_password" required>
          <el-input size="small" v-model.trim="formInline.new_password" autocomplete="off" type="password" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="重复新密码" prop="refPwd" required>
          <el-input size="small" v-model.trim="formInline.refPwd" autocomplete="off" type="password" placeholder="重复输入新密码"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="onCancel">取消</el-button>
          <el-button type="primary" @click="onSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showCodeDialog" title="验证密码" center width="25%">
      <el-form ref="formInlineRef" :model="checkForm" class="demo-form-inline" label-width="auto" :rules="rules">
        <el-form-item label="密码" prop="password">
          <el-input size="small" v-model.trim="checkForm.password" autocomplete="off" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelCheck">取消</el-button>
          <el-button type="primary" @click="submitCheck">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getUserInfo, resetPWD, getKeyCode } from '../../http/apis/userInfo';
import { format } from '../../utils/common';
export default {
  setup() {
    const userInfo = ref({});

    const formInline = reactive({
      old_password: '',
      new_password: '',
      refPwd: ''
    });

    const checkForm = reactive({
      password: ''
    });

    const rules = {
      old_password: [
        {
          required: true,
          message: '请输入原密码',
          trigger: 'blur'
        }
      ],
      new_password: [
        {
          required: true,
          message: '请输入新密码',
          trigger: 'blur'
        },
        {
          min: 8,
          message: '密码长度不能少于8位',
          trigger: 'blur'
        }
      ],
      refPwd: [
        {
          required: true,
          message: '请重复新密码',
          trigger: 'blur'
        },
        {
          min: 8,
          message: '密码长度不能少于8位',
          trigger: 'blur'
        }
      ]
    };
    const showCode = ref(false);
    const showDialog = ref(false);
    const showCodeDialog = ref(false);
    const formInlineRef = ref(null);
    const userKeyCode = ref(null);

    // 修改密码弹窗
    const setNewPwd = () => {
      showDialog.value = true;
      formInlineRef.value && formInlineRef.value.resetFields();
    };

    // 查看商户密码验证密码弹窗
    const handleShowCode = () => {
      showCodeDialog.value = true;
    };

    // 取消修改密码
    const onCancel = () => {
      formInlineRef.value.resetFields();
      showDialog.value = false;
    };

    // 获取商户信息
    const getUserData = () => {
      getUserInfo().then(res => {
        console.log(res);
        if (res.status) {
          userInfo.value = res.data;
        } else {
          ElMessage.error('获取商户信息失败！');
        }
      });
    };

    getUserData();

    // 取消密码验证
    const cancelCheck = () => {
      showCodeDialog.value = false;
      formInlineRef.value.resetFields();
    };

    // 提交密码验证
    const submitCheck = () => {
      getKeyCode(checkForm).then(res => {
        if (res.status) {
          userKeyCode.value = res.data;
          showCode.value = true;
          showCodeDialog.value = false;
          formInlineRef.value.resetFields();
        } else {
          return false;
        }
      });
    };

    // 提交修改密码
    const onSubmit = () => {
      formInlineRef.value.validate(valid => {
        if (valid) {
          if (formInline.new_password !== formInline.refPwd) {
            ElMessage.error('两次密码输入不一致，请重新输入！');
            return false;
          }
          const params = {
            ...formInline
          };
          delete params.refPwd;
          resetPWD(params).then(res => {
            if (res.status) {
              ElMessage.success('密码修改成功！');
              showDialog.value = false;
              formInlineRef.value.resetFields();
              setTimeout(()=>{
              window.location.reload();
              },2000)
            }
          });
        } else {
          return false;
        }
      });
    };

    return {
      formInline,
      showDialog,
      showCodeDialog,
      formInlineRef,
      userInfo,
      rules,
      showCode,
      setNewPwd,
      userKeyCode,
      handleShowCode,
      format,
      checkForm,
      submitCheck,
      cancelCheck,
      getUserData,
      onCancel,
      onSubmit
    };
  }
};
</script>
<style lang="less" scoped>
.add-merchant {
  margin: 0 auto;
  width: 800px;
}
.table {
  width: 100%;
  max-width: 100%;
  font-size: 13px;
  border-collapse: collapse;
  tr {
    td {
      padding: 8px;
      border: 1px solid #ccc;
    }
  }
  .text-center {
    text-align: center;
  }
  .key-code {
    display: inline-block;
    max-width: 240px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>