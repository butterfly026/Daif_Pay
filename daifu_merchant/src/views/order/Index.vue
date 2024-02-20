<template>
  <div style="padding: 20px">
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="Approved by">
        <el-input v-model="formInline.user" placeholder="Approved by"></el-input>
      </el-form-item>
      <el-form-item label="Activity zone">
        <el-select v-model="formInline.region" placeholder="Activity zone">
          <el-option label="Zone one" value="shanghai"></el-option>
          <el-option label="Zone two" value="beijing"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit" size="medium">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column type="index" label="序号" align="center" width="60" />
      <el-table-column prop="date" label="Date" align="center" />
      <el-table-column prop="name" label="Name" align="center" />
      <el-table-column prop="address" label="Address" align="center" />
      <el-table-column prop="address" label="操作" align="center">
        <template #default="">
          <el-button type="danger" size="small" @click="$refs.IndexReview.showForm()">拒绝</el-button>
          <el-button type="primary" size="small">通过</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="empty"></div>
    <el-pagination
      v-show="totalCount > 0"
      v-model:currentPage="currentPage"
      background
      :page-sizes="[10, 20, 50, 100]"
      :page-size="page_size"
      layout="total, sizes, prev, pager, next, jumper"
      :total="totalCount"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    >
    </el-pagination>
    <IndexReview ref="IndexReview" />
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElButton, ElTable, ElTableColumn, ElForm, ElFormItem, ElInput, ElOption, ElSelect, ElPagination } from 'element-plus';
import IndexReview from './components/IndexReview.vue';
export default {
  components: {
    [ElButton.name]: ElButton,
    [ElTable.name]: ElTable,
    [ElTableColumn.name]: ElTableColumn,
    [ElForm.name]: ElForm,
    [ElFormItem.name]: ElFormItem,
    [ElInput.name]: ElInput,
    [ElOption.name]: ElOption,
    [ElSelect.name]: ElSelect,
    [ElPagination.name]: ElPagination,
    IndexReview
  },

  setup() {
    const totalCount = ref(100);
    const page = ref(1);
    const page_size = ref(10);
    const currentPage = ref(1);
    var tableData = ref([
      {
        date: '2016-05-03',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles'
      },
      {
        date: '2016-05-02',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles'
      },
      {
        date: '2016-05-04',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles'
      },
      {
        date: '2016-05-01',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles'
      }
    ]);
    var formInline = ref({
      user: '',
      region: ''
    });
    const onSubmit = () => {
      currentPage.value = 1;
      console.log(formInline.value);
      getList();
    };
    const handleSizeChange = val => {
      page_size.value = val;
      state.currentPage = 1;
      getList();
      console.log(`${val} items per page`);
    };
    const handleCurrentChange = val => {
      currentPage.value = val;
      getList();
      console.log(`current page: ${val}`);
    };
    const getList = () => {
      var params = {
        page: currentPage.value,
        page_size: page_size.value,
        ...formInline.value
      };
      console.log(params);
    };
    getList();
    return {
      tableData,
      formInline,
      currentPage,
      handleSizeChange,
      handleCurrentChange,
      onSubmit,
      totalCount,
      getList,
      page,
      page_size
    };
  }
};
</script>
<style lang="less" scoped>
.empty {
  height: 20px;
}
</style>