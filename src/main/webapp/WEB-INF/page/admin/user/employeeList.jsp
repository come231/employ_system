<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>员工列表 - 员工考勤管理系统</title>
    <!-- 引入Font Awesome图标库 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <!-- 引入Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <!-- 引入拆分后的样式文件 -->
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/employeeList.css" rel="stylesheet">
</head>
<body>
<!-- 引入顶部标题栏 -->
<jsp:include page="../common/header.jsp"></jsp:include>

<!-- 引入侧边栏菜单 -->
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="employeeList"/>
</jsp:include>

<!-- 主内容区（新增，解决布局遮挡问题） -->
<div class="main-content" id="mainContent">
    <!-- 顶部信息+搜索区域 - 新增容器调整布局 -->
    <div class="top-bar-container">
        <!-- 左侧员工记录信息 -->
        <div class="record-info" id="recordInfo">
            共 <span id="totalRecordsNum">0</span> 条员工记录
            <button class="btn add-employee-btn" onclick="openAddModal()" style="margin-left: 20px; background-color: #4CAF50; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
                <i class="fa fa-plus"></i> 新增员工
            </button>
        </div>

        <!-- 右侧搜索区域 -->
        <div class="search-container">
            <!-- 优化搜索框占位符（修改HTML中对应行） -->
            <input type="text" class="search-input" id="searchInput" placeholder="例：张三/工号Y2025001/13800138000">
            <select class="search-select" id="searchField">
                <option value="empNo">工号</option>
                <option value="name">姓名</option>
                <option value="phone">电话</option>
                <option value="email">邮箱</option>
                <option value="position">职位</option>
            </select>
            <button class="search-btn" id="searchBtn">
                <i class="fa fa-search"></i> 搜索
            </button>
        </div>
    </div>

    <!-- 员工列表表格 -->
    <div class="table-container">
        <table class="employee-table">
            <!-- 表格内容不变 -->
            <thead>
            <tr>
                <th>工号</th>
                <th>姓名</th>
                <th>电话</th>
                <th>邮箱</th>
                <th>状态</th>
                <th>职位</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody id="employeeTableBody">
            <!-- 加载占位符 -->
            <tr><td colspan="7" style="text-align: center; padding: 50px;">加载中...</td></tr>
            </tbody>
        </table>

        <!-- 分页控件 - 结构不变，样式通过CSS优化 -->
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

<!-- 编辑员工弹窗 -->
<div class="modal-mask" id="editModal">
    <div class="modal-container">
        <div class="modal-header">
            <h3 class="modal-title">编辑员工信息</h3>
            <button class="modal-close" onclick="closeEditModal()">&times;</button>
        </div>
        <div class="modal-body">
            <input type="hidden" id="editEmpNo">
            <div class="form-item">
                <label class="form-label">工号</label>
                <input type="text" class="form-input" id="editEmpNoShow" readonly>
            </div>
            <div class="form-item">
                <label class="form-label">姓名</label>
                <input type="text" class="form-input" id="editName" placeholder="请输入姓名">
            </div>
            <div class="form-item">
                <label class="form-label">电话</label>
                <input type="text" class="form-input" id="editPhone" placeholder="请输入电话">
            </div>
            <div class="form-item">
                <label class="form-label">邮箱</label>
                <input type="email" class="form-input" id="editEmail" placeholder="请输入邮箱">
            </div>
            <div class="form-item">
                <label class="form-label">状态</label>
                <select class="form-select" id="editStatus">
                    <option value="1">在职</option>
                    <option value="0">离职</option>
                </select>
            </div>
            <div class="form-item">
                <label class="form-label">部门</label>
                <select class="form-select" id="editDept">
                    <option value="">请选择部门</option>
                </select>
            </div>
            <div class="form-item">
                <label class="form-label">职位/角色</label>
                <select class="form-select" id="editRole">
                    <option value="">请选择职位</option>
                </select>
            </div>
        </div>
        <div class="modal-footer">
            <button class="modal-cancel" onclick="closeEditModal()">取消</button>
            <button class="modal-confirm" onclick="submitEditForm()">提交</button>
        </div>
    </div>
</div>

<!-- 删除确认弹窗 -->
<div class="modal-mask" id="deleteModal">
    <div class="modal-container delete-modal">
        <div class="modal-header">
            <h3 class="modal-title">确认删除</h3>
            <button class="modal-close" onclick="closeDeleteModal()">&times;</button>
        </div>
        <div class="modal-body">
            <input type="hidden" id="deleteEmpNo">
            <div class="delete-icon">
                <i class="fa fa-exclamation-circle"></i>
            </div>
            <div class="delete-tip" id="deleteTipText">确定要删除员工【张三】吗？此操作不可撤销！</div>
        </div>
        <div class="modal-footer">
            <button class="modal-cancel" onclick="closeDeleteModal()">取消</button>
            <button class="modal-confirm" onclick="submitDelete()">确认删除</button>
        </div>
    </div>
</div>

<!-- 新增员工弹窗 -->
<div class="modal-mask" id="addModal">
    <div class="modal-container">
        <div class="modal-header">
            <h3 class="modal-title">新增员工</h3>
            <button class="modal-close" onclick="closeAddModal()">&times;</button>
        </div>
        <div class="modal-body">
            <div class="form-item">
                <label class="form-label">姓名 <span style="color: red;">*</span></label>
                <input type="text" class="form-input" id="addName" placeholder="请输入姓名">
            </div>
            <div class="form-item">
                <label class="form-label">电话</label>
                <input type="text" class="form-input" id="addPhone" placeholder="请输入电话">
            </div>
            <div class="form-item">
                <label class="form-label">邮箱</label>
                <input type="email" class="form-input" id="addEmail" placeholder="请输入邮箱">
            </div>
            <div class="form-item">
                <label class="form-label">部门 <span style="color: red;">*</span></label>
                <select class="form-select" id="addDept">
                    <option value="">请选择部门</option>
                </select>
            </div>
            <div class="form-item">
                <label class="form-label">职位/角色</label>
                <select class="form-select" id="addRole">
                    <option value="">请选择职位</option>
                </select>
            </div>
            <div class="form-item" style="background-color: #f0f9ff; padding: 10px; border-radius: 5px; margin-top: 10px;">
                <i class="fa fa-info-circle" style="color: #2196F3;"></i>
                <span style="color: #666; font-size: 12px;">
                    系统将自动生成工号，用户名为姓名拼音，默认密码为123
                </span>
            </div>
        </div>
        <div class="modal-footer">
            <button class="modal-cancel" onclick="closeAddModal()">取消</button>
            <button class="modal-confirm" onclick="submitAddForm()">提交</button>
        </div>
    </div>
</div>
<!-- 引入外部JS CDN -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<!-- 全局上下文路径 -->
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>

<!-- 引入公共js文件 -->
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>

<script src="${pageContext.request.contextPath}/static/js/admin/employeeList.js"></script>
</body>
</html>