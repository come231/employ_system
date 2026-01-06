package com.hkd.controller.reception;

import com.hkd.mapper.*;
import com.hkd.pojo.*;
import com.hkd.service.SysOperationLogService;
import com.hkd.util.BCryptUtil;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * 个人中心控制器
 */
@Controller
@RequestMapping("/reception/center")
@PropertySource("classpath:profile.properties")
public class ReceptionCenterController {

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private EmpInfoDao empInfoDao;

    @Autowired
    private SysDeptDao sysDeptDao;

    @Autowired
    private AttendanceRecordDao attendanceRecordDao;

    @Autowired
    private LeaveApplicationDao leaveApplicationDao;

    @Autowired
    private SupplementCardDao supplementCardDao;

    @Autowired
    private SysOperationLogService sysOperationLogService;

    @Value("${upload.avatar.path}")
    private String uploadAvatarPath;

    /**
     * 获取个人信息
     */
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    @ResponseBody
    public String getProfile(HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            EmpInfo empInfo = empInfoDao.findById(empId);
            SysUser sysUser = (SysUser) session.getAttribute("receptionUser");
            SysDept sysDept = sysDeptDao.findById(empInfo.getDeptId());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Map<String, Object> data = new HashMap<>();
            data.put("empId", empInfo.getEmpId());
            data.put("empNo", empInfo.getEmpNo());
            data.put("empName", empInfo.getEmpName());
            data.put("deptId", empInfo.getDeptId());
            data.put("deptName", sysDept != null ? sysDept.getDeptName() : "");
            data.put("phone", empInfo.getPhone());
            data.put("email", empInfo.getEmail());
            data.put("status", empInfo.getStatus());
            data.put("createTime", empInfo.getCreateTime() != null ? sdf.format(empInfo.getCreateTime()) : "");
            data.put("avatar", sysUser != null ? sysUser.getAvatar() : "");
            data.put("username", sysUser != null ? sysUser.getUsername() : "");
            data.put("role", session.getAttribute("receptionRole"));

            // 获取部门主管信息
            if (sysDept != null && sysDept.getManagerId() != null) {
                EmpInfo manager = empInfoDao.findById(sysDept.getManagerId());
                if (manager != null) {
                    data.put("managerName", manager.getEmpName());
                }
            }

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 更新个人信息
     */
    @RequestMapping(value = "/profile/update", method = RequestMethod.POST)
    @ResponseBody
    public String updateProfile(HttpServletRequest request, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            String phone = request.getParameter("phone");
            String email = request.getParameter("email");

            EmpInfo empInfo = empInfoDao.findById(empId);
            if (empInfo == null) {
                return JsonUtil.buildFalseJson(404, "员工信息不存在！");
            }

            empInfo.setPhone(phone);
            empInfo.setEmail(email);
            empInfo.setUpdateTime(new Date());

            empInfoDao.update(empInfo);

            // 更新session
            session.setAttribute("receptionPhone", phone);
            session.setAttribute("receptionEmail", email);

            // 记录操作日志
            logOperation(session, "个人中心", "修改", "更新个人信息");

            return JsonUtil.objectToJson("", "更新成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 验证原密码
     */
    @RequestMapping(value = "/password/verify", method = RequestMethod.POST)
    @ResponseBody
    public String verifyPassword(@RequestParam("password") String password, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            SysUser user = sysUserDao.findById(userId);
            if (user == null) {
                return JsonUtil.buildFalseJson(404, "用户不存在！");
            }

//            if (!BCryptUtil.checkPassword(password, user.getPassword())) {
//                return JsonUtil.buildFalseJson(400, "原密码错误！");
//            }
            //测试代码
            if (!user.getPassword().equals(password)) {
                return JsonUtil.buildFalseJson(400, "原密码错误！");
            }

            return JsonUtil.objectToJson("", "验证通过！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @RequestMapping(value = "/password/change", method = RequestMethod.POST)
    @ResponseBody
    public String changePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            if (!newPassword.equals(confirmPassword)) {
                return JsonUtil.buildFalseJson(400, "两次密码输入不一致！");
            }

            SysUser user = sysUserDao.findById(userId);
            if (user == null) {
                return JsonUtil.buildFalseJson(404, "用户不存在！");
            }

            user.setPassword(BCryptUtil.hashPassword(newPassword));
            sysUserDao.update(user);

            // 记录操作日志
            logOperation(session, "个人中心", "修改", "修改登录密码");

            return JsonUtil.objectToJson("", "密码修改成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取我的考勤记录
     */
    @RequestMapping(value = "/attendance/data", method = RequestMethod.GET)
    @ResponseBody
    public String getMyAttendance(HttpServletRequest request, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");
            String statusStr = request.getParameter("status");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");

            PageQuery pageQuery = new PageQuery(page, pageSize);
            int offset = pageQuery.getOffset();

            Integer status = null;
            if (!CommonUtil.isEmpty(statusStr)) {
                status = Integer.parseInt(statusStr);
            }

            LocalDate startDate = null;
            LocalDate endDate = null;
            if (!CommonUtil.isEmpty(startDateStr)) {
                startDate = LocalDate.parse(startDateStr);
            }
            if (!CommonUtil.isEmpty(endDateStr)) {
                endDate = LocalDate.parse(endDateStr);
            }

            // 默认查询最近一个月
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            List<AttendanceRecord> records = attendanceRecordDao.findByEmpIdAndDateRange(
                    empId, startDate, endDate, status != null ? status : -1, -1);

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

            List<Map<String, Object>> list = new ArrayList<>();
            if (records != null) {
                for (AttendanceRecord record : records) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("recordId", record.getRecordId());
                    map.put("checkDate", record.getCheckDate() != null ? sdfDate.format(record.getCheckDate()) : "");
                    map.put("checkTime", record.getCheckInTime() != null ? sdfTime.format(record.getCheckInTime()) : "");
                    map.put("type", record.getType());
                    map.put("status", record.getStatus());
                    map.put("remark", record.getRemark());
                    list.add(map);
                }
            }

            // 分页处理
            int total = list.size();
            int start = Math.min(offset, total);
            int end = Math.min(offset + pageSize, total);
            List<Map<String, Object>> pagedList = list.subList(start, end);

            return JsonUtil.buildJsonByTotalCount(pagedList, total);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取我的补卡记录
     */
    @RequestMapping(value = "/supplement/data", method = RequestMethod.GET)
    @ResponseBody
    public String getMySupplement(HttpServletRequest request, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");

            PageQuery pageQuery = new PageQuery(page, pageSize);
            int offset = pageQuery.getOffset();

            int totalCount = supplementCardDao.findCountByEmpId(empId);
            List<SupplementCard> records = supplementCardDao.findByEmpId(empId, offset, pageSize);

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<Map<String, Object>> list = new ArrayList<>();
            if (records != null) {
                for (SupplementCard record : records) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("supplementId", record.getSupplementId());
                    map.put("checkDate", record.getCheckDate() != null ? sdfDate.format(record.getCheckDate()) : "");
                    map.put("checkType", record.getCheckType());
                    map.put("checkTypeName", getCheckTypeName(record.getCheckType()));
                    map.put("actualTime", record.getActualTime() != null ? sdfTime.format(record.getActualTime()) : "");
                    map.put("reason", record.getReason());
                    map.put("approveStatus", record.getApproveStatus());
                    map.put("approveStatusName", getApproveStatusName(record.getApproveStatus()));
                    map.put("approveOpinion", record.getApproveOpinion());
                    map.put("createTime", record.getCreateTime() != null ? sdfDateTime.format(record.getCreateTime()) : "");

                    // 获取审批人信息
                    if (record.getApproverId() != null) {
                        EmpInfo approver = empInfoDao.findById(record.getApproverId());
                        map.put("approverName", approver != null ? approver.getEmpName() : "");
                    } else {
                        map.put("approverName", "-");
                    }

                    list.add(map);
                }
            }

            return JsonUtil.buildJsonByTotalCount(list, totalCount);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取我的请假记录
     */
    @RequestMapping(value = "/leave/data", method = RequestMethod.GET)
    @ResponseBody
    public String getMyLeave(HttpServletRequest request, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");

            PageQuery pageQuery = new PageQuery(page, pageSize);
            int offset = pageQuery.getOffset();

            int totalCount = leaveApplicationDao.findCountByEmpId(empId);
            List<LeaveApplication> records = leaveApplicationDao.findByEmpId(empId, offset, pageSize);

            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            List<Map<String, Object>> list = new ArrayList<>();
            if (records != null) {
                for (LeaveApplication record : records) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("leaveId", record.getLeaveId());
                    map.put("leaveType", record.getLeaveType());
                    map.put("startTime", record.getStartTime() != null ? sdfDateTime.format(record.getStartTime()) : "");
                    map.put("endTime", record.getEndTime() != null ? sdfDateTime.format(record.getEndTime()) : "");
                    map.put("leaveDays", record.getLeaveDays());
                    map.put("reason", record.getReason());
                    map.put("approveStatus", record.getApproveStatus());
                    map.put("approveStatusName", getApproveStatusName(record.getApproveStatus()));
                    map.put("approveOpinion", record.getApproveOpinion());
                    map.put("createTime", record.getCreateTime() != null ? sdfDateTime.format(record.getCreateTime()) : "");

                    // 获取审批人信息
                    if (record.getApproverId() != null) {
                        EmpInfo approver = empInfoDao.findById(record.getApproverId());
                        map.put("approverName", approver != null ? approver.getEmpName() : "");
                    } else {
                        map.put("approverName", "-");
                    }

                    list.add(map);
                }
            }

            return JsonUtil.buildJsonByTotalCount(list, totalCount);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取部门信息
     */
    @RequestMapping(value = "/dept/info", method = RequestMethod.GET)
    @ResponseBody
    public String getDeptInfo(HttpSession session) {
        try {
            Integer deptId = (Integer) session.getAttribute("receptionDeptId");
            if (CommonUtil.isEmpty(deptId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            SysDept dept = sysDeptDao.findById(deptId);
            if (dept == null) {
                return JsonUtil.buildFalseJson(404, "部门信息不存在！");
            }

            // 获取部门员工列表
            List<EmpInfo> employees = empInfoDao.findByDeptId(deptId);

            // 获取部门主管信息
            EmpInfo manager = null;
            if (dept.getManagerId() != null) {
                manager = empInfoDao.findById(dept.getManagerId());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Map<String, Object> data = new HashMap<>();
            data.put("deptId", dept.getDeptId());
            data.put("deptName", dept.getDeptName());
            data.put("employeeCount", employees != null ? employees.size() : 0);

            if (manager != null) {
                data.put("managerName", manager.getEmpName());
                data.put("managerPhone", manager.getPhone());
                data.put("managerEmail", manager.getEmail());
            }

            // 员工列表
            List<Map<String, Object>> empList = new ArrayList<>();
            if (employees != null) {
                for (EmpInfo emp : employees) {
                    Map<String, Object> empMap = new HashMap<>();
                    empMap.put("empId", emp.getEmpId());
                    empMap.put("empNo", emp.getEmpNo());
                    empMap.put("empName", emp.getEmpName());
                    empMap.put("phone", emp.getPhone());
                    empMap.put("email", emp.getEmail());
                    empMap.put("status", emp.getStatus());
                    empMap.put("createTime", emp.getCreateTime() != null ? sdf.format(emp.getCreateTime()) : "");
                    empMap.put("isManager", emp.getEmpId().equals(dept.getManagerId()));
                    empList.add(empMap);
                }
            }
            data.put("employees", empList);

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 撤销申请
     */
    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    @ResponseBody
    public String withdrawApplication(@RequestParam("type") String type,
                                      @RequestParam("id") Integer id,
                                      HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            if ("leave".equals(type)) {
                LeaveApplication leave = leaveApplicationDao.findById(id);
                if (leave == null || !leave.getEmpId().equals(empId)) {
                    return JsonUtil.buildFalseJson(404, "申请不存在或无权操作！");
                }
                if (leave.getApproveStatus() != 0) {
                    return JsonUtil.buildFalseJson(400, "只能撤销待审批的申请！");
                }
                leaveApplicationDao.delete(id);
            } else if ("supplement".equals(type)) {
                SupplementCard supplement = supplementCardDao.findById(id);
                if (supplement == null || !supplement.getEmpId().equals(empId)) {
                    return JsonUtil.buildFalseJson(404, "申请不存在或无权操作！");
                }
                if (supplement.getApproveStatus() != 0) {
                    return JsonUtil.buildFalseJson(400, "只能撤销待审批的申请！");
                }
                supplementCardDao.delete(id);
            } else {
                return JsonUtil.buildFalseJson(400, "无效的申请类型！");
            }

            logOperation(session, "个人中心", "撤销", "撤销" + ("leave".equals(type) ? "请假" : "补卡") + "申请");

            return JsonUtil.objectToJson("", "撤销成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取请假详情
     */
    @RequestMapping(value = "/leave/detail", method = RequestMethod.GET)
    @ResponseBody
    public String getLeaveDetail(@RequestParam("leaveId") Integer leaveId, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            LeaveApplication leave = leaveApplicationDao.findById(leaveId);
            if (leave == null) {
                return JsonUtil.buildFalseJson(404, "请假申请不存在！");
            }

            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Map<String, Object> data = new HashMap<>();
            data.put("leaveId", leave.getLeaveId());
            data.put("leaveType", leave.getLeaveType());
            data.put("startTime", leave.getStartTime() != null ? sdfDateTime.format(leave.getStartTime()) : "");
            data.put("endTime", leave.getEndTime() != null ? sdfDateTime.format(leave.getEndTime()) : "");
            data.put("leaveDays", leave.getLeaveDays());
            data.put("reason", leave.getReason());
            data.put("approveStatus", leave.getApproveStatus());
            data.put("approveOpinion", leave.getApproveOpinion());

            if (leave.getApproverId() != null) {
                EmpInfo approver = empInfoDao.findById(leave.getApproverId());
                data.put("approverName", approver != null ? approver.getEmpName() : "");
            }

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取补卡详情
     */
    @RequestMapping(value = "/supplement/detail", method = RequestMethod.GET)
    @ResponseBody
    public String getSupplementDetail(@RequestParam("supplementId") Integer supplementId, HttpSession session) {
        try {
            Integer empId = (Integer) session.getAttribute("receptionEmpId");
            if (CommonUtil.isEmpty(empId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            SupplementCard supplement = supplementCardDao.findById(supplementId);
            if (supplement == null) {
                return JsonUtil.buildFalseJson(404, "补卡申请不存在！");
            }

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

            Map<String, Object> data = new HashMap<>();
            data.put("supplementId", supplement.getSupplementId());
            data.put("checkDate", supplement.getCheckDate() != null ? sdfDate.format(supplement.getCheckDate()) : "");
            data.put("checkType", supplement.getCheckType());
            data.put("actualTime", supplement.getActualTime() != null ? sdfTime.format(supplement.getActualTime()) : "");
            data.put("reason", supplement.getReason());
            data.put("approveStatus", supplement.getApproveStatus());
            data.put("approveOpinion", supplement.getApproveOpinion());

            if (supplement.getApproverId() != null) {
                EmpInfo approver = empInfoDao.findById(supplement.getApproverId());
                data.put("approverName", approver != null ? approver.getEmpName() : "");
            }

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    private String getCheckTypeName(Byte checkType) {
        if (checkType == null) return "";
        switch (checkType) {
            case 1: return "上班";
            case 2: return "下班";
            case 3: return "上下班";
            default: return "";
        }
    }

    private String getApproveStatusName(Byte status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "";
        }
    }

    /**
     * 上传头像
     */
    @RequestMapping(value = "/uploadAvatar", method = RequestMethod.POST)
    @ResponseBody
    public String uploadAvatar(@RequestParam("file") MultipartFile file,
                               HttpServletRequest request,
                               HttpSession session) {
        File sourceDestFile = null;
        File deployDestFile = null;
        String newFilename = null;

        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            if (file.isEmpty()) {
                return JsonUtil.buildFalseJson(400, "请选择文件");
            }

            // 校验文件
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return JsonUtil.buildFalseJson(400, "无效的图片文件");
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!".jpg".equals(suffix) && !".jpeg".equals(suffix) && !".png".equals(suffix) && !".gif".equals(suffix)) {
                return JsonUtil.buildFalseJson(400, "只支持jpg、jpeg、png、gif格式的图片");
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                return JsonUtil.buildFalseJson(400, "图片大小不能超过2MB");
            }

            // 处理路径
            String sourceUploadPath = uploadAvatarPath;
            if (sourceUploadPath != null && !sourceUploadPath.endsWith("/") && !sourceUploadPath.endsWith("\\")) {
                sourceUploadPath = sourceUploadPath + "/";
            }
            String deployUploadPath = request.getServletContext().getRealPath("/static/images/");

            // 创建目录
            File sourceUploadDir = new File(sourceUploadPath);
            if (!sourceUploadDir.exists()) {
                sourceUploadDir.mkdirs();
            }
            File deployUploadDir = new File(deployUploadPath);
            if (!deployUploadDir.exists()) {
                deployUploadDir.mkdirs();
            }

            // 生成文件名
            newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
            sourceDestFile = new File(sourceUploadDir, newFilename);
            deployDestFile = new File(deployUploadDir, newFilename);

            // 写入文件
            file.transferTo(sourceDestFile);
            Files.copy(sourceDestFile.toPath(), deployDestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 更新数据库
            String avatarPath = "/static/images/" + newFilename;
            SysUser user = sysUserDao.findById(userId);
            if (user != null) {
                user.setAvatar(avatarPath);
                sysUserDao.update(user);
                session.setAttribute("receptionUser", user);
                session.setAttribute("receptionAvatar", avatarPath);

                Map<String, Object> data = new HashMap<>();
                data.put("code", 200);
                data.put("msg", "上传成功");
                data.put("avatar", avatarPath);
                return JsonUtil.toJson(data);
            } else {
                if (sourceDestFile.exists()) sourceDestFile.delete();
                if (deployDestFile.exists()) deployDestFile.delete();
                return JsonUtil.buildFalseJson(500, "用户不存在");
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (sourceDestFile != null && sourceDestFile.exists()) sourceDestFile.delete();
            if (deployDestFile != null && deployDestFile.exists()) deployDestFile.delete();
            return JsonUtil.buildFalseJson(500, "文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            if (sourceDestFile != null && sourceDestFile.exists()) sourceDestFile.delete();
            if (deployDestFile != null && deployDestFile.exists()) deployDestFile.delete();
            return JsonUtil.buildFalseJson(500, "上传失败！");
        }
    }

    private void logOperation(HttpSession session, String module, String type, String content) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setUserId((Integer) session.getAttribute("receptionUserId"));
            log.setUsername((String) session.getAttribute("receptionUsername"));
            log.setRoleName((String) session.getAttribute("receptionRole"));
            log.setModule(module);
            log.setOperationType(type);
            log.setOperationContent(content);
            log.setOperationTime(new Date());
            log.setStatus((byte) 1);
            sysOperationLogService.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
