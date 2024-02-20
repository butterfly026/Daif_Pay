import { getCurrentInstance, ref, reactive } from 'vue';

export default function useDialog() {
  const dialogVisible = ref(false);
  const dialogType = ref('add');
  const defaultDialogFormInfo = {
    username: '',
    password: '',
    passwordRepeat: '',
    state: -1,
    group_id: -1,
    gname: '',
    seamo: '' // 动态密码绑定
  };
  const dialogFormInfo = reactive(
    Object.assign({}, defaultDialogFormInfo)
  );

  const cancel = () => {
    closeDialog();
  };

  const closeDialog = () => {
    dialogVisible.value = false;
    // resetDialogForm();
  };

  const resetDialogForm = () => {
    Object.assign(dialogFormInfo, defaultDialogFormInfo);
  };

  return {
    defaultDialogFormInfo,
    dialogVisible,
    dialogType,
    dialogFormInfo,
    cancel,
    closeDialog
  };
}
