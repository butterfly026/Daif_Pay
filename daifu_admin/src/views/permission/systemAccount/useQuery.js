import { getCurrentInstance, ref, reactive } from 'vue';

export default function useQuery() {
  const defaultData = {
    name: '',
    state: -1,
    gname: '',
    groupid: -1,
    page: 1
  }
  const queryForm = reactive(Object.assign({}, defaultData));

  const reset = () => {
    Object.assign(queryForm, defaultData);
  }

  return {
    queryForm,
    reset
  }
}
