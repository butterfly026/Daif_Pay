<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button @click="createAction" style="float: right" type="primary">{{ t('routes.system.add_group') }}</el-button>
      <el-button @click="goBack" style="float: right; margin-right: 10px">{{ t('routes.system.back') }}</el-button>
    </div>
    <div class="search-info">
      <i class="el-icon-warning"></i>
      {{ $route.query.typeName }}{{ t('routes.system.group_manage') }}
    </div>
    <el-table border :data="list">
      <el-table-column header-align="center" align="center" type="index" :label="t('routes.system.sort_number')" width="60"></el-table-column>
      <el-table-column header-align="center" align="center" prop="types" min-width="80" :show-overflow-tooltip="true" :label="t('routes.system.group_type')">
        <template>{{ $route.query.typeName }}</template>
      </el-table-column>
      <el-table-column header-align="center" align="center" prop="gname" min-width="80" :show-overflow-tooltip="true" :label="t('routes.system.group_name')">
        <template v-slot="{ row }">
          <el-link type="primary" class="link-action" @click="goGroups(row)">
            {{ row.gname }}
          </el-link>
        </template>
      </el-table-column>

      <el-table-column header-align="center" align="center" prop="create_at" min-width="100" :label="t('routes.system.creat_time')">
        <template v-slot="{ row }">
          <span>{{ row.create_at | timeFilter }}</span>
        </template>
      </el-table-column>
      <el-table-column
        header-align="center"
        align="center"
        :show-overflow-tooltip="true"
        prop="create_by_name"
        :label="t('routes.system.create_people')"
      ></el-table-column>
      <el-table-column
        header-align="center"
        align="center"
        prop="total"
        width="60"
        :show-overflow-tooltip="true"
        :label="t('routes.system.people_acount')"
      ></el-table-column>

      <el-table-column
        header-align="center"
        min-width="80"
        :show-overflow-tooltip="true"
        prop="noted"
        :label="t('routes.system.people_acount')"
      ></el-table-column>
      <el-table-column header-align="center" align="center" prop="state" min-width="130" :label="t('routes.system.start_status')">
        <template v-slot="{ row }" v-if="row.total === 0">
          <el-switch
            class="loading-btn"
            v-loading="switchLoading"
            :disabled="switchLoading"
            element-loading-spinner="el-icon-loading"
            :value="row.state == 1"
            active-color="#13ce66"
            inactive-color="#c7c6c6"
            @change="updateItem(row)"
          >
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column header-align="center" align="center" min-width="160" :label="t('golbal.operating')">
        <template v-slot="scope">
          <el-button type="primary" size="small" @click="updateAction(scope.row)">{{ t('golbal.edit') }}</el-button>
          <el-button type="primary" size="small" v-if="scope.row.total == 0" @click="handleDelete(scope.row)">{{ t('golbal.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <MDialog
      v-if="showDialog"
      :type="type"
      :info="modifyOrCreateAccount"
      @create-action="createRequest"
      @update-action="updateRequest"
      @change-visible="showDialog = $event"
      :permissionList="permissionList"
      :visible="showDialog"
      :createDisable="createDisable"
      @changeCreateDisable="createDisable = true"
    ></MDialog>
  </div>
</template>

<script>
import MDialog from './modifyDialog';
import permission from './permission';
import { useI18n } from 'vue-i18n';
import { getGroupList, groupInsert, groupUpdate, groupUpdateState, groupDelete, getPrivList } from '@/api/permission';
export default {
  name: 'UserGroup',

  data() {
    return {
      list: [],
      total: 0,
      filter: {
        pageNum: 1,
        pageSize: 30
      },
      showDialog: false,
      modifyOrCreateAccount: {},
      type: '',
      permissionList: [],
      createDisable: false,
      switchLoading: false
    };
  },

  created() {
    this.getList();
    this.getPermissionList();
  },
  unmounted() {
    permission.map(el => {
      el.children.map(e => {
        e.children = [];
      });
    });
  },
  methods: {
    onSearch() {
      this.filter.pageNum = 1;
      this.getList();
    },
    //获取所有权限列表
    getPermissionList() {
      getPrivList({}).then(res => {
        // 先赋值二级目录
        // 本地权限配置遍历
        permission.map(manage => {
          manage.children = [];
          manage.label = manage.name;
          res.data.map(ele => {
            // 匹配types属于当前模块
            if (ele.menu && manage.pathAll.includes(ele.types)) {
              manage.children.push({
                path: ele.types,
                label: ele.name,
                id: ele.id,
                children: []
              });
            }
          });
          // 赋值操作菜单
          manage.children.map(menu => {
            menu.children = [];
            res.data.map(ele => {
              // 匹配types属于当前模块
              if (!ele.menu && menu.path == ele.types) {
                menu.children.push({
                  label: ele.name,
                  id: ele.id
                });
              }
            });
          });
        });

        this.permissionList = permission;
        // console.log(res.data,'permission');
      });
    },
    //分组管理列表
    async getList() {
      // if (!this.filter.time) this.filter.time = [];
      // this.filter.startTime = trueDate(this.filter.time[0])
      // this.filter.endTime = trueDate(this.filter.time[1])
      const res = await getGroupList({
        types: this.$route.query.type || 1
      });
      this.list = res.data;
    },
    // 删除分组管理
    handleDelete(row) {
      this.$confirm('删除后，该分组类型将不可用，是否继续？', '删除分组', {
        confirmButtonText: t('golbal.sure'),
        cancelButtonText: t('golbal.cancel'),
        type: 'warning'
      })
        .then(() => {
          groupDelete({ id: row.gid }).then(res => {
            if (res.status == 'true') {
              this.getList();
              this.$notify({
                title: t('golbal.success'),
                message: res.data || t('golbal.delete_suc'),
                type: 'success',
                duration: 2000
              });
            }
          });
        })
        .catch(err => {
          console.error(err);
        });
    },
    //编辑分组
    updateAction(row) {
      this.type = 'update';
      let item = { ...row };
      this.modifyOrCreateAccount = item;
      this.createDisable = false;
      this.showDialog = true;
    },
    //启用禁用
    async updateItem(row) {
      this.switchLoading = true;
      await groupUpdateState({
        id: row.gid,
        state: row.state == 1 ? 0 : 1
      })
        .then(res => {
          if (res.status == 'true') {
            this.getList();
            this.$message.success('修改成功');
            this.showDialog = false;
          }
        })
        .catch(e => {
          this.switchLoading = false;
        });
      this.switchLoading = false;
    },

    async updateRequest(flag, callback) {
      const modifyOrCreateAccount = { ...this.modifyOrCreateAccount };
      modifyOrCreateAccount.noted = modifyOrCreateAccount.noted.replace(/\r|\r\n|\n/g, ' ');

      delete modifyOrCreateAccount.total;
      delete modifyOrCreateAccount.create_at;
      delete modifyOrCreateAccount.create_by_name;
      delete modifyOrCreateAccount.create_by_uid;

      const res = await groupUpdate(modifyOrCreateAccount);
      this.createDisable = false;
      if (res.status == 'true') {
        this.getList();
        this.$message.success('编辑成功');
        this.showDialog = false;
      }
      callback();
    },

    async createAction() {
      this.type = 'create';
      this.modifyOrCreateAccount = {
        gname: undefined,
        types: this.$route.query.type || 1,
        noted: undefined,
        permission: undefined,
        state: ''
      };
      this.showDialog = true;
    },

    //新增完成
    async createRequest(flag, callback) {
      const modifyOrCreateAccount = { ...flag };
      const res = await groupInsert(modifyOrCreateAccount);
      this.createDisable = false;
      if (res.status == 'true') {
        this.getList();
        this.$message.success('新增成功');
        this.showDialog = false;
      }
      callback();
    },
    //返回上一页
    goBack() {
      this.$store.dispatch('tagsView/delView', this.$route);
      this.$router.replace({
        name: 'packetType'
      });
    },
    goGroups(row) {
      this.$router.push({
        name: 'account',
        query: {
          group_type: row.types,
          group_id: row.gid
        }
      });
    }
  },

  components: {
    MDialog
  }
};
</script>

<style lang="scss" scoped>
.search-info {
  height: 30px;
  background: #e8f3fe;
  margin: 0 0 15px 0;

  color: #58aafc;
  font-size: 12px;
  line-height: 30px;
  width: 100%;
  border: 1px #0013ff1a solid;
  border-radius: 3px;
  > i {
    margin-left: 15px;
  }
}
</style>
