<template>
  <div class="app-container">
    <div class="filter-container">
      <el-table border style="width: 60%" :data="list">
        <el-table-column header-align="center" align="center" type="index" label="序号" width="120"></el-table-column>
        <el-table-column header-align="center" align="center" prop="value" label="分组类型">
          <template v-slot="scope">
            <router-link :to="`/permission/userGroup?type=${scope.row.key}&typeName=${scope.row.value}`">
                    <el-link type="primary">
                      {{scope.row.value}}
                    </el-link>
            </router-link>
          </template>

        </el-table-column>
        <!-- <el-table-column header-align="center" align="center" prop="key" label="用户组数量"></el-table-column>
        <el-table-column header-align="center" align="center" prop="key" label="人数"></el-table-column> -->
      </el-table>
      <!-- <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="filter.page"
        :limit.sync="filter.pagesize"
        @pagination="getList"
      /> -->
      <MDialog
        v-if="showDialog"
        :info="modifyOrCreateAccount"
        @change-visible="showDialog = $event"
        ref="mDialog"
        :visible="showDialog"
      ></MDialog>
    </div>
  </div>
</template>

<script>

import MDialog from "./modifyDialog";
import permissionApi from "@/api/permission";
import {mapState} from "vuex";
export default {
  name: "packetType",
  data() {
    return {
      total: 0,
      showDialog: false,
      selectedRole: null,
      modifyOrCreateAccount: {},
      role: "",
      pathimg: "",
      roles: [],
    };
  },

  created() {
    this.getList();
  },

  mounted() {},

  computed: {
    ...mapState({
        list: state => state.permission.typeList,
    }),
  },

  methods: {
    onSearch() {
      // this.filter.pageNum = 1;
      this.getList();
    },

    //查看分组类型列表
    async getList() {},

    //编辑分组类型
    updateAction(row) {
      this.role = "update";
      let item = { ...row, verifyType: 1 }; // 修改时不用更新密钥
      item.password = "";
      this.googleKey = item.googleKey;
      this.modifyOrCreateAccount = item;
      this.showDialog = true;
    },
    //新建分组
    async createAction() {
      this.role = "create";
      this.modifyOrCreateAccount = {
        adminName: undefined,
        password: undefined,
        remark: undefined
      };
      this.showDialog = true;
    },
    //创建成功
    async createRequest(flag) {
      const res = await permissionApi.userCreate({
        // loadingComponentName: flag ? 'gAuthenticatorDialog' : 'mDialog',
        ...this.modifyOrCreateAccount
      });
      if (res.data.success) {
        this.getList();
        this.showDialog = false;
        this.role = "";
        this.googleKey = "";
      }
    },
    // 删除分组类型
    handleDelete(row) {
      this.$confirm("删除后，该分组类型将不可用，是否继续？", "删除分组", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          permissionApi.delUser({ id: row.ID }).then(res => {
            if (res.status == "true") {
              // const index = this.list.indexOf(row)
              // this.list.splice(index, 1)
              this.getList();
              this.$notify({
                title: "成功",
                message: res.data || "删除成功",
                type: "success",
                duration: 2000
              });
            }
          });
        })
        .catch(err => {
          console.error(err);
        });
    }
  },

  components: {

    MDialog
  }
};
</script>

<style lang="scss" scoped>
</style>
