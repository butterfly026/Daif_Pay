<template>
  <div class="ym-layout-default">
    <div class="lefts" :class="{ collapse: isCollapseClass ? true : false }">
      <div class="logo">代理后台</div>
      <div class="menu">
        <el-scrollbar>
          <Sidebar />
        </el-scrollbar>
      </div>
    </div>
    <div class="rights">
      <div class="contentlayout">
        <div class="multipleheader">
          <!-- <MultipleHeader /> -->
          <div class="flex">
            <div class="mult_left">
              <el-tag
                v-for="tag in toolList"
                class="eltag"
                @click="$router.push(tag.url)"
                :key="tag"
                :class="{ active: $route.path == tag.url }"
                :closable="toolList.length > 1 && $route.path != tag.url"
                :disable-transitions="false"
                @close="handleClose(tag)"
              >
                {{ tag.name }}
              </el-tag>
            </div>
            <div class="mult_right">
              <el-dropdown>
                <span class="el-dropdown-link"> {{ username }} ⇩ </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="setNewPwd">修改密码</el-dropdown-item>
                    <el-dropdown-item @click="logoutHanlder">退出</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        <div class="router-view">
          <div class="routerscroll">
            <el-scrollbar>
              <div class="pad">
                <div class="centermain">
                  <router-view v-slot:default="{ Component, route }">
                    <keep-alive>
                      <component :is="Component" :key="route.fullPath" />
                    </keep-alive>
                  </router-view>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </div>
      </div>
    </div>
  </div>
  <el-dialog v-model="showDialog" title="修改密码" center width="25%">
    <el-form ref="formInlineRef" :model="formInline" class="demo-form-inline" label-width="auto" :rules="rules">
      <el-form-item label="原密码" prop="old">
        <el-input size="small" v-model.trim="formInline.old" autocomplete="off" type="password" placeholder="请输入原密码"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="new" required>
        <el-input size="small" v-model.trim="formInline.new" autocomplete="off" type="password" placeholder="请输入新密码"></el-input>
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
</template>

<script>
import { defineComponent, computed, reactive, ref } from 'vue';
import Sidebar from './component/Sidebar.vue';
import { ElScrollbar, ElMessage } from 'element-plus';
import store from '../../../store/index';
import { logout, resetPWD } from '../../../http/apis/login';
import Event from '../../../event';
import { useRouter } from 'vue-router';
import { adminPassword } from '../../../utils/expressions';

export default defineComponent({
  name: 'YmLayoutDefault',
  components: {
    Sidebar,
    [ElScrollbar.name]: ElScrollbar
  },
  setup() {
    const formInline = reactive({
      old: '',
      new: '',
      refPwd: ''
    });

    const rules = {
      old: [
        {
          required: true,
          message: '请输入原密码',
          trigger: 'blur'
        }
      ],
      new: [
        {
          required: true,
          message: '请输入新密码',
          trigger: 'blur'
        },
        {
          min: 8,
          message: '密码长度不能少于8位',
          trigger: 'blur'
        },
        { pattern: adminPassword, message: '密码格式为数字和字母的组合', trigger: 'blur' }
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
        },
        { pattern: adminPassword, message: '密码格式为数字和字母的组合', trigger: 'blur' }
      ]
    };

    const { push } = useRouter();
    const logoutHanlder = () => {
      logout().then(res => {
        if (res.status) {
          ElMessage.success('退出成功');
          store.commit('updateUserInfo', {});
          store.commit('setBankInfo', {});
          Event.emit('login', { login: false });
          push('/login');
        } else {
          ElMessage.error('退出失败');
        }
      });
    };

    const handleClose = tag => {
      store.commit('removeToolList', tag.url);
    };

    const showDialog = ref(false);
    const formInlineRef = ref(null);

    // 修改密码弹窗
    const setNewPwd = () => {
      showDialog.value = true;
      formInlineRef.value && formInlineRef.value.resetFields();
    };

    // 取消修改密码
    const onCancel = () => {
      formInlineRef.value.resetFields();
      showDialog.value = false;
    };

    // 提交修改密码
    const onSubmit = () => {
      formInlineRef.value.validate(valid => {
        if (valid) {
          if (formInline.new !== formInline.refPwd) {
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
              setTimeout(() => {
                logoutHanlder();
              }, 2000);
            }
          });
        } else {
          return false;
        }
      });
    };

    return {
      rules,
      showDialog,
      setNewPwd,
      formInline,
      formInlineRef,
      onCancel,
      onSubmit,
      isCollapseClass: false,
      logoutHanlder,
      username: computed(() => store.state.userInfo.username || 'NoName'),
      toolList: computed(() => store.state.toolList),
      handleClose
    };
  }
});
</script>

<style lang="less" scoped>
@logoHeight: 48px; // logo的高度
@menuWidth: 180px; // Layout左侧Menu的宽度

.ym-layout-default {
  display: flex;
  height: 100%;

  .mult_left {
    flex: 1;
    height: 43px;
    display: flex;
    align-items: center;
    width: 0;
    padding-left: 10px;
  }

  .eltag {
    cursor: pointer;
    margin-right: 10px;
    background-color: #fff;
    color: #909399;
    &.active {
      background-color: #0960bd;
      cursor: default;
      color: #fff;
      border-color: #0960bd;
    }
  }

  .mult_right {
    padding: 12px;
    cursor: pointer;
  }

  > .lefts,
  .rights {
    height: 100%;
  }
  > .lefts {
    width: @menuWidth;
    background-color: var(--theme-bg-color);
    transition: all 0.2s;
    &.collapse {
      width: 64px !important;
    }
    > .logo {
      height: @logoHeight;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #0c2e4e;
      color: #fff;
      border-bottom: 1px solid #051728;
    }
    > .menu {
      height: calc(100% - @logoHeight);
      background-color: rgb(0, 21, 41);
    }
  }
  > .rights {
    width: 0;
    flex: 1;
    > .contentlayout {
      display: flex;
      flex-direction: column;
      height: 100%;
      > .multipleheader {
        height: 44px;
        border-bottom: 1px solid #d8dce5;
        box-shadow: 0 1px 3px 0 rgb(0 0 0 / 12%), 0 0 3px 0 rgb(0 0 0 / 4%);
      }
      > .router-view {
        flex: 1 1 auto;
        height: 0;
        .routerscroll {
          width: 100%;
          height: 100%;
          .pad {
            padding: 10px;
            .centermain {
              background-color: #fff;
            }
          }
        }
      }
    }
  }
}
</style>
