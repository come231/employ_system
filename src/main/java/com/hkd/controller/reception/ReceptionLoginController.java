package com.hkd.controller.reception;

import com.hkd.mapper.EmpInfoDao;
import com.hkd.mapper.SysDeptDao;
import com.hkd.mapper.SysUserDao;
import com.hkd.pojo.EmpInfo;
import com.hkd.pojo.SysDept;
import com.hkd.pojo.SysOperationLog;
import com.hkd.pojo.SysUser;
import com.hkd.service.SysOperationLogService;
import com.hkd.util.BCryptUtil;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.RedisUtil;
import com.hkd.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 前台员工/部门主管登录控制器
 */
@Controller
@RequestMapping("/reception")
public class ReceptionLoginController {

    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private EmpInfoDao empInfoDao;
    @Autowired
    private SysDeptDao sysDeptDao;
    @Autowired
    private SysOperationLogService sysOperationLogService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TokenUtil tokenUtil;

    // 过期时间配置
    private static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60; // 7天

    /**
     * 员工/部门主管登录（带双Token）
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session) {
        try {
            if (CommonUtil.isEmpty(username) || CommonUtil.isEmpty(password)) {
                return JsonUtil.buildFalseJson(500, "用户名或密码不能为空！");
            }

            // 根据用户名查找用户
            SysUser sysUser = sysUserDao.findByUsername(username);
            if (CommonUtil.isEmpty(sysUser)) {
                return JsonUtil.buildFalseJson(404, "用户不存在！");
            }

            // 验证密码
            if (!BCryptUtil.checkPassword(password, sysUser.getPassword())) {
                return JsonUtil.buildFalseJson(401, "密码错误！");
            }

            // 检查用户状态
            if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
                return JsonUtil.buildFalseJson(403, "账号已被禁用！");
            }

            // 检查是否关联员工信息
            if (CommonUtil.isEmpty(sysUser.getEmpId())) {
                return JsonUtil.buildFalseJson(403, "该账号不是员工账号，请使用后台管理系统登录！");
            }

            // 获取员工信息
            EmpInfo empInfo = empInfoDao.findById(sysUser.getEmpId());
            if (CommonUtil.isEmpty(empInfo)) {
                return JsonUtil.buildFalseJson(404, "员工信息不存在！");
            }

            // 检查员工状态
            if (empInfo.getStatus() != null && empInfo.getStatus() == 0) {
                return JsonUtil.buildFalseJson(403, "您已离职，无法登录！");
            }

            // 获取部门信息
            SysDept sysDept = sysDeptDao.findById(empInfo.getDeptId());
            if (CommonUtil.isEmpty(sysDept)) {
                return JsonUtil.buildFalseJson(404, "部门信息不存在！");
            }

            // 判断角色
            String role = "employee";
            if (sysDept.getManagerId() != null && sysDept.getManagerId().equals(empInfo.getEmpId())) {
                role = "manager";
            }

            // 更新最后登录时间
            sysUserDao.updateLastLoginTime(sysUser.getUserId(), new Date());

            // ====================== 生成双令牌 ======================
            Integer userId = sysUser.getUserId();
            String accessToken = tokenUtil.generateToken(String.valueOf(userId));
            String refreshToken = tokenUtil.generateRefreshToken(userId);

            // 存入Redis
//            redisUtil.set(TokenUtil.ACCESS_TOKEN_PREFIX + accessToken, userId, ACCESS_TOKEN_EXPIRE);
            //刷新令牌
            redisUtil.set(TokenUtil.REFRESH_TOKEN_PREFIX + refreshToken, userId, REFRESH_TOKEN_EXPIRE);

            // ====================== Session设置 ======================
            session.setAttribute("user", sysUser);
            session.setAttribute("receptionUserId", userId);
            session.setAttribute("receptionUsername", sysUser.getUsername());
            session.setAttribute("receptionEmpId", empInfo.getEmpId());
            session.setAttribute("receptionEmpNo", empInfo.getEmpNo());
            session.setAttribute("receptionEmpName", empInfo.getEmpName());
            session.setAttribute("receptionDeptId", empInfo.getDeptId());
            session.setAttribute("receptionDeptName", sysDept.getDeptName());
            session.setAttribute("receptionRole", role);
            session.setAttribute("receptionAvatar", sysUser.getAvatar());
            session.setAttribute("receptionPhone", empInfo.getPhone());
            session.setAttribute("receptionEmail", empInfo.getEmail());
            session.setAttribute("receptionStatus", empInfo.getStatus());
            session.setAttribute("accessToken", accessToken);
            session.setAttribute("refreshToken", refreshToken);

            // 记录登录日志
            logLoginSuccess(sysUser, empInfo, role);

            System.out.println("blacklist:"+accessToken);
            // ====================== 返回数据（带Token） ======================
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("username", sysUser.getUsername());
            data.put("empId", empInfo.getEmpId());
            data.put("empName", empInfo.getEmpName());
            data.put("deptName", sysDept.getDeptName());
            data.put("role", role);
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);

            return JsonUtil.objectToJson(data, "登录成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @RequestMapping(value = "/login/current", method = RequestMethod.GET)
    @ResponseBody
    public String current(HttpSession session) {
        try {
            Object userId = session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录或登录已过期！");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("userId", session.getAttribute("receptionUserId"));
            data.put("username", session.getAttribute("receptionUsername"));
            data.put("empId", session.getAttribute("receptionEmpId"));
            data.put("empName", session.getAttribute("receptionEmpName"));
            data.put("deptName", session.getAttribute("receptionDeptName"));
            data.put("role", session.getAttribute("receptionRole"));
            data.put("avatar", session.getAttribute("receptionAvatar"));

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "获取用户信息成功！");
            result.put("data", data);
            return JsonUtil.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }
    /**
     * 退出登录（删除accessToken，refreshToken拉入黑名单）
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public String logout(HttpSession session) {
        try {
            String accessToken = (String) session.getAttribute("accessToken");
            String refreshToken = (String) session.getAttribute("refreshToken");
            String username = (String) session.getAttribute("receptionUsername");
            String empName = (String) session.getAttribute("receptionEmpName");

            //1.添加未过期token到redis黑名单
            if(accessToken!=null){
                Claims claims = TokenUtil.getClaims(accessToken);
                if (claims != null){
                    // 剩余过期时间
                    long expire = claims.getExpiration().getTime() - System.currentTimeMillis();
                    redisUtil.addBlackList(accessToken, expire);
                }

            }

            // 2. 删除刷新令牌 refreshToken
            if (refreshToken != null) {
                String blackKey = TokenUtil.REFRESH_TOKEN_PREFIX + refreshToken;
                redisUtil.del(blackKey);
            }

            // 记录日志
            if (username != null) {
                logLogout(username, empName);
            }
            //销毁当前会话
            session.invalidate();
            return JsonUtil.objectToJson("", "退出成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    private void logLoginSuccess(SysUser user, EmpInfo empInfo, String role) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
            log.setRoleName("manager".equals(role) ? "部门主管" : "普通员工");
            log.setModule("前台系统");
            log.setOperationType("登录");
            log.setOperationContent("员工[" + empInfo.getEmpName() + "]登录前台系统");
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logLogout(String username, String empName) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUsername(username);
            log.setRoleName("员工");
            log.setModule("前台系统");
            log.setOperationType("退出");
            log.setOperationContent("员工[" + empName + "]退出前台系统");
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}