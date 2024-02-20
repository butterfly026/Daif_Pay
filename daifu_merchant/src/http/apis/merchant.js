import http from '../request';

//商户列表
export function getMerchantList(query = {}) {
  return http.get('/admin/merchant/list', query);
}

// 添加商户
export function addMerchant(params = {}) {
  return http.post('/admin/merchant/insert', params);
}

// 编辑商户
export function editMerchant( params = {}) {
  return http.post('/admin/merchant/update', params);
}

// 删除商户
export function deleteMerchant( params = {}) {
  return http.get('/admin/merchant/delete', params);
}

//商户后台管理员列表
export function getMerchantMemberList(query = {}) {
  return http.get('/admin/merchant/member/list', query);
}

// 添加后台管理员
export function addMerchantMember(params = {}) {
  return http.post('/admin/merchant/member/insert', params);
}

// 编辑后台管理员
export function editMerchantMember( params = {}) {
  return http.post('/admin/merchant/member/update', params);
}

// 删除后台管理员
export function deleteMerchantMember( params = {}) {
  return http.get('/admin/merchant/member/delete', params);
}



// 充值记录
export function merchantDepositList( params = {}) {
  return http.get('/merchant/deposit/list', params);
}


/**
 * @账变记录
 * @param {*}
 */
 export function withdrawList(params = {}) {
  return http.get('/merchant/withdraw/list', params);
}


/**
 * @生成密钥
 * @param {*}
 */
 export function getCode(params = {}) {
  return http.get('/merchant/member/nonce', params);
}


/**
 * @充值表格
 * @param {*}
 */
 export function getRechargeTable(params = {}) {
  return http.get('/merchant/excel/list', params);
}


/**
 * @提现表格
 * @param {*}
 */
 export function getWthdrawalTable(params = {}) {
  return http.get('/merchant/excel/list', params);
}


/**
 * @提现表格
 * @param {*}
 */
 export function exportOrderTable(params = {}) {
  return http.post('/merchant/excel/export', params);
}
