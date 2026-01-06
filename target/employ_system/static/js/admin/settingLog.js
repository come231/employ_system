let currentPage = 1;
let pageSize = 10;
let totalCount = 0;
let totalPages = 1;
let searchParams = {};

document.addEventListener('DOMContentLoaded', function() {
    // 登录态校验
    checkLogin('/admin/login/current.do').then(isLoggedIn => {
        if (!isLoggedIn) {
            window.location.href = `${window.baseUrl}/admin/page/login.do`;
            return;
        }

        initHeader();
        initSidebar();
        initPagination();
        initEventListeners();
        loadLogData();
    }).catch(err => {
        console.error("登录校验异常：", err);
        initHeader();
        initSidebar();
        initPagination();
        initEventListeners();
        loadLogData();
    });
});

function initEventListeners() {
    // 搜索按钮
    document.getElementById('searchBtn').addEventListener('click', function() {
        searchParams = {
            type: document.getElementById('searchField').value,
            data: document.getElementById('searchInput').value.trim()
        };
        currentPage = 1;
        loadLogData();
    });

    // 重置按钮
    document.getElementById('resetBtn').addEventListener('click', function() {
        document.getElementById('searchInput').value = '';
        document.getElementById('startDate').value = '';
        document.getElementById('endDate').value = '';
        searchParams = {};
        currentPage = 1;
        loadLogData();
    });

    // 日期搜索按钮
    document.getElementById('dateSearchBtn').addEventListener('click', function() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        if (startDate || endDate) {
            searchParams = {
                startDate: startDate,
                endDate: endDate
            };
            currentPage = 1;
            loadLogData();
        }
    });

    // 回车搜索
    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            document.getElementById('searchBtn').click();
        }
    });
}

function loadLogData() {
    const params = {
        page: currentPage,
        pageSize: pageSize,
        ...searchParams
    };

    const url = Object.keys(searchParams).length > 0 && (searchParams.data || searchParams.startDate)
        ? '/admin/setting/log/search.do'
        : '/admin/setting/log/data.do';

    ajaxRequest(url, 'GET', params,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                renderLogList(result.data || []);
                totalCount = result.count || 0;
                console.log(result);
                renderPagination();
            } else {
                renderLogList([]);
                totalCount = 0;
                renderPagination();
            }
        },
        function(error) {
            console.error('加载日志数据失败', error);
            renderLogList([]);
            totalCount = 0;
            renderPagination();
        }
    );
}

function getOperationBadgeClass(operationType) {
    const typeMap = {
        '登录': 'op-login',
        '退出': 'op-logout',
        '新增': 'op-add',
        '添加': 'op-add',
        '修改': 'op-edit',
        '编辑': 'op-edit',
        '更新': 'op-edit',
        '删除': 'op-delete',
        '审批': 'op-approve',
        '查询': 'op-query'
    };
    return typeMap[operationType] || 'op-default';
}

function renderLogList(logs) {
    const tbody = document.getElementById('logTableBody');
    tbody.innerHTML = '';
    if (!logs || logs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; padding: 50px; color: #999;"><i class="fa fa-inbox" style="font-size: 24px; display: block; margin-bottom: 10px;"></i>暂无日志数据</td></tr>';
        return;
    }
    logs.forEach(log => {
        const row = document.createElement('tr');
        const statusClass = log.status === 1 ? 'status-success' : 'status-error';
        const statusText = log.status === 1 ? '成功' : '失败';
        const opBadgeClass = getOperationBadgeClass(log.operationType);
        console.log(log.operationContent)
        row.innerHTML = `
            <td>${log.logId || ''}</td> <!-- 修复：log_id -->
            <td><strong>${log.username || ''}</strong></td> <!-- 修复：user_name -->
            <td>${log.roleName || ''}</td> <!-- 修复：role_name -->
            <td>${log.module || ''}</td>
            <td><span class="operation-badge ${opBadgeClass}">${log.operationType || ''}</span></td> <!-- 修复：operation_type -->
            <td class="content-cell" title="${log.operationContent || ''}">${log.operationContent || ''}</td> <!-- 修复：operation_content -->
            <td>${log.operationTime || ''}</td> <!-- 修复：operation_time -->
            <td><span class="status-badge ${statusClass}">${statusText}</span></td>
            <td class="error-cell" title="${log.errorMsg || ''}">${log.errorMsg || '-'}</td> <!-- 修复：error_msg -->
            `;
        tbody.appendChild(row);
    });
}

function initPagination() {
    document.getElementById('pageSizeSelect').value = pageSize;
    document.getElementById('pageSizeSelect').addEventListener('change', function() {
        pageSize = parseInt(this.value);
        currentPage = 1;
        loadLogData();
    });

    ['firstPage', 'prevPage', 'nextPage', 'lastPage'].forEach(id => {
        document.getElementById(id).addEventListener('click', function() {
            if (id === 'firstPage') goToPage(1);
            else if (id === 'prevPage') goToPage(currentPage - 1);
            else if (id === 'nextPage') goToPage(currentPage + 1);
            else goToPage(totalPages);
        });
    });
}

function renderPagination() {
    totalPages = Math.ceil(totalCount / pageSize) || 1;
    document.getElementById('totalRecordsNum').textContent = totalCount;
    document.getElementById('currentPageInfo').textContent = currentPage;
    document.getElementById('totalPages').textContent = totalPages;

    const container = document.getElementById('pageNumbers');
    container.innerHTML = '';
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, currentPage + 2);
    for (let i = startPage; i <= endPage; i++) {
        const btn = document.createElement('div');
        btn.className = 'page-number' + (i === currentPage ? ' active' : '');
        btn.textContent = i;
        btn.addEventListener('click', () => goToPage(i));
        container.appendChild(btn);
    }

    document.getElementById('firstPage').disabled = currentPage <= 1;
    document.getElementById('prevPage').disabled = currentPage <= 1;
    document.getElementById('nextPage').disabled = currentPage >= totalPages;
    document.getElementById('lastPage').disabled = currentPage >= totalPages;
}

function goToPage(page) {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    loadLogData();
}