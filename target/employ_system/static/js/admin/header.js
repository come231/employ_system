/**
 * 顶部标题栏专属JS - 用户信息渲染/下拉事件/侧边栏折叠
 * 依赖：common.js（ajaxRequest/showMessage/formatDate/checkLogin/logout等工具函数）
 */

/**
 * 加载并渲染用户信息到页面
 */
function loadAndRenderUserInfo() {
    ajaxRequest(
        '/admin/login/current.do', // 获取用户信息接口
        'GET',
        null,
        (result) => {
            if (result.code === 200) {
                // 渲染用户信息到页面
                renderUserInfo(result);
            } else {
                showMessage(result.msg || '获取用户信息失败', 'error');
                // 信息获取失败，退出登录
                logoutEvent();
            }
        },
        (error) => {
            showMessage(error.msg || '网络异常，无法获取用户信息', 'error');
            logoutEvent();
        }
    );
}

/**
 * 渲染用户信息到页面右上角（核心：头像+角色+用户名+登录时间）
 * @param {Object} userInfo 用户信息对象
 */
function renderUserInfo(userInfo) {
    try {
        const userInfoContainer = document.getElementById('userInfo');
        userInfoContainer.innerHTML = '';

        // 1. 创建用户信息展示区域
        const userProfile = document.createElement('div');
        userProfile.style.display = 'flex';
        userProfile.style.alignItems = 'center';
        userProfile.style.cursor = 'pointer';

        // 2. 头像区域
        const avatarContainer = document.createElement('div');
        avatarContainer.style.position = 'relative';
        avatarContainer.style.marginRight = '12px';

        const avatarImg = document.createElement('img');
        if (userInfo.avatar) {
            avatarImg.src = `${window.baseUrl}${userInfo.avatar}`;
        } else {
            // 默认头像（更换更美观的默认头像）
            avatarImg.src = 'https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/icons/person-circle.svg';
        }
        avatarImg.style.width = '44px'; // 放大头像
        avatarImg.style.height = '44px';
        avatarImg.style.borderRadius = '50%';
        avatarImg.style.objectFit = 'cover';
        avatarImg.style.border = '2px solid #fff';
        avatarImg.style.boxShadow = '0 0 6px rgba(0,0,0,0.2)'; // 增强阴影

        // 3. 用户信息文本区域（角色、用户名、登录时间）
        const textContainer = document.createElement('div');
        textContainer.style.textAlign = 'left';
        textContainer.style.color = '#fff';

        // 角色名称
        const roleText = document.createElement('div');
        roleText.style.fontSize = '13px'; // 放大文字
        roleText.style.opacity = '0.9';
        roleText.textContent = userInfo.adminRole || '系统管理员';

        // 用户名
        const nameText = document.createElement('div');
        nameText.style.fontSize = '15px'; // 放大文字
        nameText.style.fontWeight = '600'; // 加重字体
        nameText.textContent = userInfo.admin || '管理员';

        // 上次登录时间
        const loginTimeText = document.createElement('div');
        loginTimeText.style.fontSize = '12px'; // 放大文字
        loginTimeText.style.opacity = '0.8';
        if (userInfo.lastLoginTime) {
            loginTimeText.textContent = `上次登录：${formatDate(userInfo.lastLoginTime, 'YYYY-MM-DD HH:mm')}`;
        } else {
            loginTimeText.textContent = '首次登录';
        }

        // 组装文本区域
        textContainer.appendChild(roleText);
        textContainer.appendChild(nameText);
        textContainer.appendChild(loginTimeText);

        // 下拉箭头
        const arrowIcon = document.createElement('i');
        arrowIcon.className = 'fa fa-angle-down';
        arrowIcon.style.marginLeft = '12px'; // 放大间距
        arrowIcon.style.color = '#fff';
        arrowIcon.style.opacity = '0.8';
        arrowIcon.style.fontSize = '16px'; // 放大箭头

        // 组装用户信息展示区域
        avatarContainer.appendChild(avatarImg);
        userProfile.appendChild(avatarContainer);
        userProfile.appendChild(textContainer);
        userProfile.appendChild(arrowIcon);

        // 4. 下拉菜单
        const userDropdown = document.createElement('div');
        userDropdown.id = 'userDropdown';
        userDropdown.className = 'user-dropdown';
        userDropdown.innerHTML = `
            <a href="#" id="profileBtn">
                <i class="fa fa-user"></i> 个人信息
            </a>
            <a href="#" id="changePasswordBtn">
                <i class="fa fa-lock"></i> 修改密码
            </a>
            <div class="divider"></div>
            <a href="#" onclick="logoutEvent()" id="logoutBtn">
                <i class="fa fa-sign-out"></i> 退出登录
            </a>
        `;

        // 组装完整的用户信息容器
        userInfoContainer.appendChild(userProfile);
        userInfoContainer.appendChild(userDropdown);

        // 更新欢迎语中的角色名称
        const userRoleText = document.getElementById('userRoleText');
        if (userRoleText) {
            userRoleText.textContent = userInfo.adminRole || '系统管理员';
        }

        // 绑定个人信息和修改密码按钮事件
        setTimeout(() => {
            document.getElementById('profileBtn').addEventListener('click', function(e) {
                e.preventDefault();
                showProfileModal();
            });
            document.getElementById('changePasswordBtn').addEventListener('click', function(e) {
                e.preventDefault();
                showChangePasswordModal();
            });
        }, 100);

        console.log('用户信息渲染成功', userInfo);
    } catch (e) {
        console.error('渲染用户信息失败', e);
        showMessage('渲染用户信息失败', 'error');
    }
}

/**
 * 绑定用户信息下拉菜单事件
 */
function bindUserInfoEvents() {
    console.log("用户信息下拉菜单绑定成功！")
    const userInfo = document.getElementById('userInfo');

    userInfo.addEventListener('click', function(e) {
        e.stopPropagation();
        const userDropdown = document.getElementById('userDropdown');
        if (!userDropdown) return;

        userDropdown.classList.toggle('show');
        const arrow = this.querySelector('.fa-angle-down') || this.querySelector('.fa-angle-up');
        if (arrow) {
            if (userDropdown.classList.contains('show')) {
                arrow.classList.remove('fa-angle-down');
                arrow.classList.add('fa-angle-up');
            } else {
                arrow.classList.remove('fa-angle-up');
                arrow.classList.add('fa-angle-down');
            }
        }
    });

    // 点击页面其他区域关闭下拉菜单
    document.addEventListener('click', function() {
        const userDropdown = document.getElementById('userDropdown');
        if (userDropdown && userDropdown.classList.contains('show')) {
            userDropdown.classList.remove('show');
            const arrow = document.querySelector('#userInfo .fa-angle-up');
            if (arrow) {
                arrow.classList.remove('fa-angle-up');
                arrow.classList.add('fa-angle-down');
            }
        }
    });

    // 阻止下拉菜单内部点击事件冒泡
    document.addEventListener('click', function(e) {
        const userDropdown = document.getElementById('userDropdown');
        if (userDropdown && userDropdown.contains(e.target)) {
            e.stopPropagation();
        }
    });
}

/**
 * 绑定侧边栏折叠/展开事件
 */
function bindSidebarToggleEvent() {
    const toggleBtn = document.getElementById('sidebarToggleBtn');
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.querySelector('.main-content'); // 主内容区域（如果有）

    if (!toggleBtn || !sidebar) return;

    toggleBtn.addEventListener('click', function() {
        // 切换侧边栏显示/隐藏
        if (sidebar.style.display === 'none') {
            sidebar.style.display = 'block';
            toggleBtn.innerHTML = '<i class="fa fa-bars"></i>'; // 切换为菜单图标
            // 恢复主内容区域边距（如果有）
            if (mainContent) {
                mainContent.classList.remove('expand');
            }
        } else {
            sidebar.style.display = 'none';
            toggleBtn.innerHTML = '<i class="fa fa-bars"></i>'; // 保持图标（也可改为 ← 箭头）
            // 展开主内容区域（如果有）
            if (mainContent) {
                mainContent.classList.add('expand');
            }
        }

        // 按钮点击动效
        toggleBtn.style.transform = 'scale(0.95)';
        setTimeout(() => {
            toggleBtn.style.transform = 'scale(1)';
        }, 100);
    });
}

// 退出登录函数
function logoutEvent() {
    logout().then(isLogin => {
        if(isLogin) {
            showMessage("退出成功", "success");
            // 跳转到登录页
            window.location.href = `${window.baseUrl}/admin/page/login.do`;
        }else{
            showMessage("退出异常", "error");
        }
    }).catch((e) => {
        console.error(e);
    });
}

// 初始化顶部导航栏（供主页面调用）
function initHeader() {
    loadAndRenderUserInfo();
    bindUserInfoEvents();
    bindSidebarToggleEvent(); // 新增绑定折叠事件
}

/**
 * 显示个人信息弹窗
 */
function showProfileModal() {
    // 获取用户详细信息
    ajaxRequest(
        '/admin/profile/info.do',
        'GET',
        'result',
        function(result) {
            if (result.code === 200) {
                const avatarSrc = result.avatar ? `${window.baseUrl}${result.avatar}` : 'https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/icons/person-circle.svg';
                // 创建模态框
                const modalHtml = `
                    <div class="modal fade" id="profileModal" tabindex="-1">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title"><i class="fa fa-user"></i> 个人信息</h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                </div>
                                <div class="modal-body">
                                    <form id="profileForm">
                                        <div class="mb-3 text-center">
                                            <div style="position: relative; display: inline-block;">
                                                <img src="${avatarSrc}"
                                                     class="rounded-circle" style="width: 100px; height: 100px; object-fit: cover; border: 3px solid #eee;" id="avatarPreview">
                                                <label for="avatarFile" class="btn btn-sm btn-primary" style="position: absolute; bottom: 0; right: -10px; border-radius: 50%; width: 32px; height: 32px; padding: 0; display: flex; align-items: center; justify-content: center; cursor: pointer;">
                                                    <i class="fa fa-camera"></i>
                                                </label>
                                                <input type="file" id="avatarFile" accept="image/*" style="display: none;" onchange="previewAndUploadAvatar(this)">
                                            </div>
                                            <div class="mt-2 text-muted" style="font-size: 12px;">点击相机图标上传头像 (最大4MB)</div>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">角色名称</label>
                                            <input type="text" class="form-control" value="${result.roleName}" readonly>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">用户名</label>
                                            <input type="text" class="form-control" id="profileUsername" value="${result.username}">
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">注册时间</label>
                                            <input type="text" class="form-control" value="${formatDate(result.createTime, 'YYYY-MM-DD HH:mm:ss')}" readonly>
                                        </div>
                                    </form>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                    <button type="button" class="btn btn-primary" onclick="updateProfile()">保存修改</button>
                                </div>
                            </div>
                        </div>
                    </div>
                `;

                // 移除旧的模态框
                const oldModal = document.getElementById('profileModal');
                if (oldModal) oldModal.remove();

                // 添加新模态框
                document.body.insertAdjacentHTML('beforeend', modalHtml);

                // 显示模态框
                const modal = new bootstrap.Modal(document.getElementById('profileModal'));
                modal.show();
            } else {
                showMessage(result.msg || '获取用户信息失败', 'error');
            }
        },
        function(error) {
            showMessage('网络异常', 'error');
        }
    );
}

/**
 * 预览并上传头像
 */
function previewAndUploadAvatar(input) {
    if (input.files && input.files[0]) {
        const file = input.files[0];

        // 验证文件类型
        if (!file.type.match('image.*')) {
            showMessage('请选择图片文件', 'error');
            return;
        }

        // 验证文件大小 (2MB)
        if (file.size > 2 * 1024 * 1024) {
            showMessage('图片大小不能超过2MB', 'error');
            return;
        }

        // 先预览图片
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('avatarPreview').src = e.target.result;
        };
        reader.readAsDataURL(file);

        // 上传图片
        const formData = new FormData();
        formData.append('file', file);

        fetch(`${window.baseUrl}/admin/profile/uploadAvatar.do`, {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                showMessage('头像上传成功', 'success');
                // 更新预览图为服务器返回的路径
                document.getElementById('avatarPreview').src = `${window.baseUrl}${result.avatar}`;
            } else {
                showMessage(result.msg || '头像上传失败', 'error');
            }
        })
        .catch(error => {
            console.error('上传错误:', error);
            showMessage('头像上传失败', 'error');
        });
    }
}

/**
 * 更新个人信息
 */
function updateProfile() {
    const username = document.getElementById('profileUsername').value.trim();

    if (!username) {
        showMessage('用户名不能为空', 'error');
        return;
    }

    ajaxRequest(
        '/admin/profile/update.do',
        'POST',
        {username: username},
        function(result) {
            if (result.code === 200) {
                showMessage('更新成功', 'success');
                bootstrap.Modal.getInstance(document.getElementById('profileModal')).hide();
                // 刷新用户信息显示
                loadAndRenderUserInfo();
            } else {
                showMessage(result.msg || '更新失败', 'error');
            }
        },
        function(error) {
            showMessage('网络异常', 'error');
        }
    );
}

/**
 * 显示修改密码弹窗
 */
function showChangePasswordModal() {
    const modalHtml = `
        <div class="modal fade" id="changePasswordModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title"><i class="fa fa-lock"></i> 修改密码</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div id="passwordStep1">
                            <form id="passwordForm1">
                                <div class="mb-3">
                                    <label class="form-label">请输入原密码</label>
                                    <input type="password" class="form-control" id="oldPassword" placeholder="请输入原密码">
                                </div>
                                <div class="mb-2">
                                    <a href="#" id="forgotPasswordLink" style="font-size: 12px;">忘记密码？</a>
                                </div>
                            </form>
                        </div>
                        <div id="passwordStep2" style="display: none;">
                            <form id="passwordForm2">
                                <div class="mb-3">
                                    <label class="form-label">请输入新密码</label>
                                    <input type="password" class="form-control" id="newPassword" placeholder="请输入新密码">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">请再次输入新密码</label>
                                    <input type="password" class="form-control" id="confirmPassword" placeholder="请再次输入新密码">
                                </div>
                            </form>
                        </div>
                        <div id="forgotPasswordStep" style="display: none;">
                            <form id="forgotPasswordForm">
                                <div class="mb-3">
                                    <label class="form-label">请输入密钥</label>
                                    <input type="text" class="form-control" id="secretKey" placeholder="请输入密钥 (root)">
                                </div>
                            </form>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" id="backBtn" style="display: none;" onclick="backToPasswordStep1()">返回</button>
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                        <button type="button" class="btn btn-primary" id="continueBtn" onclick="verifyOldPassword()">继续</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // 移除旧的模态框
    const oldModal = document.getElementById('changePasswordModal');
    if (oldModal) oldModal.remove();
    
    // 添加新模态框
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // 显示模态框
    const modal = new bootstrap.Modal(document.getElementById('changePasswordModal'));
    modal.show();
    
    // 忘记密码链接事件
    document.getElementById('forgotPasswordLink').addEventListener('click', function(e) {
        e.preventDefault();
        showForgotPasswordStep();
    });
}

/**
 * 验证原密码
 */
function verifyOldPassword() {
    const oldPassword = document.getElementById('oldPassword');
    if (!oldPassword) {
        // 如果不在第一步，可能在新密码步骤
        submitNewPassword();
        return;
    }
    
    const password = oldPassword.value.trim();
    if (!password) {
        showMessage('请输入原密码', 'error');
        return;
    }
    
    ajaxRequest(
        '/admin/profile/verifyPassword.do',
        'POST',
        {password: password},
        function(result) {
            if (result.code === 200) {
                showPasswordStep2();
            } else {
                showMessage(result.msg || '密码错误', 'error');
            }
        },
        function(error) {
            showMessage('验证失败', 'error');
        }
    );
}

/**
 * 显示新密码输入步骤
 */
function showPasswordStep2() {
    document.getElementById('passwordStep1').style.display = 'none';
    document.getElementById('passwordStep2').style.display = 'block';
    document.getElementById('forgotPasswordStep').style.display = 'none';
    document.getElementById('backBtn').style.display = 'inline-block';
    document.getElementById('continueBtn').onclick = submitNewPassword;
    document.getElementById('continueBtn').textContent = '确定';
}

/**
 * 显示忘记密码步骤
 */
function showForgotPasswordStep() {
    document.getElementById('passwordStep1').style.display = 'none';
    document.getElementById('passwordStep2').style.display = 'none';
    document.getElementById('forgotPasswordStep').style.display = 'block';
    document.getElementById('backBtn').style.display = 'inline-block';
    document.getElementById('continueBtn').onclick = verifySecretKey;
    document.getElementById('continueBtn').textContent = '验证';
}

/**
 * 验证密钥
 */
function verifySecretKey() {
    const key = document.getElementById('secretKey').value.trim();
    if (!key) {
        showMessage('请输入密钥', 'error');
        return;
    }
    
    ajaxRequest(
        '/admin/profile/verifyKey.do',
        'POST',
        {key: key},
        function(result) {
            if (result.code === 200) {
                showMessage('验证成功', 'success');
                showPasswordStep2();
            } else {
                showMessage(result.msg || '密钥错误', 'error');
            }
        },
        function(error) {
            showMessage('验证失败', 'error');
        }
    );
}

/**
 * 返回到第一步
 */
function backToPasswordStep1() {
    document.getElementById('passwordStep1').style.display = 'block';
    document.getElementById('passwordStep2').style.display = 'none';
    document.getElementById('forgotPasswordStep').style.display = 'none';
    document.getElementById('backBtn').style.display = 'none';
    document.getElementById('continueBtn').onclick = verifyOldPassword;
    document.getElementById('continueBtn').textContent = '继续';
}

/**
 * 提交新密码
 */
function submitNewPassword() {
    const newPassword = document.getElementById('newPassword').value.trim();
    const confirmPassword = document.getElementById('confirmPassword').value.trim();
    
    if (!newPassword || !confirmPassword) {
        showMessage('请输入新密码', 'error');
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showMessage('两次密码不一致', 'error');
        return;
    }
    
    ajaxRequest(
        '/admin/profile/changePassword.do',
        'POST',
        {newPassword: newPassword, confirmPassword: confirmPassword},
        function(result) {
            if (result.code === 200) {
                showMessage('密码修改成功', 'success');
                bootstrap.Modal.getInstance(document.getElementById('changePasswordModal')).hide();
            } else {
                showMessage(result.msg || '密码修改失败', 'error');
            }
        },
        function(error) {
            showMessage('网络异常', 'error');
        }
    );
}