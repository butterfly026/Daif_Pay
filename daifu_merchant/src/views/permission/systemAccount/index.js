import { defineComponent, ref, reactive, computed, getCurrentInstance } from 'vue';
import { addSystemAccount, editSystemAccount, getSystemAccountList, updateSystemAccountState, getGroupList } from '../../../http/apis/permission';
import useQuery from './useQuery';
import useDialog from './useDialog';
import useValidate from './useValidate';
import { shakeParams, listToTree, long2ip, removeTreeNoState, dateFormat } from '../../../utils/common';
import { ElForm, ElTree, ElMessageBox, ElMessage } from 'element-plus';
import { useStore } from 'vuex';
import base32Encode from 'base32-encode';
import _ from 'lodash';
import { empty } from '../../../components/serch-form/empty';
import { dynamicMsg, dynamic } from '../../../utils/expressions';
import DynamicVerificationCode from '../../../components/dynamic-verification-code/index.vue';

const dialogTypes = new Map([
  ['add', "新增"],
  ['edit', "编辑"]
]);

export default defineComponent({
  name: 'SystemAccount',
  components: { DynamicVerificationCode },
  setup() {
    const app = getCurrentInstance();
    const $message = app?.appContext.config.globalProperties.$message;
    let tableData = ref([]);
    const store = useStore();
    const stateList = reactive([
      { label: "请选择", value: -1 },
      { label: "启用", value: 1 },
      { label: "禁用", value: 0 }
    ]);
    const groupUserTree = reactive([]);
    const groupUserTreeDefaultProps = reactive({
      children: 'children',
      label: 'label'
    });
    const curPage = ref(1);
    const totalCount = ref(0);
    const pageSize = ref(10);
    const groupUserList = reactive([
      {
        gid: -1,
        label: "请选择角色",
        gname: '',
        create_at: 0,
        lvl: 0,
        lft: 0,
        noted: '',
        rgt: -1,
        pid: -1,
        state: -1,
        permission: ''
      }
    ]);
    const groupUserMap = reactive(new Map());
    const elDialogForm = ref(null);
    const dialogRoleGroupUserVisible = ref(false);
    const groupUserDialogOpenFrom = ref('');
    const { queryForm, reset } = useQuery();
    const {
      // defaultDialogFormInfo,
      dialogVisible,
      dialogType,
      dialogFormInfo,
      cancel,
      closeDialog
    } = useDialog();
    const { formRules, validateForm } = useValidate();

    const dialogTitle = computed(() => dialogTypes.get(dialogType.value));
    const totalPage = computed(() => Math.ceil(totalCount.value / pageSize.value));

    const addAccount = () => {
      Object.assign(dialogFormInfo, {
        username: '',
        password: '',
        passwordRepeat: '',
        state: 1,
        group_id: -1,
        gname: '',
        google: ''
      });
      dialogVisible.value = true;
      dialogType.value = 'add';
    };

    const initTableData = () => {
      getList().then((res) => {
        if (res.status) {
          if (res.data.total) {
            totalCount.value = Number(res.data.total);
          }
        }
      });
    };

    // 获取账号列表
    const getList = () => {
      const params = {
        page: curPage.value,
        page_size: pageSize.value
      };
      if (queryForm.username) {
        params.username = queryForm.username;
      }
      if (queryForm.state !== -1) {
        params.state = queryForm.state;
      }
      if (queryForm.groupid !== -1) {
        params.group_id = queryForm.groupid;
      }
      if (!queryForm.gname) {
        params.group_id = '';
      }
      return getSystemAccountList(empty.preProcessData(params)).then((res) => {
        tableData.value = [];
        if (res.status) {
          if (res.data && res.data.d && Array.isArray(res.data.d)) {
            tableData.value = res.data.d;
          }
        }
        return res;
      });
    };

    const query = () => {
      curPage.value = 1;
      totalCount.value = 0;
      getList().then((res) => {
        if (res.status) {
          if (res.data.total) {
            totalCount.value = Number(res.data.total);
          }
        }
      });
    };

    const add = () => {
      const params = shakeParams(
        {
          username: '',
          // password: '',
          state: -1,
          group_id: -1,
          google: '' // 动态密码绑定
        },
        dialogFormInfo
      );
      params.password = dialogFormInfo.password;
      params.username = params.username;
      delete params.name;
      addSystemAccount(params).then((res) => {
        if (res.status) {
          initTableData();
          closeDialog();
          $message.success("添加成功");
        }
      });
    };
    const editFn = (par) => {
      const params = {
        password: par.password, //
        group_id: par.group_id, //
        state: par.state,
        google: par.google, //
        uid: par.uid,
        // code: par.code
      };
      editSystemAccount(empty.preProcessData(params)).then((res) => {
        if (res.status) {
          initTableData();
          closeDialog();
          $message.success("修改成功");
        }
      });
    };

    // 点击编辑处理
    const edit = (info) => {
      // 把动态密码清空
      // info.seamo = '';
      dialogVisible.value = true;
      dialogType.value = 'edit';
      Object.assign(shakeParams(dialogFormInfo, info));
      dialogFormInfo.group_id = parseInt(info.group_id, 10);
      dialogFormInfo.gname = getGroupIdText(parseInt(info.group_id, 10));
      dialogFormInfo.uid = info.uid;
    };

    const getGroupIdText = (group_id) => {
      const obj = gidList.value.find((it) => it.gid == group_id);
      return obj ? obj.gname : '';
    };

    const updateAccountState = (info) => {
      const params = {
        id: info.id,
        state: info.state
      };
      if (!params.id) {
        return;
      }
      updateSystemAccountState(params).then((res) => {
        if (res.status) {
          $message.success("修改成功");
        }
      });
    };

    const gidList = ref([]);

    // 获取角色列表
    getGroupList().then((res) => {
      if (res.status) {
        const data = res.data;
        // data.shift();
        gidList.value = _.cloneDeep(data);
        let treeData = [];
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
          groupUserMap.set(item.gid + '', item.gname);
        });
        groupUserList.push(...treeData);
        treeData = listToTree(treeData, 1, 'gid');
        console.log('---------------------', treeData);
        console.log(removeTreeNoState(treeData));
        groupUserTree.push(...removeTreeNoState(treeData));
        // console.log('groupUserTree=', groupUserTree);
        // console.log('groupUserList=', groupUserList, groupUserMap);
      }
    });

    const sure = () => {
      if (dialogType.value === 'add') {
        console.log(elDialogForm.value)
        elDialogForm.value.validate((valid) => {
          if (valid) {
            if (!validateForm(dialogFormInfo)) return;
            add();
          } else {
            console.log("验证未通过")
          }
        });
      } else {
        elDialogForm.value && elDialogForm.value.validateField('code');
        setTimeout(() => {
          // 编辑
          if (!document.querySelector('.dialog-ctx-wp')?.querySelector('.el-form-item__error')) {
            editFn(dialogFormInfo);
          }
        }, 20);
        //
        // if (elDialogForm.value && elDialogForm.value.validate) {
        //   elDialogForm.value.validate((valid: boolean) => {
        //     if (valid) {
        //       if (!validateForm(dialogFormInfo)) return;
        //       editFn(dialogFormInfo);
        //     }
        //   });
        // }
        // closeDialog();
      }
    };

    const closedDialog = () => {
      if (elDialogForm.value) {
        elDialogForm.value.clearValidate();
      }
      closeDialog();
    };

    const triggerShowGroupUserDialog = (type) => {
      groupUserDialogOpenFrom.value = type;
      dialogRoleGroupUserVisible.value = true;
    };

    // 弹出框角色树-选中项处理
    const dialogGroupUserClickHandler = (data) => {
      switch (groupUserDialogOpenFrom.value) {
        case 'dialog':
          dialogFormInfo.gname = data.gname;
          dialogFormInfo.group_id = data.gid;
          break;
        case 'query':
          queryForm.gname = data.gname;
          queryForm.groupid = data.gid;
          break;
        default:
          break;
      }
      dialogRoleGroupUserVisible.value = false;
    };

    const pagePrev = () => {
      // curPage.value--;
      // getList({ page: curPage.value });
    };

    const pageNext = () => {
      // curPage.value++;
      // getList({ page: curPage.value });
    };

    const gotoPage = (page) => {
      curPage.value = page;
      getList();
    };

    const sizesChangeHandler = (sizes) => {
      pageSize.value = sizes;
      getList();
    };

    initTableData();

    function randomWord(randomFlag, min, max) {
      let str = '',
        range = min,
        arr = [
          '0',
          '1',
          '2',
          '3',
          '4',
          '5',
          '6',
          '7',
          '8',
          '9',
          'a',
          'b',
          'c',
          'd',
          'e',
          'f',
          'g',
          'h',
          'i',
          'j',
          'k',
          'l',
          'm',
          'n',
          'o',
          'p',
          'q',
          'r',
          's',
          't',
          'u',
          'v',
          'w',
          'x',
          'y',
          'z',
          'A',
          'B',
          'C',
          'D',
          'E',
          'F',
          'G',
          'H',
          'I',
          'J',
          'K',
          'L',
          'M',
          'N',
          'O',
          'P',
          'Q',
          'R',
          'S',
          'T',
          'U',
          'V',
          'W',
          'X',
          'Y',
          'Z'
        ];

      // 随机产生
      if (randomFlag) {
        range = Math.round(Math.random() * (max - min)) + min;
      }
      for (let i = 0; i < range; i++) {
        let pos = Math.round(Math.random() * (arr.length - 1));
        str += arr[pos];
      }
      return str;
    }
    // 生成动态验证码，包含登录用户名
    // 获取用户名和用户名长度
    // 将用户名改成数组
    function getNameInfo() {
      const name = store.state?.user?.name || "";
      const len = name.length;
      const nameArr = name.split('');
      return {
        len,
        nameArr
      };
    }

    // 向字符串中插入值
    function insertStr(soure, start, newStr) {
      return soure.slice(0, start) + newStr + soure.slice(start);
    }

    function stringToUint8Array(str) {
      let arr = [];
      for (let i = 0, j = str.length; i < j; ++i) {
        arr.push(str.charCodeAt(i));
      }

      let tmpUint8Array = new Uint8Array(arr);
      return tmpUint8Array;
    }
    const generateCode = () => {
      const l = 32;
      const obj = getNameInfo();
      const len = obj.len;
      const arr = obj.nameArr;
      let randomStr = randomWord(false, l - len, l - len);
      // 把nameArr随机插入到randomStr中
      arr.forEach((item) => {
        // 生成随机数
        const w = Number.parseInt((Math.random() * (l - len)).toString());
        randomStr = insertStr(randomStr, w, item);
      });
      // console.log(base32Encode(stringToUint8Array(randomStr), 'Crockford'));
      // console.log(base32Encode(stringToUint8Array(randomStr), 'RFC3548'));
      // console.log(base32Encode(stringToUint8Array(randomStr), 'RFC4648'));
      // console.log(base32Encode(stringToUint8Array(randomStr), 'RFC4648-HEX'));
      dialogFormInfo.google = base32Encode(stringToUint8Array(randomStr), 'RFC3548');
      // dialogFormInfo.seamo = randomStr;
    };

    const deleteAccount = () => {
      ElMessageBox.confirm("您确认删除此账号吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: 'warning'
      })
        .then(() => {})
        .catch(() => {});
    };
    const dynamicVerificationCode = ref();
    let rowData = {};
    function editState(row) {
      rowData = row;
      // channelID.value = row.id;
      const txt = row.state == 1 ? "禁用" : "启用";
      if (dynamicVerificationCode.value) {
        dynamicVerificationCode.value.showForm(true, {
          title: txt,
          content: `您确定要${txt}吗？`
        });
      }
    }
    // 验证码查询
    const VitHanlder = () => {
      dynamicVerificationCode.value.submitmBtn().then((code) => {
        editSystemAccount({
          group_id: rowData.group_id,
          id: rowData.id,
          state: rowData.state == 1 ? 0 : 1,
          code
        }).then((res) => {
          if (res.status) {
            $message.success("修改成功");
            initTableData();
            closeDialog();
            // 对接接口，成功关闭弹框
            dynamicVerificationCode.value.cancel();
          }
        });
        // ElMessage.success(t('routes.tips.modify_suc'));
        //       getList();
        //       // 对接接口，成功关闭弹框
        //       dynamicVerificationCode.value.cancel();
      });
    };
    return {
      dynamicVerificationCode,
      VitHanlder,
      editState,
      queryForm,
      tableData,
      stateList,
      dialogVisible,
      dialogType,
      dialogFormInfo,
      dialogTypes,
      dialogTitle,
      groupUserList,
      curPage,
      groupUserMap,
      totalCount,
      pageSize,
      totalPage,
      formRules,
      elDialogForm,
      dialogRoleGroupUserVisible,
      groupUserTree,
      groupUserTreeDefaultProps,
      addAccount,
      query,
      reset,
      sure,
      cancel,
      edit,
      dateFormat,
      updateAccountState,
      closeDialog,
      closedDialog,
      gotoPage,
      pagePrev,
      pageNext,
      sizesChangeHandler,
      long2ip,
      dialogGroupUserClickHandler,
      triggerShowGroupUserDialog,
      generateCode,
      deleteAccount,
      dynamicMsg,
      dynamic,
    };
  }
});
