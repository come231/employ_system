// 分页相关变量
let currentPage = 1;
let pageSize = 8;
let totalCount = 0;
let totalPages = 1;
let searchParams = {}; // 存储搜索参数

// 部门员工详情分页变量
let deptEmpCurrentPage = 1;
let deptEmpPageSize = 6;
let deptEmpTotalCount = 0;
let deptEmpTotalPages = 1;
let currentDeptId = ''; // 当前查看的部门ID

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log("部门列表页面DOM加载完成，开始初始化");

    // 登录态校验
    checkLogin('/admin/login/current.do').then(isLoggedIn => {
        if (!isLoggedIn) {
            window.location.href = `${window.baseUrl}/admin/page/login.do`;
            return;
        }

        // 初始化组件
        initHeader();
        initSidebar();
        initPagination();
        loadDeptData();

        // 绑定搜索事件
        bindSearchEvent();
        // 初始化部门员工详情分页
        initDeptEmpPagination();
    }).catch(err => {
        console.error("登录校验异常：", err);
        initPagination();
        loadDeptData();
        bindSearchEvent();
        initDeptEmpPagination();
    });

    // 点击遮罩层关闭弹窗
    window.addEventListener('click', function(e) {
        const editModal = document.getElementById('editDeptModal');
        const deleteModal = document.getElementById('deleteDeptModal');
        const empModal = document.getElementById('deptEmpModal');

        if (editModal && e.target === editModal) {
            closeEditDeptModal();
        }
        if (deleteModal && e.target === deleteModal) {
            closeDeleteDeptModal();
        }
        if (empModal && e.target === empModal) {
            closeDeptEmpModal();
        }
    });
});

/**
 * 绑定搜索事件
 */
function bindSearchEvent() {
    // 搜索按钮点击事件
    const searchBtn = document.getElementById('searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const searchValue = document.getElementById('searchInput').value.trim();

            // 保存搜索参数
            searchParams = {
                deptName: searchValue
            };

            // 重置到第一页
            currentPage = 1;

            // 加载数据
            loadDeptData();
        });
    }

    // 回车键搜索
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBtn.click();
            }
        });
    }
}

/**
 * 获取部门列表数据
 */
function getDeptList() {
    const params = {
        page: currentPage,
        pageSize: pageSize
    };

    ajaxRequest('/admin/dept/data.do', 'GET', params,
        function(data) {
            if (data.code === 200 || data.code === '200') {
                renderDeptList(data.data);
                totalCount = data.count || 0;
                renderPagination();
            } else {
                showMessage('获取部门列表失败：' + (data.msg || '未知错误'), 'error');
                renderDeptList([]);
            }
        },
        function(error) {
            showMessage('获取部门列表失败：' + (error.msg || '网络异常'), 'error');
            renderDeptList([]);
        }
    );
}

/**
 * 搜索部门数据
 */
function searchDeptList() {
    const params = {
        page: currentPage,
        pageSize: pageSize,
        deptName: searchParams.deptName
    };

    ajaxRequest('/admin/dept/search.do', 'GET', params,
        function(data) {
            if (data.code === 200 || data.code === '200') {
                renderDeptList(data.data);
                totalCount = data.count || 0;
                renderPagination();
            } else {
                showMessage('搜索部门失败：' + (data.msg || '未知错误'), 'error');
                renderDeptList([]);
            }
        },
        function(error) {
            showMessage('搜索部门失败：' + (error.msg || '网络异常'), 'error');
            renderDeptList([]);
        }
    );
}

/**
 * 渲染部门列表
 * @param {Array} deptList 部门列表数据
 */
function renderDeptList(deptList) {
    const tableBody = document.getElementById('deptTableBody');
    tableBody.innerHTML = '';

    if (!deptList || deptList.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 50px;">暂无部门数据</td></tr>';
        return;
    }

    deptList.forEach(dept => {
        // 状态标签
        let statusTag = '';
        if (dept.status === 1 || dept.status === '1') {
            statusTag = '<span class="dept-status-tag dept-status-active">启用</span>';
        } else {
            statusTag = '<span class="dept-status-tag dept-status-inactive">禁用</span>';
        }

        // 格式化创建时间
        const createTime = formatDate(dept.time, 'YYYY-MM-DD HH:mm:ss');

        // 员工数量
        const empCount = dept.empInfoList ? dept.empInfoList.length : 0;

        // 部门经理名称
        const managerName = dept.empName || '未设置';

        const row = document.createElement('tr');
        row.className = 'dept-row';
        row.innerHTML = `
            <td>${dept.deptId || ''}</td>
            <td>${dept.deptName || ''}</td>
            <td>${managerName}</td>
            <td>${statusTag}</td>
            <td>${createTime}</td>
            <td>
                <button class="dept-detail-btn" onclick="openDeptEmpModal('${dept.deptId}', '${dept.deptName}')">
                    <i class="fa fa-info-circle"></i> 详情(${empCount}人)
                </button>
            </td>
            <td>
                <!-- 编辑按钮 -->
                <button class="edit-dept-btn" onclick="openEditDeptModal('${dept.deptId}', '${dept.deptName}', '${dept.managerId || ''}', '${dept.status || 1}')">
                    <i class="fa fa-pencil"></i> 编辑
                </button>
                <!-- 删除按钮 -->
                <button class="delete-dept-btn" onclick="openDeleteDeptModal('${dept.deptId}', '${dept.deptName}')">
                    <i class="fa fa-trash"></i> 删除
                </button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * 加载部门数据（整合普通查询和搜索）
 */
function loadDeptData() {
    // 显示加载中
    const tableBody = document.getElementById('deptTableBody');
    tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 50px;"><div class="spinner-border" role="status"></div></td></tr>';

    if (Object.keys(searchParams).length > 0 && searchParams.deptName) {
        // 有搜索参数，调用搜索接口
        searchDeptList();
    } else {
        // 无搜索参数，调用普通列表接口
        getDeptList();
    }
}

/**
 * 初始化分页控件
 */
function initPagination() {
    // 设置每页显示条数默认值
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.value = pageSize;

        // 绑定页码选择事件
        pageSizeSelect.addEventListener('change', function() {
            pageSize = parseInt(this.value);
            currentPage = 1; // 切换每页条数时回到第一页
            loadDeptData();
        });
    }

    // 绑定分页按钮事件
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

/**
 * 计算总页数并渲染分页控件
 */
function renderPagination() {
    totalPages = Math.ceil(totalCount / pageSize) || 1;

    // 更新左侧部门记录数显示
    const totalRecordsNumEl = document.getElementById('totalRecordsNum');
    if (totalRecordsNumEl) {
        totalRecordsNumEl.textContent = totalCount;
    }

    // 更新分页信息
    const currentPageInfoEl = document.getElementById('currentPageInfo');
    const totalPagesEl = document.getElementById('totalPages');

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

/**
 * 跳转到指定页码
 * @param {number} page 目标页码
 */
function goToPage(page) {
    if (page < 1 || page > totalPages) {
        return;
    }

    currentPage = page;
    loadDeptData();
}

// ===================== 部门编辑/删除弹窗相关 =====================
/**
 * 打开编辑部门弹窗
 */
function openEditDeptModal(deptId, deptName, managerId, status) {
    // 填充表单数据
    document.getElementById('editDeptId').value = deptId;
    document.getElementById('editDeptIdShow').value = deptId;
    document.getElementById('editDeptName').value = deptName || '';
    document.getElementById('editDeptStatus').value = status || 1;

    // 加载主管下拉选项并设置当前值
    loadEditManagerOptions(managerId);

    // 显示弹窗
    const editModal = document.getElementById('editDeptModal');
    editModal.style.display = 'flex';
    editModal.classList.add('show');
}

/**
 * 加载编辑弹窗的主管下拉选项
 */
function loadEditManagerOptions(currentManagerId) {
    ajaxRequest('/admin/dept/allEmployees.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const managerSelect = document.getElementById('editManagerId');
                managerSelect.innerHTML = '<option value="">请选择部门主管（可选）</option>';
                if (result.data && result.data.length > 0) {
                    result.data.forEach(function(emp) {
                        const option = document.createElement('option');
                        option.value = emp.empId;
                        option.textContent = `${emp.empName} (${emp.empNo})`;
                        if (emp.empId == currentManagerId) {
                            option.selected = true;
                        }
                        managerSelect.appendChild(option);
                    });
                }
            }
        },
        function(error) {
            console.error('加载员工列表失败:', error);
        }
    );
}

/**
 * 关闭编辑部门弹窗
 */
function closeEditDeptModal() {
    const editModal = document.getElementById('editDeptModal');
    editModal.classList.remove('show');
    // 延迟隐藏，等待动画完成
    setTimeout(() => {
        editModal.style.display = 'none';
    }, 300);
    // 清空表单
    document.getElementById('editDeptId').value = '';
    document.getElementById('editDeptIdShow').value = '';
    document.getElementById('editDeptName').value = '';
    document.getElementById('editManagerId').value = '';
    document.getElementById('editDeptStatus').value = 1;
}

/**
 * 提交编辑部门表单
 */
function submitEditDeptForm() {
    // 获取表单数据
    const deptData = {
        deptId: document.getElementById('editDeptId').value,
        deptName: document.getElementById('editDeptName').value,
        managerId: document.getElementById('editManagerId').value,
        status: document.getElementById('editDeptStatus').value
    };

    // 简单验证
    if (!deptData.deptName) {
        showMessage('部门名称不能为空！', 'warning');
        return;
    }

    // 发送AJAX请求
    ajaxRequest('/admin/dept/edit.do', 'POST', deptData,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                showMessage(`部门【${deptData.deptName}】编辑成功！`, 'success');
                loadDeptData();
                closeEditDeptModal();
            } else {
                showMessage('编辑失败：' + (result.msg || '未知错误'), 'error');
            }
        },
        function(error) {
            showMessage('编辑失败：' + (error.msg || '网络异常'), 'error');
        }
    );
}

/**
 * 打开删除部门弹窗
 */
function openDeleteDeptModal(deptId, deptName) {
    // 存储要删除的部门信息
    document.getElementById('deleteDeptId').value = deptId;
    document.getElementById('deleteDeptTipText').innerText = `确定要删除部门【${deptName}】吗？此操作不可撤销！`;

    // 显示弹窗
    const deleteModal = document.getElementById('deleteDeptModal');
    deleteModal.style.display = 'flex';
    deleteModal.classList.add('show');
}

/**
 * 关闭删除部门弹窗
 */
function closeDeleteDeptModal() {
    const deleteModal = document.getElementById('deleteDeptModal');
    deleteModal.classList.remove('show');
    // 延迟隐藏，等待动画完成
    setTimeout(() => {
        deleteModal.style.display = 'none';
    }, 300);
    document.getElementById('deleteDeptId').value = '';
}

/**
 * 提交删除部门操作
 */
function submitDeleteDept() {
    const deptId = document.getElementById('deleteDeptId').value;
    const deptName = document.getElementById('deleteDeptTipText').innerText.match(/【(.*?)】/)[1];

    const deleteParams = { deptId: deptId };
    ajaxRequest('/admin/dept/delete.do', 'POST', deleteParams,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                showMessage(`部门【${deptName}】删除成功！`, 'success');
                loadDeptData();
                closeDeleteDeptModal();
            } else {
                showMessage('删除失败：' + (result.msg || '未知错误'), 'error');
            }
        },
        function(error) {
            showMessage('删除失败：' + (error.msg || '网络异常'), 'error');
        }
    );
}

// ===================== 部门员工详情弹窗相关 =====================
/**
 * 打开部门员工详情弹窗
 */
function openDeptEmpModal(deptId, deptName) {
    currentDeptId = deptId;
    deptEmpCurrentPage = 1; // 重置到第一页

    // 更新弹窗标题
    document.getElementById('deptEmpModalTitle').textContent = `${deptName} - 员工列表`;

    // 显示弹窗
    const empModal = document.getElementById('deptEmpModal');
    empModal.style.display = 'flex';
    empModal.classList.add('show');

    // 加载部门员工数据
    loadDeptEmpData();
}

/**
 * 关闭部门员工详情弹窗
 */
function closeDeptEmpModal() {
    const empModal = document.getElementById('deptEmpModal');
    empModal.classList.remove('show');
    // 延迟隐藏，等待动画完成
    setTimeout(() => {
        empModal.style.display = 'none';
    }, 300);
    currentDeptId = '';
}

/**
 * 初始化部门员工详情分页控件
 */
function initDeptEmpPagination() {
    // 设置每页显示条数默认值（6条）
    const pageSizeSelect = document.getElementById('deptEmpPageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.value = deptEmpPageSize;

        // 绑定页码选择事件
        pageSizeSelect.addEventListener('change', function() {
            deptEmpPageSize = parseInt(this.value);
            deptEmpCurrentPage = 1; // 切换每页条数时回到第一页
            loadDeptEmpData();
        });
    }

    // 绑定分页按钮事件
    const firstPageBtn = document.getElementById('deptEmpFirstPage');
    const prevPageBtn = document.getElementById('deptEmpPrevPage');
    const nextPageBtn = document.getElementById('deptEmpNextPage');
    const lastPageBtn = document.getElementById('deptEmpLastPage');

    if (firstPageBtn) {
        firstPageBtn.addEventListener('click', function() {
            goToDeptEmpPage(1);
        });
    }

    if (prevPageBtn) {
        prevPageBtn.addEventListener('click', function() {
            goToDeptEmpPage(deptEmpCurrentPage - 1);
        });
    }

    if (nextPageBtn) {
        nextPageBtn.addEventListener('click', function() {
            goToDeptEmpPage(deptEmpCurrentPage + 1);
        });
    }

    if (lastPageBtn) {
        lastPageBtn.addEventListener('click', function() {
            goToDeptEmpPage(deptEmpTotalPages);
        });
    }
}

/**
 * 加载部门员工数据
 */
function loadDeptEmpData() {
    // 显示加载中
    const tableBody = document.getElementById('deptEmpTableBody');
    tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 30px;"><div class="spinner-border" role="status"></div></td></tr>';

    // 调用后端接口获取部门员工
    const params = {
        deptId: currentDeptId,
        page: deptEmpCurrentPage,
        pageSize: deptEmpPageSize
    };
    
    ajaxRequest('/admin/dept/employees.do', 'GET', params,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                renderDeptEmpList(result.data);
                deptEmpTotalCount = result.count || 0;
                renderDeptEmpPagination();
            } else {
                showMessage('获取员工列表失败：' + (result.msg || '未知错误'), 'error');
                renderDeptEmpList([]);
            }
        },
        function(error) {
            showMessage('获取员工列表失败：' + (error.msg || '网络异常'), 'error');
            renderDeptEmpList([]);
        }
    );
}

/**
 * 渲染部门员工列表
 */
function renderDeptEmpList(empList) {
    const tableBody = document.getElementById('deptEmpTableBody');
    tableBody.innerHTML = '';

    if (!empList || empList.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 30px;">该部门暂无员工</td></tr>';
        return;
    }

    empList.forEach(emp => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${emp.empNo || ''}</td>
            <td>${emp.empName || ''}</td>
            <td>${emp.phone || ''}</td>
            <td>${emp.email || ''}</td>
            <td>${emp.position || ''}</td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * 渲染部门员工分页控件
 */
function renderDeptEmpPagination() {
    deptEmpTotalPages = Math.ceil(deptEmpTotalCount / deptEmpPageSize) || 1;

    // 更新分页信息
    const currentPageInfoEl = document.getElementById('deptEmpCurrentPageInfo');
    const totalPagesEl = document.getElementById('deptEmpTotalPages');

    if (currentPageInfoEl) {
        currentPageInfoEl.textContent = deptEmpCurrentPage;
    }
    if (totalPagesEl) {
        totalPagesEl.textContent = deptEmpTotalPages;
    }

    // 生成页码按钮
    const pageNumbersContainer = document.getElementById('deptEmpPageNumbers');
    if (pageNumbersContainer) {
        pageNumbersContainer.innerHTML = '';

        // 计算显示的页码范围
        let startPage = Math.max(1, deptEmpCurrentPage - 2);
        let endPage = Math.min(deptEmpTotalPages, deptEmpCurrentPage + 2);

        // 确保至少显示5个页码（如果总页数足够）
        if (endPage - startPage < 4) {
            if (startPage === 1) {
                endPage = Math.min(deptEmpTotalPages, startPage + 4);
            } else if (endPage === deptEmpTotalPages) {
                startPage = Math.max(1, endPage - 4);
            }
        }

        // 添加页码按钮
        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = document.createElement('div');
            pageBtn.className = `page-number ${i === deptEmpCurrentPage ? 'active' : ''}`;
            pageBtn.textContent = i;
            pageBtn.addEventListener('click', function() {
                goToDeptEmpPage(i);
            });
            pageNumbersContainer.appendChild(pageBtn);
        }
    }

    // 禁用/启用分页按钮
    const firstPageBtn = document.getElementById('deptEmpFirstPage');
    const prevPageBtn = document.getElementById('deptEmpPrevPage');
    const nextPageBtn = document.getElementById('deptEmpNextPage');
    const lastPageBtn = document.getElementById('deptEmpLastPage');

    if (firstPageBtn) {
        firstPageBtn.disabled = deptEmpCurrentPage === 1;
    }
    if (prevPageBtn) {
        prevPageBtn.disabled = deptEmpCurrentPage === 1;
    }
    if (nextPageBtn) {
        nextPageBtn.disabled = deptEmpCurrentPage === deptEmpTotalPages;
    }
    if (lastPageBtn) {
        lastPageBtn.disabled = deptEmpCurrentPage === deptEmpTotalPages;
    }
}

/**
 * 跳转到指定的部门员工页码
 */
function goToDeptEmpPage(page) {
    if (page < 1 || page > deptEmpTotalPages) {
        return;
    }

    deptEmpCurrentPage = page;
    loadDeptEmpData();
}

// ===================== 新增部门相关 =====================
/**
 * 打开新增部门弹窗
 */
function openAddDeptModal() {
    // 加载员工列表作为主管选项
    loadEmployeeOptions();

    // 显示弹窗
    const addModal = document.getElementById('addDeptModal');
    addModal.style.display = 'flex';
    addModal.classList.add('show');
}

/**
 * 关闭新增部门弹窗
 */
function closeAddDeptModal() {
    const addModal = document.getElementById('addDeptModal');
    addModal.classList.remove('show');
    // 延迟隐藏，等待动画完成
    setTimeout(() => {
        addModal.style.display = 'none';
    }, 300);
    // 清空表单
    document.getElementById('addDeptName').value = '';
    document.getElementById('addDeptManager').value = '';
}

/**
 * 加载员工列表作为主管下拉选项
 */
function loadEmployeeOptions() {
    ajaxRequest('/admin/dept/allEmployees.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const managerSelect = document.getElementById('addDeptManager');
                managerSelect.innerHTML = '<option value="">请选择部门主管（可选）</option>';
                if (result.data && result.data.length > 0) {
                    result.data.forEach(function(emp) {
                        const option = document.createElement('option');
                        option.value = emp.empId;
                        option.textContent = `${emp.empName} (${emp.empNo})`;
                        managerSelect.appendChild(option);
                    });
                }
            }
        },
        function(error) {
            console.error('加载员工列表失败:', error);
        }
    );
}

/**
 * 提交新增部门表单
 */
function submitAddDeptForm() {
    // 获取表单数据
    const deptData = {
        deptName: document.getElementById('addDeptName').value.trim(),
        managerId: document.getElementById('addDeptManager').value
    };

    // 验证必填字段
    if (!deptData.deptName) {
        showMessage('请输入部门名称！', 'warning');
        return;
    }

    // 发送新增请求
    ajaxRequest('/admin/dept/add.do', 'POST', deptData,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                showMessage('部门创建成功！', 'success');
                // 重新加载数据
                loadDeptData();
                // 关闭弹窗
                closeAddDeptModal();
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
        const addModal = document.getElementById('addDeptModal');
        if (addModal && e.target === addModal) {
            closeAddDeptModal();
        }
    });
});