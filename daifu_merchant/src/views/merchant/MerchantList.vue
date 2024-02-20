<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商户列表 {{formInline.name}}</span>
        </div>
      </template>
      <div class="filter-container">
        <el-form :inline="true" :model="formInline" class="demo-form-inline">
          <el-form-item label="代付渠道">
            <el-select v-model="formInline.channel_id" placeholder="请选择代付渠道" clearable >
              <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="商户名">
            <el-input v-model="formInline.name" placeholder="请输入商户名"></el-input>
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
            <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="addMerchant"> 添加商户 </el-button>
          </el-form-item>
        </el-form>
        <!-- <el-input placeholder="请输入商户账号" v-model="query" style="width: 280px; margin-left: 0" class="filter-item" />
        <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-search" @click="searchData"> 搜索 </el-button>
        <el-button class="filter-item" style="margin-left: 10px" type="primary" icon="el-icon-circle-plus-outline" @click="addMerchant"> 添加商户 </el-button> -->
      </div>
      <div class="content-list">
        <el-table :data="list" element-loading-text="Loading" border fit highlight-current-row>
          <el-table-column label="商户ID" align="center">
            <template #default="scope">
              <span>{{ scope.row.id }}</span>
            </template>
          </el-table-column>
           <el-table-column label="商户名称" align="center">
            <template #default="scope">
              <span>{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="代付渠道" align="center">
            <template #default="scope">
              {{ channelIdToText(scope.row.channel_id) }}
            </template>
          </el-table-column>
          <el-table-column label="费率" align="center">
            <template #default="scope">
              {{ scope.row.withdraw_fee }}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center">
            <template #default="scope">
              <span :style="{color: scope.row.state == 1 ? 'green' : 'red'}">
                {{ scope.row.state == 1 ? "正常" : "关闭" }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="备注" align="center">
            <template #default="scope">
              {{ scope.row.remarks }}
            </template>
          </el-table-column>
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
      <el-dialog v-model="isShowDialog" title="添加商户" center>
        <div>
          <el-form :model="defaultForm" :rules="rules" ref="addForm">
            <el-form-item label="代付渠道" prop="channel_id" label-width="140px">
              <el-select v-model="defaultForm.channel_id" placeholder="请选择渠道">
                <el-option v-for="(item, index) in channelMenuList" :label="item.name" :value="item.id" :key="index"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="商户名称" prop="name" label-width="140px">
              <el-input v-model="defaultForm.name" maxlength="20" autocomplete="off" placeholder="请输入商户名称"></el-input>
            </el-form-item>
            <el-form-item label="密钥" prop="ppk" label-width="140px">
              <div class="login-key"><el-input v-model="defaultForm.ppk" disabled maxlength="20" autocomplete="off" placeholder="请输入密码"></el-input><el-button @click="generate">生成密钥</el-button></div>
            </el-form-item>
            <el-form-item label="代付费率" prop="withdraw_fee" label-width="140px">
              <el-input v-model="defaultForm.withdraw_fee" type="number" maxlength="50" autocomplete="off" placeholder="请输入代充费率"></el-input>
            </el-form-item>
            <el-form-item label="手动单笔代付" label-width="140px">
              <el-radio-group v-model="defaultForm.m_single_withdraw">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="手动批量代付" label-width="140px">
              <el-radio-group v-model="defaultForm.m_batch_withdraw">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="API自动代付" label-width="140px">
              <el-radio-group v-model="defaultForm.api_withdraw">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="商户状态" label-width="140px">
              <el-radio-group v-model="defaultForm.state">
                <el-radio :label="1">开启</el-radio>
                <el-radio :label="0">关闭</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" label-width="140px">
              <el-input v-model="defaultForm.remarks" type="textarea"></el-input>
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
import { reactive, toRefs, ref } from 'vue';
import { channelMenu } from '../../http/apis/channel'
import { addMerchant as addMerchantFetch, getMerchantList, editMerchant, deleteMerchant } from '../../http/apis/merchant'
import { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio, ElMessage, ElMessageBox } from 'element-plus';
// import { getMerchantList, addMerchant, editMerchant } from '@/http/apis/merchant';
import { generateCode } from '../../utils/generateCode'
import { empty } from '../../utils/common'

export default {
  name: 'MerchantList',
  components: { ElCard, ElInput, ElButton, ElTable, ElTableColumn, ElSelect, ElOption, ElPagination, ElDialog, ElForm, ElFormItem, ElRadioGroup, ElRadio },
  setup() {
    const generate = () => {
     state.defaultForm.ppk =  generateCode();
    }
    const state = reactive({
      query: '',
      formInline: {
        state: "",
        name: ""
      },
      isEdit: true,
      defaultForm: {
        channel_id: '', // 代付渠道ID
        name: '', // 商户名称
        ppk: '', // 密钥
        withdraw_fee: '', // 费率
        m_single_withdraw: 1, // 手动单条代付
        m_batch_withdraw: 1, // 批量手动代付
        api_withdraw: 1, // api自动代付
        state: 1, // 状态
        remarks: '' // 备注

      },
      currentPage: 1,
      total: 0,
      page_size: 10,
      list: [],

      isShowDialog: false
    });


    // 代付渠道列表
    const channelMenuList = ref([]);
    channelMenu().then(res => {
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
      getMerchantList(empty.preProcessData({
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
      channel_id: [
        {
          required: true,
          message: '请选择代付渠道',
          trigger: 'blur'
        }
      ],
      name: [
        {
          required: true,
          message: '请输入商户名称',
          trigger: 'blur'
        }
      ],
      ppk: [
        {
          required: true,
          message: '请先生成密钥',
          trigger: 'blur'
        }
      ],
      withdraw_fee: [
        {
          required: true,
          message: '请输入代付费率',
          trigger: 'blur'
        }
      ],
    });
    // 搜索
    const searchData = () => {
      console.log(state.query);
    };

    const addMerchant = () => {
      state.isEdit = false;
      state.isShowDialog = true;
    };

    const addForm = ref(null);

    const handleEdit = (index, row) => {
      state.isEdit = true;
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
        deleteMerchant({
          id: row.id
        }).then(res => {
          if (res.status) {
            ElMessage.success("删除成功");
            getList();
          }
        })
        
      })
    };

    const search = () => {
      state.formInline.name = decodeURIComponent(state.formInline.name || '');
      state.currentPage = 1;
      getList()
    }

    const onSubmit = () => {
      addForm.value.validate(valid => {
        if (valid) {
          [addMerchantFetch, editMerchant][state.isEdit ? 1 : 0](state.defaultForm).then(res => {
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
      search
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