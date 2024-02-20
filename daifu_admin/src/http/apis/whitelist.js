import http from '../request';

/**
 * @IP白名单列表
 * @param {*}
 */
 export function getWhiteList(params = {}) {
  return http.get('/admin/whitelist/api/list', params);
}


/**
 * @添加白名单IP
 * @param {*}
 */
 export function addWhiteList(params = {}) {
  return http.post('/admin/whitelist/api/insert', params);
}

/**
 * @删除白名单IP
 * @param {*}
 */
 export function delWhiteList(params = {}) {
  return http.get('/admin/whitelist/api/delete', params);
}


/**
 * @IP白名单列表(商户后台)
 * @param {*}
 */
 export function getMerchantWhiteList(params = {}) {
  return http.get('/admin/whitelist/merchant/list', params);
}


/**
 * @添加白名单IP(商户后台)
 * @param {*}
 */
 export function addMerchantWhiteList(params = {}) {
  return http.post('/admin/whitelist/merchant/insert', params);
}

/**
 * @删除白名单IP(商户后台)
 * @param {*}
 */
 export function delMerchantWhiteList(params = {}) {
  return http.get('/admin/whitelist/merchant/delete', params);
}
