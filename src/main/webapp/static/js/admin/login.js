// 登录表单提交事件
document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault(); // 阻止默认提交

    // 获取表单数据
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    // 基础校验
    if (!username) {
        showMessage('请输入账号', 'warning');
        return;
    }
    if (!password) {
        showMessage('请输入密码', 'warning');
        return;
    }

    // 禁用按钮防止重复提交
    const loginBtn = document.getElementById('loginBtn');
    loginBtn.disabled = true;
    loginBtn.innerText = '登录中...';

    // 调用通用AJAX登录
    ajaxRequest(
        "/admin/login1.do",
        'POST',
        { username, password},
        function(result) {
            loginBtn.disabled = false;
            loginBtn.innerText = '登录';

            if (result.code === 200 || result.code === '200') {
                showMessage('登录成功，即将进入考勤管理系统', 'success');
                setTimeout(() => {
                    window.location.href = window.baseUrl + '/admin/page/dashboard.do';
                }, 2000);
            } else {
                console.log(result);
                showMessage(result.msg || '登录失败，请检查账号密码', 'error');
            }
        },
        function(error) {
            loginBtn.disabled = false;
            loginBtn.innerText = '登录';
            showMessage(error.msg || '网络异常，登录失败', 'error');
        }
    );
});