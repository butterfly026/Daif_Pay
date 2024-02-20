export function updateUserInfo(state, item = {}) {
  if (Object.keys(item).length === 0) {
    state.userInfo = {}
    sessionStorage.removeItem('merchant_userInfo');
  } else {
    const userInfo = {...state.userInfo, ...item}
    sessionStorage.setItem('merchant_userInfo', JSON.stringify(userInfo));
    state.userInfo = userInfo
  }
}

export function initialConfig(state, config = {}) {
  state.config = config;
}

export function setBankInfo(state, bankInfo = {}){
  sessionStorage.setItem('bankInfo', JSON.stringify(bankInfo));
  state.bankInfo = bankInfo;
}

export function setToolList(state, obj = {}) {
  state.toolList.push(obj);
}

export function removeToolList(state, url) {
  const list = state.toolList.filter(item => item.url != url);
  state.toolList = list;
}