/**
 * 前台登录页面脚本
 * @description 处理员工登录表单的验证、提交及记住密码功能
 * @author 开发团队
 * @version 1.0.0
 * @dependency 无外部依赖
 */

(function() {
    'use strict';

    // ============================================================
    // 全局变量
    // ============================================================

    /** 项目上下文路径 */
    const baseUrl = window.baseUrl || '';

    // ============================================================
    // 初始化入口
    // ============================================================

    /**
     * 页面加载完成后初始化
     * @description 检查是否在登录页面，若是则初始化记住密码功能和绑定事件
     */
    document.addEventListener('DOMContentLoaded', function() {
        // 检查是否在登录页面（存在登录表单元素）
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');

        if (!usernameInput || !passwordInput) {
            // 不在登录页面，不执行登录相关逻辑
            return;
        }

        // 初始化记住密码功能
        initRememberPwd();

        // 绑定页面事件
        bindEvents();
    });

    // ============================================================
    // 记住密码功能
    // ============================================================

    /**
     * 初始化记住密码
     * @description 从localStorage读取保存的账号密码，自动填充到表单
     */
    function initRememberPwd() {
        // 从localStorage读取保存的状态和凭证
        const rememberPwd = localStorage.getItem('reception_rememberPwd');
        const savedUsername = localStorage.getItem('reception_username');
        const savedPassword = localStorage.getItem('reception_password');

        // 获取表单元素
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');
        const rememberCheckbox = document.getElementById('rememberPwd');

        // 如果已勾选记住密码且存在保存的凭证，自动填充
        if (rememberPwd === 'true' && savedUsername && savedPassword) {
            if (usernameInput) usernameInput.value = savedUsername;
            if (passwordInput) passwordInput.value = savedPassword;
            if (rememberCheckbox) rememberCheckbox.checked = true;
        }
    }

    // ============================================================
    // 事件绑定
    // ============================================================

    /**
     * 绑定页面事件
     * @description 绑定登录按钮点击、回车提交、忘记密码等事件
     */
    function bindEvents() {
        // 登录按钮点击事件
        const loginBtn = document.getElementById('loginBtn');
        if (loginBtn) {
            loginBtn.addEventListener('click', login);
        }

        // 回车键提交表单
        document.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                const usernameInput = document.getElementById('username');
                if (usernameInput) {
                    login();
                }
            }
        });

        // 忘记密码链接点击事件
        const forgotPwd = document.getElementById('forgotPwd');
        if (forgotPwd) {
            forgotPwd.addEventListener('click', function() {
                alert('请联系管理员重置密码！');
            });
        }
    }

    // ============================================================
    // 表单验证
    // ============================================================

    /**
     * 验证登录表单
     * @description 检查用户名和密码是否已填写
     * @returns {boolean} 表单是否有效
     */
    function validateForm() {
        let isValid = true;

        // 获取表单元素
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');
        const usernameError = document.getElementById('usernameError');
        const passwordError = document.getElementById('passwordError');

        if (!usernameInput || !passwordInput) {
            return false;
        }

        // 获取输入值并去除首尾空格
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        // 清除之前的错误提示
        if (usernameError) usernameError.classList.remove('show');
        if (passwordError) passwordError.classList.remove('show');

        // 验证用户名
        if (!username) {
            if (usernameError) {
                usernameError.textContent = '请输入用户名';
                usernameError.classList.add('show');
            }
            isValid = false;
        }

        // 验证密码
        if (!password) {
            if (passwordError) {
                passwordError.textContent = '请输入密码';
                passwordError.classList.add('show');
            }
            isValid = false;
        }

        return isValid;
    }

    // ============================================================
    // 登录提交
    // ============================================================

    /**
     * 执行登录操作
     * @description 验证表单后提交登录请求，成功后跳转到首页
     */
    function login() {
        // 先验证表单
        if (!validateForm()) {
            return;
        }

        // 获取表单元素
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');
        const rememberCheckbox = document.getElementById('rememberPwd');
        const loginBtn = document.getElementById('loginBtn');
        const loginLoader = document.getElementById('loginLoader');

        if (!usernameInput || !passwordInput) {
            return;
        }

        // 获取表单值
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        const rememberPwd = rememberCheckbox ? rememberCheckbox.checked : false;

        // 禁用登录按钮，显示加载动画
        if (loginBtn) loginBtn.disabled = true;
        if (loginLoader) loginLoader.classList.add('show');

        // 构建请求参数
        const formData = new URLSearchParams();
        formData.append('username', username);
        formData.append('password', password);

        // 发送登录请求
        fetch(baseUrl + '/reception/login.do', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(result) {
            if (result.code === 200 || result.code === '200') {
                // 登录成功，处理记住密码
                if (rememberPwd) {
                    // 保存凭证到localStorage
                    localStorage.setItem('reception_rememberPwd', 'true');
                    localStorage.setItem('reception_username', username);
                    localStorage.setItem('reception_password', password);
                } else {
                    // 清除之前保存的凭证
                    localStorage.removeItem('reception_rememberPwd');
                    localStorage.removeItem('reception_username');
                    localStorage.removeItem('reception_password');
                }

                // 跳转到员工首页
                window.location.href = baseUrl + '/reception/page/index.do';
            } else {
                // 登录失败，显示错误信息
                console.log("账号或密码错误！！！");
                var passwordError = document.getElementById('passwordError');
                if (passwordError) {
                    passwordError.textContent = result.msg || '登录失败';
                    passwordError.classList.add('show');
                }

                // 恢复登录按钮状态
                if (loginBtn) loginBtn.disabled = false;
                if (loginLoader) loginLoader.classList.remove('show');
            }
        })
        .catch(function(error) {
            // 网络错误处理
            console.error('登录请求失败:', error);
            var passwordError = document.getElementById('passwordError');
            if (passwordError) {
                passwordError.textContent = '网络错误，请稍后重试';
                passwordError.classList.add('show');
            }

            // 恢复登录按钮状态
            if (loginBtn) loginBtn.disabled = false;
            if (loginLoader) loginLoader.classList.remove('show');
        });
    }

})();
