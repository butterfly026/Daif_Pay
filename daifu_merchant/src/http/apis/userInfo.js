import http from '../request';


// 获取商户信息
export function getUserInfo(params = {}) {
    return http.get('/merchant/info/detail', params);
  }

// 修改商户密码
export function resetPWD(data = {}) {
    return http.post('/merchant/info/update/password', data);
  }

  // 查看商户密钥
export function getKeyCode(data = {}) {
    return http.post('/merchant/info/view/key', data);
  }
