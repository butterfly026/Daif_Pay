import http from '../request';

export function getGroupList(query = {}) {
  return http.get('/agent/group/list', query);
}

// 登录
export function login(params = {}) {
  return http.post('/agent/member/login', params);
}

// 登出
export function logout() {
  return http.get('/agent/member/logout', {});
}

// 修改密码
export function resetPWD(params = {}) {
  return http.post('/agent/member/own', params);
}

