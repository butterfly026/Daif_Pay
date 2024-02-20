import { nextTick } from 'vue';
import { toFixedN, toFixedNReport } from '@/utils/int';

/**
 *
 * @param createExcluded 需要不展示的prop数组
 * @param cloumBai 需要展示百分号的prop数组
 * @param count 需要使用外面值的数组
 * @returns
 */
export const getSummaries = (createExcluded = [], cloumBai = [], count = [], notFixed = []) => {
  return (param) => {
    const { columns, data } = param;
    const sums = [];
    columns.forEach((column, index) => {
      if (index === 0) {
        sums[index] = count.length ? "总计" : "小计";
        return;
      }
      const c = count.find((item) => item.prop === column.property);
      if (c) {
        if (c.toFixed === undefined) {
          sums[index] = toFixedN(c.val);
        } else {
          sums[index] = c.val;
        }
        return;
      }

      if (createExcluded.includes(column.property)) {
        return;
      }

      const values = data.map((item) => Number(item[column.property]));
      if (!values.every((value) => isNaN(value))) {
        sums[index] = values.reduce((prev, curr) => {
          const value = Number(curr);
          if (!isNaN(value)) {
            return prev + curr;
          } else {
            return prev;
          }
        }, 0);
        if (cloumBai.includes(column.property)) {
          sums[index] = (sums[index] * 100).toFixed(4) + '%';
        } else {
          if (notFixed.includes(column.property)) {
            sums[index] = sums[index].toFixed(0);
          } else {
            sums[index] = toFixedN(sums[index]);
          }
        }
      } else {
        sums[index] = '';
      }
    });
    return sums;
  };
};


export default function tableStyle() {
  nextTick(() => {
    const arr = Array.from(document.querySelectorAll('.el-table__footer-wrapper tbody tr td'));
    arr.forEach(item => {
      item.className += ' nscss_get_blank_style';
    });
  });
  return {};
}

export function indexMethod(index, page, page_size) {
  return (page - 1) * page_size + 1;
  return (page - 1) * page_size + index + 1;
}
