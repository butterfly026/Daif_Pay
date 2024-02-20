import http from '../request';

export function getGroupList(query = {}) {
  return http.get('/admin/group/list', query);
}

// 登录
export function login(params = {}) {
  return http.post('/admin/member/login', params);
}

// 登出
export function logout() {
  return http.get('/admin/member/logout', {});
}

// 修改密码
export function resetPWD(params = {}) {
  return http.post('/admin/member/own', params);
}

