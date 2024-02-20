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

//商户后台代理员列表
export function getMerchantAgentList(query = {}) {
  return http.get('/admin/merchant/agent/list', query);
}

// 添加后台代理员
export function addMerchantAgent(params = {}) {
  return http.post('/admin/merchant/agent/insert', params);
}

// 编辑后台代理员
export function editMerchantAgent( params = {}) {
  return http.post('/admin/merchant/agent/update', params);
}

// 删除后台代理员
export function deleteMerchantAgent( params = {}) {
  return http.get('/admin/merchant/agent/delete', params);
}
// 获取代理员列表(下拉菜单) 
export function agentMenu( params = {}) {
  return http.get('/admin/merchant/agent/menu', params);
}



// 删除后台管理员
export function merchantDepositList( params = {}) {
  return http.get('/admin/deposit/list', params);
}

// 获取账变类型
export function getAccountType( params = {}) {
  return http.get('/admin/transaction/menu', params);
}

// 商户余额调整
export function changeMerchantMoney( params = {}) {
  return http.post('/admin/merchant/adjust', params);
}


/**
 * @账变记录
 * @param {*}
 */
 export function withdrawList(params = {}) {
  return http.post('/admin/withdraw/list', params);
}

/**
 * @管理后台账变记录
 * @param {*}
 */
 export function transfromWithdrawList(params = {}) {
  return http.get('/admin/transaction/list', params);
}


/**
 * @充值列表-拒绝
 * @param {*}
 */
 export function depositReject(params = {}) {
  return http.post('/admin/deposit/reject', params);
}


/**
 * @充值列表-通过
 * @param {*}
 */
 export function depositResolve(params = {}) {
  return http.post('/admin/deposit/approve', params);
}


/**
 * @生成密钥
 * @param {*}
 */
 export function getCode(params = {}) {
  return http.get('/admin/member/nonce', params);
}


/**
 * @添加白名单IP
 * @param {*}
 */
 export function addWhiteList(params = {}) {
  return http.post('/admin/whitelist/insert', params);
}

/**
 * @删除白名单IP
 * @param {*}
 */
 export function delWhiteList(params = {}) {
  return http.get('/admin/whitelist/delete', params);
}


/**
 * @充值表格
 * @param {*}
 */
 export function getRechargeTable(params = {}) {
  return http.get('/admin/excel/list', params);
}


/**
 * @提现表格
 * @param {*}
 */
 export function getWthdrawalTable(params = {}) {
  return http.get('/admin/excel/list', params);
}

/**
 * @下载表格
 * @param {*}
 */
 export function exportOrderTable(params = {}) {
  return http.post('/admin/excel/export', params);
}
