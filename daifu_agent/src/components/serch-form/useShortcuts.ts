import { ref } from 'vue';
import dayjs from 'dayjs';
export function timerChange(e, isRecent = false) {
  const d = new Date();
  const startDate = dayjs(d)
    .startOf('day')
    .add(-(e - 1), 'days')
    .valueOf();
  const endDate = dayjs(d).endOf('day').add(0, 'days').valueOf();
  if (isRecent) {
    const end = dayjs(d).endOf('day').add(-1, 'days').valueOf();
    return [startDate, end];
  }
  return [startDate, endDate];
}

export default function useShortcuts() {
  const shortcuts = ref([
    {
      text: "今天",
      value: (() => {
        return timerChange(1);
      })()
    },
    {
      text: "昨天",
      value: (() => {
        return timerChange(2, true);
      })()
    },
    {
      text: "最近一周",
      value: (() => {
        return timerChange(7);
      })()
    },
    {
      text: "最近两周",
      value: (() => {
        return timerChange(14);
      })()
    },
    {
      text: "最近一个月",
      value: (() => {
        return timerChange(30);
      })()
    },
    {
      text: "最近三个月",
      value: (() => {
        return timerChange(90);
      })()
    },
    {
      text: "最近一年",
      value: (() => {
        return timerChange(365);
      })()
    }
  ]);
  return { shortcuts };
}
