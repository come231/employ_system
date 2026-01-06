<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 获取当前页面标识，默认值改为空（避免默认激活错误菜单）
    String currentPage = request.getParameter("currentPage") != null ? request.getParameter("currentPage") : "";

    // 新增：判断当前页面所属的父菜单，用于自动展开对应父菜单
    String parentMenu = "";
    if (currentPage.startsWith("employeeList") || currentPage.startsWith("deptList")) {
        parentMenu = "user"; // 用户管理
    } else if (currentPage.startsWith("attendanceManage") || currentPage.startsWith("leaveManage") || currentPage.startsWith("supplementManage")) {
        // 新增：supplementManage 归属考勤管理父菜单
        parentMenu = "attendance"; // 考勤管理
    } else if (currentPage.startsWith("companyAnalysis") || currentPage.startsWith("deptAnalysis")) {
        parentMenu = "analysis"; // 考勤分析
    } else if (currentPage.startsWith("settingRule") || currentPage.startsWith("settingLog")) {
        parentMenu = "setting"; // 系统设置
    } else if (currentPage.startsWith("dashboard")) {
        parentMenu = "home"; // 首页
    }
%>
<!-- 侧边栏菜单 -->
<div class="sidebar" id="sidebar">
    <ul class="sidebar-menu">
        <li class="sidebar-menu-item">
            <a href="${pageContext.request.contextPath}/admin/page/dashboard.do" class="sidebar-menu-link <%= "dashboard".equals(currentPage) ? "active-parent" : "" %>" id="menuHome">
                <i class="fa fa-home"></i>
                <span>首页</span>
            </a>
        </li>

        <li class="sidebar-menu-item">
            <a href="#" class="sidebar-menu-link" id="menuUser">
                <i class="fa fa-users"></i>
                <span>用户管理</span>
                <!-- 新增：根据parentMenu判断是否展开箭头 -->
                <i class="fa <%= "user".equals(parentMenu) ? "fa-angle-up" : "fa-angle-down" %> ml-auto"></i>
            </a>
            <!-- 修改：根据parentMenu判断是否默认展开子菜单 -->
            <ul class="sidebar-submenu <%= "user".equals(parentMenu) ? "show" : "" %>" id="submenuUser">
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/employeeList.do" class="sidebar-submenu-link <%= "employeeList".equals(currentPage) ? "active" : "" %>">员工列表</a>
                </li>
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/deptList.do" class="sidebar-submenu-link <%= "deptList".equals(currentPage) ? "active" : "" %>">部门结构</a>
                </li>
            </ul>
        </li>

        <li class="sidebar-menu-item">
            <a href="#" class="sidebar-menu-link" id="menuAttendance">
                <i class="fa fa-clock-o"></i>
                <span>考勤管理</span>
                <i class="fa <%= "attendance".equals(parentMenu) ? "fa-angle-up" : "fa-angle-down" %> ml-auto"></i>
            </a>
            <ul class="sidebar-submenu <%= "attendance".equals(parentMenu) ? "show" : "" %>" id="submenuAttendance">
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/attendanceManage.do" class="sidebar-submenu-link <%= "attendanceManage".equals(currentPage) ? "active" : "" %>">考勤查询</a>
                </li>
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/leaveManage.do" class="sidebar-submenu-link <%= "leaveManage".equals(currentPage) ? "active" : "" %>">请假管理</a>
                </li>
                <!-- 新增：补卡管理菜单 -->
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/supplementManage.do" class="sidebar-submenu-link <%= "supplementManage".equals(currentPage) ? "active" : "" %>">补卡管理</a>
                </li>
            </ul>
        </li>

        <li class="sidebar-menu-item">
            <a href="#" class="sidebar-menu-link" id="menuAnalysis">
                <i class="fa fa-line-chart"></i>
                <span>考勤分析</span>
                <i class="fa <%= "analysis".equals(parentMenu) ? "fa-angle-up" : "fa-angle-down" %> ml-auto"></i>
            </a>
            <ul class="sidebar-submenu <%= "analysis".equals(parentMenu) ? "show" : "" %>" id="submenuAnalysis">
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/companyAnalysis.do" class="sidebar-submenu-link <%= "companyAnalysis".equals(currentPage) ? "active" : "" %>">公司考勤分析</a>
                </li>
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/deptAnalysis.do" class="sidebar-submenu-link <%= "deptAnalysis".equals(currentPage) ? "active" : "" %>">部门考勤分析</a>
                </li>
            </ul>
        </li>

        <li class="sidebar-menu-item">
            <a href="#" class="sidebar-menu-link" id="menuSetting">
                <i class="fa fa-cog"></i>
                <span>系统设置</span>
                <i class="fa <%= "setting".equals(parentMenu) ? "fa-angle-up" : "fa-angle-down" %> ml-auto"></i>
            </a>
            <ul class="sidebar-submenu <%= "setting".equals(parentMenu) ? "show" : "" %>" id="submenuSetting">
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/settingRule.do" class="sidebar-submenu-link <%= "settingRule".equals(currentPage) ? "active" : "" %>">考勤规则</a>
                </li>
                <li class="sidebar-submenu-item">
                    <a href="${pageContext.request.contextPath}/admin/page/settingLog.do" class="sidebar-submenu-link <%= "settingLog".equals(currentPage) ? "active" : "" %>">操作日志</a>
                </li>
            </ul>
        </li>
    </ul>
</div>

<!-- 引入侧边栏专属JS -->
<script src="${pageContext.request.contextPath}/static/js/admin/sidebar.js"></script>