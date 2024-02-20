import dayjs from 'dayjs';

function padStart(s = '', l, w) {
  while (s.length < l) {
    s = w + s;
  }
  if (s.length > l) s = s.slice(s.length - l);
  return s;
}

/**
 * 日期格式化
 * date {Date|Number} 日期
 * fmt {String} 格式
 */
export function dateFormat(date, fmt = 'yyyy-MM-dd') {
  if (!date) date = new Date();
  if (typeof date === 'number') date = new Date(date);

  return fmt
    .replace('yyyy', String(date.getFullYear()))
    .replace('MM', String(date.getMonth() + 1).padStart(2, '0'))
    .replace('dd', String(date.getDate()).padStart(2, '0'))
    .replace('hh', String(date.getHours()).padStart(2, '0'))
    .replace('mm', String(date.getMinutes()).padStart(2, '0'))
    .replace('ss', String(date.getSeconds()).padStart(2, '0'))
    .replace('SS', String(date.getMilliseconds()));
}

export function dateTimeFormat(time) {
  const date = new Date(time);
  return (
    date.getFullYear() +
    '-' +
    padStart(String(date.getMonth() + 1), 2, '0') +
    '-' +
    padStart(String(date.getDate()), 2, '0') +
    ' ' +
    padStart(String(date.getHours()), 2, '0') +
    ':' +
    padStart(String(date.getMinutes()), 2, '0') +
    ':' +
    padStart(String(date.getSeconds()), 2, '0')
  );
}

const ONE_SECOND = 1000;
const ONE_MINUTE = 60 * ONE_SECOND;
const ONE_HOUR = 60 * ONE_MINUTE;

/**
 * 时间格式化
 * time {Number} 时分秒毫秒数
 * fmt {String} 格式
 */
export function timeFormat(time, fmt = 'hh:mm:ss') {
  return fmt
    .replace('hh', String(Math.floor(time / ONE_HOUR)).padStart(2, '0'))
    .replace('mm', String(Math.floor((time % ONE_HOUR) / ONE_MINUTE)).padStart(2, '0'))
    .replace('ss', String(Math.floor(((time % ONE_HOUR) % ONE_MINUTE) / ONE_SECOND)).padStart(2, '0'));
}

// 获取不同时区间的偏移量(相对于中南半岛)
export function getOffsetTime() {
  // 获取当地标准时间
  const d = new Date();
  // 获取不同时区间的偏移量
  const offsetTime = d.getTimezoneOffset() + 420;
  // 转成时间戳之后再相加
  return offsetTime * 60000;
}

/**
 * @获取前面几天
 *
 * @export
 * @param {number} [day=3]
 * @return {*}
 */
export function getlastThreeDay(day = 3) {
  const t = `${dayjs()
    .hour(-(24 * (day - 1)))
    .format('YYYY-MM-DD')} 00:00:00`;
  const s = `${dayjs().format('YYYY-MM-DD')} 23:59:59`;
  return [t, s];
}

/**
 * 获取前 i 周的周一和周日日期，并以数组的方式返回。
 * 当 i=1，获取的是上周一和上周日的日期；
 * 当 i=2，获取的是上上周一和上上周日的日期
 * ...以此类推
 * @param i
 */
export function getLastWeek(i) {
  let weekOfDay = parseInt(dayjs().format('d')); //计算今天是这周第几天
  let last_monday = dayjs()
    .subtract(weekOfDay + 7 * i - 1, 'days')
    .format('YYYY-MM-DD'); //周一日期
  let last_sunday = dayjs()
    .subtract(weekOfDay + 7 * (i - 1), 'days')
    .format('YYYY-MM-DD'); //周日日期
  return [last_monday, last_sunday];
}
