<template>
  <div class="pg-view permission-view">
    <section class="oper-row">
      <el-button type="primary" @click="operatHandler('add')">新增</el-button>
      <el-button type="success" @click="operatHandler('edit')">编辑</el-button>
      <el-button type="danger" @click="operatHandler('del')">删除</el-button>
    </section>
    <section class="flex">
      <div class="role-tree-wp">
        <el-tree :data="groupUserTree" :props="groupUserTreeDefaultProps" @node-click="roleAuthClickHandler" empty-text=""></el-tree>
      </div>
      <div class="flex1 role-per-sec">
        <el-form label-width="auto" :model="rolePermissionForm">
          <el-form-item label="角色名称">
            <el-input v-model="rolePermissionForm.gname" placeholder="请输入角色名称" :disabled="roleAuthFormCanEdit" maxlength="24"></el-input>
          </el-form-item>
          <el-form-item label="上级角色" class="role-p-set">
            <el-input v-model="rolePermissionForm.pname" :disabled="true"></el-input>
            <el-button type="primary" @click="setting('parentRole')" :disabled="!isAdd" class="mgl-10">配置</el-button>
          </el-form-item>
          <el-form-item label="角色权限" class="role-p-set">
            <el-input v-model="permissionStatusTxt" :disabled="true"></el-input>
            <el-button type="primary" @click="setting('role')" :disabled="roleAuthFormCanEdit" class="mgl-10">配置</el-button>
          </el-form-item>
          <el-form-item label="角色注释">
            <el-input type="textarea" v-model="rolePermissionForm.noted" maxlength="24"></el-input>
          </el-form-item>
          <el-form-item label="角色状态">
            <el-radio v-model="rolePermissionForm.state" :label="0" :disabled="roleAuthFormCanEdit">关</el-radio>
            <el-radio v-model="rolePermissionForm.state" :label="1" :disabled="roleAuthFormCanEdit">开</el-radio>
          </el-form-item>
          <!-- <el-form-item :label="t('routes.system.dynamic_psw')" style="width: 700px">
            <el-input v-model.trim="rolePermissionForm.seamo" disabled :placeholder="t('routes.system.dynamic_psw_tips')" maxlength="24">
              <template #append>
                <el-button type="primary" @click="generateCodeHanlder">{{ t('routes.system.create_psw') }}</el-button>
              </template>
            </el-input>
          </el-form-item> -->
          <el-form-item
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
            <el-input v-model="rolePermissionForm.code" placeholder="请输入动态验证码" />
          </el-form-item>
          <el-form-item v-if="isModify | isAdd">
            <el-button @click="cancel">取消</el-button>
            <el-button type="primary" @click="submit">提交</el-button>
          </el-form-item>
        </el-form>
      </div>
    </section>
    <el-dialog title="用户分组模块" v-model="dialogRoleGroupUserVisible" width="800px">
      <div class="dialog-role-tree-wp">
        <el-tree
          :data="dialogRoleGroupUserTree"
          :props="dialogRoleGroupUserTreeDefaultProps"
          @node-click="dialogRoleGroupUserClickHandler"
          empty-text=""
          :highlight-current="true"
        >
          <template v-slot:default="slotProps">
            <el-radio v-model="dialogRoleGroupUserItemRadio" :label="slotProps.data.gid">{{ slotProps.data.gname }}</el-radio>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogRoleTree">取消</el-button>
          <el-button type="primary" @click="sureDialog">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="权限模块" v-model="dialogRoleVisible" width="800px">
      <div class="dialog-role-tree-wp">
        <el-tree
          :data="dialogRoleAuthTree"
          :props="dialogRoleAuthTreeDefaultProps"
          @node-click="dialogRoleAuthClickHandler"
          empty-text=""
          :render-after-expand="false"
          :highlight-current="true"
          show-checkbox
          node-key="id"
          ref="elEltreeRole"
        >
          <template v-slot:default="slotProps"> {{ slotProps.data.label }}<span :id="slotProps.data.docId"></span> </template>
        </el-tree>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogRoleTree">取消</el-button>
          <el-button type="primary" @click="sureDialog('role')">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script src="./index.js"></script>

<style lang="less">
@import './index.less';
</style>
