const path = require('path');
const pwd = process.cwd();
const resolve = dir => path.resolve(pwd, dir);

exports.getCommandParams = () => {
  // CLI参数从第三个开始获取
  const args = process.argv.slice(2);
  const result = {};
  const REG = /^--([^-]+)/;

  for (let i = 0; i < args.length; i++) {
    const match = REG.exec(args[i]);
    // 判断当前参数是否--开头，如果--开头才算合法参数
    if (match) {
      const key = match[1];
      // 如果下个参数也是--开头，本参数则值为空
      if (REG.test(match[i + 1])) {
        // 下一个参数
        result[key] = '';
      } else {
        // 直接跳过下一个参数
        i++;
        result[key] = args[i] || '';
      }
    }
  }

  return result;
};

exports.externalModules = (env = 'dev') => {
  const cdn = env === 'dev' ? 'https://dl.yostata.xyz/lib_cdn' : '{LIB_URL}'

  return [
    // { name: 'vue', scope: 'Vue', src: `${cdn}/vue/vue.global.js`, type: 'js'},
    //{ name: 'element-plus', scope: 'ElementPlus', src: `${cdn}/element-plus/element-plus.js`, type: 'js' },
    //{ name: 'element-ui', scope: null, src: `${cdn}/element-plus/element-plus.css`, type: 'css'},
  ];
}