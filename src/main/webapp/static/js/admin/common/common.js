/**
 * 公共JavaScript工具函数库
 * @description 封装前端通用AJAX、日期/金额格式化、登录态校验、DOM操作、防抖节流等工具函数
 * @author 开发团队
 * @version 1.0.0
 * @dependency Bootstrap (alert/spinner组件样式)、原生XMLHttpRequest
 * @note 需在全局挂载window.baseUrl（项目上下文路径，如：/projectName），否则AJAX路径可能异常
 */

/**
 * 通用AJAX请求封装（核心修复：自动拼接上下文路径，避免404）
 * @description 基于原生XMLHttpRequest封装，统一处理请求路径、参数、错误、JSON解析，支持GET/POST
 * @param {string} url - 接口相对路径（示例：/product/list.do）
 * @param {string} method - HTTP请求方法，仅支持GET/POST
 * @param {Object} [data=null] - 请求参数对象（键值对形式）
 * @param {Function} [successCallback=null] - 请求成功回调函数，参数为解析后的JSON响应体
 * @param {Function} [errorCallback=null] - 请求失败回调函数，参数为{msg: 错误提示语}
 * @example
 * */

function ajaxRequest(url, method, data, successCallback, errorCallback) {
    // 初始化请求配置
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded' // 表单编码格式
        },
        credentials: 'include' // 跨域/同域请求携带Cookie
    };

    // 核心修复：拼接完整URL（上下文路径+接口路径，避免重复斜杠）
    let fullUrl = window.baseUrl + url;

    // 处理GET请求参数：拼接至URL末尾
    if (method === 'GET' && data) {
        const params = new URLSearchParams(data);
        fullUrl += '?' + params.toString();
    }

    // 创建XHR对象并配置
    const xhr = new XMLHttpRequest();
    xhr.overrideMimeType('application/json; charset=UTF-8'); // 强制指定响应编码为UTF-8
    xhr.open(method, fullUrl, true); // 异步请求

    // 监听请求状态变化
    xhr.onreadystatechange = function() {
        // 仅处理请求完成状态（readyState=4）
        if (xhr.readyState === 4) {
            // 状态码200表示请求成功
            if (xhr.status === 200) {
                try {
                    // 解析JSON响应体
                    const result = JSON.parse(xhr.responseText);
                    // 执行成功回调
                    if (successCallback) successCallback(result);
                } catch (e) {
                    // JSON解析失败的错误处理
                    console.error('JSON解析失败：', e, '响应内容：', xhr.responseText);
                    if (errorCallback) errorCallback({msg: '数据解析失败'});
                }
            } else {
                // HTTP状态码非200的错误处理
                console.error('请求失败：', xhr.status, fullUrl);
                if (errorCallback) errorCallback({msg: `网络错误(${xhr.status})，请稍后重试`});
            }
        }
    };

    // 监听网络层异常（如断网、跨域限制）
    xhr.onerror = function() {
        console.error('网络异常：', fullUrl);
        if (errorCallback) errorCallback({msg: '网络连接异常'});
    };

    // 发送请求：POST请求携带表单参数，GET请求无body
    xhr.send(method === 'POST' ? new URLSearchParams(data) : null);
}

/**
 * 轻量级消息提示框（基于Bootstrap Alert组件）
 * @description 固定在页面右上角，3秒后自动消失，支持不同类型提示
 * @param {string} message - 提示文本内容
 * @param {string} [type='info'] - 提示类型，可选值：info/success/warning/error（error会映射为bootstrap的danger）
 * @example
 * showMessage('操作成功', 'success'); // 绿色成功提示
 * showMessage('请填写必填项', 'warning'); // 黄色警告提示
 * showMessage('服务器错误', 'error'); // 红色错误提示
 * @note 依赖Bootstrap的alert样式和btn-close组件，需提前引入Bootstrap CSS/JS
 */
function showMessage(message, type = 'info') {
    // 创建提示框DOM元素
    const messageDiv = document.createElement('div');
    // 拼接Bootstrap Alert样式类
    messageDiv.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show`;
    // 固定定位样式：右上角、最高层级
    messageDiv.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    // 提示框内部HTML（包含关闭按钮）
    messageDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    // 添加到页面body
    document.body.appendChild(messageDiv);
    // 3秒后自动移除提示框
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}




/**
 * 检查用户是否已登录（异步）
 * @description 调用用户信息接口，通过响应码判断登录状态，返回Promise
 * @returns {Promise<boolean>} Promise对象，resolve结果：true=已登录，false=未登录
 * @example
 * checkLogin().then(isLogin => {
 *   if (!isLogin) redirectToLogin(); // 未登录则跳登录页
 * });
 * @note 依赖/ajax/user/getCurrentUser.do接口，接口返回格式需包含code字段（0/0表示登录）
 */
function checkLogin(url) {
    return new Promise((resolve) => {
        ajaxRequest(url, 'GET', null,
            (result) => {
                 console.log("登录校验接口返回：",result);
                 resolve(result.code === '200' || result.code === 200);
            },    // 登录态判断
            () => {resolve(false);}// 请求失败视为未登录
        );
    });
}
//获取登录用户
function userLogin() {
    return new Promise((resolve) => {
        ajaxRequest('/user/getCurrentUser.do', 'GET', null,
            (result) => {
                console.log("登录校验接口返回：",result);
                resolve(result);

            },    // 登录态判断
            () => {resolve({code:1,msg:'请求失败'});}// 请求失败视为未登录
        );
    });
}

/**
 * 日期格式化（支持自定义格式）
 * @description 处理时间戳/日期字符串，转换为指定格式的日期文本，无效日期返回空字符串
 * @param {string|number|Date} date - 日期源（时间戳/日期字符串/Date对象）
 * @param {string} [format='YYYY-MM-DD HH:mm:ss'] - 格式化模板，支持占位符：YYYY(年)、MM(月)、DD(日)、HH(时)、mm(分)、ss(秒)
 * @returns {string} 格式化后的日期字符串（如：2025-12-16 10:30:00）
 * @example
 * formatDate(new Date()); // 返回当前时间 "2025-12-16 10:30:00"
 * formatDate(1750000000000, 'YYYY-MM-DD'); // 返回 "2025-11-15"
 * formatDate('无效日期'); // 返回 ""
 */
function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
    // 空值直接返回空字符串
    if (!date) return '';
    // 转换为Date对象
    const d = new Date(date);
    // 校验日期有效性（无效日期返回空）
    if (isNaN(d.getTime())) return '';
    // 提取日期各部分（补零处理）
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0'); // 月份从0开始，补零到2位
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');
    // 替换模板占位符并返回
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

// 退出登录函数
function logout() {
    // 这里替换为你的退出登录逻辑
    if (confirm('确定要退出登录吗？')) {
        return new Promise((resolve) => {
            ajaxRequest('/admin/logout.do', 'GET', null,
                (result) => {
                    //强制清除Token
                    localStorage.removeItem('token');
                    console.log("退出校验接口返回：",result);
                    resolve(result.code===200 || result.code === '200');

                },    // 登录态判断
                () => {resolve(false)}// 请求失败视为未登录
            );
        });
    }
}


/**
 * 登录态校验后跳转（未登录则提示）
 * @description 先检查登录状态，已登录则跳转指定URL，未登录则显示警告提示
 * @param {string} url - 目标跳转URL
 * @example
 * // 点击"我的订单"按钮时调用
 * navigateIfLogin('/page/order/sidebar.jsp');
 */
function navigateIfLogin(url) {
    ajaxRequest('/user/getCurrentUser.do', 'GET', null,
        () => window.location.href = url, // 登录成功则跳转
        () => showMessage('请先登录', 'warning') // 未登录则提示
    );
}

/**
 * 确认对话框（封装原生confirm）
 * @description 弹出确认提示框，用户确认后执行回调函数
 * @param {string} message - 确认提示文本
 * @param {Function} [callback=null] - 用户点击"确认"后的回调函数
 * @example
 * // 删除按钮点击事件
 * confirmDialog('确定要删除这条记录吗？', () => {
 *   ajaxRequest('/delete.do', 'POST', {id: 1}, () => showMessage('删除成功'));
 * });
 */
function confirmDialog(message, callback) {
    if (confirm(message) && callback) callback();
}

/**
 * 显示通用加载中动画
 * @description 替换目标元素的innerHTML为加载动画（Bootstrap Spinner）
 * @param {string|HTMLElement} element - 目标元素（CSS选择器字符串 / DOM元素对象）
 * @example
 * // 示例1：通过选择器指定元素
 * showCommonLoading('#data-container');
 * // 示例2：通过DOM对象指定元素
 * const container = document.getElementById('data-container');
 * showCommonLoading(container);
 * @note 依赖Bootstrap的spinner-border样式
 */
function showCommonLoading(element) {
    // 若传入的是选择器字符串，转换为DOM元素
    if (typeof element === 'string') {
        element = document.querySelector(element);
    }
    // 元素存在则替换内容为加载动画
    if (element) {
        element.innerHTML = '<div style="text-align:center; padding:50px;"><div class="spinner-border" role="status"></div></div>';
    }
}

/**
 * 显示通用空数据提示
 * @description 替换目标元素的innerHTML为空数据提示文本
 * @param {string|HTMLElement} element - 目标元素（CSS选择器字符串 / DOM元素对象）
 * @param {string} [message='暂无数据'] - 空数据提示文本
 * @example
 * // 列表接口返回空数据时调用
 * showCommonEmpty('#data-container', '暂无商品数据');
 */
function showCommonEmpty(element, message = '暂无数据') {
    // 若传入的是选择器字符串，转换为DOM元素
    if (typeof element === 'string') {
        element = document.querySelector(element);
    }
    // 元素存在则替换内容为空提示
    if (element) {
        element.innerHTML = `<div style="text-align:center; padding:50px; color:#999;">${message}</div>`;
    }
}

/**
 * 防抖函数（Debounce）
 * @description 高频触发的函数，仅在最后一次触发后等待指定时间才执行，避免频繁执行
 * @param {Function} func - 要防抖的目标函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 包装后的防抖函数
 * @example
 * // 搜索框输入防抖（500ms后执行搜索）
 * const searchInput = document.getElementById('search-input');
 * searchInput.oninput = debounce(function(e) {
 *   ajaxRequest('/search.do', 'GET', {keyword: e.target.value}, (res) => console.log(res));
 * }, 500);
 * @note 防抖原理：每次触发重置定时器，仅最后一次触发会执行
 */
function debounce(func, wait) {
    let timeout; // 定时器标识
    // 返回包装后的函数（保留原函数参数和this指向）
    return function executedFunction(...args) {
        // 延迟执行逻辑
        const later = () => {
            clearTimeout(timeout); // 清除定时器
            func(...args); // 执行原函数
        };
        clearTimeout(timeout); // 重置定时器
        timeout = setTimeout(later, wait); // 重新设置定时器
    };
}

/**
 * 节流函数（Throttle）
 * @description 高频触发的函数，固定时间内仅执行一次，限制执行频率
 * @param {Function} func - 要节流的目标函数
 * @param {number} limit - 限制时间（毫秒）
 * @returns {Function} 包装后的节流函数
 * @example
 * // 窗口滚动节流（100ms内仅执行一次）
 * window.onscroll = throttle(function() {
 *   console.log('滚动位置：', window.scrollY);
 * }, 100);
 * @note 节流原理：通过标记控制，执行后锁定一段时间，期间不重复执行
 */
function throttle(func, limit) {
    let inThrottle; // 节流锁定标记
    // 返回包装后的函数（保留原函数this指向和参数）
    return function(...args) {
        // 未锁定时执行
        if (!inThrottle) {
            func.apply(this, args); // 执行原函数
            inThrottle = true; // 锁定
            // 限制时间后解锁
            setTimeout(() => inThrottle = false, limit);
        }
    };
}


//检查邮箱格式是否正确
function checkEmail(email){
    const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    // if (!emailReg.test(email)) {
    //     showFormError('emailTip', '邮箱格式错误');
    //     regEmail.classList.add('error');
    // } else {
    //     regEmail.classList.add('success');
    // }
    return emailReg.test(email);
}

//检查手机号格式是否正确
function checkPhoneNumber(PhoneNumber) {

     const phoneReg = /^1[3-9]\d{9}$/;
    // if (!phoneReg.test(phone)) {
    //     showFormError('phoneTip', '手机号格式错误');
    //     regPhone.classList.add('error');
    // } else {
    //     regPhone.classList.add('success');
    // }
    return phoneReg.test(PhoneNumber);

}
