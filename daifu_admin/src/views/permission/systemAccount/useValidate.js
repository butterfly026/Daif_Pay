import { reactive, getCurrentInstance } from 'vue';
import { syspsw, syspswMsg, adminPassword, syspswUserNameMsg } from '../../../utils/expressions';

export default function useValidate() {
  const app = getCurrentInstance();
  const $message = app?.appContext.config.globalProperties.$message;

  const validateGroupUser = (rule, value, callback) => {
    callback();
  };

  const formRules = reactive({
    username: [
      { required: true, message: "请输入账号", trigger: 'blur' },
      { pattern: syspsw, message: syspswUserNameMsg, trigger: 'blur' }
      // { max: 24, message: '长度在 2 到 24个字符', trigger: 'blur' }
    ],
    group_id: [
      {
        required: true,
        message: "请选择角色",
        trigger: 'blur',
        validator: validateGroupUser
      }
    ],
    password: [
      { required: true, message: "请输入登录密码", trigger: 'blur' },
      { pattern: adminPassword, message: syspswMsg, trigger: 'blur' }
    ],
    passwordRepeat: [
      { required: true, message: "请输入密码校验", trigger: 'blur' },
      { pattern: adminPassword, message: syspswMsg, trigger: 'blur' }
    ],
    seamo: [{ required: true, message: "请输入动态密码绑定", trigger: 'blur' }],
    state: [{ required: true, message: "请选择角色状态", trigger: 'blur' }]
  });

  const validateForm = (formInfo) => {
    if (formInfo.group_id === -1) {
      $message.warning("请选择角色");
      return false;
    }
    if (formInfo.state === -1) {
      $message.warning("请选择角色状态");
      return false;
    }
    if (formInfo.password !== formInfo.passwordRepeat) {
      $message.warning("您输入的密码不一致");
      return false;
    }
    return true;
  };

  return {
    formRules,
    validateForm
  };
}
