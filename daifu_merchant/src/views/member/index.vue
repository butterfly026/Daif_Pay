<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
        </div>
      </template>
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="用户名">
            <el-input v-model="formInline.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item>
            <el-form-item label="状态">
              <el-select v-model="formInline.state">
                <el-option label="全部" value="">全部</el-option>
                <el-option label="正常" value="1">开启</el-option>
                <el-option label="关闭" value="0">关闭</el-option>
              </el-select>
            </el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="addMember"> 添加会员 </el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="content-list">
        <el-table :data="list" element-loading-text="Loading" border fit highlight-current-row>
          <el-table-column label="会员账号" align="center">
            <template #default="scope">
              <span>{{ scope.row.username }}</span>
            </template>
          </el-table-column>
          <el-table-column label="会员id" align="center">
            <template #default="scope">
              <span>{{ scope.row.uid }}</span>
            </template>
          </el-table-column>
          <el-table-column label="商户id" align="center">
            <template #default="scope">
              {{ scope.row.merchant_id }}
            </template>
          </el-table-column>
          <el-table-column label="创建用户名" align="center">
            <template #default="scope">
              {{ scope.row.created_by_name }}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center">
            <template #default="scope">
              <span :style="{ color: scope.row.state == 1 ? 'green' : 'red' }">
                {{ scope.row.state == 1 ? '正常' : '关闭' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="登录时间" align="center">
            <template #default="scope">
              {{ dateFormat(scope.row.created_at * 1000) }}
            </template>
          </el-table-column>
          <el-table-column label="登录ip" align="center">
            <template #default="scope">
              {{ scope.row.updated_ip }}
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="delMemberById(scope.row)">删除</el-button>
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

    <div class="add-dialog">
      <el-dialog v-model="isShowDialog" :title="isEdit ? '编辑会员' : '添加会员'" center>
        <div>
          <el-form :model="defaultForm" :rules="rules" ref="addForm">
            <el-form-item label="会员名" prop="username" label-width="140px">
              <el-input v-model="defaultForm.username" maxlength="20" autocomplete="off" :disabled="isEdit ? true : false" placeholder="会员名"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password" label-width="140px">
              <el-input v-model="defaultForm.password" maxlength="20" autocomplete="off" placeholder="请输入密码"></el-input>
            </el-form-item>
            <el-form-item label="状态" label-width="140px">
              <el-radio-group v-model="defaultForm.state">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="登录密钥" prop="google" label-width="140px">
              <div class="login-key">
                <el-input v-model="defaultForm.google" disabled maxlength="20" autocomplete="off" placeholder="点击生成登录密钥"></el-input><el-button @click="generate">生成密钥</el-button>
              </div>
            </el-form-item>
            <el-form-item label="动态验证码" prop="code" label-width="140px">
              <el-input v-model="defaultForm.code" type="number" maxlength="50" autocomplete="off" placeholder="请输入动态验证码"></el-input>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="onCancel">取消</el-button>
            <el-button type="primary" @click="onSubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 成功添加提示 -->
      <el-dialog v-model="isShowSuccessInfo" title="操作成功" center width="20%" :show-close="false" :close-on-press-escape="false" :close-on-click-modal="false">
        <div>
          <p>登录账号：{{ defaultForm.username }}</p>
          <p>登录密码：{{ defaultForm.password }}</p>
          <p>登录密钥：{{ defaultForm.google }}</p>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button type="primary" @click="closeSuccessInfo">好的</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, ref } from 'vue';
import { addMember as addMemberFetch, getMemberList, delMember, editMenber } from '../../http/apis/member';
import { ElMessage, ElMessageBox } from 'element-plus';
import { empty, dateFormat } from '../../utils/common';
import { getCode } from '../../http/apis/merchant';

export default {
  name: 'Member',
  setup() {
    const state = reactive({
      formInline: {
        username: '',
        state: ''
      },
      isEdit: true,
      defaultForm: {
        username: '',
        state: 1,
        password: '',
        google: ''
      },
      currentPage: 1,
      total: 0,
      page_size: 10,
      list: [],

      isShowDialog: false,
      isShowSuccessInfo: false
    });

    const getList = () => {
      getMemberList(
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
      username: [
        {
          required: true,
          message: '请输入会员名',
          trigger: 'blur'
        }
      ],
      state: [
        {
          required: true,
          message: '请选择状态',
          trigger: 'blur'
        }
      ],
      password: [
        {
          required: true,
          message: '请输入密码',
          trigger: 'blur'
        }
      ],
      code: [
        {
          required: true,
          message: '请输入动态验证码',
          trigger: 'blur'
        }
      ],
      google: [
        {
          required: true,
          message: '请获取登录密钥',
          trigger: 'blur'
        }
      ]
    });
    const addMember = () => {
      state.defaultForm = {};
      state.isEdit = false;
      state.isShowDialog = true;
    };

    const addForm = ref(null);

    const handleEdit = (index, row) => {
      state.isEdit = true;
      Object.assign(state.defaultForm, row);
      state.isShowDialog = true;
    };

    const delMemberById = row => {
      ElMessageBox.confirm('你确定要删除吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delMember({
          uid: row.uid
        }).then(res => {
          if (res.status) {
            ElMessage.success('删除成功');
            getList();
          }
        });
      });
    };

    const generate = () => {
      getCode().then(res => {
        if (res.status) {
          state.defaultForm.google = res.data;
        } else {
          return false;
        }
      });
    };

    const search = () => {
      state.formInline.username = decodeURIComponent(state.formInline.username || '');
      state.currentPage = 1;
      getList();
    };

    const onSubmit = () => {
      addForm.value.validate(valid => {
        if (valid) {
          let params = {};
          if (state.isEdit) {
            Object.assign(params, {
              uid: state.defaultForm.uid,
              password: state.defaultForm.password,
              state: state.defaultForm.state,
              google: state.defaultForm.google,
              code: state.defaultForm.code
            });
          } else {
            Object.assign(params, {
              ...state.defaultForm
            });
          }
          [addMemberFetch, editMenber][state.isEdit ? 1 : 0](params).then(res => {
            if (res.status) {
              ElMessage.success(state.isEdit ? '编辑成功' : '新增成功');
              getList();
              state.isShowDialog = false;
              state.isShowSuccessInfo = true;
            }
          });
        }
      });
    };

    const onCancel = () => {
      addForm.value && addForm.value.resetFields();
      state.isShowDialog = false;
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

    const closeSuccessInfo = () => {
      state.isShowSuccessInfo = false;
      onCancel();
    };

    return {
      ...toRefs(state),
      rules,
      addForm,
      addMember,
      onCancel,
      onSubmit,
      handleEdit,
      delMemberById,
      handleSizeChange,
      handleCurrentChange,
      dateFormat,
      search,
      generate,
      closeSuccessInfo
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