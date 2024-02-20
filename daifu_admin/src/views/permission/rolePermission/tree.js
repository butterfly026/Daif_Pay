
export function listToTree(list, isAdd = false) {
  const map = {};
  list.forEach((item) => {
    if (!map[item.gid]) {
      map[item.gid] = item;
    }
  });

  list.forEach((item) => {
    if (map[item.pid]) {
      map[item.pid].children
        ? map[item.pid].children.push(item)
        : (map[item.pid].children = [item]);
    }
  });

  let listClone = [];

  if (isAdd) {
    const obj = {"create_at":1635852791,"noted":"根","permission":"","gid":"-2","state":1,"pid":"-1","gname":"根节点", children: list.filter((item) => item.pid == 0)};
    listClone.push(
      obj
    )
  } else {
    listClone = list;
  }

  return listClone.filter(item => {
    if (isAdd) {
      if (item.pid == -1) {
        return item;
      }
    } else {
      if (item.pid == 0) {
        return item;
      }
    }
  });
}

export function getPriveItemByPid(pid, list) {
  const obj = list.find((item) => item.gid == pid);

  return obj ? obj : {
    gname: "根节点",
    permission: ""
  };
}
