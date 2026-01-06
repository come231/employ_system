package com.hkd.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 18:17
 * @description TODO: 进行各种页面的跳转
 */
@Controller()
@RequestMapping("/admin/page")
public class ToPageController {

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login(){
        return "admin/login";
    }

    @RequestMapping(value = "/dashboard",method = RequestMethod.GET)
    public String dashboard(){
        return "admin/dashboard";
    }

    @RequestMapping(value = "/companyAnalysis",method = RequestMethod.GET)
    public String companyAnalysis(){
        return "admin/attendance/companyAnalysis";
    }

    @RequestMapping(value = "/deptAnalysis",method = RequestMethod.GET)
    public String deptAnalysis(){
        return "admin/attendance/deptAnalysis";
    }

    @RequestMapping(value = "/settingRule",method = RequestMethod.GET)
    public String attendanceRule(){
        return "admin/config/settingRule";
    }

    @RequestMapping(value = "/settingLog",method = RequestMethod.GET)
    public String adminLog(){
        return "admin/config/settingLog";
    }

    @RequestMapping(value = "/attendanceManage",method = RequestMethod.GET)
    public String attendanceQuery(){return "admin/record/attendanceManage";}


    @RequestMapping(value = "/leaveManage",method = RequestMethod.GET)
    public String leaveManage(){
        return "admin/record/leaveManage";
    }

    @RequestMapping(value = "/supplementManage",method = RequestMethod.GET)
    public String supplementManage(){
        return "admin/record/supplementManage";
    }

    @RequestMapping(value = "/deptList",method = RequestMethod.GET)
    public String deptStructure(){
        return "admin/user/deptList";
    }

    @RequestMapping(value = "/employeeList",method = RequestMethod.GET)
    public String employeeList(){
        return "admin/user/employeeList";
    }

}
