<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考勤管理系统 - 个人中心</title>
    <!-- 外部CDN资源 -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Tailwind配置 -->
    <script src="${pageContext.request.contextPath}/static/js/reception/tailwind.config.js"></script>
    <!-- 公共CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/reception/common.css">
</head>
<body class="font-inter bg-neutral min-h-screen flex flex-col">
<!-- 导航栏容器 -->
<div id="navbar-container"></div>

<!-- 主要内容区域 -->
<main class="flex-grow container mx-auto px-4 md:px-6 py-8">
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- 左侧侧边栏 -->
        <div class="lg:col-span-1">
            <div class="bg-white rounded-xl shadow-card p-4 sticky top-24 card-transition">
                <!-- 用户信息卡片 -->
                <div id="user-card">
                    <div class="flex flex-col items-center py-6 border-b border-gray-100 mb-4">
                        <div class="w-20 h-20 rounded-full bg-gray-200 animate-pulse mb-3"></div>
                        <div class="h-6 w-20 bg-gray-200 rounded animate-pulse mb-2"></div>
                        <div class="h-4 w-32 bg-gray-200 rounded animate-pulse"></div>
                    </div>
                </div>

                <!-- 侧边栏导航 -->
                <ul class="space-y-1">
                    <li>
                        <a href="#personal-info" class="sidebar-item active flex items-center px-4 py-3 rounded-lg text-dark font-medium" data-tab="personal-info">
                            <i class="fa-solid fa-user-circle w-5 text-center mr-3 text-primary"></i>
                            个人信息
                        </a>
                    </li>
                    <li>
                        <a href="#my-punch" class="sidebar-item flex items-center px-4 py-3 rounded-lg text-dark font-medium" data-tab="my-punch">
                            <i class="fa-solid fa-clipboard-check w-5 text-center mr-3 text-secondary"></i>
                            我的打卡
                        </a>
                    </li>
                    <li>
                        <a href="#my-repair" class="sidebar-item flex items-center px-4 py-3 rounded-lg text-dark font-medium" data-tab="my-repair">
                            <i class="fa-solid fa-id-card w-5 text-center mr-3 text-secondary"></i>
                            我的补卡
                        </a>
                    </li>
                    <li>
                        <a href="#my-leave" class="sidebar-item flex items-center px-4 py-3 rounded-lg text-dark font-medium" data-tab="my-leave">
                            <i class="fa-solid fa-calendar-times w-5 text-center mr-3 text-secondary"></i>
                            我的请假
                        </a>
                    </li>
                    <li>
                        <a href="#dept-info" class="sidebar-item flex items-center px-4 py-3 rounded-lg text-dark font-medium" data-tab="dept-info">
                            <i class="fa-solid fa-building w-5 text-center mr-3 text-secondary"></i>
                            部门信息
                        </a>
                    </li>
                </ul>
            </div>
        </div>

        <!-- 右侧内容区 -->
        <div class="lg:col-span-3">
            <!-- 个人信息标签页 -->
            <div id="personal-info" class="tab-content active">
                <div class="bg-white rounded-xl shadow-card p-6 card-transition mb-6">
                    <h2 class="text-xl font-bold text-dark mb-6 flex items-center">
                        <i class="fa-solid fa-user-circle text-primary mr-2"></i>
                        个人信息
                    </h2>
                    <div id="personal-info-content">
                        <div class="text-center py-8 text-gray-500">加载中...</div>
                    </div>
                </div>
            </div>

            <!-- 我的打卡标签页 -->
            <div id="my-punch" class="tab-content">
                <div class="bg-white rounded-xl shadow-card p-6 card-transition mb-6">
                    <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
                        <h2 class="text-xl font-bold text-dark flex items-center">
                            <i class="fa-solid fa-clipboard-check text-primary mr-2"></i>
                            我的打卡记录
                        </h2>
                        <div class="flex flex-wrap gap-2 mt-4 md:mt-0">
                            <select id="attendance-status-filter" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
                                <option value="">全部状态</option>
                                <option value="0">正常</option>
                                <option value="1">迟到</option>
                                <option value="2">早退</option>
                                <option value="3">缺勤</option>
                            </select>
                            <input type="date" id="attendance-start-date" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
                            <input type="date" id="attendance-end-date" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
                            <button id="attendance-filter-btn" class="px-4 py-1.5 bg-primary text-white rounded-lg text-sm hover:bg-primary/90 transition-colors">
                                <i class="fa-solid fa-search mr-1"></i>筛选
                            </button>
                        </div>
                    </div>

                    <!-- 打卡记录表格 -->
                    <div class="overflow-x-auto">
                        <table class="w-full text-sm text-left">
                            <thead class="bg-neutral text-secondary uppercase rounded-lg">
                            <tr>
                                <th class="px-4 py-3 rounded-l">日期</th>
                                <th class="px-4 py-3">上班打卡</th>
                                <th class="px-4 py-3">下班打卡</th>
                                <th class="px-4 py-3">状态</th>
                                <th class="px-4 py-3 rounded-r">备注</th>
                            </tr>
                            </thead>
                            <tbody id="attendance-tbody">
                            <tr><td colspan="5" class="px-4 py-8 text-center text-gray-500">加载中...</td></tr>
                            </tbody>
                        </table>
                    </div>

                    <div id="attendance-pagination" class="mt-6"></div>
                </div>
            </div>

            <!-- 我的补卡标签页 -->
            <div id="my-repair" class="tab-content">
                <div class="bg-white rounded-xl shadow-card p-6 card-transition mb-6">
                    <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
                        <h2 class="text-xl font-bold text-dark flex items-center">
                            <i class="fa-solid fa-id-card text-primary mr-2"></i>
                            我的补卡记录
                        </h2>
                        <div class="flex flex-wrap gap-2 mt-4 md:mt-0">
                            <select id="supplement-status-filter" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
                                <option value="">全部状态</option>
                                <option value="0">待审批</option>
                                <option value="1">已通过</option>
                                <option value="2">已驳回</option>
                            </select>
                            <button id="supplement-filter-btn" class="px-4 py-1.5 bg-primary text-white rounded-lg text-sm hover:bg-primary/90 transition-colors">
                                <i class="fa-solid fa-search mr-1"></i>筛选
                            </button>
                            <button id="add-supplement-btn" class="px-4 py-1.5 bg-success text-white rounded-lg text-sm hover:bg-success/90 transition-colors">
                                <i class="fa-solid fa-plus mr-1"></i>新增补卡
                            </button>
                        </div>
                    </div>

                    <!-- 补卡记录表格 -->
                    <div class="overflow-x-auto">
                        <table class="w-full text-sm text-left">
                            <thead class="bg-neutral text-secondary uppercase rounded-lg">
                            <tr>
                                <th class="px-4 py-3 rounded-l">补卡日期</th>
                                <th class="px-4 py-3">补卡类型</th>
                                <th class="px-4 py-3">申请时间</th>
                                <th class="px-4 py-3">状态</th>
                                <th class="px-4 py-3">审批人</th>
                                <th class="px-4 py-3 rounded-r">操作</th>
                            </tr>
                            </thead>
                            <tbody id="supplement-tbody">
                            <tr><td colspan="6" class="px-4 py-8 text-center text-gray-500">加载中...</td></tr>
                            </tbody>
                        </table>
                    </div>

                    <div id="supplement-pagination" class="mt-6"></div>
                </div>
            </div>

            <!-- 我的请假标签页 -->
            <div id="my-leave" class="tab-content">
                <div class="bg-white rounded-xl shadow-card p-6 card-transition mb-6">
                    <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
                        <h2 class="text-xl font-bold text-dark flex items-center">
                            <i class="fa-solid fa-calendar-times text-primary mr-2"></i>
                            我的请假记录
                        </h2>
                        <div class="flex flex-wrap gap-2 mt-4 md:mt-0">
                            <select id="leave-status-filter" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
                                <option value="">全部状态</option>
                                <option value="0">待审批</option>
                                <option value="1">已通过</option>
                                <option value="2">已驳回</option>
                            </select>
                            <button id="leave-filter-btn" class="px-4 py-1.5 bg-primary text-white rounded-lg text-sm hover:bg-primary/90 transition-colors">
                                <i class="fa-solid fa-search mr-1"></i>筛选
                            </button>
                            <button id="add-leave-btn" class="px-4 py-1.5 bg-success text-white rounded-lg text-sm hover:bg-success/90 transition-colors">
                                <i class="fa-solid fa-plus mr-1"></i>新增请假
                            </button>
                        </div>
                    </div>

                    <!-- 请假记录表格 -->
                    <div class="overflow-x-auto">
                        <table class="w-full text-sm text-left">
                            <thead class="bg-neutral text-secondary uppercase rounded-lg">
                            <tr>
                                <th class="px-4 py-3 rounded-l">请假类型</th>
                                <th class="px-4 py-3">请假时间</th>
                                <th class="px-4 py-3">天数</th>
                                <th class="px-4 py-3">状态</th>
                                <th class="px-4 py-3">审批人</th>
                                <th class="px-4 py-3 rounded-r">操作</th>
                            </tr>
                            </thead>
                            <tbody id="leave-tbody">
                            <tr><td colspan="6" class="px-4 py-8 text-center text-gray-500">加载中...</td></tr>
                            </tbody>
                        </table>
                    </div>

                    <div id="leave-pagination" class="mt-6"></div>
                </div>
            </div>

            <!-- 部门信息标签页 -->
            <div id="dept-info" class="tab-content">
                <div class="bg-white rounded-xl shadow-card p-6 card-transition mb-6">
                    <h2 class="text-xl font-bold text-dark mb-6 flex items-center">
                        <i class="fa-solid fa-building text-primary mr-2"></i>
                        部门信息
                    </h2>

                    <!-- 部门基本信息 -->
                    <div class="bg-neutral rounded-lg p-6 mb-6" id="dept-basic-info">
                        <div class="text-center py-4 text-gray-500">加载中...</div>
                    </div>

                    <!-- 部门成员列表 -->
                    <h3 class="text-lg font-semibold text-dark mb-4">部门成员</h3>
                    <div class="overflow-x-auto">
                        <table class="w-full text-sm text-left">
                            <thead class="bg-neutral text-secondary uppercase rounded-lg">
                            <tr>
                                <th class="px-4 py-3 rounded-l">姓名</th>
                                <th class="px-4 py-3">职位</th>
                                <th class="px-4 py-3">入职时间</th>
                                <th class="px-4 py-3">联系方式</th>
                                <th class="px-4 py-3 rounded-r">状态</th>
                            </tr>
                            </thead>
                            <tbody id="dept-members-tbody">
                            <tr><td colspan="5" class="px-4 py-8 text-center text-gray-500">加载中...</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- 页脚 -->
<footer class="bg-white border-t border-gray-200 py-6 mt-8">
    <div class="container mx-auto px-4 md:px-6">
        <div class="flex flex-col md:flex-row justify-between items-center">
            <div class="text-secondary text-sm mb-4 md:mb-0">
                <p>&copy; 2025 考勤管理系统 - 版权所有</p>
            </div>
            <div class="flex space-x-6 text-sm">
                <a href="#" class="text-secondary hover:text-primary transition-colors">使用帮助</a>
                <a href="#" class="text-secondary hover:text-primary transition-colors">隐私政策</a>
                <a href="#" class="text-secondary hover:text-primary transition-colors">联系我们</a>
            </div>
        </div>
    </div>
</footer>

<!-- 全局上下文路径 -->
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<!-- 引入公共工具库 -->
<script src="${pageContext.request.contextPath}/static/js/reception/common.js"></script>
<!-- 引入个人中心业务逻辑 -->
<script src="${pageContext.request.contextPath}/static/js/reception/center.js"></script>
</body>
</html>
