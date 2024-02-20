import http from '../request';

export function getChannelList(query = {}) {
  return http.get('/admin/channel/list', query);
}

// 渠道新增
export function addChannel(data = {}) {
  return http.post('/admin/channel/insert', data);
}

// 获取上游渠道列表(下拉菜单)
export function channelMenu(data = {}) {
  return http.get('/admin/channel/menu', data);
}


// 获取商户列表(下拉菜单)
export function merchantMenu(data = {}) {
  return http.get('/admin/merchant/menu', data);
}
// 渠道新增
export function editChannel(data = {}) {
  return http.post('/admin/channel/update', data);
}

// 删除上游渠道
export function delChannel(query = {}) {
  return http.get('/admin/channel/delete', query);
}
