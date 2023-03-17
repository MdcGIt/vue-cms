# RuoYi-Vue-CMS

### 系统简介

RuoYi-Vue-CMS是前后端分离的内容管理系统。技术栈：SpringBoot3 + VUE2 + MybatisPlus + Freemarker + ES + Redis + MySQL，项目基于[RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)重构，集成[SaToken](https://gitee.com/dromara/sa-token)用户权限，[xxl-job](https://gitee.com/xuxueli0323/xxl-job)任务调度。支持站群管理、多平台静态化、元数据模型扩展、轻松组织各种复杂内容形态、多语言、全文检索。

### 系统预览

后台预览地址：<http://admin.1000mz.com>

账号：demo / a123456

服务器内存不太够，未部署es和xxl-job。

##### 本地部署开启es和xxl-job:
- 去掉ruoyi-admin/pom.xml内容索引模块依赖注释。
- 修改application-dev.yml配置xxl.job.enable=true。

前台网站地址：<http://www.swikoon.com>

### 开发环境
- JDK17+
- MAVEN3.8+
- MYSQL5.7+

### 功能模块

#### 内容管理

*   [x] 站点管理
*   [x] 栏目管理
*   [x] 内容管理
*   [x] 资源管理
*   [x] 发布通道
*   [x] 模板管理
*   [x] 文件管理
*   [x] 模板指令
*   [x] 扩展模型
*   [x] 词汇管理
*   [x] 内容索引
*   [x] 检索词库
*   [x] 检索日志

#### 互动运营

*   [x] 友链管理
*   [x] 广告管理
*   [ ] 评论管理
*   [ ] 留言管理
*   [ ] 调查投票

#### 会员模块

*   [ ] 会员管理
*   [ ] 等级配置
*   [ ] 积分配置

#### 系统管理

*   [x] 用户管理
*   [x] 机构管理
*   [x] 角色管理
*   [x] 菜单管理
*   [x] 岗位管理
*   [x] 通知公告
*   [x] 字典数据
*   [x] 配置参数
*   [x] 安全配置
*   [x] 国际化配置

#### 系统监控

*   [x] 在线用户
*   [x] 任务调度
*   [x] 异步任务
*   [x] 服务监控
*   [x] 缓存监控
*   [x] 缓存列表
*   [x] 系统日志

