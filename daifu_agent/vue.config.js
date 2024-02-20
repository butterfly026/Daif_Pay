const path = require('path');
const TerserPlugin = require('terser-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin; // 打包分析
const webpack = require('webpack');
const IS_PROD = ['production'].includes(process.env.NODE_ENV);
const resolve = dir => path.join(__dirname, dir);
const { getCommandParams, externalModules } = require('./webpack.util');

const { ENV } = getCommandParams();
const now = new Date();
const VERSION = [now.getMonth() + 1, now.getDate(), now.getHours(), now.getMinutes()].join('.');

console.log('==========================');
console.log('当前版本号为===>', VERSION);
console.log('当前打包环境===>', ENV);
console.log('==========================');

const output_dev = {
  filename: `js/[name].[hash:8].js`,
  chunkFilename: `js/[name].[hash:8].js`
};

const output_prod = {
  filename: `js/[chunkhash:8].js?v=${VERSION}`,
  chunkFilename: `js/[chunkhash:8].js?v=${VERSION}`
};


const modules = externalModules(ENV);

console.log(modules.reduce((obj, item) => {
  if (item.scope) obj[item.name] = item.scope;
  return obj;
}, {}));

let plugins = [
  new webpack.DefinePlugin({
    // __VUE_OPTIONS_API__: JSON.stringify(true),
    // __VUE_PROD_DEVTOOLS__: JSON.stringify(false),
    __INTLIFY_PROD_DEVTOOLS__: JSON.stringify(false),
    ENV: `'${ENV}'`,
    VERSION: `'${VERSION}'`
  })
];

if (ENV === 'prod') {
  plugins.push(
    new TerserPlugin({
      // 启用多进程压缩文件
      parallel: true,
      terserOptions: {
        warnings: false,
        ecma: undefined,
        parse: {},
        compress: {
          pure_funcs: ['console.log'],
          drop_console: true
        },
        mangle: true,
        module: false,
        output: null,
        format: null,
        toplevel: false,
        nameCache: null,
        ie8: false,
        keep_classnames: undefined,
        keep_fnames: false,
        safari10: false
      },
      extractComments: false
    })
  );
}

module.exports = {
  publicPath: IS_PROD ? '/' : '/',
  // 相对于打包路径index.html的路径

  productionSourceMap: ENV !== 'prod',

  // 是否在开发环境下通过 eslint-loader 在每次保存时 lint 代码
  lintOnSave: false,

  // 是否使用包含运行时编译器的 Vue 构建版本
  runtimeCompiler: true,

  // 向 PWA 插件传递选项。
  pwa: {},
  chainWebpack: config => {
    // 修复热更新失效
    config.resolve.symlinks(true);

    // 添加别名
    config.resolve.alias.set('@', resolve('src'));

    // config.plugin().tap(args => {
    //   args[0].css = modules.filter(item => item.type === 'css');
    //   args[0].js = modules.filter(item => item.type === 'js');
    //   return args;
    // });

    // 生产环境打包分析
    /*
    if (IS_PROD) {
      config.plugin('webpack-report').use(BundleAnalyzerPlugin, [{ analyzerMode: 'static' }]);
    }
    */
    return config;
  },

  configureWebpack: {
    // externals: modules.reduce((obj, item) => {
    //   if (item.scope) obj[item.name] = item.scope;
    //   return obj;
    // }, {}),

    plugins: [...plugins],

    output: IS_PROD ? output_prod : output_dev
  },

  css: {
    // 是否允许css import
    requireModuleExtension: true,
    // loaderOptions: {
    //   less: {
    //     globalVars: {
    //       primary: '#005aa0'
    //     }
    //   }
    // },
    sourceMap: false
  },

  devServer: {
    overlay: {
      // 让浏览器 overlay 同时显示警告和错误
      warnings: true,
      errors: true
    },
    host: '0.0.0.0',
    port: 8082, // 端口号
    https: false, // https:{type:Boolean}
    open: false, //配置自动启动浏览器
    hotOnly: true, // 热更新
    proxy: {
      //配置多个跨域
      '/proxy': {
        // target: "https://lua.nwcvrls.cn", // 正式环境
        // target: "http://localhost/index.php", // 本地测试
        // target: "http://apadmin.x4r.cc/",  // 正式
        // target: "http://13.251.140.4:81/index.php",  // 正式
        target: "http://127.0.0.1:83",  // 正式
        // target: "http://admindev.x4r.cc/",  // 测试
        // target: 'http://pad.yomart.xyz/', // 测试环境
        changeOrigin: true,
        secure: false,
        pathRewrite: {
          '^/proxy': '/'
        }
      }
    }
  }
};
