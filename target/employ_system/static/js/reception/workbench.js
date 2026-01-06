/**
 * 前台工作台脚本（部门主管专用）
 * @description 部门主管工作台功能，包含待审批统计、部门考勤分析、请假/补卡审批等
 * @author 开发团队
 * @version 1.0.0
 * @dependency ReceptionApp (common.js), Chart.js 4.4.0
 * @access 仅部门主管角色可访问
 */

(function() {
    'use strict';

    // ============================================================
    // 全局变量
    // ============================================================

    /** ReceptionApp工具库引用 */
    const app = window.ReceptionApp;

    /**
     * 分页配置对象
     * @description 管理请假和补卡列表的分页状态和筛选条件
     */
    const pageConfig = {
        leave: { page: 1, pageSize: 10, total: 0, status: '' },
        supplement: { page: 1, pageSize: 10, total: 0, status: '' }
    };

    // ============================================================
    // 初始化入口
    // ============================================================

    /**
     * 页面加载完成后初始化
     * @description 监听用户信息加载事件，检查权限后初始化页面
     */
    document.addEventListener('DOMContentLoaded', function() {
        document.addEventListener('userLoaded', function(e) {
            initPage(e.detail);
        });

        if (app.currentUser) {
            initPage(app.currentUser);
        }
    });

    // 初始化页面
    function initPage(user) {
        // 检查是否是主管
        if (!user || user.role !== 'manager') {
            app.showToast('无权访问工作台', 'error');
            setTimeout(function() {
                window.location.href = app.baseUrl + '/reception/page/index.do';
            }, 1500);
            return;
        }

        // 渲染导航栏
        app.renderNavbar(user, 'workbench');

        // 加载待审批数量
        loadPendingCount();

        // 加载部门考勤分析
        loadDeptAnalysis();

        // 加载请假申请列表
        loadLeaveList();

        // 加载补卡申请列表
        loadSupplementList();

        // 绑定事件
        bindEvents();
    }

    // 加载待审批数量
    function loadPendingCount() {
        app.get('/reception/workbench/pending.do', function(result) {
            if (result.data) {
                const pendingLeave = document.getElementById('pending-leave-count');
                const pendingSupplement = document.getElementById('pending-supplement-count');
                const totalPending = document.getElementById('total-pending-count');

                if (pendingLeave) pendingLeave.textContent = result.data.pendingLeaveCount || 0;
                if (pendingSupplement) pendingSupplement.textContent = result.data.pendingSupplementCount || 0;
                if (totalPending) totalPending.textContent = result.data.totalPendingCount || 0;
            }
        });
    }

    // 加载部门考勤分析
    function loadDeptAnalysis() {
        app.get('/reception/workbench/analysis.do', function(result) {
            if (result.data) {
                renderAnalysisCards(result.data);
                renderAnalysisChart(result.data);
            }
        });
    }

    // 渲染分析卡片
    function renderAnalysisCards(data) {
        const totalEmployees = document.getElementById('total-employees');
        const normalCount = document.getElementById('normal-count');
        const lateCount = document.getElementById('late-count');
        const absentCount = document.getElementById('absent-count');
        const leaveCount = document.getElementById('leave-count');
        const attendanceRate = document.getElementById('attendance-rate');

        if (totalEmployees) totalEmployees.textContent = data.totalEmployees || 0;
        if (normalCount) normalCount.textContent = data.normalCount || 0;
        if (lateCount) lateCount.textContent = data.lateCount || 0;
        if (absentCount) absentCount.textContent = data.absentCount || 0;
        if (leaveCount) leaveCount.textContent = data.leaveCount || 0;
        if (attendanceRate) attendanceRate.textContent = (data.attendanceRate || 0) + '%';
    }

    // 渲染分析图表
    function renderAnalysisChart(data) {
        const chartContainer = document.getElementById('attendance-chart');
        if (!chartContainer) return;

        // 检查Chart.js是否加载
        if (typeof Chart === 'undefined') {
            console.warn('Chart.js not loaded');
            return;
        }

        // 销毁已有图表
        const existingChart = Chart.getChart(chartContainer);
        if (existingChart) {
            existingChart.destroy();
        }

        new Chart(chartContainer, {
            type: 'pie',
            data: {
                labels: ['正常', '迟到', '缺勤', '请假'],
                datasets: [{
                    data: [
                        data.normalCount || 0,
                        data.lateCount || 0,
                        data.absentCount || 0,
                        data.leaveCount || 0
                    ],
                    backgroundColor: [
                        '#00B42A',
                        '#FF7D00',
                        '#F53F3F',
                        '#165DFF'
                    ],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    }

    // 加载请假申请列表
    function loadLeaveList() {
        const config = pageConfig.leave;
        let url = `/reception/workbench/leave/data.do?page=${config.page}&pageSize=${config.pageSize}`;
        if (config.status !== '') url += `&status=${config.status}`;

        app.get(url, function(result) {
            config.total = result.count || 0;
            renderLeaveTable(result.data || []);
            renderLeavePagination();
        });
    }

    // 渲染请假表格
    function renderLeaveTable(data) {
        const tbody = document.getElementById('leave-tbody');
        if (!tbody) return;

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="px-4 py-8 text-center text-gray-500">暂无请假申请</td></tr>';
            return;
        }

        let html = '';
        data.forEach(function(item) {
            const canApprove = item.approveStatus === 0;

            html += `
                <tr class="border-b border-gray-100 hover:bg-neutral/50 transition-colors">
                    <td class="px-4 py-3">${item.empNo || '-'}</td>
                    <td class="px-4 py-3 font-medium">${item.empName || '-'}</td>
                    <td class="px-4 py-3">${item.leaveType || '-'}</td>
                    <td class="px-4 py-3">${item.startTime || '-'}</td>
                    <td class="px-4 py-3">${item.endTime || '-'}</td>
                    <td class="px-4 py-3">${item.leaveDays || 0}天</td>
                    <td class="px-4 py-3" title="${item.reason || ''}">${(item.reason || '').substring(0, 20)}${(item.reason || '').length > 20 ? '...' : ''}</td>
                    <td class="px-4 py-3">${app.getStatusBadge(item.approveStatus, 'approve')}</td>
                    <td class="px-4 py-3">
                        ${canApprove ? `
                            <button onclick="approveLeave(${item.leaveId}, 1)" class="text-success text-sm hover:underline mr-2">通过</button>
                            <button onclick="approveLeave(${item.leaveId}, 2)" class="text-danger text-sm hover:underline">驳回</button>
                        ` : '<span class="text-gray-400 text-sm">已处理</span>'}
                    </td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    // 渲染请假分页
    function renderLeavePagination() {
        const config = pageConfig.leave;
        const totalPages = Math.ceil(config.total / config.pageSize);
        app.renderPagination('leave-pagination', config.page, totalPages, function(page) {
            config.page = page;
            loadLeaveList();
        });
    }

    // 加载补卡申请列表
    function loadSupplementList() {
        const config = pageConfig.supplement;
        let url = `/reception/workbench/supplement/data.do?page=${config.page}&pageSize=${config.pageSize}`;
        if (config.status !== '') url += `&status=${config.status}`;

        app.get(url, function(result) {
            config.total = result.count || 0;
            renderSupplementTable(result.data || []);
            renderSupplementPagination();
        });
    }

    // 渲染补卡表格
    function renderSupplementTable(data) {
        const tbody = document.getElementById('supplement-tbody');
        if (!tbody) return;

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="px-4 py-8 text-center text-gray-500">暂无补卡申请</td></tr>';
            return;
        }

        let html = '';
        data.forEach(function(item) {
            const canApprove = item.approveStatus === 0;

            html += `
                <tr class="border-b border-gray-100 hover:bg-neutral/50 transition-colors">
                    <td class="px-4 py-3">${item.empNo || '-'}</td>
                    <td class="px-4 py-3 font-medium">${item.empName || '-'}</td>
                    <td class="px-4 py-3">${item.checkDate || '-'}</td>
                    <td class="px-4 py-3">${item.checkTypeName || '-'}</td>
                    <td class="px-4 py-3">${item.actualTime || '-'}</td>
                    <td class="px-4 py-3" title="${item.reason || ''}">${(item.reason || '').substring(0, 20)}${(item.reason || '').length > 20 ? '...' : ''}</td>
                    <td class="px-4 py-3">${app.getStatusBadge(item.approveStatus, 'approve')}</td>
                    <td class="px-4 py-3">
                        ${canApprove ? `
                            <button onclick="approveSupplement(${item.supplementId}, 1)" class="text-success text-sm hover:underline mr-2">通过</button>
                            <button onclick="approveSupplement(${item.supplementId}, 2)" class="text-danger text-sm hover:underline">驳回</button>
                        ` : '<span class="text-gray-400 text-sm">已处理</span>'}
                    </td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    // 渲染补卡分页
    function renderSupplementPagination() {
        const config = pageConfig.supplement;
        const totalPages = Math.ceil(config.total / config.pageSize);
        app.renderPagination('supplement-pagination', config.page, totalPages, function(page) {
            config.page = page;
            loadSupplementList();
        });
    }

    // 绑定事件
    function bindEvents() {
        // 请假筛选
        const leaveFilterBtn = document.getElementById('leave-filter-btn');
        if (leaveFilterBtn) {
            leaveFilterBtn.addEventListener('click', function() {
                pageConfig.leave.page = 1;
                pageConfig.leave.status = document.getElementById('leave-status-filter').value;
                loadLeaveList();
            });
        }

        // 补卡筛选
        const supplementFilterBtn = document.getElementById('supplement-filter-btn');
        if (supplementFilterBtn) {
            supplementFilterBtn.addEventListener('click', function() {
                pageConfig.supplement.page = 1;
                pageConfig.supplement.status = document.getElementById('supplement-status-filter').value;
                loadSupplementList();
            });
        }

        // 去审核按钮
        const goLeaveApprove = document.getElementById('go-leave-approve');
        if (goLeaveApprove) {
            goLeaveApprove.addEventListener('click', function() {
                document.getElementById('leave-section').scrollIntoView({ behavior: 'smooth' });
            });
        }

        const goSupplementApprove = document.getElementById('go-supplement-approve');
        if (goSupplementApprove) {
            goSupplementApprove.addEventListener('click', function() {
                document.getElementById('supplement-section').scrollIntoView({ behavior: 'smooth' });
            });
        }
    }

    // 审批请假
    window.approveLeave = function(leaveId, status) {
        const action = status === 1 ? '通过' : '驳回';
        showApprovalModal('请假申请审批', action, function(opinion) {
            app.post('/reception/workbench/leave/approve.do', {
                leaveId: leaveId,
                status: status,
                opinion: opinion
            }, function() {
                app.showToast('审批成功！', 'success');
                loadLeaveList();
                loadPendingCount();
            });
        });
    };

    // 审批补卡
    window.approveSupplement = function(supplementId, status) {
        const action = status === 1 ? '通过' : '驳回';
        showApprovalModal('补卡申请审批', action, function(opinion) {
            app.post('/reception/workbench/supplement/approve.do', {
                supplementId: supplementId,
                status: status,
                opinion: opinion
            }, function() {
                app.showToast('审批成功！', 'success');
                loadSupplementList();
                loadPendingCount();
            });
        });
    };

    // 显示审批模态框
    function showApprovalModal(title, action, onSubmit) {
        const existingModal = document.getElementById('approval-modal');
        if (existingModal) {
            existingModal.remove();
        }

        const isApprove = action === '通过';
        const actionClass = isApprove ? 'bg-success hover:bg-success/90' : 'bg-danger hover:bg-danger/90';

        const modal = document.createElement('div');
        modal.id = 'approval-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
            <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
                <div class="p-6 border-b border-gray-100">
                    <h3 class="text-lg font-bold text-gray-800">${title}</h3>
                </div>
                <div class="p-6 space-y-4">
                    <div class="flex items-center space-x-2">
                        <span class="text-gray-700">操作：</span>
                        <span class="${isApprove ? 'text-success' : 'text-danger'} font-medium">${action}</span>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">审批意见（可选）</label>
                        <textarea id="approval-opinion" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入审批意见"></textarea>
                    </div>
                </div>
                <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                    <button onclick="document.getElementById('approval-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>
                    <button id="approval-submit-btn" class="px-4 py-2 ${actionClass} text-white rounded-lg transition-colors">确认${action}</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        document.getElementById('approval-submit-btn').addEventListener('click', function() {
            const opinion = document.getElementById('approval-opinion').value.trim();
            modal.remove();
            onSubmit(opinion);
        });

        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });
    }

})();
