<template>
  <div class="pd-30 channel">
    <div class="">
      <el-form :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item label="渠道名称">
          <el-input size="small" v-model="formInline.name" placeholder="请输入渠道名称"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select size="small" v-model="formInline.state" placeholder="请选择状态">
            <el-option label="禁用" :value="0" :key="0"></el-option>
            <el-option label="启用" :value="1" :key="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="getChannel">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="small" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="m-b20"><el-button type="primary" size="small" @click="openDialog">新增渠道</el-button></div>
    <el-table
      border
      :loading="isLoading"
      element-loading-background="rgba(255, 255, 255, .5)"
      element-loading-text="加载中..."
      element-loading-spinner="el-icon-loading"
      :data="tableData"
      style="width: 100%"
    >
      <el-table-column label="序号" type="index" align="center" width="80" />
      <el-table-column label="渠道名称" prop="name" align="center" />
      <el-table-column label="商户号" prop="account" align="center" />
      <el-table-column label="商户秘钥" prop="ppk" align="center" />
      <el-table-column label="回调地址" prop="notify_url" align="center" />
      <el-table-column label="渠道网关" prop="gateway" align="center" />
      <el-table-column label="费率" prop="rate" align="center" width="100" />
      <el-table-column label="状态" prop="state" align="center" width="100">
        <template v-slot="scope">
          <span :style="{ color: scope.row.state === 1 ? 'green' : 'red' }">{{ ['禁用', '启用'][scope.row.state] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <!-- <el-button type="primary" size="small" @click="handleState(scope.$index, scope.row)">{{scope.row.state === 1 ? '关闭': '开启'}}</el-button> -->
          <el-button type="primary" size="small" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="showDelDialog(scope.$index, scope.row)">删除</el-button>
        </template>
      </el-table-column>
      <template v-slot:empty><div>无数据</div></template>
    </el-table>
    <div class="flex-middle-only page-wp">
      总共有{{ total }}条数据 第{{ page }}/{{ Math.ceil(total / page_size) }}页
      <el-pagination
        background
        layout="prev, pager, next, jumper,"
        :page-sizes="[10, 20, 50, 100, 200]"
        :page-size="page_size"
        @size-change="gotoPage($event, 'size')"
        @current-change="gotoPage($event, 'page')"
        :total="total"
      >
      </el-pagination>
    </div>
    <el-dialog v-model="showDialog" :title="dialogType == 'add' ? '新增渠道' : '编辑渠道'" center>
      <el-form :model="form" :rules="rules" ref="addform">
        <el-form-item label="渠道名称" prop="name" label-width="120px">
          <el-input v-model="form.name" maxlength="20" autocomplete="off" placeholder="请输入渠道名称"></el-input>
        </el-form-item>
        <el-form-item label="商户号" prop="account" label-width="120px">
          <el-input v-model="form.account" maxlength="20" autocomplete="off" placeholder="请输入商户号"></el-input>
        </el-form-item>
        <el-form-item label="商户秘钥" prop="ppk" label-width="120px">
          <el-input v-model="form.ppk" maxlength="50" autocomplete="off" placeholder="请输入商户秘钥"></el-input>
        </el-form-item>
        <el-form-item label="渠道网关" prop="gateway" label-width="120px">
          <el-input v-model="form.gateway" maxlength="20" autocomplete="off" placeholder="请输入渠道网关"></el-input>
        </el-form-item>
        <el-form-item label="回调地址" prop="notify_url" label-width="120px">
          <el-input v-model="form.notify_url" maxlength="50" autocomplete="off" placeholder="请输入回调地址"></el-input>
        </el-form-item>
        <el-form-item label="费率" prop="rate" label-width="120px">
          <el-input v-model="form.rate" maxlength="10" @input="form.rate = form.rate.replace(/[^\d.]/g, '')" autocomplete="off" placeholder="请输入费率"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="state" label-width="120px">
          <el-select v-model="form.state" placeholder="请选择状态">
            <el-option label="禁用" :value="0" :key="0"></el-option>
            <el-option label="启用" :value="1" :key="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="onCancel">取消</el-button>
          <el-button type="primary" @click="onSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="delDialog" title="提示" width="14%" center>
      <span>确认删除该上游渠道？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="delDialog = false">取消</el-button>
          <el-button type="primary" @click="handleDelChannel">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script src="./index.js"></script>


<style lang="less" scoped>
@import './index.less';
</style>
