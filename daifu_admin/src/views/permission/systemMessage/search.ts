import { ref } from 'vue';
import i18n from '@/locales';
export default function useSearchPlaceholder() {
  const FormDataList = ref([
    {
      label: i18n.global.t('routes.system.msg_subject')+':',
      model: 'a',
      placeholder: i18n.global.t('routes.system.msg_subject_tips'),
    },
    {
      label: i18n.global.t('routes.system.msg_content')+':',
      model: 'b',
      placeholder: i18n.global.t('routes.system.msg_content_tips'),
      width:'335px'
    },
    {
      label: i18n.global.t('routes.system.send_time')+':',
      model: 'c',
      type: 'datetimerange',
      modelNameArray: ['start_time', 'end_time']
    }
  ]);
  return {
    FormDataList
  };
}
