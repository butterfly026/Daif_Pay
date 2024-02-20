import http from '../request';

/**
 * @获取商户余额(通过token)
 * @param {*}
 */
 export function getBalance(params = {}) {
  return http.get('/merchant/info/balance', params);
}


// 获取银行名称
export function getBankName(params = {}) {
    return http.get('/merchant/withdraw/bank', params);
  }

// 提交单笔提现
export function singleWithdrawal(data = {}) {
    return http.post('/merchant/withdraw/insert', data);
  }

  // 提交批量提现
export function batchWithdrawal(data = {}) {
    return http.post('/merchant/withdraw/batch', data);
  }