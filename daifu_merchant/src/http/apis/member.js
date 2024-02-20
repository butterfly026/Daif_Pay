import http from '../request';

export function getMemberList(query = {}) {
  return http.get('/merchant/member/list', query);
}

// 添加会员
export function addMember(data = {}) {
  return http.post('/merchant/member/insert', data);
}

// 删除会员
export function delMember(query = {}) {
  return http.get('/merchant/member/delete', query);
}

// 编辑会员
export function editMenber(data = {}) {
  return http.post('/merchant/member/update', data);
}