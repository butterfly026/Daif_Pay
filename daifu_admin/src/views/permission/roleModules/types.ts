export interface BasePriveListItem {
  id: number;
  lvl: number;
  lft: number;
  module: string;
  name: string;
  rgt: number;
  state: number;
  docId?: string;
}

export interface ResponsePriveListItem extends BasePriveListItem {
  pid: string;
}

export interface PriveListItem extends BasePriveListItem {
  pid: number;
  label: string;
  children?: any;
}

export interface ResponsePrivList {
  status: boolean;
  data: ResponsePriveListItem[];
}
