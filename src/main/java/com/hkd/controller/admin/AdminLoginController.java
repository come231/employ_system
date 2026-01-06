package com.hkd.controller.admin;

import com.hkd.mapper.SysUserDao;
import com.hkd.mapper.SysUserRoleQueryDao;
import com.hkd.pojo.SysUser;
import com.hkd.pojo.SysOperationLog;

import com.hkd.pojo.SysUserRoleQuery;
import com.hkd.service.SysOperationLogService;
import com.hkd.util.BCryptUtil;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.TokenUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 18:21
 * @description TODO: 进行管理员的登录验证
 */
@Controller()
@RequestMapping("/admin")
public class AdminLoginController {
    @Autowired
    private SysUserRoleQueryDao sysUserRoleQueryDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysOperationLogService sysOperationLogService;

    //超级管理员的登录验证
    @PostMapping("/login1")
    @ResponseBody
    public String login1(@RequestParam("username") String username, @RequestParam("password") String password,
                         HttpSession session){
        try{
            if(CommonUtil.isEmpty(username)||CommonUtil.isEmpty(password)){
                return JsonUtil.buildFalseJson(500,"用户名或密码不能为空！");
            }
            SysUserRoleQuery sysUserRoleQuery = sysUserRoleQueryDao.findSysUserRoleBySysUserId(username);
            if(!CommonUtil.isEmpty(sysUserRoleQuery)){
                if(!BCryptUtil.checkPassword(password,sysUserRoleQuery.getPassword())){
                    return JsonUtil.buildFalseJson(404,"账号或密码错误！");
                }
                // 更新最后登录时间
                sysUserDao.updateLastLoginTime(sysUserRoleQuery.getUserId(),new Date());

                // 将管理员信息存入session
                session.setAttribute("adminId",sysUserRoleQuery.getUserId());
                session.setAttribute("admin",sysUserRoleQuery.getUsername());
                session.setAttribute("adminRole", sysUserRoleQuery.getRoleName());
                session.setAttribute("avatar",sysUserRoleQuery.getAvatar());
                session.setAttribute("lastLoginTime",sysUserRoleQuery.getLastLoginTime());
                
                // 同时设置user对象，供其他功能使用（如个人中心、日志记录）
                SysUser user = sysUserDao.findById(sysUserRoleQuery.getUserId());
                if (user != null) {
                    session.setAttribute("user", user);
                }

                // 生成Token（用于API调用）
                String token = TokenUtil.generateToken(sysUserRoleQuery.getUserId());
                
                // 记录登录日志
                logLoginSuccess(sysUserRoleQuery);
                
                return JsonUtil.objectToJson(sysUserRoleQuery,"登录成功！");
            }
            return JsonUtil.buildFalseJson(404,"账号或密码错误！");

        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }


    //获取用户登录信息
    @RequestMapping(value = "/login/current",method = RequestMethod.GET)
    @ResponseBody
    public String current(HttpSession session){
        try{
            if(!CommonUtil.isEmpty(session.getAttribute("adminId"))){
                Map<String ,Object> map =new HashMap<>();
                map.put("code",200);
                map.put("msg","获取登录信息成功！");
                map.put("adminId",session.getAttribute("adminId"));
                map.put("admin",session.getAttribute("admin"));
                map.put("adminRole",session.getAttribute("adminRole"));
                map.put("avatar",session.getAttribute("avatar"));
                map.put("lastLoginTime",session.getAttribute("lastLoginTime"));
                return JsonUtil.toJson(map);
            }else{
                return JsonUtil.buildFalseJson(404,"您还没有登录！");
            }

        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }

    //获取用户详细信息
    @RequestMapping(value = "/login/data",method = RequestMethod.GET)
    @ResponseBody
    public String data(HttpSession session){
        try{
            if(!CommonUtil.isEmpty(session.getAttribute("adminId"))){
                Map<String ,Object> map =new HashMap<>();
                map.put("code",200);
                map.put("msg","获取登录信息成功！");
                map.put("adminId",session.getAttribute("adminId"));
                map.put("admin",session.getAttribute("admin"));
                map.put("adminRole",session.getAttribute("adminRole"));
                map.put("lastLoginTime",session.getAttribute("lastLoginTime"));
                return JsonUtil.toJson(map);
            }else{
                return JsonUtil.buildFalseJson(404,"您还没有登录！");
            }

        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }

    //退出登录
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    @ResponseBody
    public String logout(HttpSession session){
        try{
            // 记录退出日志
            Object username = session.getAttribute("admin");
            if (username != null) {
                logLogout(username.toString());
            }
            
            //销毁会话
            session.invalidate();
            return JsonUtil.objectToJson("","退出成功！");
        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }
    
    /**
     * 记录登录成功日志
     */
    private void logLoginSuccess(SysUserRoleQuery user) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
            log.setRoleName(user.getRoleName());
            log.setModule("系统");
            log.setOperationType("登录");
            log.setOperationContent("用户登录系统");
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 记录退出登录日志
     */
    private void logLogout(String username) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUsername(username);
            log.setRoleName("管理员");
            log.setModule("系统");
            log.setOperationType("退出");
            log.setOperationContent("用户退出系统");
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
