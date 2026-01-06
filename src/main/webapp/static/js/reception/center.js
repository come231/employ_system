/**
 * 前台个人中心脚本
 * @description 员工个人中心功能模块，包含个人信息、打卡记录、补卡申请、请假申请、部门信息等
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

    /**
     * 分页配置对象
     * @description 管理各标签页的分页状态和筛选条件
     */
    const pageConfig = {
        attendance: { page: 1, pageSize: 10, total: 0, status: '', startDate: '', endDate: '' },
        supplement: { page: 1, pageSize: 10, total: 0, status: '' },
        leave: { page: 1, pageSize: 10, total: 0, status: '' }
    };

    // ============================================================
    // 初始化入口
    // ============================================================

    /**
     * 页面加载完成后初始化
     * @description 监听用户信息加载事件，初始化页面各模块
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
        // 渲染导航栏
        app.renderNavbar(user, 'center');

        // 渲染用户信息卡片
        renderUserCard(user);

        // 加载个人信息
        loadProfile();

        // 绑定事件
        bindEvents();

        // 默认加载第一个标签页数据
        loadTabData('personal-info');
    }

    // 渲染用户信息卡片
    function renderUserCard(user) {
        const avatar = user && user.avatar ? (app.baseUrl + user.avatar) : 'https://via.placeholder.com/80';
        const empName = user ? user.empName : '用户';
        const deptName = user ? user.deptName : '';
        // 根据角色显示职位信息（数据库无position字段，使用role代替）
        const roleText = user && user.role === 'manager' ? '部门主管' : '普通员工';
        const empNo = user ? user.empNo : '';

        // 修复状态判断逻辑：status=1为在职，status=0为离职
        const statusValue = user ? user.status : null;
        const status = (statusValue === 1 || statusValue === '1') ? '在职' : '离职';
        const statusClass = (statusValue === 1 || statusValue === '1') ? 'bg-success/10 text-success' : 'bg-warning/10 text-warning';
        console.log("员工状态：" + statusValue);

        const userCard = document.getElementById('user-card');
        if (userCard) {
            userCard.innerHTML = `
                <div class="flex flex-col items-center py-6 border-b border-gray-100 mb-4">
                    <div class="relative">
                        <img src="${avatar}" alt="用户头像" class="w-20 h-20 rounded-full object-cover border-4 border-primary/20 mb-3" id="sidebar-avatar">
                        <label for="avatar-file-input-card" class="absolute bottom-2 right-0 w-7 h-7 bg-primary hover:bg-primary/90 rounded-full flex items-center justify-center cursor-pointer shadow-md transition-colors">
                            <i class="fa-solid fa-camera text-white text-xs"></i>
                        </label>
                        <input type="file" id="avatar-file-input-card" accept="image/jpeg,image/png,image/gif" class="hidden" onchange="handleAvatarFileSelect(this)">
                    </div>
                    <h3 class="text-lg font-bold text-dark" id="sidebar-name">${empName}</h3>
                    <p class="text-secondary text-sm" id="sidebar-dept-position">${deptName} | ${roleText}</p>
                    <div class="mt-3 flex space-x-2">
                        <span class="${statusClass} text-xs px-2 py-1 rounded-full">${status}</span>
                        <span class="bg-primary/10 text-primary text-xs px-2 py-1 rounded-full">ID: ${empNo}</span>
                    </div>
                </div>
            `;
        }
    }

    // 加载个人信息
    function loadProfile() {
        app.get('/reception/center/profile.do', function(result) {
            if (result.data) {
                renderPersonalInfo(result.data);
            }
        });
    }

    // 渲染个人信息
    function renderPersonalInfo(data) {
        const container = document.getElementById('personal-info-content');
        if (!container) return;

        // 根据状态显示在职/离职
        const statusValue = data.status;
        const statusText = (statusValue === 1 || statusValue === '1') ? '在职' : '离职';
        const statusClass = (statusValue === 1 || statusValue === '1') ? 'bg-success/10 text-success' : 'bg-warning/10 text-warning';

        // 根据角色显示职位
        const roleText = data.role === 'manager' ? '部门主管' : '普通员工';

        container.innerHTML = `
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-4">
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">姓名：</label>
                        <span class="text-dark">${data.empName || '-'}</span>
                    </div>
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">入职日期：</label>
                        <span class="text-dark">${data.hireDate || data.createTime || '-'}</span>
                    </div>
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">员工编号：</label>
                        <span class="text-dark">${data.empNo || '-'}</span>
                    </div>
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">状态：</label>
                        <span class="${statusClass} text-xs px-2 py-1 rounded-full">${statusText}</span>
                    </div>
                </div>
                <div class="space-y-4">
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">手机号：</label>
                        <span class="text-dark" id="profile-phone">${data.phone || '-'}</span>
                        <button onclick="showEditPhoneModal('${data.phone || ''}')" class="ml-2 text-primary text-sm hover:underline">修改</button>
                    </div>
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">邮箱：</label>
                        <span class="text-dark" id="profile-email">${data.email || '-'}</span>
                        <button onclick="showEditEmailModal('${data.email || ''}')" class="ml-2 text-primary text-sm hover:underline">修改</button>
                    </div>
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">部门：</label>
                        <span class="text-dark">${data.deptName || '-'}</span>
                    </div>
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">职位：</label>
                        <span class="text-dark">${roleText}</span>
                    </div>
                    <div class="flex items-center">
                        <label class="w-24 text-secondary font-medium">直属上级：</label>
                        <span class="text-dark">${data.managerName || '-'}</span>
                    </div>
                </div>
            </div>
        `;
    }

    // 绑定事件
    function bindEvents() {
        // 侧边栏标签切换
        document.querySelectorAll('.sidebar-item').forEach(function(item) {
            item.addEventListener('click', function(e) {
                e.preventDefault();

                document.querySelectorAll('.sidebar-item').forEach(i => i.classList.remove('active'));
                document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));

                this.classList.add('active');
                const tabId = this.getAttribute('data-tab');
                const tabEl = document.getElementById(tabId);
                if (tabEl) {
                    tabEl.classList.add('active');
                }

                // 更新图标颜色
                document.querySelectorAll('.sidebar-item').forEach(i => {
                    const icon = i.querySelector('i');
                    if (icon) {
                        if (i.classList.contains('active')) {
                            icon.classList.remove('text-secondary');
                            icon.classList.add('text-primary');
                        } else {
                            icon.classList.remove('text-primary');
                            icon.classList.add('text-secondary');
                        }
                    }
                });

                // 加载标签页数据
                loadTabData(tabId);
            });
        });

        // 考勤筛选
        const attendanceFilterBtn = document.getElementById('attendance-filter-btn');
        if (attendanceFilterBtn) {
            attendanceFilterBtn.addEventListener('click', function() {
                pageConfig.attendance.page = 1;
                pageConfig.attendance.status = document.getElementById('attendance-status-filter').value;
                pageConfig.attendance.startDate = document.getElementById('attendance-start-date').value;
                pageConfig.attendance.endDate = document.getElementById('attendance-end-date').value;
                loadAttendanceData();
            });
        }

        // 补卡筛选
        const supplementFilterBtn = document.getElementById('supplement-filter-btn');
        if (supplementFilterBtn) {
            supplementFilterBtn.addEventListener('click', function() {
                pageConfig.supplement.page = 1;
                pageConfig.supplement.status = document.getElementById('supplement-status-filter').value;
                loadSupplementData();
            });
        }

        // 请假筛选
        const leaveFilterBtn = document.getElementById('leave-filter-btn');
        if (leaveFilterBtn) {
            leaveFilterBtn.addEventListener('click', function() {
                pageConfig.leave.page = 1;
                pageConfig.leave.status = document.getElementById('leave-status-filter').value;
                loadLeaveData();
            });
        }

        // 新增补卡按钮
        const addSupplementBtn = document.getElementById('add-supplement-btn');
        if (addSupplementBtn) {
            addSupplementBtn.addEventListener('click', function() {
                // 调用首页的补卡申请模态框
                if (window.showSupplementModal) {
                    window.showSupplementModal();
                } else {
                    showSupplementModalLocal();
                }
            });
        }

        // 新增请假按钮
        const addLeaveBtn = document.getElementById('add-leave-btn');
        if (addLeaveBtn) {
            addLeaveBtn.addEventListener('click', function() {
                // 调用首页的请假申请模态框
                if (window.showLeaveModal) {
                    window.showLeaveModal();
                } else {
                    showLeaveModalLocal();
                }
            });
        }
    }

    // 加载标签页数据
    function loadTabData(tabId) {
        switch (tabId) {
            case 'personal-info':
                loadProfile();
                break;
            case 'my-punch':
                loadAttendanceData();
                break;
            case 'my-repair':
                loadSupplementData();
                break;
            case 'my-leave':
                loadLeaveData();
                break;
            case 'dept-info':
                loadDeptInfo();
                break;
        }
    }

    // 加载考勤数据
    function loadAttendanceData() {
        const config = pageConfig.attendance;
        let url = `/reception/center/attendance/data.do?page=${config.page}&pageSize=${config.pageSize}`;
        if (config.status !== '') url += `&status=${config.status}`;
        if (config.startDate) url += `&startDate=${config.startDate}`;
        if (config.endDate) url += `&endDate=${config.endDate}`;

        app.get(url, function(result) {
            config.total = result.count || 0;
            renderAttendanceTable(result.data || []);
            renderAttendancePagination();
            updateAttendanceStats(result);
        });
    }

    // 渲染考勤表格
    function renderAttendanceTable(data) {
        const tbody = document.getElementById('attendance-tbody');
        if (!tbody) return;

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="px-4 py-8 text-center text-gray-500">暂无考勤记录</td></tr>';
            return;
        }

        let html = '';
        data.forEach(function(item) {
            html += `
                <tr class="border-b border-gray-100 hover:bg-neutral/50 transition-colors">
                    <td class="px-4 py-3 font-medium">${item.checkDate || '-'}</td>
                    <td class="px-4 py-3">${item.checkInTime || '-'}</td>
                    <td class="px-4 py-3">${item.checkOutTime || '-'}</td>
                    <td class="px-4 py-3">${app.getStatusBadge(item.status, 'attendance')}</td>
                    <td class="px-4 py-3">${item.remark || '-'}</td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    // 渲染考勤分页
    function renderAttendancePagination() {
        const config = pageConfig.attendance;
        const totalPages = Math.ceil(config.total / config.pageSize);
        app.renderPagination('attendance-pagination', config.page, totalPages, function(page) {
            config.page = page;
            loadAttendanceData();
        });
    }

    // 更新考勤统计
    function updateAttendanceStats(result) {
        // 统计数据可以从后端返回，这里暂时保持原样
    }

    // 加载补卡数据
    function loadSupplementData() {
        const config = pageConfig.supplement;
        let url = `/reception/center/supplement/data.do?page=${config.page}&pageSize=${config.pageSize}`;
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
            tbody.innerHTML = '<tr><td colspan="6" class="px-4 py-8 text-center text-gray-500">暂无补卡记录</td></tr>';
            return;
        }

        let html = '';
        data.forEach(function(item) {
            const checkTypeName = item.checkType === 1 ? '上班打卡' : (item.checkType === 2 ? '下班打卡' : '上下班打卡');
            const canWithdraw = item.approveStatus === 0;

            html += `
                <tr class="border-b border-gray-100 hover:bg-neutral/50 transition-colors">
                    <td class="px-4 py-3 font-medium">${item.checkDate || '-'}</td>
                    <td class="px-4 py-3">${checkTypeName}</td>
                    <td class="px-4 py-3">${item.createTime || '-'}</td>
                    <td class="px-4 py-3">${app.getStatusBadge(item.approveStatus, 'approve')}</td>
                    <td class="px-4 py-3">${item.approverName || '-'}</td>
                    <td class="px-4 py-3">
                        ${canWithdraw ? `<button onclick="withdrawSupplement(${item.supplementId})" class="text-red-500 text-sm hover:underline">撤销</button>` : '<button class="text-primary text-sm hover:underline" onclick="showSupplementDetail(' + item.supplementId + ')">查看</button>'}
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
            loadSupplementData();
        });
    }

    // 加载请假数据
    function loadLeaveData() {
        const config = pageConfig.leave;
        let url = `/reception/center/leave/data.do?page=${config.page}&pageSize=${config.pageSize}`;
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
            tbody.innerHTML = '<tr><td colspan="6" class="px-4 py-8 text-center text-gray-500">暂无请假记录</td></tr>';
            return;
        }

        let html = '';
        data.forEach(function(item) {
            const canWithdraw = item.approveStatus === 0;

            html += `
                <tr class="border-b border-gray-100 hover:bg-neutral/50 transition-colors">
                    <td class="px-4 py-3 font-medium">${item.leaveType || '-'}</td>
                    <td class="px-4 py-3">${item.startTime || '-'} 至 ${item.endTime || '-'}</td>
                    <td class="px-4 py-3">${item.leaveDays || 0}天</td>
                    <td class="px-4 py-3">${app.getStatusBadge(item.approveStatus, 'approve')}</td>
                    <td class="px-4 py-3">${item.approverName || '-'}</td>
                    <td class="px-4 py-3">
                        ${canWithdraw ? `<button onclick="withdrawLeave(${item.leaveId})" class="text-red-500 text-sm hover:underline">撤销</button>` : '<button class="text-primary text-sm hover:underline" onclick="showLeaveDetail(' + item.leaveId + ')">查看</button>'}
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
            loadLeaveData();
        });
    }

    // 加载部门信息
    function loadDeptInfo() {
        app.get('/reception/center/dept/info.do', function(result) {
            if (result.data) {
                renderDeptInfo(result.data);
            }
        });
    }

    // 渲染部门信息
    function renderDeptInfo(data) {
        const deptBasic = document.getElementById('dept-basic-info');
        if (deptBasic) {
            deptBasic.innerHTML = `
                <div class="flex items-center mb-4">
                    <div class="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mr-4">
                        <i class="fa-solid fa-building text-primary text-xl"></i>
                    </div>
                    <div>
                        <h3 class="text-lg font-bold text-dark">${data.deptName || '-'}</h3>
                        <p class="text-secondary text-sm">部门ID：${data.deptId || '-'}</p>
                    </div>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <p class="text-secondary text-sm mb-1">部门经理</p>
                        <p class="text-dark font-medium">${data.managerName || '-'}</p>
                    </div>
                    <div>
                        <p class="text-secondary text-sm mb-1">部门人数</p>
                        <p class="text-dark font-medium">${data.employeeCount || 0}人</p>
                    </div>
                    <div>
                        <p class="text-secondary text-sm mb-1">联系方式</p>
                        <p class="text-dark font-medium">${data.managerPhone || '-'}</p>
                    </div>
                </div>
            `;
        }

        // 渲染部门成员（后端返回employees数组）
        const membersTbody = document.getElementById('dept-members-tbody');
        const members = data.employees || data.members || [];
        if (membersTbody) {
            if (members.length === 0) {
                membersTbody.innerHTML = '<tr><td colspan="5" class="px-4 py-8 text-center text-gray-500">暂无成员</td></tr>';
                return;
            }

            let html = '';
            members.forEach(function(member) {
                const statusClass = member.status === 1 ? 'bg-success/10 text-success' : 'bg-warning/10 text-warning';
                const statusText = member.status === 1 ? '在职' : '离职';
                // 使用createTime作为入职日期
                const hireDate = member.hireDate || member.createTime || '-';

                html += `
                    <tr class="border-b border-gray-100 hover:bg-neutral/50 transition-colors">
                        <td class="px-4 py-3 font-medium">${member.empName || '-'}</td>
                        <td class="px-4 py-3">${member.position || '-'}</td>
                        <td class="px-4 py-3">${hireDate}</td>
                        <td class="px-4 py-3">${member.phone ? member.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '-'}</td>
                        <td class="px-4 py-3"><span class="${statusClass} text-xs px-2 py-1 rounded-full">${statusText}</span></td>
                    </tr>
                `;
            });
            membersTbody.innerHTML = html;
        }
    }

    // 撤销补卡申请
    window.withdrawSupplement = function(supplementId) {
        app.showConfirm('确认撤销', '确定要撤销该补卡申请吗？', function() {
            app.post('/reception/center/withdraw.do', { type: 'supplement', id: supplementId }, function() {
                app.showToast('撤销成功！', 'success');
                loadSupplementData();
            });
        });
    };

    // 撤销请假申请
    window.withdrawLeave = function(leaveId) {
        app.showConfirm('确认撤销', '确定要撤销该请假申请吗？', function() {
            app.post('/reception/center/withdraw.do', { type: 'leave', id: leaveId }, function() {
                app.showToast('撤销成功！', 'success');
                loadLeaveData();
            });
        });
    };

    // 修改手机号模态框
    window.showEditPhoneModal = function(currentPhone) {
        const modal = document.createElement('div');
        modal.id = 'edit-phone-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
            <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
                <div class="p-6 border-b border-gray-100">
                    <h3 class="text-lg font-bold text-gray-800">修改手机号</h3>
                </div>
                <div class="p-6">
                    <input type="tel" id="new-phone" value="${currentPhone}" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入新手机号">
                </div>
                <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                    <button onclick="document.getElementById('edit-phone-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>
                    <button onclick="updatePhone()" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">保存</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        modal.addEventListener('click', function(e) {
            if (e.target === modal) modal.remove();
        });
    };

    window.updatePhone = function() {
        const phone = document.getElementById('new-phone').value.trim();
        if (!phone) {
            app.showToast('请输入手机号', 'warning');
            return;
        }
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            app.showToast('请输入有效的手机号', 'warning');
            return;
        }

        app.post('/reception/center/profile/update.do', { phone: phone }, function() {
            app.showToast('手机号修改成功！', 'success');
            document.getElementById('edit-phone-modal').remove();
            loadProfile();
        });
    };

    // 修改邮箱模态框
    window.showEditEmailModal = function(currentEmail) {
        const modal = document.createElement('div');
        modal.id = 'edit-email-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
            <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
                <div class="p-6 border-b border-gray-100">
                    <h3 class="text-lg font-bold text-gray-800">修改邮箱</h3>
                </div>
                <div class="p-6">
                    <input type="email" id="new-email" value="${currentEmail}" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入新邮箱">
                </div>
                <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                    <button onclick="document.getElementById('edit-email-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>
                    <button onclick="updateEmail()" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">保存</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        modal.addEventListener('click', function(e) {
            if (e.target === modal) modal.remove();
        });
    };

    window.updateEmail = function() {
        const email = document.getElementById('new-email').value.trim();
        if (!email) {
            app.showToast('请输入邮箱', 'warning');
            return;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            app.showToast('请输入有效的邮箱', 'warning');
            return;
        }

        app.post('/reception/center/profile/update.do', { email: email }, function() {
            app.showToast('邮箱修改成功！', 'success');
            document.getElementById('edit-email-modal').remove();
            loadProfile();
        });
    };

    // 本地补卡申请模态框
    function showSupplementModalLocal() {
        const modal = document.createElement('div');
        modal.id = 'supplement-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
            <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4">
                <div class="p-6 border-b border-gray-100 flex justify-between items-center">
                    <h3 class="text-lg font-bold text-gray-800">
                        <i class="fa-solid fa-id-card text-primary mr-2"></i>补卡申请
                    </h3>
                    <button onclick="document.getElementById('supplement-modal').remove()" class="text-gray-400 hover:text-gray-600">
                        <i class="fa-solid fa-times"></i>
                    </button>
                </div>
                <div class="p-6 space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">补卡日期 <span class="text-red-500">*</span></label>
                        <input type="date" id="supplement-date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">补卡类型 <span class="text-red-500">*</span></label>
                        <select id="supplement-type" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                            <option value="">请选择补卡类型</option>
                            <option value="1">上班打卡</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">实际到岗时间 <span class="text-red-500">*</span></label>
                        <input type="time" id="supplement-time" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">补卡原因 <span class="text-red-500">*</span></label>
                        <textarea id="supplement-reason" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入补卡原因"></textarea>
                    </div>
                </div>
                <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                    <button onclick="document.getElementById('supplement-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>
                    <button onclick="submitSupplementLocal()" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">提交申请</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        modal.addEventListener('click', function(e) {
            if (e.target === modal) modal.remove();
        });
    }

    window.submitSupplementLocal = function() {
        const checkDate = document.getElementById('supplement-date').value;
        const checkType = document.getElementById('supplement-type').value;
        const actualTime = document.getElementById('supplement-time').value;
        const reason = document.getElementById('supplement-reason').value.trim();

        if (!checkDate || !checkType || !actualTime || !reason) {
            app.showToast('请填写完整信息', 'warning');
            return;
        }

        app.post('/reception/home/supplement/apply.do', {
            checkDate: checkDate,
            checkType: checkType,
            actualTime: actualTime + ':00',
            reason: reason
        }, function() {
            app.showToast('补卡申请提交成功！', 'success');
            document.getElementById('supplement-modal').remove();
            loadSupplementData();
        });
    };

    // 本地请假申请模态框
    function showLeaveModalLocal() {
        const modal = document.createElement('div');
        modal.id = 'leave-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
            <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
                <div class="p-6 border-b border-gray-100 flex justify-between items-center">
                    <h3 class="text-lg font-bold text-gray-800">
                        <i class="fa-solid fa-calendar-times text-primary mr-2"></i>请假申请
                    </h3>
                    <button onclick="document.getElementById('leave-modal').remove()" class="text-gray-400 hover:text-gray-600">
                        <i class="fa-solid fa-times"></i>
                    </button>
                </div>
                <div class="p-6 space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">请假类型 <span class="text-red-500">*</span></label>
                        <select id="leave-type" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                            <option value="">请选择请假类型</option>
                            <option value="年假">年假</option>
                            <option value="病假">病假</option>
                            <option value="事假">事假</option>
                        </select>
                    </div>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">开始时间 <span class="text-red-500">*</span></label>
                            <input type="datetime-local" id="leave-start" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">结束时间 <span class="text-red-500">*</span></label>
                            <input type="datetime-local" id="leave-end" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                        </div>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">请假天数</label>
                        <input type="number" id="leave-days" class="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50" readonly>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">请假原因 <span class="text-red-500">*</span></label>
                        <textarea id="leave-reason" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入请假原因"></textarea>
                    </div>
                </div>
                <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                    <button onclick="document.getElementById('leave-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>
                    <button onclick="submitLeaveLocal()" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">提交申请</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        // 自动计算请假天数
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

        modal.addEventListener('click', function(e) {
            if (e.target === modal) modal.remove();
        });
    }

    window.submitLeaveLocal = function() {
        const leaveType = document.getElementById('leave-type').value;
        const startTime = document.getElementById('leave-start').value;
        const endTime = document.getElementById('leave-end').value;
        const leaveDays = document.getElementById('leave-days').value;
        const reason = document.getElementById('leave-reason').value.trim();

        if (!leaveType || !startTime || !endTime || !reason) {
            app.showToast('请填写完整信息', 'warning');
            return;
        }

        app.post('/reception/home/leave/apply.do', {
            leaveType: leaveType,
            startTime: startTime,
            endTime: endTime,
            leaveDays: leaveDays || 1,
            reason: reason
        }, function() {
            app.showToast('请假申请提交成功！', 'success');
            document.getElementById('leave-modal').remove();
            loadLeaveData();
        });
    };

    // 查看请假详情
    window.showLeaveDetail = function(leaveId) {
        app.get('/reception/center/leave/detail.do?leaveId=' + leaveId, function(result) {
            if (result.data) {
                const data = result.data;
                const modal = document.createElement('div');
                modal.id = 'leave-detail-modal';
                modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
                modal.innerHTML = `
                    <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4">
                        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
                            <h3 class="text-lg font-bold text-gray-800">
                                <i class="fa-solid fa-calendar-times text-primary mr-2"></i>请假详情
                            </h3>
                            <button onclick="document.getElementById('leave-detail-modal').remove()" class="text-gray-400 hover:text-gray-600">
                                <i class="fa-solid fa-times"></i>
                            </button>
                        </div>
                        <div class="p-6 space-y-4">
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="text-sm text-gray-500">请假类型</label>
                                    <p class="font-medium">${data.leaveType || '-'}</p>
                                </div>
                                <div>
                                    <label class="text-sm text-gray-500">请假天数</label>
                                    <p class="font-medium">${data.leaveDays || 0}天</p>
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="text-sm text-gray-500">开始时间</label>
                                    <p class="font-medium">${data.startTime || '-'}</p>
                                </div>
                                <div>
                                    <label class="text-sm text-gray-500">结束时间</label>
                                    <p class="font-medium">${data.endTime || '-'}</p>
                                </div>
                            </div>
                            <div>
                                <label class="text-sm text-gray-500">请假原因</label>
                                <p class="font-medium">${data.reason || '-'}</p>
                            </div>
                            <div>
                                <label class="text-sm text-gray-500">审批状态</label>
                                <p class="font-medium">${app.getStatusBadge(data.approveStatus, 'approve')}</p>
                            </div>
                            <div>
                                <label class="text-sm text-gray-500">审批意见</label>
                                <p class="font-medium">${data.approveOpinion || '-'}</p>
                            </div>
                        </div>
                        <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end">
                            <button onclick="document.getElementById('leave-detail-modal').remove()" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">关闭</button>
                        </div>
                    </div>
                `;
                document.body.appendChild(modal);
                modal.addEventListener('click', function(e) {
                    if (e.target === modal) modal.remove();
                });
            }
        });
    };

    // 查看补卡详情
    window.showSupplementDetail = function(supplementId) {
        app.get('/reception/center/supplement/detail.do?supplementId=' + supplementId, function(result) {
            if (result.data) {
                const data = result.data;
                const checkTypeName = data.checkType === 1 ? '上班打卡' : (data.checkType === 2 ? '下班打卡' : '上下班打卡');
                const modal = document.createElement('div');
                modal.id = 'supplement-detail-modal';
                modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
                modal.innerHTML = `
                    <div class="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4">
                        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
                            <h3 class="text-lg font-bold text-gray-800">
                                <i class="fa-solid fa-id-card text-primary mr-2"></i>补卡详情
                            </h3>
                            <button onclick="document.getElementById('supplement-detail-modal').remove()" class="text-gray-400 hover:text-gray-600">
                                <i class="fa-solid fa-times"></i>
                            </button>
                        </div>
                        <div class="p-6 space-y-4">
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="text-sm text-gray-500">补卡日期</label>
                                    <p class="font-medium">${data.checkDate || '-'}</p>
                                </div>
                                <div>
                                    <label class="text-sm text-gray-500">补卡类型</label>
                                    <p class="font-medium">${checkTypeName}</p>
                                </div>
                            </div>
                            <div>
                                <label class="text-sm text-gray-500">实际到岗时间</label>
                                <p class="font-medium">${data.actualTime || '-'}</p>
                            </div>
                            <div>
                                <label class="text-sm text-gray-500">补卡原因</label>
                                <p class="font-medium">${data.reason || '-'}</p>
                            </div>
                            <div>
                                <label class="text-sm text-gray-500">审批状态</label>
                                <p class="font-medium">${app.getStatusBadge(data.approveStatus, 'approve')}</p>
                            </div>
                            <div>
                                <label class="text-sm text-gray-500">审批意见</label>
                                <p class="font-medium">${data.approveOpinion || '-'}</p>
                            </div>
                        </div>
                        <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end">
                            <button onclick="document.getElementById('supplement-detail-modal').remove()" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">关闭</button>
                        </div>
                    </div>
                `;
                document.body.appendChild(modal);
                modal.addEventListener('click', function(e) {
                    if (e.target === modal) modal.remove();
                });
            }
        });
    };

    // 头像上传模态框
    window.showAvatarUploadModal = function() {
        const currentAvatar = document.getElementById('sidebar-avatar') ? document.getElementById('sidebar-avatar').src : '';
        const modal = document.createElement('div');
        modal.id = 'avatar-upload-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML = `
            <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
                <div class="p-6 border-b border-gray-100 flex justify-between items-center">
                    <h3 class="text-lg font-bold text-gray-800">
                        <i class="fa-solid fa-camera text-primary mr-2"></i>更换头像
                    </h3>
                    <button onclick="document.getElementById('avatar-upload-modal').remove()" class="text-gray-400 hover:text-gray-600">
                        <i class="fa-solid fa-times"></i>
                    </button>
                </div>
                <div class="p-6">
                    <div class="flex flex-col items-center">
                        <div class="relative mb-4">
                            <img id="avatar-preview" src="${currentAvatar}" alt="头像预览" class="w-32 h-32 rounded-full object-cover border-4 border-primary/20">
                        </div>
                        <p class="text-sm text-gray-500 mb-4">支持 jpg、png、gif 格式，大小不超过 2MB</p>
                        <label class="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors cursor-pointer">
                            <i class="fa-solid fa-upload mr-2"></i>选择图片
                            <input type="file" id="avatar-file-input" accept="image/jpeg,image/png,image/gif" class="hidden" onchange="previewAvatar(this)">
                        </label>
                    </div>
                </div>
                <div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">
                    <button onclick="document.getElementById('avatar-upload-modal').remove()" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>
                    <button onclick="uploadAvatar()" id="upload-avatar-btn" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">上传</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        modal.addEventListener('click', function(e) {
            if (e.target === modal) modal.remove();
        });
    };

    // 预览头像
    window.previewAvatar = function(input) {
        if (input.files && input.files[0]) {
            const file = input.files[0];

            // 校验文件类型
            const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!allowedTypes.includes(file.type)) {
                app.showToast('只支持 jpg、png、gif 格式的图片', 'warning');
                input.value = '';
                return;
            }

            // 校验文件大小（2MB）
            if (file.size > 2 * 1024 * 1024) {
                app.showToast('图片大小不能超过 2MB', 'warning');
                input.value = '';
                return;
            }

            const reader = new FileReader();
            reader.onload = function(e) {
                const preview = document.getElementById('avatar-preview');
                if (preview) {
                    preview.src = e.target.result;
                }
            };
            reader.readAsDataURL(file);
        }
    };

    // 上传头像
    window.uploadAvatar = function() {
        const fileInput = document.getElementById('avatar-file-input');
        if (!fileInput || !fileInput.files || !fileInput.files[0]) {
            app.showToast('请先选择图片', 'warning');
            return;
        }

        const file = fileInput.files[0];
        const formData = new FormData();
        formData.append('file', file);

        // 禁用上传按钮
        const uploadBtn = document.getElementById('upload-avatar-btn');
        if (uploadBtn) {
            uploadBtn.disabled = true;
            uploadBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-2"></i>上传中...';
        }

        fetch(app.baseUrl + '/reception/center/uploadAvatar.do', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(result => {
            if (result.code === 200 || result.code === '200') {
                app.showToast('头像上传成功', 'success');

                // 更新页面上的头像
                const newAvatar = app.baseUrl + result.avatar;
                const sidebarAvatar = document.getElementById('sidebar-avatar');
                if (sidebarAvatar) {
                    sidebarAvatar.src = newAvatar;
                }

                // 更新导航栏头像
                const navAvatar = document.getElementById('nav-avatar');
                if (navAvatar) {
                    navAvatar.src = newAvatar;
                }

                // 更新 currentUser
                if (app.currentUser) {
                    app.currentUser.avatar = result.avatar;
                }

                // 关闭模态框
                const modal = document.getElementById('avatar-upload-modal');
                if (modal) modal.remove();
            } else {
                app.showToast(result.msg || '上传失败', 'error');
            }
        })
        .catch(error => {
            console.error('Upload error:', error);
            app.showToast('上传失败，请稍后重试', 'error');
        })
        .finally(() => {
            // 恢复上传按钮
            if (uploadBtn) {
                uploadBtn.disabled = false;
                uploadBtn.innerHTML = '上传';
            }
        });
    };

    // 直接选择文件后上传头像（点击相机按钮）
    window.handleAvatarFileSelect = function(input) {
        if (input.files && input.files[0]) {
            const file = input.files[0];

            // 校验文件类型
            const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!allowedTypes.includes(file.type)) {
                app.showToast('只支持 jpg、png、gif 格式的图片', 'warning');
                input.value = '';
                return;
            }

            // 校验文件大小（2MB）
            if (file.size > 2 * 1024 * 1024) {
                app.showToast('图片大小不能超过 2MB', 'warning');
                input.value = '';
                return;
            }

            // 直接上传
            const formData = new FormData();
            formData.append('file', file);

            app.showToast('正在上传...', 'info');

            fetch(app.baseUrl + '/reception/center/uploadAvatar.do', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(result => {
                if (result.code === 200 || result.code === '200') {
                    app.showToast('头像上传成功', 'success');

                    // 更新页面上的头像
                    const newAvatar = app.baseUrl + result.avatar;
                    const sidebarAvatar = document.getElementById('sidebar-avatar');
                    if (sidebarAvatar) {
                        sidebarAvatar.src = newAvatar;
                    }

                    // 更新导航栏头像
                    const navAvatar = document.getElementById('nav-avatar');
                    if (navAvatar) {
                        navAvatar.src = newAvatar;
                    }

                    // 更新 currentUser
                    if (app.currentUser) {
                        app.currentUser.avatar = result.avatar;
                    }
                } else {
                    app.showToast(result.msg || '上传失败', 'error');
                }
            })
            .catch(error => {
                console.error('Upload error:', error);
                app.showToast('上传失败，请稍后重试', 'error');
            });

            // 清空input以便下次选择同一文件时也能触发change事件
            input.value = '';
        }
    };

})();
