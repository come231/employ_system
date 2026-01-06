let currentPage = 1;
let pageSize = 8;
let totalCount = 0;
let totalPages = 1;
let searchParams = {}; // 存储搜索参数

// 页面加载完成后初始化（修复DOM加载时序问题）
document.addEventListener('DOMContentLoaded', function() {
    console.log("页面DOM加载完成，开始初始化");


    // 第一步：先进行登录态校验
    checkLogin('/admin/login/current.do').then(isLoggedIn => {
        if (!isLoggedIn) {
            // 未登录，跳转到登录页面
            window.location.href = `${window.baseUrl}/admin/page/login.do`;
            return;
        }
        // 修复：无论分支都执行初始化（原代码只在else执行）
        // 先初始化侧边栏和头部菜单（修复菜单功能失效）
        initHeader();
        initSidebar();
        initPagination();
        loadEmployeeData();
    }).catch(err => {
        console.error("登录校验异常：", err);
        // 异常时仍初始化分页和加载数据（降级处理）
        initPagination();
        loadEmployeeData();
    });
});

// 获取员工列表
function getEmployeeList() {
    const params = {
        page: currentPage,
        pageSize: pageSize
    };

    ajaxRequest('/admin/employee/list.do', 'GET', params,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                console.log("成功"+result.data);
                renderEmployeeList(result.data);
                totalCount = result.count || 0;
                renderPagination();
            } else {
                console.log("失败"+result.data);
                showMessage('获取员工列表失败：' + (result.msg || '未知错误'), 'error');
                renderEmployeeList([]); // 显示空数据
            }
        },
        function(error) {
            showMessage('获取员工列表失败：' + (error.msg || '网络异常'), 'error');
            renderEmployeeList([]); // 显示空数据
        }
    );
}

// 优化渲染员工列表函数（添加职位标签和状态扩展）
function renderEmployeeList(employeeList) {
    const tableBody = document.getElementById('employeeTableBody');
    tableBody.innerHTML = '';

    if (!employeeList || employeeList.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 50px;">暂无员工数据</td></tr>';
        return;
    }

    employeeList.forEach(employee => {
        // 职位标签类名匹配
        let positionClass = 'position-other';
        const position = (employee.position || '').toLowerCase();
        if (position.includes('研发') || position.includes('技术') || position.includes('开发')) {
            positionClass = 'position-rd';
        } else if (position.includes('人事') || position.includes('hr')) {
            positionClass = 'position-hr';
        } else if (position.includes('行政') || position.includes('后勤')) {
            positionClass = 'position-admin';
        } else if (position.includes('销售') || position.includes('业务')) {
            positionClass = 'position-sales';
        }

        // 状态标签扩展（支持休假状态）
        let statusTag = '';
        if (employee.status === 1) {
            statusTag = '<span class="status-tag status-active">在职</span>';
        } else if (employee.status === 0) {
            statusTag = '<span class="status-tag status-inactive">离职</span>';
        } else if (employee.status === 2) { // 新增休假状态
            statusTag = '<span class="status-tag status-leave">休假</span>';
        }

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${employee.empNo || ''}</td>
            <td>${employee.name || ''}</td>
            <td>${employee.phone || ''}</td>
            <td>${employee.email || ''}</td>
            <td>${statusTag}</td>
            <td><span class="position-tag ${positionClass}">${employee.position || '无'}</span></td>
            <td>
                <button class="btn edit-btn" onclick="openEditModal('${employee.empNo || ''}', '${employee.name || ''}', '${employee.phone || ''}', '${employee.email || ''}', '${employee.status || '1'}', '${employee.deptId || ''}', '${employee.roleId || ''}')">
                    <i class="fa fa-pencil"></i> 编辑
                </button>
                <button class="btn delete-btn" onclick="openDeleteModal('${employee.empNo || ''}', '${employee.name || ''}')">
                    <i class="fa fa-trash"></i> 删除
                </button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// 提交编辑表单
function submitEditForm() {
    // 获取表单数据
    const empData = {
        empNo: document.getElementById('editEmpNo').value,
        name: document.getElementById('editName').value,
        phone: document.getElementById('editPhone').value,
        email: document.getElementById('editEmail').value,
        status: document.getElementById('editStatus').value,
        deptId: document.getElementById('editDept').value,
        roleId: document.getElementById('editRole').value
    };

    // 简单验证
    if (!empData.name || !empData.phone || !empData.email) {
        showMessage('姓名、电话、邮箱不能为空！', 'warning');
        return;
    }

    // 验证邮箱格式
    if (!checkEmail(empData.email)) {
        showMessage('邮箱格式错误！', 'warning');
        return;
    }

    // 验证手机号格式
    if (!checkPhoneNumber(empData.phone)) {
        showMessage('手机号格式错误！', 'warning');
        return;
    }

    // 发送编辑请求
    ajaxRequest('/admin/employee/edit.do', 'POST', empData,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                showMessage(`员工【${empData.name}】信息编辑成功！`, 'success');
                // 重新加载数据，保持当前分页状态
                loadEmployeeData();
                // 关闭弹窗
                closeEditModal();
            } else {
                showMessage('编辑失败：' + (result.msg || '未知错误'), 'error');
            }
        },
        function(error) {
            showMessage('编辑失败：' + (error.msg || '网络异常'), 'error');
        }
    );
}

// 打开编辑弹窗
function openEditModal(empNo, name, phone, email, status, deptId, roleId) {
    // 填充表单数据
    document.getElementById('editEmpNo').value = empNo;
    document.getElementById('editEmpNoShow').value = empNo;
    document.getElementById('editName').value = name;
    document.getElementById('editPhone').value = phone;
    document.getElementById('editEmail').value = email;
    document.getElementById('editStatus').value = status;

    // 加载部门和角色下拉选项，并设置当前值
    loadEditDeptOptions(deptId);
    loadEditRoleOptions(roleId);

    // 显示弹窗（添加show类触发动画）
    const editModal = document.getElementById('editModal');
    editModal.style.display = 'flex';
    editModal.classList.add('show');
}

/**
 * 加载编辑弹窗的部门下拉选项
 */
function loadEditDeptOptions(currentDeptId) {
    ajaxRequest('/admin/employee/depts.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const deptSelect = document.getElementById('editDept');
                deptSelect.innerHTML = '<option value="">请选择部门</option>';
                if (result.data && result.data.length > 0) {
                    result.data.forEach(function(dept) {
                        const option = document.createElement('option');
                        option.value = dept.deptId;
                        option.textContent = dept.deptName;
                        if (dept.deptId == currentDeptId) {
                            option.selected = true;
                        }
                        deptSelect.appendChild(option);
                    });
                }
            }
        },
        function(error) {
            console.error('加载部门列表失败:', error);
        }
    );
}

/**
 * 加载编辑弹窗的角色下拉选项
 */
function loadEditRoleOptions(currentRoleId) {
    ajaxRequest('/admin/employee/roles.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const roleSelect = document.getElementById('editRole');
                roleSelect.innerHTML = '<option value="">请选择职位</option>';
                if (result.data && result.data.length > 0) {
                    result.data.forEach(function(role) {
                        const option = document.createElement('option');
                        option.value = role.roleId;
                        option.textContent = role.roleName;
                        if (role.roleId == currentRoleId) {
                            option.selected = true;
                        }
                        roleSelect.appendChild(option);
                    });
                }
            }
        },
        function(error) {
            console.error('加载角色列表失败:', error);
        }
    );
}

// 关闭编辑弹窗
function closeEditModal() {
    const editModal = document.getElementById('editModal');
    editModal.classList.remove('show');
    // 延迟隐藏，等待动画完成
    setTimeout(() => {
        editModal.style.display = 'none';
    }, 300);
    // 清空表单
    document.getElementById('editEmpNo').value = '';
    document.getElementById('editEmpNoShow').value = '';
    document.getElementById('editName').value = '';
    document.getElementById('editPhone').value = '';
    document.getElementById('editEmail').value = '';
    document.getElementById('editStatus').value = '1';
    document.getElementById('editDept').value = '';
    document.getElementById('editRole').value = '';
}

// 打开删除确认弹窗
function openDeleteModal(empNo, name) {
    // 存储要删除的员工信息
    document.getElementById('deleteEmpNo').value = empNo;
    document.getElementById('deleteTipText').innerText = `确定要删除员工【${name}】吗？此操作不可撤销！`;

    // 显示弹窗（添加show类触发动画）
    const deleteModal = document.getElementById('deleteModal');
    deleteModal.style.display = 'flex';
    deleteModal.classList.add('show');
}

// 关闭删除弹窗
function closeDeleteModal() {
    const deleteModal = document.getElementById('deleteModal');
    deleteModal.classList.remove('show');
    // 延迟隐藏，等待动画完成
    setTimeout(() => {
        deleteModal.style.display = 'none';
    }, 300);
    document.getElementById('deleteEmpNo').value = '';
}

// 提交删除操作
function submitDelete() {
    const empNo = document.getElementById('deleteEmpNo').value;
    const deleteParams = {
        empNo: empNo
    };

    // 发送删除请求
    ajaxRequest('/admin/employee/delete.do', 'POST', deleteParams,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                showMessage(`员工【${empNo}】删除成功！`, 'success');
                // 重新加载数据，保持当前分页状态
                loadEmployeeData();
                // 关闭弹窗
                closeDeleteModal();
            } else {
                showMessage('删除失败：' + (result.msg || '未知错误'), 'error');
            }
        },
        function(error) {
            showMessage('删除失败：' + (error.msg || '网络异常'), 'error');
        }
    );
}

// 搜索功能
document.addEventListener('DOMContentLoaded', function() {
    // 搜索按钮点击事件（修复DOM加载后绑定）
    const searchBtn = document.getElementById('searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const searchValue = document.getElementById('searchInput').value.trim();
            const searchField = document.getElementById('searchField').value;

            // 保存搜索参数
            searchParams = {
                type: searchField,
                data: searchValue
            };

            // 重置到第一页
            currentPage = 1;

            // 加载数据
            loadEmployeeData();
        });
    }

    // 回车键搜索（修复DOM加载后绑定）
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBtn.click();
            }
        });
    }
});

// 辅助函数：更新表格数据
function updateTableData(empData) {
    const tableRows = document.querySelectorAll('#employeeTableBody tr');
    tableRows.forEach(row => {
        if (row.cells[0].innerText === empData.empNo) {
            row.cells[1].innerText = empData.name;
            row.cells[2].innerText = empData.phone;
            row.cells[3].innerText = empData.email;
            // 更新状态标签
            const statusTag = row.cells[4].querySelector('.status-tag');
            if (empData.status === '1') {
                statusTag.className = 'status-tag status-active';
                statusTag.innerText = '在职';
            } else {
                statusTag.className = 'status-tag status-inactive';
                statusTag.innerText = '离职';
            }
            row.cells[5].innerText = empData.position;

            // 更新行内的编辑/删除按钮点击事件
            const editBtn = row.querySelector('.edit-btn');
            if (editBtn) {
                editBtn.setAttribute('onclick', `openEditModal('${empData.empNo}', '${empData.name}', '${empData.phone}', '${empData.email}', '${empData.status}', '${empData.position}')`);
            }

            const deleteBtn = row.querySelector('.delete-btn');
            if (deleteBtn) {
                deleteBtn.setAttribute('onclick', `openDeleteModal('${empData.empNo}', '${empData.name}')`);
            }
        }
    });
}

// 辅助函数：移除表格行
function removeTableRow(empNo) {
    const tableRows = document.querySelectorAll('#employeeTableBody tr');
    tableRows.forEach(row => {
        if (row.cells[0].innerText === empNo) {
            row.remove();
        }
    });
}

// 点击遮罩层关闭弹窗（修复DOM加载后绑定）
document.addEventListener('DOMContentLoaded', function() {
    window.addEventListener('click', function(e) {
        const editModal = document.getElementById('editModal');
        const deleteModal = document.getElementById('deleteModal');

        if (editModal && e.target === editModal) {
            closeEditModal();
        }
        if (deleteModal && e.target === deleteModal) {
            closeDeleteModal();
        }
    });
});

// 分页相关函数
// 初始化分页控件
function initPagination() {
    // 设置每页显示条数默认值
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.value = pageSize;

        // 绑定页码选择事件
        pageSizeSelect.addEventListener('change', function() {
            pageSize = parseInt(this.value);
            currentPage = 1; // 切换每页条数时回到第一页
            loadEmployeeData();
        });
    }

    // 绑定分页按钮事件（修复DOM加载后绑定）
    const firstPageBtn = document.getElementById('firstPage');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');
    const lastPageBtn = document.getElementById('lastPage');

    if (firstPageBtn) {
        firstPageBtn.addEventListener('click', function() {
            goToPage(1);
        });
    }

    if (prevPageBtn) {
        prevPageBtn.addEventListener('click', function() {
            goToPage(currentPage - 1);
        });
    }

    if (nextPageBtn) {
        nextPageBtn.addEventListener('click', function() {
            goToPage(currentPage + 1);
        });
    }

    if (lastPageBtn) {
        lastPageBtn.addEventListener('click', function() {
            goToPage(totalPages);
        });
    }
}

// 计算总页数并渲染分页控件
function renderPagination() {
    totalPages = Math.ceil(totalCount / pageSize) || 1;

    // 新增：更新左侧员工记录数显示
    const totalRecordsNumEl = document.getElementById('totalRecordsNum');
    if (totalRecordsNumEl) {
        totalRecordsNumEl.textContent = totalCount;
    }
    // 更新分页信息
    const totalRecordsEl = document.getElementById('totalRecords');
    const currentPageInfoEl = document.getElementById('currentPageInfo');
    const totalPagesEl = document.getElementById('totalPages');

    if (totalRecordsEl) {
        totalRecordsEl.textContent = `共 ${totalCount} 条记录`;
    }
    if (currentPageInfoEl) {
        currentPageInfoEl.textContent = currentPage;
    }
    if (totalPagesEl) {
        totalPagesEl.textContent = totalPages;
    }

    // 生成页码按钮
    const pageNumbersContainer = document.getElementById('pageNumbers');
    if (pageNumbersContainer) {
        pageNumbersContainer.innerHTML = '';

        // 计算显示的页码范围
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, currentPage + 2);

        // 确保至少显示5个页码（如果总页数足够）
        if (endPage - startPage < 4) {
            if (startPage === 1) {
                endPage = Math.min(totalPages, startPage + 4);
            } else if (endPage === totalPages) {
                startPage = Math.max(1, endPage - 4);
            }
        }

        // 添加页码按钮
        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = document.createElement('div');
            pageBtn.className = `page-number ${i === currentPage ? 'active' : ''}`;
            pageBtn.textContent = i;
            pageBtn.addEventListener('click', function() {
                goToPage(i);
            });
            pageNumbersContainer.appendChild(pageBtn);
        }
    }

    // 禁用/启用分页按钮
    const firstPageBtn = document.getElementById('firstPage');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');
    const lastPageBtn = document.getElementById('lastPage');

    if (firstPageBtn) {
        firstPageBtn.disabled = currentPage === 1;
    }
    if (prevPageBtn) {
        prevPageBtn.disabled = currentPage === 1;
    }
    if (nextPageBtn) {
        nextPageBtn.disabled = currentPage === totalPages;
    }
    if (lastPageBtn) {
        lastPageBtn.disabled = currentPage === totalPages;
    }
}

// 跳转到指定页码
function goToPage(page) {
    if (page < 1 || page > totalPages) {
        return;
    }

    currentPage = page;
    loadEmployeeData();
}

// 加载员工数据（整合获取和渲染逻辑）
function loadEmployeeData() {
    // 根据是否有搜索参数决定调用哪个接口
    if (Object.keys(searchParams).length > 0 && searchParams.data) {
        // 有搜索参数，调用搜索接口
        const params = {
            type: searchParams.type,
            data: searchParams.data,
            page: currentPage,
            pageSize: pageSize
        };

        ajaxRequest('/admin/employee/search.do', 'GET', params,
            function(result) {
                if (result.code === 200 || result.code === '200') {
                    console.log("搜索成功"+result.data);
                    renderEmployeeList(result.data || []);
                    totalCount = result.count || 0;
                    renderPagination();
                } else {
                    console.log("搜索失败"+result.data);
                    showMessage('获取员工列表失败：' + (result.msg || '未知错误'), 'error');
                    renderEmployeeList([]);
                }
            },
            function(error) {
                showMessage('获取员工列表失败：' + (error.msg || '网络异常'), 'error');
                renderEmployeeList([]);
            }
        );
    } else {
        // 无搜索参数，调用普通列表接口
        getEmployeeList();
    }
}

// ===================== 新增员工相关 =====================
/**
 * 打开新增员工弹窗
 */
function openAddModal() {
    // 加载部门列表
    loadDeptOptions();
    // 加载角色列表
    loadRoleOptions();

    // 显示弹窗
    const addModal = document.getElementById('addModal');
    addModal.style.display = 'flex';
    addModal.classList.add('show');
}

/**
 * 关闭新增员工弹窗
 */
function closeAddModal() {
    const addModal = document.getElementById('addModal');
    addModal.classList.remove('show');
    // 延迟隐藏，等待动画完成
    setTimeout(() => {
        addModal.style.display = 'none';
    }, 300);
    // 清空表单
    document.getElementById('addName').value = '';
    document.getElementById('addPhone').value = '';
    document.getElementById('addEmail').value = '';
    document.getElementById('addDept').value = '';
    document.getElementById('addRole').value = '';
}

/**
 * 加载部门下拉选项
 */
function loadDeptOptions() {
    ajaxRequest('/admin/employee/depts.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const deptSelect = document.getElementById('addDept');
                deptSelect.innerHTML = '<option value="">请选择部门</option>';
                if (result.data && result.data.length > 0) {
                    result.data.forEach(function(dept) {
                        const option = document.createElement('option');
                        option.value = dept.deptId;
                        option.textContent = dept.deptName;
                        deptSelect.appendChild(option);
                    });
                }
            }
        },
        function(error) {
            console.error('加载部门列表失败:', error);
        }
    );
}

/**
 * 加载角色下拉选项
 */
function loadRoleOptions() {
    ajaxRequest('/admin/employee/roles.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const roleSelect = document.getElementById('addRole');
                roleSelect.innerHTML = '<option value="">请选择职位</option>';
                if (result.data && result.data.length > 0) {
                    result.data.forEach(function(role) {
                        const option = document.createElement('option');
                        option.value = role.roleId;
                        option.textContent = role.roleName;
                        roleSelect.appendChild(option);
                    });
                }
            }
        },
        function(error) {
            console.error('加载角色列表失败:', error);
        }
    );
}

/**
 * 提交新增员工表单
 */
function submitAddForm() {
    // 获取表单数据
    const empData = {
        name: document.getElementById('addName').value.trim(),
        phone: document.getElementById('addPhone').value.trim(),
        email: document.getElementById('addEmail').value.trim(),
        deptId: document.getElementById('addDept').value,
        roleId: document.getElementById('addRole').value
    };

    // 验证必填字段
    if (!empData.name) {
        showMessage('请输入员工姓名！', 'warning');
        return;
    }
    if (!empData.deptId) {
        showMessage('请选择部门！', 'warning');
        return;
    }

    // 验证邮箱格式（如果填写了）
    if (empData.email && !checkEmail(empData.email)) {
        showMessage('邮箱格式错误！', 'warning');
        return;
    }

    // 验证手机号格式（如果填写了）
    if (empData.phone && !checkPhoneNumber(empData.phone)) {
        showMessage('手机号格式错误！', 'warning');
        return;
    }

    // 发送新增请求
    ajaxRequest('/admin/employee/add.do', 'POST', empData,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                // 显示成功消息，包含用户名和默认密码
                let msg = '员工创建成功！';
                if (result.data) {
                    msg = `员工创建成功！工号：${result.data.empNo}，用户名：${result.data.username}，默认密码：${result.data.defaultPassword}`;
                }
                showMessage(msg, 'success');
                // 重新加载数据
                loadEmployeeData();
                // 关闭弹窗
                closeAddModal();
            } else {
                showMessage('新增失败：' + (result.msg || '未知错误'), 'error');
            }
        },
        function(error) {
            showMessage('新增失败：' + (error.msg || '网络异常'), 'error');
        }
    );
}

// 点击遮罩层关闭新增弹窗
document.addEventListener('DOMContentLoaded', function() {
    window.addEventListener('click', function(e) {
        const addModal = document.getElementById('addModal');
        if (addModal && e.target === addModal) {
            closeAddModal();
        }
    });
});