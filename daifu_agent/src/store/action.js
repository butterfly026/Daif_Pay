import { login, logout } from '@/http/apis/login'

export function loginIn({ commit }, params) {
  return login(params)
    .then(res => {
      commit('updateUserInfo', {login: res.status});
      return Promise.resolve(res)
    })
}

export function loginOut({ commit }) {
  return logout()
    .then(res => {
      // console.log('handleLoginOut')
      if (res.status) {
        commit('updateUserInfo', {});
      }

      return Promise.resolve(res)
    })
    .catch(err => {
      return Promise.reject(err)
    })
}