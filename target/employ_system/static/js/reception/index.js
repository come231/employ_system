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
    const app = window.ReceptionApp;
    let currentCalendarDate = new Date();
    let currentPunchType = 1; // 1=上班 0=下班
    let currentPunchName = "上班";

    // 从接口获取的考勤规则
    let attendanceRule = {
        workStartTime: "09:00",
        workEndTime: "18:00",
        lateThreshold: 15,
        earlyThreshold: 15
    };

    // ============================================================
    // 初始化入口
    // ============================================================
    document.addEventListener('DOMContentLoaded', function() {
        document.addEventListener('userLoaded', function(e) {
            initPage(e.detail);
        });
        if (app.currentUser) {
            initPage(app.currentUser);
        }
    });

    function initPage(user) {
        app.renderNavbar(user, 'index');
        setWelcomeInfo(user);
        setCurrentDate();
        checkTodayPunchStatus();
        loadSummaryData();
        loadRankingData('month');
        renderCalendar(currentCalendarDate);
        bindEvents();
        startCountdown();
    }

    // ============================================================
    // 打卡状态检查 + 加载考勤规则（从接口获取）
    // ============================================================
    function checkTodayPunchStatus() {
        app.get('/reception/home/punch/status.do', function(result) {
            if (result.data) {
                const data = result.data;

                // 加载接口返回的考勤规则
                if (data.workStartTime) attendanceRule.workStartTime = data.workStartTime;
                if (data.workEndTime) attendanceRule.workEndTime = data.workEndTime;
                if (data.lateThreshold || data.lateThreshold === 0) attendanceRule.lateThreshold = data.lateThreshold;
                if (data.earlyThreshold || data.earlyThreshold === 0) attendanceRule.earlyThreshold = data.earlyThreshold;

                // 从接口获取当前打卡类型
                currentPunchType = data.currentPunchType;
                currentPunchName = data.currentPunchTypeName;

                const punchBtn = document.getElementById('punchBtn');
                const punchStatus = document.getElementById('punch-status');
                const workTimeText = '今日打卡时间：' + attendanceRule.workStartTime + ' - ' + attendanceRule.workEndTime;

                if (data.punched) {
                    updatePunchButtonToPunched(punchBtn, data.checkTime);
                    updatePunchStatusText(punchStatus, workTimeText, data.status, data.statusText);
                } else if (data.onLeave === true) {
                    updatePunchButtonToLeave(punchBtn);
                    punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-primary">已请假（无需打卡）</span></p>';
                } else if (data.canPunch === false) {
                    updatePunchButtonToAbsent(punchBtn);
                    punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-danger">缺勤（已超过打卡时间，请申请补卡）</span></p>';
                } else if (data.status === 1) {
                    updatePunchButtonToLate(punchBtn, data.message);
                    punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-warning">' + (data.message || '迟到中') + '</span></p>';
                } else {
                    punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-warning">未打卡（' + currentPunchName + '）</span></p>';
                }
            }
        });
    }

    /**
     * 已打卡：自动显示 上班/下班
     */
    function updatePunchButtonToPunched(punchBtn, checkTime) {
        if (!punchBtn) return;
        punchBtn.disabled = true;
        punchBtn.classList.remove('bg-success', 'hover:bg-success/90', 'bg-warning');
        punchBtn.classList.add('bg-gray-400', 'cursor-not-allowed');
        punchBtn.innerHTML =
            '<i class="fa-solid fa-check-circle text-3xl mb-3"></i>' +
            '<span class="text-xl font-bold">' + currentPunchName + '已打卡</span>' +
            '<span class="text-sm mt-2">' + (checkTime || '') + '</span>';
    }

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

    function updatePunchButtonToLate(punchBtn, message) {
        if (!punchBtn) return;
        punchBtn.classList.remove('bg-success');
        punchBtn.classList.add('bg-warning');
        punchBtn.innerHTML =
            '<i class="fa-solid fa-clock text-3xl mb-3"></i>' +
            '<span class="text-xl font-bold">' + currentPunchName + '迟到打卡</span>' +
            '<span class="text-sm mt-2">' + (message || '') + '</span>';
    }

    function updatePunchStatusText(punchStatus, workTimeText, status, statusText) {
        if (!punchStatus) return;
        const text = status === 0 ? currentPunchName+'正常打卡' : (status === 1 ? currentPunchName+'迟到打卡' : statusText);
        punchStatus.innerHTML = '<p>' + workTimeText + ' | 当前状态：<span class="text-success">' + text + '</span></p>';
    }

    // ============================================================
    // 页面基础方法（不变）
    // ============================================================
    function setWelcomeInfo(user) {
        const welcomeTitle = document.getElementById('welcome-title');
        if (welcomeTitle && user) {
            welcomeTitle.innerHTML = '您好，' + user.empName + ' <span class="text-primary">👋</span>';
        }
    }

    function setCurrentDate() {
        const now = new Date();
        const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
        const currentDateEl = document.getElementById('currentDate');
        if (currentDateEl) {
            currentDateEl.textContent = now.toLocaleDateString('zh-CN', options);
        }
    }

    function loadSummaryData() {
        app.get('/reception/home/summary.do', function(result) {
            if (result.data) {
                const data = result.data;
                updateElementText('attendance-rate', (data.monthlyAttendanceRate || 0) + '%');
                updateElementText('remain-leave', (data.remainAnnualLeave || 0) + '天');
                updateElementText('used-supplement', (data.usedSupplementCount || 0) + '次');
                updateElementText('avg-approval-time', data.averageApprovalTime || '-');
                updateElementText('approval-pass-rate', data.approvalPassRate || '-');
            }
        });
    }

    function updateElementText(elementId, text) {
        const el = document.getElementById(elementId);
        if (el) el.textContent = text;
    }

    // ============================================================
    // 排名、日历（不变）
    // ============================================================
    function loadRankingData(period) {
        app.get('/reception/home/ranking.do?period=' + period, function(result) {
            if (result.data) {
                renderEmployeeRanking(result.data.employeeRanking || []);
                renderDeptRanking(result.data.deptRanking || []);
            }
        });
    }

    function renderEmployeeRanking(data) {
        const tbody = document.getElementById('employee-ranking-body');
        if (!tbody) return;
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="px-4 py-3 text-center text-gray-500">暂无数据</td></tr>';
            return;
        }
        let html = '';
        data.forEach((item, index) => {
            const rankClass = index === 0 ? 'text-warning' : (index === 1 ? 'text-gray-400' : (index === 2 ? 'text-gray-500' : 'text-gray-600'));
            html += `<tr class="border-b border-gray-100 hover:bg-white/50 transition-colors">
                <td class="px-4 py-3 font-bold ${rankClass}">${index+1}</td>
                <td class="px-4 py-3">${item.empName||''}</td>
                <td class="px-4 py-3">${item.deptName||''}</td>
                <td class="px-4 py-3 font-medium">${item.attendanceCount||0}</td></tr>`;
        });
        tbody.innerHTML = html;
    }

    function renderDeptRanking(data) {
        const tbody = document.getElementById('dept-ranking-body');
        if (!tbody) return;
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="px-4 py-3 text-center text-gray-500">暂无数据</td></tr>';
            return;
        }
        let html = '';
        data.forEach((item, index) => {
            const rankClass = index === 0 ? 'text-warning' : (index === 1 ? 'text-gray-400' : (index === 2 ? 'text-gray-500' : 'text-gray-600'));
            html += `<tr class="border-b border-gray-100 hover:bg-white/50 transition-colors">
                <td class="px-4 py-3 font-bold ${rankClass}">${index+1}</td>
                <td class="px-4 py-3">${item.deptName||''}</td>
                <td class="px-4 py-3">${item.empCount||0}</td>
                <td class="px-4 py-3 font-medium text-success">${item.attendanceRate||0}%</td></tr>`;
        });
        tbody.innerHTML = html;
    }

    function renderCalendar(date) {
        const year = date.getFullYear();
        const month = date.getMonth();
        const calendarTitle = document.getElementById('calendar-title');
        if (calendarTitle) calendarTitle.textContent = year + '年' + (month + 1) + '月';

        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startWeekDay = firstDay.getDay();
        const prevMonthLastDay = new Date(year, month, 0).getDate();

        app.get('/reception/home/calendar.do?year=' + year + '&month=' + (month + 1), function(result) {
            const attendanceMap = {};
            if (result.data && result.data.records) {
                result.data.records.forEach(item => {
                    attendanceMap[item.date] = item;
                });
            }
            renderCalendarDays(year, month, daysInMonth, startWeekDay, prevMonthLastDay, attendanceMap);
        }, () => {
            renderCalendarDays(year, month, daysInMonth, startWeekDay, prevMonthLastDay, {});
        });
    }

    function renderCalendarDays(year, month, daysInMonth, startWeekDay, prevMonthLastDay, attendanceMap) {
        const calendarBody = document.getElementById('calendar-body');
        if (!calendarBody) return;
        const today = new Date();
        const todayStr = app.formatDate(today, 'yyyy-MM-dd');
        let html = '';

        for (let i = startWeekDay - 1; i >= 0; i--) {
            html += '<div class="calendar-day text-gray-300">' + (prevMonthLastDay - i) + '</div>';
        }

        for (let day = 1; day <= daysInMonth; day++) {
            const dateStr = app.formatDate(new Date(year, month, day), 'yyyy-MM-dd');
            const isToday = dateStr === todayStr;
            const dayOfWeek = (startWeekDay + day - 1) % 7;
            const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
            const attendance = attendanceMap[dateStr];
            let statusClass = '';
            let statusText = '';

            if (isToday) {
                statusClass = 'bg-primary/10 border-2 border-primary';
                statusText = '今日';
            } else if (attendance) {
                switch (attendance.status) {
                    case 0: statusClass = 'punch-status-normal'; statusText = '正常'; break;
                    case 1: statusClass = 'punch-status-late'; statusText = '迟到'; break;
                    case 2: statusClass = 'punch-status-absent'; statusText = '早退'; break;
                    case 3: statusClass = 'punch-status-absent'; statusText = '缺勤'; break;
                    default: statusClass = 'punch-status-normal'; statusText = '正常';
                }
            } else if (isWeekend && new Date(year, month, day) <= today) {
                statusClass = 'punch-status-holiday'; statusText = '休';
            }
            html += `<div class="calendar-day ${statusClass}">${day}${statusText ? '<span class="text-xs mt-1">'+statusText+'</span>' : ''}</div>`;
        }

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
    function bindEvents() {
        document.getElementById('prev-month-btn')?.addEventListener('click', () => {
            currentCalendarDate.setMonth(currentCalendarDate.getMonth() - 1);
            renderCalendar(currentCalendarDate);
        });
        document.getElementById('next-month-btn')?.addEventListener('click', () => {
            currentCalendarDate.setMonth(currentCalendarDate.getMonth() + 1);
            renderCalendar(currentCalendarDate);
        });
        document.getElementById('leave-apply-btn')?.addEventListener('click', showLeaveModal);
        document.getElementById('supplement-apply-btn')?.addEventListener('click', showSupplementModal);
        document.getElementById('punchBtn')?.addEventListener('click', doPunch);
        document.getElementById('export-btn')?.addEventListener('click', () => {
            app.showConfirm('导出确认', '确定要导出本月考勤记录表吗？', function() {
                try {
                    // 创建下载链接
                    let link = document.createElement('a');
                    link.href = app.baseUrl + '/reception/home/exportMonthAttendance.do';
                    // 新窗口下载，防止页面跳转
                    link.target = '_blank';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                    app.showToast('导出成功，文件正在下载', 'success');
                } catch (e) {
                    app.showToast('导出失败，请稍后重试', 'error');
                    console.error('导出异常:', e);
                }
            });
        });
    }

    // ============================================================
    // 打卡倒计时（完全根据接口规则动态计算）
    // ============================================================
    function startCountdown() {
        function update() {
            const now = new Date();
            const nowMinutes = now.getHours() * 60 + now.getMinutes();

            // 解析接口规则
            const [wh, wm] = attendanceRule.workStartTime.split(':').map(Number);
            const [eh, em] = attendanceRule.workEndTime.split(':').map(Number);
            const workStart = wh * 60 + wm;
            const workEnd = eh * 60 + em;
            const lateLimit = workStart + attendanceRule.lateThreshold;
            const earlyStart = workEnd - attendanceRule.earlyThreshold;

            let targetTime;
            let tipText = "";

            // ============= 上班打卡 =============
            if (currentPunchType === 1) {
                if (nowMinutes < workStart) {
                    targetTime = new Date(now);
                    targetTime.setHours(wh, wm, 0);
                    tipText = "距上班打卡开始";
                } else if (nowMinutes <= lateLimit) {
                    targetTime = new Date(now);
                    targetTime.setHours(wh, wm + attendanceRule.lateThreshold, 0);
                    tipText = "距上班打卡结束";
                } else {
                    currentPunchType = 0;
                    currentPunchName = "下班";
                    update();
                    return;
                }
            }
            // ============= 下班打卡 =============
            else {
                if (nowMinutes < earlyStart) {
                    targetTime = new Date(now);
                    targetTime.setHours(eh, em - attendanceRule.earlyThreshold, 0);
                    tipText = "距下班打卡开始";
                } else if (nowMinutes <= workEnd) {
                    targetTime = new Date(now);
                    targetTime.setHours(eh, em, 0);
                    tipText = "距下班打卡结束";
                } else {
                    document.getElementById('countdownTip').textContent = "";
                    document.getElementById('countdown').textContent = "今日打卡已结束";
                    return;
                }
            }

            const diff = targetTime - now;
            if (diff <= 0) {
                document.getElementById('countdown').textContent = "打卡已结束";
                return;
            }

            const h = Math.floor(diff / 3600000);
            const m = Math.floor((diff % 3600000) / 60000);
            const s = Math.floor((diff % 60000) / 1000);
            const timeStr = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;

            document.getElementById('countdownTip').textContent = tipText;
            document.getElementById('countdown').textContent = timeStr;
        }
        update();
        setInterval(update, 1000);
    }

    // ============================================================
    // 执行打卡
    // ============================================================
    function doPunch() {
        const punchBtn = document.getElementById('punchBtn');
        if (punchBtn?.disabled) {
            app.showToast(punchBtn.classList.contains('bg-red-400') ? '已超过打卡时间，请申请补卡！' : '今日已打卡！', 'warning');
            return;
        }

        app.showConfirm('确认打卡', `确定要进行【${currentPunchName}】打卡吗？`, function() {
            app.post('/reception/home/punch.do', {punchType: currentPunchType}, function(result) {
                if (result.data) {
                    const data = result.data;
                    punchBtn.disabled = true;
                    punchBtn.classList.remove('bg-success', 'hover:bg-success/90', 'bg-warning');
                    punchBtn.classList.add('bg-gray-400', 'cursor-not-allowed');
                    punchBtn.innerHTML = `<i class="fa-solid fa-check-circle text-3xl mb-3"></i>
                        <span class="text-xl font-bold">${currentPunchName}已打卡</span>
                        <span class="text-sm mt-2">${data.checkTime || ''}</span>`;

                    const punchStatus = document.getElementById('punch-status');
                    const statusText = data.status === 0 ? `${currentPunchName}正常打卡` : `${currentPunchName}迟到打卡`;
                    if (punchStatus) {
                        punchStatus.innerHTML = `<p>当前状态：<span class="text-success">${statusText}</span></p>`;
                    }
                    app.showToast(`${currentPunchName}打卡成功！`, 'success');
                    renderCalendar(currentCalendarDate);
                }
            }, function(err) {
                app.showToast(err?.msg || '打卡失败', 'error');
            });
        });
    }


    // ============================================================
    // 请假、补卡模态框（不变）
    // ============================================================
    function showLeaveModal() {
        document.getElementById('leave-modal')?.remove();
        const modal = document.createElement('div');
        modal.id = 'leave-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
        <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
            <div class="p-6 border-b border-gray-100 flex justify-between items-center">
                <h3 class="text-lg font-bold text-gray-800"><i class="fa-solid fa-calendar-times text-primary mr-2"></i>请假申请</h3>
                <button onclick="document.getElementById('leave-modal').remove()" class="text-gray-400 hover:text-gray-600"><i class="fa-solid fa-times"></i></button>
            </div>
            <div class="p-6 space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">请假类型 <span class="text-red-500">*</span></label>
                    <select id="leave-type" class="w-full px-4 py-2 border border-gray-300 rounded-lg">
                        <option value="">请选择请假类型</option>
                        <option value="年假">年假</option>
                        <option value="病假">病假</option>
                        <option value="事假">事假</option>
                        <option value="婚假">婚假</option>
                        <option value="产假">产假</option>
                        <option value="丧假">丧假</option>
                        <option value="其他">其他</option>
                    </select>
                </div>
                <div class="grid grid-cols-2 gap-4">
                    <div><label class="block text-sm font-medium text-gray-700 mb-1">开始时间 *</label><input type="datetime-local" id="leave-start" class="w-full px-4 py-2 border rounded-lg"></div>
                    <div><label class="block text-sm font-medium text-gray-700 mb-1">结束时间 *</label><input type="datetime-local" id="leave-end" class="w-full px-4 py-2 border rounded-lg"></div>
                </div>
                <div><label class="block text-sm font-medium text-gray-700 mb-1">请假天数</label><input type="number" id="leave-days" class="w-full px-4 py-2 border bg-gray-50" readonly placeholder="自动计算"></div>
                <div><label class="block text-sm font-medium text-gray-700 mb-1">请假原因 *</label><textarea id="leave-reason" rows="3" class="w-full px-4 py-2 border rounded-lg" placeholder="请输入请假原因"></textarea></div>
            </div>
            <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                <button onclick="document.getElementById('leave-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg">取消</button>
                <button id="leave-submit-btn" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg">提交申请</button>
            </div>
        </div>`;
        document.body.appendChild(modal);
        const start = document.getElementById('leave-start');
        const end = document.getElementById('leave-end');
        const days = document.getElementById('leave-days');
        function calc() {
            if (start.value && end.value) {
                days.value = Math.ceil((new Date(end.value) - new Date(start.value)) / 86400000) || 0;
            }
        }
        start.addEventListener('change', calc);
        end.addEventListener('change', calc);
        document.getElementById('leave-submit-btn').addEventListener('click', submitLeaveApplication);
        modal.addEventListener('click', e => e.target === modal && modal.remove());
    }

    function submitLeaveApplication() {
        const type = document.getElementById('leave-type').value;
        const start = document.getElementById('leave-start').value;
        const end = document.getElementById('leave-end').value;
        const days = document.getElementById('leave-days').value || 1;
        const reason = document.getElementById('leave-reason').value.trim();
        if (!type || !start || !end || !reason) {
            app.showToast('请完善必填项', 'warning');
            return;
        }
        app.post('/reception/home/leave/apply.do', {leaveType:type, startTime:start, endTime:end, leaveDays:days, reason:reason}, () => {
            app.showToast('请假申请提交成功！', 'success');
            document.getElementById('leave-modal').remove();
        });
    }

    function showSupplementModal() {
        document.getElementById('supplement-modal')?.remove();
        const modal = document.createElement('div');
        modal.id = 'supplement-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
        <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4">
            <div class="p-6 border-b border-gray-100 flex justify-between items-center">
                <h3 class="text-lg font-bold text-gray-800"><i class="fa-solid fa-id-card text-primary mr-2"></i>补卡申请</h3>
                <button onclick="document.getElementById('supplement-modal').remove()" class="text-gray-400 hover:text-gray-600"><i class="fa-solid fa-times"></i></button>
            </div>
            <div class="p-6 space-y-4">
                <div><label class="block text-sm font-medium text-gray-700 mb-1">补卡日期 *</label><input type="date" id="supplement-date" class="w-full px-4 py-2 border rounded-lg"></div>
                <div><label class="block text-sm font-medium text-gray-700 mb-1">补卡类型 *</label>
                    <select id="supplement-type" class="w-full px-4 py-2 border rounded-lg">
                        <option value="">请选择</option>
                        <option value="1">上班打卡</option>
                        <option value="0">下班打卡</option>
                    </select>
                </div>
                <div><label class="block text-sm font-medium text-gray-700 mb-1">实际到岗时间 *</label><input type="time" id="supplement-time" class="w-full px-4 py-2 border rounded-lg"></div>
                <div><label class="block text-sm font-medium text-gray-700 mb-1">补卡原因 *</label><textarea id="supplement-reason" rows="3" class="w-full px-4 py-2 border rounded-lg" placeholder="请输入补卡原因"></textarea></div>
            </div>
            <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                <button onclick="document.getElementById('supplement-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg">取消</button>
                <button id="supplement-submit-btn" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg">提交申请</button>
            </div>
        </div>`;
        document.body.appendChild(modal);
        document.getElementById('supplement-submit-btn').addEventListener('click', submitSupplementApplication);
        modal.addEventListener('click', e => e.target === modal && modal.remove());
    }

    function submitSupplementApplication() {
        const date = document.getElementById('supplement-date').value;
        const type = document.getElementById('supplement-type').value;
        const time = document.getElementById('supplement-time').value;
        const reason = document.getElementById('supplement-reason').value.trim();
        if (!date || !type || !time || !reason) {
            app.showToast('请完善必填项', 'warning');
            return;
        }
        app.post('/reception/home/supplement/apply.do', {checkDate:date, checkType:type, actualTime:time+':00', reason:reason}, () => {
            app.showToast('补卡申请提交成功！', 'success');
            document.getElementById('supplement-modal').remove();
        });
    }

})();