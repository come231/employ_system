package com.hkd.controller.admin;

import com.hkd.pojo.SysDept;
import com.hkd.service.*;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/27
 * @description 部门考勤分析数据Controller
 */
@Controller
@RequestMapping("/admin/analysis")
public class
DeptAnalysisDataController {
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private EmpInfoService empInfoService;

    @RequestMapping(value = "/dept/data", method = RequestMethod.GET)
    @ResponseBody
    public String getDeptAnalysisData(HttpServletRequest request) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List<SysDept> deptList = sysDeptService.findAll(0, 100); // 获取所有部门
            
            if (!CommonUtil.isEmpty(deptList)) {
                for (SysDept dept : deptList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("deptId", dept.getDeptId());
                    map.put("deptName", dept.getDeptName());
                    
                    // 统计部门员工数
                    Integer empCount = empInfoService.findByDeptId(dept.getDeptId()).size();
                    map.put("empCount", empCount);
                    
                    result.add(map);
                }
            }
            
            return JsonUtil.objectToJson(result, "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }
}
