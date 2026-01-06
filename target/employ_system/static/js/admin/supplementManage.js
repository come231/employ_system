// 分页相关变量
let currentPage = 1;
let pageSize = 8;
let totalCount = 0;
let totalPages = 1;
let searchParams = {}; // 存储搜索参数

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log("补卡管理页面DOM加载完成，开始初始化");

    // 第一步：登录态校验
    checkLogin('/admin/login/current.do').then(isLoggedIn => {
        if (!isLoggedIn) {
            window.location.href = `${window.baseUrl}/admin/page/login.do`;
            return;
        }
        // 初始化侧边栏、分页、加载数据
        initHeader();
        initSidebar();
        initPagination();
        loadSupplementData();
        initApproveModal();
    }).catch(err => {
        console.error("登录校验异常：", err);
        initPagination();
        loadSupplementData();
        initApproveModal();
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
            loadSupplementData();
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
 * 初始化审批弹窗事件
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
            document.getElementById('currentSupplementId').value = '';
            document.getElementById('approveResult').value = '';
            document.getElementById('approveOpinionInput').value = '';
        });
    }

    // 提交审批
    if (submitApproveBtn) {
        submitApproveBtn.addEventListener('click', function() {
            const supplementId = document.getElementById('currentSupplementId').value;
            const approveResult = document.getElementById('approveResult').value;
            const approveOpinion = document.getElementById('approveOpinionInput').value.trim();

            // 校验
            if (!supplementId || !approveResult) {
                showMessage('请选择审批结果', 'warning');
                return;
            }

            // 构造审批参数
            const params = {
                supplementId: supplementId,
                approveStatus: approveResult,
                approveOpinion: approveOpinion,
                approverId: 1 // 这里应该从session中获取当前登录用户ID
            };

            // 调用审批接口
            ajaxRequest('/admin/supplement/approve.do', 'POST', params,
                function(result) {
                    if (result.code === 200 || result.code === '200') {
                        showMessage('审批操作成功', 'success');
                        // 关闭弹窗
                        document.getElementById('approveModal').style.display = 'none';
                        // 刷新数据
                        loadSupplementData();
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
                document.getElementById('currentSupplementId').value = '';
                document.getElementById('approveResult').value = '';
                document.getElementById('approveOpinionInput').value = '';
            }
        });
    }
}

/**
 * 打开审批弹窗
 * @param {string} supplementId 补卡ID
 */
function openApproveModal(supplementId) {
    document.getElementById('currentSupplementId').value = supplementId;
    document.getElementById('approveModal').style.display = 'flex';
}

/**
 * 获取补卡列表（无搜索参数时调用）
 */
function getSupplementList() {
    const params = {
        page: currentPage,
        pageSize: pageSize
    };

    ajaxRequest('/admin/supplement/data.do', 'GET', params,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                console.log("获取补卡列表成功：", result.data);
                renderSupplementList(result.data || []);
                totalCount = result.count || 0;
                renderPagination();
            } else {
                console.log("获取补卡列表失败：", result.msg);
                showMessage('获取补卡列表失败：' + (result.msg || '未知错误'), 'error');
                renderSupplementList([]);
            }
        },
        function(error) {
            showMessage('获取补卡列表失败：' + (error.msg || '网络异常'), 'error');
            renderSupplementList([]);
        }
    );
}

/**
 * 渲染补卡列表
 * @param {Array} supplementList 补卡数据列表
 */
function renderSupplementList(supplementList) {
    const tableBody = document.getElementById('supplementTableBody');
    tableBody.innerHTML = '';

    if (!supplementList || supplementList.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="11" style="text-align: center; padding: 50px;">暂无补卡数据</td></tr>';
        return;
    }

    supplementList.forEach(supplement => {
        // 处理补卡类型标签
        let checkTypeClass = 'check-type-checkin';
        let checkTypeName = '上班';
        const checkType = supplement.checkType;
        if (checkType === 1 || checkType === '1') {
            checkTypeClass = 'check-type-checkin';
            checkTypeName = '上班';
        } else if (checkType === 2 || checkType === '2') {
            checkTypeClass = 'check-type-checkout';
            checkTypeName = '下班';
        } else if (checkType === 3 || checkType === '3') {
            checkTypeClass = 'check-type-both';
            checkTypeName = '上下班';
        }

        // 处理审批状态标签
        let statusClass = 'status-pending';
        let statusName = '待审批';
        const status = supplement.status;
        if (status === 1 || status === '1') {
            statusClass = 'status-pass';
            statusName = '通过';
        } else if (status === 2 || status === '2') {
            statusClass = 'status-reject';
            statusName = '驳回';
        }

        // 格式化时间
        const formatCheckDate = formatDate(supplement.checkDate, 'YYYY-MM-DD');
        const formatActualTime = formatDate(supplement.actualTime, 'YYYY-MM-DD HH:mm:ss');
        const formatTime = formatDate(supplement.time, 'YYYY-MM-DD HH:mm:ss');

        // 处理审批意见（无意见显示"无"）
        const approveOpinion = supplement.opinion || '无';

        // 操作列：待审批显示审批按钮，已审批显示灰色不可点击按钮
        let operateHtml = '';
        if (status === 0 || status === '0') {
            operateHtml = `<button class="approve-btn" onclick="openApproveModal('${supplement.Id}')">审批</button>`;
        } else {
            operateHtml = `<button class="approve-btn disabled" disabled>已审批</button>`;
        }

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${supplement.Id || ''}</td>
            <td>${supplement.empNo || ''}</td>
            <td>${formatCheckDate}</td>
            <td><span class="check-type-tag ${checkTypeClass}">${checkTypeName}</span></td>
            <td>${formatActualTime}</td>
            <td>${formatTime}</td>
            <td>${supplement.approveName || '暂无'}</td>
            <td><span class="approve-status-tag ${statusClass}">${statusName}</span></td>
            <td class="supplement-reason-col">${supplement.reason || '无'}</td>
            <td class="approve-opinion-col">${approveOpinion}</td>
            <td class="operate-col">${operateHtml}</td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * 加载补卡数据（整合普通查询和搜索查询）
 */
function loadSupplementData() {
    if (Object.keys(searchParams).length > 0 && searchParams.data) {
        // 有搜索参数，调用搜索接口
        const params = {
            type: searchParams.type,
            data: searchParams.data,
            page: currentPage,
            pageSize: pageSize
        };

        ajaxRequest('/admin/supplement/search.do', 'GET', params,
            function(result) {
                if (result.code === 200 || result.code === '200') {
                    console.log("搜索补卡列表成功：", result.data);
                    renderSupplementList(result.data || []);
                    totalCount = result.count || 0;
                    renderPagination();
                } else {
                    console.log("搜索补卡列表失败：", result.msg);
                    showMessage('搜索补卡列表失败：' + (result.msg || '未知错误'), 'error');
                    renderSupplementList([]);
                }
            },
            function(error) {
                showMessage('搜索补卡列表失败：' + (error.msg || '网络异常'), 'error');
                renderSupplementList([]);
            }
        );
    } else {
        // 无搜索参数，调用普通列表接口
        getSupplementList();
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
            currentPage = 1;
            loadSupplementData();
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

    // 更新左侧补卡记录数显示
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
    loadSupplementData();
}
