<template>
  <div class="pg-view permission-view">
    <section class="oper-row">
      <el-button type="primary" @click="operatHandler('add')">{{ t('global.increment') }}</el-button>
      <!-- <el-button type="danger" @click="operatHandler('del')">删除</el-button> -->
      <el-button type="success" @click="operatHandler('edit')">{{ t('global.edit') }}</el-button>
    </section>
    <section class="flex">
      <div class="role-tree-wp">
        <el-tree :data="roleAuthTree" :props="roleAuthTreeDefaultProps" @node-click="roleAuthClickHandler" empty-text=""></el-tree>
      </div>
      <div class="flex1 role-per-sec">
        <el-form label-width="auto" :model="rolePermissionForm">
          <!-- <el-form-item label="权限名称">
              <el-input v-model="rolePermissionForm.name" placeholder="请输入权限名称" ></el-input>
            </el-form-item>
            <el-form-item label="角色权限" class="role-p-set">
              <el-input v-model="rolePermissionForm.rolePermission" :disabled="true"></el-input>
              <el-button type="primary" @click="setting">配置</el-button>
            </el-form-item>
            <el-form-item label="数据权限">
              <el-input v-model="rolePermissionForm.module"></el-input>
            </el-form-item>
            <el-form-item label="状态">
              <el-radio v-model="rolePermissionForm.state" :label="0">关</el-radio>
              <el-radio v-model="rolePermissionForm.state" :label="1">开</el-radio>
            </el-form-item> -->

          <el-form-item :label="t('routes.system.module_name')">
            <el-input v-model="rolePermissionForm.name" :placeholder="t('routes.system.module_tips1')" :disabled="roleAuthFormCanEdit"></el-input>
          </el-form-item>
          <el-form-item :label="t('routes.system.module_router')">
            <el-input v-model="rolePermissionForm.module" :placeholder="t('routes.system.module_tips2')" :disabled="roleAuthFormCanEdit"></el-input>
          </el-form-item>
          <el-form-item :label="t('routes.system.module_up')" class="role-p-set">
            <el-input v-model="rolePermissionForm.pname" :disabled="true"></el-input>
            <el-button type="primary" @click="setting" :disabled="!isAdd" class="mgl-10">{{ t('routes.system.pz') }}</el-button>
          </el-form-item>
          <el-form-item :label="t('routes.system.module_state')">
            <el-radio v-model="rolePermissionForm.state" :label="0" :disabled="roleAuthFormCanEdit">{{ t('routes.system.close') }}</el-radio>
            <el-radio v-model="rolePermissionForm.state" :label="1" :disabled="roleAuthFormCanEdit">{{ t('routes.system.open') }}</el-radio>
          </el-form-item>
          <el-form-item
            :label="t('routes.finance_manage_page.dynamic_code')"
            prop="code"
            :rules="[
              {
                required: true,
                message: t('routes.finance_manage_page.dynamic_code_tip'),
                trigger: 'blur'
              },
              {
                pattern: dynamic,
                message: dynamicMsg
              }
            ]"
          >
            <el-input v-model="rolePermissionForm.code" :placeholder="t('routes.finance_manage_page.dynamic_code_tip')" />
          </el-form-item>
          <!-- <el-form-item :label="t('routes.system.dynamic_psw')" style="width: 700px">
            <el-input v-model.trim="rolePermissionForm.seamo" disabled :placeholder="t('routes.system.dynamic_psw_tips')" maxlength="24">
              <template #append>
                <el-button type="primary" @click="generateCodeHanlder">{{t('routes.system.create_psw')}}</el-button>
              </template>
            </el-input>
          </el-form-item> -->

          <!-- <el-form-item label="上级角色" >
              <el-select v-model="rolePermissionForm.parentRole" placeholder="请选择上线角色" :disabled="true">
              </el-select>
            </el-form-item>
            <el-form-item label="用户列表">
              <el-input type="textarea" v-model="rolePermissionForm.userList"></el-input>
            </el-form-item> -->
          <el-form-item v-if="isModify | isAdd">
            <el-button @click="cancel">{{ t('global.cancel') }}</el-button>
            <el-button type="primary" @click="submit">{{ t('global.submit') }}</el-button>
          </el-form-item>
        </el-form>
      </div>
    </section>
    <el-dialog :title="t('routes.system.role_moudel')" v-model="dialogFormVisible" width="800px">
      <!-- <header>
          <span>权限名称</span>
          <el-input class="ipt-permision-name" />
          <el-button type="primary" @click="dialogFormVisible = false">筛选</el-button>
          <el-button @click="dialogFormVisible = false">重置</el-button>
        </header> 
                    
        -->
      <div class="dialog-role-tree-wp">
        <el-tree
          :data="dialogRoleAuthTree"
          :props="dialogRoleAuthTreeDefaultProps"
          @node-click="dialogRoleAuthClickHandler"
          empty-text=""
          :highlight-current="true"
        >
          <template v-slot:default="slotProps">
            <el-radio v-model="dialogRoleTreeItemRadio" :label="slotProps.data.id">{{ slotProps.data.name }}</el-radio>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogRoleTree">{{ t('global.cancel') }}</el-button>
          <el-button type="primary" @click="sureDialogRoleTree">{{ t('global.sure') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script src="./index.ts"></script>

<style lang="scss">
@import './index.scss';
</style>
