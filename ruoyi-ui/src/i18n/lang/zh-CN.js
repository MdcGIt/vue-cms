export default {
  APP: {
    TITLE: 'RuoYi-Vue-CMS'
  },
  Common: {
    Search: '搜索',
    Reset: '重置',
    More: '更多',
    None: '无',
    OK: 'OK',
    Confirm: '确定',
    Cancel: '取消',
    Open: '开启',
    Close: '关闭',
    Submit: '提交',
    GoBack: '返回',
    Success: '成功',
    Fail: '失败',
    Normal: '正常',
    OpSuccess: '操作成功',
    OpFail: '操作失败',
    AddSuccess: '添加成功',
    SaveSuccess: "保存成功",
    EditSuccess: '修改成功',
    DeleteSuccess: '删除成功',
    Yes: '是',
    No: '否',
    Enable: "启用",
    Disable: "禁用",
    BeginDate: '开始日期',
    EndDate: '结束日期',
    BeginTime: '开始时间',
    EndTime: '结束时间',
    Add: '新增',
    Edit: '修改',
    Save: '保存',
    Delete: '删除',
    Remove: "移除",
    Import: '导入',
    Export: '导出',
    Refresh: '刷新',
    RefreshCache: '刷新缓存',
    Clean: "清空",
    View: "查看",
    Show: "显示",
    Hide: "隐藏",
    Move: "移动",
    Copy: "复制",
    Sort: "排序",
    Details: "详情",
    Remark: '备注',
    CreateTime: '创建时间',
    CreateBy: '创建人',
    UpdateTime: '更新时间',
    UpdateBy: '更新人',
    Operation: '操作',
    Select: '选择',
    Upload: '上传',
    RowNo: '序号',
    ExpandOrCollapse: '展开 / 折叠',
    Expand: '展开',
    Collapse: '折叠',
    CheckAll: '全选',
    CheckInverse: '全不选',
    TreeLinkage: '父子联动',
    ConfirmDelete: '是否确认删除？',
    LastWeek: '最近一周',
    LastMonth: '最近一个月',
    LastThreeMonth: '最近三个月',
    SessionExpired: '登录状态已过期，您可以继续留在该页面，或者重新登录',
    Tips: "提示",
    SystemTip: '系统提示',
    Relogin: '重新登录',
    InvalidSession: '无效的会话，或者会话已过期，请重新登录。',
    RepeatSubmit: '请求正在处理，请勿重复提交',
    ServerConnectFailed: '后端接口连接异常',
    ServerConnectTimeout: '系统接口请求超时',
    ServerApiError: '系统接口{0}异常',
    Downloading: '正在下载数据，请稍候',
    Loading: '正在加载，请稍后',
    DownloadFailed: '下载文件出现错误，请联系管理员！',
    InvalidFileSuffix: '文件格式错误，仅支持：{0}',
    SelectFirst: '请先选择至少一条记录',
    RuleTips: {
      NotEmpty: "不能为空",
      Email: "Email格式错误",
      Url: "URL链接格式错误，必须以http(s)://开头",
      Code: "只能使用大小写字母、数字和下划线",
      PhoneNumber: "手机号码格式错误"
    }
  },
  Router: {
    Home: '首页',
    AccountCenter: '个人中心',
    UserPreference: '用户偏好',
    LayoutSetting: '布局设置',
    Logout: '退出登录'
  },
  Login: {
    Login: '登 录',
    Logining: '登 录 中...',
    Register: '立即注册',
    Account: '账号',
    Password: '密码',
    RememberMe: '记住密码',
    Captcha: '验证码',
    AccountRuleTip: "请输入您的账号",
    PasswordRuleTip: "请输入您的密码",
    CaptchaRuleTip: "请输入验证码",
    Language: '语言',
  },
  AccountCenter: {
    PersonInfo: '个人信息',
    Account: '用户账号',
    PhoneNumber: '手机号码',
    Email: '邮箱',
    Dept: '所属部门',
    Roles: '所属角色',
    CreateTime: '创建时间',
    Basic: '基础资料',
    ResetPassword: '修改密码',
    NickName: '昵称',
    Gender: '性别',
    GenderMale: '男',
    GenderFemale: '女',
    NickNameEmptyTip: '用户昵称不能为空',
    EmailEmptyTip: '邮箱地址不能为空',
    EmailRuleTip: '请输入正确的邮箱地址',
    PhoneNumEmptyTip: '手机号码不能为空',
    PhoneNumRuleTip: '请输入正确的手机号码',
    OldPwd: '旧密码',
    NewPwd: '新密码',
    ConfirmPwd: '确认密码',
    OldPwdPlaceHolder: '请输入旧密码',
    NewPwdPlaceHolder: '请输入新密码',
    ConfirmPwdPlaceHolder: '请确认新密码',
    OldPwdEmptyTip: '旧密码不能为空',
    NewPwdEmptyTip: '新密码不能为空',
    ConfirmPwdEmptyTip: '确认新密码不能为空',
    NewPwdLenTip: '长度必须在 {0} 到 {1} 个字符',
    NewPwdSensitiveTip: '不能包含用户信息敏感字符',
    NewPwdTip: '新密码不合符安全规则',
    NewPwdRuleLETTER_NUMBERTip: '必须包含字母、数字',
    NewPwdRuleUPPER_LOW_LETTER_NUMBERTip: '必须包含大小写字母、数字',
    NewPwdRuleLETTER_NUMBER_SPECIALTip: '必须包含字母、数字、特殊字符',
    NewPwdRuleUPPER_LOW_LETTER_NUMBER_SPECIALTip: '必须包含大小写字母、数字、特殊字符',
    ConfirmPwdErrTip: '两次输入的密码不一致',
  },
  Component: {
    Progress: {
      TaskIsRunning: "任务进行中...",
      Completed: "已完成",
    },
    CommonImageViewer: {
      UploadSizeErr: "上传文件大小不能超过 {0}!",
      Uploading: "正在上传文件，请稍候...",
      Tips: "只能上传jpg/png文件，且不超过2Mb"
    },
    Crontab: {
      Second: "秒",
      Minute: "分钟",
      Hour: "小时",
      Day: "日",
      Month: "月",
      Week: "周",
      Year: "年",
      TimeExpression: "时间表达式",
      CronExpression: "Cron表达式",
      Next5RunTime: "最近5次运行时间",
      Waiting: "计算结果中..."
    },
    IconSelect: {
      Placeholder: "请输入图标名称"
    },
    RightToolbar: {
      Show: "显示",
      Hide: "隐藏",
      ShowOrHide: "显示/隐藏",
      HideSearch: "隐藏搜索",
      ShowSearch: "显示搜索",
      ShowTableColumn: "显隐列",
    },
    Screenfull: {
      UnSupported: "您的浏览器不支持全屏"
    },
    TopNav: {
      MoreMenu: "更多菜单"
    },
    Layout: {
      Settings: {
        ThemeStyle: '主题风格设置',
        ThemeColor: '主题颜色',
        SystemLayoutConfig: '系统布局配置',
        EnableTopNav: '开启 TopNav',
        EnableTagsViews: '开启 Tags-Views',
        EnableFixedHeader: '固定 Header',
        ShowLogo: '显示 Logo',
        EnableDynamicTitle: '动态标题',
      },
      Navbar: {
        FullScreen: '全屏显示',
        LayoutSize: '布局大小',
        Language: '语言切换',
        ConfirmLogoutTip: '确定注销并退出系统吗？'
      },
      TagViews: {
        RefreshPage: "刷新页面",
        CloseCurrent: "关闭当前",
        CloseOther: "关闭其他",
        CloseLeft: "关闭左侧",
        CloseRight: "关闭右侧",
        CloseAll: "全部关闭"
      }
    }
  },
  Home: {
    Welcome: "欢迎回来",
    LastLoginTime: "上次登录时间",
    LastLoginIP: "上次登录IP"
  },
  System: {
    User: {
      UserId: "用户ID",
      UserName: "用户名",
      NickName: "昵称",
      Dept: "所属部门",
      PhoneNumber: "手机号码",
      Email: "邮箱",
      Password: "密码",
      Gender: "性别",
      Status: "状态",
      Post: "岗位",
      Role: "角色",
      ResetPwd: "重置密码",
      RoleSetting: "分配角色",
      Enable: "启用",
      Disable: "禁用",
      ConfirmChangeStatus: '确认要{0}"{1}"的用户吗？',
      ConfirmDelete: '是否确认删除ID为"{0}"的用户数据？',
      InputNewPwd: '请输入"{0}"的新密码',
      ChangePwdSucc: '修改成功，新密码是：{0}',
      ImportResult: "导入结果",
      ImportTip1: "拖到文件到此处，或<em>点击上传</em>",
      ImportUpdate: "是否更新已经存在的用户数据",
      DownloadTemplate: "下载模板",
      EditAvatar: '修改头像',
      UploadAvatar: '点击上传头像',
      SelectUser: "选择用户",
      Dialog: {
        Add: "添加用户信息",
        Edit: "编辑用户信息",
        Import: "用户导入"
      },
      Placeholder: {
        DeptName: "请输入部门名称",
        UserName: "请输入用户名称",
        NickName: "请输入用户昵称",
        PhoneNumber: "请输入手机号码",
        Dept: "请选择所属部门",
        Email: "请输入用户邮箱",
        Password: "请输入用户密码",
        Gender: "请选择用户性别",
        Status: "用户状态",
        Post: "请选择用户岗位",
        Role: "请选择用户角色"
      },
      RuleTips: {
        UserName1: "用户名称不能为空",
        UserName2: "用户名称长度必须介于 2 和 20 之间",
        DeptId: "所属机构不能为空",
        NickName: "用户昵称不能为空",
        Password1: "用户密码不能为空",
        Email: "请输入正确的邮箱地址",
        PhoneNumber: "请输入正确的手机号码"
      }
    },
    UserPreference: {
      Shortcut: "快捷导航菜单",
      StatIndex: "统计分析默认菜单",
      IncludeChildContent: "内容列表是否显示子栏目内容",
      OpenContentEditorW: "内容编辑是否使用新窗口",
      ShowContentSubTitle: "默认显示内容副标题"
    },
    UserRole: {
      UserInfo: "用户信息",
      Roles: "角色信息",
      AddUser: "添加用户",
      RemoveUser: "取消授权"
    },
    Role: {
      RoleId: "角色ID",
      RoleName: "角色名称",
      RoleKey: "角色编码",
      RoleKeyTips: "不能为空且只能使用字母、数字和下划线",
      Sort: "显示顺序",
      Status: "状态",
      UserSetting: "分配用户",
      MenuPerms: "菜单权限",
      PermissionSetting: "权限设置",
      Enable: "启用",
      Disable: "禁用",
      ConfirmChangeStatus: '确认要{0}"{1}"的角色吗？',
      DataScope: "数据权限",
      ConfirmRemoveUser: "是否确认从角色用户列表移除选中用户？",
      Dialog: {
        Add: "添加角色信息",
        Edit: "修改角色信息",
        DataScope: "分配数据权限"
      },
      Placeholder: {
        RoleName: "请输入角色名称",
        RoleKey: "请输入角色编码",
        Status: "角色状态"
      },
      RuleTips: {
        RoleName: "角色名称不能为空",
        RoleKey: "角色编码不能为空",
        Sort: "排序字段不能为空"
      }
    },
    Menu: {
      MenuName: "菜单名称",
      MenuType: "菜单类型",
      Icon: "图标",
      Status: "状态",
      StatusTips: "选择停用则路由将不会出现在侧边栏，也不能被访问",
      Sort: "排序",
      Perms: "权限标识",
      Component: "组件路径",
      ComponentTips: "访问的组件路径，如：`system/user/index`，默认在`views`目录下",
      ParentMenu: "上级菜单",
      TypeDir: "目录",
      TypeMenu: "菜单",
      TypeBtn: "按钮",
      IsExternalLink: "是否外链",
      IsExternalLinkTips: "选择是外链则路由地址需要以`http(s)://`开头",
      RouterLink: "路由地址",
      RouterLinkTips: "访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头",
      Perms: "权限字符",
      PermsTips: "控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasPermi('system:user:list')`)",
      RouterParams: "路由参数",
      RouterParamsTips: '访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`',
      IsCache: "是否缓存",
      IsCacheTips: "选择是则会被`keep-alive`缓存，需要匹配组件的`name`和地址保持一致",
      EnableCache: "缓存",
      DisableCache: "不缓存",
      Visible: "显示状态",
      VisibleTips: "择隐藏则路由将不会出现在侧边栏，但仍然可以访问",
      ConfirmDelete: '是否确认删除名称为"{0}"的数据项？',
      Dialog: {
        Add: "添加菜单信息",
        Edit: "修改菜单信息"
      },
      Placeholder: {
        MenuName: "请输入菜单名称",
        Status: "菜单状态",
        ParentMenu: "选择上级菜单",
        Icon: "点击选择图标",
        RouterLink: "请输入路由地址",
        Component: "请输入组件路径",
        Perms: "请输入权限字符",
        RouterParams: "请输入路由参数",
      },
      RuleTips: {
        MenuName: "菜单名称不能为空",
        Sort: "菜单顺序不能为空",
        RouterLink: "路由地址不能为空"
      },
    },
    Dept: {
      DeptName: "部门名称",
      Status: "部门状态",
      ParentDept: "上级部门",
      Sort: "显示排序",
      Leader: "负责人",
      Phone: "联系电话",
      Email: "邮箱地址",
      ConfirmDelete: '是否确认删除名称为"{0}"的数据项？',
      Dialog: {
        Add: "添加部门信息",
        Edit: "编辑部门信息"
      },
      Placeholder: {
        DeptName: "请输入部门名称",
        Status: "部门状态",
        ParentDept: "请选择上级部门",
        Leader: "请输入负责人",
        Phone: "请输入联系电话",
        Email: "请输入邮箱地址"
      },
      RuleTips: {
        DeptName: "部门名称不能为空",
        ParentDept: "上级部门不能为空",
        Sort: "显示排序不能为空",
        Email: "请输入正确的邮箱地址",
        Phone: "请输入正确的手机号码"
      }
    },
    Post: {
      PostId: "岗位ID",
      PostCode: "岗位编码",
      PostName: "岗位名称",
      Status: "状态",
      PostSort: "岗位排序",
      ConfirmDelete: '是否确认删除岗位编号为"{0}"的数据项？',
      Dialog: {
        Add: "添加岗位信息",
        Edit: "修改岗位信息"
      },
      Placeholder: {
        PostCode: "请输入岗位编码",
        PostName: "请输入岗位名称",
        Status: "岗位状态"
      },
      RuleTips: {
        PostName: "岗位名称不能为空",
        PostCode: "岗位编码不能为空",
        PostSort: "岗位顺序不能为空"
      }
    },
    Dict: {
      DictName: "字典名称",
      DictType: "字典类型",
      DictCode: "字典项编码",
      DictLabel: "字典项标签",
      DictValue: "字典项值",
      DictSort: "字典排序",
      SystemFixed: "系统固定",
      CssClass: "样式属性",
      ListClass: "回显样式",
      Status: "状态",
      DataList: "字典数据",
      ConfirmDelete: '是否确认删除字典ID为"{0}"的数据项？',
      ConfirmDeleteDatas: '是否确认删除字典编码为"{0}"的数据项？',
      Dialog: {
        Add: "添加字典类型",
        Edit: "修改字典类型信息",
        AddData: "添加字典数据",
        EditData: "修改字典数据"
      },
      Placeholder: {
        DictName: "请输入字典名称",
        DictType: "请输入字典类型",
        DictCode: "请输入字典项编码",
        DictLabel: "请输入字典项标签",
        DictValue: "请输入字典项值",
        CssClass: "请输入样式属性",
        Status: "字典状态"
      },
      RuleTips: {
        DictName: "字典名称不能为空",
        DictType: "字典类型不能为空",
        DictCode: "字典项编码不能为空",
        DictLabel: "字典项标签不能为空",
        DictValue: "字典项值不能为空",
        DictSort: "字典顺序不能为空",
      }
    },
    Config: {
      ConfigId: "参数ID",
      ConfigName: "参数名称",
      ConfigKey: "参数键名",
      ConfigValue: "参数键值",
      ConfigType: "固定配置",
      ConfirmDelete: '是否确认删除参数ID为"{0}"的数据项？',
      Dialog: {
        Add: "添加参数配置",
        Edit: "修改参数配置"
      },
      Placeholder: {
        ConfigName: "请输入参数名称",
        ConfigKey: "请输入参数键名",
        ConfigValue: "请输入参数键值",
        ConfigType: "固定配置",
      },
      RuleTips: {
        ConfigName: "参数名称不能为空",
        ConfigKey: "参数键名不能为空",
        ConfigValue: "参数键值不能为空",
      }
    },
    Notice: {
      NoticeTitle: "公告标题",
      NoticeType: "类型",
      NoticeContent: "内容",
      Status: "状态",
      ConfirmDelete: '是否确认删除公告编号为"{0}"的数据项？',
      Dialog: {
        Add: "添加公告",
        Edit: "修改公告"
      },
      RuleTips: {
        NoticeTitle: "公告标题不能为空",
        NoticeType: "公告类型不能为空"
      }
    },
    I18n: {
      LangTag: "语言标签",
      LangKey: "国际化键名",
      LangValue: "国际化键值",
      EditorTitle: "国际化【{0}】",
      Dialog: {
        Add: "添加国际化配置",
        Edit: "修改国际化配置"
      },
      RuleTips: {
        LangTag: "语言标签不能为空",
        LangKey: "国家化键名不能为空",
        LangValue: "国家化键值不能为空",
      }
    },
    IPRule: {
      Type: "类型",
      ExpireTime: "过期时间",
      CIDR: "掩码（CIDR）",
      Dialog: {
        Add: "添加IP规则信息",
        Edit: "修改IP规则信息",
      },
      RuleTips: {
        IP: "IP地址不能为空",
        Type: "类型不能为空",
        CIDR: "掩码（CIDR）不能为空"
      }
    },
    OpLog: {
      Title: "标题",
      Operator: "操作人员",
      OpType: "操作类型",
      ResponseCode: "响应状态",
      OpTime: "操作时间",
      Cost: "操作耗时",
      LogId: "日志ID",
      RequestMethod: "请求方式",
      IP: "操作IP",
      Location: "操作地址",
      Details: "详情",
      DetailsTitle: "操作日志详情",
      LoginInfo: "登录信息",
      RequestUrl: "请求地址",
      Method: "操作方法",
      RequestParams: "请求参数",
      Response: "返回数据",
      ConfirmDelete: '是否确认删除日志编号为"{0}"的数据项？',
      ConfirmClean: '是否确认清空所有操作日志数据项？'
    },
    LoginInfo: {
      LogId: "日志ID",
      IP: "IP地址",
      UserType: '用户类型',
      UserId: "用户ID",
      UserName: "用户名",
      Status: "登录状态",
      LoginTime: "登录时间",
      Unlock: "解锁",
      Location: "登录地点",
      Browser: "浏览器",
      OS: "操作系统",
      OpMsg: "操作信息",
      ConfirmUnlock: '是否确认解锁用户：{0}？',
      UnlockSuccess: '用户"{0}"解锁成功'
    },
    GenCode: {
      EditGenConfig: '修改生成配置'
    },
    Security: {
      PasswordLength: "密码长度",
      PasswordRule: "密码校验规则",
      PasswordExpireSeconds: "密码过期时长",
      PasswordRetryLimit: "密码重试阈值",
      Status: "状态",
      PasswordConfig: "密码安全配置",
      PasswordMinLength: "密码最小长度",
      PasswordMaxLength: "密码最大长度",
      PasswordRule: "密码组成规则",
      PasswordSensitive: "密码敏感字符",
      WeakPasswords: "弱密码",
      WeakPasswordsPlaceholder: "每行一个弱密码",
      PasswordExpireSeconds: "密码过期时长（单位：秒）",
      PasswordExpireSecondsTip: "0表示永不过期，最长不超过100天",
      ForceModifyPwdAfterAdd: "首次登录是否强制修改密码",
      ForceModifyPwdAfterAddTip: "仅适用于后台添加用户",
      ForceModifyPwdAfterReset: "重置密码后是否强制修改密码",
      ForceModifyPwdAfterResetTip: "仅适用于后台重置用户密码",
      LoginConfig: "登录校验配置",
      PasswordRetryLimit: "每日密码错误次数阈值",
      PasswordRetryLimitTip: "0表示无限制",
      PasswordRetryStrategy: "密码错误阈值处理策略",
      PasswordRetryLockSeconds: "锁定用户时长（单位：秒）",
      PasswordRetryLockSecondsTip: "最长不超过365天",
      AddTitle: "添加安全配置",
      EditTitle: "编辑安全配置",
    }
  },
  Monitor: {
    Job: {
      JobId: "任务ID",
      JobName: "任务名称",
      JobGroup: "任务分组",
      Status: "状态",
      InvokeTarget: "调用目标字符串",
      CronExpression: "cron执行表达式",
      CronGenerator: "Cron表达式生成器",
      GenCron: "生成表达式",
      Concurrent: "是否并发",
      Allow: "允许",
      Forbid: "禁止",
      Enable: "启用",
      Disable: "禁用",
      MisfirePolicy: "执行策略",
      DefaultPolicy: "默认策略",
      RunNow: "立即执行",
      RunOnce: "执行一次",
      Manual: "手动执行",
      Details: "任务详情",
      BeanExample: "Bean调用示例：ryTask.ryParams('ry')",
      ClassExample: "Class类调用示例：com.test.quartz.task.RyTask.ryParams('ry')",
      ParamsTips: "参数说明：支持字符串，布尔类型，长整型，浮点型，整型",
      NextRunTime: "下次执行时间",
      ConfirmDelete: '确认要删除ID为"{0}"的任务吗？',
      ConfirmChangeStatus: '确认要"{0}""{1}"任务吗？',
      ConfirmRunOnce: '确认要立即执行一次"{0}"任务吗？',
      Logs: "调度日志",
      ExecTime: "执行时间",
      JobLogId: "日志ID",
      JobLogMessage: "日志信息",
      LogDetails: "详情",
      ExceptionInfo: "异常信息",
      ConfirmClean: '是否确认清空所有调度日志数据项？',
      Dialog: {
        Add: "添加任务信息",
        Edit: "修改任务信息",
        Log: "调度日志详细",
      },
      Placeholder: {
        InvokeTarget: "请输入调用目标字符串"
      },
      RuleTips: {
        JobName: "任务名称不能为空",
        InvokeTaget: "调用目标字符串不能为空",
        CronExpression: "cron执行表达式不能为空"
      }
    },
    ScheduledTask: {
      Type: "类型",
      Status: "状态",
      ReadyTime: "准备就绪时间",
      ExecOnce: "执行一次",
      Logs: "日志",
      TriggerType: "触发方式",
      CronExpression: "CRON表达式",
      TriggerTypePeriodic: "定期执行",
      FixedRate: "固定间隔",
      FixedRateTipY: "是：两次任务开始时间之间间隔指定时长",
      FixedRateTipN: "否：上一次任务的结束时间与下一次任务开始时间间隔指定时长",
      PeriodicSeconds: "间隔时间（秒）",
      PeriodicDelaySeconds: "首次执行延时（秒）",
      ExecResult: "执行结果",
      ErrMsg: "错误信息",
      AddTtitle: "添加任务",
      EditTitle: "编辑任务信息",
      LogDialogTitle: "任务执行记录"
    },
    Logs: {
      LogId: "日志分类ID",
      LogName: "日志分类名称"
    },
    Online: {
      LoginIP: "登录IP",
      UserName: "用户名称",
      TokenID: "TokenID",
      DeptName: "部门名称",
      LoginLocation: "登录地址",
      Browser: "浏览器",
      OS: "操作系统",
      LoginTime: "登录时间",
      ForceExit: "强退",
      ConfirmForceExit: '是否确认强退名称为"{0}"的用户？',
    },
    Server: {
      Loading: "正在加载服务监控数据，请稍候！",
      Attribute: "属性",
      Value: "值",
      CPU: "CPU",
      CPUCoreNum: "核心数",
      CPUUserUsage: "用户使用率",
      CPUSysUsage: "系统使用率",
      CPUIdle: "当前空闲率",
      Memory: "内存",
      JVM: "JVM",
      TotalMemory: "总内存",
      UsedMemory: "已用内存",
      LeftMemory: "剩余内存",
      MemoryUsage: "使用率",
      ServerInfo: "服务器信息",
      ServerName: "服务器名称",
      OSName: "操作系统",
      ServerIP: "服务器IP",
      OSArch: "系统架构",
      JVMInfo: "Java虚拟机信息",
      JVMName: "Java名称",
      JVMVersion: "Java版本",
      JVMStartTime: "启动时间",
      JVMRunTime: "运行时间",
      JVMHome: "安装了路径",
      ProjectDir: "项目路径",
      JVMArgs: "运行参数",
      Disk: "磁盘状态",
      DiskPath: "盘符路径",
      FileSystem: "文件系统",
      DiskType: "盘符类型",
      DiskSize: "总大小",
      DiskLeftSize: "可用大小",
      DiskUsedSize: "已用大小",
      DiskUsedPercent: "已用百分比"
    },
    Cache: {
      Basic: "基本信息",
      RedisVersion: "Redis版本",
      RunMode: "运行模式",
      Port: "端口",
      ClientNumber: "客户端数",
      UpTimeInDays: "运行时间(天)",
      Memory: "使用内存",
      CPU: "使用CPU",
      MaxMemory: "内存配置",
      AOFStatus: "AOF是否开启",
      RDBStatus: "RDB是否成功",
      DBSize: "Key数量",
      InputKbps: "网络入口/出口",
      CommandStat: "命令统计",
      MemoryInfo: "内存信息",
      Command: "命令",
      PeakValue: "峰值",
      MemoryCost: "内存消耗",
      CacheList: "缓存列表",
      CacheName: "缓存名称",
      CacheKeyList: "键名列表",
      CacheKey: "缓存键名",
      CacheValue: "缓存内容",
      ClearCache: "清理全部",
      ExpireTime: "过期时间（单位：秒）",
      ClearSuccess: "清理缓存名称[{0}]成功"
    },
    Async: {
      Type: "类型",
      TaskID: "任务ID",
      Status: "状态",
      Percent: "进度",
      ErrMessage: "错误信息",
      Stop: "中止",
      ReadyTime: "任务就绪时间"
    }
  },
  CMS: {
    ContentCore: {
      ContentType: "内容类型",
      CatalogType: "栏目类型",
      InternalDataType: "内部数据类型",
      ResourceType: "资源类型",
      InternalUrl: "内部链接",
      SelectCatalog: "选择栏目",
      SelectContent: "选择内容",
      Publish: "发布",
      Preview: "预览",
      PublishSuccess: "发布成功",
      PublishProgressTitle: "发布任务",
      ApplyToCatalog: "应用到栏目",
      SEOConfig: "SEO配置",
      SEOTitle: "SEO标题",
      SEOKeyword: "SEO关键字",
      SEODescription: "SEO描述",
      PublishPipe: "发布通道",
      Route: {
        EditSite: "站点编辑",
        EditTemplate: "模板编辑",
        EditFile: "文件编辑",
        EditContent: "内容编辑",
        EditManualBlock: "编辑区块",
        EditAdvSpace: "编辑广告位",
        EditAdv: "编辑广告",
      }
    },
    UEditor: {
      InsertResource: "插入素材",
      ResourceType: {
        Image: "图片",
        Audio: "音频",
        Video: "视频",
        File: "文件"
      },
      InsertThirdVideo: "插入第三方视频分享",
      ThirdVideo: {
        DialogTitle: "插入第三方视频分享",
        Code: "第三方视频分享代码",
        CodeTip: "请使用第三方视频网站分享的`&lt;iframe&gt;`嵌入代码",
        Width: "宽度",
        Height: "高度",
        Align: "对齐方式",
        AlignCenter: "居中对齐",
        AlignLeft: "左对齐",
        AlignRight: "右对齐",
        RuleTips: {
          Code: "不能为空",
          IframeSrc: "嵌入代码的<iframe>标签缺少src属性！"
        }
      }
    },
    Site: {
      SiteId: "站点ID",
      Name: "站点名称",
      Path: "站点目录",
      Logo: "LOGO",
      PublishHome: "发布首页",
      PublishAll: "发布全站",
      PublishPublished: "发布已发布的内容",
      PublishToPublish: "发布待发布的内容",
      PublishDraft: "发布初稿的内容",
      PublishReEdit: "发布重新编辑的内容",
      PublishOffline: "发布已下线的内容",
      BasicCardTitle: "基础属性",
      ResourceUrl: "资源域名",
      Desc: "描述",
      DeleteProgressTitle: "删除站点",
      PublishPipeCardTitle: "发布通道属性",
      StaticFileType: "静态文件类型",
      IndexTemplate: "首页模板",
      NoPublishPipeTip: "预览/发布需要先添加发布通道",
      GoAddPublishPipe: "去添加",
      ExModelCardTitle: "扩展模型属性",
      CurrentSelectorTitle: "切换当前站点",
      AlreayCurrentSite: "请选择非当前站点",
      Tab: {
        Basic: "基础信息",
        Extend: "扩展配置",
        Property: "扩展属性",
        DefaultTemplate: "默认模板"
      },
      Dialog: {
        AddTitle: "新建站点"
      },
      RuleTips: {
        Name: "站点名称不能为空",
        Path: "不能为空且只能使用大小写字母和数字"
      },
      Extend: {
        BasicCardTitle: "基础配置",
        EnableIndex: "是否开启索引",
        TitleRepeatCheck: "标题查重",
        TitlteRepeatCheckNone: "不校验",
        TitlteRepeatCheckSite: "全站校验",
        TitlteRepeatCheckCatalog: "栏目校验",
        ExModel: "站点扩展模型",
        PublishMaxPageNum: "内容发布更新列表页数",
        EnableEditPublished: "允许编辑已发布内容",
        EnableSSI: "是否启用SSI",
        ContentConfCardTitle: "内容配置",
        AutoArticleLogo: "正文首图作为logo",
        RecycleKeepDays: "回收站内容保留天数",
        RecycleKeepDaysTip: "永久保留填0",
        ResourceConfCardTitle: "素材库",
        StorageType: "存储策略",
        Local: "本地",
        AliyunOSS: "阿里云OSS",
        TencentCOS: "腾讯云COS",
        MinIO: "MinIO",
        ImageWatermarkCardTitle: "图片水印配置",
        ImageWatermark: "开启图片水印",
        WatermarkImage: "水印图片",
        WatermarkPosition: "水印位置",
        WatermarkOpacity: "不透明度",
        WatermarkOpacityTip: "数值越低透明度越高，默认1不透明",
        WatermarkRatio: "占比",
        WatermarkRatioTip: "水印占目标图片的比例，水印宽高至少20",
        WordConfCardTitle: "词汇配置",
        SensitiveWordEnable: "开启敏感词替换",
        ErrorProneWordEnable: "开启易错词替换",
        HotWordGroup: "热词分组",
        StatConfCardTitle: "统计配置",
        BaiduApiKey: "百度统计ApiKey",
        BaiduSecretKey: "百度统计SecretKey",
        BaiduRefreshToken: "百度统计RefreshToken",
        BaiduAccessToken: "百度统计AccessToken",
      },
      Property: {
        QueryPlaceholder: "输入名称/编码查询",
        PropName: "属性名称",
        PropCode: "属性编码",
        PropValue: "属性值",
        RuleTips: {
          PropName: "不能为空",
          PropCode: "不能为空且只能使用字母、数字和下划线"
        }
      },
      DefaultTemplate: {
        Title: "默认模板配置",
        CatalogList: "栏目列表页模板",
        ContentDetail: "详情页模板",
        SelectCatalogFirst: "请先选择栏目"
      }
    },
    Catalog: {
      Tab: {
        Basic: "基本信息",
        Extend: "扩展配置",
      },
      AddCatalog: "添加栏目",
      CatalogNamePlaceholder: "输入栏目名称",
      ParentCatalog: "上级栏目",
      Basic: "基础属性",
      Content: "内容",
      SortTip: "正数下移，负数上移",
      DeleteTip: "删除栏目会删除包括子栏目下所有数据确定删除吗？",
      CatalogId: "栏目ID",
      Name: "栏目名称",
      Alias: "栏目别名",
      Path: "栏目路径",
      CatalogType: "栏目类型",
      RedirectUrl: "链接地址",
      Desc: "栏目描述",
      Logo: "Logo",
      PublishPipeConf: "发布通道属性",
      IndexTemplate: "栏目首页模板",
      ListTemplate: "栏目列表页模板",
      DetailTemplate: "详情页模板",
      ContentExTemplate: "内容扩展模板",
      ApplyToChildren: "应用到子栏目",
      ExModelProps: "扩展模型属性",
      PublishDialogTitle: "发布栏目",
      PublishTips: "本次生成的静态页面将覆盖原有页面，确认生成所有静态页面吗？",
      ContainsChildren: "包含子栏目",
      SelectCatalog: "选择栏目",
      CopyContent: "拷贝副本",
      CopyContentTip: "拷贝副本：完整复制内容，独立于来源自由修改。",
      MappingContent: "映射内容",
      MappingContentTip: "映射内容：仅生成基础信息，独立页面，扩展内容共享自源内容。",
      RuleTips: {
        Name: "栏目名称不能为空",
        Alias: "不能为空且只能使用字母、数字和下划线",
        Path: "不能为空且只能使用字母、数字和下划线",
        CatalogType: "栏目类型不能为空"
      },
      SelectCatalogFirst: "请先选择一个栏目",
      PublishProgressTitle: "发布栏目",
      DeleteProgressTitle: "删除栏目",
      MoveProgressTitle: "转移栏目",
      Extend: {
        Basic: "基础配置",
        EnableIndex: "是否启用索引",
        CatalogExModel: "栏目扩展模型",
        ContentExModel: "内容扩展模型",
        ContentConfig: "内容配置",
        WordConfig: "词汇配置",
        HotWordGroup: "热词分组"
      }
    },
    Content: {
      Tab: {
        ContentList: "内容列表",
        PageWidget: "页面部件",
        RecycleBin: "回收站"
      },
      Placeholder: {
        Title: "输入内容标题",
      },
      Title: "标题",
      SubTitle: "副标题",
      ShortTitle: "短标题",
      ContentType: "内容类型",
      Status: "状态",
      SetTop: "置顶",
      CancelTop: "取消置顶",
      TopEndTime: "置顶结束时间",
      Offline: "下线",
      Archive: "归档",
      GenIndex: "生成索引",
      SortOption: {
        Default: "默认排序",
        CreateTimeAsc: '添加时间升序',
        CreateTimeDesc: '添加时间降序',
        PublishDateAsc: '发布时间升序',
        PublishDateDesc: '发布时间降序'
      },
      Restore: "恢复",
      SelectCatalogFirst: "请先选择一个栏目",
      StatusBefore: " 删除前状态",
      DeleteTime: "删除时间",
      DeleteUser: "删除人",
      BackToList: "返回列表",
      Lock: "锁定",
      Unlock: "解锁",
      LinkFlag: "链接内容",
      RedirectUrl: "链接地址",
      DownloadImage: "下载图片",
      DownloadImageTip: "下载文章正文中的远程图片",
      Basic: "基本属性",
      Catalog: "所属栏目",
      Logo: "LOGO",
      Author: "作者",
      Editor: "编辑",
      Original: "原创",
      Attribute: "属性",
      Summary: "摘要",
      Tags: "标签",
      Keywords: "关键词",
      Source: "来源",
      SourceUrl: "来源地址",
      PublishDate: "发布时间",
      OfflineDate: "下线时间",
      PublishPipe: "发布通道",
      StaticPath: "独立路径",
      Template: "独立模板",
      RuleTips:  {
        Title: "标题不能为空"
      },
      SaveProgressTitle: "保存内容中",
      SortDialogTitle: "内容排序",
      SortDialogTip: "内容将会排在下方列表选中的内容之前，选中内容置顶则排序内容也会添加置顶状态，反之则会取消置顶状态。",
      CloseContentEditorTip: "关闭页面会导致未保存的内容丢失，确认关闭吗？",
      ExtendConfig: "扩展配置",
      ExtendTemplate: "扩展模板",
    },
    Image: {
      Title: "标题",
      Summary: "摘要",
      RedirectUrl: "链接",
      MoveUp: "上移",
      MoveDown: "下移",
      SetLogo: "设为LOGO",
      Add: "添加图片",
      WidthHeight: "宽高",
      FileSize: "大小"
    },
    Audio: {
      Parameter: "参数",
      Type: "类型",
      FileSize: "大小",
      Format: "格式",
      Duration: "时长",
      Decoder: "编码方式",
      Channels: "声道数",
      BitRate: "比特率",
      SamplingRate: "采样率",
      Title: "标题",
      Author: "作者",
      Desc: "简介",
      MoveUp: "上移",
      MoveDown: "下移",
      Add: "添加音频"
    },
    Video: {
      Parameter: "参数",
      Type: "类型",
      FileSize: "大小",
      WidthHeight: "分辨率",
      Format: "格式",
      Duration: "时长",
      Decoder: "编码方式",
      BitRate: "比特率",
      FrameRate: "帧率",
      Details: "详情",
      Title: "标题",
      Desc: "简介",
      MoveUp: "上移",
      MoveDown: "下移",
      Add: "添加视频"
    },
    PageWidget: {
      Type: "类型",
      Name: "名称",
      Code: "编码",
      PublishPipe: "发布通道",
      Path: "发布目录",
      Template: "模板",
      InvalidPageWidgetId: "页面部件ID错误：{0}",
      Placeholder: {
        Type: "选择类型"
      },
      RuleTips: {
        Type: "类型不能为空",
        Name: "名称不能为空",
        Code: "不能为空且只能使用字母、数字和下划线",
        PublishPipe: "发布通道不能为空",
        Path: "不能为空且只能使用字母、数字和下划线"
      },
      AddTitle: "添加页面部件",
    },
    Adv: {
      Basic: "基础属性",
      AdList: "广告列表",
      AdName: "广告名称",
      Type: "类型",
      Status: "状态",
      Weight: "权重",
      OnlineDate: "上线时间",
      OfflineDate: "下线时间",
      Placeholder: {
        Name: "广告名称"
      },
      AddSpaceTitle: "添加广告位",
      GoBack: "返回上一页",
      AdMaterials: "广告素材",
      RedirectUrl: "跳转链接",
      RuleTips: {
        Type: "类型不能为空",
        Name: "名称不能为空",
        Weight: "权重不能为空",
        OnlineDate: "上线时间不能为空",
        OfflineDate: "下线时间不能为空"
      }
    },
    Block: {
      Basic: "基础属性",
      ManualList: "自定义列表",
      Title: "标题",
      AddRow: "添加行",
      Clean: "清空",
      InsertRow: "插入行",
      Link: "链接",
      Summary: "摘要",
      Date: "日期",
      AddItem: "添加列表项",
      EditItem: "编辑列表项"
    },
    Resource: {
      Name: "资源名称",
      Type: "类型",
      StorageType: "存储类型",
      FileSize: "大小",
      UploadResource: "上传资源",
      UploadTip1: "将文件拖到此处，或点击上传",
      UploadTip2: "只能上传{0}文件，且不超过{1}",
      AddDialogTitle: "添加资源",
      EditDialogTitle: "编辑资源",
      FileTypeErrMsg: "文件格式错误，请上传图片类型,如：.jpg，.png后缀的文件。",
      RuleTips: {
        Name: "资源名称不能为空"
      },
      SelectorTitle: "素材库",
      LocalUpload: "本地上传",
      RemoteLink: "网络链接",
      Source: "来源",
      Upload: "上传",
      UPloadTip: "只能上传{0}文件，且不超过{1}",
      Tag: "标签",
      MaterialLibrary: "素材库",
      MyMaterial: "我的素材",
      Cut: "裁剪",
      UploadLimit: "上传文件数量不能超过：{0}",
      RemoteLinkErr: "资源外链不能为空且必须是http://或https://开头的网络地址"
    },
    PublishPipe: {
      Name: "名称",
      Code: "编码",
      Status: "状态",
      AddDialogTitle: "添加发布通道",
      EditDialogTitle: "编辑发布通道",
      RuleTips: {
        Name: "名称不能为空",
        Code: "不能为空且只能使用字母、数字和下划线",
        Status: "状态不能为空",
        Sort: "排序值不能为空"
      }
    },
    File: {
      Upload: "上传",
      FileName: "文件名",
      FileSize: "大小",
      ModifyTime: "最后修改时间",
      InputFileName: "请输入文件名",
      Rename: "重命名",
      AddTtitle: "新建文件/目录",
      Type: "类型",
      Name: "名称",
      File: "文件",
      Directory: "目录",
      UploadTitle: "上传文件",
      UploadTip: "将文件拖到此处，或点击上传",
      RuleTips: {
        FileName: "不能为空且只能使用字母、数字和下划线"
      }
    },
    Template: {
      Name: "模板名称",
      FileSize: "大小",
      ModifyTime: "更新时间",
      Rename: "重命名",
      RuleTips: {
        Name: "只能使用字母、数字和下划线，且后缀必须为：{0}"
      },
      AddTitle: "添加模板文件",
      EditTitle: "编辑模板文件名",
      SelectorTitle: "选择模板"
    },
    Staticize: {
      Tag: "模板标签",
      TagName: "标签名",
      TagDirective: "标签",
      TagDesc: "描述",
      InputTagName: "输入模板标签名称",
      TagAttr: "标签属性：",
      TagAttrName: "属性名",
      TagAttrDataType: "数据类型",
      TagAttrMandatory: "是否必填",
      TagAttrOptions: "可用值",
      TagAttrDesc: "描述",
      Func: "模板函数",
      InputFuncName: "输入模板函数名称",
      UsageDesc: "用法描述：",
      FuncArgs: "函数参数：",
      FuncName: "函数方法名",
      FuncDesc: "描述",
      FuncAttr: "参数",
      FuncAttrName: "名称",
      FuncAttrType: "类型",
      FuncAttrRequired: "是否必填",
      FuncAttrDesc: "描述"
    },
    ExModel: {
      Name: "名称",
      Code: "编码",
      OwnerType: "分类",
      TableName: "数据表",
      AddTtitle: "新建模型",
      EditTitle: "编辑模型",
      GoBack: "返回模型列表",
      FieldName: "名称",
      FieldCode: "编码",
      FieldType: "字段类型",
      FieldControlType: "控件类型",
      FieldMappingName: "数据表字段",
      FieldMandatory: "是否必填",
      FieldDefaultValue: "默认值",
      FieldOptions: "可选配置",
      FieldOptionsInput: "手动输入",
      FieldOptionsDict: "字典数据",
      AddFieldTitle: "添加模型字段",
      EditFieldTitle: "编辑模型字段",
      RouteExModelField: "扩展模型字段",
      Placeholder: {
        Query: "输入模型名称/编码查询",
        FieldQuery: "字段名称/编码查询"
      },
      RuleTips: {
        Name: "名称不能为空",
        Code: "编码不能为空且只能使用字母、数字和下划线",
        TableName: "数据表不能为空",
        FieldName: "名称不能为空",
        FieldCode: "编码不能为空",
        FieldControlType: "控件类型不能为空",
        FieldMandatory: "是否必填不能为空",
        FieldType: "字段类型不能为空",
        FieldMappingName: "数据表字段不能为空",
        FieldMappingUsed: "数据表字段已被占用",
      }
    },
    FriendLink: {
      GroupName: "分组名称",
      GroupCode: "分组编码",
      AddTitle: "添加友情链接分组",
      EditTitle: "编辑友情链接分组",
      GoBack: "返回",
      LinkName: "名称",
      LinkUrl: "链接",
      AddLinkTitle: "添加友情链接",
      EditLinkTitle: "编辑友情链接",
      RouteLinkList: "友链列表",
      Placeholder: {
        GroupQuery: "输入分组名称/编码",
        LinkQuery: "输入友链名称/链接查询"
      }
    },
    ESIndex: {
      RebuildAll: "重建全站索引",
      Query: "输入搜索词",
      OnlyTitle: "仅匹配标题",
      IndexDetails: "索引详情",
      ProgressTitle: "创建索引任务",
    }
  },
  Stat: {
    Site: {
      PageView: "浏览量（PV）",
      UserView: "访客数（UV）",
      IPView: "IP数",
      AvgVisitTime: "平均访问时长",
      VisitCount: "访问次数",
      VisitTrend: "访问趋势",
      Gran: "时间粒度",
      GranHour: "按小时",
      GranDay: "按天",
      GranWeek: "按周",
      GranMonth: "按月",
      Top10EntryPage: "TOP10入口页面",
      URL: "URL地址",
      Ratio: "占比",
      VisitLocation: "地域分布",
      Location: "地域",
      NoSite: "无可用站点数据",
      UnitSecond: "秒",
      TrendCharts: "趋势分析",
    },
    Adv: {
      AdName: "广告名称",
      Location: "地域",
      Source: "来源",
      DeviceType: "设备类型",
      Browser: "浏览器",
      Time: "时间",
      Click: "点击",
      View: "展现",
      ClickRatio: "点击率",
      Trend: "趋势",
      TrendDialogTitle: "趋势图"
    }
  },
  Comment: {
    AuditPass: "审核通过",
    AuditNotPass: "审核不通过",
    SourceType: "来源类型",
    SourceId: "来源标识",
    UID: "用户ID",
    AuditStatus: "审核状态",
    Content: "评论内容",
    LikeCount: "点赞数",
    ReplyCount: "回复数",
    Time: "评论时间",
    Location: "属地",
    ClientType: "客户端类型",
    ReplyContent: "回复内容",
    ReplyTime: "回复时间",
    LikeList: "点赞记录",
    LikeTime: "点赞时间"
  },
  Vote: {
    Title: "标题",
    Code: "编码",
    CodeTip: "只能使用大小写字母、数字和下划线组合",
    Status: "状态",
    TimeRange: "时间",
    Total: "参与数",
    UserType: "用户类型",
    ViewType: "结果查看方式",
    DayAndTotalLimit: "日/总上限",
    EidtSubjects: "编辑主题",
    DayLimit: "日次数上限",
    TotalLimit: "总次数上限",
    AddVoteTitle: "新增调查问卷",
    EditVoteTitle: "编辑调查问卷",
    ItemTotal: "票数",
    InputItemTip: "输入类型主题无需配置选项",
    Subject: "主题",
    SubjectType: "类型",
    SubjectTitle: "标题",
    InsertSubject: "插入主题",
    SubjectItemType: "类型",
    InsertSubjectItem: "插入选项",
    AddSubjectItem: "添加选项",
    AddSubjectTitle: "新增主题信息",
    EditSubjectTitle: "编辑主题信息"
  },
  Member: {
    UID: "会员ID",
    UserName: "用户名",
    NickName: "昵称",
    PhoneNumber: "手机号",
    Password: "密码",
    Birthday: "生日",
    Status: "状态",
    Source: "来源",
    RegistTime: "注册时间",
    LastLoginTime: "最近登录时间",
    ResetPwd: "重置密码",
    InputPwd: "请输入`{0}`的新密码",
    RestPwdSuccess: "修改成功，新密码是：{0}",
    AddTitle: "添加会员",
    EditTitle: "编辑会员",
    LevelType: "积分类型",
    Level: {
      Type: "类型",
      Level: "等级",
      Name: "名称",
      NextNeedExp: "下一级所需经验",
      AddTitle: "添加等级配置",
      EditTitle: "添加等级配置"
    },
    ExpOpType: "操作类型",
    ExpOpNoLimit: "无限制",
    Exp: "经验值",
    ExpOpDayLimit: "日上限",
    ExpOpTotalLimit: "总上限",
    AddExpOpTitle: "添加经验配置",
    EditExpOpTitle: "编辑经验配置",
    RuleTips: {
      UserName: "必须以字母开头，且只能为（大小写字母，数字，下滑线）",
      Member: "用户名/手机号/Email不能全为空"
    }
  }
};