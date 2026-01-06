/**
 * 前台首页脚本
 * @description 员工首页功能模块，包含打卡、日历、排行榜、请假/补卡申请等功能
 * @author 开发团队
 * @version 1.0.0
 * @dependency ReceptionApp (common.js)
 */

(function() {
    'use strict';

    // ============================================================
    // 全局变量
    // ============================================================

    /** ReceptionApp工具库引用 */
    const app = window.ReceptionApp;

    /** 当前日历显示的年月 */
    let currentCalendarDate = new Date();

    // ============================================================
    // 初始化入口
    // ============================================================

    /**
     * 页面加载完成后初始化
     * @description 等待用户信息加载完成后初始化页面各模块
     */
    document.addEventListener('DOMContentLoaded', function() {
        // 监听用户信息加载完成事件（由common.js触发）
        document.addEventListener('userLoaded', function(e) {
            initPage(e.detail);
        });

        // 如果用户信息已经加载完成，直接初始化
        if (app.currentUser) {
            initPage(app.currentUser);
        }
    });

    /**
     * 初始化页面
     * @description 初始化所有页面模块
     * @param {Object} user - 当前登录用户信息
     */
    function initPage(user) {
        // 渲染导航栏（传入当前页面标识）
        app.renderNavbar(user, 'index');

        // 设置欢迎信息
        setWelcomeInfo(user);

        // 设置当前日期显示
        setCurrentDate();

        // 检查今日打卡状态
        checkTodayPunchStatus();

        // 加载首页汇总数据（考勤率、剩余年假等）
        loadSummaryData();

        // 加载考勤排名数据
        loadRankingData('month');

        // 渲染考勤日历
        renderCalendar(currentCalendarDate);

        // 绑定页面事件
        bindEvents();

        // 启动打卡倒计时
        startCountdown();
    }

    // ============================================================
    // 打卡状态检查
    // ============================================================

    /**
     * 检查今日打卡状态
     * @description 从后端获取今日打卡状态，更新页面UI
     */
    function checkTodayPunchStatus() {
        app.get('/reception/home/punch/status.do', function(result) {
            if (result.data) {
                const data = result.data;
                const punchBtn = document.getElementById('punchBtn');
                const punchStatus = document.getElementById('punch-status');

                // 构建打卡时间提示文本
                const workTimeText = '今日打卡时间：' + (data.workStartTime || '09:00') + ' - ' + (data.workEndTime || '18:00');

                if (data.punched) {
                    // 状态1：已完成打卡
                    updatePunchButtonToPunched(punchBtn, data.checkTime);
                    updatePunchStatusText(punchStatus, workTimeText, data.status, data.statusText);
                } else if (data.onLeave === true) {
                    // 状态2：今日已请假，无需打卡
                    updatePunchButtonToLeave(punchBtn);
                    if (punchStatus) {
                        punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-primary">已请假（无需打卡）</span></p>';
                    }
                } else if (data.canPunch === false) {
                    // 状态3：已超过打卡时间（缺勤）
                    updatePunchButtonToAbsent(punchBtn);
                    if (punchStatus) {
                        punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-danger">缺勤（已超过打卡时间，请申请补卡）</span></p>';
                    }
                } else if (data.status === 1) {
                    // 状态4：迟到中但仍可打卡
                    updatePunchButtonToLate(punchBtn, data.message);
                    if (punchStatus) {
                        punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-warning">' + (data.message || '迟到中') + '</span></p>';
                    }
                } else {
                    // 状态5：正常未打卡状态
                    if (punchStatus) {
                        punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-warning">未打卡</span></p>';
                    }
                }
            }
        });
    }

    /**
     * 更新打卡按钮为"已打卡"状态
     * @param {HTMLElement} punchBtn - 打卡按钮元素
     * @param {string} checkTime - 打卡时间
     */
    function updatePunchButtonToPunched(punchBtn, checkTime) {
        if (!punchBtn) return;
        punchBtn.disabled = true;
        punchBtn.classList.remove('bg-success', 'hover:bg-success/90');
        punchBtn.classList.add('bg-gray-400', 'cursor-not-allowed');
        punchBtn.innerHTML =
            '<i class="fa-solid fa-check-circle text-3xl mb-3"></i>' +
            '<span class="text-xl font-bold">已打卡</span>' +
            '<span class="text-sm mt-2">' + (checkTime || '') + '</span>';
    }

    /**
     * 更新打卡按钮为"已请假"状态
     * @param {HTMLElement} punchBtn - 打卡按钮元素
     */
    function updatePunchButtonToLeave(punchBtn) {
        if (!punchBtn) return;
        punchBtn.disabled = true;
        punchBtn.classList.remove('bg-success', 'hover:bg-success/90');
        punchBtn.classList.add('bg-blue-400', 'cursor-not-allowed');
        punchBtn.innerHTML =
            '<i class="fa-solid fa-calendar-check text-3xl mb-3"></i>' +
            '<span class="text-xl font-bold">已请假</span>' +
            '<span class="text-sm mt-2">今日无需打卡</span>';
    }

    /**
     * 更新打卡按钮为"缺勤"状态
     * @param {HTMLElement} punchBtn - 打卡按钮元素
     */
    function updatePunchButtonToAbsent(punchBtn) {
        if (!punchBtn) return;
        punchBtn.disabled = true;
        punchBtn.classList.remove('bg-success', 'hover:bg-success/90');
        punchBtn.classList.add('bg-red-400', 'cursor-not-allowed');
        punchBtn.innerHTML =
            '<i class="fa-solid fa-times-circle text-3xl mb-3"></i>' +
            '<span class="text-xl font-bold">缺勤</span>' +
            '<span class="text-sm mt-2">请申请补卡</span>';
    }

    /**
     * 更新打卡按钮为"迟到"状态（仍可打卡）
     * @param {HTMLElement} punchBtn - 打卡按钮元素
     * @param {string} message - 提示信息
     */
    function updatePunchButtonToLate(punchBtn, message) {
        if (!punchBtn) return;
        punchBtn.classList.remove('bg-success');
        punchBtn.classList.add('bg-warning');
        punchBtn.innerHTML =
            '<i class="fa-solid fa-clock text-3xl mb-3"></i>' +
            '<span class="text-xl font-bold">迟到打卡</span>' +
            '<span class="text-sm mt-2">' + (message || '') + '</span>';
    }

    /**
     * 更新打卡状态文本
     * @param {HTMLElement} punchStatus - 状态显示元素
     * @param {string} workTimeText - 工作时间文本
     * @param {number} status - 打卡状态（0=正常，1=迟到）
     * @param {string} statusText - 状态文本
     */
    function updatePunchStatusText(punchStatus, workTimeText, status, statusText) {
        if (!punchStatus) return;
        const text = status === 0 ? '正常打卡' : (status === 1 ? '迟到打卡' : statusText);
        punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-success">' + text + '</span></p>';
    }

    // ============================================================
    // 页面信息显示
    // ============================================================

    /**
     * 设置欢迎信息
     * @description 显示用户名和问候语
     * @param {Object} user - 用户信息
     */
    function setWelcomeInfo(user) {
        const welcomeTitle = document.getElementById('welcome-title');
        if (welcomeTitle && user) {
            welcomeTitle.innerHTML = '您好，' + user.empName + ' <span class="text-primary">👋</span>';
        }
    }

    /**
     * 设置当前日期
     * @description 显示当前日期（中文格式）
     */
    function setCurrentDate() {
        const now = new Date();
        const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
        const currentDateEl = document.getElementById('currentDate');
        if (currentDateEl) {
            currentDateEl.textContent = now.toLocaleDateString('zh-CN', options);
        }
    }

    // ============================================================
    // 数据加载
    // ============================================================

    /**
     * 加载首页汇总数据
     * @description 加载考勤率、剩余年假、补卡次数、审批时长等统计数据
     */
    function loadSummaryData() {
        app.get('/reception/home/summary.do', function(result) {
            if (result.data) {
                const data = result.data;
                console.log("汇总数据："+data.todayStatus);

                // 更新本月考勤率
                updateElementText('attendance-rate', (data.monthlyAttendanceRate || 0) + '%');

                // 更新剩余年假天数
                updateElementText('remain-leave', (data.remainAnnualLeave || 0) + '天');

                // 更新本月已用补卡次数
                updateElementText('used-supplement', (data.usedSupplementCount || 0) + '次');

                // 更新平均审批时长
                updateElementText('avg-approval-time', data.averageApprovalTime || '-');

                // 更新审批通过率
                updateElementText('approval-pass-rate', data.approvalPassRate || '-');

                // 更新今日打卡状态
                updateTodayPunchStatus(data.todayStatus);
            }
        });
    }

    /**
     * 更新元素文本内容
     * @param {string} elementId - 元素ID
     * @param {string} text - 文本内容
     */
    function updateElementText(elementId, text) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = text;
        }
    }

    /**
     * 更新今日打卡状态显示
     * @param {string} status - 打卡状态
     */
    function updateTodayPunchStatus(status) {
        const punchStatus = document.getElementById('punch-status');
        const punchBtn = document.getElementById('punchBtn');
        if (punchStatus) {
            if (status === 'checked_in') {
                punchStatus.innerHTML = '当前状态：<span class="text-success">已打卡</span>';
                if (punchBtn) {
                    punchBtn.classList.remove('bg-success');
                    punchBtn.classList.add('bg-primary');
                    punchBtn.innerHTML =
                        '<i class="fa-solid fa-check-circle text-3xl mb-3"></i>' +
                        '<span class="text-xl font-bold">已打卡</span>' +
                        '<span class="text-sm mt-2">今日已完成打卡</span>';
                }
                return
            } else {
                punchStatus.innerHTML = '当前状态：<span class="text-warning">未打卡</span>';
            }
        }
    }

    // ============================================================
    // 考勤排名
    // ============================================================

    /**
     * 加载考勤排名数据
     * @param {string} period - 时间周期（month=本月）
     */
    function loadRankingData(period) {
        app.get('/reception/home/ranking.do?period=' + period, function(result) {
            if (result.data) {
                console.log("月度员工考勤排名："+result.data);
                // 渲染员工考勤次数排名
                renderEmployeeRanking(result.data.employeeRanking || []);
                // 渲染部门考勤率排名
                renderDeptRanking(result.data.deptRanking || []);
            }
        });
    }

    /**
     * 渲染员工考勤次数排名
     * @param {Array} data - 员工排名数据
     */
    function renderEmployeeRanking(data) {
        const tbody = document.getElementById('employee-ranking-body');
        if (!tbody) return;

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="px-4 py-3 text-center text-gray-500">暂无数据</td></tr>';
            return;
        }

        let html = '';
        data.forEach(function(item, index) {
            // 前三名使用不同的颜色样式
            const rankClass = index === 0 ? 'text-warning' : (index === 1 ? 'text-gray-400' : (index === 2 ? 'text-gray-500' : 'text-gray-600'));
            html +=
                '<tr class="border-b border-gray-100 hover:bg-white/50 transition-colors">' +
                    '<td class="px-4 py-3 font-bold ' + rankClass + '">' + (index + 1) + '</td>' +
                    '<td class="px-4 py-3">' + (item.empName || '') + '</td>' +
                    '<td class="px-4 py-3">' + (item.deptName || '') + '</td>' +
                    '<td class="px-4 py-3 font-medium">' + (item.attendanceCount || 0) + '</td>' +
                '</tr>';
        });
        tbody.innerHTML = html;
    }

    /**
     * 渲染部门考勤率排名
     * @param {Array} data - 部门排名数据
     */
    function renderDeptRanking(data) {
        const tbody = document.getElementById('dept-ranking-body');
        if (!tbody) return;

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="px-4 py-3 text-center text-gray-500">暂无数据</td></tr>';
            return;
        }

        let html = '';
        data.forEach(function(item, index) {
            // 前三名使用不同的颜色样式
            const rankClass = index === 0 ? 'text-warning' : (index === 1 ? 'text-gray-400' : (index === 2 ? 'text-gray-500' : 'text-gray-600'));
            html +=
                '<tr class="border-b border-gray-100 hover:bg-white/50 transition-colors">' +
                    '<td class="px-4 py-3 font-bold ' + rankClass + '">' + (index + 1) + '</td>' +
                    '<td class="px-4 py-3">' + (item.deptName || '') + '</td>' +
                    '<td class="px-4 py-3">' + (item.empCount || 0) + '</td>' +
                    '<td class="px-4 py-3 font-medium text-success">' + (item.attendanceRate || 0) + '%</td>' +
                '</tr>';
        });
        tbody.innerHTML = html;
    }

    // ============================================================
    // 考勤日历
    // ============================================================

    /**
     * 渲染考勤日历
     * @description 根据指定日期渲染当月日历，并加载当月考勤数据
     * @param {Date} date - 要显示的日期
     */
    function renderCalendar(date) {
        const year = date.getFullYear();
        const month = date.getMonth();

        // 更新日历标题
        const calendarTitle = document.getElementById('calendar-title');
        if (calendarTitle) {
            calendarTitle.textContent = year + '年' + (month + 1) + '月';
        }

        // 计算当月日历所需的信息
        const firstDay = new Date(year, month, 1);      // 当月第一天
        const lastDay = new Date(year, month + 1, 0);   // 当月最后一天
        const daysInMonth = lastDay.getDate();          // 当月天数
        const startWeekDay = firstDay.getDay();         // 第一天是星期几（0=周日）

        // 获取上月最后一天的日期（用于填充日历开头空白）
        const prevMonthLastDay = new Date(year, month, 0).getDate();

        // 从后端加载当月考勤数据
        app.get('/reception/home/calendar.do?year=' + year + '&month=' + (month + 1), function(result) {
            const attendanceMap = {};

            // 将考勤数据转换为以日期为key的Map，便于快速查找
            if (result.data && result.data.records && result.data.records.length > 0) {
                result.data.records.forEach(function(item) {
                    // 后端返回的日期字段名为 date
                    attendanceMap[item.date] = item;
                });
            }

            // 渲染日历格子
            renderCalendarDays(year, month, daysInMonth, startWeekDay, prevMonthLastDay, attendanceMap);
        }, function() {
            // 请求失败时也渲染日历（无考勤数据）
            renderCalendarDays(year, month, daysInMonth, startWeekDay, prevMonthLastDay, {});
        });
    }

    /**
     * 渲染日历日期格子
     * @param {number} year - 年份
     * @param {number} month - 月份（0-11）
     * @param {number} daysInMonth - 当月天数
     * @param {number} startWeekDay - 当月第一天是星期几
     * @param {number} prevMonthLastDay - 上月最后一天日期
     * @param {Object} attendanceMap - 考勤数据Map
     */
    function renderCalendarDays(year, month, daysInMonth, startWeekDay, prevMonthLastDay, attendanceMap) {
        const calendarBody = document.getElementById('calendar-body');
        if (!calendarBody) return;

        const today = new Date();
        const todayStr = app.formatDate(today, 'yyyy-MM-dd');

        let html = '';

        // 填充上月日期（灰色显示）
        for (let i = startWeekDay - 1; i >= 0; i--) {
            html += '<div class="calendar-day text-gray-300">' + (prevMonthLastDay - i) + '</div>';
        }

        // 渲染当月日期
        for (let day = 1; day <= daysInMonth; day++) {
            const dateStr = app.formatDate(new Date(year, month, day), 'yyyy-MM-dd');
            const isToday = dateStr === todayStr;
            const dayOfWeek = (startWeekDay + day - 1) % 7;
            const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;

            // 获取当天考勤记录
            const attendance = attendanceMap[dateStr];
            let statusClass = '';
            let statusText = '';

            if (isToday) {
                // 今日高亮显示
                statusClass = 'bg-primary/10 border-2 border-primary';
                statusText = '今日';
            } else if (attendance) {
                // 根据考勤状态设置样式
                switch (attendance.status) {
                    case 0:
                        statusClass = 'punch-status-normal';
                        statusText = '正常';
                        break;
                    case 1:
                        statusClass = 'punch-status-late';
                        statusText = '迟到';
                        break;
                    case 2:
                        statusClass = 'punch-status-absent';
                        statusText = '早退';
                        break;
                    case 3:
                        statusClass = 'punch-status-absent';
                        statusText = '缺勤';
                        break;
                    default:
                        statusClass = 'punch-status-normal';
                        statusText = '正常';
                }
            } else if (isWeekend && new Date(year, month, day) <= today) {
                // 已过去的周末显示为休息日
                statusClass = 'punch-status-holiday';
                statusText = '休';
            }

            html +=
                '<div class="calendar-day ' + statusClass + '">' +
                    day +
                    (statusText ? '<span class="text-xs mt-1">' + statusText + '</span>' : '') +
                '</div>';
        }

        // 填充下月日期（灰色显示）
        const totalCells = startWeekDay + daysInMonth;
        const remainingCells = (7 - totalCells % 7) % 7;
        for (let i = 1; i <= remainingCells; i++) {
            html += '<div class="calendar-day text-gray-300">' + i + '</div>';
        }

        calendarBody.innerHTML = html;
    }

    // ============================================================
    // 事件绑定
    // ============================================================

    /**
     * 绑定页面事件
     * @description 绑定日历切换、打卡、请假申请、补卡申请等按钮事件
     */
    function bindEvents() {
        // 日历上一月按钮
        const prevMonthBtn = document.getElementById('prev-month-btn');
        if (prevMonthBtn) {
            prevMonthBtn.addEventListener('click', function() {
                currentCalendarDate.setMonth(currentCalendarDate.getMonth() - 1);
                renderCalendar(currentCalendarDate);
            });
        }

        // 日历下一月按钮
        const nextMonthBtn = document.getElementById('next-month-btn');
        if (nextMonthBtn) {
            nextMonthBtn.addEventListener('click', function() {
                currentCalendarDate.setMonth(currentCalendarDate.getMonth() + 1);
                renderCalendar(currentCalendarDate);
            });
        }

        // 请假申请按钮
        const leaveApplyBtn = document.getElementById('leave-apply-btn');
        if (leaveApplyBtn) {
            leaveApplyBtn.addEventListener('click', showLeaveModal);
        }

        // 补卡申请按钮
        const supplementApplyBtn = document.getElementById('supplement-apply-btn');
        if (supplementApplyBtn) {
            supplementApplyBtn.addEventListener('click', showSupplementModal);
        }

        // 打卡按钮
        const punchBtn = document.getElementById('punchBtn');
        if (punchBtn) {
            punchBtn.addEventListener('click', doPunch);
        }

        // 导出按钮
        const exportBtn = document.getElementById('export-btn');
        if (exportBtn) {
            exportBtn.addEventListener('click', function() {
                app.showToast('功能开发中...', 'info');
            });
        }
    }

    // ============================================================
    // 打卡功能
    // ============================================================

    /**
     * 启动打卡倒计时
     * @description 显示距离下班时间（18:00）的倒计时
     */
    function startCountdown() {
        function updateCountdown() {
            const now = new Date();
            // 设置下班时间为当天18:00
            const endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 18, 0, 0);

            // 如果已过下班时间，计算到明天下班的时间
            if (now > endTime) {
                endTime.setDate(endTime.getDate() + 1);
            }

            // 计算时间差
            const diff = endTime - now;
            const hours = Math.floor(diff / (1000 * 60 * 60));
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((diff % (1000 * 60)) / 1000);

            // 格式化显示
            const formattedTime = String(hours).padStart(2, '0') + ':' +
                                  String(minutes).padStart(2, '0') + ':' +
                                  String(seconds).padStart(2, '0');

            const countdownEl = document.getElementById('countdown');
            if (countdownEl) {
                countdownEl.textContent = formattedTime;
            }
        }

        // 立即更新一次
        updateCountdown();
        // 每秒更新
        setInterval(updateCountdown, 1000);
    }

    /**
     * 执行打卡操作
     * @description 确认后调用打卡API，更新页面状态
     */
    function doPunch() {
        const punchBtn = document.getElementById('punchBtn');

        // 检查按钮是否已禁用
        if (punchBtn && punchBtn.disabled) {
            if (punchBtn.classList.contains('bg-red-400')) {
                app.showToast('已超过打卡时间，请申请补卡！', 'warning');
            } else {
                app.showToast('今日已打卡！', 'info');
            }
            return;
        }

        // 弹出确认对话框
        app.showConfirm('确认打卡', '确定要进行打卡操作吗？', function() {
            // 调用打卡API
            app.post('/reception/home/punch.do', {}, function(result) {
                if (result.data) {
                    const data = result.data;

                    // 更新打卡按钮为已打卡状态
                    if (punchBtn) {
                        punchBtn.disabled = true;
                        punchBtn.classList.remove('bg-success', 'hover:bg-success/90', 'bg-warning');
                        punchBtn.classList.add('bg-gray-400', 'cursor-not-allowed');
                        punchBtn.innerHTML =
                            '<i class="fa-solid fa-check-circle text-3xl mb-3"></i>' +
                            '<span class="text-xl font-bold">已打卡</span>' +
                            '<span class="text-sm mt-2">' + (data.checkTime || '') + '</span>';
                    }

                    // 更新状态文本
                    const punchStatus = document.getElementById('punch-status');
                    if (punchStatus) {
                        const statusText = data.status === 0 ? '正常打卡' : '迟到打卡';
                        punchStatus.innerHTML = '<p>当前状态：<span class="text-success">' + statusText + '</span></p>';
                    }

                    app.showToast(data.message || '打卡成功！', 'success');

                    // 刷新日历显示最新打卡记录
                    renderCalendar(currentCalendarDate);
                }
            }, function(error) {
                // 处理超时错误
                if (error && typeof error === 'string' && error.includes('超过打卡时间')) {
                    if (punchBtn) {
                        punchBtn.disabled = true;
                        punchBtn.classList.remove('bg-success', 'hover:bg-success/90', 'bg-warning');
                        punchBtn.classList.add('bg-red-400', 'cursor-not-allowed');
                        punchBtn.innerHTML =
                            '<i class="fa-solid fa-times-circle text-3xl mb-3"></i>' +
                            '<span class="text-xl font-bold">缺勤</span>' +
                            '<span class="text-sm mt-2">请申请补卡</span>';
                    }
                }
                app.showToast((error && error.msg) || '打卡失败，请稍后重试', 'error');
            });
        });
    }

    // ============================================================
    // 请假申请模态框
    // ============================================================

    /**
     * 显示请假申请模态框
     */
    function showLeaveModal() {
        // 移除已存在的模态框
        const existingModal = document.getElementById('leave-modal');
        if (existingModal) {
            existingModal.remove();
        }

        // 创建模态框DOM
        const modal = document.createElement('div');
        modal.id = 'leave-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML =
            '<div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">' +
                '<div class="p-6 border-b border-gray-100 flex justify-between items-center">' +
                    '<h3 class="text-lg font-bold text-gray-800">' +
                        '<i class="fa-solid fa-calendar-times text-primary mr-2"></i>请假申请' +
                    '</h3>' +
                    '<button onclick="document.getElementById(\'leave-modal\').remove()" class="text-gray-400 hover:text-gray-600">' +
                        '<i class="fa-solid fa-times"></i>' +
                    '</button>' +
                '</div>' +
                '<div class="p-6 space-y-4">' +
                    '<div>' +
                        '<label class="block text-sm font-medium text-gray-700 mb-1">请假类型 <span class="text-red-500">*</span></label>' +
                        '<select id="leave-type" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">' +
                            '<option value="">请选择请假类型</option>' +
                            '<option value="年假">年假</option>' +
                            '<option value="病假">病假</option>' +
                            '<option value="事假">事假</option>' +
                            '<option value="婚假">婚假</option>' +
                            '<option value="产假">产假</option>' +
                            '<option value="丧假">丧假</option>' +
                            '<option value="其他">其他</option>' +
                        '</select>' +
                    '</div>' +
                    '<div class="grid grid-cols-2 gap-4">' +
                        '<div>' +
                            '<label class="block text-sm font-medium text-gray-700 mb-1">开始时间 <span class="text-red-500">*</span></label>' +
                            '<input type="datetime-local" id="leave-start" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">' +
                        '</div>' +
                        '<div>' +
                            '<label class="block text-sm font-medium text-gray-700 mb-1">结束时间 <span class="text-red-500">*</span></label>' +
                            '<input type="datetime-local" id="leave-end" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">' +
                        '</div>' +
                    '</div>' +
                    '<div>' +
                        '<label class="block text-sm font-medium text-gray-700 mb-1">请假天数</label>' +
                        '<input type="number" id="leave-days" class="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50" readonly placeholder="自动计算">' +
                    '</div>' +
                    '<div>' +
                        '<label class="block text-sm font-medium text-gray-700 mb-1">请假原因 <span class="text-red-500">*</span></label>' +
                        '<textarea id="leave-reason" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入请假原因"></textarea>' +
                    '</div>' +
                '</div>' +
                '<div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">' +
                    '<button onclick="document.getElementById(\'leave-modal\').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>' +
                    '<button id="leave-submit-btn" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">提交申请</button>' +
                '</div>' +
            '</div>';

        document.body.appendChild(modal);

        // 绑定日期变化事件，自动计算请假天数
        const startInput = document.getElementById('leave-start');
        const endInput = document.getElementById('leave-end');
        const daysInput = document.getElementById('leave-days');

        function calculateDays() {
            if (startInput.value && endInput.value) {
                const start = new Date(startInput.value);
                const end = new Date(endInput.value);
                const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
                daysInput.value = diff > 0 ? diff : 0;
            }
        }

        startInput.addEventListener('change', calculateDays);
        endInput.addEventListener('change', calculateDays);

        // 绑定提交按钮事件
        document.getElementById('leave-submit-btn').addEventListener('click', submitLeaveApplication);

        // 点击背景关闭模态框
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });
    }

    /**
     * 提交请假申请
     */
    function submitLeaveApplication() {
        const leaveType = document.getElementById('leave-type').value;
        const startTime = document.getElementById('leave-start').value;
        const endTime = document.getElementById('leave-end').value;
        const leaveDays = document.getElementById('leave-days').value;
        const reason = document.getElementById('leave-reason').value.trim();

        // 表单验证
        if (!leaveType) {
            app.showToast('请选择请假类型', 'warning');
            return;
        }
        if (!startTime) {
            app.showToast('请选择开始时间', 'warning');
            return;
        }
        if (!endTime) {
            app.showToast('请选择结束时间', 'warning');
            return;
        }
        if (!reason) {
            app.showToast('请输入请假原因', 'warning');
            return;
        }

        // 提交请假申请
        app.post('/reception/home/leave/apply.do', {
            leaveType: leaveType,
            startTime: startTime,
            endTime: endTime,
            leaveDays: leaveDays || 1,
            reason: reason
        }, function() {
            app.showToast('请假申请提交成功！', 'success');
            document.getElementById('leave-modal').remove();
        });
    }

    // ============================================================
    // 补卡申请模态框
    // ============================================================

    /**
     * 显示补卡申请模态框
     */
    function showSupplementModal() {
        // 移除已存在的模态框
        const existingModal = document.getElementById('supplement-modal');
        if (existingModal) {
            existingModal.remove();
        }

        // 创建模态框DOM
        const modal = document.createElement('div');
        modal.id = 'supplement-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML =
            '<div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4">' +
                '<div class="p-6 border-b border-gray-100 flex justify-between items-center">' +
                    '<h3 class="text-lg font-bold text-gray-800">' +
                        '<i class="fa-solid fa-id-card text-primary mr-2"></i>补卡申请' +
                    '</h3>' +
                    '<button onclick="document.getElementById(\'supplement-modal\').remove()" class="text-gray-400 hover:text-gray-600">' +
                        '<i class="fa-solid fa-times"></i>' +
                    '</button>' +
                '</div>' +
                '<div class="p-6 space-y-4">' +
                    '<div>' +
                        '<label class="block text-sm font-medium text-gray-700 mb-1">补卡日期 <span class="text-red-500">*</span></label>' +
                        '<input type="date" id="supplement-date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">' +
                    '</div>' +
                    '<div>' +
                        '<label class="block text-sm font-medium text-gray-700 mb-1">补卡类型 <span class="text-red-500">*</span></label>' +
                        '<select id="supplement-type" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">' +
                            '<option value="">请选择补卡类型</option>' +
                            '<option value="1">上班打卡</option>' +
                        '</select>' +
                    '</div>' +
                    '<div>' +
                        '<label class="block text-sm font-medium text-gray-700 mb-1">实际到岗时间 <span class="text-red-500">*</span></label>' +
                        '<input type="time" id="supplement-time" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">' +
                    '</div>' +
                    '<div>' +
                        '<label class="block text-sm font-medium text-gray-700 mb-1">补卡原因 <span class="text-red-500">*</span></label>' +
                        '<textarea id="supplement-reason" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入补卡原因（如：忘记打卡、设备故障等）"></textarea>' +
                    '</div>' +
                '</div>' +
                '<div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">' +
                    '<button onclick="document.getElementById(\'supplement-modal\').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>' +
                    '<button id="supplement-submit-btn" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">提交申请</button>' +
                '</div>' +
            '</div>';

        document.body.appendChild(modal);

        // 绑定提交按钮事件
        document.getElementById('supplement-submit-btn').addEventListener('click', submitSupplementApplication);

        // 点击背景关闭模态框
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });
    }

    /**
     * 提交补卡申请
     */
    function submitSupplementApplication() {
        const checkDate = document.getElementById('supplement-date').value;
        const checkType = document.getElementById('supplement-type').value;
        const actualTime = document.getElementById('supplement-time').value;
        const reason = document.getElementById('supplement-reason').value.trim();

        // 表单验证
        if (!checkDate) {
            app.showToast('请选择补卡日期', 'warning');
            return;
        }
        if (!checkType) {
            app.showToast('请选择补卡类型', 'warning');
            return;
        }
        if (!actualTime) {
            app.showToast('请输入实际到岗时间', 'warning');
            return;
        }
        if (!reason) {
            app.showToast('请输入补卡原因', 'warning');
            return;
        }

        // 提交补卡申请
        app.post('/reception/home/supplement/apply.do', {
            checkDate: checkDate,
            checkType: checkType,
            actualTime: actualTime + ':00',
            reason: reason
        }, function() {
            app.showToast('补卡申请提交成功！', 'success');
            document.getElementById('supplement-modal').remove();
        });
    }

})();
