-- 先关闭外键检查（避免创建顺序导致的外键报错，执行完后重新开启）
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 系统角色表（补充权限标识，区分系统级/业务级角色）
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    role_id INT AUTO_INCREMENT COMMENT '角色ID' PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（超级管理员/部门主管/普通员工）',
    role_type TINYINT NOT NULL DEFAULT 2 COMMENT '角色类型 1-系统级（如超级管理员，无业务关联） 2-业务级（需关联员工/部门）',
    role_desc VARCHAR(200) DEFAULT '' COMMENT '角色描述',
    STATUS TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT = '系统角色表';

-- ----------------------------
-- 员工信息模块（先创建无外键依赖/低依赖的表）
-- ----------------------------
-- 1. 部门表（无修改，保持原有逻辑）
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    dept_id INT AUTO_INCREMENT COMMENT '部门ID' PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    manager_id INT DEFAULT NULL COMMENT '部门主管ID（关联员工ID）',
    STATUS TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
    -- 先注释外键，最后统一添加：FOREIGN KEY (manager_id) REFERENCES emp_info(emp_id) ON DELETE SET NULL
) COMMENT = '部门信息表（单层）';

-- 2. 员工信息表（无修改，保持原有逻辑）
DROP TABLE IF EXISTS emp_info;
CREATE TABLE emp_info (
    emp_id INT AUTO_INCREMENT COMMENT '员工ID' PRIMARY KEY,
    emp_no VARCHAR(20) NOT NULL UNIQUE COMMENT '工号',
    emp_name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    dept_id INT NOT NULL COMMENT '所属部门ID',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号（优化：改为VARCHAR(20)兼容国际号码）',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    STATUS TINYINT DEFAULT 1 COMMENT '状态 1-在职 0-离职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (dept_id) REFERENCES sys_dept(dept_id) ON DELETE CASCADE
) COMMENT = '员工基本信息表';

-- ----------------------------
-- 2. 系统用户表（核心优化：允许超级管理员不关联员工ID，补充最后登录IP）
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_id INT AUTO_INCREMENT COMMENT '用户ID' PRIMARY KEY,
    emp_id INT NULL COMMENT '关联员工ID（系统级角色可空，业务级角色必填）',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名（账号）',
    PASSWORD VARCHAR(100) NOT NULL COMMENT '加密密码（建议BCrypt，而非MD5/SHA256）',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像路径',
    STATUS TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (emp_id) REFERENCES emp_info(emp_id) ON DELETE CASCADE
) COMMENT = '系统用户表';

-- ----------------------------
-- 3. 用户角色关联表（无修改，保持原有逻辑）
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id INT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    role_id INT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(role_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id) COMMENT '用户角色唯一'
) COMMENT = '用户角色关联表';

-- ----------------------------
-- 考勤业务模块（无修改，保持原有逻辑）
-- ----------------------------
-- 1. 考勤打卡记录表
DROP TABLE IF EXISTS attendance_record;
CREATE TABLE attendance_record (
    record_id INT AUTO_INCREMENT COMMENT '记录ID' PRIMARY KEY,
    emp_id INT NOT NULL COMMENT '员工ID',
    check_date DATE NOT NULL COMMENT '打卡日期',
    TYPE INT NOT NULL COMMENT '打卡类型：1上班/0下班',
    check_in_time DATETIME DEFAULT NULL COMMENT '打卡时间',
    STATUS TINYINT DEFAULT 0 COMMENT '考勤状态 0-正常 1-迟到 2-早退 3-旷工 4-外勤',
    remark VARCHAR(200) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (emp_id) REFERENCES emp_info(emp_id) ON DELETE CASCADE
) COMMENT = '考勤打卡记录表（无地点）';

-- 2. 请假申请表
DROP TABLE IF EXISTS leave_application;
CREATE TABLE leave_application (
    leave_id INT AUTO_INCREMENT COMMENT '请假ID' PRIMARY KEY,
    emp_id INT NOT NULL COMMENT '员工ID',
    leave_type VARCHAR(20) NOT NULL COMMENT '请假类型（年假/事假/病假）',
    start_time DATETIME NOT NULL COMMENT '请假开始时间',
    end_time DATETIME NOT NULL COMMENT '请假结束时间',
    leave_days DECIMAL(5,1) NOT NULL COMMENT '请假天数',
    reason VARCHAR(500) DEFAULT '' COMMENT '请假原因',
    approver_id INT DEFAULT NULL COMMENT '审批人ID（部门主管/管理员）',
    approve_status TINYINT DEFAULT 0 COMMENT '审批状态 0-待审批 1-通过 2-驳回',
    approve_opinion VARCHAR(500) DEFAULT '' COMMENT '审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (emp_id) REFERENCES emp_info(emp_id) ON DELETE CASCADE,
    FOREIGN KEY (approver_id) REFERENCES emp_info(emp_id) ON DELETE SET NULL
) COMMENT = '请假申请表';

-- 3. 补卡申请表
DROP TABLE IF EXISTS supplement_card;
CREATE TABLE supplement_card (
    supplement_id INT AUTO_INCREMENT COMMENT '补卡ID' PRIMARY KEY,
    emp_id INT NOT NULL COMMENT '员工ID',
    check_date DATE NOT NULL COMMENT '补卡日期',
    check_type TINYINT NOT NULL COMMENT '补卡类型 1-上班 2-下班 3-上下班',
    actual_time DATETIME NOT NULL COMMENT '实际打卡时间（补卡填写）',
    reason VARCHAR(500) DEFAULT '' COMMENT '补卡原因',
    approver_id INT DEFAULT NULL COMMENT '审批人ID',
    approve_status TINYINT DEFAULT 0 COMMENT '审批状态 0-待审批 1-通过 2-驳回',
    approve_opinion VARCHAR(500) DEFAULT '' COMMENT '审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (emp_id) REFERENCES emp_info(emp_id) ON DELETE CASCADE,
    FOREIGN KEY (approver_id) REFERENCES emp_info(emp_id) ON DELETE SET NULL
) COMMENT = '补卡申请表';

-- 4. 考勤规则配置表
DROP TABLE IF EXISTS attendance_rule;
CREATE TABLE attendance_rule (
    rule_id INT AUTO_INCREMENT COMMENT '规则ID' PRIMARY KEY,
    work_start_time TIME NOT NULL COMMENT '上班时间',
    work_end_time TIME NOT NULL COMMENT '下班时间',
    late_threshold INT DEFAULT 15 COMMENT '迟到阈值（分钟）',
    early_threshold INT DEFAULT 15 COMMENT '早退阈值（分钟）',
    supplement_valid_hours INT DEFAULT 24 COMMENT '补卡有效期（小时）',
    full_attendance_days INT DEFAULT 22 COMMENT '全勤判定天数（每月）',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT = '考勤规则配置表';

-- ----------------------------
-- 新增：操作日志表（修复语法错误+补充字段）
-- ----------------------------
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    log_id BIGINT AUTO_INCREMENT COMMENT '日志ID（用BIGINT避免主键溢出）' PRIMARY KEY,
    user_id INT NOT NULL COMMENT '操作用户ID（关联sys_user）',
    username VARCHAR(50) NOT NULL COMMENT '操作用户名（冗余存储）',
    role_name VARCHAR(50) NOT NULL COMMENT '操作时的角色名称（如：超级管理员/研发部主管/普通员工）',
    module VARCHAR(50) NOT NULL COMMENT '操作模块（如：用户管理/考勤规则/请假审批/补卡审批）',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型（新增/修改/删除/审批/登录/退出）',
    operation_content VARCHAR(50) COMMENT '操作内容（详细描述）',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    STATUS TINYINT DEFAULT 1 COMMENT '操作状态 1-成功 0-失败',
    error_msg VARCHAR(50) DEFAULT '' COMMENT '失败原因（状态为0时填写）'
) COMMENT = '系统操作日志表（全量记录关键操作）';

CREATE TABLE `sys_notification` (
                                    `notification_id` INT NOT NULL AUTO_INCREMENT COMMENT '通知ID（主键）',
                                    `user_id` INT NOT NULL COMMENT '关联用户ID',
                                    `type` VARCHAR(50) DEFAULT '' COMMENT '通知类型（如：系统通知、审批通知等）',
                                    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
                                    `content` TEXT COMMENT '通知内容',
                                    `related_id` INT DEFAULT 0 COMMENT '关联业务ID（如：审批单ID、消息ID等）',
                                    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读（0：未读，1：已读）',
                                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (`notification_id`),
                                    KEY `idx_user_id` (`user_id`),
                                    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 补加部门表的外键（因为emp_info已创建，现在可关联）
ALTER TABLE sys_dept ADD CONSTRAINT fk_dept_manager FOREIGN KEY (manager_id) REFERENCES emp_info(emp_id) ON DELETE SET NULL;

-- 重新开启外键检查
SET FOREIGN_KEY_CHECKS = 1;

