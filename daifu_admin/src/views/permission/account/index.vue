<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="filter" ref="filter" class="filter">
        <el-form-item>
          <el-input placeholder="账号名称" v-model="filter.username" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input placeholder="请输入创建人" style="width: 150px" v-model="filter.createname"></el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="filter.timeType" style="width: 140px" @change="timeTypeChange" placeholder="请选择">
            <el-option v-for="(item, index) in timeTypeList" :key="index" :label="item.value" :value="item.key"></el-option>
          </el-select>

          <el-date-picker
            v-model="filter.time"
            type="daterange"
            range-separator="至"
            :default-time="['00:00:00', '23:59:59']"
            start-placeholder="开始日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            end-placeholder="结束日期"
            :clearable="true"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-select placeholder="类型" style="width: 120px" v-model="filter.group_type" @change="changeGroupList">
            <el-option label="全部类型" value=""></el-option>
            <el-option v-for="(item, index) in typeList" :key="index" :label="item.value" :value="item.key"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select placeholder="分组" :disabled="!filter.group_type" v-model="filter.group_id" style="width: 120px">
            <el-option label="全部分组" value=""></el-option>
            <el-option v-for="(item, index) in groupList" :key="index" :label="item.gname" :value="item.gid"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select placeholder="状态" v-model="filter.state" style="width: 120px">
            <el-option :value="2" label="全部状态"></el-option>
            <el-option :value="1" label="启用"></el-option>
            <el-option :value="0" label="停用"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            v-loading="listLoading"
            :disabled="listLoading"
            class="loading-btn"
            element-loading-spinner="el-icon-loading"
            @click="onSearch"
          >
            查询
          </el-button>
          <el-button @click="createAction">新建账号</el-button>
        </el-form-item>
      </el-form>

      <el-table border fit v-loading="listLoading" :data="list">
        <template v-slot:empty>
          <p>找不到任何与检索数据匹配的数据，请重试！</p>
        </template>
        <el-table-column header-align="center" align="center" type="index" label="序号" width="60"></el-table-column>

        <el-table-column header-align="center" align="center" prop="username" min-width="80" :show-overflow-tooltip="true" label="账号名称"></el-table-column>

        <el-table-column header-align="center" align="center" prop="group_id" min-width="80" :show-overflow-tooltip="true" label="分组类型">
          <template v-slot="{ row }">
            <span>{{ !!row.group_type ? typeList[row.group_type - 1].value : '--/--' }}</span>
          </template>
        </el-table-column>
        <el-table-column header-align="center" align="center" prop="group_type_name" min-width="80" :show-overflow-tooltip="true" label="用户分组">
        </el-table-column>
        <el-table-column header-align="center" align="center" prop="create_time" min-width="150" :show-overflow-tooltip="true" label="创建时间">
          <template v-slot="{ row }">
            <span>{{ row.create_time | timeFilter }}</span>
          </template>
        </el-table-column>
        <el-table-column header-align="center" align="center" :show-overflow-tooltip="true" prop="create_by_name" label="创建人">
          <template v-slot="scope">{{ !!scope.row.create_by_name ? scope.row.create_by_name : '--/--' }}</template>
        </el-table-column>
        <el-table-column header-align="center" min-width="150" align="center" prop="LastLoginTime" label="最近登录时间">
          <!--  -->
          <template v-slot="{ row }">
            <span v-if="row.LastLoginTime">{{ row.LastLoginTime | timeFilter }}</span>
            <span v-else>--/--</span>
          </template>
        </el-table-column>
        <el-table-column header-align="center" align="center" min-width="100" prop="LastLoginIP" label="最近登录IP">
          <template v-slot="scope">
            <span>{{ !!scope.row.LastLoginIP ? long2ip(scope.row.LastLoginIP) : '--/--' }}</span>
          </template>
        </el-table-column>

        <el-table-column header-align="center" align="center" prop="state" min-width="150" label="启用状态">
          <template v-slot="scope">
            <el-switch
              class="loading-btn"
              v-loading="switchLoading"
              :disabled="switchLoading || (scope.row.group_type == 5 && scope.row.group_id == 1)"
              element-loading-spinner="el-icon-loading"
              :value="scope.row.state == 1"
              active-color="#13ce66"
              inactive-color="#c7c6c6"
              @change="updateItem(scope.row)"
            >
            </el-switch>
          </template>
        </el-table-column>

        <el-table-column header-align="center" min-width="160" align="center" label="操作" class-name="small-padding fixed-width">
          <template v-slot="scope">
            <el-button type="primary" size="small" @click="updateAction(scope.row)" :disabled="scope.row.group_type == 5 && scope.row.group_id == 1"
              >编辑账号</el-button
            >
            <!-- <el-button
                            class="link-action"
                            size="small"
                            type="primary"
                            @click="updateVerifyType(scope.row)"
                        >
                            更新密钥
                        </el-button> -->
          </template>
        </el-table-column>
      </el-table>
      <pagination :total="total" v-model:page="filter.page" v-model:limit="filter.page_size" @pagination="getList" v-show="total > 0" />
    </div>
    <!-- :createDisable="createDisable" -->
    <MDialog
      v-if="showDialog"
      :info="modifyOrCreateAccount"
      :role="role"
      :typeList="typeList"
      @change-visible="showDialog = $event"
      @create-action="createRequest"
      ref="mDialog"
      :visible="showDialog"
      @update-action="updateRequest"
    ></MDialog>
    <GAuthenticatorDialog
      :visible="showGDialog"
      :info="modifyOrCreateAccount"
      :pathimg="pathimg"
      :googleKey="googleKey"
      @clean-googleKey="googleKey = ''"
      :role="role"
      ref="gAuthenticatorDialog"
      v-if="showGDialog"
      @change-visible="showGDialog = $event"
      @update-action="updateRequest"
      @create-action="createRequest"
      @updateVerifyType-action="updateVerifyTypeRequest"
    ></GAuthenticatorDialog>
  </div>
</template>

<script>
import MDialog from './modifyDialog';
import GAuthenticatorDialog from './GAuthenticatorDialog.vue';
import { memberInsert, memberList, memberUpdatestate, groupDetail, memberUpdate, googlePath, updateGooglePath, getGroupList } from '@/api/permission';
import { mapState } from 'vuex';
import { long2ip } from '@/utils';
const timeTypeList = [
  { key: 1, value: '创建时间' },
  { key: 2, value: '最近登陆时间' }
];
export default {
  name: 'account',
  data() {
    return {
      googleKey: '',
      switchLoading: false,
      list: [],
      total: 0,
      filter: {
        page: 1,
        page_size: 20,
        time: [],
        username: '',
        state: 2,
        createname: '',
        group_type: '',
        timeType: 1, //查询时间类型
        group_id: ''
      },
      showDialog: false,
      showGDialog: false,
      selectedRole: null,
      modifyOrCreateAccount: {},
      role: '',
      pathimg: '',
      listLoading: false, // 控制列表加载loading
      // createDisable: false,
      timeTypeList, //时间查询类型
      groupList: [] //分组列表
    };
  },

  created() {
    const query = this.$route.query;
    if (query.group_type) {
      this.filter.group_type = Number(query.group_type);
      this.getGroupList();
    }
    if (query.group_id) {
      this.filter.group_id = Number(query.group_id);
    }
    this.getList();
  },

  computed: {
    ...mapState({
      typeList: state => state.permission.typeList
    })
  },

  methods: {
    long2ip,
    onSearch() {
      this.filter.page = 1;
      this.getList();
    },
    changeGroupList() {
      if (this.filter.group_type) {
        this.getGroupList();
      }
      this.filter.group_id = '';
    },
    //分组管理列表
    async getGroupList() {
      const res = await getGroupList({
        types: this.filter.group_type
      });
      this.loading = false;
      this.groupList = res.data;
    },
    async getImgPath(id) {
      const imgres = await googlePath({
        user_id: id
      });
      this.pathimg = imgres.data.pic;
      this.googleKey = imgres.data.secretKey;
    },

    async updateVerifyType(row) {
      this.modifyOrCreateAccount = { ...row };
      this.role = 'updateVerifyType';
      this.getImgPath(row.id);
      this.showGDialog = true;
    },
    //更新谷歌验证码
    async updateVerifyTypeRequest(callback) {
      const res = await updateGooglePath({
        id: this.modifyOrCreateAccount.id
      });
      if (res.status == 'true') {
        this.$message.success('更新成功~');
        this.getImgPath(this.modifyOrCreateAccount.id);
      }
      callback();
    },
    //查询type类型更改
    timeTypeChange() {
      this.filter.time = [];
    },
    async getList() {
      this.listLoading = true;
      let filter = { ...this.filter };
      if (!filter.time) filter.time = [];
      if (filter.timeType == 1) {
        filter.start_time = filter.time && filter.time[0] ? filter.time[0] : '';
        filter.end_time = filter.time && filter.time[1] ? filter.time[1] : '';
      } else {
        filter.login_start_time = filter.time && filter.time[0] ? filter.time[0] : '';
        filter.login_end_time = filter.time && filter.time[1] ? filter.time[1] : '';
      }
      delete filter.time;
      delete filter.timeType;
      const res = await memberList(filter);
      this.listLoading = false;
      if (this.filter.page == 1) {
        this.total = res.data.t;
      }
      if (!res.data.d) {
        this.list = [];
        return;
      }
      const group_use = res.data.d.map(el => {
        return el.group_id;
      });
      const groupList = await groupDetail({
        id: group_use.join(',')
      });
      this.list = res.data.d.map(el => {
        return {
          ...el,
          group_type_name: groupList.data[el.group_id]
        };
      });
    },

    updateAction(row) {
      this.role = 'update';
      let item = { ...row }; // 修改时不用更新密钥
      item.pwd = '';
      // this.googleKey = item.googleKey;
      this.modifyOrCreateAccount = item;
      this.showDialog = true;
      // this.createDisable = false;
    },
    //账号启用禁用
    async updateItem(row) {
      this.switchLoading = true;
      await memberUpdatestate({
        id: row.id,
        state: row.state == 1 ? 0 : 1
      })
        .then(res => {
          if (res.status == 'true') {
            this.getList();
          }
        })
        .catch(e => {
          this.switchLoading = false;
        });
      this.switchLoading = false;
    },

    async updateRequest(flag, callback) {
      const modifyOrCreateAccount = { ...this.modifyOrCreateAccount };
      delete modifyOrCreateAccount.update_by_id;
      delete modifyOrCreateAccount.create_by_id;
      delete modifyOrCreateAccount.create_by_name;
      delete modifyOrCreateAccount.LastLoginIP;
      delete modifyOrCreateAccount.LastLoginTime;
      delete modifyOrCreateAccount.update_by_name;
      delete modifyOrCreateAccount.update_time;
      delete modifyOrCreateAccount.group_type_name;
      const res = await memberUpdate({
        ...modifyOrCreateAccount
      });
      // this.createDisable = false;
      if (res.status == 'true') {
        this.showDialog = false;
        this.getList();
        this.$message.success('编辑成功');
      }
      callback();
    },

    async createAction() {
      this.role = 'create';
      // this.createDisable = false;
      this.modifyOrCreateAccount = {
        // verifyType: 2,
        username: undefined,
        pwd: undefined,
        group_id: undefined,
        state: undefined,
        group_type: undefined
      };
      this.showDialog = true;
    },

    async createRequest(flag, callback) {
      // delete this.modifyOrCreateAccount.superUserRoleList;
      // if (this.modifyOrCreateAccount.verifyType == 2) {
      //   this.modifyOrCreateAccount.googleKey = this.googleKey;
      // }
      // console.info(falg);
      const res = await memberInsert({
        ...this.modifyOrCreateAccount
      });
      // this.createDisable = false;
      if (res.status == 'true') {
        this.$message.success('新增成功');
        this.showDialog = false;
        this.getList();
      }
      callback();
      this.role = '';
      //   this.googleKey = "";
      // }
    }
  },

  components: {
    MDialog,
    GAuthenticatorDialog
  }
};
</script>

<style lang="scss" scoped>
.bottom {
  text-align: right;
}

.filter {
  margin-bottom: 5px;
  .el-form-item--mini.el-form-item,
  .el-form-item--small.el-form-item {
    margin-bottom: 0px !important;
  }
}
// .el-form--inline .el-form-item__content {
//     display: flex;
// }
//
</style>
