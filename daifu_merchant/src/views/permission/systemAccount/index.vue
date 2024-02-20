<template>
  <div class="pg-view system-account-view">
    <section class="query-sec">
      <span>账号：</span>
      <el-input v-model="queryForm.username" placeholder="请输入账号" maxlength="20" style="width: 200px"></el-input>
      <span class="mgl-20" style="margin-left: 10px">角色：</span>
      <el-input
        v-model="queryForm.gname"
        placeholder="请选择角色"
        maxlength="24"
        @click="triggerShowGroupUserDialog('query')"
        clearable
        class="crs-p mgl-10 ipt-input"
        style="width: 220px"
      ></el-input>
      <!-- <el-select v-model="queryForm.groupid" placeholder="请选择角色" class="mgl-10">
          <el-option
            v-for="item in groupUserList"
            :key="item.gid"
        新增账号    :label="item.label"
            :value="item.gid"
          >
          </el-option>
        </el-select> -->
      <span class="mgl-20" style="margin-left: 10px">状态：</span>
      <el-select v-model="queryForm.state" placeholder="请选择状态" class="mgl-10" style="padding-right: 10px">
        <el-option v-for="item in stateList" :key="item.value" :label="item.label" :value="item.value"> </el-option>
      </el-select>
      <el-button type="primary" @click="query" class="mgl-20" style="padding-right: 10px">筛选</el-button>
      <el-button @click="reset">重置</el-button>
    </section>
    <section class="pdb-20 mgt-20" style="padding: 10px 0;">
      <el-button type="primary" @click="addAccount">新增</el-button>
    </section>
    <section class="">
      <el-table :data="tableData" border fit style="width: 100%">
        <el-table-column type="index" label="序号" width="100" align="center"></el-table-column>
        <el-table-column prop="username" label="账号" width="130" align="center"></el-table-column>
        <!-- <el-table-column
          prop="email"
          label="邮箱"
          align="center"
        ></el-table-column> -->
        <el-table-column label="角色" align="center">
          <template #default="scope">
            <span>{{ groupUserMap.get(scope.row.group_id) }}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column
          prop="name"
          label="U盾序列"
          align="center"
        ></el-table-column> -->
        <el-table-column prop="create_at" label="创建时间1" align="center">
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
            <span style="color: red" v-if="row.state == 0">禁用</span>
            <span style="color: green" v-if="row.state == 1">启用</span>
            <!-- <vxe-switch :model-value="row.state" open-label="启用" :open-value="1" close-label="禁用" :close-value="0"></vxe-switch> -->
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button @click="edit(scope.row)" type="text" class="crs-p oper-txt-c">编辑</el-button>
            <!-- <el-divider direction="vertical"></el-divider>
            <el-button type="text" @click="deleteAccount">删除</el-button> -->
          </template>
        </el-table-column>
      </el-table>
    </section>
    <section class="flex" style="margin-top: 20px">
      <span style="line-height: 32px">共有{{ totalCount }}条数据 第{{ curPage }}/{{ totalPage }}页</span>
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="totalCount"
        :page-sizes="[10, 20, 50, 100, 200]"
        :page-size="pageSize"
        @size-change="sizesChangeHandler"
        @current-change="gotoPage"
      />
    </section>

    <el-dialog :title="`${dialogTitle}账号`" v-model="dialogVisible" width="800px" :close-on-click-modal="false" @close="closedDialog">
      <div class="dialog-ctx-wp">
        <el-form label-width="auto" :model="dialogFormInfo" :rules="formRules" ref="elDialogForm">
          <el-form-item label="账号" prop="name">
            <el-input
              v-model.trim="dialogFormInfo.username"
              placeholder="请输入账号"
              maxlength="20"
              :disabled="dialogType === 'edit'"
            ></el-input>
          </el-form-item>
          <el-form-item label="角色" prop="group_id">
            <el-input
              v-model="dialogFormInfo.gname"
              placeholder="请选择角色"
              maxlength="24"
              readonly
              @click="triggerShowGroupUserDialog('dialog')"
              class="crs-p"
            ></el-input>
            <!-- <el-select v-model="dialogFormInfo.group_id" prop="group_id" placeholder="请选择角色">
              <el-option
                v-for="item in groupUserList"
                :key="item.gid"
                :label="item.label"
                :value="item.gid"
              >
              </el-option>
            </el-select> -->
          </el-form-item>
          <el-form-item label="登录密码" prop="password">
            <el-input class="display_form" v-model.trim="dialogFormInfo.password" type="password"></el-input>
            <!-- <el-input v-model.trim="dialogFormInfo.password" placeholder="请输入密码校验" maxlength="20" show-password></el-input> -->
            <vxe-input style="width: 100%" v-model="dialogFormInfo.password" placeholder="密码类型" type="password"></vxe-input>
          </el-form-item>
          <el-form-item label="密码校验" prop="passwordRepeat" v-if="dialogType === 'add'">
            <el-input class="display_form" v-model.trim="dialogFormInfo.passwordRepeat" type="password"></el-input>
            <!-- <el-input v-model.trim="dialogFormInfo.passwordRepeat" placeholder="请输入密码校验" maxlength="20" show-password></el-input> -->
            <vxe-input style="width: 100%" v-model="dialogFormInfo.passwordRepeat" placeholder="密码类型" type="password"></vxe-input>
          </el-form-item>
          <el-form-item label="动态密码绑定" prop="google">
            <el-input v-model.trim="dialogFormInfo.google" disabled placeholder="请输入动态密码绑定">
              <template #append>
                <el-button type="primary" v-clipboard="dialogFormInfo.google">点击复制</el-button>
                <el-divider direction="vertical" content-position="left"></el-divider>
                <el-button type="primary" @click="generateCode">生成动态验证码</el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="角色状态" prop="state">
            <el-radio v-model="dialogFormInfo.state" :label="0">关</el-radio>
            <el-radio v-model="dialogFormInfo.state" :label="1">开</el-radio>
          </el-form-item>
          <!-- <el-form-item
            v-if="dialogType !== 'add'"
            label="动态验证码"
            prop="code"
            :rules="[
              {
                required: true,
                message: '请输入动态验证码',
                trigger: 'blur'
              },
              {
                pattern: dynamic,
                message: dynamicMsg
              }
            ]"
          >
            <el-input v-model="dialogFormInfo.code" placeholder="请输入动态验证码" />
          </el-form-item> -->
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancel">取消</el-button>
          <el-button type="primary" @click="sure">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="角色选择" v-model="dialogRoleGroupUserVisible" width="400px">
      <div class="dialog-groupuser-tree-wp">
        <el-tree
          :data="groupUserTree"
          :props="groupUserTreeDefaultProps"
          @node-click="dialogGroupUserClickHandler"
          empty-text=""
          :highlight-current="true"
          node-key="gid"
          :default-expanded-keys="[0, 1]"
        />
      </div>
    </el-dialog>
    <DynamicVerificationCode ref="dynamicVerificationCode">
      <template #vit>
        <el-button type="primary" @click="VitHanlder">验证</el-button>
      </template>
    </DynamicVerificationCode>
  </div>
</template>

<script src="./index.js"></script>

<style lang="less" scoped>
.system-account-view {
  padding: 20px;
}
// 隐藏一个form表单元素，使rules生效
.display_form {
  position: absolute;
  z-index: -10;
  height: 0;
  width: 0;
  overflow: hidden;
}
</style>
