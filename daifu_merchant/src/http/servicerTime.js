export function getServiceUrl(path = '') {
  
  let SERVICE_HOST = process.env.NODE_ENV === 'development' ? '//merchant_dpay.9mis5u.xyz' : '//merchant_dpay.9mis5u.xyz';
  return SERVICE_HOST + '/' + path + (path.includes('?') ? '&' : '?') + `_t=${Date.now()}`;
}

const duration = 20000;
let loopTimer = null;

function clearTimer() {
  if(loopTimer) {
    clearTimeout(loopTimer)
    loopTimer = null
  }
}

/**
 * 获取服务器时间
 */
let serverTimeOffset = false;
export function getServerTime() {
  const url = getServiceUrl('date');
  const clientTimestamp = Date.parse(new Date().toUTCString());
  return new Promise(resolve => {
    const xhr = new XMLHttpRequest();
    xhr.open('head', url, true);
    xhr.send(null);
    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4) {
        const serverDateStr = xhr.getResponseHeader('Date');
        const serverTimestamp = Date.parse(new Date(Date.parse(serverDateStr)).toUTCString());

        // 请求到响应时间差
        const serverClientRequestDiffTime = serverTimestamp - clientTimestamp;
        const nowTimeStamp = Date.parse(new Date().toUTCString());

        const serverClientResponseDiffTime = nowTimeStamp - serverTimestamp;
        const responseTime = (serverClientRequestDiffTime - nowTimeStamp + clientTimestamp - serverClientResponseDiffTime )/2;

        serverTimeOffset = (serverClientResponseDiffTime - responseTime);

        const date = new Date();

        date.setTime(date.getTime() + serverTimeOffset);
        resolve(date.toUTCString());
      }
    };
  });
}

export function loopGetServiceTime(callback = () => {}) {
  /*
  clearTimer()

  loopTimer = setTimeout(() => {
    getServerTime()
      .then(time => {
        callback && callback(time)

        loopGetServiceTime
      })
  }, duration)
  */
}