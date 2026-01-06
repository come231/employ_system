<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考勤记录 - 员工考勤管理系统</title>
    <!-- 引入公共资源 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <!-- 引入拆分的样式文件 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <!-- 引入考勤记录专属独立CSS（替换原内联style） -->
    <link href="${pageContext.request.contextPath}/static/css/admin/attendanceManage.css" rel="stylesheet">
</head>
<body>
<!-- 引入顶部标题栏 -->
<jsp:include page="../common/header.jsp"></jsp:include>

<!-- 引入侧边栏菜单 -->
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="attendanceManage"/>
</jsp:include>

<!-- 主内容区 -->
<div class="main-content" id="mainContent">
    <!-- 顶部信息+搜索区域 -->
    <div class="top-bar-container">
        <!-- 左侧考勤记录信息 -->
        <div class="record-info" id="recordInfo">共 <span id="totalRecordsNum">0</span> 条考勤记录</div>

        <!-- 右侧搜索区域 -->
        <div class="search-container">
            <input type="text" class="search-input" id="searchInput" placeholder="例：Y2025001（请输入员工工号）">
            <button class="search-btn" id="searchBtn">
                <i class="fa fa-search"></i> 搜索
            </button>
        </div>
    </div>

    <!-- 日期搜索区域 -->
    <div class="date-search-container" style="background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center; gap: 15px; flex-wrap: wrap;">
        <div style="display: flex; align-items: center; gap: 10px;">
            <label style="font-weight: 500; color: #555;">开始日期:</label>
            <input type="date" id="startDate" class="form-control" style="width: 160px;">
        </div>
        <div style="display: flex; align-items: center; gap: 10px;">
            <label style="font-weight: 500; color: #555;">结束日期:</label>
            <input type="date" id="endDate" class="form-control" style="width: 160px;">
        </div>
        <button class="btn btn-primary" id="dateSearchBtn" onclick="searchByDate()">
            <i class="fa fa-calendar"></i> 按日期搜索
        </button>
        <button class="btn btn-outline-secondary" id="resetBtn" onclick="resetSearch()">
            <i class="fa fa-refresh"></i> 重置
        </button>
    </div>

    <!-- 考勤记录表格 -->
    <div class="table-container">
        <table class="attendance-table">
            <thead>
            <tr>
                <th>记录ID</th>
                <th>员工工号</th>
                <th>考勤日期</th>
                <th>打卡时间</th>
                <th>考勤状态</th>
                <th>备注</th>
            </tr>
            </thead>
            <tbody id="attendanceTableBody">
            <tr><td colspan="6" style="text-align: center; padding: 50px;">加载中...</td></tr>
            </tbody>
        </table>

        <!-- 分页控件 -->
        <div class="pagination-container" id="paginationContainer">
            <div class="pagination-info">
                <span>每页显示</span>
                <select class="page-size-select" id="pageSizeSelect">
                    <option value="4">4</option>
                    <option value="8">8</option>
                    <option value="10">10</option>
                    <option value="20">20</option>
                    <option value="50">50</option>
                </select>
                <span>条</span>
            </div>
            <div class="pagination-controls">
                <button class="page-btn" id="firstPage">首页</button>
                <button class="page-btn" id="prevPage">上一页</button>
                <div class="page-numbers" id="pageNumbers"></div>
                <button class="page-btn" id="nextPage">下一页</button>
                <button class="page-btn" id="lastPage">末页</button>
            </div>
            <div class="pagination-info">
                <span>第 <span id="currentPageInfo">1</span> / <span id="totalPages">1</span> 页</span>
            </div>
        </div>
    </div>
</div>

<!-- 引入外部JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<!-- 全局上下文路径 -->
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>

<!-- 引入公共js文件 -->
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>

<!-- 引入考勤记录专属独立JS（替换原内联考勤JS） -->
<script src="${pageContext.request.contextPath}/static/js/admin/attendanceManage.js"></script>
</body>
</html>