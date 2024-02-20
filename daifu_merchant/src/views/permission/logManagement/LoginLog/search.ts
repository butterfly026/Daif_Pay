import { ref } from 'vue';
import i18n from '@/locales';
import { loginTypeAll } from '@/utils/selectOptions';
import { user_name } from '@/utils/expressions';

export default function useSearchPlaceholder(t: any) {
  const FormDataList = ref([
    {
      label: i18n.global.t('routes.system.operating_time'),
      model: 'b',
      type: 'datetimerange',
      modelNameArray: ['start_time', 'end_time'],
      rules: [{ required: true, message: ' ' }]
    },
    {
      label: i18n.global.t('global.operating_people'),
      model: 'name',
      placeholder: i18n.global.t('routes.agent.enter_account'),
      rules: [
        {
          pattern: user_name,
          message: ' '
        }
      ]
    },
    {
      type: 'select',
      label: i18n.global.t('routes.system.type'),
      options: loginTypeAll,
      model: 'flag',
      default: '0'
    }
  ]);
  return {
    FormDataList
  };
}
