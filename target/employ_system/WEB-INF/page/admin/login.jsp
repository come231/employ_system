<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员登录 - 考勤管理系统</title>
    <!-- 引入Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- 引入分离的登录样式 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/login.css">
</head>
<body>
<div class="login-card">
    <!-- 系统名称 -->
    <div class="system-name">
        <h1>考勤管理系统</h1>
        <p>ATTENDANCE MANAGEMENT SYSTEM</p>
    </div>
    <!-- 登录标题 -->
    <div class="login-title">
        <h2>管理员登录</h2>
    </div>
    <!-- 登录表单 -->
    <form id="loginForm">
        <div class="form-group">
            <label class="form-label" for="username">账号</label>
            <input type="text" class="form-input" id="username" placeholder="请输入管理员账号" required>
        </div>
        <div class="form-group">
            <label class="form-label" for="password">密码</label>
            <input type="password" class="form-input" id="password" placeholder="请输入登录密码" required>
        </div>
        <!-- 仅超级管理员登录 -->
        <button type="submit" class="login-btn" id="loginBtn">登录</button>
    </form>
</div>

<!-- 引入Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    <!-- 全局上下文路径 -->
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<!-- 引入通用工具库 -->
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<!-- 引入登录业务逻辑 -->
<script src="${pageContext.request.contextPath}/static/js/admin/login.js"></script>
</body>
</html>