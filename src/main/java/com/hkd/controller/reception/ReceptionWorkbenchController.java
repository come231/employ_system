package com.hkd.controller.reception;

import com.hkd.mapper.*;
import com.hkd.pojo.*;
import com.hkd.service.SysOperationLogService;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * 部门主管工作台控制器
 */
@Controller
@RequestMapping("/reception/workbench")
public class ReceptionWorkbenchController {

    @Autowired
    private EmpInfoDao empInfoDao;

    @Autowired
    private SysDeptDao sysDeptDao;

    @Autowired
    private LeaveApplicationDao leaveApplicationDao;

    @Autowired
    private SupplementCardDao supplementCardDao;

    @Autowired
    private AttendanceRecordDao attendanceRecordDao;

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysNotificationDao sysNotificationDao;

    @Autowired
    private SysOperationLogService sysOperationLogService;

    /**
     * 检查是否为部门主管
     */
    private boolean isManager(HttpSession session) {
        String role = (String) session.getAttribute("receptionRole");
        return "manager".equals(role);
    }

    /**
     * 获取待审批数量
     */
    @RequestMapping(value = "/pending", method = RequestMethod.GET)
    @ResponseBody
    public String getPendingCount(HttpSession session) {
        try {
            if (!isManager(session)) {
                return JsonUtil.buildFalseJson(403, "无权访问！");
            }

            Integer deptId = (Integer) session.getAttribute("receptionDeptId");
            Integer managerEmpId = (Integer) session.getAttribute("receptionEmpId");

            // 获取部门所有员工（排除主管自己）
            List<EmpInfo> employees = empInfoDao.findByDeptId(deptId);
            Set<Integer> empIds = new HashSet<>();
            for (EmpInfo emp : employees) {
                // 排除主管自己
                if (!emp.getEmpId().equals(managerEmpId)) {
                    empIds.add(emp.getEmpId());
                }
            }

            // 统计待审批的请假申请（不包括主管自己的）
            int pendingLeaveCount = 0;
            List<LeaveApplication> allLeaves = leaveApplicationDao.findByApproveStatus((byte) 0, 0, 1000);
            if (allLeaves != null) {
                for (LeaveApplication la : allLeaves) {
                    if (empIds.contains(la.getEmpId())) {
                        pendingLeaveCount++;
                    }
                }
            }

            // 统计待审批的补卡申请（不包括主管自己的）
            int pendingSupplementCount = 0;
            List<SupplementCard> allSupplements = supplementCardDao.findByApproveStatus((byte) 0, 0, 1000);
            if (allSupplements != null) {
                for (SupplementCard sc : allSupplements) {
                    if (empIds.contains(sc.getEmpId())) {
                        pendingSupplementCount++;
                    }
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("pendingLeaveCount", pendingLeaveCount);
            data.put("pendingSupplementCount", pendingSupplementCount);
            data.put("totalPendingCount", pendingLeaveCount + pendingSupplementCount);

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取部门考勤分析数据
     */
    @RequestMapping(value = "/analysis", method = RequestMethod.GET)
    @ResponseBody
    public String getDeptAnalysis(HttpSession session) {
        try {
            if (!isManager(session)) {
                return JsonUtil.buildFalseJson(403, "无权访问！");
            }

            Integer deptId = (Integer) session.getAttribute("receptionDeptId");

            // 获取部门员工
            List<EmpInfo> employees = empInfoDao.findByDeptId(deptId);
            int totalEmployees = employees != null ? employees.size() : 0;

            // 今日考勤统计
            LocalDate today = LocalDate.now();
            int normalCount = 0;
            int lateCount = 0;
            int absentCount = 0;
            int leaveCount = 0;

            if (employees != null) {
                for (EmpInfo emp : employees) {
                    List<AttendanceRecord> todayRecords = attendanceRecordDao.findByEmpIdAndDateRange(
                            emp.getEmpId(), today, today, -1, 1);

                    if (todayRecords == null || todayRecords.isEmpty()) {
                        // 检查是否请假
                        List<LeaveApplication> leaves = leaveApplicationDao.findByEmpId(emp.getEmpId(), 0, 100);
                        boolean onLeave = false;
                        if (leaves != null) {
                            for (LeaveApplication la : leaves) {
                                if (la.getApproveStatus() != null && la.getApproveStatus() == 1) {
                                    Date now = new Date();
                                    if (la.getStartTime().before(now) && la.getEndTime().after(now)) {
                                        onLeave = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (onLeave) {
                            leaveCount++;
                        } else {
                            absentCount++;
                        }
                    } else {
                        AttendanceRecord record = todayRecords.get(0);
                        if (record.getStatus() == 0) {
                            normalCount++;
                        } else if (record.getStatus() == 1) {
                            lateCount++;
                        } else {
                            normalCount++;
                        }
                    }
                }
            }

            // 本月考勤趋势
            LocalDate monthStart = today.withDayOfMonth(1);
            List<Map<String, Object>> monthTrend = new ArrayList<>();

            for (int i = 0; i < today.getDayOfMonth(); i++) {
                LocalDate date = monthStart.plusDays(i);
                int dayAttendance = 0;

                if (employees != null) {
                    for (EmpInfo emp : employees) {
                        List<AttendanceRecord> records = attendanceRecordDao.findByEmpIdAndDateRange(
                                emp.getEmpId(), date, date, -1, 1);
                        if (records != null && !records.isEmpty()) {
                            dayAttendance++;
                        }
                    }
                }

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.toString());
                dayData.put("count", dayAttendance);
                monthTrend.add(dayData);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("totalEmployees", totalEmployees);
            data.put("normalCount", normalCount);
            data.put("lateCount", lateCount);
            data.put("absentCount", absentCount);
            data.put("leaveCount", leaveCount);
            data.put("attendanceRate", totalEmployees > 0 ?
                    String.format("%.1f", (normalCount + lateCount) * 100.0 / totalEmployees) : "0");
            data.put("monthTrend", monthTrend);

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取部门请假申请列表
     */
    @RequestMapping(value = "/leave/data", method = RequestMethod.GET)
    @ResponseBody
    public String getDeptLeaves(HttpServletRequest request, HttpSession session) {
        try {
            if (!isManager(session)) {
                return JsonUtil.buildFalseJson(403, "无权访问！");
            }

            Integer deptId = (Integer) session.getAttribute("receptionDeptId");
            Integer managerEmpId = (Integer) session.getAttribute("receptionEmpId");
            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");
            String statusStr = request.getParameter("status");

            // 获取部门员工ID列表（排除主管自己）
            List<EmpInfo> employees = empInfoDao.findByDeptId(deptId);
            Set<Integer> empIds = new HashSet<>();
            Map<Integer, EmpInfo> empMap = new HashMap<>();
            for (EmpInfo emp : employees) {
                // 排除主管自己
                if (!emp.getEmpId().equals(managerEmpId)) {
                    empIds.add(emp.getEmpId());
                }
                empMap.put(emp.getEmpId(), emp);
            }

            // 获取所有请假申请并过滤
            List<LeaveApplication> allLeaves;
            if (!CommonUtil.isEmpty(statusStr)) {
                allLeaves = leaveApplicationDao.findByApproveStatus(Byte.parseByte(statusStr), 0, 1000);
            } else {
                allLeaves = leaveApplicationDao.findAll(0, 1000);
            }

            // 过滤本部门的申请（不包括主管自己的申请）
            List<LeaveApplication> deptLeaves = new ArrayList<>();
            if (allLeaves != null) {
                for (LeaveApplication la : allLeaves) {
                    if (empIds.contains(la.getEmpId())) {
                        deptLeaves.add(la);
                    }
                }
            }

            // 分页
            int total = deptLeaves.size();
            PageQuery pageQuery = new PageQuery(page, pageSize);
            int start = Math.min(pageQuery.getOffset(), total);
            int end = Math.min(pageQuery.getOffset() + pageSize, total);
            List<LeaveApplication> pagedLeaves = deptLeaves.subList(start, end);

            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            List<Map<String, Object>> list = new ArrayList<>();
            for (LeaveApplication la : pagedLeaves) {
                Map<String, Object> map = new HashMap<>();
                EmpInfo emp = empMap.get(la.getEmpId());
                map.put("leaveId", la.getLeaveId());
                map.put("empNo", emp != null ? emp.getEmpNo() : "");
                map.put("empName", emp != null ? emp.getEmpName() : "");
                map.put("leaveType", la.getLeaveType());
                map.put("startTime", la.getStartTime() != null ? sdfDateTime.format(la.getStartTime()) : "");
                map.put("endTime", la.getEndTime() != null ? sdfDateTime.format(la.getEndTime()) : "");
                map.put("leaveDays", la.getLeaveDays());
                map.put("reason", la.getReason());
                map.put("approveStatus", la.getApproveStatus());
                map.put("approveStatusName", getApproveStatusName(la.getApproveStatus()));
                map.put("createTime", la.getCreateTime() != null ? sdfDateTime.format(la.getCreateTime()) : "");
                list.add(map);
            }

            return JsonUtil.buildJsonByTotalCount(list, total);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取部门补卡申请列表
     */
    @RequestMapping(value = "/supplement/data", method = RequestMethod.GET)
    @ResponseBody
    public String getDeptSupplements(HttpServletRequest request, HttpSession session) {
        try {
            if (!isManager(session)) {
                return JsonUtil.buildFalseJson(403, "无权访问！");
            }

            Integer deptId = (Integer) session.getAttribute("receptionDeptId");
            Integer managerEmpId = (Integer) session.getAttribute("receptionEmpId");
            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");
            String statusStr = request.getParameter("status");

            // 获取部门员工ID列表（排除主管自己）
            List<EmpInfo> employees = empInfoDao.findByDeptId(deptId);
            Set<Integer> empIds = new HashSet<>();
            Map<Integer, EmpInfo> empMap = new HashMap<>();
            for (EmpInfo emp : employees) {
                // 排除主管自己
                if (!emp.getEmpId().equals(managerEmpId)) {
                    empIds.add(emp.getEmpId());
                }
                empMap.put(emp.getEmpId(), emp);
            }

            // 获取所有补卡申请并过滤
            List<SupplementCard> allSupplements;
            if (!CommonUtil.isEmpty(statusStr)) {
                allSupplements = supplementCardDao.findByApproveStatus(Byte.parseByte(statusStr), 0, 1000);
            } else {
                allSupplements = supplementCardDao.findAll(0, 1000);
            }

            // 过滤本部门的申请（不包括主管自己的申请）
            List<SupplementCard> deptSupplements = new ArrayList<>();
            if (allSupplements != null) {
                for (SupplementCard sc : allSupplements) {
                    if (empIds.contains(sc.getEmpId())) {
                        deptSupplements.add(sc);
                    }
                }
            }

            // 分页
            int total = deptSupplements.size();
            PageQuery pageQuery = new PageQuery(page, pageSize);
            int start = Math.min(pageQuery.getOffset(), total);
            int end = Math.min(pageQuery.getOffset() + pageSize, total);
            List<SupplementCard> pagedSupplements = deptSupplements.subList(start, end);

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<Map<String, Object>> list = new ArrayList<>();
            for (SupplementCard sc : pagedSupplements) {
                Map<String, Object> map = new HashMap<>();
                EmpInfo emp = empMap.get(sc.getEmpId());
                map.put("supplementId", sc.getSupplementId());
                map.put("empNo", emp != null ? emp.getEmpNo() : "");
                map.put("empName", emp != null ? emp.getEmpName() : "");
                map.put("checkDate", sc.getCheckDate() != null ? sdfDate.format(sc.getCheckDate()) : "");
                map.put("checkType", sc.getCheckType());
                map.put("checkTypeName", getCheckTypeName(sc.getCheckType()));
                map.put("actualTime", sc.getActualTime() != null ? sdfTime.format(sc.getActualTime()) : "");
                map.put("reason", sc.getReason());
                map.put("approveStatus", sc.getApproveStatus());
                map.put("approveStatusName", getApproveStatusName(sc.getApproveStatus()));
                map.put("createTime", sc.getCreateTime() != null ? sdfDateTime.format(sc.getCreateTime()) : "");
                list.add(map);
            }

            return JsonUtil.buildJsonByTotalCount(list, total);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 审批请假申请
     */
    @RequestMapping(value = "/leave/approve", method = RequestMethod.POST)
    @ResponseBody
    public String approveLeave(@RequestParam("leaveId") Integer leaveId,
                               @RequestParam("status") Byte status,
                               @RequestParam(value = "opinion", required = false) String opinion,
                               HttpSession session) {
        try {
            if (!isManager(session)) {
                return JsonUtil.buildFalseJson(403, "无权访问！");
            }

            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            Integer deptId = (Integer) session.getAttribute("receptionDeptId");

            LeaveApplication leave = leaveApplicationDao.findById(leaveId);
            if (leave == null) {
                return JsonUtil.buildFalseJson(404, "申请不存在！");
            }

            // 检查是否是本部门员工的申请
            EmpInfo applicant = empInfoDao.findById(leave.getEmpId());
            if (applicant == null || !deptId.equals(applicant.getDeptId())) {
                return JsonUtil.buildFalseJson(403, "无权审批此申请！");
            }

            // 检查不能审批自己的申请
            if (empId.equals(leave.getEmpId())) {
                return JsonUtil.buildFalseJson(403, "不能审批自己的申请！");
            }

            if (leave.getApproveStatus() != 0) {
                return JsonUtil.buildFalseJson(400, "该申请已审批！");
            }

            leave.setApproveStatus(status);
            leave.setApproveOpinion(opinion);
            leave.setApproverId(empId);
            leave.setUpdateTime(new Date());

            leaveApplicationDao.update(leave);

            // 发送通知
            sendNotification(leave.getEmpId(), "leave_" + (status == 1 ? "approved" : "rejected"),
                    "请假申请" + (status == 1 ? "已通过" : "已驳回"),
                    "您的请假申请(" + leave.getLeaveType() + ")已" + (status == 1 ? "通过" : "驳回") +
                            (opinion != null ? "，审批意见：" + opinion : ""),
                    leaveId);

            // 记录操作日志
            logOperation(session, "工作台", "审批", "审批请假申请ID:" + leaveId + "，结果:" + (status == 1 ? "通过" : "驳回"));

            return JsonUtil.objectToJson("", "审批成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 审批补卡申请
     */
    @RequestMapping(value = "/supplement/approve", method = RequestMethod.POST)
    @ResponseBody
    public String approveSupplement(@RequestParam("supplementId") Integer supplementId,
                                    @RequestParam("status") Byte status,
                                    @RequestParam(value = "opinion", required = false) String opinion,
                                    HttpSession session) {
        try {
            if (!isManager(session)) {
                return JsonUtil.buildFalseJson(403, "无权访问！");
            }

            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            Integer deptId = (Integer) session.getAttribute("receptionDeptId");

            SupplementCard supplement = supplementCardDao.findById(supplementId);
            if (supplement == null) {
                return JsonUtil.buildFalseJson(404, "申请不存在！");
            }

            // 检查是否是本部门员工的申请
            EmpInfo applicant = empInfoDao.findById(supplement.getEmpId());
            if (applicant == null || !deptId.equals(applicant.getDeptId())) {
                return JsonUtil.buildFalseJson(403, "无权审批此申请！");
            }

            // 检查不能审批自己的申请
            if (empId.equals(supplement.getEmpId())) {
                return JsonUtil.buildFalseJson(403, "不能审批自己的申请！");
            }

            if (supplement.getApproveStatus() != 0) {
                return JsonUtil.buildFalseJson(400, "该申请已审批！");
            }

            supplementCardDao.updateApproveStatus(supplementId, status, opinion, empId);

            // 如果审批通过，更新或创建考勤记录
            if (status == 1) {
                updateAttendanceRecordForSupplement(supplement);
            }

            // 发送通知
            sendNotification(supplement.getEmpId(), "supplement_" + (status == 1 ? "approved" : "rejected"),
                    "补卡申请" + (status == 1 ? "已通过" : "已驳回"),
                    "您的补卡申请已" + (status == 1 ? "通过" : "驳回") +
                            (opinion != null ? "，审批意见：" + opinion : ""),
                    supplementId);

            // 记录操作日志
            logOperation(session, "工作台", "审批", "审批补卡申请ID:" + supplementId + "，结果:" + (status == 1 ? "通过" : "驳回"));

            return JsonUtil.objectToJson("", "审批成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 补卡通过后更新考勤记录
     * @param supplement 补卡申请
     */
    private void updateAttendanceRecordForSupplement(SupplementCard supplement) {
        try {
            // 查找该员工该日期的考勤记录
            AttendanceRecord existingRecord = attendanceRecordDao.findByEmpIdAndCheckDate(
                    supplement.getEmpId(), supplement.getCheckDate());

            String remarkContent = "补卡通过：" + (supplement.getReason() != null ? supplement.getReason() : "");

            if (existingRecord != null) {
                // 已存在考勤记录，更新状态为正常
                existingRecord.setStatus((byte) 0); // 正常
                existingRecord.setRemark(remarkContent);
                // 如果补卡提供了实际时间，更新打卡时间
                if (supplement.getActualTime() != null) {
                    existingRecord.setCheckInTime(supplement.getActualTime());
                }
                attendanceRecordDao.update(existingRecord);
            } else {
                // 不存在考勤记录，创建新记录
                AttendanceRecord newRecord = new AttendanceRecord();
                newRecord.setEmpId(supplement.getEmpId());
                newRecord.setCheckDate(supplement.getCheckDate());
                newRecord.setType(supplement.getCheckType() != null ? supplement.getCheckType().intValue() : 1); // 默认上班打卡
                newRecord.setCheckInTime(supplement.getActualTime() != null ? supplement.getActualTime() : new Date());
                newRecord.setStatus((byte) 0); // 正常
                newRecord.setRemark(remarkContent);
                newRecord.setCreateTime(new Date());
                attendanceRecordDao.insert(newRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 不抛出异常，防止影响主流程
        }
    }

    private void sendNotification(Integer userId, String type, String title, String content, Integer relatedId) {
        try {
            // 获取员工对应的用户ID
            SysUser user = sysUserDao.findByEmpId(userId);
            if (user != null && sysNotificationDao != null) {
                SysNotification notification = new SysNotification();
                notification.setUserId(user.getUserId());
                notification.setType(type);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setRelatedId(relatedId);
                notification.setIsRead((byte) 0);
                notification.setCreateTime(new Date());
                sysNotificationDao.insert(notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCheckTypeName(Byte checkType) {
        if (checkType == null) return "";
        switch (checkType) {
            case 1: return "上班";
            case 2: return "下班";
            case 3: return "上下班";
            default: return "";
        }
    }

    private String getApproveStatusName(Byte status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "";
        }
    }

    private void logOperation(HttpSession session, String module, String type, String content) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUserId((Integer) session.getAttribute("receptionUserId"));
            log.setUsername((String) session.getAttribute("receptionUsername"));
            log.setRoleName("部门主管");
            log.setModule(module);
            log.setOperationType(type);
            log.setOperationContent(content);
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
