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

// 提现列表-(拒绝)
export function withdrawReject(query = {}) {
  return http.post('/admin/withdraw/reject', query);
}

// 提现列表-(成功)
export function withdrawSuccess(query = {}) {
  return http.post('/admin/withdraw/success', query);
}


// 提现列表--(手动出款)
export function withdrawApprove(query = {}) {
  return http.post('/admin/withdraw/approve', query);
}


// 提现列表-(手动回调)
export function withdrawNotify(query = {}) {
  return http.post('/admin/withdraw/notify', query);
}

// 禁用/开启所有渠道 0 关闭 1 开启
export function closeAllChannel(query = {}) {
  return http.get('/admin/channel/close', query);
}

// 查询商户余额
export function getChannelBalance(query = {}) {
  return http.get('/admin/channel/balance', query);
}

export function withdrawQuery(query = {}) {
  return http.post('/admin/withdraw/query', query);
}