<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>操作日志 - 员工考勤管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <style>
        .main-content {
            margin-left: 280px;
            padding: 20px 0;
            padding-top: 20px;
            min-height: calc(100vh - 70px);
            box-sizing: border-box;
        }
        .main-content.expand {
            margin-left: 0;
        }
        .top-bar-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 20px 15px;
            flex-wrap: wrap;
            gap: 10px;
        }
        .record-info {
            font-size: 14px;
            color: #666;
        }
        .record-info span {
            font-weight: 600;
            color: #f79f1f;
        }
        .search-container {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .search-input {
            padding: 8px 12px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            font-size: 14px;
            width: 200px;
            outline: none;
        }
        .search-input:focus {
            border-color: #f79f1f;
        }
        .search-select {
            padding: 8px 12px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            font-size: 14px;
            outline: none;
        }
        .search-btn {
            padding: 8px 16px;
            background: #f79f1f;
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 5px;
        }
        .search-btn:hover {
            background: #e68a00;
        }
        .reset-btn {
            padding: 8px 16px;
            background: #6c757d;
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 5px;
        }
        .reset-btn:hover {
            background: #5a6268;
        }
        /* 日期搜索区域 */
        .date-search-container {
            background: white;
            padding: 15px 20px;
            border-radius: 8px;
            margin: 0 20px 15px;
            display: flex;
            align-items: center;
            gap: 15px;
            flex-wrap: wrap;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        }
        .date-search-container label {
            font-weight: 500;
            color: #555;
            margin-right: 5px;
        }
        .date-search-container input[type="date"] {
            padding: 8px 12px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            font-size: 14px;
        }
        /* 表格容器 */
        .table-container {
            padding: 0 20px;
            box-sizing: border-box;
        }
        .log-table {
            width: 100%;
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }
        .log-table thead {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
            color: white;
        }
        .log-table th {
            padding: 15px 12px;
            text-align: left;
            font-weight: 500;
            font-size: 14px;
        }
        .log-table td {
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 14px;
            color: #333;
        }
        .log-table tbody tr:hover {
            background: #f8f9fa;
        }
        .log-table tbody tr:last-child td {
            border-bottom: none;
        }
        /* 状态徽章 */
        .status-badge {
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;
        }
        .status-success {
            background: #d4edda;
            color: #155724;
        }
        .status-error {
            background: #f8d7da;
            color: #721c24;
        }
        /* 操作类型徽章 */
        .operation-badge {
            padding: 3px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 500;
        }
        .op-login { background: #e3f2fd; color: #1565c0; }
        .op-logout { background: #fce4ec; color: #c2185b; }
        .op-add { background: #e8f5e9; color: #2e7d32; }
        .op-edit { background: #fff3e0; color: #ef6c00; }
        .op-delete { background: #ffebee; color: #c62828; }
        .op-approve { background: #f3e5f5; color: #7b1fa2; }
        .op-query { background: #e0f7fa; color: #00838f; }
        .op-default { background: #f5f5f5; color: #616161; }
        /* 分页样式 */
        .pagination-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px 0;
            flex-wrap: wrap;
            gap: 10px;
        }
        .pagination-info {
            font-size: 14px;
            color: #666;
        }
        .page-size-select {
            padding: 5px 10px;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            margin: 0 5px;
        }
        .pagination-controls {
            display: flex;
            align-items: center;
            gap: 5px;
        }
        .page-btn {
            padding: 6px 12px;
            border: 1px solid #e0e0e0;
            background: white;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
        }
        .page-btn:hover:not(:disabled) {
            background: #f0f0f0;
        }
        .page-btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
        .page-numbers {
            display: flex;
            gap: 5px;
        }
        .page-number {
            width: 32px;
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
        }
        .page-number:hover {
            background: #f0f0f0;
        }
        .page-number.active {
            background: #f79f1f;
            color: white;
            border-color: #f79f1f;
        }
        /* 操作内容单元格 */
        .content-cell {
            max-width: 200px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .content-cell:hover {
            white-space: normal;
            word-break: break-all;
        }
        /* 错误信息单元格 */
        .error-cell {
            max-width: 150px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            color: #dc3545;
        }
        .error-cell:hover {
            white-space: normal;
            word-break: break-all;
        }
    </style>
</head>
<body>
<jsp:include page="../common/header.jsp"></jsp:include>
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="settingLog"/>
</jsp:include>

<div class="main-content">
    <div class="top-bar-container">
        <div class="record-info">共 <span id="totalRecordsNum">0</span> 条日志记录</div>
        <div class="search-container">
            <input type="text" class="search-input" id="searchInput" placeholder="输入用户名/模块/操作类型">
            <select class="search-select" id="searchField">
                <option value="username">用户名</option>
                <option value="module">模块</option>
                <option value="operationType">操作类型</option>
            </select>
            <button class="search-btn" id="searchBtn"><i class="fa fa-search"></i> 搜索</button>
            <button class="reset-btn" id="resetBtn"><i class="fa fa-refresh"></i> 重置</button>
        </div>
    </div>

    <!-- 日期搜索区域 -->
    <div class="date-search-container">
        <div>
            <label>开始日期:</label>
            <input type="date" id="startDate">
        </div>
        <div>
            <label>结束日期:</label>
            <input type="date" id="endDate">
        </div>
        <button class="search-btn" id="dateSearchBtn"><i class="fa fa-calendar"></i> 按日期筛选</button>
    </div>

    <div class="table-container">
        <table class="log-table">
            <thead>
            <tr>
                <th>日志ID</th>
                <th>用户名</th>
                <th>角色</th>
                <th>模块</th>
                <th>操作类型</th>
                <th>操作内容</th>
                <th>操作时间</th>
                <th>状态</th>
                <th>错误信息</th>
            </tr>
            </thead>
            <tbody id="logTableBody">
            <tr><td colspan="9" style="text-align: center; padding: 50px;">加载中...</td></tr>
            </tbody>
        </table>

        <div class="pagination-container">
            <div class="pagination-info">
                <span>每页显示</span>
                <select class="page-size-select" id="pageSizeSelect">
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

<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<script src="${pageContext.request.contextPath}/static/js/admin/settingLog.js"></script>

</body>
</html>
