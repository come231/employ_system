package com.hkd.util;

import com.hkd.pojo.SysOperationLog;
import com.hkd.pojo.SysUser;
import com.hkd.service.SysOperationLogService;

import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * 操作日志工具类
 */
public class OperationLogUtil {
    
    /**
     * 记录操作日志
     * @param session HTTP会话
     * @param sysOperationLogService 操作日志服务
     * @param module 模块名称（如：员工管理、考勤管理等）
     * @param operationType 操作类型（如：新增、修改、删除、查询、审批等）
     * @param operationContent 操作内容描述
     */
    public static void log(HttpSession session, SysOperationLogService sysOperationLogService, 
                          String module, String operationType, String operationContent) {
        try {
            // 尝试从session中获取用户信息（支持多种session属性名）
            Integer userId = null;
            String username = null;
            String roleName = null;
            
            // 首先尝试获取SysUser对象
            SysUser currentUser = (SysUser) session.getAttribute("user");
            if (currentUser != null) {
                userId = currentUser.getUserId();
                username = currentUser.getUsername();
                roleName = "管理员";
            } else {
                // 尝试从adminId、admin、adminRole属性获取
                userId = (Integer) session.getAttribute("adminId");
                username = (String) session.getAttribute("admin");
                roleName = (String) session.getAttribute("adminRole");
            }
            
            if (userId == null || username == null) {
                return; // 未登录用户不记录日志
            }
            
            SysOperationLog log = new SysOperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setRoleName(roleName != null ? roleName : "管理员");
            log.setModule(module);
            log.setOperationType(operationType);
            log.setOperationContent(operationContent);
            log.setOperationTime(new Date());
            log.setStatus((byte) 1); // 1表示成功
            
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            e.printStackTrace();
        }
    }
    
    /**
     * 记录操作失败日志
     */
    public static void logError(HttpSession session, SysOperationLogService sysOperationLogService,
                               String module, String operationType, String operationContent, String errorMsg) {
        try {
            // 尝试从session中获取用户信息（支持多种session属性名）
            Integer userId = null;
            String username = null;
            String roleName = null;
            
            // 首先尝试获取SysUser对象
            SysUser currentUser = (SysUser) session.getAttribute("user");
            if (currentUser != null) {
                userId = currentUser.getUserId();
                username = currentUser.getUsername();
                roleName = "管理员";
            } else {
                // 尝试从adminId、admin、adminRole属性获取
                userId = (Integer) session.getAttribute("adminId");
                username = (String) session.getAttribute("admin");
                roleName = (String) session.getAttribute("adminRole");
            }
            
            if (userId == null || username == null) {
                return; // 未登录用户不记录日志
            }
            
            SysOperationLog log = new SysOperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setRoleName(roleName != null ? roleName : "管理员");
            log.setModule(module);
            log.setOperationType(operationType);
            log.setOperationContent(operationContent);
            log.setOperationTime(new Date());
            log.setStatus((byte) 0); // 0表示失败
            log.setErrorMsg(errorMsg);
            
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
