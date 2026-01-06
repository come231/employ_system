<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>部门列表 - 员工考勤管理系统</title>
    <!-- 引入公共资源 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <!-- 引入拆分的样式文件 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/deptList.css" rel="stylesheet">
    <!-- 引入分离后的主样式文件 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/deptList.css" rel="stylesheet">
</head>
<body>
<!-- 引入顶部标题栏 -->
<jsp:include page="../common/header.jsp"></jsp:include>

<!-- 引入侧边栏菜单 -->
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="deptList"/>
</jsp:include>

<!-- 主内容区 -->
<div class="main-content" id="mainContent">
    <!-- 顶部信息+搜索区域 - 完全模仿员工列表布局 -->
    <div class="top-bar-container">
        <!-- 左侧部门记录信息 -->
        <div class="record-info" id="recordInfo">
            共 <span id="totalRecordsNum">0</span> 条部门记录
            <button class="btn add-dept-btn" onclick="openAddDeptModal()" style="margin-left: 20px; background-color: #4CAF50; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
                <i class="fa fa-plus"></i> 新增部门
            </button>
        </div>

        <!-- 右侧搜索区域 -->
        <div class="search-container">
            <input type="text" class="search-input" id="searchInput" placeholder="例：研发部/人事部/行政部">
            <button class="search-btn" id="searchBtn">
                <i class="fa fa-search"></i> 搜索
            </button>
        </div>
    </div>

    <!-- 部门列表表格 -->
    <div class="table-container">
        <table class="dept-table">
            <thead>
            <tr>
                <th>部门ID</th>
                <th>部门名称</th>
                <th>部门经理</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>员工数</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody id="deptTableBody">
            <tr><td colspan="7" style="text-align: center; padding: 50px;">加载中...</td></tr>
            </tbody>
        </table>

        <!-- 分页控件 - 完全模仿员工列表样式 -->
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

<!-- 编辑部门弹窗 -->
<div class="modal-mask" id="editDeptModal">
    <div class="modal-container">
        <div class="modal-header">
            <h3 class="modal-title">编辑部门信息</h3>
            <button class="modal-close" onclick="closeEditDeptModal()">&times;</button>
        </div>
        <div class="modal-body">
            <input type="hidden" id="editDeptId">
            <div class="form-item">
                <label class="form-label">部门ID</label>
                <input type="text" class="form-input" id="editDeptIdShow" readonly>
            </div>
            <div class="form-item">
                <label class="form-label">部门名称</label>
                <input type="text" class="form-input" id="editDeptName" placeholder="请输入部门名称">
            </div>
            <div class="form-item">
                <label class="form-label">部门经理</label>
                <select class="form-select" id="editManagerId">
                    <option value="">请选择部门主管（可选）</option>
                </select>
            </div>
            <div class="form-item">
                <label class="form-label">状态</label>
                <select class="form-select" id="editDeptStatus">
                    <option value="1">启用</option>
                    <option value="0">禁用</option>
                </select>
            </div>
        </div>
        <div class="modal-footer">
            <button class="modal-cancel" onclick="closeEditDeptModal()">取消</button>
            <button class="modal-confirm" onclick="submitEditDeptForm()">提交</button>
        </div>
    </div>
</div>

<!-- 删除部门弹窗 -->
<div class="modal-mask" id="deleteDeptModal">
    <div class="modal-container delete-modal">
        <div class="modal-header">
            <h3 class="modal-title">确认删除</h3>
            <button class="modal-close" onclick="closeDeleteDeptModal()">&times;</button>
        </div>
        <div class="modal-body">
            <input type="hidden" id="deleteDeptId">
            <div class="delete-icon">
                <i class="fa fa-exclamation-circle"></i>
            </div>
            <div class="delete-tip" id="deleteDeptTipText">确定要删除部门【研发部】吗？此操作不可撤销！</div>
        </div>
        <div class="modal-footer">
            <button class="modal-cancel" onclick="closeDeleteDeptModal()">取消</button>
            <button class="modal-confirm" onclick="submitDeleteDept()">确认删除</button>
        </div>
    </div>
</div>

<!-- 部门员工详情弹窗 -->
<div class="modal-mask dept-emp-modal" id="deptEmpModal">
    <div class="modal-container">
        <div class="modal-header">
            <h3 class="modal-title" id="deptEmpModalTitle">研发部 - 员工列表</h3>
            <button class="modal-close" onclick="closeDeptEmpModal()">&times;</button>
        </div>
        <div class="modal-body">
            <table class="emp-detail-table">
                <thead>
                <tr>
                    <th>工号</th>
                    <th>姓名</th>
                    <th>电话</th>
                    <th>邮箱</th>
                    <th>职位</th>
                </tr>
                </thead>
                <tbody id="deptEmpTableBody">
                <tr><td colspan="5" style="text-align: center; padding: 30px;">加载中...</td></tr>
                </tbody>
            </table>
            <!-- 员工分页控件 -->
            <div class="pagination-container" id="deptEmpPaginationContainer">
                <div class="pagination-info">
                    <span>每页显示</span>
                    <select class="page-size-select" id="deptEmpPageSizeSelect">
                        <option value="6" selected>6</option>
                        <option value="10">10</option>
                        <option value="20">20</option>
                    </select>
                    <span>条</span>
                </div>
                <div class="pagination-controls">
                    <button class="page-btn" id="deptEmpFirstPage">首页</button>
                    <button class="page-btn" id="deptEmpPrevPage">上一页</button>
                    <div class="page-numbers" id="deptEmpPageNumbers"></div>
                    <button class="page-btn" id="deptEmpNextPage">下一页</button>
                    <button class="page-btn" id="deptEmpLastPage">末页</button>
                </div>
                <div class="pagination-info">
                    <span>第 <span id="deptEmpCurrentPageInfo">1</span> / <span id="deptEmpTotalPages">1</span> 页</span>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button class="modal-confirm" onclick="closeDeptEmpModal()">关闭</button>
        </div>
    </div>
</div>

<!-- 新增部门弹窗 -->
<div class="modal-mask" id="addDeptModal">
    <div class="modal-container">
        <div class="modal-header">
            <h3 class="modal-title">新增部门</h3>
            <button class="modal-close" onclick="closeAddDeptModal()">&times;</button>
        </div>
        <div class="modal-body">
            <div class="form-item">
                <label class="form-label">部门名称 <span style="color: red;">*</span></label>
                <input type="text" class="form-input" id="addDeptName" placeholder="请输入部门名称">
            </div>
            <div class="form-item">
                <label class="form-label">部门主管</label>
                <select class="form-select" id="addDeptManager">
                    <option value="">请选择部门主管（可选）</option>
                </select>
            </div>
            <div class="form-item" style="background-color: #f0f9ff; padding: 10px; border-radius: 5px; margin-top: 10px;">
                <i class="fa fa-info-circle" style="color: #2196F3;"></i>
                <span style="color: #666; font-size: 12px;">
                    部门创建后默认为启用状态，可在编辑时修改
                </span>
            </div>
        </div>
        <div class="modal-footer">
            <button class="modal-cancel" onclick="closeAddDeptModal()">取消</button>
            <button class="modal-confirm" onclick="submitAddDeptForm()">提交</button>
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
<!-- 引入分离后的部门列表JS文件 -->
<script src="${pageContext.request.contextPath}/static/js/admin/deptList.js"></script>
</body>
</html>