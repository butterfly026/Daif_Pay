export default [
    {
        name: "数据管理",
        id: "",
        pathAll: ["game", "tournament", "teams", "odd"],
        children: [
            {
                path: "game",
                name: "游戏管理",
                id: "",
                children: []
            },
            {
                path: "tournament",
                name: "联赛管理",
                id: "",
                children: []
            },
            {
                path: "teams",
                name: "战队管理",
                id: "",
                children: []
            },
            {
                path: "odd",
                name: "玩法管理",
                id: "",
                children: []
            }
        ]
    },

    {
        name: "操盘管理",
        id: "",
        pathAll: ["matchEntry", "matchAudit", "matchMain", "mktMonitor", "stopMktMonitor"],
        children: [
            {
                path: "matchEntry",
                name: "赛事录入",
                id: "",
                children: []
            },
            // {
            //     path:'matchAudit',
            //     name: "赛事审核",
            //     id:'',
            //     children: []
            // },
            {
                path: "matchMain",
                name: "赛事主页",
                id: "",
                children: []
            },
            {
                path: "stopMktMonitor",
                name: "暂停盘口监控",
                id: "",
                children: []
            },
            {
                path: "mktMonitor",
                name: "盘口预警监控",
                id: "",
                children: []
            }
        ]
    },

    {
        name: "注单管理",
        id: "",
        pathAll: [
            "currentOrder",
            "cuurentParleyOrder",
            'realTimeRoundList',
            "historyOrder",
            "historyParleyOrder",
            'historyRoundList',
            "cimpleRevoke",
            "complexRevoke"
        ],
        children: [
            {
                path: "currentOrder",
                name: "单注实时注单",
                id: "",
                children: []
            },
            {
                path: "cuurentParleyOrder",
                name: "串关实时注单",
                id: "",
                children: []
            },
            {
                path: "realTimeRoundList",
                name: "局内串关实时注单",
                id: "",
                children: []
            },
            {
                path: "historyOrder",
                name: "单注注单查询",
                id: "",
                children: []
            },
            {
                path: "historyParleyOrder",
                name: "串关注单查询",
                id: "",
                children: []
            },
            {
                path: "historyRoundList",
                name: "局内串关查询",
                id: "",
                children: []
            },
            {
                path: "cimpleRevoke",
                name: "单注撤单",
                id: "",
                children: []
            },
            {
                path: "complexRevoke",
                name: "串关撤单",
                id: "",
                children: []
            }
        ]
    },

    {
        name: "结算管理",
        id: "",
        pathAll: ["matchSettle", "matchRecord", "reSettle"],
        children: [
            {
                path: "matchSettle",
                name: "赛事结算",
                id: "",
                children: []
            },
            {
                path: "matchRecord",
                name: "赛事记录",
                id: "",
                children: []
            },
            {
                path: "reSettle",
                name: "重新结算",
                id: "",
                children: []
            }
        ]
    },

    {
        name: "商户管理",
        id: "",
        pathAll: ["merchantList"],
        children: [
            {
                path: "merchantList",
                name: "商户列表",
                id: "",
                children: []
            },
        ]
    },

    {
        name: "风控管理",
        id: "",
        pathAll: ["riskCommon", "matchLevel","riskWinLimit","riskParams", 'riskMember'],
        children: [
            {
                path: "riskCommon",
                name: "基本参数设置",
                id: "",
                children: []
            },
            {
                path: "matchLevel",
                name: "赛事等级设置",
                id: "",
                children: []
            },
            {
                path:'riskWinLimit',
                name: "限红比例设置",
                id:'',
                children: []
            },
            {
                path:'riskParams',
                name: "风险参数设置",
                id:'',
                children: []
            },
            // {
            //     path:'riskMonitor',
            //     name: "风险监控",
            //     id:'',
            //     children: []
            // },
            {
                path:'riskMember',
                name: "风险会员",
                id:'',
                children: []
            },
        ]
    },

    {
        name: "报表管理",
        id: "",
        pathAll: [
            "reportRecord",
            "merchantReport",
            "gameReport",
            "matchReport",
            "parleyReport",
            "memberReport",
            "traderReport",
            "impact",
            'roundParleyReport'
        ],
        children: [
            {
                path: "reportRecord",
                name: "总盈利",
                id: "",
                children: []
            },
            {
                path: "merchantReport",
                name: "顶级商户盈利",
                id: "",
                children: []
            },
            {
                path: "gameReport",
                name: "游戏盈利",
                id: "",
                children: []
            },
            {
                path: "matchReport",
                name: "赛事盈利",
                id: "",
                children: []
            },
            {
                path: "parleyReport",
                name: "串关盈利",
                id: "",
                children: []
            },
            {
                path: "roundParleyReport",
                name: "局内串关盈利",
                id: "",
                children: []
            },
            {
                path: "memberReport",
                name: "会员盈利",
                id: "",
                children: []
            },
            {
                path: "impact",
                name: "父商户冲正统计",
                id: "",
                children: []
            }
            // {
            //     path:'traderReport',
            //     name: "操盘手盈利",
            //     id:'',
            //     children: []
            // },
        ]
    },

    {
        name: "会员管理",
        id: "",
        pathAll: ["memberList", "memberTransfer", "memberLevel", "memberLog"],
        children: [
            {
                path: "memberList",
                name: "会员列表",
                id: "",
                children: []
            },
            {
                path: "memberTransfer",
                name: "账变记录",
                id: "",
                children: []
            }
            // {
            //     path:'memberLevel',
            //     name: "会员等级设置",
            //     id:'',
            //     children: []
            // },
            // {
            //     path:'memberLog',
            //     name: "会员日志",
            //     id:'',
            //     children: []
            // }
        ]
    },

    {
        name: "会员公告",
        id: "",
        pathAll: ["memberNotice"],
        children: [
            {
                path: "notice",
                name: "会员公告",
                id: "",
                children: []
            }
        ]
    },

    {
        name: "权限管理",
        id: "",
        pathAll: ["priv", "user", "group"],
        children: [
            {
                path: "priv",
                name: "权限分配",
                id: "",
                children: []
            },
            {
                path: "user",
                name: "后台账号管理",
                id: "",
                children: []
            },
            {
                path: "group",
                name: "分组类型管理",
                id: "",
                children: []
            }
        ]
    },

    {
        name: "日志管理",
        id: "",
        pathAll: ["loginLog", "operationLog", "matchLog", "handicapLog"],
        children: [
            {
                path: "loginLog",
                name: "登录日志",
                id: "",
                children: []
            },
            {
                path: "operationLog",
                name: "操作日志",
                id: "",
                children: []
            },
            {
                path: "matchLog",
                name: "赛事操作日志",
                id: "",
                children: []
            },
            {
                path: "handicapLog",
                name: "盘口操作日志",
                id: "",
                children: []
            },
        ]
    },

    {
        name: "域名管理",
        id: "",
        pathAll: ["webManagement", "domain"],
        children: [
            {
                path: "webManagement",
                name: "站点管理",
                id: "",
                children: []
            },
            {
                path: "domain",
                name: "静态域名管理",
                id: "",
                children: []
            }
        ]
    },

    // {
    //     name: "爬虫数据页面",
    //     id: "",
    //     pathAll: ["spider"],
    //     children: [
    //         {
    //             path: "spider",
    //             name: "爬虫数据",
    //             id: "",
    //             children: []
    //         }
    //     ]
    // },

    {
        name: "基本权限管理",
        id: "",
        pathAll: ["basis"],
        children: [
            {
                path: "basis",
                name: "基本权限",
                id: "",
                children: []
            }
        ]
    },
];
