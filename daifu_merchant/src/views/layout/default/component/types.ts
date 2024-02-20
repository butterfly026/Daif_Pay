// Menu菜单类型
export interface ImenuListType {
  name: string;
  path?: string;
  icon?: string;
  children?: ImenuListType[];
}
