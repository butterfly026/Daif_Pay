import http from '../request';


// 获取代理信息
export function getAgentInfo(params = {}) {
    return http.get('/agent/member/info/detail', params);
  }

// 修改代理密码
export function resetPWD(data = {}) {
    return http.post('/agent/member/update/password', data);
  }

  
// 充值记录
export function merchantDepositList( params = {}) {
  return http.get('/agent/deposit/list', params);
}