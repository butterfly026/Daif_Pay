<template>
  <div class="ym-layout-default">
    <div class="lefts" :class="{ collapse: isCollapseClass ? true : false }">
      <div class="logo">商户后台</div>
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
                @click="$router.push(tag.url)"
                class="eltag"
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
</template>

<script>
import { defineComponent, computed } from 'vue';
import Sidebar from './component/Sidebar.vue';
import { ElScrollbar, ElMessage } from 'element-plus';
import store from '../../../store/index';
import { logout } from '../../../http/apis/login';
import Event from '../../../event';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'YmLayoutDefault',
  components: {
    Sidebar,
    [ElScrollbar.name]: ElScrollbar
  },
  setup() {
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

    return {
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
    margin-right: 10px;
    background-color: #fff;
    color: #909399;
    cursor: pointer;
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
