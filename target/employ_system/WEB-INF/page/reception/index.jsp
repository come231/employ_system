<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考勤管理系统 - 员工首页</title>
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
    <!-- 欢迎卡片 -->
    <div class="bg-white rounded-xl shadow-card p-6 mb-8 card-transition">
        <div class="flex flex-col md:flex-row md:items-center justify-between">
            <div>
                <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-dark" id="welcome-title">
                    您好，用户 <span class="text-primary">👋</span>
                </h2>
                <p class="text-secondary mt-2">
                    今天是 <span class="font-medium text-dark" id="currentDate"></span> | 本月考勤率：<span class="text-success font-medium" id="attendance-rate">0%</span>
                </p>
            </div>
            <div class="mt-4 md:mt-0">
                <button id="export-btn" class="bg-primary hover:bg-primary/90 text-white px-6 py-2 rounded-lg transition-colors flex items-center">
                    <i class="fa-solid fa-download mr-2"></i>导出本月考勤表
                </button>
            </div>
        </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左侧内容区 -->
        <div class="lg:col-span-2 space-y-6">
            <!-- 考勤榜单模块 -->
            <div class="bg-white rounded-xl shadow-card p-6 card-transition">
                <div class="flex flex-col md:flex-row md:items-center justify-between mb-6">
                    <h2 class="text-xl font-bold text-dark flex items-center">
                        <i class="fa-solid fa-ranking-star text-warning mr-2"></i>
                        考勤榜单
                    </h2>
                    <div class="flex space-x-2 mt-2 md:mt-0">
                        <span class="px-3 py-1 rounded-lg bg-primary text-white text-sm">本月</span>
                    </div>
                </div>

                <!-- 双榜单 -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <!-- 个人员工考勤次数榜单 -->
                    <div class="bg-neutral/50 rounded-lg p-4">
                        <h3 class="text-lg font-semibold text-dark mb-4">个人考勤次数榜 (TOP 5)</h3>
                        <div class="overflow-x-auto">
                            <table class="w-full text-sm text-left">
                                <thead class="bg-white/80 text-secondary uppercase rounded-lg">
                                <tr>
                                    <th class="px-4 py-3 rounded-l">排名</th>
                                    <th class="px-4 py-3">姓名</th>
                                    <th class="px-4 py-3">部门</th>
                                    <th class="px-4 py-3 rounded-r">考勤次数</th>
                                </tr>
                                </thead>
                                <tbody id="employee-ranking-body">
                                <tr><td colspan="4" class="px-4 py-3 text-center text-gray-500">加载中...</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- 部门考勤率榜单 -->
                    <div class="bg-neutral/50 rounded-lg p-4">
                        <h3 class="text-lg font-semibold text-dark mb-4">部门考勤率榜 (TOP 5)</h3>
                        <div class="overflow-x-auto">
                            <table class="w-full text-sm text-left">
                                <thead class="bg-white/80 text-secondary uppercase rounded-lg">
                                <tr>
                                    <th class="px-4 py-3 rounded-l">排名</th>
                                    <th class="px-4 py-3">部门</th>
                                    <th class="px-4 py-3">人数</th>
                                    <th class="px-4 py-3 rounded-r">考勤率</th>
                                </tr>
                                </thead>
                                <tbody id="dept-ranking-body">
                                <tr><td colspan="4" class="px-4 py-3 text-center text-gray-500">加载中...</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- 请假/补卡申请模块 -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
                    <!-- 请假申请 -->
                    <div class="bg-white rounded-lg shadow-sm p-5 card-transition border border-gray-100">
                        <div class="flex items-start justify-between mb-4">
                            <h3 class="text-lg font-semibold text-dark flex items-center">
                                <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center mr-3">
                                    <i class="fa-solid fa-calendar-times text-primary"></i>
                                </div>
                                请假申请
                            </h3>
                            <span class="bg-warning/10 text-warning text-xs px-2 py-1 rounded-full">快速审批</span>
                        </div>
                        <p class="text-secondary text-sm mb-5">支持年假、病假、事假等多种假期类型，在线提交快速审批</p>
                        <div class="grid grid-cols-2 gap-3 mb-5">
                            <div class="bg-neutral rounded-lg p-3">
                                <p class="text-xs text-secondary mb-1">本月剩余年假</p>
                                <p class="text-xl font-bold text-dark" id="remain-leave">-天</p>
                            </div>
                            <div class="bg-neutral rounded-lg p-3">
                                <p class="text-xs text-secondary mb-1">平均审批时长</p>
                                <p class="text-xl font-bold text-dark" id="avg-approval-time">-</p>
                            </div>
                        </div>
                        <button id="leave-apply-btn" class="w-full bg-primary hover:bg-primary/90 text-white py-2 rounded-lg transition-colors flex items-center justify-center">
                            <i class="fa-solid fa-plus mr-2"></i>提交请假申请
                        </button>
                    </div>

                    <!-- 补卡申请 -->
                    <div class="bg-white rounded-lg shadow-sm p-5 card-transition border border-gray-100">
                        <div class="flex items-start justify-between mb-4">
                            <h3 class="text-lg font-semibold text-dark flex items-center">
                                <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center mr-3">
                                    <i class="fa-solid fa-id-card text-primary"></i>
                                </div>
                                补卡申请
                            </h3>
                            <span class="bg-primary/10 text-primary text-xs px-2 py-1 rounded-full">每月限3次</span>
                        </div>
                        <p class="text-secondary text-sm mb-5">忘记打卡？在线提交补卡申请，管理人员快速审核</p>
                        <div class="grid grid-cols-2 gap-3 mb-5">
                            <div class="bg-neutral rounded-lg p-3">
                                <p class="text-xs text-secondary mb-1">本月已用次数</p>
                                <p class="text-xl font-bold text-dark" id="used-supplement">-次</p>
                            </div>
                            <div class="bg-neutral rounded-lg p-3">
                                <p class="text-xs text-secondary mb-1">审批通过率</p>
                                <p class="text-xl font-bold text-dark" id="approval-pass-rate">-</p>
                            </div>
                        </div>
                        <button id="supplement-apply-btn" class="w-full bg-primary hover:bg-primary/90 text-white py-2 rounded-lg transition-colors flex items-center justify-center">
                            <i class="fa-solid fa-plus mr-2"></i>提交补卡申请
                        </button>
                    </div>
                </div>
            </div>

            <!-- 打卡按钮 -->
            <section class="bg-white rounded-xl shadow-card p-6 card-transition">
                <h2 class="text-xl font-bold text-dark mb-6 flex items-center">
                    <i class="fa-solid fa-clipboard-check text-success mr-2"></i>
                    今日打卡
                </h2>
                <div class="flex justify-center">
                    <button id="punchBtn" class="bg-success hover:bg-success/90 text-white rounded-full w-48 h-48 md:w-60 md:h-60 flex flex-col items-center justify-center transition-all shadow-lg hover:shadow-xl">
                        <i class="fa-solid fa-clock text-3xl mb-3"></i>
                        <span id="countdown" class="text-xl font-bold">00:00:00</span>
                        <span class="text-sm mt-2">距离打卡结束</span>
                    </button>
                </div>
                <div class="flex justify-center mt-4 text-secondary text-sm" id="punch-status">
                    <p>今日打卡时间：09:00 - 18:00 | 当前状态：<span class="text-warning">未打卡</span></p>
                </div>
            </section>
        </div>

        <!-- 右侧日期打卡状态表 -->
        <section class="bg-white rounded-xl shadow-card p-6 card-transition">
            <h2 class="text-xl font-bold text-dark mb-6 flex items-center">
                <i class="fa-solid fa-calendar-days text-primary mr-2"></i>
                本月打卡记录
            </h2>
            <div class="overflow-auto">
                <!-- 日历头部 -->
                <div class="flex justify-between items-center mb-6">
                    <h3 class="font-semibold text-lg" id="calendar-title">2025年12月</h3>
                    <div class="flex space-x-2">
                        <button id="prev-month-btn" class="w-8 h-8 rounded-full flex items-center justify-center text-secondary hover:bg-primary/5 hover:text-primary transition-colors">
                            <i class="fa-solid fa-chevron-left"></i>
                        </button>
                        <button id="next-month-btn" class="w-8 h-8 rounded-full flex items-center justify-center text-secondary hover:bg-primary/5 hover:text-primary transition-colors">
                            <i class="fa-solid fa-chevron-right"></i>
                        </button>
                    </div>
                </div>

                <!-- 星期标题 -->
                <div class="grid grid-cols-7 gap-1 mb-3">
                    <div class="text-center text-secondary text-sm font-medium py-2 rounded-lg bg-neutral">日</div>
                    <div class="text-center text-secondary text-sm font-medium py-2 rounded-lg bg-neutral">一</div>
                    <div class="text-center text-secondary text-sm font-medium py-2 rounded-lg bg-neutral">二</div>
                    <div class="text-center text-secondary text-sm font-medium py-2 rounded-lg bg-neutral">三</div>
                    <div class="text-center text-secondary text-sm font-medium py-2 rounded-lg bg-neutral">四</div>
                    <div class="text-center text-secondary text-sm font-medium py-2 rounded-lg bg-neutral">五</div>
                    <div class="text-center text-secondary text-sm font-medium py-2 rounded-lg bg-neutral">六</div>
                </div>

                <!-- 日历日期容器 -->
                <div class="grid grid-cols-7 gap-1" id="calendar-body">
                    <div class="text-center py-4 text-gray-400">加载中...</div>
                </div>

                <!-- 状态说明 -->
                <div class="mt-6 flex flex-wrap gap-4 text-sm">
                    <div class="flex items-center">
                        <span class="inline-block w-4 h-4 rounded-full punch-status-normal mr-2"></span>
                        <span>正常打卡</span>
                    </div>
                    <div class="flex items-center">
                        <span class="inline-block w-4 h-4 rounded-full punch-status-late mr-2"></span>
                        <span>迟到打卡</span>
                    </div>
                    <div class="flex items-center">
                        <span class="inline-block w-4 h-4 rounded-full punch-status-absent mr-2"></span>
                        <span>缺勤未打</span>
                    </div>
                    <div class="flex items-center">
                        <span class="inline-block w-4 h-4 rounded-full punch-status-holiday mr-2"></span>
                        <span>休息日</span>
                    </div>
                </div>
            </div>
        </section>
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
<!-- 引入首页业务逻辑 -->
<script src="${pageContext.request.contextPath}/static/js/reception/index.js"></script>
</body>
</html>
