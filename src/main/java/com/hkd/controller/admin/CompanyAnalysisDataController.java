package com.hkd.controller.admin;

import com.hkd.service.*;
import com.hkd.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/27
 * @description 公司考勤分析数据Controller
 */
@Controller
@RequestMapping("/admin/analysis")
public class CompanyAnalysisDataController {
    @Autowired
    private AttendanceRecordService attendanceRecordService;
    @Autowired
    private EmpInfoService empInfoService;
    @Autowired
    private LeaveApplicationService leaveApplicationService;
    @Autowired
    private SupplementCardService supplementCardService;

    @RequestMapping(value = "/company/data", method = RequestMethod.GET)
    @ResponseBody
    public String getCompanyAnalysisData(HttpServletRequest request) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 统计总员工数
            Integer totalEmpCount = empInfoService.findAllCount();
            result.put("totalEmpCount", totalEmpCount);
            
            // 统计在职员工数
            Integer activeEmpCount = empInfoService.findCount(1);
            result.put("activeEmpCount", activeEmpCount);
            
            // 统计考勤记录总数
            Integer totalAttendanceCount = attendanceRecordService.findAllCount();
            result.put("totalAttendanceCount", totalAttendanceCount);
            
            // 统计请假申请总数
            Integer totalLeaveCount = leaveApplicationService.findAllCount();
            result.put("totalLeaveCount", totalLeaveCount);
            
            // 统计补卡申请总数
            Integer totalSupplementCount = supplementCardService.findAllCount();
            result.put("totalSupplementCount", totalSupplementCount);
            
            // 统计待审批的请假申请数
            Integer pendingLeaveCount = leaveApplicationService.findCountByStatus((byte)0);
            result.put("pendingLeaveCount", pendingLeaveCount);
            
            // 统计待审批的补卡申请数
            Integer pendingSupplementCount = supplementCardService.findCountByStatus((byte)0);
            result.put("pendingSupplementCount", pendingSupplementCount);
            
            return JsonUtil.objectToJson(result, "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }
}
