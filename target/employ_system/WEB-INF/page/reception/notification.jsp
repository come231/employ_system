<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考勤管理系统 - 消息通知</title>
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
    <div class="max-w-4xl mx-auto">
        <!-- 页面标题 -->
        <div class="bg-white rounded-xl shadow-card p-6 mb-6 card-transition">
            <div class="flex items-center justify-between">
                <div class="flex items-center">
                    <i class="fa-solid fa-bell text-primary text-2xl mr-3"></i>
                    <div>
                        <h1 class="text-2xl font-bold text-dark">消息通知</h1>
                        <p class="text-secondary text-sm mt-1">查看您的所有通知消息</p>
                    </div>
                </div>
                <div class="flex items-center space-x-4">
                    <span id="unread-badge" class="bg-red-100 text-red-600 px-3 py-1 rounded-full text-sm font-medium hidden">
                        <span id="unread-count">0</span> 条未读
                    </span>
                    <button id="mark-all-read-btn" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors text-sm">
                        <i class="fa-solid fa-check-double mr-1"></i>全部已读
                    </button>
                </div>
            </div>
        </div>

        <!-- 筛选选项 -->
        <div class="bg-white rounded-xl shadow-card p-4 mb-6">
            <div class="flex flex-wrap items-center gap-4">
                <div class="flex items-center space-x-2">
                    <span class="text-secondary text-sm">筛选：</span>
                    <select id="filter-type" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
                        <option value="">全部类型</option>
                        <option value="leave_approve">请假审批</option>
                        <option value="supplement_approve">补卡审批</option>
                        <option value="promotion">升职通知</option>
                        <option value="demotion">职位变更</option>
                    </select>
                </div>
                <div class="flex items-center space-x-2">
                    <select id="filter-status" class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
                        <option value="">全部状态</option>
                        <option value="unread">未读</option>
                        <option value="read">已读</option>
                    </select>
                </div>
                <button id="filter-btn" class="px-4 py-1.5 bg-primary text-white rounded-lg text-sm hover:bg-primary/90 transition-colors">
                    <i class="fa-solid fa-search mr-1"></i>筛选
                </button>
                <button id="reset-btn" class="px-4 py-1.5 bg-gray-200 text-gray-700 rounded-lg text-sm hover:bg-gray-300 transition-colors">
                    <i class="fa-solid fa-refresh mr-1"></i>重置
                </button>
            </div>
        </div>

        <!-- 通知列表 -->
        <div class="bg-white rounded-xl shadow-card overflow-hidden">
            <div id="notification-list" class="divide-y divide-gray-100">
                <!-- 加载中状态 -->
                <div class="p-8 text-center text-gray-500">
                    <i class="fa-solid fa-spinner fa-spin text-2xl mb-2"></i>
                    <p>加载中...</p>
                </div>
            </div>

            <!-- 分页 -->
            <div id="notification-pagination" class="p-4 border-t border-gray-100"></div>
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

<!-- 通知详情模态框 -->
<div id="notification-detail-modal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-[9999] hidden">
    <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4">
        <div class="p-6 border-b border-gray-100 flex items-center justify-between">
            <h3 id="modal-title" class="text-lg font-bold text-gray-800">通知详情</h3>
            <button id="close-modal-btn" class="text-gray-400 hover:text-gray-600">
                <i class="fa-solid fa-times text-xl"></i>
            </button>
        </div>
        <div class="p-6">
            <div class="mb-4">
                <span id="modal-type-badge" class="px-3 py-1 rounded-full text-sm font-medium"></span>
            </div>
            <p id="modal-content" class="text-gray-700 mb-4"></p>
            <p id="modal-time" class="text-sm text-gray-500"></p>
        </div>
        <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-between">
            <button id="modal-delete-btn" class="px-4 py-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 transition-colors">
                <i class="fa-solid fa-trash mr-1"></i>删除
            </button>
            <div class="flex space-x-3">
                <button id="modal-view-btn" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors hidden">
                    <i class="fa-solid fa-eye mr-1"></i>查看详情
                </button>
                <button id="modal-close-btn" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors">关闭</button>
            </div>
        </div>
    </div>
</div>

<!-- 全局上下文路径 -->
<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<!-- 引入公共工具库 -->
<script src="${pageContext.request.contextPath}/static/js/reception/common.js"></script>
<!-- 引入消息通知业务逻辑 -->
<script src="${pageContext.request.contextPath}/static/js/reception/notification.js"></script>
</body>
</html>
