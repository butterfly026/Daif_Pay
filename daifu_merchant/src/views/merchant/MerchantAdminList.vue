<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商户管理员列表 {{formInline.name}}</span>
        </div>
      </template>
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="商户">
            <el-select v-model="formInline.merchant_id" placeholder="请选择商户" clearable >
              <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="会员名">
            <el-input v-model="formInline.username" placeholder="请输入会员名"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="formInline.state">
              <el-option label="全部" value="">全部</el-option>
              <el-option label="正常" value="1">正常</el-option>
              <el-option label="关闭" value="0">关闭</el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="addMerchant"> 添加商户后台会员 </el-button>
          </el-form-item>
        </el-form>
        <!-- <el-input placeholder="请输入商户账号" v-model="query" style="width: 280px; margin-left: 0" class="filter-item" />
        <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-search" @click="searchData"> 搜索 </el-button>
        <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="addMerchant"> 添加商户 </el-button> -->
      </div>
      <div class="content-list">
        <el-table :data="list" element-loading-text="Loading" border fit highlight-current-row>
          <el-table-column type="index" label="序号" width="100" align="center"></el-table-column>
          <el-table-column prop="username" label="会员名" width="130" align="center"></el-table-column>
          <el-table-column prop="create_at" label="商户" align="center">
            <template #default="scope">
              <span>{{ channelIdToText(scope.row.merchant_id) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="create_at" label="创建时间" align="center">
            <template #default="scope">
              <span>{{ dateFormat(scope.row.created_at * 1000) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最后登录时间" align="center">
            <template #default="scope">
              <span>{{ dateFormat(scope.row.updated_at * 1000) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="LastLoginIP" label="最后登录IP" align="center">
            <template #default="scope">
              <span>{{ long2ip(scope.row.created_ip) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="created_by_name" label="创建人" align="center"></el-table-column>
          <el-table-column label="状态" align="center">
            <template #default="{ row }">
              <span style="color: red" v-if="row.state == 0">关闭</span>
              <span style="color: green" v-if="row.state == 1">正常</span>
              <!-- <vxe-switch :model-value="row.state" open-label="启用" :open-value="1" close-label="禁用" :close-value="0"></vxe-switch> -->
            </template>
          </el-table-column>
          <!-- <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button @click="edit(scope.row)" type="text" class="crs-p oper-txt-c">编辑</el-button> -->
              <!-- <el-divider direction="vertical"></el-divider>
              <el-button type="text" @click="deleteAccount">删除</el-button> -->
            <!-- </template>
          </el-table-column> -->
          <el-table-column align="center" label="操作">
            <template #default="scope">
              <el-button type="primary" size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="delMerchant(scope.row)">删除</el-button>
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
      <el-dialog v-model="isShowDialog" :title="'添加商户后台会员' + isRules" center>
        <div>
          <el-form :model="defaultForm" :rules="rules" ref="addForm">
            <el-form-item label="商户" prop="merchant_id" label-width="140px">
              <el-select v-model="defaultForm.merchant_id" placeholder="请选择商户">
                <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="会员名" prop="username" label-width="140px">
              <el-input v-model="defaultForm.username" maxlength="20" autocomplete="off" placeholder="请输入会员名"></el-input>
            </el-form-item>
            <template v-if="isRules">
              <el-form-item label="密码" label-width="140px">
                <el-input v-model="defaultForm.password" autofocus maxlength="20" autocomplete="off" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item label="确认密码" label-width="140px">
                <el-input v-model="defaultForm.password2" maxlength="20" autocomplete="off" placeholder="请确认密码"></el-input>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="密码" :prop="'password'" label-width="140px">
                <el-input v-model="defaultForm.password" autofocus maxlength="20" autocomplete="off" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item label="确认密码" :prop="'password2'" label-width="140px">
                <el-input v-model="defaultForm.password2" maxlength="20" autocomplete="off" placeholder="请确认密码"></el-input>
              </el-form-item>
            </template>
            <el-form-item v-if="!isEdit" label="登录密钥" prop="google" label-width="140px">
              <div class="login-key"><el-input v-model="defaultForm.google" disabled maxlength="20" autocomplete="off" placeholder="点击生成登录密钥"></el-input><el-button @click="generate">生成密钥</el-button></div>
            </el-form-item>
            <el-form-item label="商户状态" label-width="140px">
              <el-radio-group v-model="defaultForm.state">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
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
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, ref, computed } from 'vue';
import { merchantMenu } from '../../http/apis/channel'
import { addMerchantMember, getMerchantMemberList, editMerchantMember, deleteMerchantMember } from '../../http/apis/merchant'
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio, ElMessage, ElMessageBox } from 'element-plus';
// import { getMerchantList, addMerchant, editMerchant } from '@/http/apis/merchant';
import { generateCode } from '../../utils/generateCode'
import { empty, dateFormat, long2ip } from '../../utils/common'
import { user_name, password, user_name_prompt, password2Msg } from '../../utils/expressions'

export default {
  name: 'MerchantList',
  components: { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio },
  setup() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (state.defaultForm.password2 !== '') {
          addForm.value.validateField('password2')
        }
        callback()
      }
    }
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入确认密码'))
      } else if (value !== state.defaultForm.password) {
        callback(new Error("密码不正确"))
      } else {
        callback()
      }
    }

    const generate = () => {
     state.defaultForm.google =  generateCode();
    }

    // 是否验证密码
    const isRules = computed(() => {
      // 编辑，并且密码为空，返回true
      return state.isEdit && (!state.defaultForm.password && !state.defaultForm.password2)
    })

    const state = reactive({
      query: '',
      formInline: {
        state: "",
        name: ""
      },
      isEdit: true,
      defaultForm: {
        state: 1
      },
      currentPage: 1,
      total: 0,
      page_size: 10,
      list: [],

      isShowDialog: false
    });


    // 代付渠道列表
    const channelMenuList = ref([]);
    merchantMenu().then(res => {
      if (res.status) {
       if (Array.isArray(res.data)) {
          channelMenuList.value = res.data
        } else {
          channelMenuList.value = []
        }
      }
    })

    // 代付列表转文字
    const channelIdToText = (pid) => {
      const obj = channelMenuList.value.find(item => item.id == pid);
      return obj ? obj.name : "-"
    }

    const getList = () => {
      getMerchantMemberList(empty.preProcessData({
        ...state.formInline,
        page: state.currentPage,
        page_size: state.page_size
      })).then(res => {
        if (res.status) {
          state.total = Number(res.data.total) || 0;
          state.list = res.data.d || [];
        }
      })
    }
    getList();
    
    const rules = reactive({
      merchant_id: [
        {
          required: true,
          message: '请选择代付渠道',
          trigger: 'blur'
        }
      ],
      username: [
        {
          required: true,
          message: '请输入用户名称',
          trigger: 'blur'
        },
        {
          pattern: user_name,
          message: user_name_prompt
        }
      ],
      google: [
        {
          required: true,
          message: '请先生成密钥',
          trigger: 'blur'
        }
      ],
      password: [{
          required: true,
          message: '请输入密码',
          trigger: 'blur'
        },{
          pattern: password,
          message: password2Msg
        },{ validator: validatePass, trigger: 'blur' }],
      password2: [{
          required: true,
          message: '请输入确认密码',
          trigger: 'blur'
        },{
          pattern: password,
          message: password2Msg
        },{ validator: validatePass2, trigger: 'blur' }],
    });
    // 搜索
    const searchData = () => {
      console.log(state.query);
    };

    const addMerchant = () => {
      state.isEdit = false;
      state.defaultForm = {};
      state.isShowDialog = true;
    };

    const addForm = ref(null);

    const handleEdit = (index, row) => {
      state.isEdit = true;
      state.defaultForm.password = "";
      state.defaultForm.password2 = "";
      Object.assign(state.defaultForm, row);
      console.log(state.defaultForm)
      state.isShowDialog = true;
    };

    const delMerchant = row => {
      ElMessageBox.confirm('你确定要删除吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMerchantMember({
          uid: row.uid
        }).then(res => {
          if (res.status) {
            ElMessage.success("删除成功");
            getList();
          }
        })
        
      })
    };

    const search = () => {
      state.formInline.username = decodeURIComponent(state.formInline.username || '');
      state.currentPage = 1;
      getList()
    }

    const onSubmit = () => {
      addForm.value.validate(valid => {
        if (valid) {
          let params = {};
          if (state.isEdit) {
            Object.assign(params, {
              uid: state.defaultForm.uid,
              username: state.defaultForm.username,
              password: state.defaultForm.password,
              state: state.defaultForm.state,
            })
          } else {
            Object.assign(params, {
              ...state.defaultForm
            })
          }
          
          delete params.password2;
          [addMerchantMember, editMerchantMember][state.isEdit ? 1 : 0](params).then(res => {
            if (res.status) {
              ElMessage.success(state.isEdit ? '编辑成功' : '新增成功');
              getList();
              onCancel();
            }
          })
        }
      });
    };

    const onCancel = () => {
      addForm.value && addForm.value.resetFields();
      state.defaultForm = {};
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

    return {
      ...toRefs(state),
      rules,
      addForm,
      searchData,
      addMerchant,
      channelIdToText,
      onCancel,
      onSubmit,
      handleEdit,
      delMerchant,
      handleSizeChange,
      handleCurrentChange,
      channelMenuList,
      generate,
      search,
      dateFormat, 
      long2ip,
      isRules
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