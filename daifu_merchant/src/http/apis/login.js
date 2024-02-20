import http from '../request';

export function getGroupList(query = {}) {
  return http.get('/merchant/group/list', query);
}

// 登录
export function login(params = {}) {
  return http.post('/merchant/member/login', params);
}

// 登录
export function logout() {
  return http.get('/merchant/member/logout', {});
}

