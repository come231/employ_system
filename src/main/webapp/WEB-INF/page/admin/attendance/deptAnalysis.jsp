<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>部门考勤分析 - 员工考勤管理系统</title>
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

        /* 部门选择器 */
        .dept-selector {
            background: white;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            display: flex;
            align-items: center;
            gap: 15px;
        }
        .dept-selector label {
            font-weight: 600;
            color: #333;
            white-space: nowrap;
        }
        .dept-selector select {
            flex: 1;
            max-width: 300px;
            padding: 10px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 15px;
            transition: border-color 0.2s;
        }
        .dept-selector select:focus {
            outline: none;
            border-color: #1976d2;
        }

        /* 部门信息头 */
        .dept-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 12px;
            padding: 25px;
            margin-bottom: 25px;
            color: white;
        }
        .dept-header h3 {
            margin: 0 0 10px;
            font-size: 24px;
        }
        .dept-header p {
            margin: 0;
            opacity: 0.9;
        }

        /* 统计卡片 */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 25px;
        }
        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            text-align: center;
            transition: transform 0.2s;
        }
        .stat-card:hover {
            transform: translateY(-3px);
        }
        .stat-card .icon {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            margin: 0 auto 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
        }
        .stat-card.primary .icon { background: #e3f2fd; color: #1976d2; }
        .stat-card.success .icon { background: #e8f5e9; color: #388e3c; }
        .stat-card.warning .icon { background: #fff3e0; color: #f57c00; }
        .stat-card.danger .icon { background: #ffebee; color: #d32f2f; }
        .stat-card .value {
            font-size: 28px;
            font-weight: 700;
            color: #333;
        }
        .stat-card .label {
            font-size: 13px;
            color: #666;
            margin-top: 5px;
        }

        /* 图表区域 */
        .charts-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }
        .chart-card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }
        .chart-card h4 {
            font-size: 15px;
            font-weight: 600;
            color: #333;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .chart-card h4 i {
            color: #1976d2;
        }
        .chart-wrapper {
            height: 250px;
        }

        /* 部门概览列表 */
        .dept-overview {
            background: white;
            border-radius: 12px;
            padding: 20px;
            margin-top: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }
        .dept-overview h4 {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin-bottom: 15px;
        }
        .dept-list-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 15px;
        }
        .dept-list-item {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 15px;
            cursor: pointer;
            transition: all 0.2s;
            border: 2px solid transparent;
        }
        .dept-list-item:hover {
            background: #e3f2fd;
        }
        .dept-list-item.active {
            border-color: #1976d2;
            background: #e3f2fd;
        }
        .dept-list-item h5 {
            margin: 0 0 8px;
            font-size: 14px;
            color: #333;
        }
        .dept-list-item .count {
            font-size: 20px;
            font-weight: 700;
            color: #1976d2;
        }
        .dept-list-item .count span {
            font-size: 12px;
            font-weight: normal;
            color: #666;
        }

        @media (max-width: 1200px) {
            .stats-grid { grid-template-columns: repeat(2, 1fr); }
            .charts-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<jsp:include page="../common/header.jsp"></jsp:include>
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="deptAnalysis"/>
</jsp:include>

<div class="main-content">
    <h2><i class="fa fa-sitemap"></i> 部门考勤分析</h2>

    <!-- 部门选择器 -->
    <div class="dept-selector mt-4">
        <label><i class="fa fa-building"></i> 选择部门:</label>
        <select id="deptSelect" onchange="onDeptChange()">
            <option value="">-- 请选择部门 --</option>
        </select>
    </div>

    <!-- 部门信息头 -->
    <div class="dept-header" id="deptHeader" style="display: none;">
        <h3 id="selectedDeptName">部门名称</h3>
        <p><i class="fa fa-users"></i> 员工人数: <strong id="selectedDeptEmpCount">0</strong> 人</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid" id="statsGrid" style="display: none;">
        <div class="stat-card primary">
            <div class="icon"><i class="fa fa-users"></i></div>
            <div class="value" id="deptEmpCount">0</div>
            <div class="label">部门员工</div>
        </div>
        <div class="stat-card success">
            <div class="icon"><i class="fa fa-check-circle"></i></div>
            <div class="value" id="deptTodayAttendance">0</div>
            <div class="label">今日出勤</div>
        </div>
        <div class="stat-card warning">
            <div class="icon"><i class="fa fa-clock-o"></i></div>
            <div class="value" id="deptTodayLate">0</div>
            <div class="label">今日迟到</div>
        </div>
        <div class="stat-card danger">
            <div class="icon"><i class="fa fa-calendar-times-o"></i></div>
            <div class="value" id="deptTodayLeave">0</div>
            <div class="label">今日请假</div>
        </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid" id="chartsGrid" style="display: none;">
        <div class="chart-card">
            <h4><i class="fa fa-pie-chart"></i> 今日考勤分布</h4>
            <div class="chart-wrapper">
                <canvas id="deptPieChart"></canvas>
            </div>
        </div>
        <div class="chart-card">
            <h4><i class="fa fa-bar-chart"></i> 部门员工统计</h4>
            <div class="chart-wrapper">
                <canvas id="deptBarChart"></canvas>
            </div>
        </div>
    </div>

    <!-- 部门概览 -->
    <div class="dept-overview">
        <h4><i class="fa fa-list"></i> 所有部门概览 (点击查看详情)</h4>
        <div class="dept-list-grid" id="deptListGrid"></div>
    </div>
</div>

<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<script>
let deptData = [];
let pieChartInstance = null;
let barChartInstance = null;

document.addEventListener('DOMContentLoaded', function() {
    initHeader();
    initSidebar();
    loadDeptList();
});

function loadDeptList() {
    ajaxRequest('/admin/analysis/dept/data.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                deptData = result.data || [];
                renderDeptSelect();
                renderDeptOverview();
            }
        },
        function(error) {
            console.error('加载部门数据失败', error);
        }
    );
}

function renderDeptSelect() {
    const select = document.getElementById('deptSelect');
    select.innerHTML = '<option value="">-- 请选择部门 --</option>';
    deptData.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.deptId;
        option.textContent = dept.deptName + ' (' + dept.empCount + '人)';
        select.appendChild(option);
    });
}

function renderDeptOverview() {
    const container = document.getElementById('deptListGrid');
    container.innerHTML = '';
    deptData.forEach(dept => {
        const item = document.createElement('div');
        item.className = 'dept-list-item';
        item.setAttribute('data-dept-id', dept.deptId);
        item.innerHTML = '<h5>' + dept.deptName + '</h5><div class="count">' + dept.empCount + ' <span>人</span></div>';
        item.onclick = function() {
            document.getElementById('deptSelect').value = dept.deptId;
            onDeptChange();
            // 高亮选中项
            document.querySelectorAll('.dept-list-item').forEach(el => el.classList.remove('active'));
            item.classList.add('active');
        };
        container.appendChild(item);
    });
}

function onDeptChange() {
    const deptId = document.getElementById('deptSelect').value;
    if (!deptId) {
        document.getElementById('deptHeader').style.display = 'none';
        document.getElementById('statsGrid').style.display = 'none';
        document.getElementById('chartsGrid').style.display = 'none';
        document.querySelectorAll('.dept-list-item').forEach(el => el.classList.remove('active'));
        return;
    }

    // 高亮对应的概览项
    document.querySelectorAll('.dept-list-item').forEach(el => {
        if (el.getAttribute('data-dept-id') == deptId) {
            el.classList.add('active');
        } else {
            el.classList.remove('active');
        }
    });

    // 找到选中的部门
    const dept = deptData.find(d => d.deptId == deptId);
    if (dept) {
        // 显示部门信息
        document.getElementById('deptHeader').style.display = 'block';
        document.getElementById('statsGrid').style.display = 'grid';
        document.getElementById('chartsGrid').style.display = 'grid';

        document.getElementById('selectedDeptName').textContent = dept.deptName;
        document.getElementById('selectedDeptEmpCount').textContent = dept.empCount;
        document.getElementById('deptEmpCount').textContent = dept.empCount;

        // 模拟今日考勤数据 (实际应调用后端API获取)
        const todayAttendance = Math.floor(dept.empCount * 0.8);
        const todayLate = Math.floor(dept.empCount * 0.1);
        const todayLeave = Math.floor(dept.empCount * 0.1);

        document.getElementById('deptTodayAttendance').textContent = todayAttendance;
        document.getElementById('deptTodayLate').textContent = todayLate;
        document.getElementById('deptTodayLeave').textContent = todayLeave;

        // 渲染图表
        renderPieChart(todayAttendance, todayLate, todayLeave);
        renderBarChart(dept);
    }
}

function renderPieChart(attendance, late, leave) {
    const ctx = document.getElementById('deptPieChart').getContext('2d');
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

function renderBarChart(dept) {
    const ctx = document.getElementById('deptBarChart').getContext('2d');

    if (barChartInstance) barChartInstance.destroy();

    barChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['总人数', '今日出勤', '今日迟到', '今日请假'],
            datasets: [{
                label: dept.deptName,
                data: [
                    dept.empCount,
                    Math.floor(dept.empCount * 0.8),
                    Math.floor(dept.empCount * 0.1),
                    Math.floor(dept.empCount * 0.1)
                ],
                backgroundColor: ['#1976d2', '#10b981', '#f59e0b', '#ef4444'],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
}
</script>
</body>
</html>
