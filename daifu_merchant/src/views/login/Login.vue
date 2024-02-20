<template>
  <div class="title">
    <h1>星星代付V3.0</h1>
  </div>
  <div class="login-cont">
    <h2 class="center">商户后台系统</h2>
    <p class="center">请登录你的账号</p>
    <el-form label-position="right" ref="refForm" label-width="auto" :model="ruleForm" :rules="{
        username: [
          {
            required: true,
            message: '请输入账号',
            trigger: 'blur'
          }
        ],
        password: [
          {
            required: true,
            message: '请输入账号密码',
            trigger: 'blur'
          },
          {
            pattern: password,
            message: '密码格式不正确，请重新输入！',
            trigger: ['blur', 'change']
          }
        ],
        code: [
          {
            required: true,
            message: '请输入动态密码',
            trigger: ['blur', 'change']
          }
        ]
      }">
      <el-form-item label="账号" prop="username">
        <el-input v-model.trim="ruleForm.username"  placeholder="请输入账号"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password" class="e-over-row">
        <el-input
          v-model.trim="ruleForm.password"

          placeholder="请输入密码"
          type="password"
          maxlength="24"
        ></el-input>
      </el-form-item>
      <el-form-item label="动态密码" prop="code">
        <el-input v-model.trim="ruleForm.code" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item class="login-btn">
        <el-button type="primary" @click="submitForm()">登录</el-button>
      </el-form-item>
    </el-form>
    <!-- <abc /> -->
  </div>
</template>
<script>
  import { reactive, ref } from 'vue'
  import { ElButton } from 'element-plus';
  import { login, getGroupList } from '@/http/apis/login'
  import {password} from '../../utils/expressions'
  import store from '../../store/index'
  import {useRouter} from 'vue-router'

  export default {
    components: {
      [ElButton.name]: ElButton
    },

    setup() {

      const router = useRouter();

      const ruleForm = reactive({
        username: "",
        password: '',
        code: ''
      })

      const refForm = ref(null);

      function submitForm(formName) {
        refForm.value.validate(valid => {
          if (valid) {
            login(ruleForm).then(res => {
              if (res.status) {
                store.commit('updateUserInfo', {
                  token: res['data'],
                  login: true,
                  username: ruleForm.username
                });
                router.push('/vendor/manufactureruserlist')
              }
            }).catch(err => {})
          } else {
            console.log('error submit!!');
            return false;
          }
        });
      }

      return {
        submitForm,
        ruleForm,
        refForm,
        password
      }
    }
  }
</script>

<style scoped lang="less">
.title{
  text-align: center;
  position: fixed;
  left: 50%;
  top: 25%;
  transform: translate(-50%, -50%);
}
.login-cont {
  width: 400px;
  background-color: #fff;
  padding: 30px;
  border-radius: 10px;
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  > p {
    padding: 10px 20px;
  }
}
</style>