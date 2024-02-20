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
    width="900px"
    @close="close"
  >
    <el-form label-width="auto" :model="info" :rules="rules" ref="postForm">
      <el-form-item label="分组类型：">
        {{ $route.query.typeName }}
      </el-form-item>
      <el-form-item label="分组名称：" prop="gname">
        <el-input v-model="info.gname" placeholder="请输入分组名称" maxlength="16"></el-input>
      </el-form-item>
      <el-form-item label="启用状态：" prop="state">
        <el-select placeholder="启用状态" v-model="info.state">
          <el-option :value="1" :label="$t('routes.system.enable')"></el-option>
          <el-option :value="0" :label="$t('routes.system.close_use')"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="权限分配：" prop="permission">
        <el-tree
          class="dj_tree"
          ref="tree"
          node-key="id"
          show-checkbox
          default-expand-all
          highlight-current
          check-on-click-node
          :data="treeList"
          :expand-on-click-node="false"
          :default-checked-keys="defaultCheckedKeys"
        >
        </el-tree>
      </el-form-item>
      <el-form-item label="描述：" prop="noted">
        <el-input v-model="info.noted" maxlength="280" type="textarea" placeholder></el-input>
      </el-form-item>
    </el-form>

    <div class="bottom" slot="footer">
      <el-button @click="close">{{$t('global.cancel')}}</el-button>
      <el-button
        class="loading-btn"
        v-loading="btnLoading"
        element-loading-spinner="el-icon-loading"
        @click="confirm"
        :disabled="btnLoading || createDisable"
        type="primary"
      >

        {{$t('global.sure')}}
      </el-button>
    </div>
  </el-dialog>
</template>
<script>
import { unique5 } from '@/utils';

export default {
  name: 'ModifyDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    // types: {
    //   type: Number,
    //   default: () => 1
    // },
    permissionList: {
      type: Array,
      default: () => []
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
    }
  },
  created() {
    this.treeList = this.permissionList || [];
    let permission = this.info.permission ? this.info.permission.split(',') : [];
    let ids = [];
    this.treeList.map((ele, i) => {
      ele.id = 'root' + i;
      ele.children.map(el => {
        el.children.map(e => {
          permission.includes(String(e.id)) && ids.push(e.id);
        });
      });
    });

    this.defaultCheckedKeys = ids;
  },
  computed: {
    title() {
      return this.type === 'update' ? '修改分组' : '新增分组';
    }
  },
  data() {
    return {
      loading: false,
      modifyOrCreateAccount: null,
      btnLoading: false,
      rules: {
        gname: [{ required: true, message: '分组名称不能为空', trigger: 'blur' }],
        // types: [
        //   { required: true, message: "请先选择分组类型", trigger: "blur" }
        // ],
        state: [{ required: true, message: '请先选择启用状态', trigger: 'blur' }],
        noted: [
          {
            max: 280,
            message: '描述内容最多280个字符',
            trigger: 'blur'
          }
        ]
      },
      treeList: [],
      defaultCheckedKeys: []
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
          const selectedIds = this.$refs.tree.getCheckedNodes(false, true).map(a => a.id);
          let ids = selectedIds.filter(id => !String(id).includes('root') && id);
          ids = unique5(ids);
          if (ids.length < 1) {
            this.$message.error('至少选择一个权限');
            return;
          }
          me.info.permission = ids.join(',');
          me.$emit('changeCreateDisable', true);
          this.btnLoading = true;
          if (me.type == 'update') {
            me.$emit('update-action', { ...me.info }, () => {
              this.btnLoading = false;
            });
          } else {
            me.$refs.postForm.validate(valid => {
              if (valid) {
                // me.info.types=me.types;
                me.$emit('create-action', { ...me.info }, () => {
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
<style lang="scss">
.dj_tree {
  height: 220px;
  overflow: hidden;
  overflow-y: auto;
  .el-tree-node__children .el-tree-node__children {
    display: flex;
    flex-wrap: wrap;
  }
  .is-checked + .el-tree-node__label {
    color: #409eff;
  }
}
</style>
