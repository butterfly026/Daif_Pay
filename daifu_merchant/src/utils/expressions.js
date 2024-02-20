// import i18n from '@/locales';
// i18n.global.t('global.remark')

/**
 * 邮箱正则表达式
 */
export const email = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
export const email_prompt = "邮箱格式不正确";
/**
 * 密码正则表达式
 */
export const password = /^[a-zA-Z0-9]{6,20}$/;
export const password2 = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$/;
export const password2Msg = "密码格式为8-15位数字和字母的组合";
/**
 * 会员名称正则表达式
 */
export const user_name = /^[a-zA-Z]{2}[0-9a-zA-Z]{2,7}$/; // 前两位必须是字母，长度4-9位
export const user_name_prompt = "格式不正确";
// export const user_name_prompt = i18n.global.t('routes.tips.user_name_prompt');

/**
 * 正整数
 */
export const integers = /^[1-9]\d*$/;

/**
 * 手机号验证
 */
// export const phone = /^1[345678]\d{9}$/;
export const phone = /^(0([0-9]{9})|(1[345678]\d{9}))$/; // 同时验证越南和中国手机号
export const phone_prompt = "手机号格式不正确";
export const moneys = /^\+?[1-9]*\d$/;

// 越南手机号验证  ---别用
export const viPhone = /^0(8[1-5]|3[2-9]|7[0|6-9]|5[6|8|9])+([0-9]{7})$/g;

/**
 * 匹配url地址
 */
export const https = /^(http|https):\/\/([\w.]+\/?)\S*/;
export const https_prompt = "地址格式错误";

/**
 * 会员配置
 */
export const vip = /^[0-9]\d*$/;
/**
 * 长度为3
 */
export const threeLength = /^([1-9_-]){1,3}$/;
/**
 * 长度为10
 */
export const tenLength = /^([1-9_-]){1,10}$/;
/**
 * 银行卡
 */
export const backCard = /^([0-9]{1})(\d{5,19})$/;
export const backCardTips = "请输入正确银行卡号！";
/**
 * 真实姓名
 */
export const realName = /^[\u4e00-\u9fa5_a-zA-Z]+$/;

/**
 * 设备号
 */
export const recNumber = /^[A-Za-z0-9\-]+$/;
/**
 * IP地址
 */
export const ipNumber = /((25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))/;
export const ipTips = "请输入正确的IP地址";

/**
 * 保留小数点后4位
 */
export const fourNumber = /^(([1-9]{1}\d*)|(0{1}))(\.\d{1,4})?$/;
export const fourNumberMsg = "格式不正确";

// 验证是否是图片
export const imgReg = /\.(jpeg|jpe|jpg|gif|png|webp|svg)$/;

// 版本号1-99,0-99,0-99
export const appNumber = /^([1-9]\d|[1-9])(\.([1-9]\d|\d)){2}$/;
export const appErroMsg = "请输入正确版本号格式";
// 保留4位小数
export const fixedNum = /^(([1-9]{1}\d*)|(0{1}))(\.\d{1,4})?$/;
export const fixedNum3 = /^(([1-9]{1}\d*)|(0{1}))(\.\d{1,4})?$/;
// 1-20位中文
export const chinaLength = /^[\u4e00-\u9fa5]{1,20}$/;

export const user_name2 = /^[^\u4e00-\u9fa5]{0,}$/;

// 中文
export const chinaLang = /[\u4e00-\u9fa5]/;

/**
 * 长度为20
 */
export const twentyLength = /^([1-9_-]){1,20}$/;

// 动态验证码
export const dynamic = /^[0-9]{6}$/;
export const dynamicMsg = "验证码格式为6位纯数字";

// 1-999正整数
export const one_nine = /^\+?[1-9]{1}[0-9]{0,2}\d{0,0}$/;
export const nine_tips = "请输入1-999的正整数";

// 0-999正整数
export const zero_nine = /^\+?[0-9]{1}[0-9]{0,2}\d{0,0}$/;
export const zero_nine_tips = "请输入0-999的正整数";

// 1-1亿 ，保留4位小数
export const one_billion = /^(([1-9]{1}[0-9]{0,7})(\.(\d){0,4})?|(10{8})(\.0{0,4})?)$/;
export const one_billion_tips = "请输入1-1亿，最多保留4位小数";

// 1-100
export const one_hundred = /^([1-9][0-9]{0,1}|100)$/;
export const one_hundred_tips = "请输入1-100的正整数";

// 1-10亿
export const ten_billion = /^(?:(?!0)\d{1,9}|10{9})(\.\d{1,4})*$/;
export const ten_billion_tips = "请输入1-10亿，最多保留4位小数";

// 系统账号密码限制
export const syspsw = /^(?![0-9]+$)[0-9A-Za-z]{5,20}$/;
export const adminPassword = /^(?![0-9]+$)(?![A-Za-z]+$)[0-9A-Za-z]{5,20}$/;
export const syspswMsg = "格式为5-20位数字和字母的组合";
export const syspswUserNameMsg = '5-20位数字和字母的组合，可以是纯字母';

// 0-10亿 ，保留4位小数
export const teny = /^(([0-9]{1}[0-9]{0,8})(\.(\d){0,4})?|(10{9})(\.0{0,4})?)$/;
export const teny_tips = '请输入0-10亿，最多保留4位小数';

// 多个会员账号逗号隔开
export const usernames = /^([A-z0-9]{4,9},)*[A-z0-9]{4,9}$/;
// 验证数字
export const input_numbers = /^[0-9]*$/;
// 0-9999正整数
export const reg_number = /^\+?[0-9]{1}[0-9]{0,3}\d{0,0}$/;
export const reg_number_tips = '请输入0-9999的正整数';
