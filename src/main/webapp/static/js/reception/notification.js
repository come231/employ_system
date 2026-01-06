/**
 * 前台消息通知页面业务逻辑
 * @description 处理消息通知的列表展示、筛选、已读标记、删除等功能
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

    /** 当前页码 */
    let currentPage = 1;

    /** 每页显示条数 */
    const pageSize = 10;

    /** 通知总数 */
    let totalCount = 0;

    /** 当前筛选条件 */
    let currentFilter = {
        type: '',    // 通知类型
        status: ''   // 已读状态
    };

    /** 当前选中的通知（用于模态框操作） */
    let currentNotification = {
        id: null,
        type: null
    };

    // ============================================================
    // 初始化入口
    // ============================================================

    /**
     * 页面初始化
     * @description 检查登录状态，渲染导航栏，加载通知列表
     */
    document.addEventListener('DOMContentLoaded', function() {
        // 检查登录状态
        app.checkLogin(function(isLoggedIn, user) {
            if (isLoggedIn) {
                // 绑定事件
                bindEvents();
                // 渲染导航栏
                app.renderNavbar(user, 'notification');
                // 加载通知列表
                loadNotifications();

            }
        });
    });

    /**
     * 绑定事件
     */
    function bindEvents() {
        // 全部已读按钮
        const markAllReadBtn = document.getElementById('mark-all-read-btn');
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', function() {
                markAllAsRead();
            });
        }

        // 筛选按钮
        const filterBtn = document.getElementById('filter-btn');
        if (filterBtn) {
            filterBtn.addEventListener('click', function() {
                currentFilter.type = document.getElementById('filter-type').value;
                currentFilter.status = document.getElementById('filter-status').value;
                currentPage = 1;
                loadNotifications();
            });
        }

        // 重置按钮
        const resetBtn = document.getElementById('reset-btn');
        if (resetBtn) {
            resetBtn.addEventListener('click', function() {
                document.getElementById('filter-type').value = '';
                document.getElementById('filter-status').value = '';
                currentFilter = { type: '', status: '' };
                currentPage = 1;
                loadNotifications();
            });
        }

        // 模态框关闭按钮
        const closeModalBtn = document.getElementById('close-modal-btn');
        const modalCloseBtn = document.getElementById('modal-close-btn');
        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', closeDetailModal);
        }
        if (modalCloseBtn) {
            modalCloseBtn.addEventListener('click', closeDetailModal);
        }

        // 点击模态框背景关闭
        const modal = document.getElementById('notification-detail-modal');
        if (modal) {
            modal.addEventListener('click', function(e) {
                if (e.target === modal) {
                    closeDetailModal();
                }
            });
        }

        // 删除按钮
        const deleteBtn = document.getElementById('modal-delete-btn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', function() {
                if (currentNotification.id) {
                    deleteNotification(currentNotification.id);
                }
            });
        }

        // 查看详情按钮
        const viewBtn = document.getElementById('modal-view-btn');
        if (viewBtn) {
            viewBtn.addEventListener('click', function() {
                navigateToDetail(currentNotification.type);
            });
        }
    }

    /**
     * 加载通知列表
     */
    function loadNotifications() {
        // 1. 强制获取容器并清空，增加存在性判断，确保初始加载状态被清除
        const listContainer = document.getElementById('notification-list');
        if (!listContainer) {
            console.error('未找到notification-list容器');
            return;
        }
        // 强制清空，覆盖初始的“加载中”DOM
        listContainer.innerHTML = '';
        console.log(listContainer);

        const requestUrl = '/reception/notification/list.do?page=' + currentPage + '&pageSize=' + pageSize;
        console.log('正在请求通知列表:', requestUrl);

        // 调用API获取通知列表
        app.get(requestUrl, function(result) {
            console.log('通知列表接口返回:', result);
            // 重置列表（双重保障）
            listContainer.innerHTML = '';
            if (result.data && result.data.length > 0) {
                console.log("消息列表1：", result.data);
                console.log("数据条数：", result.data.length);
                totalCount = result.count || result.data.length;
                renderNotificationList(result.data);
                updateUnreadBadge();
            } else {
                console.log('通知列表为空或无数据');
                showEmptyList(); // 调用已定义的函数
            }
        }, function(error) {
            console.log('专用接口失败，尝试备用接口。错误:', error);
            // 如果专用接口失败，尝试使用Home接口
            app.get('/reception/home/notifications.do?pageSize=50', function(result) {
                console.log('备用接口返回:', result);
                if (result.data && result.data.list && result.data.list.length > 0) {
                    let notifications = result.data.list;
                    console.log("消息列表2：", notifications);
                    // 应用筛选
                    if (currentFilter.type) {
                        notifications = notifications.filter(n => n.type === currentFilter.type);
                    }
                    if (currentFilter.status) {
                        if (currentFilter.status === 'unread') {
                            notifications = notifications.filter(n => n.isRead === 0);
                        } else if (currentFilter.status === 'read') {
                            notifications = notifications.filter(n => n.isRead === 1);
                        }
                    }

                    totalCount = notifications.length;

                    // 手动分页
                    const startIndex = (currentPage - 1) * pageSize;
                    const endIndex = startIndex + pageSize;
                    const pagedNotifications = notifications.slice(startIndex, endIndex);

                    if (pagedNotifications.length > 0) {
                        renderNotificationList(pagedNotifications);
                    } else {
                        showEmptyList(); // 调用已定义的函数
                    }

                    // 更新未读数
                    const unreadCount = result.data.unreadCount || 0;
                    updateUnreadBadgeValue(unreadCount);
                } else {
                    showEmptyList(); // 调用已定义的函数
                }
            }, function() {
                // 两个接口都失败了
                showEmptyList(); // 调用已定义的函数
            });
        });
    }

// 关键修改2：正式定义showEmptyList函数
    function showEmptyList() {
        const listContainer = document.getElementById('notification-list');
        if (!listContainer) return; // 增加存在性判断
        listContainer.innerHTML='';
        // 渲染暂无数据
        listContainer.innerHTML = `
    <div class="p-12 text-center">
        <i class="fa-solid fa-bell-slash text-gray-300 text-5xl mb-4"></i>
        <p class="text-gray-500">暂无通知消息</p>
    </div>
    `;
        // 增加paginationContainer存在性判断，避免报错
        const paginationContainer = document.getElementById('notification-pagination');
        if (paginationContainer) {
            paginationContainer.innerHTML = '';
        }
    }

    /**
     * 渲染通知列表
     */
    function renderNotificationList(notifications) {
        const listContainer = document.getElementById('notification-list');
        if (!listContainer) return; // 增加存在性判断

        console.log('renderNotificationList 接收到数据：', notifications);
        // 先强制清空，再判断数据
        listContainer.innerHTML = '';
        if (!notifications || !Array.isArray(notifications) || notifications.length === 0) {
            listContainer.innerHTML = `
            <div class="p-12 text-center">
                <i class="fa-solid fa-bell-slash text-gray-300 text-5xl mb-4"></i>
                <p class="text-gray-500">暂无通知消息</p>
            </div>
        `;
            return;
        }

        let html = '';
        notifications.forEach(function(notification) {
            // 修复：使用 == 进行宽松比较，或转换为数字比较
            const isUnread = Number(notification.isRead) === 0;
            const typeInfo = getTypeInfo(notification.type);
            console.log('渲染通知项:', notification.notificationId, 'isRead:', notification.isRead, 'isUnread:', isUnread);

            html += `
                <div class="notification-item p-4 cursor-pointer ${isUnread ? 'unread' : ''}"
                     data-id="${notification.notificationId}"
                     data-type="${notification.type || ''}"
                     data-title="${escapeHtml(notification.title || '')}"
                     data-content="${escapeHtml(notification.content || '')}"
                     data-time="${notification.createTime || ''}"
                     data-read="${notification.isRead}">
                    <div class="flex items-start space-x-4">
                        <div class="flex-shrink-0 w-10 h-10 rounded-full ${typeInfo.bgColor} flex items-center justify-center">
                            <i class="${typeInfo.icon} ${typeInfo.iconColor}"></i>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center justify-between">
                                <h4 class="text-sm font-semibold text-gray-800 truncate">${notification.title || '系统通知'}</h4>
                                <span class="flex-shrink-0 text-xs text-gray-400">${notification.createTime || ''}</span>
                            </div>
                            <p class="text-sm text-gray-600 mt-1 line-clamp-2">${notification.content || ''}</p>
                            <div class="flex items-center mt-2 space-x-2">
                                <span class="px-2 py-0.5 rounded-full text-xs font-medium ${typeInfo.badgeColor}">${typeInfo.label}</span>
                                ${isUnread ? '<span class="px-2 py-0.5 bg-red-100 text-red-600 rounded-full text-xs font-medium">未读</span>' : ''}
                            </div>
                        </div>
                        ${isUnread ? '<span class="flex-shrink-0 w-2 h-2 bg-red-500 rounded-full mt-2"></span>' : ''}
                    </div>
                </div>
            `;
        });
        console.log(html);
        listContainer.innerHTML = html;

        // 绑定点击事件
        listContainer.querySelectorAll('.notification-item').forEach(function(item) {
            item.addEventListener('click', function() {
                const id = this.getAttribute('data-id');
                const title = this.getAttribute('data-title');
                const content = this.getAttribute('data-content');
                const type = this.getAttribute('data-type');
                const time = this.getAttribute('data-time');
                const isRead = this.getAttribute('data-read');

                showDetailModal(id, title, content, type, time);

                // 如果未读，标记为已读
                if (isRead === '0') {
                    markAsRead(id, this);
                }
            });
        });

        // 渲染分页
        renderPagination();
    }

    /**
     * 获取类型信息
     */
    function getTypeInfo(type) {
        const typeMap = {
            'leave_pending': {
                icon: 'fa-solid fa-clock',
                iconColor: 'text-orange-500',
                bgColor: 'bg-orange-100',
                badgeColor: 'bg-orange-100 text-orange-600',
                label: '待审批请假'
            },
            'supplement_pending': {
                icon: 'fa-solid fa-clock',
                iconColor: 'text-orange-500',
                bgColor: 'bg-orange-100',
                badgeColor: 'bg-orange-100 text-orange-600',
                label: '待审批补卡'
            },
            'leave_approve': {
                icon: 'fa-solid fa-calendar-check',
                iconColor: 'text-blue-500',
                bgColor: 'bg-blue-100',
                badgeColor: 'bg-blue-100 text-blue-600',
                label: '请假审批'
            },
            'leave_approved': {
                icon: 'fa-solid fa-check-circle',
                iconColor: 'text-green-500',
                bgColor: 'bg-green-100',
                badgeColor: 'bg-green-100 text-green-600',
                label: '请假已通过'
            },
            'leave_rejected': {
                icon: 'fa-solid fa-times-circle',
                iconColor: 'text-red-500',
                bgColor: 'bg-red-100',
                badgeColor: 'bg-red-100 text-red-600',
                label: '请假已驳回'
            },
            'supplement_approve': {
                icon: 'fa-solid fa-id-card',
                iconColor: 'text-purple-500',
                bgColor: 'bg-purple-100',
                badgeColor: 'bg-purple-100 text-purple-600',
                label: '补卡审批'
            },
            'supplement_approved': {
                icon: 'fa-solid fa-check-circle',
                iconColor: 'text-green-500',
                bgColor: 'bg-green-100',
                badgeColor: 'bg-green-100 text-green-600',
                label: '补卡已通过'
            },
            'supplement_rejected': {
                icon: 'fa-solid fa-times-circle',
                iconColor: 'text-red-500',
                bgColor: 'bg-red-100',
                badgeColor: 'bg-red-100 text-red-600',
                label: '补卡已驳回'
            },
            'promotion': {
                icon: 'fa-solid fa-arrow-up',
                iconColor: 'text-green-500',
                bgColor: 'bg-green-100',
                badgeColor: 'bg-green-100 text-green-600',
                label: '升职通知'
            },
            'demotion': {
                icon: 'fa-solid fa-exchange-alt',
                iconColor: 'text-orange-500',
                bgColor: 'bg-orange-100',
                badgeColor: 'bg-orange-100 text-orange-600',
                label: '职位变更'
            }
        };

        return typeMap[type] || {
            icon: 'fa-solid fa-bell',
            iconColor: 'text-gray-500',
            bgColor: 'bg-gray-100',
            badgeColor: 'bg-gray-100 text-gray-600',
            label: '系统消息'
        };
    }

    /**
     * 显示详情模态框
     */
    function showDetailModal(id, title, content, type, time) {
        const modal = document.getElementById('notification-detail-modal');
        const typeInfo = getTypeInfo(type);

        // 保存当前通知信息
        currentNotification.id = id;
        currentNotification.type = type;

        document.getElementById('modal-title').textContent = title || '通知详情';
        document.getElementById('modal-content').textContent = content || '无内容';
        document.getElementById('modal-time').textContent = '发送时间：' + (time || '未知');

        const badge = document.getElementById('modal-type-badge');
        badge.className = 'px-3 py-1 rounded-full text-sm font-medium ' + typeInfo.badgeColor;
        badge.textContent = typeInfo.label;

        // 根据通知类型决定是否显示"查看详情"按钮
        const viewBtn = document.getElementById('modal-view-btn');
        if (viewBtn) {
            const hasDetailPage = ['leave_pending', 'leave_approve', 'leave_approved', 'leave_rejected',
                                   'supplement_pending', 'supplement_approve', 'supplement_approved', 'supplement_rejected',
                                   'promotion', 'demotion'].includes(type);
            if (hasDetailPage) {
                viewBtn.classList.remove('hidden');
            } else {
                viewBtn.classList.add('hidden');
            }
        }

        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    /**
     * 关闭详情模态框
     */
    function closeDetailModal() {
        const modal = document.getElementById('notification-detail-modal');
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }

    /**
     * 标记单条为已读
     */
    function markAsRead(id, element) {
        app.post('/reception/notification/read.do', { notificationId: id }, function() {
            // 更新UI
            if (element) {
                element.classList.remove('unread');
                element.setAttribute('data-read', '1');
                const unreadBadge = element.querySelector('.bg-red-100.text-red-600');
                if (unreadBadge) unreadBadge.remove();
                const redDot = element.querySelector('.bg-red-500.rounded-full');
                if (redDot) redDot.remove();
            }
            updateUnreadBadge();
        }, function() {
            // 尝试使用Home接口
            app.post('/reception/home/notification/read.do', { notificationId: id }, function() {
                if (element) {
                    element.classList.remove('unread');
                    element.setAttribute('data-read', '1');
                    const unreadBadge = element.querySelector('.bg-red-100.text-red-600');
                    if (unreadBadge) unreadBadge.remove();
                    const redDot = element.querySelector('.bg-red-500.rounded-full');
                    if (redDot) redDot.remove();
                }
                updateUnreadBadge();
            });
        });
    }

    /**
     * 全部标记为已读
     */
    function markAllAsRead() {
        app.post('/reception/notification/readAll.do', {}, function() {
            app.showToast('已全部标记为已读', 'success');
            loadNotifications();
        }, function() {
            // 尝试使用Home接口
            app.post('/reception/home/notification/readAll.do', {}, function() {
                app.showToast('已全部标记为已读', 'success');
                loadNotifications();
            });
        });
    }

    /**
     * 更新未读徽章
     */
    function updateUnreadBadge() {
        app.get('/reception/notification/unread/count.do', function(result) {
            if (result.data && result.data.unreadCount !== undefined) {
                updateUnreadBadgeValue(result.data.unreadCount);
            }
        }, function() {
            // 尝试使用Home接口
            app.get('/reception/home/notifications.do?pageSize=1', function(result) {
                if (result.data && result.data.unreadCount !== undefined) {
                    updateUnreadBadgeValue(result.data.unreadCount);
                }
            });
        });
    }

    /**
     * 更新未读徽章值
     */
    function updateUnreadBadgeValue(count) {
        const badge = document.getElementById('unread-badge');
        const countSpan = document.getElementById('unread-count');
        if (badge && countSpan) {
            if (count > 0) {
                countSpan.textContent = count;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }
    }

    /**
     * 删除通知
     */
    function deleteNotification(id) {
        if (!confirm('确定要删除这条通知吗？')) {
            return;
        }

        app.post('/reception/notification/delete.do', { notificationId: id }, function(result) {
            app.showToast('删除成功', 'success');
            closeDetailModal();
            loadNotifications();
        }, function() {
            app.showToast('删除失败', 'error');
        });
    }

    /**
     * 根据通知类型跳转到相关页面
     */
    function navigateToDetail(type) {
        let url = '';
        const baseUrl = window.baseUrl || '';

        switch (type) {
            case 'leave_pending':
            case 'leave_approve':
            case 'leave_approved':
            case 'leave_rejected':
                // 跳转到个人中心的请假记录页
                url = baseUrl + '/reception/page/center.do?tab=leave';
                break;
            case 'supplement_pending':
            case 'supplement_approve':
            case 'supplement_approved':
            case 'supplement_rejected':
                // 跳转到个人中心的补卡记录页
                url = baseUrl + '/reception/page/center.do?tab=supplement';
                break;
            case 'promotion':
            case 'demotion':
                // 跳转到个人中心
                url = baseUrl + '/reception/page/center.do';
                break;
            default:
                return;
        }

        if (url) {
            closeDetailModal();
            window.location.href = url;
        }
    }

    /**
     * 渲染分页
     */
    function renderPagination() {
        const totalPages = Math.ceil(totalCount / pageSize);
        if (totalPages <= 1) {
            document.getElementById('notification-pagination').innerHTML = '';
            return;
        }
        console.log("页数："+totalPages);

        app.renderPagination('notification-pagination', currentPage, totalPages, function(page) {
            currentPage = page;
            loadNotifications();
        });
    }

    /**
     * HTML转义
     */
    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

})();
