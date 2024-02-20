import http from '../request';

// 获取上游渠道列表(下拉菜单)
export function channelMenu(data = {}) {
  return http.get('/agent/merchant/channel/menu', data);
}


// 获取商户列表(下拉菜单)
export function merchantMenu(data = {}) {
  return http.get('/agent/merchant/menu', data);
}
