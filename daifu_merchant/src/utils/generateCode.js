import store from '../store/index';
import base32Encode from 'base32-encode';

function randomWord(randomFlag, min, max) {
  let str = '',
    range = min,
    arr = [
      '0',
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      'a',
      'b',
      'c',
      'd',
      'e',
      'f',
      'g',
      'h',
      'i',
      'j',
      'k',
      'l',
      'm',
      'n',
      'o',
      'p',
      'q',
      'r',
      's',
      't',
      'u',
      'v',
      'w',
      'x',
      'y',
      'z',
      'A',
      'B',
      'C',
      'D',
      'E',
      'F',
      'G',
      'H',
      'I',
      'J',
      'K',
      'L',
      'M',
      'N',
      'O',
      'P',
      'Q',
      'R',
      'S',
      'T',
      'U',
      'V',
      'W',
      'X',
      'Y',
      'Z'
    ];

  // 随机产生
  if (randomFlag) {
    range = Math.round(Math.random() * (max - min)) + min;
  }
  for (let i = 0; i < range; i++) {
    let pos = Math.round(Math.random() * (arr.length - 1));
    str += arr[pos];
  }
  return str;
}

// 生成动态验证码，包含登录用户名
// 获取用户名和用户名长度
// 将用户名改成数组
function getNameInfo() {
  const name = store.state?.user || "";
  const len = name.length;
  const nameArr = name.split('');
  return {
    len,
    nameArr
  };
}

// 向字符串中插入值
function insertStr(soure, start, newStr) {
  return soure.slice(0, start) + newStr + soure.slice(start);
}
function stringToUint8Array(str) {
  let arr = [];
  for (let i = 0, j = str.length; i < j; ++i) {
    arr.push(str.charCodeAt(i));
  }

  let tmpUint8Array = new Uint8Array(arr);
  return tmpUint8Array;
}

export const generateCode = () => {
  const l = 32;
  const obj = getNameInfo();
  const len = obj.len;
  const arr = obj.nameArr;
  let randomStr = randomWord(false, l - len, l - len);
  // 把nameArr随机插入到randomStr中
  arr.forEach((item) => {
    // 生成随机数
    const w = Number.parseInt((Math.random() * (l - len)).toString());
    randomStr = insertStr(randomStr, w, item);
  });
  // console.log(base32Encode(stringToUint8Array(randomStr), 'Crockford'));
  // console.log(base32Encode(stringToUint8Array(randomStr), 'RFC3548'));
  // console.log(base32Encode(stringToUint8Array(randomStr), 'RFC4648'));
  // console.log(base32Encode(stringToUint8Array(randomStr), 'RFC4648-HEX'));
  return base32Encode(stringToUint8Array(randomStr), 'RFC3548');
  // dialogFormInfo.seamo = randomStr;
};
