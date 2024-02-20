import { getCurrentInstance } from 'vue';
import i18n from '@/locales/index';

const UNSETTING = i18n.global.t('routes.system.wpz');
export default function userValidate() {
  const app = getCurrentInstance();
  const $message = app?.appContext.config.globalProperties.$message;
  const validate = (data: any, type?: string) => {
    if (!data.gname) {
      $message.warning(i18n.global.t('routes.system.role_tips1'));
      return false;
    }
    if (!data.noted) {
      $message.warning(i18n.global.t('routes.system.role_tips2'));
      return false;
    }
    if (!data.code) {
      $message.warning(i18n.global.t('routes.finance_manage_page.dynamic_code_tip'));
      return false;
    }
    if (type === 'add') {
      if (data.pid === -1) {
        $message.warning(i18n.global.t('routes.system.role_tips3'));
      }
      if (data.permission === UNSETTING || data.permission.length === 0) {
        $message.warning(i18n.global.t('routes.system.role_tips4'));
        return false;
      }
    }
    return true;
  };
  return {
    validate
  };
}
