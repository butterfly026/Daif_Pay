<template>
  <!-- 弹窗 -->
  <el-dialog
    :title="title"
    :close-on-click-modal="false"
    class="permission_dialog"
    v-loading="loading"
    element-loading-text="请求加载中"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.1)"
    :visible="visible"
    width="38%"
    @close="close"
  >
    <el-form label-width="auto" :model="info" :rules="rules" ref="postForm">
      <el-form-item label="创建人：" v-if="info.createdBy">{{ info.createdBy }}</el-form-item>
      <el-form-item label="创建日期：" v-if="info.createdAt">{{ dateFormat(new Date(info.createdAt)) }}</el-form-item>

      <el-form-item label="账户名称：" prop="username">
        <el-input v-model="info.username" placeholder="请输入账户名称" maxlength="30" :disabled="role == 'update'"></el-input>
      </el-form-item>

      <!--            修改密码时，不输入就显示几个圈，不更新，新增不用判断-->
      <el-form-item label="账户密码：" prop="pwd">
        <el-input
          size="small"
          :type="passwordType"
          v-model="info.pwd"
          auto-complete="new-password"
          maxlength="30"
          @focus="inputPassword = true"
          @blur="inputPassword = false"
          :placeholder="role === 'update' ? (inputPassword ? '' : '************') : '请输入账户密码'"
          :class="role === 'update' ? 'password' : ''"
        >
          <i class="el-icon-view el-input__icon" v-if="!!info.pwd" slot="suffix" @click="showPassword"></i>
          <i slot="prefix" class="icon-mima"></i>
        </el-input>
      </el-form-item>
      <el-form-item label="分组类型：" prop="group_type">
        <el-select class="input-width" v-model="info.group_type" @change="changeGroupList" placeholder="请选择">
          <el-option v-for="(item, index) in typeList" :key="index" :label="item.value" :value="item.key"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="用户分组：" prop="group_id" v-if="info.group_type">
        <el-select class="input-width" v-model="info.group_id" placeholder="请选择">
          <el-option v-for="(item, index) in groupList" :key="index" :label="item.gname" :value="item.gid"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="启用状态：" prop="state">
        <el-select placeholder="启用状态" v-model="info.state">
          <el-option :value="1" label="启用"></el-option>
          <el-option :value="0" label="禁用"></el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <div class="bottom" slot="footer">
      <el-button @click="close">取消</el-button>
      <!-- :disabled="createDisable" -->
      <el-button v-loading="btnLoading" :disabled="btnLoading" class="loading-btn" element-loading-spinner="el-icon-loading" @click="confirm" type="primary">
        <!-- {{ info.verifyType == 2 && !info.googleKey ? '下一步' : '确认' }} -->
        确认
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getGroupList } from '@/api/permission';
export default {
  name: 'ModifyDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    roles: {
      type: Array,
      default: () => []
    },
    info: {
      type: Object
    },
    role: {
      type: String,
      default: () => ''
    },
    typeList: {
      type: Array,
      default: () => []
    },
    typeList: {
      type: Array,
      default: () => []
    }
    // createDisable: {
    //     type: Boolean,
    //     default: false
    // }
  },
  computed: {
    title() {
      return this.role === 'update' ? '修改账号' : '新增账号';
    }
  },
  data() {
    var validateUserName = async (rule, value, callback) => {
      const patt = /^[0-9a-zA-Z]+$/;
      if (!value) {
        callback(new Error('账户名称不能为空'));
        return;
      }
      if (!patt.test(value)) {
        callback(new Error('用户名由2~30位字母、数字组成'));
        return;
      }
      if (value.length < 2 || value.length > 30) {
        callback(new Error('用户名由2~30位字母、数字组成'));
        return;
      }
      // if (this.role !== 'update') {
      //     const allMerchants = await this.$request('getAllMerchants')({
      //         adminName: value,
      //     })
      //     if (allMerchants.data.data) {
      //         callback(new Error('账号已存在'))
      //     }
      // }
      callback();
    };
    var validatePass = (rule, value, callback) => {
      if (!value) {
        callback(new Error('账户密码不能为空'));
      } else {
        if (!/^(?!([a-zA-Z]+|\d+)$)[a-zA-Z\d]{6,30}$/.test(value)) {
          callback(new Error('账户密码由字母数字组成并同时含数字和字母 长度为6-30'));
        }
        callback();
      }
    };
    var validatePass1 = (rule, value, callback) => {
      if (value === '') {
        callback();
        return;
      }
      if (!/^(?!([a-zA-Z]+|\d+)$)[a-zA-Z\d]{6,30}$/.test(value)) {
        callback(new Error('账户密码由字母数字组成并同时含数字和字母 长度为6-30'));
      }
      callback();
      return;
    };
    return {
      loading: false,
      btnLoading: false,
      modifyOrCreateAccount: null,
      passwordType: 'password',
      rules:
        this.role === 'update'
          ? {
              username: [
                {
                  required: true,
                  validator: validateUserName,
                  trigger: 'blur'
                }
              ],
              pwd: [
                {
                  required: true,
                  validator: validatePass1,
                  trigger: 'blur'
                }
              ],
              group_type: [
                {
                  required: true,
                  message: '请先选择用户分类',
                  trigger: 'blur'
                }
              ],
              state: [
                {
                  required: true,
                  message: '请先选择用户状态',
                  trigger: 'blur'
                }
              ],
              group_id: [
                {
                  required: true,
                  message: '请先选择用户分组',
                  trigger: 'blur'
                }
              ]
            }
          : {
              username: [
                {
                  required: true,
                  validator: validateUserName,
                  trigger: 'blur'
                }
              ],
              pwd: [
                {
                  required: true,
                  validator: validatePass,
                  trigger: 'blur'
                }
              ],
              group_id: [
                {
                  required: true,
                  message: '请先选择用户分组',
                  trigger: 'blur'
                }
              ],
              state: [
                {
                  required: true,
                  message: '请先选择用户状态',
                  trigger: 'blur'
                }
              ],
              group_type: [
                {
                  required: true,
                  message: '请先选择用户分类',
                  trigger: 'blur'
                }
              ]
            },
      inputPassword: false,
      groupList: []
    };
  },
  //
  created() {
    if (this.role === 'update') {
      this.loading = true;
      this.getGroupList();
    }
  },
  methods: {
    showPassword() {
      this.passwordType == '' ? (this.passwordType = 'password') : (this.passwordType = '');
    },

    close() {
      this.$emit('change-visible', false);
    },

    updateInfo() {
      this.$emit('change-row', { ...this.info });
    },
    changeGroupList() {
      this.getGroupList();
      this.info.group_id = '';
    },
    //分组管理列表
    async getGroupList() {
      const res = await getGroupList({
        types: this.info.group_type,
        state: 1
      });
      this.loading = false;
      this.groupList = res.data;
    },
    confirm() {
      const me = this;
      this.$refs.postForm.validate(valid => {
        if (valid) {
          // this.createDisable = true;
          // console.info( { ...me.info })
          // if (me.info.verifyType == 2 && !me.info.googleKey) {
          // me.$emit('change-visible', false)
          // me.$emit('openGADialog')
          //         } else {
          if (this.role === 'update') {
            this.btnLoading = true;
            me.$emit('update-action', { ...me.info }, () => {
              this.btnLoading = false;
            });
          } else {
            //                 me.$refs.postForm.validate(valid => {
            //                     if (valid) {
            this.btnLoading = true;
            me.$emit('create-action', { ...me.info }, () => {
              this.btnLoading = false;
            });
            // me.$emit('create-action')
            //                     } else {
            //                         console.log('error submit!!')
            //                         return false
            //                     }
            //                 })
          }
          //         }
          //     } else {
          //         console.warn('error submit!!')
          //         return false
        }
      });
    }
  }
};
</script>

<style lang="scss">
.el-input--prefix .el-input__inner {
  padding-left: 18px !important;
}
</style>
