package com.hkd.controller.reception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

/**
 * 前台页面导航控制器
 */
@Controller
@RequestMapping("/reception/page")
public class ReceptionPageController {

    /**
     * 前台登录页
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "reception/login";
    }

    /**
     * 前台首页
     */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(HttpSession session) {
        // 检查登录状态
        if (session.getAttribute("receptionUserId") == null) {
            return "reception/login";
        }
        return "reception/index";
    }

    /**
     * 个人中心页
     */
    @RequestMapping(value = "/center", method = RequestMethod.GET)
    public String center(HttpSession session) {
        // 检查登录状态
        if (session.getAttribute("receptionUserId") == null) {
            return "reception/login";
        }
        return "reception/center";
    }

    /**
     * 工作台页面（仅部门主管可访问）
     */
    @RequestMapping(value = "/workbench", method = RequestMethod.GET)
    public String workbench(HttpSession session) {
        // 检查登录状态
        if (session.getAttribute("receptionUserId") == null) {
            return "reception/login";
        }
        // 检查是否为部门主管
        String role = (String) session.getAttribute("receptionRole");
        if (!"manager".equals(role)) {
            return "reception/index";
        }
        return "reception/workbench";
    }

    /**
     * 消息通知页面
     */
    @RequestMapping(value = "/notification", method = RequestMethod.GET)
    public String notification(HttpSession session) {
        // 检查登录状态
        if (session.getAttribute("receptionUserId") == null) {
            return "reception/login";
        }
        return "reception/notification";
    }
}
