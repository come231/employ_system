// static/js/admin/attendanceList.js
// 分页相关变量
let currentPage = 1;
let pageSize = 4;
let totalCount = 0;
let totalPages = 1;
let searchParams = {}; // 存储搜索参数
let dateSearchParams = {}; // 存储日期搜索参数

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log("考勤记录页面DOM加载完成，开始初始化");

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
        loadAttendanceData();

        // 绑定搜索事件
        bindSearchEvent();
    }).catch(err => {
        console.error("登录校验异常：", err);
        initPagination();
        loadAttendanceData();
        bindSearchEvent();
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
                empNo: searchValue
            };

            // 重置到第一页
            currentPage = 1;

            // 加载数据
            loadAttendanceData();
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
 * 获取考勤记录列表数据
 */
function getAttendanceList() {
    const params = {
        page: currentPage,
        pageSize: pageSize
    };

    ajaxRequest('/admin/attendanceManage/data.do', 'GET', params,
        function(data) {
            if (data.code === 200 || data.code === '200') {
                renderAttendanceList(data.data);
                totalCount = data.count || 0;
                renderPagination();
            } else {
                showMessage('获取考勤记录失败：' + (data.msg || '未知错误'), 'error');
                renderAttendanceList([]);
            }
        },
        function(error) {
            showMessage('获取考勤记录失败：' + (error.msg || '网络异常'), 'error');
            renderAttendanceList([]);
        }
    );
}

/**
 * 搜索考勤记录数据
 */
function searchAttendanceList() {
    const params = {
        empNo: searchParams.empNo,
        page: currentPage,
        pageSize: pageSize
    };

    ajaxRequest('/admin/attendanceManage/search.do', 'GET', params,
        function(data) {
            if (data.code === 200 || data.code === '200') {
                renderAttendanceList(data.data);
                totalCount = data.count || 0;
                renderPagination();
            } else {
                showMessage('搜索考勤记录失败：' + (data.msg || '未知错误'), 'error');
                renderAttendanceList([]);
            }
        },
        function(error) {
            showMessage('搜索考勤记录失败：' + (error.msg || '网络异常'), 'error');
            renderAttendanceList([]);
        }
    );
}

/**
 * 渲染考勤记录列表
 * @param {Array} attendanceList 考勤记录列表数据
 */
function renderAttendanceList(attendanceList) {
    const tableBody = document.getElementById('attendanceTableBody');
    tableBody.innerHTML = '';

    if (!attendanceList || attendanceList.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 50px;">暂无考勤记录</td></tr>';
        return;
    }

    attendanceList.forEach(record => {
        // 状态标签 - 根据status值匹配不同样式
        let statusTag = '';
        let statusText = '';
        switch (record.status) {
            case 0:
            case '0':
                statusText = '正常';
                statusTag = `<span class="attendance-status-tag status-normal">${statusText}</span>`;
                break;
            case 1:
            case '1':
                statusText = '迟到';
                statusTag = `<span class="attendance-status-tag status-late">${statusText}</span>`;
                break;
            case 3:
            case '3':
                statusText = '缺勤';
                statusTag = `<span class="attendance-status-tag status-absent">${statusText}</span>`;
                break;
            case 4:
            case '4':
                statusText = '请假';
                statusTag = `<span class="attendance-status-tag status-leave">${statusText}</span>`;
                break;
            default:
                statusText = '未知';
                statusTag = `<span class="attendance-status-tag">${statusText}</span>`;
        }

        // 格式化日期时间
        const checkDate = formatDate(record.checkDate, 'YYYY-MM-DD');
        const checkInTime = formatDate(record.checkInTime, 'HH:mm:ss');

        // 处理备注字段
        const remark = record.remark || '无';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${record.recordId || ''}</td>
            <td>${record.empNo || ''}</td>
            <td>${checkDate}</td>
            <td>${checkInTime}</td>
            <td>${statusTag}</td>
            <td>${remark}</td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * 加载考勤数据（整合普通查询和搜索）
 */
function loadAttendanceData() {
    // 显示加载中
    const tableBody = document.getElementById('attendanceTableBody');
    tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 50px;"><div class="spinner-border" role="status"></div></td></tr>';

    if (Object.keys(dateSearchParams).length > 0 && dateSearchParams.startDate && dateSearchParams.endDate) {
        // 有日期搜索参数，调用日期搜索接口
        loadDateSearchData();
    } else if (Object.keys(searchParams).length > 0 && searchParams.empNo) {
        // 有员工号搜索参数，调用搜索接口
        searchAttendanceList();
    } else {
        // 无搜索参数，调用普通列表接口
        getAttendanceList();
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
            loadAttendanceData();
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

    // 更新左侧考勤记录数显示
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
    loadAttendanceData();
}

/**
 * 按日期范围搜索考勤记录
 */
function searchByDate() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        showMessage('请选择开始日期和结束日期', 'warning');
        return;
    }

    if (startDate > endDate) {
        showMessage('开始日期不能大于结束日期', 'warning');
        return;
    }

    // 保存日期搜索参数
    dateSearchParams = {
        startDate: startDate,
        endDate: endDate
    };

    // 清除员工号搜索参数
    searchParams = {};
    document.getElementById('searchInput').value = '';

    // 重置到第一页
    currentPage = 1;

    // 加载数据
    loadDateSearchData();
}

/**
 * 加载日期搜索数据
 */
function loadDateSearchData() {
    const tableBody = document.getElementById('attendanceTableBody');
    tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 50px;"><div class="spinner-border" role="status"></div></td></tr>';

    const params = {
        startDate: dateSearchParams.startDate,
        endDate: dateSearchParams.endDate,
        page: currentPage,
        pageSize: pageSize
    };

    ajaxRequest('/admin/attendanceManage/searchByDate.do', 'GET', params,
        function(data) {
            if (data.code === 200 || data.code === '200') {
                renderAttendanceList(data.data);
                totalCount = data.count || 0;
                renderPagination();
            } else {
                showMessage('按日期搜索失败：' + (data.msg || '未知错误'), 'error');
                renderAttendanceList([]);
            }
        },
        function(error) {
            showMessage('按日期搜索失败：' + (error.msg || '网络异常'), 'error');
            renderAttendanceList([]);
        }
    );
}

/**
 * 重置搜索条件
 */
function resetSearch() {
    // 清除所有搜索参数
    searchParams = {};
    dateSearchParams = {};

    // 清除输入框
    document.getElementById('searchInput').value = '';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';

    // 重置到第一页
    currentPage = 1;

    // 重新加载全部数据
    loadAttendanceData();
}