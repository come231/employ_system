package com.hkd.controller.reception;

import com.hkd.dto.NoticeMessage;
import com.hkd.dto.PunchMessage;
import com.hkd.mapper.*;
import com.hkd.pojo.*;
import com.hkd.service.AttendanceRecordService;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.RedisUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * 前台首页控制器
 */
@Controller
@RequestMapping("/reception/home")
public class ReceptionHomeController {

    @Autowired
    private AttendanceRecordDao attendanceRecordDao;

    @Autowired
    private AttendanceRecordService attendanceRecordService;

    @Autowired
    private LeaveApplicationDao leaveApplicationDao;

    @Autowired
    private SupplementCardDao supplementCardDao;

    @Autowired
    private EmpInfoDao empInfoDao;

    @Autowired
    private SysDeptDao sysDeptDao;

    @Autowired
    private SysNotificationDao sysNotificationDao;

    @Autowired
    private AttendanceRuleDao attendanceRuleDao;

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    @Autowired
    private RedisUtil redisUtil;

    // 注入 rabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取首页汇总数据
     */
    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    @ResponseBody
    public String getSummary(HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            Map<String, Object> data = new HashMap<>();

            // 获取用户基本信息
            data.put("empName", session.getAttribute("receptionEmpName"));
            data.put("deptName", session.getAttribute("receptionDeptName"));
            data.put("avatar", session.getAttribute("receptionAvatar"));
            data.put("role", session.getAttribute("receptionRole"));

            // 计算本月考勤率
            LocalDate now = LocalDate.now();
            LocalDate monthStart = now.withDayOfMonth(1);
            List<AttendanceRecord> monthRecords = attendanceRecordDao.findByEmpIdAndDateRange(
                    empId, monthStart, now, -1, 1);
            System.out.println("数据：" + monthRecords);
            int totalDays = now.getDayOfMonth();
            int attendedDays = monthRecords != null ? monthRecords.size() : 0;
            double attendanceRate = totalDays > 0 ? (attendedDays * 100.0 / totalDays) : 0;
            data.put("monthlyAttendanceRate", String.format("%.1f", attendanceRate));

            // 获取待审核的请假申请数量
            List<LeaveApplication> pendingLeaves = leaveApplicationDao.findByEmpId(empId, 0, 100);
            int pendingLeaveCount = 0;
            if (pendingLeaves != null) {
                for (LeaveApplication la : pendingLeaves) {
                    if (la.getApproveStatus() != null && la.getApproveStatus() == 0) {
                        pendingLeaveCount++;
                    }
                }
            }
            data.put("pendingLeaveCount", pendingLeaveCount);

            // 获取待审核的补卡申请数量
            List<SupplementCard> pendingSupplements = supplementCardDao.findByEmpId(empId, 0, 100);
            int pendingSupplementCount = 0;
            if (pendingSupplements != null) {
                for (SupplementCard sc : pendingSupplements) {
                    if (sc.getApproveStatus() != null && sc.getApproveStatus() == 0) {
                        pendingSupplementCount++;
                    }
                }
            }
            data.put("pendingSupplementCount", pendingSupplementCount);

            // 获取今日打卡状态
            LocalDate today = LocalDate.now();
            List<AttendanceRecord> todayRecords = attendanceRecordDao.findByEmpIdAndDateRange(
                    empId, today, today, -1, 1);
            String todayStatus = "unchecked";
            if (todayRecords != null && !todayRecords.isEmpty()) {
                todayStatus = "checked_in";
            }
            data.put("todayStatus", todayStatus);

            // 获取未读通知数量
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            int unreadCount = 0;
            if (sysNotificationDao != null) {
                try {
                    unreadCount = sysNotificationDao.countUnreadByUserId(userId);
                } catch (Exception e) {
                    // 通知表可能还不存在
                }
            }
            data.put("unreadNotificationCount", unreadCount);

            // 计算平均审批时长
            String averageApprovalTime = "0小时";
            try {
                Double avgSeconds = leaveApplicationDao.getAverageApprovalTimeInSeconds();
                if (avgSeconds != null && avgSeconds > 0) {
                    long hours = Math.round(avgSeconds / 3600);
                    if (hours < 1) {
                        long minutes = Math.round(avgSeconds / 60);
                        averageApprovalTime = minutes + "分钟";
                    } else if (hours > 24) {
                        long days = Math.round(hours / 24.0);
                        averageApprovalTime = days + "天";
                    } else {
                        averageApprovalTime = hours + "小时";
                    }
                }
            } catch (Exception e) {
                // 忽略错误，使用默认值
            }
            data.put("averageApprovalTime", averageApprovalTime);

            // 计算审批通过率（请假+补卡综合）
            String approvalPassRate = "0%";
            try {
                Integer leaveApproved = leaveApplicationDao.getApprovedCount();
                Integer leaveDecided = leaveApplicationDao.getDecidedCount();
                Integer supplementApproved = supplementCardDao.getApprovedCount();
                Integer supplementDecided = supplementCardDao.getDecidedCount();

                int totalApproved = (leaveApproved != null ? leaveApproved : 0) + (supplementApproved != null ? supplementApproved : 0);
                int totalDecided = (leaveDecided != null ? leaveDecided : 0) + (supplementDecided != null ? supplementDecided : 0);

                if (totalDecided > 0) {
                    double rate = (totalApproved * 100.0) / totalDecided;
                    approvalPassRate = String.format("%.0f%%", rate);
                }
            } catch (Exception e) {
                // 忽略错误，使用默认值
            }
            data.put("approvalPassRate", approvalPassRate);

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取月度考勤日历数据
     */
    @RequestMapping(value = "/calendar", method = RequestMethod.GET)
    @ResponseBody
    public String getCalendar(@RequestParam(value = "year", required = false) Integer year,
                              @RequestParam(value = "month", required = false) Integer month,
                              HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            // 默认使用当前年月
            LocalDate now = LocalDate.now();
            if (year == null) year = now.getYear();
            if (month == null) month = now.getMonthValue();

            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            // 获取当月所有考勤记录
            List<AttendanceRecord> records = attendanceRecordDao.findByEmpIdAndDateRange(
                    empId, monthStart, monthEnd, -1, 1);

            // 构建日历数据
            List<Map<String, Object>> calendarData = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if (records != null) {
                for (AttendanceRecord record : records) {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", sdf.format(record.getCheckDate()));
                    dayData.put("status", record.getStatus());
                    dayData.put("checkTime", record.getCheckInTime() != null ?
                            new SimpleDateFormat("HH:mm:ss").format(record.getCheckInTime()) : "");
                    calendarData.add(dayData);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("year", year);
            result.put("month", month);
            result.put("records", calendarData);

            return JsonUtil.objectToJson(result, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 提交请假申请
     */
    @RequestMapping(value = "/leave/apply", method = RequestMethod.POST)
    @ResponseBody
    public String applyLeave(HttpServletRequest request, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            String leaveType = request.getParameter("leaveType");
            String startTimeStr = request.getParameter("startTime");
            String endTimeStr = request.getParameter("endTime");
            String leaveDaysStr = request.getParameter("leaveDays");
            String reason = request.getParameter("reason");

            if (CommonUtil.isEmpty(leaveType) || CommonUtil.isEmpty(startTimeStr) ||
                    CommonUtil.isEmpty(endTimeStr)) {
                return JsonUtil.buildFalseJson(400, "请填写完整的请假信息！");
            }

            // 支持多种日期格式
            Date startTime = parseDateTime(startTimeStr);
            Date endTime = parseDateTime(endTimeStr);

            if (startTime == null || endTime == null) {
                return JsonUtil.buildFalseJson(400, "日期格式错误！");
            }

            Double leaveDays = 1.0;
            if (!CommonUtil.isEmpty(leaveDaysStr)) {
                try {
                    leaveDays = Double.parseDouble(leaveDaysStr);
                } catch (Exception e) {
                    leaveDays = 1.0;
                }
            }

            LeaveApplication leave = new LeaveApplication();
            leave.setEmpId(empId);
            leave.setLeaveType(leaveType);
            leave.setStartTime(startTime);
            leave.setEndTime(endTime);
            leave.setLeaveDays(leaveDays);
            leave.setReason(reason);
            leave.setApproveStatus((byte) 0); // 待审批
            leave.setCreateTime(new Date());

            // 检查是否为部门主管，主管的请假申请需要提交给超级管理员审批
            String role = (String) session.getAttribute("receptionRole");
            if ("manager".equals(role)) {
                // 查找超级管理员（roleId=1）
                Integer superAdminEmpId = findSuperAdminEmpId();
                if (superAdminEmpId != null) {
                    leave.setApproverId(superAdminEmpId);
                }
            }

            leaveApplicationDao.insert(leave);

            // 发送待审批通知给审批人
            String empName = (String) session.getAttribute("receptionEmpName");
            String leaveTypeText = leaveType;
            if ("manager".equals(role)) {
                // 主管的请假通知超级管理员
                Integer superAdminUserId = findSuperAdminUserId();
                if (superAdminUserId != null) {
                    sendNotification(superAdminUserId, "leave_pending", "新请假申请待审批",
                            empName + "提交了一条" + leaveTypeText + "申请，请及时处理", leave.getLeaveId());
                }
            } else {
                // 普通员工的请假通知部门主管
                Integer managerUserId = findDeptManagerUserId(empId);
                if (managerUserId != null) {
                    sendNotification(managerUserId, "leave_pending", "新请假申请待审批",
                            empName + "提交了一条" + leaveTypeText + "申请，请及时处理", leave.getLeaveId());
                }
            }

            String message = "manager".equals(role) ? "请假申请已提交，将由超级管理员审批！" : "请假申请提交成功！";
            return JsonUtil.objectToJson(leave, message);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 查找超级管理员的员工ID
     *
     * @return 超级管理员的empId，如果未找到返回null
     */
    private Integer findSuperAdminEmpId() {
        try {
            // 查找角色ID为1（超级管理员）的用户
            List<SysUserRole> adminRoles = sysUserRoleDao.findByRoleId(1);
            if (adminRoles != null && !adminRoles.isEmpty()) {
                // 获取第一个超级管理员的userId
                Integer adminUserId = adminRoles.get(0).getUserId();
                // 通过userId查找对应的empId
                SysUser adminUser = sysUserDao.findById(adminUserId);
                if (adminUser != null) {
                    return adminUser.getEmpId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查找超级管理员的userId（用于发送通知）
     *
     * @return 超级管理员的userId，如果未找到返回null
     */
    private Integer findSuperAdminUserId() {
        try {
            List<SysUserRole> adminRoles = sysUserRoleDao.findByRoleId(1);
            if (adminRoles != null && !adminRoles.isEmpty()) {
                return adminRoles.get(0).getUserId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查找员工所在部门主管的userId
     *
     * @param empId 员工ID
     * @return 部门主管的userId，如果未找到返回null
     */
    private Integer findDeptManagerUserId(Integer empId) {
        try {
            // 获取员工信息
            EmpInfo empInfo = empInfoDao.findById(empId);
            if (empInfo == null || empInfo.getDeptId() == null) {
                return null;
            }

            // 获取部门信息
            SysDept dept = sysDeptDao.findById(empInfo.getDeptId());
            if (dept == null || dept.getManagerId() == null) {
                return null;
            }

            // 获取部门主管的userId
            SysUser managerUser = sysUserDao.findByEmpId(dept.getManagerId());
            if (managerUser != null) {
                return managerUser.getUserId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送通知
     *
     * @param userId    接收通知的用户ID
     * @param type      通知类型
     * @param title     通知标题
     * @param content   通知内容
     * @param relatedId 关联ID
     */
    private void sendNotification(Integer userId, String type, String title, String content, Integer relatedId) {
        try {
            if (userId == null || sysNotificationDao == null) {
                return;
            }
//            SysNotification notification = new SysNotification();
//            notification.setUserId(userId);
//            notification.setType(type);
//            notification.setTitle(title);
//            notification.setContent(content);
//            notification.setRelatedId(relatedId != null ? relatedId : 0);
//            notification.setIsRead((byte) 0);
//            notification.setCreateTime(new Date());
            //sysNotificationDao.insert(notification);
            NoticeMessage msg = new NoticeMessage();
            msg.setUserId(userId);
            msg.setType(type);
            msg.setTitle(title);
            msg.setContent(content);
            msg.setRelatedId(relatedId);
            // 原来的代码
// rabbitTemplate.convertAndSend(..., msg);

// 改成 JSON 字符串发送（SSM 最稳）
            String json = com.alibaba.fastjson.JSON.toJSONString(msg);
            rabbitTemplate.convertAndSend(
                    "notice.exchange",   // 正确
                    "notice.route",      // 正确
                    json
            );

        } catch (Exception e) {
            // 通知发送失败不影响主流程
            e.printStackTrace();
        }
    }

    /**
     * 解析日期时间字符串，支持多种格式
     */
    private Date parseDateTime(String dateStr) {
        if (CommonUtil.isEmpty(dateStr)) return null;

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                return sdf.parse(dateStr);
            } catch (Exception e) {
                // 尝试下一个格式
            }
        }
        return null;
    }

    /**
     * 打卡接口 - 根据时间自动区分上班/下班打卡（<12点上班，≥12点下班）
     */
    @RequestMapping(value = "/punch", method = RequestMethod.POST)
    @ResponseBody
    public String punch(HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            // 获取当前时间
            Calendar cal = Calendar.getInstance();
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            int currentMinute = cal.get(Calendar.MINUTE);
            int currentTotalMinutes = currentHour * 60 + currentMinute;
            LocalDate today = LocalDate.now();

            // ============= 核心修改：自动判断上班/下班 =============
            int punchType; // 1=上班 0=下班
            String punchTypeName;
            if (currentHour < 12) {
                punchType = 1;
                punchTypeName = "上班";
            } else {
                punchType = 0;
                punchTypeName = "下班";
            }

            // 检查今日是否已打过【当前类型】的卡（上班/下班分开判断）
            List<AttendanceRecord> todayTypeRecords = attendanceRecordDao.findByEmpIdAndDateRange(
                    empId, today, today, -1, punchType);
            if (todayTypeRecords != null && !todayTypeRecords.isEmpty()) {
                return JsonUtil.buildFalseJson(400, "今日" + punchTypeName + "已打卡！");
            }

            // 获取考勤规则
            AttendanceRule rule = attendanceRuleDao.findLatest();
            if (rule == null) {
                return JsonUtil.buildFalseJson(500, "未配置考勤规则！");
            }

            // 创建打卡记录
            AttendanceRecord record = new AttendanceRecord();
            record.setEmpId(empId);
            record.setCheckDate(java.sql.Date.valueOf(today));
            record.setType(punchType); // 自动设置打卡类型
            record.setCheckInTime(new Date());
            record.setCreateTime(new Date());
            String msg = "";

            // ============= 上班打卡逻辑（完全保留你原来的代码）=============
            if (punchType == 1) {
                // 解析上班时间
                Time workStartTime = rule.getWorkStartTime();
                int workStartHour = workStartTime.getHours();
                int workStartMinute = workStartTime.getMinutes();
                int workStartTotalMinutes = workStartHour * 60 + workStartMinute;

                // 迟到阈值
                int lateThreshold = rule.getLateThreshold() != null ? rule.getLateThreshold() : 15;
                int lateLimitMinutes = workStartTotalMinutes + lateThreshold;

                // 超过迟到阈值，不能打卡
                if (currentTotalMinutes > lateLimitMinutes) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("canPunch", false);
                    data.put("status", 3);
                    data.put("message", "已超过上班打卡时间，请申请补卡！");
                    return JsonUtil.buildFalseJson(400, "已超过打卡时间，只能申请补卡！");
                }

                // 判断迟到
                if (currentTotalMinutes > workStartTotalMinutes) {
                    record.setStatus((byte) 1);
                    record.setRemark("上班迟到（" + (currentTotalMinutes - workStartTotalMinutes) + "分钟）");
                    msg = "上班打卡成功（迟到）！";
                } else {
                    record.setStatus((byte) 0);
                    record.setRemark("正常上班打卡");
                    msg = "上班打卡成功！";
                }
            }
            // ============= 下班打卡逻辑（新增：早退判断）=============
            else {
                // 解析下班时间
                Time workEndTime = rule.getWorkEndTime();
                int workEndHour = workEndTime.getHours();
                int workEndMinute = workEndTime.getMinutes();
                int workEndTotalMinutes = workEndHour * 60 + workEndMinute;

                // 早退阈值
                int earlyThreshold = rule.getEarlyThreshold() != null ? rule.getEarlyThreshold() : 15;
                int earlyLimitMinutes = workEndTotalMinutes - earlyThreshold;

                // 判断早退
                if (currentTotalMinutes < earlyLimitMinutes) {
                    record.setStatus((byte) 2); // 早退
                    record.setRemark("下班早退（" + (earlyLimitMinutes - currentTotalMinutes) + "分钟）");
                    msg = "下班打卡成功（早退）！";
                } else {
                    record.setStatus((byte) 0); // 正常
                    record.setRemark("正常下班打卡");
                    msg = "下班打卡成功！";
                }
            }

            // 插入记录
//            attendanceRecordDao.insert(record);

            // 构建消息
            PunchMessage msg1 = new PunchMessage();
            msg1.setEmpId(empId);
            msg1.setPunchType(punchType);
            msg1.setCheckTime(new Date());
            msg1.setStatus(record.getStatus());
            msg1.setRemark(record.getRemark());

            // 原来的代码
// rabbitTemplate.convertAndSend(..., msg);

// 改成 JSON 字符串发送（SSM 最稳）
            String json = com.alibaba.fastjson.JSON.toJSONString(msg1);
            rabbitTemplate.convertAndSend(
                    "punch.exchange",   // 正确
                    "punch.route",      // 正确
                    json
            );

            return JsonUtil.objectToJson(null,"打卡成功!");

//            Map<String, Object> data = new HashMap<>();
//            data.put("punchType", punchType);
//            data.put("punchTypeName", punchTypeName);
//            data.put("status", record.getStatus());
//            data.put("checkTime", new SimpleDateFormat("HH:mm:ss").format(record.getCheckInTime()));
//            data.put("message", msg);
//
//            return JsonUtil.objectToJson(data, msg);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取今日打卡状态 - 区分上班/下班，互不影响
     */
    @RequestMapping(value = "/punch/status", method = RequestMethod.GET)
    @ResponseBody
    public String getPunchStatus(HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            LocalDate today = LocalDate.now();
            Calendar cal = Calendar.getInstance();
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            int currentMinute = cal.get(Calendar.MINUTE);
            int currentTotalMinutes = currentHour * 60 + currentMinute;

            // ============= 自动判断当前是上班/下班时段 =============
            int currentPunchType; // 1=上班 0=下班
            String currentPunchTypeName;
            if (currentHour < 12) {
                currentPunchType = 1;
                currentPunchTypeName = "上班";
            } else {
                currentPunchType = 0;
                currentPunchTypeName = "下班";
            }

            // 检查今日是否已批准请假
            List<LeaveApplication> approvedLeaves = leaveApplicationDao.findByEmpId(empId, 0, 100);
            Date todayDate = java.sql.Date.valueOf(today);
            boolean onLeaveToday = false;
            if (approvedLeaves != null) {
                for (LeaveApplication leave : approvedLeaves) {
                    if (leave.getApproveStatus() != null && leave.getApproveStatus() == 1) {
                        if (leave.getStartTime() != null && leave.getEndTime() != null) {
                            if (!todayDate.before(leave.getStartTime()) && !todayDate.after(leave.getEndTime())) {
                                onLeaveToday = true;
                                break;
                            }
                        }
                    }
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("currentPunchType", currentPunchType);
            data.put("currentPunchTypeName", currentPunchTypeName);

            // 今日请假，直接返回
            if (onLeaveToday) {
                AttendanceRule ruleForLeave = attendanceRuleDao.findLatest();
                String workStartTimeStr = "09:00";
                String workEndTimeStr = "18:00";
                if (ruleForLeave != null) {
                    Time workStartTime = ruleForLeave.getWorkStartTime();
                    Time workEndTime = ruleForLeave.getWorkEndTime();
                    workStartTimeStr = String.format("%02d:%02d", workStartTime.getHours(), workStartTime.getMinutes());
                    workEndTimeStr = String.format("%02d:%02d", workEndTime.getHours(), workEndTime.getMinutes());
                }
                data.put("workStartTime", workStartTimeStr);
                data.put("workEndTime", workEndTimeStr);
                data.put("punched", false);
                data.put("canPunch", false);
                data.put("onLeave", true);
                data.put("status", 5);
                data.put("statusText", "已请假");
                data.put("message", "今日已批准请假，无需打卡");
                return JsonUtil.objectToJson(data, "获取成功！");
            }

            // ============= 查询当前时段（上班/下班）的打卡记录 =============
            List<AttendanceRecord> todayTypeRecords = attendanceRecordDao.findByEmpIdAndDateRange(
                    empId, today, today, -1, currentPunchType);

            // 获取考勤规则
            AttendanceRule rule = attendanceRuleDao.findLatest();
            String workStartTimeStr = "09:00";
            String workEndTimeStr = "18:00";
            int lateThreshold = 15;
            int earlyThreshold = 15;

            if (rule != null) {
                Time workStartTime = rule.getWorkStartTime();
                Time workEndTime = rule.getWorkEndTime();
                lateThreshold = rule.getLateThreshold() != null ? rule.getLateThreshold() : 15;
                earlyThreshold = rule.getEarlyThreshold() != null ? rule.getEarlyThreshold() : 15;
                workStartTimeStr = String.format("%02d:%02d", workStartTime.getHours(), workStartTime.getMinutes());
                workEndTimeStr = String.format("%02d:%02d", workEndTime.getHours(), workEndTime.getMinutes());
            }

            data.put("workStartTime", workStartTimeStr);
            data.put("workEndTime", workEndTimeStr);
            data.put("lateThreshold", lateThreshold);
            data.put("earlyThreshold", earlyThreshold);

            // ============= 已打卡：显示当前时段状态 =============
            if (todayTypeRecords != null && !todayTypeRecords.isEmpty()) {
                AttendanceRecord record = todayTypeRecords.get(0);
                data.put("punched", true);
                data.put("canPunch", false);
                data.put("checkTime", record.getCheckInTime() != null ?
                        new SimpleDateFormat("HH:mm:ss").format(record.getCheckInTime()) : "");
                data.put("status", record.getStatus());
                data.put("statusText", getStatusText(record.getStatus()));
            }
            // ============= 未打卡：根据时段判断可打卡状态 =============
            else {
                data.put("punched", false);
                if (rule != null) {
                    // -------- 上班时段判断 --------
                    if (currentPunchType == 1) {
                        Time workStartTime = rule.getWorkStartTime();
                        int workStartTotalMinutes = workStartTime.getHours() * 60 + workStartTime.getMinutes();
                        int lateLimitMinutes = workStartTotalMinutes + lateThreshold;

                        if (currentTotalMinutes > lateLimitMinutes) {
                            data.put("canPunch", false);
                            data.put("status", 3);
                            data.put("statusText", "缺勤");
                            data.put("message", "已超过上班打卡时间，请申请补卡");
                        } else if (currentTotalMinutes > workStartTotalMinutes) {
                            data.put("canPunch", true);
                            data.put("status", 1);
                            data.put("statusText", "迟到中");
                            data.put("message", "已迟到" + (currentTotalMinutes - workStartTotalMinutes) + "分钟");
                        } else {
                            data.put("canPunch", true);
                            data.put("status", 0);
                            data.put("statusText", "未打卡（上班）");
                        }
                    }
                    // -------- 下班时段判断 --------
                    else {
                        Time workEndTime = rule.getWorkEndTime();
                        int workEndTotalMinutes = workEndTime.getHours() * 60 + workEndTime.getMinutes();
                        int earlyLimitMinutes = workEndTotalMinutes - earlyThreshold;

                        if (currentTotalMinutes < earlyLimitMinutes) {
                            data.put("canPunch", true);
                            data.put("status", 2);
                            data.put("statusText", "早退中");
                            data.put("message", "即将早退" + (earlyLimitMinutes - currentTotalMinutes) + "分钟");
                        } else {
                            data.put("canPunch", true);
                            data.put("status", 0);
                            data.put("statusText", "未打卡（下班）");
                        }
                    }
                } else {
                    data.put("canPunch", true);
                    data.put("statusText", "未打卡");
                }
            }

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取考勤状态文本
     */
    private String getStatusText(Byte status) {
        if (status == null) return "未知";
        switch (status) {
            case 0:
                return "正常";
            case 1:
                return "迟到";
            case 2:
                return "早退";
            case 3:
                return "缺勤";
            case 4:
                return "外勤";
            default:
                return "未知";
        }
    }

    /**
     * 提交补卡申请
     */
    @RequestMapping(value = "/supplement/apply", method = RequestMethod.POST)
    @ResponseBody
    public String applySupplement(HttpServletRequest request, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            String checkDateStr = request.getParameter("checkDate");
            String checkTypeStr = request.getParameter("checkType");
            String actualTimeStr = request.getParameter("actualTime");
            String reason = request.getParameter("reason");

            if (CommonUtil.isEmpty(checkDateStr) || CommonUtil.isEmpty(checkTypeStr) ||
                    CommonUtil.isEmpty(actualTimeStr)) {
                return JsonUtil.buildFalseJson(400, "请填写完整的补卡信息！");
            }

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date checkDate = sdfDate.parse(checkDateStr);
            Date actualTime = sdfTime.parse(checkDateStr + " " + actualTimeStr);
            Byte checkType = Byte.parseByte(checkTypeStr);

            SupplementCard supplement = new SupplementCard();
            supplement.setEmpId(empId);
            supplement.setCheckDate(checkDate);
            supplement.setCheckType(checkType);
            supplement.setActualTime(actualTime);
            supplement.setReason(reason);
            supplement.setApproveStatus((byte) 0); // 待审批
            supplement.setCreateTime(new Date());

            supplementCardDao.insert(supplement);

            // 发送待审批通知给审批人
            String empName = (String) session.getAttribute("receptionEmpName");
            String role = (String) session.getAttribute("receptionRole");
            String checkTypeText = checkType == 1 ? "上班打卡" : (checkType == 2 ? "下班打卡" : "上下班打卡");

            if ("manager".equals(role)) {
                // 主管的补卡通知超级管理员
                Integer superAdminUserId = findSuperAdminUserId();
                if (superAdminUserId != null) {
                    sendNotification(superAdminUserId, "supplement_pending", "新补卡申请待审批",
                            empName + "提交了一条" + checkTypeText + "补卡申请，请及时处理", supplement.getSupplementId());
                }
            } else {
                // 普通员工的补卡通知部门主管
                Integer managerUserId = findDeptManagerUserId(empId);
                if (managerUserId != null) {
                    sendNotification(managerUserId, "supplement_pending", "新补卡申请待审批",
                            empName + "提交了一条" + checkTypeText + "补卡申请，请及时处理", supplement.getSupplementId());
                }
            }

            return JsonUtil.objectToJson(supplement, "补卡申请提交成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取通知列表
     */
    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    @ResponseBody
    public String getNotifications(HttpServletRequest request, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            // 获取分页参数
            String pageSizeStr = request.getParameter("pageSize");
            int pageSize = 10;
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                try {
                    pageSize = Integer.parseInt(pageSizeStr);
                } catch (NumberFormatException e) {
                    pageSize = 10;
                }
            }

            List<Map<String, Object>> notifications = new ArrayList<>();
            int unreadCount = 0;

            if (sysNotificationDao != null) {
                try {
                    // 获取未读数量
                    Integer unreadResult = sysNotificationDao.countUnreadByUserId(userId);
                    unreadCount = unreadResult != null ? unreadResult : 0;

                    // 获取通知列表
                    List<SysNotification> list = sysNotificationDao.findByUserId(userId, 0, pageSize);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (list != null) {
                        for (SysNotification n : list) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("notificationId", n.getNotificationId());
                            map.put("type", n.getType());
                            map.put("title", n.getTitle());
                            map.put("content", n.getContent());
                            map.put("isRead", n.getIsRead());
                            map.put("createTime", n.getCreateTime() != null ? sdf.format(n.getCreateTime()) : "");
                            notifications.add(map);
                        }
                    }
                } catch (Exception e) {
                    // 通知表可能还不存在，返回空列表
                    e.printStackTrace();
                }
            }

            // 返回包含list和unreadCount的结构
            Map<String, Object> result = new HashMap<>();
            result.put("list", notifications);
            result.put("unreadCount", unreadCount);

            return JsonUtil.objectToJson(result, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @RequestMapping(value = "/notification/read", method = RequestMethod.POST)
    @ResponseBody
    public String markNotificationRead(@RequestParam("notificationId") Integer notificationId,
                                       HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            if (sysNotificationDao != null) {
                try {
                    sysNotificationDao.markAsRead(notificationId);
                } catch (Exception e) {
                    // 忽略错误
                }
            }

            return JsonUtil.objectToJson("", "标记成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 标记所有通知为已读
     */
    @RequestMapping(value = "/notification/readAll", method = RequestMethod.POST)
    @ResponseBody
    public String markAllNotificationsRead(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            if (sysNotificationDao != null) {
                try {
                    sysNotificationDao.markAllAsRead(userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return JsonUtil.objectToJson("", "全部标记为已读！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }



    /**
     * 获取考勤排行榜数据（Redis ZSet 缓存 30分钟）
     */
    @RequestMapping(value = "/ranking", method = RequestMethod.GET)
    @ResponseBody
    public String getRanking(@RequestParam(value = "period", defaultValue = "month") String period,
                             HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            // ====================== 1. 构建 Redis Key ======================
            String redisKeyEmp = "attendance:ranking:emp:" + period;
            String redisKeyDept = "attendance:ranking:dept:" + period;
            final long CACHE_EXPIRE = 30 * 60;

            boolean hasEmpCache = redisUtil.exists(redisKeyEmp);
            boolean hasDeptCache = redisUtil.exists(redisKeyDept);

            List<Map<String, Object>> top5Employees = new ArrayList<>();
            List<Map<String, Object>> top5Depts = new ArrayList<>();

            // ====================== 2. 从 Redis 读取两个排行榜 ======================
            if (hasEmpCache && hasDeptCache) {
                // 读取员工排行
                Set<Object> empSet = redisUtil.zReverseRange(redisKeyEmp, 0, 4);
                if (empSet != null) {
                    for (Object o : empSet) {
                        top5Employees.add((Map<String, Object>) o);
                    }
                }
                // 读取部门排行
                Set<Object> deptSet = redisUtil.zReverseRange(redisKeyDept, 0, 4);
                if (deptSet != null) {
                    for (Object o : deptSet) {
                        top5Depts.add((Map<String, Object>) o);
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("employeeRanking", top5Employees);
                result.put("deptRanking", top5Depts);
                return JsonUtil.objectToJson(result, "获取成功！[缓存]");
            }

            // ====================== 3. 缓存不存在 → 查询数据库 ======================
            LocalDate now = LocalDate.now();
            LocalDate startDate;
            if ("year".equals(period)) {
                startDate = now.withDayOfYear(1);
            } else if ("lastMonth".equals(period)) {
                startDate = now.minusMonths(1).withDayOfMonth(1);
            } else {
                startDate = now.withDayOfMonth(1);
            }

            List<EmpInfo> employees = empInfoDao.findAll(0, 1000);
            List<Map<String, Object>> employeeRanking = new ArrayList<>();
            Map<Integer, Integer> deptAttendance = new HashMap<>();
            Map<Integer, Integer> deptTotal = new HashMap<>();

            for (EmpInfo emp : employees) {
                List<AttendanceRecord> records = attendanceRecordDao.findByEmpIdAndDateRange(
                        emp.getEmpId(), startDate, now, 0, 1);
                int attendanceCount = records != null ? records.size() : 0;

                List<SupplementCard> supplements = supplementCardDao.findByEmpId(emp.getEmpId(), 0, 1000);
                int supplementCount = 0;
                if (supplements != null) {
                    java.sql.Date startSql = java.sql.Date.valueOf(startDate);
                    java.sql.Date endSql = java.sql.Date.valueOf(now);
                    for (SupplementCard sc : supplements) {
                        if (sc.getApproveStatus() == 1 && sc.getCheckDate() != null) {
                            if (!sc.getCheckDate().before(startSql) && !sc.getCheckDate().after(endSql)) {
                                supplementCount++;
                            }
                        }
                    }
                }

                int count = attendanceCount + supplementCount;
                Map<String, Object> empData = new HashMap<>();
                empData.put("empName", emp.getEmpName());
                empData.put("deptId", emp.getDeptId());
                empData.put("attendanceCount", count);
                employeeRanking.add(empData);

                deptAttendance.merge(emp.getDeptId(), count, Integer::sum);
                deptTotal.merge(emp.getDeptId(), 1, Integer::sum);
            }

            // 员工排行排序
            employeeRanking.sort((a, b) -> (Integer) b.get("attendanceCount") - (Integer) a.get("attendanceCount"));
            for (int i = 0; i < Math.min(5, employeeRanking.size()); i++) {
                Map<String, Object> emp = employeeRanking.get(i);
                SysDept dept = sysDeptDao.findById((Integer) emp.get("deptId"));
                emp.put("deptName", dept != null ? dept.getDeptName() : "");
                emp.put("rank", i + 1);
                top5Employees.add(emp);
            }

            // 部门排行
            int workDays = now.getDayOfMonth();
            List<Map<String, Object>> deptRanking = new ArrayList<>();
            for (Integer deptId : deptTotal.keySet()) {
                SysDept dept = sysDeptDao.findById(deptId);
                int totalEmp = deptTotal.get(deptId);
                int totalAttend = deptAttendance.getOrDefault(deptId, 0);
                double rate = (totalAttend * 100.0) / (totalEmp * workDays);

                Map<String, Object> deptData = new HashMap<>();
                deptData.put("deptName", dept != null ? dept.getDeptName() : "");
                deptData.put("empCount", totalEmp);
                deptData.put("attendanceRate", String.format("%.1f", rate));
                deptRanking.add(deptData);
            }

            deptRanking.sort((a, b) -> Double.compare(
                    Double.parseDouble((String) b.get("attendanceRate")),
                    Double.parseDouble((String) a.get("attendanceRate"))));

            for (int i = 0; i < Math.min(5, deptRanking.size()); i++) {
                Map<String, Object> d = deptRanking.get(i);
                d.put("rank", i + 1);
                top5Depts.add(d);
            }

            // ====================== 4. 两个排行榜都存入 Redis ======================
            // 存员工
            for (Map<String, Object> e : top5Employees) {
                redisUtil.zAdd(redisKeyEmp, e, (int) e.get("attendanceCount"));
            }
            // 存部门（用考勤率当分数）
            for (Map<String, Object> d : top5Depts) {
                double rate = Double.parseDouble((String) d.get("attendanceRate"));
                redisUtil.zAdd(redisKeyDept, d, rate);
            }

            redisUtil.expire(redisKeyEmp, CACHE_EXPIRE);
            redisUtil.expire(redisKeyDept, CACHE_EXPIRE);

            // ====================== 5. 返回 ======================
            Map<String, Object> result = new HashMap<>();
            result.put("employeeRanking", top5Employees);
            result.put("deptRanking", top5Depts);
            return JsonUtil.objectToJson(result, "获取成功！[数据库]");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    @GetMapping("/exportMonthAttendance")
    public void exportMonthAttendance(
            HttpServletResponse response,
            HttpSession session
    ) throws Exception {

        // 1. 获取当前登录员工信息
        Integer empId = (Integer) session.getAttribute("receptionEmpId");
        String empName = (String) session.getAttribute("receptionEmpName");

        if (empId == null) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
            return;
        }

        // 2. 查询【本月所有考勤记录】
        List<AttendanceRecord> recordList = attendanceRecordService.getMonthAttendance(empId);

        // 3. 创建 Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("考勤记录");

        // 表头
        String[] headers = {
                "日期", "星期", "上班打卡", "下班打卡",
                "考勤状态", "迟到(分)", "早退(分)", "备注"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        // 4. 写入数据
        int row = 1;
        for (AttendanceRecord r : recordList) {
            Row dataRow = sheet.createRow(row++);

            // 日期
            dataRow.createCell(0).setCellValue(r.getCheckDate().toString());
            // 星期
            dataRow.createCell(1).setCellValue(getWeekDay(r.getCheckDate()));
            // 上班
            dataRow.createCell(2).setCellValue(r.getCheckInTime() == null ? "" : r.getCheckInTime().toString());
            // 状态 0正常 1迟到 2早退 3缺勤
            String status = r.getStatus() == 0 ? "正常"
                    : r.getStatus() == 1 ? "迟到"
                    : r.getStatus() == 2 ? "早退" : "缺勤";
            dataRow.createCell(4).setCellValue(status);;
            dataRow.createCell(5).setCellValue(r.getRemark() == null ? "" : r.getRemark());
        }

        // 自动列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 5. 下载
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode(empName + "_本月考勤记录.xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ===================== 工具方法 =====================
    private String getWeekDay(Date date) {
        String[] weeks = {"日", "一", "二", "三", "四", "五", "六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return weeks[cal.get(Calendar.DAY_OF_WEEK) - 1];
    }
}