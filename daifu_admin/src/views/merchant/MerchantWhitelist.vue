<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" ref="searchForm" class="demo-form-inline" :rules="rules">
          <el-form-item label="商户" prop="merchant_id">
            <el-select v-model="formInline.merchant_id" placeholder="请选择商户" clearable>
              <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="IP" prop="ip">
            <el-input v-model="formInline.ip" placeholder="请输入IP地址" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="handleShowWhite"> 添加 </el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="content-list">
        <el-table :data="list" element-loading-text="Loading" border fit highlight-current-row>
          <el-table-column prop="id" label="id" align="center"></el-table-column>
          <el-table-column prop="create_at" label="商户" align="center">
            <template #default="scope">
              <span>{{ channelIdToText(scope.row.merchant_id) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="create_at" label="IP" align="center">
            <template #default="scope">
              <span>{{ scope.row.ip }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="create_at" label="创建时间" align="center">
            <template #default="scope">
              <span>{{ dateFormat(scope.row.created_at * 1000) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="created_by_name" label="创建人" align="center">
            <template #default="scope">
              <span>{{ scope.row.created_by_name }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="130">
            <template #default="scope">
              <el-button size="mini" type="danger" @click="delIP(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-pagination
        v-model:currentPage="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="page_size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
      </el-pagination>
    </el-card>

    <el-dialog v-model="isShowWhite" title="IP白名单" center width="20%">
      <el-form ref="addForm" :model="defaultForm" class="demo-form-inline" label-width="auto" :rules="rules">
        <el-form-item label="商户" prop="merchant_id">
          <el-select v-model="defaultForm.merchant_id" placeholder="请选择商户" clearable>
            <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="IP" prop="ip">
          <el-input size="small" v-model.trim="defaultForm.ip" autocomplete="off" style="max-width: 216px"></el-input>
        </el-form-item>
        <el-form-item label="动态验证" prop="code">
          <el-input size="small" v-model.trim="defaultForm.code" placeholder="请输入动态验证码" type="number" style="max-width: 216px"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelIP">取消</el-button>
          <el-button type="primary" @click="submitIP">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, toRefs, ref } from 'vue';
import { merchantMenu } from '../../http/apis/channel';
import { getMerchantWhiteList, addMerchantWhiteList, delMerchantWhiteList } from '../../http/apis/whitelist';
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElMessage, ElMessageBox } from 'element-plus';
import { empty, dateFormat } from '../../utils/common';
import { ipNumber, ipTips } from '../../utils/expressions';

export default {
  name: 'Whitelist',
  components: { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem },
  setup() {
    const state = reactive({
      formInline: {
        ip: '',
        merchant_id: ''
      },
      defaultForm: {},
      currentPage: 1,
      total: 0,
      page_size: 10,
      list: [],

      isShowDialog: false,
      isShowSuccessInfo: false,
      isShowWhite: false
    });

    // 代付渠道列表
    const channelMenuList = ref([]);
    merchantMenu().then(res => {
      if (res.status) {
        if (Array.isArray(res.data)) {
          channelMenuList.value = res.data;
        } else {
          channelMenuList.value = [];
        }
      }
    });

    // 代付列表转文字
    const channelIdToText = pid => {
      const obj = channelMenuList.value.find(item => item.id == pid);
      return obj ? obj.name : '-';
    };

    const getList = () => {
      getMerchantWhiteList(
        empty.preProcessData({
          ...state.formInline,
          page: state.currentPage,
          page_size: state.page_size
        })
      ).then(res => {
        state.list = [];
        if (state.currentPage == 1) {
          state.total = Number(res.data.total) || 0;
        }
        if (res.status) {
          if (Array.isArray(res.data.d)) {
            state.list = res.data.d || [];
          }
        }
      });
    };
    getList();

    const rules = reactive({
      merchant_id: [
        {
          required: true,
          message: '请选择商户',
          trigger: 'blur'
        }
      ],
      ip: [
        {
          required: true,
          message: '请输入白名单IP',
          trigger: 'blur'
        },
        {
          pattern: ipNumber,
          message: ipTips
        }
      ],
      code: [
        {
          required: true,
          message: '请输入动态验证码',
          trigger: 'blur'
        }
      ]
    });

    const addForm = ref(null);
    const searchForm = ref(null);

    const search = () => {
      state.formInline.merchant_id = decodeURIComponent(state.formInline.merchant_id || '');
      state.formInline.ip = decodeURIComponent(state.formInline.ip || '');
      state.currentPage = 1;
      getList();
    };

    const handleSizeChange = val => {
      state.page_size = val;
      state.currentPage = 1;
      getList();
    };
    const handleCurrentChange = val => {
      state.currentPage = val;
      getList();
    };

    const handleShowWhite = () => {
      addForm.value && addForm.value.resetFields();
      state.isShowWhite = true;
    };

    const submitIP = () => {
      addForm.value.validate(valid => {
        if (valid) {
          let params = {};
          params.id = state.defaultForm.merchant_id;
          params.ip = state.defaultForm.ip;
          params.code = state.defaultForm.code;
          addMerchantWhiteList(params).then(res => {
            if (res.status) {
              ElMessage.success('添加成功');
              searchForm.value && searchForm.value.resetFields();
              cancelIP();
              getList();
            }
          });
        }
      });
    };

    const cancelIP = () => {
      state.isShowWhite = false;
      addForm.value && addForm.value.resetFields();
    };

    const delIP = row => {
      ElMessageBox.confirm('你确定要删除吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delMerchantWhiteList({
          id: row.id
        }).then(res => {
          if (res.status) {
            ElMessage.success('删除成功');
            getList();
          }
        });
      });
    };

    return {
      ...toRefs(state),
      addForm,
      searchForm,
      rules,
      channelIdToText,
      handleSizeChange,
      handleCurrentChange,
      channelMenuList,
      search,
      dateFormat,
      handleShowWhite,
      cancelIP,
      submitIP,
      delIP
    };
  }
};
</script>
<style lang="less" scoped>
.app-container {
  .content-list {
    margin-top: 20px;
    margin-bottom: 20px;
  }
}
.login-key {
  display: flex;
}
</style>