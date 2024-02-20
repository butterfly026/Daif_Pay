import { useI18n } from 'vue-i18n';

export function formatNumberToCurrency(value = '') {
  if (!value) return '0';
  let number = mul(value,1000);
  const reg = /\d{1,3}(?=(\d{3})+$)/g;
  const strList = number.toString().split('.');
  const dec = strList[1] ? `.${strList[1].substring(0, 4)}` : '';
  return strList[0].replace(reg, '$&' + ',') + dec;
}

// 格式化日期选择框文本
export function dateTimePickerFormatter(type, val) {
  const { t } = useI18n();
  const str = val.replace(/\b(0+)/gi, '');
  if (type === 'year') {
    return `${t('year', { y: str })}`;
  }
  if (type === 'month') {
    return `${t('month', { m: str })}`;
  }
  if (type === 'day') {
    return `${t('day', { d: str })}`;
  }
  return str;
}

export function colorSet(num) {
  if (num === 0) {
    return '#414655';
  } else if (num > 0) {
    return '#29B14E';
  } else if (num < 0) {
    return '#DB6372';
  }
}

// 乘法，解决失去精度问题
function mul(num1,num2){//传参 数字1，数字2
  let str1 = String(num1)
  let str2 = String(num2)
  let m = 0
 
  try{m+=str1.split(".")[1].length}catch(e){}
  try{m+=str2.split(".")[1].length}catch(e){}

  return Number(str1.replace(".",""))*Number(str2.replace(".",""))/Math.pow(10,m)
}