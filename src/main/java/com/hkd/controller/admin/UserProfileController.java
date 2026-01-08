package com.hkd.controller.admin;

import com.hkd.pojo.SysUser;
import com.hkd.pojo.SysUserRoleQuery;
import com.hkd.service.SysOperationLogService;
import com.hkd.service.SysUserRoleQueryService;
import com.hkd.service.SysUserService;
import com.hkd.util.JsonUtil;
import com.hkd.util.OperationLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 18:17
 * @description TODO: 用户个人信息和密码管理，头像上传控制器
 */

@Controller
@RequestMapping("/admin/profile")
@PropertySource("classpath:profile.properties")
public class UserProfileController {
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysUserRoleQueryService sysUserRoleQueryService;
    
    @Autowired
    private SysOperationLogService sysOperationLogService;

    @Value("${upload.avatar.path}")
    private String uploadAvatarPath; // 注入配置文件中的路径
    
    /**
     * 获取当前用户的完整信息（包含角色信息）
     */
    @GetMapping("/info")
    @ResponseBody
    public String getUserProfile(HttpSession session) {
        try {
            SysUser currentUser = (SysUser) session.getAttribute("user");
            if (currentUser == null) {
                return JsonUtil.buildFalseJson(401, "未登录");
            }
            
            // 获取用户角色信息
            SysUserRoleQuery userRoleInfo = sysUserRoleQueryService.findByUserId(currentUser.getUserId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("userId", currentUser.getUserId());
            result.put("username", currentUser.getUsername());
            result.put("avatar", currentUser.getAvatar());
            result.put("createTime", currentUser.getCreateTime());
            result.put("roleName", userRoleInfo != null ? userRoleInfo.getRoleName() : "未分配角色");
            
            return JsonUtil.toJson(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "获取用户信息失败！");
        }
    }
    
    /**
     * 更新用户头像和用户名
     */
    @PostMapping("/update")
    @ResponseBody
    public String updateUserProfile(@RequestParam("username") String username,
                                   @RequestParam(value = "avatar", required = false) String avatar,
                                   HttpSession session) {
        try {
            SysUser currentUser = (SysUser) session.getAttribute("user");
            if (currentUser == null) {
                return JsonUtil.buildFalseJson(401, "未登录");
            }

            // 检查用户名是否已被其他用户使用
            if (!username.equals(currentUser.getUsername())) {
                SysUser existUser = sysUserService.findByUsername(username);
                if (existUser != null && !existUser.getUserId().equals(currentUser.getUserId())) {
                    return JsonUtil.buildFalseJson(400, "用户名已被使用");
                }
            }

            // 更新用户信息
            currentUser.setUsername(username);
            if (avatar != null && !avatar.isEmpty()) {
                currentUser.setAvatar(avatar);
            }

            int result = sysUserService.update(currentUser);
            if (result > 0) {
                // 更新session中的用户信息
                session.setAttribute("user", currentUser);
                session.setAttribute("admin", username);
                session.setAttribute("avatar", currentUser.getAvatar());
                OperationLogUtil.log(session, sysOperationLogService, "个人中心", "修改",
                    "更新个人信息,用户名:" + username);
                return JsonUtil.buildSuccessJson(200, "更新成功");
            } else {
                return JsonUtil.buildFalseJson(500, "更新失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "更新用户信息失败！");
        }
    }

    /**
     * 上传头像文件
     */
    /**
     * 上传头像文件
     */
    @PostMapping("/uploadAvatar")
    @ResponseBody
    public String uploadAvatar(@RequestParam("file") MultipartFile file,
                               HttpServletRequest request,
                               HttpSession session) {
        // 定义唯一文件名（全局可用，方便双目录写入）
        String newFilename = null;
        // 定义源码目录文件和部署目录文件
        File sourceDestFile = null;
        File deployDestFile = null;

        try {
            SysUser currentUser = (SysUser) session.getAttribute("user");
            if (currentUser == null) {
                return JsonUtil.buildFalseJson(401, "未登录");
            }

            if (file.isEmpty()) {
                return JsonUtil.buildFalseJson(400, "请选择文件");
            }

            // ========== 1. 基础校验 + 注入路径打印 ==========
            System.out.println("【头像上传】注入的源码目录路径：" + uploadAvatarPath);
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return JsonUtil.buildFalseJson(400, "无效的图片文件");
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            // 图片格式校验
            if (!".jpg".equals(suffix) && !".jpeg".equals(suffix) && !".png".equals(suffix) && !".gif".equals(suffix)) {
                return JsonUtil.buildFalseJson(400, "只支持jpg、jpeg、png、gif格式的图片");
            }
            // 图片大小校验（4MB）
            if (file.getSize() > 4 * 1024 * 1024) {
                return JsonUtil.buildFalseJson(400, "图片大小不能超过4MB");
            }

            // ========== 2. 处理两个核心路径 ==========
            // ① 源码目录路径（补全末尾斜杠）
            String sourceUploadPath = uploadAvatarPath;
            if (sourceUploadPath != null && !sourceUploadPath.endsWith("/") && !sourceUploadPath.endsWith("\\")) {
                sourceUploadPath = sourceUploadPath + "/";
            }
            // ② 部署目录路径（获取Tomcat部署后的static/images目录，自动补全斜杠）
            String deployUploadPath = request.getServletContext().getRealPath("/static/images/");
            System.out.println("【头像上传】部署后的目录路径：" + deployUploadPath);

            // ========== 3. 创建两个目录（若不存在） ==========
            // 源码目录
            File sourceUploadDir = new File(sourceUploadPath);
            if (!sourceUploadDir.exists()) {
                sourceUploadDir.mkdirs();
                System.out.println("【头像上传】源码目录不存在，已自动创建：" + sourceUploadDir.getAbsolutePath());
            }
            // 部署目录
            File deployUploadDir = new File(deployUploadPath);
            if (!deployUploadDir.exists()) {
                deployUploadDir.mkdirs();
                System.out.println("【头像上传】部署目录不存在，已自动创建：" + deployUploadDir.getAbsolutePath());
            }

            // ========== 4. 生成唯一文件名（避免重复） ==========
            newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
            // 源码目录文件对象
            sourceDestFile = new File(sourceUploadDir, newFilename);
            // 部署目录文件对象
            deployDestFile = new File(deployUploadDir, newFilename);

            // ========== 5. 双目录同步写入文件（核心：先写源码，再写部署） ==========
            // ① 写入源码目录
            file.transferTo(sourceDestFile);
            System.out.println("【头像上传】源码目录文件写入成功：" + sourceDestFile.getAbsolutePath());
            // ② 写入部署目录（使用Files.copy，兼容更多场景，若失败则回滚源码文件）
            Files.copy(sourceDestFile.toPath(), deployDestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("【头像上传】部署目录文件写入成功：" + deployDestFile.getAbsolutePath());

            // ========== 6. 数据库 + Session 更新 ==========
            String avatarPath = "/static/images/" + newFilename; // 统一相对路径
            currentUser.setAvatar(avatarPath);
            int result = sysUserService.update(currentUser);
            if (result > 0) {
                session.setAttribute("user", currentUser);
                session.setAttribute("avatar", avatarPath);

                Map<String, Object> data = new HashMap<>();
                data.put("code", 200);
                data.put("msg", "上传成功（源码+部署目录同步完成）");
                data.put("avatar", avatarPath);
                return JsonUtil.toJson(data);
            } else {
                // 数据库更新失败，删除两个目录的临时文件（垃圾清理）
                if (sourceDestFile.exists()) {
                    sourceDestFile.delete();
                    System.out.println("【头像上传】数据库更新失败，已删除源码目录临时文件");
                }
                if (deployDestFile.exists()) {
                    deployDestFile.delete();
                    System.out.println("【头像上传】数据库更新失败，已删除部署目录临时文件");
                }
                return JsonUtil.buildFalseJson(500, "保存头像失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
            // IO异常时，清理已写入的文件
            if (sourceDestFile != null && sourceDestFile.exists()) {
                sourceDestFile.delete();
            }
            if (deployDestFile != null && deployDestFile.exists()) {
                deployDestFile.delete();
            }
            System.out.println("【头像上传】文件写入IO异常：" + e.getMessage());
            return JsonUtil.buildFalseJson(500, "文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // 未知异常时，清理已写入的文件
            if (sourceDestFile != null && sourceDestFile.exists()) {
                sourceDestFile.delete();
            }
            if (deployDestFile != null && deployDestFile.exists()) {
                deployDestFile.delete();
            }
            System.out.println("【头像上传】未知异常：" + e.getMessage());
            return JsonUtil.buildFalseJson(500, "上传失败！");
        }
    }
    
    /**
     * 验证原密码
     */
    @PostMapping("/verifyPassword")
    @ResponseBody
    public String verifyPassword(@RequestParam("password") String password, HttpSession session) {
        try {
            SysUser currentUser = (SysUser) session.getAttribute("user");
            if (currentUser == null) {
                return JsonUtil.buildFalseJson(401, "未登录");
            }
            
            if (currentUser.getPassword().equals(password)) {
                return JsonUtil.buildSuccessJson(200, "密码正确");
            } else {
                return JsonUtil.buildFalseJson(400, "密码错误");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "验证失败！");
        }
    }
    
    /**
     * 验证密钥（用于忘记密码）
     */
    @PostMapping("/verifyKey")
    @ResponseBody
    public String verifyKey(@RequestParam("key") String key, HttpSession session) {
        try {
            // 密钥验证（这里使用固定密钥"root"）
            if ("root".equals(key)) {
                return JsonUtil.buildSuccessJson(200, "密钥正确");
            } else {
                return JsonUtil.buildFalseJson(400, "密钥错误");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "验证失败！");
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    @ResponseBody
    public String changePassword(@RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session) {
        try {
            SysUser currentUser = (SysUser) session.getAttribute("user");
            if (currentUser == null) {
                return JsonUtil.buildFalseJson(401, "未登录");
            }
            
            // 验证两次密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                return JsonUtil.buildFalseJson(400, "两次密码不一致");
            }
            
            // 更新密码
            currentUser.setPassword(newPassword);
            int result = sysUserService.update(currentUser);
            
            if (result > 0) {
                // 更新session中的用户信息
                session.setAttribute("user", currentUser);
                OperationLogUtil.log(session, sysOperationLogService, "个人中心", "修改", 
                    "修改登录密码");
                return JsonUtil.buildSuccessJson(200, "密码修改成功");
            } else {
                return JsonUtil.buildFalseJson(500, "密码修改失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "修改密码失败！");
        }
    }
}
