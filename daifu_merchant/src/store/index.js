import { createStore, useStore as baseUseStore } from 'vuex';
import * as actions from './action';
import * as mutations from './mutations';
import getters from './getters'


const logPlugin = (store) => {
  // called when the store is initialized
  store.subscribe((mutation, state) => {
    // called after every mutation.
    // The mutation comes in the format of `{ type, payload }`.
    // console.log('mutation', mutation)
    console.log('state', state)
  })
}


export default createStore({
  state: {
    userInfo: {
      token: '',
      login: false,
      ...JSON.parse(sessionStorage.getItem('merchant_userInfo') || '{}')
    },
    toolList: []
  },
  getters,
  mutations,
  actions,
  plugins: [logPlugin]
})

