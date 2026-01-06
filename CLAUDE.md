# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Employee Attendance Management System (考勤管理系统) - A Java web application built with SSM framework (Spring + SpringMVC + MyBatis) for managing employee attendance, leave applications, and system operations.

## Build and Run Commands

```bash
# Build with Maven (requires JDK 1.8+)
mvn clean package

# Compile only (faster for checking compilation errors)
mvn compile

# Deploy to Tomcat (copy WAR file to Tomcat webapps directory)
# The WAR file will be generated as: target/employ_system.war

# Run single test class
mvn test -Dtest=TestClassName

# Database initialization
mysql -u root -p employ_system < src/main/resources/database.sql

# Load test data (optional)
mysql -u root -p employ_system < src/main/resources/test_data.sql
```

**Access URLs:**
- Admin Panel: `http://localhost:8080/employ_system_2/admin/page/login.do`
- Employee Portal: `http://localhost:8080/employ_system_2/reception/page/login.do`

**Default Admin Login:** username: `admin`

**Forgot Password Key:** `root` (used in password recovery flow)

## Architecture

### Technology Stack
- **Backend:** Spring 5.3.28 + SpringMVC 5.2.8 + MyBatis 3.5.13
  - **Note:** Do NOT upgrade SpringMVC to 5.3.28 - it has known issues
- **Database:** MySQL 8.0 with Druid connection pool
- **Frontend:** JSP + Bootstrap 5.3.0 + Chart.js 4.4.0
- **Build:** Maven

### Layer Structure
```
com.hkd
├── controller/
│   ├── admin/            # Admin panel controllers (all under /admin path)
│   └── reception/        # Employee self-service portal (under /reception path)
├── service/              # Service interfaces
├── service/impl/         # Service implementations
├── mapper/               # MyBatis DAO interfaces (named *Dao.java, mapped to *Mapper.xml)
├── pojo/                 # Entity classes
└── util/                 # Utility classes (JsonUtil, BCryptUtil, etc.)
```

### MyBatis Naming Convention
- DAO interfaces: `*Dao.java` in `com.hkd.mapper` package
- XML mappers: `*Mapper.xml` in `src/main/resources/mapper/`
- Example: `EmpInfoDao.java` → `EmpInfoMapper.xml`

### URL Pattern
All requests use `.do` suffix (configured in web.xml). JSP views are located in `/WEB-INF/page/`.
- Admin panel navigation: `ToPageController` at `/admin/page/*.do` - maps URL to JSP view (e.g., `/admin/page/dashboard.do` → `admin/dashboard.jsp`)
- Employee portal navigation: `ReceptionPageController` at `/reception/page/*.do`

### Request Flow
1. `*.do` requests → DispatcherServlet (springmvc.xml)
2. CharacterEncodingFilter (UTF-8) applied first
3. XssFilter applied to all requests for security
4. Controller returns view name → resolved to `/WEB-INF/page/{viewName}.jsp`

### Key Utilities
- **JsonUtil:** Unified JSON response format with `objectToJson()`, `buildFalseJson()`, `buildJsonByTotalCount()`, `buildSuccessJson()`
- **OperationLogUtil:** Records system operation logs to `sys_operation_log` table via `log()` and `logError()` methods
- **BCryptUtil:** Password hashing with BCrypt
- **PageQuery/PageResult:** Pagination support classes
- **CommonUtil:** Common utility methods including `isEmpty()` checks
- **PinyinUtil:** Chinese character to Pinyin conversion

### Session Attributes
After login, these session attributes are set:

**Admin Portal:**
- `adminId`, `admin` (username), `adminRole`, `avatar`, `lastLoginTime`
- `user` (SysUser object for profile operations)

**Employee Portal:**
- `empId`, `empInfo` (EmpInfo object), `empName`

### Database Schema
Main tables: `sys_user`, `sys_role`, `sys_user_role`, `sys_dept`, `emp_info`, `attendance_record`, `leave_application`, `supplement_card`, `attendance_rule`, `sys_operation_log`, `sys_notification`

MyBatis is configured with `mapUnderscoreToCamelCase=true` for automatic column-to-property mapping.

### Test Structure
Tests are located in `src/main/Test/Test00/` (non-standard location).

### Configuration Files
- `src/main/resources/db.properties` - Database connection settings
- `src/main/resources/spring.xml` - Spring core config (DataSource, MyBatis, transactions, ThreadPoolTaskExecutor)
- `src/main/resources/springmvc.xml` - SpringMVC config (view resolver, message converters, file upload max 10MB)
- `src/main/resources/mapper/*.xml` - MyBatis mapper XML files
- `src/main/resources/database.sql` - Full database schema initialization
- `src/main/resources/test_data.sql` - Sample test data

### Static Resources
Frontend assets are in `src/main/webapp/static/`:
- `static/js/admin/` - Admin panel JavaScript files
- `static/js/reception/` - Employee portal JavaScript files
- `static/css/admin/` - Admin panel stylesheets
- `static/css/reception/` - Employee portal stylesheets
- `static/images/` - Profile photos and images

## API Response Format

All API responses follow this JSON structure (note: code is string "200" for success):
```json
{"code": "200", "msg": "message", "data": {...}}
{"code": "200", "msg": "message", "count": 10, "data": [...]}  // paginated
{"code": 500, "msg": "error message"}  // error (integer code for errors)
```

Use `JsonUtil` methods:
- `objectToJson(obj, msg)` - Single object response
- `buildJsonByTotalCount(list, totalCount)` - Paginated list response
- `buildSuccessJson(code, msg)` - Simple success response
- `buildFalseJson(code, msg)` - Error response

## Controller Endpoints

**Admin Panel** (`/admin/*`):
- `/admin/login*.do` - Authentication (AdminLoginController)
- `/admin/dashboard/*.do` - Dashboard data and charts
- `/admin/profile/*.do` - User profile and password management
- `/admin/leave/*.do` - Leave application management
- `/admin/attendance/*.do` - Attendance record management
- `/admin/supplement/*.do` - Card supplement requests
- `/admin/employee/*.do` - Employee CRUD
- `/admin/dept/*.do` - Department management
- `/admin/setting/rule/*.do` - Attendance rules configuration
- `/admin/setting/log/*.do` - Operation log viewing

**Employee Self-Service Portal** (`/reception/*`):
- `/reception/login*.do` - Employee authentication (ReceptionLoginController)
- `/reception/home/*.do` - Employee homepage (ReceptionHomeController)
- `/reception/workbench/*.do` - Attendance, leave, supplement operations (ReceptionWorkbenchController)
- `/reception/center/*.do` - Employee profile management (ReceptionCenterController)
- `/reception/notification/*.do` - Notifications (ReceptionNotificationController)

## Operation Logging

When using `OperationLogUtil.log()`, use these standard module names:
- 系统登录, 员工管理, 部门管理, 考勤管理, 请假管理, 补卡管理, 个人中心, 系统设置

Standard operation types: 登录, 退出, 查询, 新增, 修改, 删除, 审批

## Database Status Codes

Common status values used throughout the system:
- **Approval status** (`approve_status`): 0=待审批, 1=通过, 2=驳回
- **Attendance status**: 0=正常, 1=迟到, 2=早退, 3=旷工, 4=外勤
- **Check type** (`check_type`): 1=上班, 2=下班, 3=上下班
- **Operation log status**: 0=失败, 1=成功
- **Notification read status** (`is_read`): 0=未读, 1=已读
- **General entity status**: 0=禁用, 1=启用
