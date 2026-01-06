<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 顶部导航栏 -->
<div class="top-header">
    <!-- 左侧折叠侧边栏按钮 -->
    <div class="sidebar-toggle-btn" id="sidebarToggleBtn">
        <i class="fa fa-bars"></i>
    </div>

    <!-- 系统名称（居中显示） -->
    <div class="system-name">
        <i class="fa fa-calendar-check-o"></i>
        <span>员工考勤管理系统</span>
    </div>

    <!-- 进入前台按钮 -->
    <a href="${pageContext.request.contextPath}/reception/page/login.do" class="go-frontend-btn" title="进入前台员工端">
        <i class="fa fa-external-link"></i>
        <span>进入前台</span>
    </a>

    <!-- 登录用户信息（由JS动态渲染） -->
    <div class="user-info" id="userInfo">
        <i class="fa fa-user-circle"></i>
        <span>加载中...</span>
        <i class="fa fa-angle-down"></i>
    </div>
</div>

<!-- 引入顶部标题栏专属JS -->
<script src="${pageContext.request.contextPath}/static/js/admin/header.js"></script>