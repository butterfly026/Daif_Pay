import qs from 'qs';
import store from '../store';
import { encryptQuery } from './xxtea';
import { getServerTime } from './servicerTime';
import Event from '../event';
import { showFullScreenLoading, tryHideFullScreenLoading } from '@/utils/loading';
import router from '../router/index'

import { mergeOptions, setHeaders, startLoading, closeLoading, alertError, pare, sign, defaultOptions, dateToUtcString } from './requestUtil';

const BASE_URL = process.env.NODE_ENV === 'development' ? '/proxy' : window.location.origin;

let Timestamp = 0;
/*
setInterval(() => {
  getServerTime().then(time => (Timestamp = time));
}, 20000);
*/
const request = async (options = defaultOptions) => {
  //if (Timestamp < 100000) Timestamp = await getServerTime();
  const option = mergeOptions(options, defaultOptions);
  const { method = 'get', url, data = {}, timeout, headers, loading, toastError, responseType } = option;
  const _method = method.toLocaleLowerCase();
  // console.log('修改前的数据', data);
  // 将data里面的时间转换为utc格式
  // dateToUtcString(data);
  // console.info('%c 修改后的数据', 'background-color: #1DAEF8; color: red');

  const _headers = {
    ...headers,
    'X-Ca-Timestamp': Timestamp,
    'X-Ca-Nonce': sign(option, Timestamp)
  };

  // console.log(qs.stringify(data));

  if (loading) startLoading();
  showFullScreenLoading()
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();

    const _url = _method === 'get' ? url + pare(data) : url;
    const body = _method === 'get' ? null : option.sign ? encryptQuery(data, '38') : qs.stringify(data);
    const URL = BASE_URL + _url;
    xhr.open(_method, URL, true);
    xhr.responseType = responseType;
    // 设置请求头
    setHeaders(xhr, _headers);

    if (_method === 'post') {
      xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    } else {
      xhr.setRequestHeader('Content-Type', 'application/json');
    }

    xhr.send(body);
    xhr.timeout = timeout;
    xhr.ontimeout = () => {
      if (loading) closeLoading();
      tryHideFullScreenLoading();
    };
    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4) {
        // 关闭loading
        if (loading) closeLoading();
        tryHideFullScreenLoading()

        if (xhr.status >= 200 && xhr.status < 300) {
          // 图片类的二进制数据
          if (responseType === 'blob') {
            resolve({
              url: window.URL.createObjectURL(xhr.response),
              vid: xhr.getResponseHeader('vid')
            });
          } else {
            const response = JSON.parse(xhr.responseText);

            // 如果token失效
            if (!response.status && response.data === 'token') {
              store.commit('updateUserInfo', {});
              store.commit('setBankInfo', {});
              Event.emit('login', { login: false });
              router.push('/login');
            }

            // 如果是登录接口 成功后token取 header返回的ID
            if (url.includes('/agency/login') && response.status) {
              store.commit('updateUserInfo', { token: xhr.getResponseHeader('id') });
            }

            if (!response.status && toastError) {
              alertError(JSON.parse(xhr.responseText));
            }
            resolve(response);
          }
        } else {
          if (xhr.status === 502) {
            alertError({ data: 502 });
          }
          if (toastError) {
            // console.log('xhr.responseText', xhr.responseText);
            alertError(xhr.responseText);
          }
          reject(xhr.responseText);
        }
      }
    };
  });
};

export default {
  get(url, query = {}, options = {}) {
    return request({
      ...options,
      url: `${url}?r=${performance.now()}`,
      method: 'GET',
      data: query
    });
  },

  post(url = '', params = {}, options = {}) {
    return request({
      ...options,
      url,
      method: 'POST',
      data: params
    });
  },

  head(url = '', params = {}, options = {}) {
    return request({
      ...options,
      url,
      method: 'HEAD',
      data: params
    });
  }
};
