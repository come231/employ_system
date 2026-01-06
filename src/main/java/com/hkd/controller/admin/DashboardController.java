package com.hkd.controller.admin;

import com.hkd.pojo.*;
import com.hkd.service.AttendanceRecordService;
import com.hkd.service.EmpInfoService;
import com.hkd.service.LeaveApplicationService;
import com.hkd.service.SupplementCardService;
import com.hkd.service.SysOperationLogService;
import com.hkd.service.SysUserService;
import com.hkd.mapper.SysDeptDao;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.Data;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/27
 * @description 首页数据分析页面
 */
@Controller
@RequestMapping("/admin")
public class DashboardController {
    @Autowired
    private LeaveApplicationService leaveApplicationService;
    @Autowired
    private EmpInfoService empInfoService;
    @Autowired
    private AttendanceRecordService attendanceRecordService;
    @Autowired
    private SysOperationLogService sysOperationLogService;
    @Autowired
    private SupplementCardService supplementCardService;
    @Autowired
    private SysDeptDao sysDeptDao;
    
    /**
     * 表盘数据显示页面
     */
    @RequestMapping(value = "/dashboard/data",method = RequestMethod.GET)
    @ResponseBody
    public String index() {
            try{
                LocalDate localDate =LocalDate.now();
                LocalDateTime localTime=LocalDateTime.of(localDate,LocalTime.MIN);
                LocalDateTime endTime =LocalDateTime.of(localDate,LocalTime.MAX);

                Map<String,Object> result=new HashMap<>();
                if(!CommonUtil.isEmpty(localDate)&&!CommonUtil.isEmpty(localTime)){

                    result.put("code",200);
                    result.put("msg","获取数据成功！");
                    result.put("employCount",empInfoService.findCount(1));
                    result.put("successCount",attendanceRecordService.findByEmpIdAndDateRange(-1,localDate,localDate,0,1).size());
                    result.put("laterCount",attendanceRecordService.findByEmpIdAndDateRange(-1,localDate,localDate,1,1).size());
                    result.put("leaveCount",leaveApplicationService.findByDateRange(-1,localTime,endTime,-1,"").size());
                    return JsonUtil.toJson(result);
                }
                return JsonUtil.buildFalseJson(500,"获取当日日期异常！");

            }catch (Exception e){
                e.printStackTrace();
                return JsonUtil.buildFalseJson(500,"获取数据异常！");
            }
    }

    /**
     * 获取月度考勤数据接口（用于图表展示）
     */
    @GetMapping(value = "/dashboard/monthData")
    @ResponseBody
    public String getMonthAttendanceData() {
        try {
            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.withDayOfMonth(1); // 本月第一天
            LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth()); // 本月最后一天

            Map<String, Object> result = new HashMap<>();
            Map<String, Integer> dayDataMap = new HashMap<>();

            // 循环获取本月每天的出勤和迟到数据
            for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                int attendance = attendanceRecordService.findByEmpIdAndDateRange(-1, date, date, 0, 1).size();
                int late = attendanceRecordService.findByEmpIdAndDateRange(-1, date, date, 1, 1).size();

                dayDataMap.put(date.getDayOfMonth() + "", attendance);
                dayDataMap.put(date.getDayOfMonth() + "_late", late);
            }

            result.put("code", 200);
            result.put("msg", "获取月度数据成功");
            result.put("monthData", dayDataMap);
            result.put("monthDays", now.lengthOfMonth()); // 本月总天数

            return JsonUtil.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "获取月度数据异常！");
        }
    }

    /**
     * 获取今日考勤饼图数据（出勤/请假/迟到比例）
     */
    @GetMapping(value = "/dashboard/pieData")
    @ResponseBody
    public String getTodayAttendancePieData() {
        try {
            LocalDate localDate = LocalDate.now();
            LocalDateTime localTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map<String, Object> result = new HashMap<>();
            
            // 获取今日出勤、迟到、请假人数
            int attendanceCount = attendanceRecordService.findByEmpIdAndDateRange(-1, localDate, localDate, 0, 1).size();
            int lateCount = attendanceRecordService.findByEmpIdAndDateRange(-1, localDate, localDate, 1, 1).size();
            int leaveCount = leaveApplicationService.findByDateRange(-1, localTime, endTime, -1, "").size();

            result.put("code", 200);
            result.put("msg", "获取饼图数据成功");
            result.put("attendanceCount", attendanceCount);
            result.put("lateCount", lateCount);
            result.put("leaveCount", leaveCount);

            return JsonUtil.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "获取饼图数据异常！");
        }
    }

    /**
     * 获取最近5条操作日志
     */
    @GetMapping(value = "/dashboard/recentLogs")
    @ResponseBody
    public String getRecentLogs() {
        try {
            List<SysOperationLog> logs = sysOperationLogService.findAll(0, 5);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "获取日志成功");
            result.put("logs", logs);

            return JsonUtil.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "获取日志异常！");
        }
    }

    /**
     * 获取待审批数量（请假+补卡）
     */
    @GetMapping(value = "/dashboard/pendingCount")
    @ResponseBody
    public String getPendingApprovalCount() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 待审批状态 = 0
            int pendingLeaveCount = leaveApplicationService.findCountByStatus((byte) 0);
            int pendingSupplementCount = supplementCardService.findCountByStatus((byte) 0);

            result.put("code", 200);
            result.put("msg", "获取待审批数量成功");
            result.put("pendingLeaveCount", pendingLeaveCount);
            result.put("pendingSupplementCount", pendingSupplementCount);

            // 获取所有部门主管的员工ID
            Set<Integer> managerEmpIds = new HashSet<>();
            List<SysDept> allDepts = sysDeptDao.findAll(0, 1000);
            if (allDepts != null) {
                for (SysDept dept : allDepts) {
                    if (dept.getManagerId() != null) {
                        managerEmpIds.add(dept.getManagerId());
                    }
                }
            }

            // 统计主管的待审批请假数量
            int managerPendingLeaveCount = 0;
            List<LeaveApplication> pendingLeaves = leaveApplicationService.findByApproveStatus((byte) 0, 0, 1000);
            if (pendingLeaves != null) {
                for (LeaveApplication leave : pendingLeaves) {
                    if (managerEmpIds.contains(leave.getEmpId())) {
                        managerPendingLeaveCount++;
                    }
                }
            }

            // 统计主管的待审批补卡数量
            int managerPendingSupplementCount = 0;
            List<SupplementCard> pendingSupplements = supplementCardService.findByApproveStatus((byte) 0, 0, 1000);
            if (pendingSupplements != null) {
                for (SupplementCard supplement : pendingSupplements) {
                    if (managerEmpIds.contains(supplement.getEmpId())) {
                        managerPendingSupplementCount++;
                    }
                }
            }

            result.put("managerPendingLeaveCount", managerPendingLeaveCount);
            result.put("managerPendingSupplementCount", managerPendingSupplementCount);

            return JsonUtil.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "获取待审批数量异常！");
        }
    }
}
