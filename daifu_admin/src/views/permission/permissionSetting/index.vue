<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" ref="form" label-width="auto" class="filter-panel">
        <!-- <el-form-item>
                    <el-select
                        v-model="listQuery.types"
                        placeholder="权限名称"
                        style="width: 200px"
                        class="filter-item"
                        clearable=""
                    >
                        <el-option
                            v-for="item in typeList"
                            :key="item.key"
                            :label="item.value"
                            :value="item.key"
                        />
                    </el-select>
                </el-form-item>
                <el-button @click="getList" type="primary"
                >搜索</el-button> -->
        <el-button @click="createAction" type="primary">{{ i18n.global.t('global.increment') }}</el-button>
      </el-form>
    </div>
    <el-table border :data="list" v-loading="listLoading" :span-method="objectSpanMethod">
      <template v-slot:empty>
        <p>{{ $t('routes.system.permission_name') }}</p>
      </template>
      <!-- <el-table-column
                header-align="center"
                align="center"
                type="index"
                prop="id"
                label="ID"
                width="60"
            ></el-table-column> -->
      <el-table-column header-align="center" align="center" prop="name" :show-overflow-tooltip="true"
                       :label="$t('routes.system.type')" min-width="160px"></el-table-column>

      <el-table-column header-align="center" align="center" prop="name" :show-overflow-tooltip="true"
                       :label="$t('routes.system.call_name')" min-width="160px"></el-table-column>

      <el-table-column header-align="center" align="center" prop="module" :label="$t('routes.system.route')"
                       min-width="160px"></el-table-column>
      <!-- <el-table-column
                header-align="center"
                align="center"
                prop="types"
                min-width="70"
                :show-overflow-tooltip="true"
                label="组别"
            ></el-table-column> -->

      <el-table-column header-align="center" align="center" :show-overflow-tooltip="true" prop="menu"
                       :label="$t('routes.system.menu_status')" min-width="150px">
        <template v-slot="scope">
          {{ scope.row.menu == 1 ? $t('routes.system.menu') : $t('routes.system.no_menu') }}
        </template>
      </el-table-column>
      <el-table-column header-align="center" align="center" :label="$t('global.operating')" min-width="160px">
        <template v-slot="scope">
          <el-button type="primary" size="small" @click="updateAction(scope.row)">
            {{ $t('routes.system.edit') }}
          </el-button>
          <el-button type="primary" size="small" @click="handleDelete(scope.row)" v-if="scope.row.menu != 1">
            {{ $t('routes.system.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <MDialog
        v-if="showDialog"
        :info="modifyOrCreateAccount"
        @change-visible="showDialog = $event"
        @create-action="createRequest"
        :createDisable="createDisable"
        :typeList="typeList"
        @update-action="updateRequest"
        :visible="showDialog"
        :type="type"
    ></MDialog>
  </div>
</template>

<script>
//
import MDialog from './modifyDialog';
import {getPrivList, privInsert, privUpdate, privDelete, privType} from '@/api/permission';
import i18n from "@/locales";

export default {
  name: 'permissionSetting',
  data() {
    return {
      googleKey: '',
      list: [],
      total: 0,
      showDialog: false,
      modifyOrCreateAccount: {},
      type: '',
      createDisable: false,
      typeList: [],
      listQuery: {
        types: '' // 组别筛选
      },
      listLoading: true,
      privTypeMap: []
    };
  },

  created() {
    this.privTypeList();
  },

  mounted() {
  },

  computed: {},

  methods: {
    //权限组别
    async privTypeList() {
      const res = await privType({});
      this.privTypeMap = res.data;
      let list = [];
      for (var i in res.data) {
        const item = {
          key: i,
          value: res.data[i]
        };
        list.push(item);
      }
      this.typeList = list;
      this.getList();
    },
    async getList() {
      this.listLoading = true;
      const res = await getPrivList(this.listQuery);
      // 存储操作的数据
      let privList_new = [];
      // 遍历type对象
      for (let key in this.privTypeMap) {
        // 上一次存储数组的长度
        let length_old = privList_new.length;

        // 遍历返回的数据，将对应key的加入到存储数组
        res.data.forEach(item => {
          if (item.types && item.types == key) {
            privList_new.push(item);
          }
        });

        // 下一次需要合并的第一项
        let nextRowSpan = privList_new[length_old];

        if (nextRowSpan) {
          //添加标示，标示哪一行要进行rowspan
          nextRowSpan.rowspanMark = true;
          //要进行rowspan的行数
          nextRowSpan.rowspanCount = privList_new.length - length_old;
          //在这一行添加类型名称
          nextRowSpan.privTypeName = this.privTypeMap[key];
        }
      }
      this.list = privList_new;

      setTimeout(() => {
        this.listLoading = false;
      }, 200);
    },
    // 删除权限分配
    handleDelete(row) {
      this.$confirm(i18n.global.t('global.operating'), i18n.global.t('routes.system.del_group'), {
        confirmButtonText: i18n.global.t('global.sure'),
        cancelButtonText: i18n.global.t('global.cancel'),
        type: 'warning'
      }).then(() => {
        privDelete({id: row.id}).then(res => {
          if (res.status == 'true') {
            this.getList();
            this.$notify({
              title: i18n.global.t('global.success'),
              message: res.data || i18n.global.t('global.delete_suc'),
              type: 'success',
              duration: 2000
            });
          }
        });
      }).catch(err => {
        console.error(err);
      });
    },
    //编辑权限分配
    updateAction(row) {
      this.type = 'update';
      let item = {...row};
      this.modifyOrCreateAccount = item;
      this.createDisable = false;
      this.showDialog = true;
    },

    //编辑权限分配
    async updateRequest(flag, callback) {
      const modifyOrCreateAccount = {...this.modifyOrCreateAccount};
      const res = await privUpdate(modifyOrCreateAccount);
      this.createDisable = false;
      if (res.status == 'true') {
        this.getList();
        this.$message.success(i18n.global.t('global.editsuc'));
        this.showDialog = false;
      }
      callback();
    },
    //新增权限分配
    async createAction() {
      this.type = 'create';
      this.modifyOrCreateAccount = {
        module: undefined,
        name: undefined,
        types: undefined,
        menu: 0
      };
      this.createDisable = false;
      this.showDialog = true;
    },

    //新增权限分配
    async createRequest(flag, callback) {
      const res = await privInsert({
        ...this.modifyOrCreateAccount
      });
      this.createDisable = false;
      if (res.status == 'true') {
        this.getList();
        this.$message.success(i18n.global.t('global.add_suc'));
        this.showDialog = false;
      }
      callback();
    },

    // 合并
    objectSpanMethod({row, column, rowIndex, columnIndex}) {
      if (columnIndex == 0) {
        if (row.rowspanMark) {
          return {
            rowspan: row.rowspanCount,
            colspan: 1
          };
        }
        return {
          rowspan: 0,
          colspan: 0
        };
      } else {
        return {
          rowspan: 1,
          colspan: 1
        };
      }
    }
  },
  components: {
    //
    MDialog
  }
};
</script>

<style lang="scss" scoped>
.bottom {
  text-align: right;
}

.clipboard {
  margin-left: 5px;
}

.filter {
  margin-bottom: 5px;

  .el-form-item--mini.el-form-item,
  .el-form-item--small.el-form-item {
    margin-bottom: 0px !important;
  }
}
</style>
