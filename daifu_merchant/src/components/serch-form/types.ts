export interface OptionsType {
  value: string;
  label: string;
}

export interface RefObjectKeyRulesType {
  [key: string]: any;
}
export interface FormData {
  model: string;
  label?: string;
  type?: string;
  width?: string;
  url?: string;
  loading?: boolean;
  multiple?: boolean;
  require?: boolean;
  options?: OptionsType[];
  labelList?: OptionsType[];
  selectModel?: any;
  value?: any;
  modelName?: any;
  modelValue?: string;
  children?: FormData;
  placeholder?: string;
  modelNameArray?: any[];
  selectType?: any;
  valueModel?: any;
  decideyear?: any; // 只保留年月日
  default?: number | string | any[];
}
