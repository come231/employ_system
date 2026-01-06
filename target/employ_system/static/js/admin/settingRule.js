document.addEventListener('DOMContentLoaded', function() {
    initHeader();
    initSidebar();
    loadRule();

    // 监听输入变化实时更新可视化
    ['workStartTime', 'workEndTime', 'lateThreshold', 'earlyThreshold', 'supplementValidHours', 'fullAttendanceDays'].forEach(id => {
        document.getElementById(id).addEventListener('change', updateVisualization);
    });
});

function formatTimeTo24H(timeStr, defaultTime) {
    const defaultVal = defaultTime || '09:00';
    if (!timeStr || typeof timeStr !== 'string' || timeStr.trim() === '') {
        return defaultVal;
    }

    const timeStrTrim = timeStr.trim();
    // 修正：用indexOf判断，避免编码问题
    const isAM = timeStrTrim.indexOf('上午') !== -1;
    const isPM = timeStrTrim.indexOf('下午') !== -1;
    let hour = 0;
    let minute = 0;

    // 提取时间部分（去掉“上午/下午”）
    const timePart = timeStrTrim.replace('上午', '').replace('下午', '').trim();
    const timeParts = timePart.split(':');
    if (timeParts.length >= 2) {
        hour = parseInt(timeParts[0], 10);
        minute = parseInt(timeParts[1], 10);
    } else {
        return defaultVal;
    }

    // 12转24小时制
    if (isPM && hour !== 12) {
        hour += 12;
    } else if (isAM && hour === 12) {
        hour = 0;
    }

    // 校验有效性
    if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
        return defaultVal;
    }

    // 补前导0
    const hour24 = hour.toString().padStart(2, '0');
    const minute2 = minute.toString().padStart(2, '0');
    return `${hour24}:${minute2}`;
}


function loadRule() {
    ajaxRequest('/admin/setting/rule/data.do', 'GET', {},
        function(result) {
            if (result.code === 200 || result.code === '200') {
                const rule = result.data;
                // 修正：后端字段名是workEndTime（大写T），前端要对应
                console.log(rule);
                console.log(rule.workStartTime);
                const rawStartTime = rule.workStartTime || '09:00:00 上午';
                const rawEndTime = rule.workEndTime || '06:00:00 下午'; // 改为workEndTime

                const startTime24 = formatTimeTo24H(rawStartTime, '09:00');
                const endTime24 = formatTimeTo24H(rawEndTime, '18:00');

                document.getElementById('workStartTime').value = startTime24;
                document.getElementById('workEndTime').value = endTime24;
                console.log(startTime24, endTime24);

                // 其他字段赋值（保留原逻辑，补充注释）
                document.getElementById('lateThreshold').value = rule.lateThreshold || 30; // 迟到阈值默认30分钟
                document.getElementById('earlyThreshold').value = rule.earlyThreshold || 30; // 早退阈值默认30分钟
                document.getElementById('supplementValidHours').value = rule.supplementValidHours || 24; // 补卡有效期默认24小时
                document.getElementById('fullAttendanceDays').value = rule.fullAttendanceDays || 22; // 全勤天数默认22天

                // 更新可视化界面
                updateVisualization();
            } else {
                showMessage('加载规则失败: ' + (result.msg || '未知错误'), 'error');
            }
        },
        function(error) {
            showMessage('加载规则失败: 网络异常', 'error');
        }
    );
}


function updateVisualization() {
    // 获取表单值（为空时用默认值）
    const startTime = document.getElementById('workStartTime').value || '09:00'; // 上班默认09:00
    const endTime = document.getElementById('workEndTime').value || '18:00'; // 下班默认18:00
    const lateThreshold = parseInt(document.getElementById('lateThreshold').value) || 30;
    const earlyThreshold = parseInt(document.getElementById('earlyThreshold').value) || 30;
    const supplementHours = document.getElementById('supplementValidHours').value || 24;
    const fullDays = document.getElementById('fullAttendanceDays').value || 22;

    // 更新规则卡片的显示
    document.getElementById('cardWorkTime').textContent = startTime + ' ~ ' + endTime;
    document.getElementById('cardLate').textContent = lateThreshold;
    document.getElementById('cardEarly').textContent = earlyThreshold;
    document.getElementById('cardSupplement').textContent = supplementHours;
    document.getElementById('cardFullDays').textContent = fullDays;

    // 时间转百分比（用于时间轴定位：基于6:00-22:00的时间范围）
    const timeToPercent = (time) => {
        const [h, m] = time.split(':').map(Number);
        const minutes = h * 60 + m;
        // 总时间范围：22:00(1320分钟) - 6:00(360分钟) = 960分钟
        return ((minutes - 360) / 960) * 100;
    };

    // 时间增加/减少分钟
    const addMinutes = (time, mins) => {
        const [h, m] = time.split(':').map(Number);
        const total = h * 60 + m + mins;
        const newH = Math.floor(total / 60);
        const newM = total % 60;
        return String(newH).padStart(2, '0') + ':' + String(newM).padStart(2, '0');
    };

    // 计算各时间点的百分比位置
    const startPercent = timeToPercent(startTime);
    const endPercent = timeToPercent(endTime);
    const lateTime = addMinutes(startTime, lateThreshold); // 迟到线时间
    const earlyTime = addMinutes(endTime, -earlyThreshold); // 早退线时间
    const latePercent = timeToPercent(lateTime);
    const earlyPercent = timeToPercent(earlyTime);

    // 设置时间轴的工作时段条
    document.getElementById('workPeriod').style.left = startPercent + '%';
    document.getElementById('workPeriod').style.width = (endPercent - startPercent) + '%';

    // 更新时间轴标记的位置和显示文本
    document.getElementById('markerStart').style.left = startPercent + '%';
    document.getElementById('markerStartTime').textContent = startTime;
    document.getElementById('markerEnd').style.left = endPercent + '%';
    document.getElementById('markerEndTime').textContent = endTime;
    document.getElementById('markerLate').style.left = latePercent + '%';
    document.getElementById('markerLateTime').textContent = lateTime;
    document.getElementById('markerEarly').style.left = earlyPercent + '%';
    document.getElementById('markerEarlyTime').textContent = earlyTime;
}
function saveRule() {
    const params = {
        ruleId: 1,
        workStartTime: document.getElementById('workStartTime').value,
        workEndTime: document.getElementById('workEndTime').value,
        lateThreshold: document.getElementById('lateThreshold').value,
        earlyThreshold: document.getElementById('earlyThreshold').value,
        supplementValidHours: document.getElementById('supplementValidHours').value,
        fullAttendanceDays: document.getElementById('fullAttendanceDays').value
    };

    // 验证
    if (!params.workStartTime || !params.workEndTime) {
        showMessage('请设置工作时间', 'warning');
        return;
    }

    ajaxRequest('/admin/setting/rule/update.do', 'POST', params,
        function(result) {
            if (result.code === 200 || result.code === '200') {
                showMessage('保存成功', 'success');
            } else {
                showMessage('保存失败: ' + (result.msg || '未知错误'), 'error');
            }
        },
        function(error) {
            showMessage('保存失败: 网络异常', 'error');
        }
    );
}