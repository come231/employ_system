<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考勤管理系统 - 工作台</title>
    <!-- 外部CDN资源 -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
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
    <!-- 页面标题 -->
    <div class="mb-8">
        <h1 class="text-2xl font-bold text-dark flex items-center">
            <i class="fa-solid fa-briefcase text-primary mr-3"></i>
            部门主管工作台
        </h1>
        <p class="text-secondary mt-2">管理您部门的考勤审批和数据分析</p>
    </div>

    <!-- 待审批汇总卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <!-- 待审批总数 -->
        <div class="bg-white rounded-xl shadow-card p-6 card-transition border-l-4 border-primary">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-secondary text-sm mb-1">待审批总数</p>
                    <p class="text-3xl font-bold text-dark" id="total-pending-count">0</p>
                </div>
                <div class="w-14 h-14 rounded-full bg-primary/10 flex items-center justify-center">
                    <i class="fa-solid fa-clipboard-list text-primary text-2xl"></i>
                </div>
            </div>
        </div>

        <!-- 待审批请假 -->
        <div class="bg-white rounded-xl shadow-card p-6 card-transition border-l-4 border-warning">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-secondary text-sm mb-1">待审批请假</p>
                    <p class="text-3xl font-bold text-dark" id="pending-leave-count">0</p>
                </div>
                <div class="w-14 h-14 rounded-full bg-warning/10 flex items-center justify-center">
                    <i class="fa-solid fa-calendar-times text-warning text-2xl"></i>
                </div>
            </div>
            <button id="go-leave-approve" class="mt-4 text-sm text-primary hover:underline">
                去审核 <i class="fa-solid fa-arrow-right ml-1"></i>
            </button>
        </div>

        <!-- 待审批补卡 -->
        <div class="bg-white rounded-xl shadow-card p-6 card-transition border-l-4 border-success">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-secondary text-sm mb-1">待审批补卡</p>
                    <p class="text-3xl font-bold text-dark" id="pending-supplement-count">0</p>
                </div>
                <div class="w-14 h-14 rounded-full bg-success/10 flex items-center justify-center">
                    <i class="fa-solid fa-id-card text-success text-2xl"></i>
                </div>
            </div>
            <button id="go-supplement-approve" class="mt-4 text-sm text-primary hover:underline">
                去审核 <i class="fa-solid fa-arrow-right ml-1"></i>
            </button>
        </div>
    </div>

    <!-- 部门考勤分析 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <!-- 今日考勤统计 -->
        <div class="lg:col-span-2 bg-white rounded-xl shadow-card p-6 card-transition">
            <h2 class="text-xl font-bold text-dark mb-6 flex items-center">
                <i class="fa-solid fa-chart-pie text-primary mr-2"></i>
                今日部门考勤统计
            </h2>

            <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
                <div class="bg-neutral rounded-lg p-4 text-center">
                    <p class="text-secondary text-sm mb-1">部门人数</p>
                    <p class="text-2xl font-bold text-dark" id="total-employees">0</p>
                </div>
                <div class="bg-success/10 rounded-lg p-4 text-center">
                    <p class="text-secondary text-sm mb-1">正常出勤</p>
                    <p class="text-2xl font-bold text-success" id="normal-count">0</p>
                </div>
                <div class="bg-warning/10 rounded-lg p-4 text-center">
                    <p class="text-secondary text-sm mb-1">迟到</p>
                    <p class="text-2xl font-bold text-warning" id="late-count">0</p>
                </div>
                <div class="bg-danger/10 rounded-lg p-4 text-center">
                    <p class="text-secondary text-sm mb-1">缺勤</p>
                    <p class="text-2xl font-bold text-danger" id="absent-count">0</p>
                </div>
                <div class="bg-primary/10 rounded-lg p-4 text-center">
                    <p class="text-secondary text-sm mb-1">请假</p>
                    <p class="text-2xl font-bold text-primary" id="leave-count">0</p>
                </div>
            </div>

            <div class="flex items-center justify-center">
                <div class="w-64 h-64">
                    <canvas id="attendance-chart"></canvas>
                </div>
            </div>
        </div>

        <!-- 考勤率 -->
        <div class="bg-white rounded-xl shadow-card p-6 card-transition">
            <h2 class="text-xl font-bold text-dark mb-6 flex items-center">
                <i class="fa-solid fa-percentage text-primary mr-2"></i>
                今日考勤率
            </h2>

            <div class="flex flex-col items-center justify-center h-64">
                <div class="relative w-40 h-40">
                    <svg class="w-full h-full transform -rotate-90">
                        <circle cx="80" cy="80" r="70" fill="none" stroke="#F5F7FA" stroke-width="12"></circle>
                        <circle cx="80" cy="80" r="70" fill="none" stroke="#00B42A" stroke-width="12"
                                stroke-dasharray="440" stroke-dashoffset="44" stroke-linecap="round"
                                id="attendance-progress"></circle>
                    </svg>
                    <div class="absolute inset-0 flex items-center justify-center">
                        <span class="text-3xl font-bold text-dark" id="attendance-rate">0%</span>
                    </div>
                </div>
                <p class="mt-4 text-secondary text-sm">正常出勤 + 迟到 / 总人数</p>
            </div>
        </div>
    </div>

    <!-- 请假审批列表 -->
    <div id="leave-section" class="bg-white rounded-xl shadow-card p-6 card-transition mb-8">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-dark flex items-center">
                <i class="fa-solid fa-calendar-times text-warning mr-2"></i>
                部门请假申请
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
            </div>
        </div>

        <div class="overflow-x-auto">
            <table class="w-full text-sm text-left">
                <thead class="bg-neutral text-secondary uppercase rounded-lg">
                <tr>
                    <th class="px-4 py-3 rounded-l">工号</th>
                    <th class="px-4 py-3">姓名</th>
                    <th class="px-4 py-3">请假类型</th>
                    <th class="px-4 py-3">开始时间</th>
                    <th class="px-4 py-3">结束时间</th>
                    <th class="px-4 py-3">天数</th>
                    <th class="px-4 py-3">原因</th>
                    <th class="px-4 py-3">状态</th>
                    <th class="px-4 py-3 rounded-r">操作</th>
                </tr>
                </thead>
                <tbody id="leave-tbody">
                <tr><td colspan="9" class="px-4 py-8 text-center text-gray-500">加载中...</td></tr>
                </tbody>
            </table>
        </div>

        <div id="leave-pagination" class="mt-6"></div>
    </div>

    <!-- 补卡审批列表 -->
    <div id="supplement-section" class="bg-white rounded-xl shadow-card p-6 card-transition">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-dark flex items-center">
                <i class="fa-solid fa-id-card text-success mr-2"></i>
                部门补卡申请
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
            </div>
        </div>

        <div class="overflow-x-auto">
            <table class="w-full text-sm text-left">
                <thead class="bg-neutral text-secondary uppercase rounded-lg">
                <tr>
                    <th class="px-4 py-3 rounded-l">工号</th>
                    <th class="px-4 py-3">姓名</th>
                    <th class="px-4 py-3">补卡日期</th>
                    <th class="px-4 py-3">补卡类型</th>
                    <th class="px-4 py-3">实际时间</th>
                    <th class="px-4 py-3">原因</th>
                    <th class="px-4 py-3">状态</th>
                    <th class="px-4 py-3 rounded-r">操作</th>
                </tr>
                </thead>
                <tbody id="supplement-tbody">
                <tr><td colspan="8" class="px-4 py-8 text-center text-gray-500">加载中...</td></tr>
                </tbody>
            </table>
        </div>

        <div id="supplement-pagination" class="mt-6"></div>
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
<!-- 引入工作台业务逻辑 -->
<script src="${pageContext.request.contextPath}/static/js/reception/workbench.js"></script>
</body>
</html>
