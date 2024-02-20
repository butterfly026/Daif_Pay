<template>
  <!-- 弹窗 -->
  <el-dialog
    :title="title"
    :close-on-click-modal="false"
    class="permission_dialog"
    v-loading="loading"
    :element-loading-text="$t('routes.system.request_loading')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.1)"
    :visible="visible"
    width="38%"
    @close="close"
  >
    <el-form label-width="auto" :model="info" :rules="rules" ref="postForm">
      <el-form-item :label="$t('routes.system.permission_name')" prop="name">
        <el-input v-model="info.name" :placeholder="$t('routes.system.input_premison_phaceholder')" maxlength="50"></el-input>
      </el-form-item>
      <el-form-item :label="$t('routes.system.auth_route')" prop="module">
        <el-input v-model="info.module" :placeholder="$t('routes.system.input_auth_route_placeholder')" maxlength="100" :disabled="type == 'update' && info.menu == 1"></el-input>
      </el-form-item>
      <el-form-item :label="$t('routes.system.auth_group')" prop="types">
        <el-select :placeholder="$t('routes.system.auth_group')" v-model="info.types" :disabled="type == 'update' && info.menu == 1">
          <el-option :value="t.key" :label="t.value" v-for="(t, index) in typeList" :key="t.key"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('routes.system.menu_status')+':'" prop="menu">
        <el-select :placeholder="$t('routes.system.menu_status')" v-model="info.menu" disabled>
          <el-option :value="1" :label="$t('routes.system.menu')"></el-option>
          <el-option :value="0" :label="$t('routes.system.no_menu')"></el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <div class="bottom" slot="footer">
      <el-button @click="close">{{$t('global.cancel')}}</el-button>
      <el-button
        @click="confirm"
        class="loading-btn"
        v-loading="btnLoading"
        element-loading-spinner="el-icon-loading"
        :disabled="btnLoading || createDisable"
        type="primary"
      >
        {{$t('global.sure')}}
      </el-button>
    </div>
  </el-dialog>
</template>
<script>
import i18n from "@/locales";
export default {
  name: 'ModifyDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    info: {
      type: Object
    },
    type: {
      type: String,
      default: () => ''
    },
    createDisable: {
      type: Boolean,
      default: false
    },
    typeList: {
      type: Array
    }
  },
  computed: {
    title() {
      return this.type === 'update' ? i18n.global.t('routes.system.modify_auth') : i18n.global.t('routes.system.add_auth');
    }
  },
  data() {
    var validateName = async (rule, value, callback) => {
      const patt = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/;
      if (!value) {
        callback(new Error(i18n.global.t('routes.system.rules_tips1')));
        return;
      }
      if (!patt.test(value)) {
        callback(new Error(i18n.global.t('routes.system.rules_tips2')));
        return;
      }
      if (value.length > 50) {
        callback(new Error(i18n.global.t('routes.system.rules_tips3')));
        return;
      }
      callback();
    };
    var validateRoute = async (rule, value, callback) => {
      const patt = /^[a-zA-Z\/]+$/;
      if (!value) {
        callback(new Error(i18n.global.t('routes.system.rules_tips4')));
        return;
      }
      if (!patt.test(value)) {
        callback(new Error(i18n.global.t('routes.system.rules_tips6')));
        return;
      }
      if (value.length > 100) {
        callback(new Error(i18n.global.t('routes.system.rules_tips5')));
        return;
      }
      callback();
    };
    return {
      loading: false,
      btnLoading: false,
      rules: {
        name: [
          {
            required: true,
            validator: validateName,
            trigger: 'blur'
          }
        ],
        module: [
          {
            required: true,
            validator: validateRoute,
            trigger: 'blur'
          }
        ],
        types: [
          {
            required: true,
            message: i18n.global.t('routes.system.msg_tips1'),
            trigger: 'change'
          }
        ],
        menu: [
          {
            required: true,
            message: i18n.global.t('routes.system.msg_tips2'),
            trigger: 'blur'
          }
        ]
      },
      typesList: [
        {
          key: 'priv',
          value: i18n.global.t('routes.system.auth_model')
        },
        {
          key: 'user',
          value: i18n.global.t('routes.system.user')
        },
        {
          key: 'group',
          value: i18n.global.t('routes.system.user_group')
        }
      ]
    };
  },

  methods: {
    close() {
      this.$emit('change-visible', false);
    },

    confirm() {
      const me = this;
      this.$refs.postForm.validate(valid => {
        if (valid) {
          //   this.createDisable=true;
          this.btnLoading = true;
          if (me.type == 'update') {
            me.$emit('update-action', { ...this.info }, () => {
              this.btnLoading = false;
            });
          } else {
            me.$refs.postForm.validate(valid => {
              if (valid) {
                me.$emit('create-action', { ...this.info }, () => {
                  this.btnLoading = false;
                });
              } else {
                console.log('error submit!!');
                return false;
              }
            });
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
