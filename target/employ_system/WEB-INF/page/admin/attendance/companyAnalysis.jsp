<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>公司考勤分析 - 员工考勤管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <style>
        .main-content {
            margin-left: 280px;
            padding: 30px;
            min-height: calc(100vh - 70px);
            background: #f5f7fa;
        }

        /* 统计卡片样式 */
        .stats-cards {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            display: flex;
            align-items: center;
            gap: 15px;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .stat-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 4px 15px rgba(0,0,0,0.12);
        }
        .stat-card .icon {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
        }
        .stat-card.primary .icon { background: #e3f2fd; color: #1976d2; }
        .stat-card.success .icon { background: #e8f5e9; color: #388e3c; }
        .stat-card.warning .icon { background: #fff3e0; color: #f57c00; }
        .stat-card.danger .icon { background: #ffebee; color: #d32f2f; }
        .stat-card .info h3 {
            font-size: 28px;
            font-weight: 700;
            margin: 0;
            color: #333;
        }
        .stat-card .info p {
            font-size: 14px;
            color: #666;
            margin: 5px 0 0;
        }

        /* 图表容器样式 */
        .charts-row {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
            margin-bottom: 30px;
        }
        .chart-container {
            background: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }
        .chart-container h4 {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .chart-container h4 i {
            color: #1976d2;
        }
        .chart-wrapper {
            height: 300px;
        }

        /* 月度统计卡片 */
        .month-stats {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 15px;
        }
        .month-stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 10px;
            padding: 20px;
            color: white;
            text-align: center;
        }
        .month-stat-card.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
        .month-stat-card.orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
        .month-stat-card.blue { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
        .month-stat-card h5 {
            font-size: 14px;
            opacity: 0.9;
            margin-bottom: 10px;
        }
        .month-stat-card .value {
            font-size: 32px;
            font-weight: 700;
        }

        @media (max-width: 1200px) {
            .stats-cards { grid-template-columns: repeat(2, 1fr); }
            .charts-row { grid-template-columns: 1fr; }
            .month-stats { grid-template-columns: repeat(2, 1fr); }
        }
    </style>
</head>
<body>
<jsp:include page="../common/header.jsp"></jsp:include>
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="companyAnalysis"/>
</jsp:include>

<div class="main-content">
    <h2><i class="fa fa-building"></i> 公司考勤分析</h2>

    <!-- 统计卡片 -->
    <div class="stats-cards mt-4">
        <div class="stat-card primary">
            <div class="icon"><i class="fa fa-users"></i></div>
            <div class="info">
                <h3 id="totalEmpCount">0</h3>
                <p>总员工数</p>
            </div>
        </div>
        <div class="stat-card success">
            <div class="icon"><i class="fa fa-user-check"></i></div>
            <div class="info">
                <h3 id="activeEmpCount">0</h3>
                <p>在职员工数</p>
            </div>
        </div>
        <div class="stat-card warning">
            <div class="icon"><i class="fa fa-clock-o"></i></div>
            <div class="info">
                <h3 id="todayAttendance">0</h3>
                <p>今日出勤</p>
            </div>
        </div>
        <div class="stat-card danger">
            <div class="icon"><i class="fa fa-file-text-o"></i></div>
            <div class="info">
                <h3 id="pendingLeaveCount">0</h3>
                <p>待审批请假</p>
            </div>
        </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
        <div class="chart-container">
            <h4><i class="fa fa-line-chart"></i> 本月考勤趋势</h4>
            <div class="chart-wrapper">
                <canvas id="monthlyChart"></canvas>
            </div>
        </div>
        <div class="chart-container">
            <h4><i class="fa fa-pie-chart"></i> 今日考勤分布</h4>
            <div class="chart-wrapper">
                <canvas id="pieChart"></canvas>
            </div>
        </div>
    </div>

    <!-- 月度统计 -->
    <div class="chart-container">
        <h4><i class="fa fa-calendar"></i> 本月统计概览</h4>
        <div class="month-stats">
            <div class="month-stat-card">
                <h5>本月总出勤次数</h5>
                <div class="value" id="monthTotalAttendance">0</div>
            </div>
            <div class="month-stat-card green">
                <h5>本月正常出勤</h5>
                <div class="value" id="monthNormalAttendance">0</div>
            </div>
            <div class="month-stat-card orange">
                <h5>本月迟到次数</h5>
                <div class="value" id="monthLateCount">0</div>
            </div>
            <div class="month-stat-card blue">
                <h5>本月请假次数</h5>
                <div class="value" id="monthLeaveCount">0</div>
            </div>
        </div>
    </div>
</div>

<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<script>
let monthlyChartInstance = null;
let pieChartInstance = null;

document.addEventListener('DOMContentLoaded', function() {
    initHeader();
    initSidebar();
    loadAnalysisData();
    loadMonthlyData();
    loadTodayPieData();
});

function loadAnalysisData() {
    ajaxRequest('/admin/analysis/company/data.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const data = result.data;
                document.getElementById('totalEmpCount').textContent = data.totalEmpCount || 0;
                document.getElementById('activeEmpCount').textContent = data.activeEmpCount || 0;
                document.getElementById('pendingLeaveCount').textContent = data.pendingLeaveCount || 0;
            }
        },
        function(error) {
            console.error('加载数据失败', error);
        }
    );
}

function loadMonthlyData() {
    ajaxRequest('/admin/dashboard/monthData.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                initMonthlyChart(result.monthData, result.monthDays);
                calculateMonthStats(result.monthData, result.monthDays);
            }
        },
        function(error) {
            console.error('加载月度数据失败', error);
            initMonthlyChart(null, 30);
        }
    );
}

function loadTodayPieData() {
    ajaxRequest('/admin/dashboard/pieData.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                document.getElementById('todayAttendance').textContent = result.attendanceCount || 0;
                initPieChart(result.attendanceCount, result.lateCount, result.leaveCount);
            }
        },
        function(error) {
            console.error('加载饼图数据失败', error);
            initPieChart(0, 0, 0);
        }
    );
}

function initMonthlyChart(monthData, monthDays) {
    const ctx = document.getElementById('monthlyChart').getContext('2d');
    const labels = Array.from({length: monthDays}, (_, i) => (i+1) + '日');

    let attendanceData = [];
    let lateData = [];

    if (monthData && typeof monthData === 'object') {
        for (let i = 1; i <= monthDays; i++) {
            attendanceData.push(monthData[i] || 0);
            lateData.push(monthData[i + "_late"] || 0);
        }
    } else {
        attendanceData = new Array(monthDays).fill(0);
        lateData = new Array(monthDays).fill(0);
    }

    if (monthlyChartInstance) monthlyChartInstance.destroy();

    monthlyChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '正常出勤',
                    data: attendanceData,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 2
                },
                {
                    label: '迟到',
                    data: lateData,
                    borderColor: '#f59e0b',
                    backgroundColor: 'rgba(245, 158, 11, 0.1)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top' }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
}

function initPieChart(attendance, late, leave) {
    const ctx = document.getElementById('pieChart').getContext('2d');
    const total = attendance + late + leave;

    if (pieChartInstance) pieChartInstance.destroy();

    pieChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: total > 0 ? ['正常出勤', '迟到', '请假'] : ['暂无数据'],
            datasets: [{
                data: total > 0 ? [attendance, late, leave] : [1],
                backgroundColor: total > 0 ? ['#10b981', '#f59e0b', '#ef4444'] : ['#e0e0e0'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function calculateMonthStats(monthData, monthDays) {
    let totalAttendance = 0;
    let totalLate = 0;

    if (monthData) {
        for (let i = 1; i <= monthDays; i++) {
            totalAttendance += (monthData[i] || 0);
            totalLate += (monthData[i + "_late"] || 0);
        }
    }

    document.getElementById('monthTotalAttendance').textContent = totalAttendance + totalLate;
    document.getElementById('monthNormalAttendance').textContent = totalAttendance;
    document.getElementById('monthLateCount').textContent = totalLate;

    // 获取本月请假数据
    ajaxRequest('/admin/analysis/company/data.do', 'GET', {},
        function(result) {
            if (result.code === 200) {
                document.getElementById('monthLeaveCount').textContent = result.data.totalLeaveCount || 0;
            }
        }
    );
}
</script>
</body>
</html>
