import http from '../request';

/**
 * @权限模块列表
 * @param {*}
 */
export function getPrivList(params = {}) {
  return http.get('/admin/group/priv', params);
}

/**
 * @添加权限模块
 * @param {*}
 */
export function privInsert(data = {}) {
  return http.post('/admin/priv/insert', data);
}

/**
 * @更新权限模块
 * @param {*}
 */
export function privUpdate(data = {}) {
  return http.post('/admin/priv/update', data);
}

/**
 * @删除权限模块
 * @param {*}
 */
export function privDelete(params = {}) {
  return http.get('/admin/priv/delete', params);
}

/**
 * @用户组列表
 * @param {*}
 */
export function getGroupList(params = {}) {
  return http.get('/admin/group/list', params);
}

/**
 * @添加用户组
 * @param {*}
 */
export function groupInsert(data = {}) {
  return http.post('/admin/group/insert', data);
}

/**
 * @用户组更新
 * @param {*}
 */
export function groupUpdate(data = {}) {
  return http.post('/admin/group/update', data);
}

/**
 * @用户组删除
 * @param {*}
 */
 export function groupDelete(data = {}) {
  return http.get('/admin/group/delete', data);
}

/**
 * @添加系统账号
 * @param {*}
 */
export function addSystemAccount(data = {}) {
  return http.post('/admin/member/insert', data);
}

/**
 * @编辑系统账号
 * @param {*}
 */
export function editSystemAccount(data = {}) {
  return http.post('/admin/member/update', data);
}

/**
 * @获取系统账号列表
 * @param {*}
 */
export function getSystemAccountList(params = {}) {
  return http.get('/admin/member/list', params);
}

/**
 * @修改系统账号状态
 * @param {*}
 */
export function updateSystemAccountState(params = {}) {
  return http.get('/admin/admin/updatestate', params);
}
/**
 * @日志管理系统日志
 * @param {*}
 */
export function getSystemList(data = {}) {
  return http.post('/admin/sys/log/system/list', data);
}
export default {};
