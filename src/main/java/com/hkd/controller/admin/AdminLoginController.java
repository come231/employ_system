package com.hkd.controller.admin;

import com.hkd.mapper.SysUserDao;
import com.hkd.mapper.SysUserRoleQueryDao;
import com.hkd.pojo.SysOperationLog;
import com.hkd.pojo.SysUser;
import com.hkd.pojo.SysUserRoleQuery;
import com.hkd.service.SysOperationLogService;
import com.hkd.util.BCryptUtil;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.RedisUtil;
import com.hkd.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private SysUserRoleQueryDao sysUserRoleQueryDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysOperationLogService sysOperationLogService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TokenUtil tokenUtil;

    private static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60;

    // 超级管理员登录（带双Token）
    @PostMapping("/login1")
    @ResponseBody
    public String login1(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         HttpSession session) {
        try {
            if (CommonUtil.isEmpty(username) || CommonUtil.isEmpty(password)) {
                return JsonUtil.buildFalseJson(500, "用户名或密码不能为空！");
            }

            SysUserRoleQuery sysUserRoleQuery = sysUserRoleQueryDao.findSysUserRoleBySysUserId(username);
            if (CommonUtil.isEmpty(sysUserRoleQuery)) {
                return JsonUtil.buildFalseJson(404, "账号或密码错误！");
            }

            if (!BCryptUtil.checkPassword(password, sysUserRoleQuery.getPassword())) {
                return JsonUtil.buildFalseJson(404, "账号或密码错误！");
            }

            // 更新登录时间
            sysUserDao.updateLastLoginTime(sysUserRoleQuery.getUserId(), new Date());

            // ====================== 生成双令牌 ======================
            Integer userId = sysUserRoleQuery.getUserId();
            String accessToken = tokenUtil.generateToken(String.valueOf(userId));
            String refreshToken = tokenUtil.generateRefreshToken(userId);

            // 存入Redis
//            redisUtil.set(TokenUtil.ACCESS_TOKEN_PREFIX + accessToken, userId, ACCESS_TOKEN_EXPIRE);
            redisUtil.set(TokenUtil.REFRESH_TOKEN_PREFIX + refreshToken, userId, REFRESH_TOKEN_EXPIRE);

            // Session
            session.setAttribute("adminId", userId);
            session.setAttribute("admin", sysUserRoleQuery.getUsername());
            session.setAttribute("adminRole", sysUserRoleQuery.getRoleName());
            session.setAttribute("avatar", sysUserRoleQuery.getAvatar());
            session.setAttribute("lastLoginTime", sysUserRoleQuery.getLastLoginTime());
            session.setAttribute("accessToken", accessToken);
            session.setAttribute("refreshToken", refreshToken);

            SysUser user = sysUserDao.findById(userId);
            if (user != null) session.setAttribute("user", user);

            // 记录日志
            logLoginSuccess(sysUserRoleQuery);

            // ====================== 返回Token ======================
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("username", sysUserRoleQuery.getUsername());
            result.put("roleName", sysUserRoleQuery.getRoleName());
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);

            return JsonUtil.objectToJson(result, "登录成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    // 获取登录信息
    @RequestMapping(value = "/login/current", method = RequestMethod.GET)
    @ResponseBody
    public String current(HttpSession session) {
        try {
            if (CommonUtil.isEmpty(session.getAttribute("adminId"))) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("msg", "获取成功");
            map.put("adminId", session.getAttribute("adminId"));
            map.put("admin", session.getAttribute("admin"));
            map.put("adminRole", session.getAttribute("adminRole"));
            map.put("avatar", session.getAttribute("avatar"));
            return JsonUtil.toJson(map);
        } catch (Exception e) {
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
            String username = (String) session.getAttribute("admin");
            //1.添加未过期token到redis黑名单
            if(!accessToken.isEmpty()){
                Claims claims = TokenUtil.getClaims(accessToken);
                if (claims != null){
                    // 剩余过期时间
                    long expire = claims.getExpiration().getTime() - System.currentTimeMillis();
                    redisUtil.addBlackList(accessToken, expire);
                }

            }

            // 2. 删除刷新令牌
            if (refreshToken != null) {
                String blackKey = TokenUtil.REFRESH_TOKEN_PREFIX + refreshToken;
                redisUtil.del(blackKey);
            }

            if (username != null) {
                logLogout(username);
            }

            session.invalidate();
            return JsonUtil.objectToJson("", "退出成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    private void logLoginSuccess(SysUserRoleQuery user) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
            log.setRoleName(user.getRoleName());
            log.setModule("系统");
            log.setOperationType("登录");
            log.setOperationContent("管理员登录系统");
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logLogout(String username) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUsername(username);
            log.setRoleName("管理员");
            log.setModule("系统");
            log.setOperationType("退出");
            log.setOperationContent("管理员退出系统");
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}