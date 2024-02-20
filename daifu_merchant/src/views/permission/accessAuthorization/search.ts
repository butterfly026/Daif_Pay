import i18n from '@/locales';
import { ref } from 'vue';

export default function useSearchPlaceholder() {
  const FormDataList = ref([
    {
      label: i18n.global.t('routes.system.authorized_obj'),
      model: 'a',
      placeholder: ''
    }
  ]);
  return {
    FormDataList
  };
}
