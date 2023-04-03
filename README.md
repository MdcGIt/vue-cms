# RuoYi-Vue-CMS

### 系统简介

RuoYi-Vue-CMS是前后端分离的内容管理系统。技术栈：SpringBoot3 + VUE2 + MybatisPlus + Freemarker + ES + Redis + MySQL，项目基于[RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)重构，集成[SaToken](https://gitee.com/dromara/sa-token)用户权限，[xxl-job](https://gitee.com/xuxueli0323/xxl-job)任务调度。支持站群管理、多平台静态化、元数据模型扩展、轻松组织各种复杂内容形态、多语言、全文检索。

### 系统预览

后台预览地址：<http://admin.1000mz.com>

账号：demo / a123456

服务器内存不太够，未部署es和xxl-job。

前台网站地址：<http://www.swikoon.com>

##### 演示站静态资源
演示站的静态资源已提交到ruoyi-vue-cms-wwwroot，如有需要自行下载吧。
CMS资源路径配置application.yml中ruoyi.cms.resourceRoot，默认与项目同级的wwwroot_release

##### 本地部署开启es和xxl-job:
- 修改application-prod.yml配置spring.data.elasticsearch.repositories.enabled=true
- 修改application-dev.yml配置xxl.job.enable=true。

##### 本地开发环境部署

docker环境配置文件docker-compose_env.yml在ruoyi-admin/docker目录下。
- 修改mysql配置root密码与映射端口号，请与application-dev.yml保持一致：

```
services: 
  cc-mysql:
    environment:
      - MYSQL_ROOT_PASSWORD=xxxxxx
    ports:
      - '33066:3306'
```

``
`- '33066:3306'`
- 修改redis配置密码和端口，请与application-dev.yml保持一致：

```
services: 
  cc-redis:
    command:
      # 密码也可以在配置文件直接配置
      redis-server --port 6379 --requirepass "xxxxx" --appendonly yes
```
- xxl-job配置数据库访问密码，与上面的mysql配置一致

```
services:
  cc-xxl-job-admin:
    ports:                                                
      - 18080:8080
    environment:
      PARAMS: "--spring.datasource.url=jdbc:mysql://cc-mysql/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai --spring.datasource.username=root --spring.datasource.password=xxxxxx"
```
- elasticsearch-ik的镜像构建配置文件在ruoyi-search/docker下。

### 开发环境
- JDK17
- MAVEN3.8
- MYSQL5.7

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
*   [x] 评论管理
*   [ ] 留言管理
*   [x] 调查投票

#### 会员模块

*   [x] 会员管理
*   [x] 等级配置
*   [x] 积分配置

#### 访问统计

*   [ ] 网站访问统计
*   [ ] 广告点击/展现统计

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

