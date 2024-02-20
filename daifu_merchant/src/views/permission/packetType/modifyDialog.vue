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
    width="300"
    @close="close"
  >
    <el-form label-width="auto" :model="info" :rules="rules" ref="postForm">
      <el-form-item label="类型名称：" prop="adminName">
        <el-input v-model="info.adminName" placeholder="请输入类型名称" maxlength="30"></el-input>
      </el-form-item>

      <el-form-item label="描述：" prop="remark">
        <el-input maxlength="280" v-model="info.remark" type="textarea" placeholder></el-input>
      </el-form-item>
    </el-form>

    <div class="bottom" slot="footer">
      <el-button @click="close">取消</el-button>
      <el-button @click="confirm" type="primary"> 确定 </el-button>
    </div>
  </el-dialog>
</template>
<script>
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
    }
  },
  computed: {
    title() {
      return this.role === 'update' ? '编辑分组类型' : '新增分组类型';
    }
  },
  data() {
    var validateUserName = async (rule, value, callback) => {
      if (!value) {
        callback(new Error('分组名称不能为空'));
        return;
      }
      // if (this.role !== 'update') {
      //     const allMerchants = await this.$request('getAllMerchants')({
      //         adminName: value,
      //     })
      //     if (allMerchants.data.data) {
      //         callback(new Error('该分组名称已被使用，请改用其他账号名'))
      //     }
      // }
      callback();
    };
    return {
      name: 'accountPrivileges',
      loading: false,
      modifyOrCreateAccount: null,
      rules:
        this.role === 'update'
          ? {
              adminName: [{ required: true, validator: validateUserName, trigger: 'blur' }],
              remark: [{ max: 280, message: '描述内容最多280个字符', trigger: 'blur' }]
            }
          : {
              adminName: [{ required: true, validator: validateUserName, trigger: 'blur' }],
              remark: [{ max: 280, message: '描述内容最多280个字符', trigger: 'blur' }]
            },
      inputPassword: false
    };
  },

  methods: {
    close() {
      this.$emit('change-visible', false);
    },

    confirm() {
      const me = this;
      me.$emit('change-row', { ...me.info });
      this.$refs.postForm.validate(valid => {
        if (valid) {
          if (me.info.verifyType == 2 && !me.info.googleKey) {
            me.$emit('change-visible', false);
            this.$emit('openGADialog');
          } else {
            if (me.info.id) {
              me.$emit('update-action');
            } else {
              me.$refs.postForm.validate(valid => {
                if (valid) {
                  me.$emit('create-action');
                } else {
                  console.log('error submit!!');
                  return false;
                }
              });
            }
          }
        } else {
          console.warn('error submit!!');
          return false;
        }
      });
    }
  }
};
</script>
<style lang="scss"></style>
