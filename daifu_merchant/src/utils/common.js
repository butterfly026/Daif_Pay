
import _, { cloneDeep } from 'lodash';
import dayjs from 'dayjs';
import numeral from 'numeral'

/**
 * URL参数 转对象
 */
export const getUrlParameters = (url = '') => {
  const arr = url.match(/([^?=&]+)(=([^&]*))/g) || [];
  return arr.reduce((a, v) => ((a[v.slice(0, v.indexOf('='))] = v.slice(v.indexOf('=') + 1)), a), {});
};

/**
 *  get请求表单序列化
 */
export function param(data) {
  let url = '';
  for (let k in data) {
    let value = data[k] !== undefined ? data[k] : '';
    url += '&' + k + '=' + encodeURIComponent(value);
  }

  return url ? url.substring(1) : '';
}

/**
 * 日期格式化
 * date--> 可以是 时间戳 和 标准的日期格式
 * fmt 年月日时分秒 --> 'yyyy-MM-dd hh:mm:ss'
 * ex --> format(new Date(), 'yyyy-MM-dd hh:mm:ss')
 */

export function format(date, fmt) {
  date = new Date(date);
  let o = {
    'M+': date.getMonth() + 1, // 月份
    'd+': date.getDate(), // 日
    'h+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    S: date.getMilliseconds() // 毫秒
  };
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
  }
  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length));
  }
  return fmt;
}

/**
 *  复制
 */
export function copy(val) {
  const input = document.createElement('input');
  input.value = val;
  document.body.appendChild(input);
  input.select();
  document.execCommand('Copy');
  document.body.removeChild(input);
}

// 防抖
export function debounce(fn, timeout) {
  let timer = null;
  return function() {
    clearTimeout(timer);
    timer = setTimeout(() => {
      fn.call(this, arguments);
    }, timeout);
  };
}

// 节流
export function throttle(fn, wait) {
  let timer;
  return function() {
    if (!timer) {
      timer = setTimeout(() => {
        timer = null;
        fn.apply(this, arguments);
      }, wait);
    }
  };
}

// 浮点数相乘
export function numMulti(num1, num2) {
  var baseNum = 0;
  try {
    baseNum += num1.toString().split('.')[1].length;
  } catch (e) {}
  try {
    baseNum += num2.toString().split('.')[1].length;
  } catch (e) {}
  return (Number(num1.toString().replace('.', '')) * Number(num2.toString().replace('.', ''))) / Math.pow(10, baseNum);
}


/**
 * 判断是否是外部链接
 * @export
 * @param {string} path
 * @return {*}  {boolean}
 */
 export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path);
}


const compact = (arr) => arr.filter(Boolean);

/**
 * 类似path.resolve
 * @param args
 */
export const _resolve = (...args) => {
  const resolveArr = [];
  args = compact(args);
  for (let i = 0; i < args.length; i++) {
    resolveArr.push(args[i].replace('./', ''));
  }
  return resolveArr.join('/');
};


/**
 *  精确小数点
 *  l 小数点位数
 * */
 export function toFixedN(value, l = 4) {
  return stripNum(value, l);
}

// @数字精度问题
export function stripNum(num, l = 4) {
  if (!num) {
    return strip(0, l);
  }
  if (Number.isNaN(Number(num))) {
    return strip(0, l);
  }
  return (+parseFloat(Number(num).toPrecision(20))).toFixed(l);
}

export function strip(num, l = 4) {
  // if (!num && num !== 0) return;
  // return (+parseFloat(num.toPrecision(precision))).toFixed(2);
  return toFixedN2(num, l);
}

/**
 *  精确小数点
 *  l 小数点位数
 * */
 export function toFixedN2(value, l = 4) {
  if (!value) {
    return `0.${''.padEnd(l, '0')}`;
  }
  const str = value.toString().split('.');
  const str1 = str[1] ? str[1].substr(0, l).padEnd(l, '0') : ''.padEnd(l, '0');

  return str[0] + '.' + str1;
}

/**
 *  精确小数点-----报表使用
 *  l 小数点位数
 * */
 export function toFixedNReport(value, l = 4) {
  if (l == 2) {
    return strip(value, 2);
  }
  return numeral((numeral(stripNum(value, l)).value() || 0)).format('0,0.000');
}

export function listToTree(
  list,
  root = 1,
  idProp = 'gid',
  pidProp = 'pid'
) {
  const map = {};
  list.forEach((item) => {
    if (!map[item[idProp]]) {
      map[item[idProp]] = item;
    }
  });

  list.forEach((item) => {
    if (map[item[pidProp]]) {
      map[item[pidProp]].children
        ? map[item[pidProp]].children.push(item)
        : (map[item[pidProp]].children = [item]);
    }
  });

  console.error(list)

  return list.filter(item => {
    if (item[pidProp] == 0) {
      return item;
    }
  });
}

/**
 * @移除tree中state为0的对象
 * @param {*} arrList 移除对象
 * @return {*}
 */
export function ListData(arr) {
  if (arr.length > 0) {
    for (let i = arr.length - 1; i >= 0; i--) {
      if (arr[i].state == '0') {
        arr.splice(i, 1);
      } else if (arr[i].children) {
        ListData(arr[i].children);
      }
    }
  }
  return arr;
}

export function removeTreeNoState(arrList) {
  const arr = cloneDeep(arrList);
  ListData(arr);
  return arr;
}


export function shakeParams(target = {}, source = {}) {
  for (const p of Object.keys(target)) {
    target[p] = source[p];
  }
  return target;
}


/**
 * 转换ip
 * @param long number | string
 * @returns
 */
 export function long2ip(long,  isTransfer) {
  // 如果 isTransfer 不存在， 不转换 long 的值， 直接返回 long
  // if (!isTransfer) {
  //   return long;
  // }
  if (typeof long === 'string' && long.includes(':')) {
    return long;
  }
  if (typeof long === 'string' && long.includes('.')) {
    return long;
  } else {
    if (!long || long == 0) return '-';
    const MAX_IP_IN_LONG = 4294967295; // 255.255.255.255
    const MIN_IP_IN_LONG = 0; // 0.0.0.0

    if (typeof long !== 'number' || long > MAX_IP_IN_LONG || long < MIN_IP_IN_LONG) {
      return false;
    }

    const ip = [long >>> 24, (long >>> 16) & 0xff, (long >>> 8) & 0xff, long & 0xff].join('.');

    return ip;
  }
}


// 获取不同时区间的偏移量(相对于中南半岛)
export function getOffsetTime() {
  // 获取当地标准时间
  const d = new Date();
  // 获取不同时区间的偏移量
  const offsetTime = d.getTimezoneOffset() + 480;
  // 转成时间戳之后再相加
  return offsetTime * 60000;
}

/**
 * @时间戳转换
 *
 * @export
 * @param {*} value
 * @param {boolean} [isMir=true] false 就是秒， true 就是毫秒
 * @param {string} [format='YYYY-MM-DD HH:mm:ss']
 * @return {*}
 */
export function dateFormat(value, isMir = true, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!value || value === '0') {
    return '-';
  }
  if (isMir) {
    return dayjs(value + getOffsetTime()).format(format);
  } else {
    return dayjs(value * 1000 + getOffsetTime()).format(format);
  }
}



export const empty = {
  isEmpty(obj) {
    if (typeof obj === 'undefined' || obj === null || obj === '') {
      return true;
    } else {
      return false;
    }
  },
  preProcessData(data) {
    let formData = _.cloneDeep(data);
    /* 删除空值 */
    Object.keys(formData).forEach(item => {
      if (this.isEmpty(formData[item])) {
        delete formData[item];
      }
      if (_.isDate(formData[item])) {
        formData[item] = dayjs(formData[item]).format('YYYY-MM-DD HH:mm:ss');
      }
    });
    return formData;
  }
};


export const getSummaries = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '小计'
      return
    }
    const values = data.map((item) => Number(item[column.property]))
    if (!values.every((value) => isNaN(value))) {
      sums[index] = toFixedNReport(values.reduce((prev, curr) => {
        const value = Number(curr)
        if (!isNaN(value)) {
          return prev + curr
        } else {
          return prev
        }
      }, 0))
    } else {
      sums[index] = ''
    }
  })

  return sums
}