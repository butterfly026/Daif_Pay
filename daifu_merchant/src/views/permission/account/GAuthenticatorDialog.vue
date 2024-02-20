<template>
  <el-dialog
    :title="$t('routes.system.bind_goole')"
    v-loading="loading"
    :element-loading-text="$t('routes.system.request_loading')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.1)"
    :visible="visible"
    :close-on-click-modal="false"
    width="850px"
    @close="close"
  >
    <div class="gbox">
      <div class="gbox_main">
        <div class="gbox_code">
          <div class="gbox_code-left">
            <img width="180" height="180" v-bind:src="'data:image/jpg;base64,' + pathimg" alt="" />
            <p style="font-size: 8px">
              <span id="JS-clipboard-code" style="font-weight: bold">{{ googleKey }}</span>
              <a class="clipboard" data-clipboard-target="#JS-clipboard-code" @click="copy"> {{$t('routes.system.copy')}} </a>
            </p>
          </div>
          <div class="gbox_code-right">
            <p class="gbox_code-tip">{{$t('routes.system.maring_tips')}}</p>
            <ul class="tipsetup">
              <li>1.{{$t('routes.system.ercode_tip1')}}</li>
              <li>2.{{$t('routes.system.ercode_tip2')}}</li>
              <li>
                <br />
                <el-button
                  type="primary"
                  @click="confirm"
                  v-loading="btnloading"
                  :disabled="btnloading"
                  class="loading-btn"
                  element-loading-spinner="el-icon-loading"
                  >{{$t('routes.system.update_keys')}}</el-button
                >
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <div slot="footer">
      <el-button type="primary" @click="close">{{$t('global.sure')}}</el-button>
    </div>
  </el-dialog>
</template>
<script>
import Clipboard from 'clipboard';
import i18n from "@/locales";
export default {
  name: 'GAuthenticatorDialog',

  props: {
    googleKey: {
      type: String,
      default: () => ''
    },
    role: {
      type: String,
      default: () => ''
    },
    pathimg: {
      type: String,
      default: () => ''
    },
    info: {
      type: Object,
      default: () => {}
    },
    visible: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      name: 'accountPrivileges',
      loading: false,
      btnloading: false,
      postForm: {},
      rules: {
        code: [
          {
            required: true,
            message: i18n.global.t('routes.system.code_not_empty'),
            trigger: 'blur'
          }
          // { type: 'number', message: 'Google验证码必须是数字', trigger: 'blur' },
        ]
      }
    };
  },

  mounted() {
    // 初始化复制按钮
    new Clipboard('.clipboard');
  },

  methods: {
    copy() {
      this.$message.success(i18n.global().t('routes.system.keys_copysuc_tips'));
    },
    close() {
      this.$emit('change-visible', false);
    },
    confirm() {
      this.btnloading = true;
      this.$emit('updateVerifyType-action', () => {
        this.btnloading = false;
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.gbox_title {
  text-align: center;
}
.gbox_code {
  display: flex;

  &-tip {
    color: #d86c7c;
    font-weight: bold;
    margin: 0;
  }

  &-left {
    width: 300px;
  }

  &-right {
    margin-left: 20px;
    width: 330px;
  }
}

.tipsetup {
  list-style: none;
  padding-left: 0;
}

.gbox_main {
  width: 650px;
  margin: 20px auto 10px;
}
.clipboard {
  color: blue;
}
</style>
