<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>员工考勤管理系统 - 登录</title>
    <!-- 外部CDN资源 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- 登录页专用CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/reception/login.css">
</head>
<body>
<div class="login-container">
    <div class="login-header">
        <h3>员工考勤管理系统</h3>
        <p>请登录您的账号</p>
    </div>

    <form class="login-form" id="loginForm">
        <div class="form-group">
            <label for="username">用户名</label>
            <i class="fas fa-user form-icon"></i>
            <input type="text" id="username" class="form-control" placeholder="请输入用户名">
            <div class="error-tip" id="usernameError"></div>
        </div>

        <div class="form-group">
            <label for="password">密码</label>
            <i class="fas fa-lock form-icon"></i>
            <input type="password" id="password" class="form-control" placeholder="请输入密码">
            <div class="error-tip" id="passwordError"></div>
        </div>

        <div class="form-options">
            <label class="checkbox-label">
                <input type="checkbox" id="rememberPwd"> 记住密码
            </label>
            <a href="javascript:void(0);" class="forgot-password" id="forgotPwd">忘记密码？</a>
        </div>

        <button type="button" class="login-btn" id="loginBtn">
            <span>登录系统</span>
            <div class="loader" id="loginLoader"></div>
        </button>
    </form>

    <div class="login-footer">
        <p>&copy; 2025 员工考勤管理系统</p>
    </div>

    <a href="${pageContext.request.contextPath}/admin/page/login.do" class="admin-link">
        <i class="fas fa-cog"></i> 进入后台管理系统
    </a>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/static/js/reception/login.js"></script>
</body>
</html>
