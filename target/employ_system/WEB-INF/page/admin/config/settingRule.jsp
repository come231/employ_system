<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>考勤规则设置 - 员工考勤管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <link href="${pageContext.request.contextPath}/static/css/admin/common/common.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/header.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/static/css/admin/sidebar.css" rel="stylesheet">
    <style>
        .main-content {
            margin-left: 280px;
            padding: 30px;
            min-height: calc(100vh - 70px);
        }

        /* 可视化卡片样式 */
        .rule-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .rule-card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            text-align: center;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .rule-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 4px 15px rgba(0,0,0,0.15);
        }
        .rule-card .icon {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 15px;
            font-size: 24px;
        }
        .rule-card.work-time .icon { background: #e3f2fd; color: #1976d2; }
        .rule-card.late .icon { background: #fff3e0; color: #f57c00; }
        .rule-card.early .icon { background: #fce4ec; color: #c2185b; }
        .rule-card.supplement .icon { background: #e8f5e9; color: #388e3c; }
        .rule-card.attendance .icon { background: #f3e5f5; color: #7b1fa2; }
        .rule-card .value {
            font-size: 28px;
            font-weight: 700;
            color: #333;
            margin-bottom: 5px;
        }
        .rule-card .label {
            font-size: 14px;
            color: #666;
        }

        /* 时间轴样式 */
        .timeline-container {
            background: white;
            border-radius: 12px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .timeline-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #333;
        }
        .timeline-visual {
            position: relative;
            height: 80px;
            background: linear-gradient(90deg, #f5f5f5 0%, #e3f2fd 25%, #e3f2fd 75%, #f5f5f5 100%);
            border-radius: 8px;
            margin: 20px 0;
        }
        .timeline-bar {
            position: absolute;
            top: 50%;
            left: 0;
            right: 0;
            height: 4px;
            background: #ddd;
            transform: translateY(-50%);
        }
        .timeline-work-period {
            position: absolute;
            top: 50%;
            height: 8px;
            background: linear-gradient(90deg, #4caf50, #2196f3);
            transform: translateY(-50%);
            border-radius: 4px;
        }
        .timeline-marker {
            position: absolute;
            top: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
        }
        .timeline-marker .dot {
            width: 16px;
            height: 16px;
            border-radius: 50%;
            border: 3px solid white;
            box-shadow: 0 2px 4px rgba(0,0,0,0.2);
            margin: 0 auto;
        }
        .timeline-marker .time {
            font-size: 12px;
            font-weight: 600;
            margin-top: 8px;
            white-space: nowrap;
        }
        .timeline-marker .label {
            font-size: 11px;
            color: #666;
        }
        .timeline-marker.start .dot { background: #4caf50; }
        .timeline-marker.end .dot { background: #f44336; }
        .timeline-marker.late .dot { background: #ff9800; }
        .timeline-marker.early .dot { background: #e91e63; }

        .timeline-hours {
            display: flex;
            justify-content: space-between;
            margin-top: 10px;
            font-size: 11px;
            color: #999;
        }

        /* 编辑表单样式 */
        .form-container {
            background: white;
            border-radius: 12px;
            padding: 30px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .form-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #333;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .form-title i {
            color: #1976d2;
        }
        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
        }
        .form-group {
            margin-bottom: 0;
        }
        .form-group label {
            font-weight: 500;
            color: #555;
            margin-bottom: 8px;
            display: block;
        }
        .form-group .input-group-text {
            background: #f8f9fa;
            border-color: #ced4da;
        }
    </style>
</head>
<body>
<jsp:include page="../common/header.jsp"></jsp:include>
<jsp:include page="../common/sidebar.jsp">
    <jsp:param name="currentPage" value="settingRule"/>
</jsp:include>

<div class="main-content">
    <h2><i class="fa fa-cog"></i> 考勤规则设置</h2>

    <!-- 规则可视化卡片 -->
    <div class="rule-cards mt-4">
        <div class="rule-card work-time">
            <div class="icon"><i class="fa fa-clock-o"></i></div>
            <div class="value" id="cardWorkTime">--:-- ~ --:--</div>
            <div class="label">工作时间</div>
        </div>
        <div class="rule-card late">
            <div class="icon"><i class="fa fa-hourglass-half"></i></div>
            <div class="value"><span id="cardLate">--</span> 分钟</div>
            <div class="label">迟到阈值</div>
        </div>
        <div class="rule-card early">
            <div class="icon"><i class="fa fa-sign-out"></i></div>
            <div class="value"><span id="cardEarly">--</span> 分钟</div>
            <div class="label">早退阈值</div>
        </div>
        <div class="rule-card supplement">
            <div class="icon"><i class="fa fa-pencil-square-o"></i></div>
            <div class="value"><span id="cardSupplement">--</span> 小时</div>
            <div class="label">补卡有效期</div>
        </div>
        <div class="rule-card attendance">
            <div class="icon"><i class="fa fa-calendar-check-o"></i></div>
            <div class="value"><span id="cardFullDays">--</span> 天</div>
            <div class="label">月度全勤标准</div>
        </div>
    </div>

    <!-- 时间轴可视化 -->
    <div class="timeline-container">
        <div class="timeline-title"><i class="fa fa-line-chart"></i> 工作时间可视化</div>
        <div class="timeline-visual" id="timelineVisual">
            <div class="timeline-bar"></div>
            <div class="timeline-work-period" id="workPeriod"></div>
            <div class="timeline-marker start" id="markerStart">
                <div class="dot"></div>
                <div class="time" id="markerStartTime">09:00</div>
                <div class="label">上班</div>
            </div>
            <div class="timeline-marker late" id="markerLate">
                <div class="dot"></div>
                <div class="time" id="markerLateTime">09:15</div>
                <div class="label">迟到线</div>
            </div>
            <div class="timeline-marker early" id="markerEarly">
                <div class="dot"></div>
                <div class="time" id="markerEarlyTime">17:45</div>
                <div class="label">早退线</div>
            </div>
            <div class="timeline-marker end" id="markerEnd">
                <div class="dot"></div>
                <div class="time" id="markerEndTime">18:00</div>
                <div class="label">下班</div>
            </div>
        </div>
        <div class="timeline-hours">
            <span>06:00</span>
            <span>08:00</span>
            <span>10:00</span>
            <span>12:00</span>
            <span>14:00</span>
            <span>16:00</span>
            <span>18:00</span>
            <span>20:00</span>
            <span>22:00</span>
        </div>
    </div>

    <!-- 编辑表单 -->
    <div class="form-container">
        <div class="form-title"><i class="fa fa-edit"></i> 编辑规则</div>
        <form id="ruleForm">
            <input type="hidden" id="ruleId" value="1">
            <div class="form-grid">
                <div class="form-group">
                    <label>上班时间</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fa fa-sun-o"></i></span>
                        <input type="time" class="form-control" id="workStartTime">
                    </div>
                </div>
                <div class="form-group">
                    <label>下班时间</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fa fa-moon-o"></i></span>
                        <input type="time" class="form-control" id="workEndTime">
                    </div>
                </div>
                <div class="form-group">
                    <label>迟到阈值</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fa fa-hourglass-half"></i></span>
                        <input type="number" class="form-control" id="lateThreshold" min="0" max="60">
                        <span class="input-group-text">分钟</span>
                    </div>
                </div>
                <div class="form-group">
                    <label>早退阈值</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fa fa-sign-out"></i></span>
                        <input type="number" class="form-control" id="earlyThreshold" min="0" max="60">
                        <span class="input-group-text">分钟</span>
                    </div>
                </div>
                <div class="form-group">
                    <label>补卡有效期</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fa fa-pencil-square-o"></i></span>
                        <input type="number" class="form-control" id="supplementValidHours" min="1" max="72">
                        <span class="input-group-text">小时</span>
                    </div>
                </div>
                <div class="form-group">
                    <label>全勤天数</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fa fa-calendar-check-o"></i></span>
                        <input type="number" class="form-control" id="fullAttendanceDays" min="1" max="31">
                        <span class="input-group-text">天/月</span>
                    </div>
                </div>
            </div>
            <div class="mt-4">
                <button type="button" class="btn btn-primary" onclick="saveRule()">
                    <i class="fa fa-save"></i> 保存设置
                </button>
                <button type="button" class="btn btn-outline-secondary ms-2" onclick="loadRule()">
                    <i class="fa fa-refresh"></i> 重置
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    window.baseUrl = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/static/js/admin/common/common.js"></script>
<script src="${pageContext.request.contextPath}/static/js/admin/settingRule.js"></script>
</body>
</html>
