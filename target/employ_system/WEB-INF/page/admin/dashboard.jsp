<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员控制台 - 考勤管理系统</title>
    <!-- 引入外部CDN资源 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
    <!-- 引入Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>

    <!-- 引入拆分后的样式文件 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/dashboard.css" rel="stylesheet">
</head>
<body>
<!-- 引入顶部标题栏 -->
<jsp:include page="common/header.jsp"></jsp:include>

<!-- 引入侧边栏菜单 -->
<jsp:include page="common/sidebar.jsp">
    <jsp:param name="currentPage" value="dashboard"/>
</jsp:include>

<!-- 主内容区域 -->
<div class="main-content">
    <!-- 欢迎信息 -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="card welcome-card">
                <div class="card-body">
                    <h5 class="card-title">
                        <i class="fa fa-bullhorn text-warning"></i> 欢迎使用考勤管理系统
                    </h5>
                    <p class="card-text">尊敬的<span id="userRoleText">系统管理员</span>，今天是 <span id="currentDate" class="text-warning fw-medium">2025年12月26日 15:30</span>，祝您工作愉快！</p>
                </div>
            </div>
        </div>
    </div>

    <!-- 动态统计卡片 -->
    <div class="row mb-4">
        <div class="col-md-3 col-sm-6">
            <div class="card stat-card card-bg-primary mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between">
                        <div>
                            <h5 class="card-title text-white-80">总员工数</h5>
                            <p class="card-text display-4 mb-0" id="totalEmployees">--</p>
                        </div>
                        <div class="align-self-center">
                            <i class="fa fa-users fa-3x opacity-80"></i>
                        </div>
                    </div>
                </div>
                <div class="card-footer">
                    <a href="${pageContext.request.contextPath}/admin/page/empList.do" class="card-link">
                        <i class="fa fa-arrow-right"></i> 查看详情
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-3 col-sm-6">
            <div class="card stat-card card-bg-success mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between">
                        <div>
                            <h5 class="card-title text-white-80">今日出勤</h5>
                            <p class="card-text display-4 mb-0" id="todayAttendance">--</p>
                        </div>
                        <div class="align-self-center">
                            <i class="fa fa-check-circle-o fa-3x opacity-80"></i>
                        </div>
                    </div>
                </div>
                <div class="card-footer">
                    <a href="${pageContext.request.contextPath}/admin/page/attendanceManage.do" class="card-link">
                        <i class="fa fa-arrow-right"></i> 查看详情
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-3 col-sm-6">
            <div class="card stat-card card-bg-warning mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between">
                        <div>
                            <h5 class="card-title text-white-80">今日迟到</h5>
                            <p class="card-text display-4 mb-0" id="todayLate">--</p>
                        </div>
                        <div class="align-self-center">
                            <i class="fa fa-clock-o fa-3x opacity-80"></i>
                        </div>
                    </div>
                </div>
                <div class="card-footer">
                    <a href="${pageContext.request.contextPath}/admin/page/attendanceManage.do" class="card-link">
                        <i class="fa fa-arrow-right"></i> 查看详情
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-3 col-sm-6">
            <div class="card stat-card card-bg-danger mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between">
                        <div>
                            <h5 class="card-title text-white-80">请假申请</h5>
                            <p class="card-text display-4 mb-0" id="leaveApplications">--</p>
                        </div>
                        <div class="align-self-center">
                            <i class="fa fa-calendar-times-o fa-3x opacity-80"></i>
                        </div>
                    </div>
                </div>
                <div class="card-footer">
                    <a href="${pageContext.request.contextPath}/admin/page/leaveManage.do" class="card-link">
                        <i class="fa fa-arrow-right"></i> 查看详情
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- 待审批卡片区域 -->
    <div class="row mb-4">
        <div class="col-md-6">
            <div class="card" style="border-left: 4px solid #f59e0b;">
                <div class="card-body d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <div style="width: 50px; height: 50px; background: #fef3c7; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-right: 15px;">
                            <i class="fa fa-file-text-o fa-lg" style="color: #f59e0b;"></i>
                        </div>
                        <div>
                            <h6 class="mb-1" style="color: #666;">请假待审批</h6>
                            <h3 class="mb-0" id="pendingLeaveCount" style="color: #f59e0b;">0</h3>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/page/leaveManage.do" class="btn btn-warning btn-sm">
                        <i class="fa fa-arrow-right"></i> 去审批
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card" style="border-left: 4px solid #8b5cf6;">
                <div class="card-body d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <div style="width: 50px; height: 50px; background: #ede9fe; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-right: 15px;">
                            <i class="fa fa-credit-card fa-lg" style="color: #8b5cf6;"></i>
                        </div>
                        <div>
                            <h6 class="mb-1" style="color: #666;">补卡待审批</h6>
                            <h3 class="mb-0" id="pendingSupplementCount" style="color: #8b5cf6;">0</h3>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/page/supplementManage.do" class="btn btn-sm" style="background: #8b5cf6; color: white;">
                        <i class="fa fa-arrow-right"></i> 去审批
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- 部门主管待审批卡片区域 -->
    <div class="row mb-4">
        <div class="col-md-6">
            <div class="card" style="border-left: 4px solid #ec4899;">
                <div class="card-body d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <div style="width: 50px; height: 50px; background: #fce7f3; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-right: 15px;">
                            <i class="fa fa-user-circle-o fa-lg" style="color: #ec4899;"></i>
                        </div>
                        <div>
                            <h6 class="mb-1" style="color: #666;">主管请假待审批</h6>
                            <h3 class="mb-0" id="managerPendingLeaveCount" style="color: #ec4899;">0</h3>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/page/leaveManage.do?type=manager" class="btn btn-sm" style="background: #ec4899; color: white;">
                        <i class="fa fa-arrow-right"></i> 去审批
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card" style="border-left: 4px solid #06b6d4;">
                <div class="card-body d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <div style="width: 50px; height: 50px; background: #cffafe; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-right: 15px;">
                            <i class="fa fa-id-card-o fa-lg" style="color: #06b6d4;"></i>
                        </div>
                        <div>
                            <h6 class="mb-1" style="color: #666;">主管补卡待审批</h6>
                            <h3 class="mb-0" id="managerPendingSupplementCount" style="color: #06b6d4;">0</h3>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/page/supplementManage.do?type=manager" class="btn btn-sm" style="background: #06b6d4; color: white;">
                        <i class="fa fa-arrow-right"></i> 去审批
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- 图表和最近记录 -->
    <div class="row">
        <!-- 本月考勤统计折线图 -->
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">
                        <i class="fa fa-line-chart text-warning"></i> 本月考勤统计
                    </h5>
                </div>
                <div class="card-body p-0">
                    <canvas id="attendanceChart" height="220"></canvas>
                </div>
            </div>
        </div>
        <!-- 今日考勤饼图 -->
        <div class="col-md-3">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">
                        <i class="fa fa-pie-chart text-warning"></i> 今日考勤分析
                    </h5>
                </div>
                <div class="card-body">
                    <canvas id="attendancePieChart" height="220"></canvas>
                </div>
            </div>
        </div>
        <!-- 最近操作记录（真实数据） -->
        <div class="col-md-3">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">
                        <i class="fa fa-history text-warning"></i> 最近操作记录
                    </h5>
                </div>
                <div class="card-body">
                    <ul class="list-group list-group-flush" id="recentLogs">
                        <li class="list-group-item border-0 ps-0 pe-0 text-center text-muted">
                            <i class="fa fa-spinner fa-spin"></i> 加载中...
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 页脚 -->
<footer class="footer">
    <span>© 2025 员工考勤管理系统 - 版权所有</span>
</footer>

<!-- 引入外部JS CDN -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- 全局上下文路径 -->
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>

<!-- 引入公共JS（工具函数） -->
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<!-- 引入仪表盘主内容JS -->
<script src="${pageContext.request.contextPath}/static/js/admin/dashboard.js"></script>
</body>
</html>