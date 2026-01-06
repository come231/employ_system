/**
 * 仪表盘主内容专属JS - 数据加载/图表渲染/日期初始化
 * 依赖：common.js（ajaxRequest/showMessage/formatDate等工具函数）
 * 依赖：header.js（initHeader）、sidebar.js（initSidebar）
 */

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log("进入页面初始化");
    // 第一步：先进行登录态校验
    checkLogin('/admin/login/current.do').then(isLoggedIn => {
        if (!isLoggedIn) {
            // 未登录，跳转到登录页面
            window.location.href = `${window.baseUrl}/admin/page/login.do`;
            return;
        }

        // 已登录，初始化各模块
        initHeader();                // 1. 初始化顶部标题栏（用户信息）
        initSidebar();               // 2. 初始化侧边栏菜单
        initCurrentDate();           // 3. 初始化当前日期
        getDashboardData();          // 4. 获取仪表盘基础数据
        getMonthAttendanceData();    // 5. 获取月度考勤数据并初始化图表
        getTodayPieData();           // 6. 获取今日考勤饼图数据
        getRecentLogs();             // 7. 获取最近操作日志
        getPendingApprovalCount();   // 8. 获取待审批数量
    });
});

/**
 * 初始化当前日期显示（实时时间）
 */
function initCurrentDate() {
    function updateDate() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const dateStr = `${year}年${month}月${day}日 ${hours}:${minutes}`;
        document.getElementById('currentDate').textContent = dateStr;
    }
    // 初始化一次
    updateDate();
    // 每分钟更新一次
    setInterval(updateDate, 60000);
}

/**
 * 获取仪表盘基础数据（总员工、今日出勤、迟到、请假）
 */
function getDashboardData() {
    ajaxRequest(
        '/admin/dashboard/data.do',
        'GET',
        'result',
        function (result) {
            if (result.code === 200) {
                // 渲染数据到页面
                document.getElementById('totalEmployees').textContent = result.employCount || 0;
                document.getElementById('todayAttendance').textContent = result.successCount || 0;
                document.getElementById('todayLate').textContent = result.laterCount || 0;
                document.getElementById('leaveApplications').textContent = result.leaveCount || 0;
            } else {
                showMessage(result.msg || "获取数据失败！", 'error');
            }
        },
        function(error) {
            showMessage(error.msg || '网络异常，获取数据失败', 'error');
        }
    )
}

/**
 * 获取月度考勤数据并初始化图表
 */
function getMonthAttendanceData() {
    ajaxRequest(
        '/admin/dashboard/monthData.do',
        'GET',
        'result',
        function (result) {
            if (result.code === 200) {
                initAttendanceChart(result.monthData, result.monthDays);
            } else {
                showMessage(result.msg, 'error');
                initAttendanceChart(null,25);
            }
        },
        function(error) {
            showMessage(error.msg || '网络异常，获取数据失败', 'error');
            initAttendanceChart(null,25);
        }
    )
}

/**
 * 初始化考勤统计图表
 * @param {Object} monthData 月度数据对象
 * @param {Number} monthDays 本月天数
 */
function initAttendanceChart(monthData, monthDays) {
    const ctx = document.getElementById('attendanceChart').getContext('2d');

    // 准备标签（日期）
    const labels = Array.from({length: monthDays}, (_, i) => `${i+1}日`);

    // 准备数据
    let attendanceData = []; // 出勤人数
    let lateData = [];       // 迟到人数

    // 如果有后端数据，使用真实数据；否则使用模拟数据
    if (monthData && typeof monthData === 'object') {
        for (let i = 1; i <= monthDays; i++) {
            attendanceData.push(monthData[i] || 0);
            lateData.push(monthData[i + "_late"] || 0);
        }
    } else {
        // 模拟数据（备用）
        attendanceData = [145, 148, 142, 140, 150, 147, 149, 143, 146, 144,
            151, 148, 145, 142, 147, 149, 146, 144, 148, 145,
            143, 147, 142, 140, 142].slice(0, monthDays);
        lateData = [5, 7, 9, 3, 6, 8, 4, 7, 5, 9,
            4, 6, 8, 7, 5, 3, 6, 8, 5, 7,
            9, 6, 8, 7, 8].slice(0, monthDays);
    }

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '出勤人数',
                    data: attendanceData,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 2
                },
                {
                    label: '迟到人数',
                    data: lateData,
                    borderColor: '#f59e0b', // 黄色主题
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
                legend: {
                    position: 'top',
                    labels: {
                        color: '#475569',
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                    titleColor: '#334155',
                    bodyColor: '#475569',
                    borderColor: '#e2e8f0',
                    borderWidth: 1,
                    padding: 12,
                    boxPadding: 6
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    min: 0,
                    title: {
                        display: true,
                        text: '人数',
                        color: '#64748b'
                    },
                    grid: {
                        color: '#f1f5f9'
                    },
                    ticks: {
                        color: '#64748b'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: '日期',
                        color: '#64748b'
                    },
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: '#64748b'
                    }
                }
            }
        }
    });
}

/**
 * 获取今日考勤饼图数据
 */
function getTodayPieData() {
    ajaxRequest(
        '/admin/dashboard/pieData.do',
        'GET',
        'result',
        function (result) {
            if (result.code === 200) {
                initPieChart(result.attendanceCount, result.lateCount, result.leaveCount);
            } else {
                showMessage(result.msg, 'error');
                initPieChart(0, 0, 0);
            }
        },
        function(error) {
            showMessage(error.msg || '网络异常，获取饼图数据失败', 'error');
            initPieChart(0, 0, 0);
        }
    )
}

/**
 * 初始化今日考勤饼图
 */
function initPieChart(attendanceCount, lateCount, leaveCount) {
    const ctx = document.getElementById('attendancePieChart').getContext('2d');

    // 检查是否所有数据都为0
    const total = attendanceCount + lateCount + leaveCount;
    let chartData, chartLabels, chartColors, chartBorderColors;

    if (total === 0) {
        // 没有数据时显示"暂无数据"
        chartLabels = ['暂无考勤数据'];
        chartData = [1];
        chartColors = ['rgba(200, 200, 200, 0.5)'];
        chartBorderColors = ['#ccc'];
    } else {
        chartLabels = ['正常出勤', '迟到', '请假'];
        chartData = [attendanceCount, lateCount, leaveCount];
        chartColors = [
            'rgba(16, 185, 129, 0.8)',  // 绿色 - 出勤
            'rgba(245, 158, 11, 0.8)',  // 黄色 - 迟到
            'rgba(239, 68, 68, 0.8)'    // 红色 - 请假
        ];
        chartBorderColors = ['#10b981', '#f59e0b', '#ef4444'];
    }

    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: chartLabels,
            datasets: [{
                data: chartData,
                backgroundColor: chartColors,
                borderColor: chartBorderColors,
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#475569',
                        font: {
                            size: 11
                        },
                        padding: 10
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                    titleColor: '#334155',
                    bodyColor: '#475569',
                    borderColor: '#e2e8f0',
                    borderWidth: 1,
                    padding: 10,
                    callbacks: {
                        label: function(context) {
                            if (total === 0) {
                                return '今日暂无考勤记录';
                            }
                            let label = context.label || '';
                            let value = context.parsed || 0;
                            let percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                            return label + ': ' + value + '人 (' + percentage + '%)';
                        }
                    }
                }
            }
        }
    });
}

/**
 * 获取最近操作日志
 */
function getRecentLogs() {
    ajaxRequest(
        '/admin/dashboard/recentLogs.do',
        'GET',
        'result',
        function (result) {
            if (result.code === 200) {
                renderRecentLogs(result.logs);
            } else {
                document.getElementById('recentLogs').innerHTML = 
                    '<li class="list-group-item border-0 ps-0 pe-0 text-center text-muted">暂无日志</li>';
            }
        },
        function(error) {
            document.getElementById('recentLogs').innerHTML = 
                '<li class="list-group-item border-0 ps-0 pe-0 text-center text-danger">加载失败</li>';
        }
    )
}

/**
 * 渲染最近操作日志
 */
function renderRecentLogs(logs) {
    const logsContainer = document.getElementById('recentLogs');
    logsContainer.innerHTML = '';

    if (!logs || logs.length === 0) {
        logsContainer.innerHTML = '<li class="list-group-item border-0 ps-0 pe-0 text-center text-muted">暂无日志</li>';
        return;
    }

    logs.forEach(log => {
        const li = document.createElement('li');
        li.className = 'list-group-item border-0 ps-0 pe-0 log-item';
        li.innerHTML = `
            <div class="log-action">${log.operationDesc || log.operationType || '操作'}</div>
            <div class="log-time">${formatDate(log.operationTime, 'YYYY-MM-DD HH:mm')}</div>
            <div class="text-muted fs-7">操作人：${log.username || '系统'}</div>
        `;
        logsContainer.appendChild(li);
    });
}

/**
 * 获取待审批数量（请假+补卡）
 */
function getPendingApprovalCount() {
    ajaxRequest(
        '/admin/dashboard/pendingCount.do',
        'GET',
        'result',
        function (result) {
            if (result.code === 200) {
                document.getElementById('pendingLeaveCount').textContent = result.pendingLeaveCount || 0;
                document.getElementById('pendingSupplementCount').textContent = result.pendingSupplementCount || 0;
                // 主管待审批数量
                document.getElementById('managerPendingLeaveCount').textContent = result.managerPendingLeaveCount || 0;
                document.getElementById('managerPendingSupplementCount').textContent = result.managerPendingSupplementCount || 0;
            }
        },
        function(error) {
            console.error('获取待审批数量失败', error);
        }
    );
}