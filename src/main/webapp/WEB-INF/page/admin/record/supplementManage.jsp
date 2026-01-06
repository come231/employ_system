<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>补卡管理 - 员工考勤管理系统</title>
    <!-- 公共依赖引入 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- 公共样式引入 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <!-- 补卡管理专属样式 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/supplementManage.css" rel="stylesheet">
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
        .top-bar-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 20px 15px;
            width: 100%;
            box-sizing: border-box;
        }
    </style>
</head>
<body>
<!-- 引入顶部标题栏 -->
<jsp:include page="../common/header.jsp"></jsp:include>

<!-- 引入侧边栏菜单 -->
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="supplementManage"/>
</jsp:include>

<!-- 主内容区 -->
<div class="main-content" id="mainContent">
    <!-- 顶部信息+搜索区域 -->
    <div class="top-bar-container">
        <!-- 左侧补卡记录信息 -->
        <div class="record-info" id="recordInfo">共 <span id="totalRecordsNum">0</span> 条补卡记录</div>

        <!-- 右侧搜索区域 -->
        <div class="search-container">
            <input type="text" class="search-input" id="searchInput" placeholder="例：工号Y2025001/上班/待审批">
            <select class="search-select" id="searchField">
                <option value="empNo">工号</option>
                <option value="checkType">补卡类型</option>
                <option value="status">审批状态</option>
            </select>
            <button class="search-btn" id="searchBtn">
                <i class="fa fa-search"></i> 搜索
            </button>
        </div>
    </div>

    <!-- 补卡列表表格 -->
    <div class="table-container">
        <table class="supplement-table">
            <thead>
            <tr>
                <th>补卡ID</th>
                <th>工号</th>
                <th>补卡日期</th>
                <th>补卡类型</th>
                <th>实际时间</th>
                <th>申请时间</th>
                <th>审批人</th>
                <th>审批状态</th>
                <th>补卡原因</th>
                <th>审批意见</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody id="supplementTableBody">
            <!-- 加载占位符 -->
            <tr><td colspan="11" style="text-align: center; padding: 50px;">加载中...</td></tr>
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

<!-- 审批弹窗 -->
<div class="approve-modal" id="approveModal">
    <div class="approve-modal-content">
        <h3 style="margin-bottom: 20px; color: #333;">补卡审批</h3>
        <input type="hidden" id="currentSupplementId">
        <div class="modal-form-item">
            <label>审批结果</label>
            <select id="approveResult">
                <option value="">请选择</option>
                <option value="1">通过</option>
                <option value="2">驳回</option>
            </select>
        </div>
        <div class="modal-form-item">
            <label>审批意见</label>
            <textarea id="approveOpinionInput" placeholder="请输入审批意见（选填）"></textarea>
        </div>
        <div class="modal-btn-group">
            <button class="modal-btn cancel-btn" id="closeModalBtn">
                <i class="fa fa-times"></i> 取消
            </button>
            <button class="modal-btn confirm-btn" id="submitApproveBtn">
                <i class="fa fa-check"></i> 提交审批
            </button>
        </div>
    </div>
</div>

<!-- 全局上下文路径 -->
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>

<!-- 引入公共js文件 -->
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<script src="${pageContext.request.contextPath}/static/js/admin/supplementManage.js"></script>
</body>
</html>
