import { defineComponent, ref, reactive, toRefs, getCurrentInstance, computed, nextTick, inject } from 'vue';
import { getPrivList, getGroupList, groupInsert, groupUpdate, groupDelete } from '../../../http/apis/permission';
import { ElMessageBox  } from 'element-plus';
import { listToTree, getPriveItemByPid } from './tree';
import { listToTree as listToTreeOfRole } from '../roleModules/tree';
import _ from 'lodash';
import { dynamicMsg, dynamic } from '../../../utils/expressions';
import { generateCode } from '../systemAccount/generateCode';

export default defineComponent({
  name: 'RolePermission',
  components: {},
  setup() {
    const app = getCurrentInstance();
    const updateHanlder = inject('updateHanlder');
    const $message = app?.appContext.config.globalProperties.$message;
    const UNSETTING = "未配置";
    const ALREADY_SET = "已配置";
    const roleForm = {
      gid: -1,
      gname: '',
      noted: '',
      pname: UNSETTING,
      permission: UNSETTING,
      parentPermission: '',
      module: '',
      pid: -1,
      state: 0,
      seamo: '',
      code: ''
    };
    const formeDatas = reactive({
      rolePermissionForm: Object.assign({}, roleForm)
    });
    const isModify = ref(false);
    const isAdd = ref(false);
    // 侧边栏用户组
    const groupUserTree = reactive([]);
    const groupUserTreeDefaultProps = reactive({
      children: 'children',
      label: 'label'
    });
    // 弹出框用户组
    const dialogRoleGroupUserTree = reactive([]);
    const dialogRoleGroupUserTreeDefaultProps = reactive({
      children: 'children',
      label: 'label'
    });
    // 弹出框角色
    const dialogRoleAuthTree = reactive([]);
    const dialogRoleAuthTreeDefaultProps = reactive({
      children: 'children',
      label: 'label'
    });
    const dialogRoleGroupUserVisible = ref(false); // 用户
    const dialogRoleVisible = ref(false);
    const dialogRoleGroupUserItemRadio = ref(-1);
    const dialogRoleTreeItemRadio = ref(-1);
    const dialogRoleTreeCheckedKeys = reactive([]);
    let storePriveList = [];
    let storeDialogPriveList = [];
    let cachedDialogRoleTreeSelected = {};
    let cachedDialogTreeData = [];
    const elEltreeRole = ref(null);

    const roleAuthFormCanEdit = computed(() => {
      return !isModify.value && !isAdd.value;
    });

    const permissionStatusTxt = computed(() => {
      return formeDatas.rolePermissionForm.permission.length > 0 && formeDatas.rolePermissionForm.permission !== UNSETTING ? ALREADY_SET : UNSETTING;
    });

    // 侧边角色树导航-选中项处理
    const roleAuthClickHandler = (data) => {
      // 上级角色
      const item = getPriveItemByPid(data.pid, storePriveList);
      if (isAdd.value) {
        Object.assign(formeDatas.rolePermissionForm, {
          gid: 0,
          gname: '',
          module: '',
          pid: data.pid,
          pname: item?.gname || ''
        });
      } else {
        // 无论是否是编辑-填充表单
        Object.assign(formeDatas.rolePermissionForm, {
          gid: data.gid,
          gname: data.gname,
          pname: item?.gname || '',
          pid: data.pid,
          permission: data.permission,
          parentPermission: item?.permission,
          noted: data.noted,
          state: data.state
        });
        // 编辑-重新计算角色权限
        if (isModify.value) {
          // updateDialogRoleAuthTreeByIds(parsePermissionToIds(data.permission));
          // updateDialogRoleAuthTreeByIds(parsePermissionToIds(item?.permission || data.permission));
        }
      }
      // console.log('roleAuthClickHandler data=', formeDatas.rolePermissionForm);
    };

    // 弹出框角色树-选中项处理
    const dialogRoleGroupUserClickHandler = (data) => {
      cachedDialogRoleTreeSelected = Object.assign({}, formeDatas.rolePermissionForm, {
        gid: -1,
        pname: data.gname,
        pid: data.gid,
        permission: data.permission,
        noted: data.noted,
        state: data.state
      });
      updateDialogRoleAuthTreeByIds(parsePermissionToIds(data.permission));
      // console.log('dialogRoleGroupUserClickHandler data=', data);
    };

    // 弹出框权限树-选中项处理
    const dialogRoleAuthClickHandler = (data) => {
      cachedDialogRoleTreeSelected = Object.assign({}, formeDatas.rolePermissionForm, {
        pname: data.name,
        pid: data.id,
        state: data.state
      });
      // console.log('dialogRoleAuthClickHandler data=', data);
    };

    const parsePermissionToIds = (p) => {
      let ids = [];
      if (p !== UNSETTING && p.length > 0) {
        ids = p.split(',').map((n) => parseInt(n, 10));
      }
      return ids;
    };

    const delUser = () => {
      const params = {
        gid: formeDatas.rolePermissionForm.gid,
      }
      ElMessageBox.confirm('你确定删除该用户组吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        groupDelete(params).then((res) => {
          if (res.status) {
            $message.success("删除成功");
            isModify.value = false;
            updateHanlder();
          }
        });
      }).catch(() => {})
    }



    const operatHandler = (type) => {
      isAdd.value = false;
      isModify.value = false;
      switch (type) {
        case 'add':
          resetForm();
          isAdd.value = true;
          break;
        case 'del':
          if (formeDatas.rolePermissionForm.gid === -1) {
            $message.warning("请先选择角色");
            return;
          }
          delUser();
          break;
        case 'edit':
          if (formeDatas.rolePermissionForm.gid === -1) {
            $message.warning("请先选择角色");
            return;
          }
          isModify.value = true;
          // updateDialogRoleAuthTreeByIds(parsePermissionToIds(formeDatas.rolePermissionForm.parentPermission || formeDatas.rolePermissionForm.permission));
          break;
        default:
          break;
      }
    };

    // 重置数据
    const resetData = () => {
      isAdd.value = false;
      isModify.value = false;
      groupUserTree.splice(0, groupUserTree.length);
      formeDatas.rolePermissionForm = Object.assign({}, roleForm);
      dialogRoleAuthTree.splice(0, dialogRoleAuthTree.length);
      dialogRoleGroupUserTree.splice(0, dialogRoleGroupUserTree.length);
      resetDialogRoleTreeCheckedKeys();
    };

    const resetDialogRoleTreeCheckedKeys = () => {
      if (elEltreeRole.value) {
        elEltreeRole.value.setCheckedKeys([]);
      }
      dialogRoleTreeCheckedKeys.splice(0, dialogRoleTreeCheckedKeys.length);
    };

    const resetForm = () => {
      resetDialogRoleTreeCheckedKeys();
      Object.assign(formeDatas.rolePermissionForm, {
        gname: '',
        permission: UNSETTING,
        noted: '',
        pname: UNSETTING,
        pid: -1,
        state: 1
      });
    };

    // 修改角色权限
    const modify = () => {
      if (!isModify.value) return;
      const params = {
        pid: formeDatas.rolePermissionForm.pid,
        gid: formeDatas.rolePermissionForm.gid,
        gname: formeDatas.rolePermissionForm.gname,
        permission: formeDatas.rolePermissionForm.permission,
        noted: formeDatas.rolePermissionForm.noted,
        state: formeDatas.rolePermissionForm.state,
        seamo: formeDatas.rolePermissionForm.seamo,
        code: formeDatas.rolePermissionForm.code,
      };

      if (!validate(params)) return;
      groupUpdate(params).then((res) => {
        if (res.status) {
          $message.success("修改成功");
          isModify.value = false;
          updateHanlder();
        }
      });
    };

    const validate = (data, type) => {
      if (!data.gname) {
        $message.warning("请输入角色名称");
        return false;
      }
      if (!data.noted) {
        $message.warning("请输入角色注释");
        return false;
      }
      if (type === 'add') {
        if (data.pid === -1) {
          $message.warning("请配置上级角色");
        }
        if (data.permission === UNSETTING || data.permission.length === 0) {
          $message.warning("请配置角色权限");
          return false;
        }
      }
      if (!data.code) {
        app?.appContext.config.globalProperties.$message.warning("请输入动态验证码");
        return false;
      }
      return true;
    };

    // 插入角色权限
    const insert = () => {
      const params = {
        gname: formeDatas.rolePermissionForm.gname,
        pid: formeDatas.rolePermissionForm.pid,
        noted: formeDatas.rolePermissionForm.noted,
        permission: formeDatas.rolePermissionForm.permission,
        state: formeDatas.rolePermissionForm.state,
        seamo: formeDatas.rolePermissionForm.seamo,
        code: formeDatas.rolePermissionForm.code
      };

      //  如果pid == -2 代表选择根节点,将 pid 设置为0，0是最高级别

      if (
        params.pid == '-2'
      ) {
        params.pid = 0;
      }

      if (!validate(params, 'add')) return;
      groupInsert(params).then((res) => {
        if (res.status) {
          app?.appContext.config.globalProperties.$message.success("新增成功");
          updateHanlder();
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

    function getTreeDataListId(list, arr) {
      for (let i = 0; i < list.length; i++) {
        const id = list[i].id;
        if (id) {
          arr.push(id);
        }
        if (list[i].children && list[i].children.length) {
          getTreeDataListId(list[i].children, arr);
        }
      }
    }

    // 上级角色，角色权限设置
    const setting = (type) => {
      if (type === 'parentRole') {
        dialogRoleGroupUserVisible.value = true;
      } else {
        if (type === 'role') {
          dialogRoleVisible.value = true;
          // 设置默认选中
          const permission = formeDatas.rolePermissionForm.permission;
          let keys = [];
          if (permission.length > 0 && permission !== UNSETTING) {
            keys = permission.split(',').map(n => parseInt(n, 10));
          }
          if (keys && keys.length > 0) {
            dialogRoleTreeCheckedKeys.splice(0, dialogRoleTreeCheckedKeys.length);
            dialogRoleTreeCheckedKeys.push(...keys);
            nextTick(() => {
              if (elEltreeRole.value) {
                elEltreeRole.value.setCheckedKeys(dialogRoleTreeCheckedKeys.map(item => String(item)));
                const alist = []; // 所有的权限节点 id
                // console.error(dialogRoleTreeCheckedKeys); //  被选中的节点
                getTreeDataListId(dialogRoleAuthTree, alist);
                // 需要取消选择的 checked
                const noChecked = alist.filter(function (v) {
                  return dialogRoleTreeCheckedKeys.indexOf(Number(v)) == -1;
                });
                noChecked.forEach((v) => {
                  // 判断是否选中
                  const label = document.getElementById(`preid${v}`)?.closest('.el-tree-node__content')?.querySelector('.el-checkbox');
                  setTimeout(() => {
                    if (label.className.indexOf('is-checked') != -1) {
                      label.click();
                    }
                  });
                });
              }
            });
          }
        }
      }
      console.log('dialogRoleTreeCheckedKeys=', dialogRoleTreeCheckedKeys);
    };

    const initData = () => {
      resetData();
      getGroupList().then((res) => {
        if (res.status) {
          const data = res.data;
          // const root = data.shift();
          let treeData = [];
          let dialogGroupUserTreeData = [];
          let temp;
          data.forEach((item) => {
            temp = {
              create_at: item.create_at,
              gid: item.gid,
              lvl: item.lvl,
              lft: item.lft,
              gname: item.gname,
              noted: item.noted,
              permission: item.permission,
              rgt: item.rgt,
              state: item.state,
              label: item.gname,
              pid: parseInt(item.pid, 10)
            };
            treeData.push(Object.assign({}, temp));
            if (item.state === 1) {
              dialogGroupUserTreeData.push(Object.assign({}, temp));
            }
          });
          treeData = listToTree(treeData);
          groupUserTree.push(...treeData);
          // if (root) {
          //   data.unshift(root);
          // }
          storePriveList = data;
          storeDialogPriveList = data;
          // if (root) {
          //   data.unshift(root);
          //   dialogGroupUserTreeData.push({
          //     create_at: root.create_at,
          //     gid: root.gid,
          //     lvl: root.lvl,
          //     lft: root.lft,
          //     gname: root.gname,
          //     noted: root.noted,
          //     permission: root.permission,
          //     rgt: root.rgt,
          //     state: root.state,
          //     label: root.gname,
          //     pid: parseInt(root.pid, 10)
          //   });
          // }

          console.log('dialogGroupUserTreeData=', dialogGroupUserTreeData);
          dialogGroupUserTreeData = listToTree(dialogGroupUserTreeData, true);
          dialogRoleGroupUserTree.push(...dialogGroupUserTreeData);
          console.log('dialogGroupUserTreeData=', dialogGroupUserTreeData);
          // console.log('storePriveList=', storePriveList);
          // console.log('storeDialogPriveList=', storeDialogPriveList);
          console.log('dialogRoleAuthTree=', dialogRoleAuthTree);
        }
      });

      getPrivList().then((res) => {
        if (res.status) {
          const data = res.data;
          // const root = data.shift();
          const root = null;
          let dialogTreeData = [];
          let temp;
          data.forEach((item) => {
            temp = {
              id: item.id,
              lvl: item.lvl,
              lft: item.lft,
              module: item.module,
              name: item.name,
              rgt: item.rgt,
              state: item.state,
              label: item.name,
              docId: `preid${item.id}`,
              pid: parseInt(item.pid, 10)
            };
            if (item.state === 1) {
              dialogTreeData.push(Object.assign({}, temp));
            }
          });

          // if (root) {
          //   data.unshift(root);
          // }
          // storePriveList = data;
          // storeDialogPriveList = data;
          // if (root) {
          //   data.unshift(root);
          //   dialogTreeData.push({
          //     id: root.id,
          //     lvl: root.lvl,
          //     lft: root.lft,
          //     module: root.module,
          //     name: root.name,
          //     rgt: root.rgt,
          //     state: root.state,
          //     label: root.name,
          //     pid: parseInt(root.pid, 10)
          //   });
          // }
          cachedDialogTreeData = _.cloneDeep(dialogTreeData);
          // console.log('dialogTreeData=', dialogTreeData);
          dialogTreeData = listToTreeOfRole(dialogTreeData, 0);
          dialogRoleAuthTree.push(...dialogTreeData);
          // console.log('storePriveList=', storePriveList);
          // console.log('storeDialogPriveList=', storeDialogPriveList);
          // console.log('dialogRoleAuthTree=', dialogRoleAuthTree);
        }
      });
    };

    const updateDialogRoleAuthTreeByIds = (ids = []) => {
      let dialogTreeData = _.cloneDeep(cachedDialogTreeData);
      if (ids.length > 0) {
        dialogTreeData = dialogTreeData.filter((item) => {
          return ids.includes(Number(item.id))
        });
      }
      // let root = dialogTreeData.find((item) => item.pid === 0);
      // if (!root) {
      //   root = { id: 1, lvl: 1, lft: 1, module: 'A/B', name: "跟节点1", label: "跟节点1", rgt: 58, pid: 0, state: 1 };
      //   dialogTreeData.unshift(root);
      // }
      dialogTreeData = listToTreeOfRole(dialogTreeData, 0);

      dialogRoleAuthTree.splice(0, dialogRoleAuthTree.length);
      dialogRoleAuthTree.push(...dialogTreeData);
    };

    // 用户分组模块,角色模块 确定
    const sureDialog = (type) => {
      if (type === 'role') {
        if (elEltreeRole.value) {
          const keys = elEltreeRole.value.getCheckedKeys();
          if (keys && keys.length > 0) {
            formeDatas.rolePermissionForm.permission = keys && keys.join(',');
          }
        }
        dialogRoleVisible.value = false;
      } else {
        Object.assign(formeDatas.rolePermissionForm, cachedDialogRoleTreeSelected);
        closeDialogRoleTree();
      }
    };

    const closeDialogRoleTree = () => {
      dialogRoleVisible.value = false;
      dialogRoleGroupUserVisible.value = false;
      cachedDialogRoleTreeSelected = {};
    };

    const cancelDialogRoleTree = () => {
      dialogRoleTreeItemRadio.value = -1;
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
      dialogRoleGroupUserVisible,
      dialogRoleVisible,
      groupUserTree,
      groupUserTreeDefaultProps,
      dialogRoleGroupUserTree,
      dialogRoleGroupUserTreeDefaultProps,
      dialogRoleAuthTree,
      dialogRoleAuthTreeDefaultProps,
      dialogRoleGroupUserItemRadio,
      dialogRoleTreeItemRadio,
      dialogRoleTreeCheckedKeys,
      elEltreeRole,
      permissionStatusTxt,
      operatHandler,
      roleAuthClickHandler,
      dialogRoleGroupUserClickHandler,
      dialogRoleAuthClickHandler,
      setting,
      cancel,
      submit,
      sureDialog,
      closeDialogRoleTree,
      cancelDialogRoleTree,
      dynamicMsg,
      dynamic,
    };
  }
});
