import http from '../request';

/**
 * @获取商户余额(通过token)
 * @param {*}
 */
export function getBalance(params = {}) {
  return http.get('/merchant/info/balance', params);
}

/**
 * @发起充值
 * @param {*}
 */
 export function depositInsert(params = {}) {
    return http.post('/merchant/deposit/insert', params);
  }
  

/**
 * @账变记录
 * @param {*}
 */
 export function transactionList(params = {}) {
  return http.get('/merchant/transaction/list', params);
}

/**
 * @查询是否自带充值功能
 * @param {*}
 */
export function chkSelfcharge(params = {}) {
  return http.get('/merchant/info/chkselfcharge', params);
}

