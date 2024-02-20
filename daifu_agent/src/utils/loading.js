import { ElLoading } from 'element-plus';

/* 当页面有两个接口时，第一个接口loading的close事件会直接将第二个接口的loading实例也close */
let loadingInstance = null;
export function startLoading() {
  loadingInstance = ElLoading.service({
    lock: true,
    text: 'Loading',
    spinner: 'el-icon-loading',
    background: 'rgba(0, 0, 0, 0.7)'
  });
}
export function endLoading() {
  loadingInstance.close();
}
let needLoadingRequestCount = 0;
export function showFullScreenLoading() {
  if (needLoadingRequestCount === 0) {
    startLoading();
  }
  needLoadingRequestCount++;
}
export function tryHideFullScreenLoading() {
  if (needLoadingRequestCount <= 0) return;
  needLoadingRequestCount--;
  if (needLoadingRequestCount === 0) {
    endLoading();
  }
}
