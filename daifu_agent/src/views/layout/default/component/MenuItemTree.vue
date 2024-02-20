<template>
  <template v-for="(item, i) in menuList" :key="i">
    <el-submenu
      v-if="item.children && !item?.meta?.noChild"
      :index="resolvePath(item.path)"
    >
      <template #title>
        <i :class="item?.meta?.icon ? item?.meta?.icon : ''"></i>
        <span>{{item?.meta?.title}}</span>
      </template>
      <menu-item-tree
        :menu-list="item.children"
        :base-path="resolvePath(item.path)"
      ></menu-item-tree>
    </el-submenu>
    <el-menu-item v-else :index="resolvePath(item.path)">
      <i :class="item?.meta?.icon ? item.meta?.icon : ''"></i>
      <template #title>
        <span>{{item?.meta?.title}}</span>
      </template>
    </el-menu-item>
  </template>
</template>

<script>
import { defineComponent } from 'vue';
import { _resolve, isExternal } from '@/utils/common';
import {ElSubmenu, ElMenuItem} from 'element-plus';

export default defineComponent({
  name: 'MenuItemTree',
  components: {
    [ElSubmenu.name]: ElSubmenu,
    [ElMenuItem.name]: ElMenuItem,
  },
  props: {
    menuList: {
      type: Object
    },
    basePath: {
      type: String,
      default: ''
    }
  },
  setup({ basePath, menuList }) {
    const resolvePath = (rotePath) => {
      if (isExternal(rotePath)) {
        return rotePath;
      }
      if (isExternal(basePath)) {
        return basePath;
      }
      return _resolve(basePath, rotePath);
    };


    function t(msg) {
      return msg;
    }
    return {
      resolvePath,
      t
    };
  }
});
</script>