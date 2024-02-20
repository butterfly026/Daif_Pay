import Event from '../event';
import { KEYS, encryptQuery } from './xxtea';
import { murmurhash3_32_gc } from './murmurhash3';
import store from '../store';
import dayjs from 'dayjs';

export const defaultOptions = {
  method: 'get',
  url: '',
  data: {},
  loading: false,
  toastError: true,
  timeout: 12000,
  headers: {},
  responseType: 'text',
  sign: true, // 默认不加签
};

let timer = null;
function clearTimer() {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
}

export function setHeaders(xhr, headers = {}) {
  for (const key in headers) {
    xhr.setRequestHeader(key, headers[key]);
  }

  xhr.setRequestHeader('t', store.getters.userInfo.token);
  xhr.setRequestHeader('v', VERSION);
  xhr.setRequestHeader('d', '38');
}

// 合并请求参数
export function mergeOptions(options = {}, default_options = {}) {
  const option = {};
  for (const key in options) {
    const o_value = options[key];
    const d_value = default_options[key] || {};
    // 如果options中的参数 在默认中有并且是对象则合并
    if (o_value && typeof o_value === 'object') {
      option[key] = { ...d_value, ...o_value };
    }
  }

  return { ...defaultOptions, ...options, ...option };
}

export function startLoading() {
  clearTimer();

  timer = setTimeout(() => {
    Event.emit('loading', { loading: true, text: 'is loading' });
  }, 200);
}
export function closeLoading() {
  clearTimer();
  Event.emit('loading', { loading: false });
}


export function alertError(responese = {}) {
  console.log('responese', responese);
  Event.emit('toast', responese);
}

export function pare(params = {}) {
  let query = [];
  for (let key in params) {
    const value = params[key];
    if (value !== undefined && value !== null) {
      query.push(`${key}=${(value)}`);
    }
  }

  let queryStr = query.join('&');
  return queryStr ? `&${queryStr}` : '';
}

export function sign(option, Timestamp = 0) {
  const D = '38';
  const { method, url, data } = option;
  const l_t = Timestamp;
  const _method = method.toLocaleString();
  let _path = url;
  let signStr = '';
  const key = KEYS[D];
  if (_method === 'GET') {
    // get请求 如果是 json
    _path = url + pare(data);
    signStr = key + l_t + _path;
  } else if (_method === 'POST') {
    _path = url;
    let s = encryptQuery(data, D);
    signStr = s + key + l_t + _path;
  }
  // console.log('===================');
  // console.log(method, url);
  // console.log('参数', data);
  // console.log('_path', _path);
  // console.log('l_t', l_t);
  // console.log('-----', signStr);
  return murmurhash3_32_gc(signStr, 31);
}

export function dateToUtcString(data) {
  const time = dayjs().format('HH:mm:ss');
  Object.keys(data).forEach(key => {
    const item = data[key];
    if (isDateStr(item)) {
      let date = dayjs(item).format(`YYYY-MM-DD ${time}`);
      data[key] = dayjs(date)
        .toDate()
        .toUTCString();
    }
  });
}

function isDateStr(timers) {
  const reg = /^([\d]{4}-[\d]{2})((-[\d]{2})|(-[\d]{2} [\d]{2}:[\d]{2}:[\d]{2}))?$/;
  return reg.test(timers);
}
