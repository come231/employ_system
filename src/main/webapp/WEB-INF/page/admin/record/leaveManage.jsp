<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>请假管理 - 员工考勤管理系统</title>
    <!-- 公共依赖引入 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- 公共样式引入 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <!-- 请假管理专属样式 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/leaveManage.css" rel="stylesheet">
    <style>
        /* 主内容区布局 - 修复标题栏覆盖问题 */
        body {
            padding-top: 70px; /* 完全等于顶部导航栏高度 */
        }
        .main-content {
            margin-left: 280px;
            margin-top: 20px; /* 额外间距，避免紧贴导航栏 */
            padding: 0 0 30px 0;
            min-height: calc(100vh - 90px);
            box-sizing: border-box;
        }
        .main-content.expand {
            margin-left: 0;
        }
        /* 搜索框右侧布局：顶部栏弹性布局，搜索区域靠右 */
        .top-bar-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 20px 15px;
            width: 100%;
            box-sizing: border-box;
        }
        /* 审批弹窗样式 */
        .approve-modal {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: none;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        }
        .approve-modal-content {
            background-color: #fff;
            padding: 25px;
            border-radius: 10px;
            width: 450px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }
        .modal-btn {
            /* 统一按钮内边距和对齐方式，修复文字错乱 */
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 8px 16px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.3s ease;
        }
        .modal-btn i {
            margin-right: 6px; /* 图标与文字间距统一 */
            font-size: 14px;
        }
        .modal-form-item {
            margin-bottom: 20px;
        }
        .modal-form-item label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: #333;
        }
        .modal-form-item select, .modal-form-item textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            outline: none;
            font-size: 14px;
        }
        .modal-form-item textarea {
            min-height: 100px;
            resize: vertical;
        }
        .modal-btn-group {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            margin-top: 20px;
        }
        .btn-confirm {
            background-color: #f79f1f;
            color: #fff;
        }
        .btn-confirm:hover {
            background-color: #e68a00;
        }
        .close {
            background-color: #e0e0e0;
            color: #333;
        }
        .close:hover {
            background-color: #d0d0d0;
        }
        /* 表格容器增加左右内边距，拉开与侧边的距离 */
        .table-container {
            padding: 0 20px; /* 左右各20px间距，可根据需求调整 */
            box-sizing: border-box;
        }
    </style>
</head>
<body>
<!-- 引入顶部标题栏 -->
<jsp:include page="../common/header.jsp"></jsp:include>

<!-- 引入侧边栏菜单（传递currentPage，实现菜单激活） -->
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="leaveManage"/>
</jsp:include>

<!-- 主内容区 -->
<div class="main-content" id="mainContent">
    <!-- 顶部信息+搜索区域（日期查询在左，搜索在右） -->
    <div class="top-bar-container">
        <!-- 左侧请假记录信息 -->
        <div class="record-info" id="recordInfo">共 <span id="totalRecordsNum">0</span> 条请假记录</div>
    </div>

    <!-- 搜索区域：日期在左，搜索栏在右 -->
    <div class="top-bar-container" style="padding-top: 0;">
        <!-- 左侧日期搜索 -->
        <div class="search-container" style="gap: 8px;">
            <input type="datetime-local" class="search-input" id="startDate" style="width: 180px;">
            <span style="margin: 0 5px;">至</span>
            <input type="datetime-local" class="search-input" id="endDate" style="width: 180px;">
            <button class="search-btn" id="dateSearchBtn">
                <i class="fa fa-calendar"></i> 时间查询
            </button>
            <button class="search-btn" id="resetBtn" style="background-color: #6c757d;">
                <i class="fa fa-refresh"></i> 重置
            </button>
        </div>

        <!-- 右侧关键字搜索 -->
        <div class="search-container">
            <input type="text" class="search-input" id="searchInput" placeholder="例：Y2025001/事假/待审批">
            <select class="search-select" id="searchField">
                <option value="empNo">员工工号</option>
                <option value="type">请假类型</option>
                <option value="status">审批状态</option>
            </select>
            <button class="search-btn" id="searchBtn">
                <i class="fa fa-search"></i> 搜索
            </button>
        </div>
    </div>

    <!-- 请假列表表格容器（已加左右内边距） -->
    <div class="table-container">
        <table class="leave-table">
            <thead>
            <tr>
                <th>请假ID</th>
                <th>员工工号</th>
                <th>申请时间</th>
                <th>请假类型</th>
                <th>请假天数</th>
                <th>审批人</th>
                <th>审批状态</th>
                <th>请假理由</th>
                <th>审批意见</th> <!-- 新增：审批意见列 -->
                <th>操作</th>     <!-- 新增：操作列 -->
            </tr>
            </thead>
            <tbody id="leaveTableBody">
            <!-- 加载占位符 -->
            <tr><td colspan="10" style="text-align: center; padding: 50px;">加载中...</td></tr>
            </tbody>
        </table>

        <!-- 分页控件（复用员工列表分页结构） -->
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

<!-- 新增：审批弹窗 -->
<div class="approve-modal" id="approveModal">
    <div class="approve-modal-content">
        <h4 style="margin-bottom: 20px; color: #333; font-weight: 600;">请假审批</h4>
        <input type="hidden" id="currentLeaveId" value=""> <!-- 存储当前审批的请假ID -->
        <div class="modal-form-item">
            <label for="approveResult">审批结果</label>
            <select id="approveResult" required>
                <option value="">请选择审批结果</option>
                <option value="1">通过</option>
                <option value="2">驳回</option>
            </select>
        </div>
        <div class="modal-form-item">
            <label for="approveOpinionInput">审批意见</label>
            <textarea id="approveOpinionInput" placeholder="请输入审批意见（可选）"></textarea>
        </div>
        <!-- 审批弹窗按钮组（修复后） -->
        <div class="modal-btn-group">
            <button class="modal-btn close" id="closeModalBtn">
                <i class="fa fa-times"></i>关闭
            </button>
            <button class="modal-btn btn-confirm" id="submitApproveBtn">
                <i class="fa fa-check"></i>确定
            </button>
        </div>
    </div>
</div>

<!-- 全局上下文路径配置 -->
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
    window.adminId=${sessionScope.adminId}

</script>

<!-- 公共JS工具库引入 -->
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<!-- 请假管理专属JS -->
<script src="${pageContext.request.contextPath}/static/js/admin/leaveManage.js"></script>
</body>
</html>