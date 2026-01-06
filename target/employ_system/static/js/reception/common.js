/**
 * 前台员工模块公共工具库
 * @description 封装前端通用AJAX、消息提示、模态框、分页、导航栏等工具函数
 * @author 开发团队
 * @version 2.0.1
 * @dependency TailwindCSS、FontAwesome 6.4.0
 * @note 需在全局挂载window.baseUrl（项目上下文路径，如：/employ_system_2）
 */

// 全局配置命名空间
window.ReceptionApp = window.ReceptionApp || {};

(function(app) {
    'use strict';

    // ============================================================
    // 基础配置
    // ============================================================

    /** 基础URL（项目上下文路径） */
    app.baseUrl = window.baseUrl || '';

    /** 当前登录用户信息对象 */
    app.currentUser = null;

    // ============================================================
    // 核心AJAX请求方法（与admin的ajaxRequest保持一致）
    // ============================================================

    /**
     * 通用AJAX请求封装（核心方法）
     * @description 基于原生XMLHttpRequest封装，统一处理请求路径、参数、错误、JSON解析
     * @param {string} url - 接口相对路径（示例：/reception/home/summary.do）
     * @param {string} method - HTTP请求方法（GET/POST）
     * @param {Object} [data=null] - 请求参数对象（键值对形式）
     * @param {Function} [successCallback=null] - 请求成功回调函数，参数为解析后的JSON响应体
     * @param {Function} [errorCallback=null] - 请求失败回调函数，参数为{msg: 错误提示语}
     * @example
     * ajaxRequest('/reception/home/summary.do', 'GET', null, function(result) {
     *     console.log('成功:', result);
     * }, function(error) {
     *     console.log('失败:', error.msg);
     * });
     */
    function ajaxRequest(url, method, data, successCallback, errorCallback) {
        // 初始化请求配置
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded' // 表单编码格式
            },
            credentials: 'include' // 跨域/同域请求携带Cookie
        };

        // 核心：拼接完整URL（上下文路径+接口路径，避免重复斜杠）
        let fullUrl = app.baseUrl + url;

        // 处理GET请求参数：拼接至URL末尾
        if (method === 'GET' && data) {
            const params = new URLSearchParams(data);
            fullUrl += (fullUrl.indexOf('?') === -1 ? '?' : '&') + params.toString();
        }

        // 创建XHR对象并配置
        const xhr = new XMLHttpRequest();
        xhr.overrideMimeType('application/json; charset=UTF-8'); // 强制指定响应编码为UTF-8
        xhr.open(method, fullUrl, true); // 异步请求

        // 设置请求头
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

        // 监听请求状态变化
        xhr.onreadystatechange = function() {
            // 仅处理请求完成状态（readyState=4）
            if (xhr.readyState === 4) {
                // 状态码200表示请求成功
                if (xhr.status === 200) {
                    try {
                        // 解析JSON响应体
                        const result = JSON.parse(xhr.responseText);

                        // 检查业务状态码
                        if (result.code === 200 || result.code === '200') {
                            // 业务成功，执行成功回调
                            if (successCallback) successCallback(result);
                        } else if (result.code === 401 || result.code === '401') {
                            // 未登录或登录过期，跳转登录页
                            app.showToast('登录已过期，请重新登录', 'warning');
                            setTimeout(function() {
                                window.location.href = app.baseUrl + '/reception/page/login.do';
                            }, 1500);
                        } else {
                            // 业务失败，执行错误回调
                            if (errorCallback) errorCallback(result);
                            if (result.msg) {
                                app.showToast(result.msg, 'error');
                            }
                        }
                    } catch (e) {
                        // JSON解析失败的错误处理
                        console.error('JSON解析失败：', e, '响应内容：', xhr.responseText);
                        if (errorCallback) errorCallback({msg: '数据解析失败'});
                    }
                } else {
                    // HTTP状态码非200的错误处理
                    console.error('请求失败：', xhr.status, fullUrl);
                    if (errorCallback) errorCallback({msg: '网络错误(' + xhr.status + ')，请稍后重试'});
                }
            }
        };

        // 监听网络层异常（如断网、跨域限制）
        xhr.onerror = function() {
            console.error('网络异常：', fullUrl);
            if (errorCallback) errorCallback({msg: '网络连接异常'});
            app.showToast('网络错误，请稍后重试', 'error');
        };

        // 发送请求：POST请求携带表单参数，GET请求无body
        if (method === 'POST' && data) {
            const formData = new URLSearchParams();
            for (const key in data) {
                if (data.hasOwnProperty(key) && data[key] !== null && data[key] !== undefined) {
                    formData.append(key, data[key]);
                }
            }
            xhr.send(formData);
        } else {
            xhr.send(null);
        }
    }

    // 将ajaxRequest挂载到app命名空间，便于外部调用
    app.ajaxRequest = ajaxRequest;

    // ============================================================
    // 便捷AJAX方法（基于ajaxRequest封装）
    // ============================================================

    /**
     * GET请求快捷方法
     * @param {string} url - 请求URL（可包含查询参数）
     * @param {Function} [success] - 成功回调
     * @param {Function} [error] - 失败回调
     * @example
     * app.get('/reception/home/summary.do', function(result) {
     *     console.log(result.data);
     * });
     */
    app.get = function(url, success, error) {
        ajaxRequest(url, 'GET', null, success, error || function() {});
    };

    /**
     * POST请求快捷方法
     * @param {string} url - 请求URL
     * @param {Object} data - 请求参数对象
     * @param {Function} [success] - 成功回调
     * @param {Function} [error] - 失败回调
     * @example
     * app.post('/reception/home/punch.do', {type: 1}, function(result) {
     *     console.log('打卡成功');
     * });
     */
    app.post = function(url, data, success, error) {
        ajaxRequest(url, 'POST', data, success, error || function() {});
    };

    /**
     * 通用AJAX方法（兼容旧代码）
     * @param {Object} options - 请求配置对象
     * @param {string} options.url - 请求URL
     * @param {string} [options.method='GET'] - 请求方法
     * @param {Object} [options.data] - 请求参数
     * @param {Function} [options.success] - 成功回调
     * @param {Function} [options.error] - 失败回调
     * @deprecated 建议使用app.get()或app.post()
     */
    app.ajax = function(options) {
        const method = options.method || 'GET';
        const url = options.url || '';
        const data = options.data || null;
        const success = options.success || function() {};
        const error = options.error || function() {};

        ajaxRequest(url, method, data, success, error);
    };

    // ============================================================
    // 应用初始化
    // ============================================================

    /**
     * 初始化应用
     * @description 获取当前用户信息，通常在页面加载时自动调用
     */
    app.init = function() {
        app.getCurrentUser();
    };

    /**
     * 获取当前登录用户信息
     * @param {Function} [callback] - 获取成功后的回调函数，参数为用户信息对象
     */
    app.getCurrentUser = function(callback) {
        app.get('/reception/login/current.do', function(result) {
            app.currentUser = result.data;
            if (callback) callback(result.data);
            // 触发用户信息加载完成事件，供其他模块监听
            document.dispatchEvent(new CustomEvent('userLoaded', { detail: result.data }));
        }, function() {
            // 未登录或获取失败
            app.currentUser = null;
        });
    };

    /**
     * 检查登录状态
     * @param {Function} [callback] - 回调函数，参数为(isLoggedIn, userData)
     */
    app.checkLogin = function(callback) {
        app.get('/reception/login/current.do', function(result) {
            if (callback) callback(true, result.data);
        }, function() {
            if (callback) callback(false, null);
            // 未登录则跳转到登录页
            window.location.href = app.baseUrl + '/reception/page/login.do';
        });
    };

    /**
     * 退出登录
     */
    app.logout = function() {
        app.get('/reception/logout.do', function() {
            window.location.href = app.baseUrl + '/reception/page/login.do';
        }, function() {
            // 即使请求失败也跳转到登录页
            window.location.href = app.baseUrl + '/reception/page/login.do';
        });
    };

    // ============================================================
    // 消息提示组件
    // ============================================================

    /**
     * 显示Toast提示消息
     * @param {string} message - 消息内容
     * @param {string} [type='info'] - 消息类型：success/error/warning/info
     * @param {number} [duration=3000] - 显示时长（毫秒）
     * @example
     * app.showToast('操作成功', 'success');
     * app.showToast('请填写必填项', 'warning');
     * app.showToast('服务器错误', 'error', 5000);
     */
    app.showToast = function(message, type, duration) {
        type = type || 'info';
        duration = duration || 3000;

        // 移除已存在的toast，确保只显示一个
        const existingToast = document.getElementById('app-toast');
        if (existingToast) {
            existingToast.remove();
        }

        // 图标映射
        const iconMap = {
            success: 'fa-check-circle',
            error: 'fa-times-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };

        // 背景色映射
        const colorMap = {
            success: 'bg-green-500',
            error: 'bg-red-500',
            warning: 'bg-yellow-500',
            info: 'bg-blue-500'
        };

        // 创建Toast DOM元素
        const toast = document.createElement('div');
        toast.id = 'app-toast';
        toast.className = 'fixed top-20 left-1/2 transform -translate-x-1/2 ' + colorMap[type] +
            ' text-white px-6 py-3 rounded-lg shadow-lg z-[9999] flex items-center space-x-2 transition-all duration-300 opacity-0';
        toast.innerHTML = '<i class="fa-solid ' + iconMap[type] + '"></i><span>' + message + '</span>';

        document.body.appendChild(toast);

        // 显示动画（淡入）
        setTimeout(function() {
            toast.classList.remove('opacity-0');
            toast.classList.add('opacity-100');
        }, 10);

        // 自动隐藏（淡出后移除）
        setTimeout(function() {
            toast.classList.remove('opacity-100');
            toast.classList.add('opacity-0');
            setTimeout(function() {
                toast.remove();
            }, 300);
        }, duration);
    };

    /**
     * 显示确认对话框
     * @param {string} title - 对话框标题
     * @param {string} message - 对话框内容
     * @param {Function} [onConfirm] - 点击确认按钮的回调
     * @param {Function} [onCancel] - 点击取消按钮的回调
     * @example
     * app.showConfirm('确认删除', '确定要删除这条记录吗？', function() {
     *     // 执行删除操作
     * });
     */
    app.showConfirm = function(title, message, onConfirm, onCancel) {
        // 移除已存在的对话框
        const existingModal = document.getElementById('app-confirm-modal');
        if (existingModal) {
            existingModal.remove();
        }

        // 创建确认对话框DOM
        const modal = document.createElement('div');
        modal.id = 'app-confirm-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML =
            '<div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 transform transition-all">' +
            '<div class="p-6">' +
            '<h3 class="text-lg font-bold text-gray-800 mb-2">' + title + '</h3>' +
            '<p class="text-gray-600">' + message + '</p>' +
            '</div>' +
            '<div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">' +
            '<button id="confirm-cancel" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>' +
            '<button id="confirm-ok" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">确定</button>' +
            '</div>' +
            '</div>';

        document.body.appendChild(modal);

        // 绑定确认按钮事件
        modal.querySelector('#confirm-ok').addEventListener('click', function() {
            modal.remove();
            if (onConfirm) onConfirm();
        });

        // 绑定取消按钮事件
        modal.querySelector('#confirm-cancel').addEventListener('click', function() {
            modal.remove();
            if (onCancel) onCancel();
        });

        // 点击遮罩层关闭
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
                if (onCancel) onCancel();
            }
        });
    };

    // ============================================================
    // 模态框工具方法
    // ============================================================

    /**
     * 显示模态框
     * @param {string} modalId - 模态框元素ID
     */
    app.showModal = function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('hidden');
            modal.classList.add('flex');
            document.body.style.overflow = 'hidden'; // 禁止页面滚动
        }
    };

    /**
     * 隐藏模态框
     * @param {string} modalId - 模态框元素ID
     */
    app.hideModal = function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
            document.body.style.overflow = ''; // 恢复页面滚动
        }
    };

    // ============================================================
    // 工具方法
    // ============================================================

    /**
     * 格式化日期
     * @param {Date|string|number} date - 日期（Date对象/日期字符串/时间戳）
     * @param {string} [format='yyyy-MM-dd'] - 格式模板
     * @returns {string} 格式化后的日期字符串
     * @example
     * app.formatDate(new Date(), 'yyyy-MM-dd HH:mm:ss'); // "2025-12-30 10:30:00"
     * app.formatDate('2025-12-30', 'yyyy年MM月dd日'); // "2025年12月30日"
     */
    app.formatDate = function(date, format) {
        if (!date) return '';
        if (typeof date === 'string') {
            date = new Date(date);
        }
        format = format || 'yyyy-MM-dd';

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');

        return format
            .replace('yyyy', year)
            .replace('MM', month)
            .replace('dd', day)
            .replace('HH', hours)
            .replace('mm', minutes)
            .replace('ss', seconds);
    };

    /**
     * 获取状态标签HTML
     * @param {number} status - 状态值
     * @param {string} type - 类型：approve(审批状态)/attendance(考勤状态)
     * @returns {string} 状态标签HTML字符串
     * @example
     * app.getStatusBadge(0, 'approve');    // 返回"待审批"标签
     * app.getStatusBadge(1, 'attendance'); // 返回"迟到"标签
     */
    app.getStatusBadge = function(status, type) {
        if (type === 'approve') {
            // 审批状态：0-待审批，1-已通过，2-已驳回
            switch (status) {
                case 0:
                    return '<span class="bg-warning/10 text-warning text-xs px-2 py-1 rounded-full">待审批</span>';
                case 1:
                    return '<span class="bg-success/10 text-success text-xs px-2 py-1 rounded-full">已通过</span>';
                case 2:
                    return '<span class="bg-danger/10 text-danger text-xs px-2 py-1 rounded-full">已驳回</span>';
                default:
                    return '<span class="bg-gray-100 text-gray-600 text-xs px-2 py-1 rounded-full">未知</span>';
            }
        } else if (type === 'attendance') {
            // 考勤状态：0-正常，1-迟到，2-早退，3-缺勤
            switch (status) {
                case 0:
                    return '<span class="bg-success/10 text-success text-xs px-2 py-1 rounded-full">正常</span>';
                case 1:
                    return '<span class="bg-warning/10 text-warning text-xs px-2 py-1 rounded-full">迟到</span>';
                case 2:
                    return '<span class="bg-danger/10 text-danger text-xs px-2 py-1 rounded-full">早退</span>';
                case 3:
                    return '<span class="bg-danger/10 text-danger text-xs px-2 py-1 rounded-full">缺勤</span>';
                default:
                    return '<span class="bg-gray-100 text-gray-600 text-xs px-2 py-1 rounded-full">未知</span>';
            }
        }
        return '';
    };

    // ============================================================
    // 分页组件
    // ============================================================

    /**
     * 渲染分页组件
     * @param {string} containerId - 分页容器元素ID
     * @param {number} current - 当前页码
     * @param {number} total - 总页数
     * @param {Function} onClick - 页码点击回调，参数为页码
     * @example
     * app.renderPagination('pagination-container', 1, 10, function(page) {
     *     loadData(page);
     * });
     */
    app.renderPagination = function(containerId, current, total, onClick) {
        const container = document.getElementById(containerId);
        if (!container) return;

        // 无数据或只有一页时不显示分页
        if (total <= 1) {
            container.innerHTML = '';
            return;
        }

        let html = '<div class="flex items-center justify-center space-x-2">';

        // 上一页按钮
        if (current > 1) {
            html += '<button class="pagination-btn px-3 py-1 rounded-lg bg-neutral hover:bg-primary/10 text-secondary" data-page="' + (current - 1) + '">上一页</button>';
        }

        // 计算显示的页码范围（当前页前后各2页）
        const startPage = Math.max(1, current - 2);
        const endPage = Math.min(total, current + 2);

        // 第一页及省略号
        if (startPage > 1) {
            html += '<button class="pagination-btn px-3 py-1 rounded-lg bg-neutral hover:bg-primary/10 text-secondary" data-page="1">1</button>';
            if (startPage > 2) {
                html += '<span class="px-2 text-secondary">...</span>';
            }
        }

        // 页码按钮
        for (var i = startPage; i <= endPage; i++) {
            var isActive = i === current;
            html += '<button class="pagination-btn px-3 py-1 rounded-lg ' +
                (isActive ? 'bg-primary text-white' : 'bg-neutral hover:bg-primary/10 text-secondary') +
                '" data-page="' + i + '">' + i + '</button>';
        }

        // 最后一页及省略号
        if (endPage < total) {
            if (endPage < total - 1) {
                html += '<span class="px-2 text-secondary">...</span>';
            }
            html += '<button class="pagination-btn px-3 py-1 rounded-lg bg-neutral hover:bg-primary/10 text-secondary" data-page="' + total + '">' + total + '</button>';
        }

        // 下一页按钮
        if (current < total) {
            html += '<button class="pagination-btn px-3 py-1 rounded-lg bg-neutral hover:bg-primary/10 text-secondary" data-page="' + (current + 1) + '">下一页</button>';
        }

        html += '</div>';
        container.innerHTML = html;

        // 绑定页码点击事件
        container.querySelectorAll('.pagination-btn').forEach(function(btn) {
            btn.addEventListener('click', function() {
                var page = parseInt(this.getAttribute('data-page'));
                if (onClick) onClick(page);
            });
        });
    };

    // ============================================================
    // 头像上传核心功能（新增/改造）
    // ============================================================

    /**
     * 预览并上传头像（核心方法）
     * @param {HTMLInputElement} input - 文件选择框DOM元素
     */
    app.previewAndUploadAvatar = function(input) {
        if (!input.files || !input.files[0]) {
            return;
        }
        const file = input.files[0];

        // 1. 验证文件类型
        if (!file.type.match('image.*')) {
            app.showToast('请选择有效的图片文件', 'error');
            input.value = ''; // 清空选择
            return;
        }

        // 2. 验证文件大小（2MB限制）
        const maxSize = 2 * 1024 * 1024;
        if (file.size > maxSize) {
            app.showToast('图片大小不能超过2MB', 'error');
            input.value = ''; // 清空选择
            return;
        }

        // 3. 预览图片（即时显示）
        const reader = new FileReader();
        reader.onload = function(e) {
            // 更新头像预览（导航栏头像 + 模态框预览头像）
            const navAvatar = document.getElementById('nav-avatar');
            const avatarPreview = document.getElementById('avatar-preview-modal') ? document.getElementById('avatar-preview-modal').querySelector('img') : null;
            if (navAvatar) {
                navAvatar.src = e.target.result;
            }
            if (avatarPreview) {
                avatarPreview.src = e.target.result;
            }
        };
        reader.readAsDataURL(file);

        // 4. 构建FormData（文件上传专用）
        const formData = new FormData();
        formData.append('file', file);

        // 5. 发送文件上传AJAX请求（使用XMLHttpRequest专门处理FormData）
        const xhr = new XMLHttpRequest();
        const uploadUrl = app.baseUrl + '/admin/profile/uploadAvatar.do'; // 前台头像上传接口
        xhr.open('POST', uploadUrl, true);
        xhr.withCredentials = true; // 携带Cookie

        // 上传成功回调
        xhr.onload = function() {
            if (xhr.status === 200) {
                try {
                    const result = JSON.parse(xhr.responseText);
                    if (result.code === 200 || result.code === '200') {
                        app.showToast('头像上传成功', 'success');
                        // 更新当前用户信息中的头像路径
                        if (app.currentUser) {
                            app.currentUser.avatar = result.avatar;
                        }
                        // 清空文件选择框
                        input.value = '';
                        // 隐藏头像预览模态框（如果存在）
                        const avatarModal = document.getElementById('avatar-preview-modal');
                        if (avatarModal) {
                            avatarModal.remove();
                        }
                    } else {
                        app.showToast(result.msg || '头像上传失败', 'error');
                        // 恢复原有头像
                        const originalAvatar = app.currentUser && app.currentUser.avatar ? (app.baseUrl + app.currentUser.avatar) : 'https://via.placeholder.com/40';
                        const navAvatar = document.getElementById('nav-avatar');
                        if (navAvatar) {
                            navAvatar.src = originalAvatar;
                        }
                        input.value = '';
                    }
                } catch (e) {
                    console.error('头像上传响应解析失败：', e);
                    app.showToast('头像上传响应异常', 'error');
                    input.value = '';
                }
            } else {
                console.error('头像上传请求失败，状态码：', xhr.status);
                app.showToast('网络错误，头像上传失败', 'error');
                // 恢复原有头像
                const originalAvatar = app.currentUser && app.currentUser.avatar ? (app.baseUrl + app.currentUser.avatar) : 'https://via.placeholder.com/40';
                const navAvatar = document.getElementById('nav-avatar');
                if (navAvatar) {
                    navAvatar.src = originalAvatar;
                }
                input.value = '';
            }
        };

        // 上传异常回调
        xhr.onerror = function() {
            console.error('头像上传网络异常');
            app.showToast('网络连接异常，头像上传失败', 'error');
            // 恢复原有头像
            const originalAvatar = app.currentUser && app.currentUser.avatar ? (app.baseUrl + app.currentUser.avatar) : 'https://via.placeholder.com/40';
            const navAvatar = document.getElementById('nav-avatar');
            if (navAvatar) {
                navAvatar.src = originalAvatar;
            }
            input.value = '';
        };

        // 发送FormData数据
        xhr.send(formData);
    };

    // ============================================================
    // 导航栏组件（改造：头像右下方添加上传按钮）
    // ============================================================

    /**
     * 渲染导航栏
     * @param {Object} user - 当前用户信息对象
     * @param {string} activePage - 当前激活的页面：index/center/workbench
     */
    // 前台公共工具库中 app.renderNavbar 函数的改造：
    app.renderNavbar = function(user, activePage) {
        const navbarContainer = document.getElementById('navbar-container');
        if (!navbarContainer) return;

        // 判断是否为部门主管
        const isManager = user && user.role === 'manager';
        const displayName = user ? user.empName : '用户';
        const displayRole = isManager ? (user.deptName + '主管') : '普通员工';
        const avatar = user && user.avatar ? (app.baseUrl + user.avatar) : 'https://via.placeholder.com/40';

        // 构建导航栏HTML（移除上传按钮，仅保留头像）
        var navHtml =
            '<header class="bg-white shadow-sm sticky top-0 z-50 transition-all duration-300">' +
            '<div class="container mx-auto px-4 md:px-6 py-3 flex items-center justify-between">' +
            // Logo区域（不变）
            '<div class="flex items-center space-x-2">' +
            '<div class="w-10 h-10 rounded-lg gradient-bg flex items-center justify-center">' +
            '<i class="fa-solid fa-clock text-white text-xl"></i>' +
            '</div>' +
            '<h1 class="text-xl md:text-2xl font-bold text-dark">考勤管理系统</h1>' +
            '</div>' +
            // 导航菜单（不变）
            '<nav class="flex items-center space-x-6">' +
            '<a href="' + app.baseUrl + '/reception/page/index.do" class="' + (activePage === 'index' ? 'text-primary font-medium' : 'text-secondary') + ' hover:text-primary transition-colors flex items-center">' +
            '<i class="fa-solid fa-house mr-1"></i>首页' +
            '</a>' +
            '<a href="' + app.baseUrl + '/reception/page/center.do" class="' + (activePage === 'center' ? 'text-primary font-medium' : 'text-secondary') + ' hover:text-primary transition-colors flex items-center">' +
            '<i class="fa-solid fa-user mr-1"></i>个人中心' +
            '</a>' +
            (isManager ?
                '<a href="' + app.baseUrl + '/reception/page/workbench.do" class="' + (activePage === 'workbench' ? 'text-primary font-medium' : 'text-secondary') + ' hover:text-primary transition-colors flex items-center">' +
                '<i class="fa-solid fa-briefcase mr-1"></i>工作台' +
                '</a>' : '') +
            // 通知铃铛（不变）
            '<div class="relative notification-wrapper">' +
            '<button id="notification-btn" class="relative p-2 text-secondary hover:text-primary transition-colors">' +
            '<i class="fa-solid fa-bell text-xl"></i>' +
            '<span id="notification-badge" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center hidden">0</span>' +
            '</button>' +
            '<div id="notification-dropdown" class="hidden absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg z-50 max-h-96 overflow-hidden">' +
            '<div class="p-4 border-b border-gray-100 flex justify-between items-center">' +
            '<h4 class="font-semibold text-gray-800">通知消息</h4>' +
            '<button id="mark-all-read" class="text-sm text-primary hover:underline">全部已读</button>' +
            '</div>' +
            '<div id="navbar-notification-list" class="max-h-72 overflow-y-auto">' +
            '<p class="p-4 text-center text-gray-500">暂无通知</p>' +
            '</div>' +
            '<div class="p-3 border-t border-gray-100 text-center">' +
            '<a href="' + app.baseUrl + '/reception/page/notification.do" class="text-sm text-primary hover:underline">查看全部消息</a>' +
            '</div>' +
            '</div>' +
            '</div>' +
            // 用户头像下拉菜单（核心修改：移除上传按钮）
            '<div class="dropdown-hover relative">' +
            '<div class="flex items-center space-x-2 cursor-pointer py-1 px-2 rounded-full hover:bg-neutral transition-colors">' +
            // 头像容器：仅保留头像，删除上传按钮和input
            '<img src="' + avatar + '" alt="用户头像" class="w-10 h-10 rounded-full object-cover border-2 border-primary/20" id="nav-avatar">' +
            '<div class="hidden sm:block">' +
            '<span class="text-sm font-medium text-dark">' + displayRole + ':' + displayName + '</span>' +
            '</div>' +
            '<i class="fa-solid fa-chevron-down text-xs text-secondary"></i>' +
            '</div>' +
            '<div class="dropdown-menu hidden absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-card py-2 z-10">' +
            '<div class="px-4 py-3 border-b border-gray-100">' +
            '<div class="flex items-center space-x-3">' +
            '<img src="' + avatar + '" alt="头像" class="w-12 h-12 rounded-full object-cover">' +
            '<div>' +
            '<p class="font-medium text-gray-800">' + displayName + '</p>' +
            '<p class="text-sm text-gray-500">' + displayRole + '</p>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '<a href="javascript:void(0);" onclick="ReceptionApp.showAvatarPreview()" class="block px-4 py-2 text-sm text-gray-700 hover:bg-primary/5 hover:text-primary transition-colors">' +
            '<i class="fa-solid fa-image mr-2"></i>查看头像' +
            '</a>' +
            '<a href="' + app.baseUrl + '/reception/page/center.do" class="block px-4 py-2 text-sm text-gray-700 hover:bg-primary/5 hover:text-primary transition-colors">' +
            '<i class="fa-solid fa-user mr-2"></i>个人信息' +
            '</a>' +
            '<a href="javascript:void(0);" onclick="ReceptionApp.showPasswordModal()" class="block px-4 py-2 text-sm text-gray-700 hover:bg-primary/5 hover:text-primary transition-colors">' +
            '<i class="fa-solid fa-key mr-2"></i>修改密码' +
            '</a>' +
            '<div class="border-t border-gray-100 my-1"></div>' +
            '<a href="' + app.baseUrl + '/admin/page/login.do" class="block px-4 py-2 text-sm text-gray-700 hover:bg-primary/5 hover:text-primary transition-colors">' +
            '<i class="fa-solid fa-cog mr-2"></i>进入后台' +
            '</a>' +
            '<a href="javascript:void(0);" onclick="ReceptionApp.logout()" class="block px-4 py-2 text-sm text-red-500 hover:bg-red-50 transition-colors">' +
            '<i class="fa-solid fa-right-from-bracket mr-2"></i>退出登录' +
            '</a>' +
            '</div>' +
            '</div>' +
            '</nav>' +
            '</div>' +
            '</header>';

        navbarContainer.innerHTML = navHtml;
        app.initNotifications();
    };    // ============================================================
    // 通知功能
    // ============================================================

    /**
     * 初始化通知功能
     * @description 绑定通知按钮事件，加载未读数量
     */
    app.initNotifications = function() {
        const notificationBtn = document.getElementById('notification-btn');
        const notificationDropdown = document.getElementById('notification-dropdown');

        if (notificationBtn && notificationDropdown) {
            // 点击铃铛切换下拉菜单
            notificationBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                notificationDropdown.classList.toggle('hidden');
                if (!notificationDropdown.classList.contains('hidden')) {
                    app.loadNotifications();
                }
            });

            // 点击页面其他区域关闭下拉菜单
            document.addEventListener('click', function(e) {
                if (!notificationDropdown.contains(e.target) && !notificationBtn.contains(e.target)) {
                    notificationDropdown.classList.add('hidden');
                }
            });

            // 全部已读按钮
            const markAllReadBtn = document.getElementById('mark-all-read');
            if (markAllReadBtn) {
                markAllReadBtn.addEventListener('click', function() {
                    app.markAllNotificationsRead();
                });
            }
        }

        // 加载未读通知数量
        app.loadUnreadCount();
    };

    /**
     * 加载未读通知数量
     */
    app.loadUnreadCount = function() {
        app.get('/reception/home/notifications.do?pageSize=1', function(result) {
            const badge = document.getElementById('notification-badge');
            if (badge && result.data) {
                const unreadCount = result.data.unreadCount || 0;
                if (unreadCount > 0) {
                    badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                    badge.classList.remove('hidden');
                } else {
                    badge.classList.add('hidden');
                }
            }
        });
    };

    /**
     * 加载通知列表
     */
    app.loadNotifications = function() {
        const notificationList = document.getElementById('navbar-notification-list');
        if (!notificationList) return;

        app.get('/reception/home/notifications.do?pageSize=10', function(result) {
            if (result.data && result.data.list && result.data.list.length > 0) {
                var html = '';
                result.data.list.forEach(function(notification) {
                    var isRead = notification.isRead === 1;

                    // 通知类型图标映射
                    var typeIconMap = {
                        'leave_pending': 'fa-clock text-orange-500',
                        'supplement_pending': 'fa-clock text-orange-500',
                        'leave_approved': 'fa-check-circle text-green-500',
                        'leave_rejected': 'fa-times-circle text-red-500',
                        'leave_approve': 'fa-check-circle text-green-500',
                        'supplement_approved': 'fa-check-circle text-green-500',
                        'supplement_rejected': 'fa-times-circle text-red-500',
                        'supplement_approve': 'fa-check-circle text-green-500',
                        'promotion': 'fa-arrow-up text-blue-500',
                        'demotion': 'fa-exchange-alt text-yellow-500',
                        'system': 'fa-info-circle text-blue-500'
                    };
                    var icon = typeIconMap[notification.type] || 'fa-bell text-gray-500';

                    html +=
                        '<div class="notification-item px-4 py-3 border-b border-gray-100 hover:bg-gray-50 cursor-pointer ' + (isRead ? 'opacity-60' : '') + '" data-id="' + notification.notificationId + '" data-type="' + (notification.type || '') + '">' +
                        '<div class="flex items-start space-x-3">' +
                        '<i class="fa-solid ' + icon + ' mt-1"></i>' +
                        '<div class="flex-1">' +
                        '<p class="text-sm font-medium text-gray-800">' + notification.title + '</p>' +
                        '<p class="text-xs text-gray-500 mt-1">' + (notification.content || '') + '</p>' +
                        '<p class="text-xs text-gray-400 mt-1">' + (notification.createTime || '') + '</p>' +
                        '</div>' +
                        (!isRead ? '<span class="w-2 h-2 bg-red-500 rounded-full"></span>' : '') +
                        '</div>' +
                        '</div>';
                });
                notificationList.innerHTML = html;

                // 绑定通知项点击事件
                notificationList.querySelectorAll('.notification-item').forEach(function(item) {
                    item.addEventListener('click', function() {
                        var id = this.getAttribute('data-id');
                        var type = this.getAttribute('data-type');
                        app.markNotificationRead(id);
                        app.navigateByNotificationType(type);
                    });
                });
            } else {
                notificationList.innerHTML = '<p class="p-4 text-center text-gray-500">暂无通知</p>';
            }
        });
    };

    /**
     * 标记单条通知为已读
     * @param {number|string} notificationId - 通知ID
     */
    app.markNotificationRead = function(notificationId) {
        app.post('/reception/home/notification/read.do', { notificationId: notificationId }, function() {
            app.loadNotifications();
            app.loadUnreadCount();
        });
    };

    /**
     * 全部标记为已读
     */
    app.markAllNotificationsRead = function() {
        app.post('/reception/home/notification/readAll.do', {}, function() {
            app.loadNotifications();
            app.loadUnreadCount();
            app.showToast('已全部标记为已读', 'success');
        });
    };

    /**
     * 根据通知类型跳转到相关页面
     * @param {string} type - 通知类型
     */
    app.navigateByNotificationType = function(type) {
        var url = '';

        switch (type) {
            case 'leave_pending':
            case 'leave_approve':
            case 'leave_approved':
            case 'leave_rejected':
                // 跳转到个人中心的请假记录页
                url = app.baseUrl + '/reception/page/center.do?tab=leave';
                break;
            case 'supplement_pending':
            case 'supplement_approve':
            case 'supplement_approved':
            case 'supplement_rejected':
                // 跳转到个人中心的补卡记录页
                url = app.baseUrl + '/reception/page/center.do?tab=supplement';
                break;
            case 'promotion':
            case 'demotion':
                // 跳转到个人中心
                url = app.baseUrl + '/reception/page/center.do';
                break;
            default:
                // 跳转到消息通知页面
                url = app.baseUrl + '/reception/page/notification.do';
                break;
        }

        if (url) {
            window.location.href = url;
        }
    };

    // ============================================================
    // 用户操作模态框
    // ============================================================

    /**
     * 显示头像预览
     */
    app.showAvatarPreview = function() {
        var avatar = app.currentUser && app.currentUser.avatar ? (app.baseUrl + app.currentUser.avatar) : 'https://via.placeholder.com/200';

        var modal = document.createElement('div');
        modal.id = 'avatar-preview-modal';
        modal.className = 'fixed inset-0 bg-black/70 flex items-center justify-center z-[9999]';
        modal.innerHTML =
            '<div class="relative">' +
            '<img src="' + avatar + '" alt="头像预览" class="max-w-[300px] max-h-[300px] rounded-lg shadow-xl">' +
            '<button class="absolute -top-3 -right-3 w-8 h-8 bg-white rounded-full shadow-lg flex items-center justify-center text-gray-600 hover:text-gray-800" onclick="this.closest(\'#avatar-preview-modal\').remove()">' +
            '<i class="fa-solid fa-times"></i>' +
            '</button>' +
            '</div>';

        // 点击遮罩层关闭
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });

        document.body.appendChild(modal);
    };

    /**
     * 显示修改密码模态框
     */
    app.showPasswordModal = function() {
        // 移除已存在的模态框
        var existingModal = document.getElementById('password-modal');
        if (existingModal) {
            existingModal.remove();
        }

        var modal = document.createElement('div');
        modal.id = 'password-modal';
        modal.className = 'fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]';
        modal.innerHTML =
            '<div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">' +
            '<div class="p-6 border-b border-gray-100">' +
            '<h3 class="text-lg font-bold text-gray-800">修改密码</h3>' +
            '</div>' +
            '<div class="p-6 space-y-4">' +
            '<div>' +
            '<label class="block text-sm font-medium text-gray-700 mb-1">原密码</label>' +
            '<input type="password" id="old-password" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入原密码">' +
            '</div>' +
            '<div>' +
            '<label class="block text-sm font-medium text-gray-700 mb-1">新密码</label>' +
            '<input type="password" id="new-password" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请输入新密码">' +
            '</div>' +
            '<div>' +
            '<label class="block text-sm font-medium text-gray-700 mb-1">确认密码</label>' +
            '<input type="password" id="confirm-password" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="请再次输入新密码">' +
            '</div>' +
            '</div>' +
            '<div class="px-6 py-4 bg-gray-50 rounded-b-xl flex justify-end space-x-3">' +
            '<button id="pwd-cancel" class="px-4 py-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors">取消</button>' +
            '<button id="pwd-submit" class="px-4 py-2 bg-primary text-white hover:bg-primary/90 rounded-lg transition-colors">确认修改</button>' +
            '</div>' +
            '</div>';

        document.body.appendChild(modal);

        // 绑定取消按钮
        modal.querySelector('#pwd-cancel').addEventListener('click', function() {
            modal.remove();
        });

        // 绑定提交按钮
        modal.querySelector('#pwd-submit').addEventListener('click', function() {
            app.changePassword();
        });

        // 点击遮罩层关闭
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });
    };

    /**
     * 执行密码修改
     */
    app.changePassword = function() {
        var oldPassword = document.getElementById('old-password').value.trim();
        var newPassword = document.getElementById('new-password').value.trim();
        var confirmPassword = document.getElementById('confirm-password').value.trim();

        // 表单验证
        if (!oldPassword) {
            app.showToast('请输入原密码', 'warning');
            return;
        }
        if (!newPassword) {
            app.showToast('请输入新密码', 'warning');
            return;
        }
        if (newPassword !== confirmPassword) {
            app.showToast('两次输入的密码不一致', 'warning');
            return;
        }

        // 先验证原密码
        app.post('/reception/center/password/verify.do', { password: oldPassword }, function() {
            // 验证成功，执行密码修改
            app.post('/reception/center/password/change.do', {
                newPassword: newPassword,
                confirmPassword: confirmPassword
            }, function() {
                app.showToast('密码修改成功，请重新登录', 'success');
                var modal = document.getElementById('password-modal');
                if (modal) modal.remove();
                // 延迟后退出登录
                setTimeout(function() {
                    app.logout();
                }, 1500);
            });
        }, function(result) {
            app.showToast(result.msg || '原密码错误', 'error');
        });
    };

})(window.ReceptionApp);

// ============================================================
// 页面加载完成后自动初始化
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    ReceptionApp.init();
});