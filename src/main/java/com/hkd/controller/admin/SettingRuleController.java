package com.hkd.controller.admin;

import com.hkd.pojo.AttendanceRule;
import com.hkd.service.AttendanceRuleService;
import com.hkd.service.SysOperationLogService;
import com.hkd.util.CommonUtil;
import com.hkd.util.DateTimeFormatterUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.OperationLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Time;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/27
 * @description 考勤规则设置Controller
 */
@Controller
@RequestMapping("/admin/setting/rule")
public class SettingRuleController {
    @Autowired
    private AttendanceRuleService attendanceRuleService;

    @Autowired
    private SysOperationLogService sysOperationLogService;

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    @ResponseBody
    public String getAttendanceRule(HttpServletRequest request) {
        try {
            AttendanceRule rule = attendanceRuleService.findById(1); // 假设只有一条规则记录
            if (!CommonUtil.isEmpty(rule)) {
                return JsonUtil.objectToJson(rule, "获取成功");
            }
            return JsonUtil.buildFalseJson(404, "未找到考勤规则");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public String updateAttendanceRule(HttpServletRequest request, HttpSession session) {
        try {
            Integer ruleId = Integer.parseInt(request.getParameter("ruleId"));
            String workStartTime = request.getParameter("workStartTime");
            String workEndTime = request.getParameter("workEndTime");
            Integer lateThreshold = Integer.parseInt(request.getParameter("lateThreshold"));
            Integer earlyThreshold = Integer.parseInt(request.getParameter("earlyThreshold"));
            Integer supplementValidHours = Integer.parseInt(request.getParameter("supplementValidHours"));
            Integer fullAttendanceDays = Integer.parseInt(request.getParameter("fullAttendanceDays"));

            AttendanceRule rule = attendanceRuleService.findById(ruleId);
            if (CommonUtil.isEmpty(rule)) {
                OperationLogUtil.logError(session, sysOperationLogService, "系统设置", "更新",
                    "更新考勤规则ID:" + ruleId, "考勤规则不存在");
                return JsonUtil.buildFalseJson(404, "未找到考勤规则");
            }

            rule.setWorkStartTime(Time.valueOf(workStartTime + ":00"));
            rule.setWorkEndTime(Time.valueOf(workEndTime + ":00"));
            rule.setLateThreshold(lateThreshold);
            rule.setEarlyThreshold(earlyThreshold);
            rule.setSupplementValidHours(supplementValidHours);
            rule.setFullAttendanceDays(fullAttendanceDays);

            int result = attendanceRuleService.update(rule);
            if (result > 0) {
                OperationLogUtil.log(session, sysOperationLogService, "系统设置", "更新",
                    "更新考勤规则:上班时间" + workStartTime + ",下班时间" + workEndTime +
                    ",迟到阈值" + lateThreshold + "分钟,早退阈值" + earlyThreshold + "分钟");
                return JsonUtil.objectToJson(null, "更新成功");
            }
            OperationLogUtil.logError(session, sysOperationLogService, "系统设置", "更新",
                "更新考勤规则ID:" + ruleId, "更新数据库失败");
            return JsonUtil.buildFalseJson(500, "更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }
}
