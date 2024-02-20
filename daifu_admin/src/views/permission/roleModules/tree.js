
export function listToTree(list, root = 1) {
  const map = {};
  list.forEach((item) => {
    if (!map[item.id]) {
      map[item.id] = item;
    }
  });

  list.forEach((item) => {
    // if (item.pid !== 1 && map[item.pid]) {
    if (map[item.pid]) {
      map[item.pid].children
        ? map[item.pid].children.push(item)
        : (map[item.pid].children = [item]);
    }
  });

  return list.filter(item => {
    if (item.pid == 0) {
      return item;
    }
  });
}

export function getPriveItemByPid(pid, list) {
  return list.find((item) => item.id === pid);
}
