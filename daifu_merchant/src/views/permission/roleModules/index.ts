import { defineComponent, ref, reactive, toRefs, getCurrentInstance, computed } from 'vue';
import { getPrivList, privUpdate, privInsert } from '@/http/modules/permission';
import { ElTree } from 'element-plus';
import { listToTree, getPriveItemByPid } from './tree';
import { ResponsePriveListItem, PriveListItem } from './types';
import { generateCode } from '../systemAccount/generateCode';
import { useI18n } from 'vue-i18n';
import { dynamicMsg, dynamic } from '@/utils/expressions';

export default defineComponent({
  name: 'RoleModules',
  components: {},
  setup() {
    const { t } = useI18n({ useScope: 'global' });
    const app = getCurrentInstance();
    const roleForm = {
      id: -1,
      name: '',
      pname: t('routes.system.wpz'),
      rolePermission: '',
      module: '',
      pid: -1,
      userList: '',
      state: 0,
      seamo: '',
      code: ''
    };
    const formeDatas = reactive({
      rolePermissionForm: Object.assign({}, roleForm)
    });
    const isModify = ref(false);
    const isAdd = ref(false);
    const roleAuthTree = reactive<any>([]);
    const roleAuthTreeDefaultProps = reactive({
      children: 'children',
      label: 'label'
    });
    const dialogRoleAuthTree = reactive<any>([]);
    const dialogRoleAuthTreeDefaultProps = reactive({
      children: 'children',
      label: 'label'
    });
    const ruleFomes = reactive({
      rules: {
        name: [{ required: true, message: t('routes.system.module_tips1'), trigger: 'blur' }]
      }
    });
    const dialogFormVisible = ref(false);
    const dialogRoleTreeItemRadio = ref(-1);
    let storePriveList: ResponsePriveListItem[] = [];
    let storeDialogPriveList: ResponsePriveListItem[] = [];
    let cachedDialogRoleTreeSelected: PriveListItem | ObjectParams = {};

    const roleAuthFormCanEdit = computed(() => {
      return !isModify.value && !isAdd.value;
    });
    console.log('roleAuthFormCanEdit=', roleAuthFormCanEdit.value);

    const roleAuthClickHandler = (data: typeof ElTree) => {
      const item: ResponsePriveListItem | undefined = getPriveItemByPid(data.pid, storePriveList);
      if (isAdd.value) {
        Object.assign(formeDatas.rolePermissionForm, { id: 0, name: '', module: '', pid: data.pid, pname: item?.name || '' });
      } else {
        Object.assign(formeDatas.rolePermissionForm, {
          id: data.id,
          name: data.name,
          pname: item?.name || '',
          module: data.module,
          pid: data.pid,
          state: data.state
        });
      }
    };

    const dialogRoleAuthClickHandler = (data: typeof ElTree) => {
      cachedDialogRoleTreeSelected = Object.assign({}, formeDatas.rolePermissionForm, { pname: data.name, pid: data.id, state: data.state });
      // Object.assign(formeDatas.rolePermissionForm, {pname: data.name, pid: data.id, state: data.state});
      // console.log('dialogRoleAuthClickHandler data=', data);
      // console.log('dialogRoleAuthClickHandler rolePermissionForm=', formeDatas.rolePermissionForm);
    };

    const operatHandler = (type: string) => {
      isAdd.value = false;
      isModify.value = false;
      switch (type) {
        case 'add':
          // Object.assign(formeDatas.rolePermissionForm, {name: '', module: '', pname: '未配置', pid: -1, state: 1});
          resetForm();
          isAdd.value = true;
          break;
        case 'del':
          break;
        case 'edit':
          if (formeDatas.rolePermissionForm.id === -1) {
            app?.appContext.config.globalProperties.$message.warning(t('routes.system.select_tips'));
            return;
          }
          isModify.value = true;
          break;
        default:
          break;
      }
    };

    // 重置数据
    const resetData = () => {
      isAdd.value = false;
      isModify.value = false;
      roleAuthTree.splice(0, roleAuthTree.length);
      formeDatas.rolePermissionForm = Object.assign({}, roleForm);
      dialogRoleAuthTree.splice(0, dialogRoleAuthTree.length);
    };

    const resetForm = () => {
      Object.assign(formeDatas.rolePermissionForm, { name: '', module: '', pname: t('routes.system.wpz'), pid: -1, state: 1 });
    };

    // 修改角色权限
    const modify = () => {
      if (!isModify.value) return;
      isModify.value = false;
      const params = {
        id: formeDatas.rolePermissionForm.id,
        name: formeDatas.rolePermissionForm.name,
        module: formeDatas.rolePermissionForm.module,
        pid: formeDatas.rolePermissionForm.pid,
        state: formeDatas.rolePermissionForm.state,
        seamo: formeDatas.rolePermissionForm.seamo,
        code: formeDatas.rolePermissionForm.code
      };
      if (!validate(params)) return;
      privUpdate(params).then((res: any) => {
        if (res.status) {
          initData();
          app?.appContext.config.globalProperties.$message.success(t('routes.tips.modify_suc'));
        }
      });
    };

    const validate = (data: any, type?: string) => {
      if (!data.name) {
        app?.appContext.config.globalProperties.$message.warning(t('routes.system.module_tips1'));
        return false;
      }
      if (!data.module) {
        app?.appContext.config.globalProperties.$message.warning(t('routes.system.module_tips2'));
        return false;
      }
      if (type === 'add') {
        if (data.pid === -1) {
          app?.appContext.config.globalProperties.$message.warning(t('routes.system.module_tips3'));
          return false;
        }
      }
      if (!data.code) {
        app?.appContext.config.globalProperties.$message.warning(t('routes.finance_manage_page.dynamic_code_tip'));
        return false;
      }
      return true;
    };

    // 插入角色权限
    const insert = () => {
      const params = {
        name: formeDatas.rolePermissionForm.name,
        module: formeDatas.rolePermissionForm.module,
        pid: formeDatas.rolePermissionForm.pid,
        state: formeDatas.rolePermissionForm.state,
        seamo: formeDatas.rolePermissionForm.seamo,
        code: formeDatas.rolePermissionForm.code
      };
      if (!validate(params, 'add')) return;
      privInsert(params).then((res: any) => {
        if (res.status) {
          initData();
          app?.appContext.config.globalProperties.$message.success(t('global.add_suc'));
        }
      });
    };

    const cancel = () => {
      isAdd.value = false;
      isModify.value = false;
      cancelDialogRoleTree();
      resetForm();
    };

    const submit = () => {
      if (isModify.value) {
        modify();
      } else {
        if (isAdd.value) {
          insert();
        }
      }
    };

    const setting = () => {
      dialogFormVisible.value = true;
    };

    const initData = () => {
      resetData();
      getPrivList().then((res: any) => {
        if (res.status) {
          const data: ResponsePriveListItem[] = res.data;
          const root = data.shift();
          let treeData: PriveListItem[] = [];
          let dialogTreeData: PriveListItem[] = [];
          let temp: PriveListItem;
          data.forEach((item: ResponsePriveListItem) => {
            temp = {
              id: item.id,
              lvl: item.lvl,
              lft: item.lft,
              module: item.module,
              name: item.name,
              rgt: item.rgt,
              state: item.state,
              label: item.name,
              pid: parseInt(item.pid, 10)
            };
            treeData.push(Object.assign({}, temp));
            if (item.state === 1) {
              dialogTreeData.push(Object.assign({}, temp));
            }
          });
          // console.log('treeData=', treeData);
          treeData = listToTree(treeData);
          roleAuthTree.push(...treeData);
          if (root) {
            data.unshift(root);
          }
          storePriveList = data;
          storeDialogPriveList = data;
          if (root) {
            data.unshift(root);
            dialogTreeData.push({
              id: root.id,
              lvl: root.lvl,
              lft: root.lft,
              module: root.module,
              name: root.name,
              rgt: root.rgt,
              state: root.state,
              label: root.name,
              pid: parseInt(root.pid, 10)
            });
          }

          // console.log('dialogTreeData=', dialogTreeData);
          dialogTreeData = listToTree(dialogTreeData, 0);
          dialogRoleAuthTree.push(...dialogTreeData);

          // console.log('storePriveList=', storePriveList);
          // console.log('storeDialogPriveList=', storeDialogPriveList);
          // console.log('dialogRoleAuthTree=', dialogRoleAuthTree);
        }
      });
    };

    const sureDialogRoleTree = () => {
      Object.assign(formeDatas.rolePermissionForm, cachedDialogRoleTreeSelected);
      closeDialogRoleTree();
    };

    const closeDialogRoleTree = () => {
      dialogFormVisible.value = false;
      cachedDialogRoleTreeSelected = {};
    };

    const cancelDialogRoleTree = () => {
      dialogRoleTreeItemRadio.value = -1;
      // resetForm();
      closeDialogRoleTree();
    };

    initData();

    function generateCodeHanlder() {
      formeDatas.rolePermissionForm.seamo = generateCode();
    }
    return {
      isAdd,
      isModify,
      generateCodeHanlder,
      roleAuthFormCanEdit,
      ...toRefs(formeDatas),
      ...toRefs(ruleFomes),
      dialogFormVisible,
      roleAuthTree,
      roleAuthTreeDefaultProps,
      dialogRoleAuthTree,
      dialogRoleAuthTreeDefaultProps,
      dialogRoleTreeItemRadio,
      t,
      operatHandler,
      roleAuthClickHandler,
      dialogRoleAuthClickHandler,
      setting,
      cancel,
      submit,
      sureDialogRoleTree,
      closeDialogRoleTree,
      cancelDialogRoleTree,
      dynamicMsg,
      dynamic
    };
  }
});
