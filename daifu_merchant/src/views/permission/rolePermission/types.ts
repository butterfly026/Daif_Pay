
export interface BaseGroupUserItem {
  create_at: number;
  gid: number;
  gname: string;
  lft: number;
  lvl: number;
  noted: string;
  permission: string;
  rgt: number;
  state: number;
}

export interface ResponseGroupUserItem extends BaseGroupUserItem {
  pid: string;
}

export interface GroupUserItem extends BaseGroupUserItem {
  pid: number;
  label: string;
  children?: any;
}
