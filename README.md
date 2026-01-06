# employ_system

#### 介绍
JavaWeb项目，后端基于ssm框架，前端基于jsp、Bootstrap、Chart.js，数据库基于Mysql，开发工具为：IDEA；
主要功能：前后台分离；
前台实现考勤打卡、当日考勤数据统计、以及请假、补卡的申请与审批（审批是部门主管功能）；
后台实现考勤数据统计、员工管理、部门管理、考勤记录、请假审批与查看、补卡审批与查看、公司考勤分析、部门分析、考勤规则、日志记录

#### 软件架构
软件架构说明：
采用**前后端分离思想**的Java Web经典三层架构：
- **表现层（View/Controller）**：负责接收前端请求、返回响应结果，由JSP、Servlet实现。
- **业务逻辑层（Service）**：处理核心业务逻辑（如考勤统计、请假审批），由Spring管理。
- **数据访问层（DAO/Mapper）**：负责与数据库交互，由MyBatis实现。

### 技术栈明细
1. **前端技术**
   - 页面渲染：JSP
   - 样式与布局：Bootstrap 5.3
   - 数据可视化：Chart.js 4.4
   - 前端交互：JavaScript

2. **后端技术**
   - 核心框架：SSM（Spring 5.3 + Spring MVC 5.3 + MyBatis 3.5）
   - 数据库连接：Druid（可选，数据库连接池）
   - 日志管理：Log4j（可选）

3. **数据库**
   - 数据库类型：MySQL 8.0（建议标注版本）
   - 数据库设计：采用关系型模型，包含员工表、考勤表、请假表等核心表。

4. **开发与部署工具**
   - 开发工具：IntelliJ IDEA 2024
   - 服务器：Tomcat 9.0.19
   - 构建工具：Maven 3.9.11

#### 安装教程

直接下载项目到本地，然后在IDEA打开，配置相应的Maven依赖和Tomcat服务器即可

#### 使用说明

第一次上传项目，这是我课程设计期间完成的，还有很多不足

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
