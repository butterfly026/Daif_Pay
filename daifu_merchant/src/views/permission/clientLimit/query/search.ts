import { ref } from 'vue';
import i18n from '@/locales';
export default function useSearchPlaceholder() {
  const FormDataList = ref([
    {
      label: i18n.global.t('routes.system.IP_addr'+':'),
      model: 'a',
      placeholder: i18n.global.t('routes.system.input_ipaddr_search')
    }
  ]);
  return {
    FormDataList
  };
}
