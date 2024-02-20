import http from '../request';

//商户列表
export function getMerchantList(query = {}) {
  return http.get('/agent/merchant/list', query);
}

// 获取上游渠道列表(下拉菜单)
export function channelMenu(data = {}) {
  return http.get('/agent/merchant/channel/menu', data);
}
// 添加商户

//商户后台管理员列表
export function getMerchantMemberList(query = {}) {
  return http.get('/agent/member/list', query);
}


// 删除后台管理员
export function merchantDepositList( params = {}) {
  return http.get('/agent/deposit/list', params);
}


/**
 * @账变记录
 * @param {*}
 */
 export function withdrawList(params = {}) {
  return http.post('/agent/withdraw/list', params);
}

/**
 * @代理后台账变记录
 * @param {*}
 */
 export function transfromWithdrawList(params = {}) {
  return http.get('/agent/transaction/list', params);
}

/**
 * @提现表格
 * @param {*}
 */
 export function getWthdrawalTable(params = {}) {
  return http.get('/agent/excel/list', params);
}
