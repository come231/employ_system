// 分页相关变量（和员工列表保持一致）
let currentPage = 1;
let pageSize = 8;
let totalCount = 0;
let totalPages = 1;
let searchParams = {}; // 存储搜索参数

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log("请假管理页面DOM加载完成，开始初始化");

    // 第一步：登录态校验
    checkLogin('/admin/login/current.do').then(isLoggedIn => {
        if (!isLoggedIn) {
            window.location.href = `${window.baseUrl}/admin/page/login.do`;
            return;
        }
        console.log("adminId: " + adminId);
        // 初始化侧边栏、分页、加载数据
        initHeader();
        initSidebar();
        initPagination();
        loadLeaveData();
        initApproveModal(); // 新增：初始化审批弹窗
    }).catch(err => {
        console.error("登录校验异常：", err);
        initPagination();
        loadLeaveData();
        initApproveModal(); // 新增：初始化审批弹窗
    });

    // 绑定搜索按钮点击事件
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
            loadLeaveData();
        });
    }

    // 绑定时间范围查询按钮
    const dateSearchBtn = document.getElementById('dateSearchBtn');
    if (dateSearchBtn) {
        dateSearchBtn.addEventListener('click', function() {
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;

            if (!startDate || !endDate) {
                showMessage('请选择开始和结束时间', 'error');
                return;
            }

            if (new Date(startDate) > new Date(endDate)) {
                showMessage('开始时间不能大于结束时间', 'error');
                return;
            }

            // 保存时间范围查询参数
            searchParams = {
                dateRange: true,
                startDate: startDate,
                endDate: endDate
            };

            // 重置到第一页
            currentPage = 1;

            // 加载数据
            loadLeaveData();
        });
    }

    // 绑定重置按钮
    const resetBtn = document.getElementById('resetBtn');
    if (resetBtn) {
        resetBtn.addEventListener('click', function() {
            // 清空所有搜索条件
            document.getElementById('searchInput').value = '';
            document.getElementById('startDate').value = '';
            document.getElementById('endDate').value = '';
            searchParams = {};
            currentPage = 1;
            loadLeaveData();
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
});

/**
 * 新增：初始化审批弹窗事件
 */
function initApproveModal() {
    const closeModalBtn = document.getElementById('closeModalBtn');
    const submitApproveBtn = document.getElementById('submitApproveBtn');
    const approveModal = document.getElementById('approveModal');

    // 关闭弹窗
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', function() {
            approveModal.style.display = 'none';
            // 重置弹窗表单
            document.getElementById('currentLeaveId').value = '';
            document.getElementById('approveResult').value = '';
            document.getElementById('approveOpinionInput').value = '';
        });
    }

    // 提交审批
    if (submitApproveBtn) {
        submitApproveBtn.addEventListener('click', function() {
            const leaveId = document.getElementById('currentLeaveId').value;
            const approveResult = document.getElementById('approveResult').value;
            const approveOpinion = document.getElementById('approveOpinionInput').value.trim();

            // 校验
            if (!leaveId || !approveResult) {
                showMessage('请选择审批结果', 'warning');
                return;
            }

            // 构造审批参数（可根据后端接口调整）
            const params = {
                approverId:adminId,
                leaveId: leaveId,
                approveStatus: approveResult,
                approveOpinion: approveOpinion
            };

            // 调用审批接口（此处需替换为你的实际接口）
            ajaxRequest('/admin/leave/approve.do', 'POST', params,
                function(result) {
                    if (result.code === 200 || result.code === '200') {
                        showMessage('审批操作成功', 'success');
                        // 关闭弹窗
                        document.getElementById('approveModal').style.display = 'none';
                        // 刷新数据
                        loadLeaveData();
                    } else {
                        showMessage('审批失败：' + (result.msg || '未知错误'), 'error');
                    }
                },
                function(error) {
                    showMessage('审批失败：' + (error.msg || '网络异常'), 'error');
                }
            );
        });
    }

    // 点击弹窗外部关闭
    const modal = document.getElementById('approveModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.style.display = 'none';
                // 重置表单
                document.getElementById('currentLeaveId').value = '';
                document.getElementById('approveResult').value = '';
                document.getElementById('approveOpinionInput').value = '';
            }
        });
    }
}

/**
 * 新增：打开审批弹窗
 * @param {string} leaveId 请假ID
 */
function openApproveModal(leaveId) {
    document.getElementById('currentLeaveId').value = leaveId;
    document.getElementById('approveModal').style.display = 'flex';
}

/**
 * 获取请假列表（无搜索参数时调用）
 */
function getLeaveList() {
    const params = {
        page: currentPage,
        pageSize: pageSize
    };

    ajaxRequest('/admin/leave/data.do', 'GET', params,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                console.log("获取请假列表成功：", result.data);
                renderLeaveList(result.data || []);
                totalCount = result.count || 0;
                renderPagination();
            } else {
                console.log("获取请假列表失败：", result.msg);
                showMessage('获取请假列表失败：' + (result.msg || '未知错误'), 'error');
                renderLeaveList([]);
            }
        },
        function(error) {
            showMessage('获取请假列表失败：' + (error.msg || '网络异常'), 'error');
            renderLeaveList([]);
        }
    );
}

/**
 * 渲染请假列表（核心修改：操作列已审批按钮）
 * @param {Array} leaveList 请假数据列表
 */
function renderLeaveList(leaveList) {
    const tableBody = document.getElementById('leaveTableBody');
    tableBody.innerHTML = '';

    if (!leaveList || leaveList.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 50px;">暂无请假数据</td></tr>';
        return;
    }

    leaveList.forEach(leave => {
        // 处理请假类型标签
        let leaveTypeClass = 'leave-type-other';
        let leaveTypeName = leave.type || '未知类型';
        if (leaveTypeName.includes('事假')) {
            leaveTypeClass = 'leave-type-personal';
        } else if (leaveTypeName.includes('病假')) {
            leaveTypeClass = 'leave-type-sick';
        } else if (leaveTypeName.includes('年假')) {
            leaveTypeClass = 'leave-type-annual';
        }

        // 处理审批状态标签
        let statusClass = 'status-pending';
        let statusName = '待审批';
        const status = leave.status;
        if (status === 1 || status === '1') {
            statusClass = 'status-pass';
            statusName = '通过';
        } else if (status === 2 || status === '2') {
            statusClass = 'status-reject';
            statusName = '驳回';
        }

        // 格式化申请时间
        const formatTime = formatDate(leave.time, 'YYYY-MM-DD HH:mm:ss');

        // 处理审批意见（无意见显示"无"）
        const approveOpinion = leave.opinion || '无';

        // 【关键修改】操作列：待审批显示审批按钮，已审批显示灰色不可点击按钮
        let operateHtml = '';
        if (status === 0 || status === '0') { // 待审批状态
            operateHtml = `<button class="approve-btn" onclick="openApproveModal('${leave.Id}')">审批</button>`;
        } else { // 已通过/驳回状态
            operateHtml = `<button class="approve-btn disabled" disabled>已审批</button>`;
        }

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${leave.Id || ''}</td>
            <td>${leave.empNo || ''}</td>
            <td>${formatTime}</td>
            <td><span class="leave-type-tag ${leaveTypeClass}">${leaveTypeName}</span></td>
            <td>${leave.leaveDays || 0} 天</td>
            <td>${leave.approveName || '暂无'}</td>
            <td><span class="approve-status-tag ${statusClass}">${statusName}</span></td>
            <td class="leave-reason-col">${leave.reason || '无'}</td>
            <td class="approve-opinion-col">${approveOpinion}</td>
            <td class="operate-col">${operateHtml}</td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * 加载请假数据（整合普通查询和搜索查询）
 */
function loadLeaveData() {
    let url = '';
    let params = {
        page: currentPage,
        pageSize: pageSize
    };
    if (Object.keys(searchParams).length > 0 || searchParams.data || searchParams.dateRange) {
        // 判断是否为时间范围查询
        if (searchParams.dateRange) {
            url = '/admin/leave/searchByDate.do';
            params.startDate = searchParams.startDate;
            params.endDate = searchParams.endDate;
        }
        // 判断是否为普通搜索
        else if (Object.keys(searchParams).length > 0 && searchParams.data) {
            url = '/admin/leave/search.do';
            params.type = searchParams.type;
            params.data = searchParams.data;
        }
        // 默认查询所有
        else {
            url = '/admin/leave/data.do';
        }

        ajaxRequest(url, 'GET', params,
            function(result) {
                if (result.code === 200 || result.code === '200') {
                    console.log("请假列表成功：", result.data);
                    renderLeaveList(result.data || []);
                    totalCount = result.count || 0;
                    renderPagination();
                } else {
                    console.log("请假列表失败：", result.msg);
                    showMessage('获取请假列表失败：' + (result.msg || '未知错误'), 'error');
                    renderLeaveList([]);
                }
            },
            function(error) {
                console.log("请假列表异常：", error);
                showMessage('获取请假列表失败：' + (error.msg || '网络异常'), 'error');
                renderLeaveList([]);
            })

    }else{
        // 无搜索参数，调用普通列表接口
        getLeaveList();
    }

}

/**
 * 初始化分页控件（和员工列表分页逻辑一致）
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
            loadLeaveData();
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
 * 渲染分页控件
 */
function renderPagination() {
    totalPages = Math.ceil(totalCount / pageSize) || 1;

    // 更新左侧请假记录数显示
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

        // 确保至少显示5个页码
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
    loadLeaveData();
}