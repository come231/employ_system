package com.hkd.controller.admin;

import com.hkd.mapper.EmpInfoDao;
import com.hkd.mapper.SysRoleDao;
import com.hkd.pojo.EmpInfo;
import com.hkd.pojo.SysDept;
import com.hkd.pojo.SysRole;
import com.hkd.pojo.SysUser;
import com.hkd.pojo.SysUserRole;
import com.hkd.service.EmpInfoService;
import com.hkd.service.SysDeptService;
import com.hkd.service.SysNotificationService;
import com.hkd.service.SysOperationLogService;
import com.hkd.service.SysRoleService;
import com.hkd.service.SysUserRoleService;
import com.hkd.service.SysUserService;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.OperationLogUtil;
import com.hkd.util.PageQuery;
import com.hkd.util.PinyinUtil;
import com.hkd.util.BCryptUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.management.remote.JMXServerErrorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 18:21
 * @description TODO: 获取员工列表数据，根据相应关键字进行查询
 */
@Controller
@RequestMapping("/admin/employee")
public class EmployeeListController {
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private EmpInfoService empInfoService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysOperationLogService sysOperationLogService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysNotificationService sysNotificationService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public String findAll(HttpServletRequest request) {
        try{
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset,pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount=empInfoService.findAllCount();
            int empId;
            List<EmpInfo> empInfos=empInfoService.findAll(offset,pageSize);
            List<Map<String,Object>> lists=new ArrayList<>();
            if(!CommonUtil.isEmpty(empInfos) && empInfos.size()>0){
                for(EmpInfo empInfo:empInfos){
                    SysUser sysUser=sysUserService.findByEmpId(empInfo.getEmpId());
                    SysUserRole sysUserRole=sysUserRoleService.findByUserId(sysUser.getUserId());
                    SysRole sysRole=sysRoleService.findById(sysUserRole.getRoleId());
                    String roleName=sysRole.getRoleName();
                    Map<String,Object> map=new HashMap<>();
                    map.put("code",200);
                    map.put("msg","success");
                    map.put("empNo",empInfo.getEmpNo());//工号
                    map.put("name",empInfo.getEmpName());//姓名
                    map.put("phone",empInfo.getPhone());//手机号
                    map.put("email",empInfo.getEmail());//邮箱
                    map.put("status",empInfo.getStatus());//状态
                    map.put("position",roleName);//职位角色
                    map.put("deptId",empInfo.getDeptId());//部门ID
                    map.put("roleId",sysUserRole.getRoleId());//角色ID
                    map.put("sysUser",sysUser);
                    lists.add(map);
                }
                return JsonUtil.buildJsonByTotalCount(lists,totalCount);
            }
            return JsonUtil.buildFalseJson(404,"获取数据失败！");

        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }

    /**
     * 编辑员工信息
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public String editEmployee(HttpServletRequest request, HttpSession session) {
        try {
            String empNo = request.getParameter("empNo");
            String name = request.getParameter("name");
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");
            String status = request.getParameter("status");
            String deptIdStr = request.getParameter("deptId");
            String roleIdStr = request.getParameter("roleId");

            // 根据工号查找员工
            EmpInfo empInfo = empInfoService.findByNo(empNo);
            if (CommonUtil.isEmpty(empInfo)) {
                OperationLogUtil.logError(session, sysOperationLogService, "员工管理", "编辑",
                    "编辑员工工号:" + empNo, "员工不存在");
                return JsonUtil.buildFalseJson(404, "员工不存在！");
            }

            // 保存旧部门ID（用于后续同步主管）
            Integer oldDeptId = empInfo.getDeptId();

            // 更新员工信息
            empInfo.setEmpName(name);
            empInfo.setPhone(phone);
            empInfo.setEmail(email);
            empInfo.setStatus(Byte.parseByte(status));

            // 更新部门（如果提供）
            if (!CommonUtil.isEmpty(deptIdStr)) {
                empInfo.setDeptId(Integer.parseInt(deptIdStr));
            }
            empInfo.setUpdateTime(new java.util.Date());

            int result = empInfoService.update(empInfo);
            if (result <= 0) {
                OperationLogUtil.logError(session, sysOperationLogService, "员工管理", "编辑",
                    "编辑员工工号:" + empNo, "更新数据库失败");
                return JsonUtil.buildFalseJson(500, "编辑失败！");
            }

            // 用于记录职位变更信息
            String roleChangeInfo = "";

            // 更新角色（如果提供）
            if (!CommonUtil.isEmpty(roleIdStr)) {
                Integer newRoleId = Integer.parseInt(roleIdStr);
                SysUser sysUser = sysUserService.findByEmpId(empInfo.getEmpId());
                if (sysUser != null) {
                    // 获取旧角色信息
                    SysUserRole oldUserRole = sysUserRoleService.findByUserId(sysUser.getUserId());
                    Integer oldRoleId = oldUserRole != null ? oldUserRole.getRoleId() : null;

                    // 记录职位变更信息并发送通知
                    if (oldRoleId != null && !oldRoleId.equals(newRoleId)) {
                        SysRole oldRole = sysRoleService.findById(oldRoleId);
                        SysRole newRole = sysRoleService.findById(newRoleId);
                        String oldRoleName = oldRole != null ? oldRole.getRoleName() : "未知";
                        String newRoleName = newRole != null ? newRole.getRoleName() : "未知";
                        roleChangeInfo = ", 职位:" + oldRoleName + "→" + newRoleName;

                        // 发送职位变更通知给员工
                        sendPositionChangeNotification(sysUser.getUserId(), empInfo, oldRoleName, newRoleName);
                    }

                    // 更新用户角色
                    sysUserRoleService.updateRoleByUserId(sysUser.getUserId(), newRoleId);

                    // 同步更新部门主管ID（传入旧部门ID和新部门ID）
                    syncDeptManagerId(empInfo, oldDeptId, oldRoleId, newRoleId, session);
                }
            }

            OperationLogUtil.log(session, sysOperationLogService, "员工管理", "编辑",
                "编辑员工:" + name + "(工号:" + empNo + ")" + roleChangeInfo);
            return JsonUtil.objectToJson(null, "编辑成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 删除员工
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteEmployee(HttpServletRequest request, HttpSession session) {
        try {
            String empNo = request.getParameter("empNo");

            // 根据工号查找员工
            EmpInfo empInfo = empInfoService.findByNo(empNo);
            if (CommonUtil.isEmpty(empInfo)) {
                OperationLogUtil.logError(session, sysOperationLogService, "员工管理", "删除",
                    "删除员工工号:" + empNo, "员工不存在");
                return JsonUtil.buildFalseJson(404, "员工不存在！");
            }

            String empName = empInfo.getEmpName();
            int result = empInfoService.delete(empInfo.getEmpId());
            if (result > 0) {
                OperationLogUtil.log(session, sysOperationLogService, "员工管理", "删除",
                    "删除员工:" + empName + "(工号:" + empNo + ")");
                return JsonUtil.objectToJson(null, "删除成功！");
            }
            OperationLogUtil.logError(session, sysOperationLogService, "员工管理", "删除",
                "删除员工工号:" + empNo, "删除数据库记录失败");
            return JsonUtil.buildFalseJson(500, "删除失败！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    @RequestMapping(value = "/search",method = RequestMethod.GET)
    @ResponseBody
    public String findByMsg(@RequestParam("type") String type,
                            HttpServletRequest request){
        try{
            String data=request.getParameter("data");
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset,pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount=0;
            List<EmpInfo> empInfos=new ArrayList<>();
            System.out.println(type);
            if("empNo".equals(type)){
                System.out.println(type);
                totalCount=empInfoService.findCountByEmpNo(data);
                empInfos=empInfoService.findByEmpNo(data,offset,pageSize);
            }else if("name".equals(type)){
                // 优化：使用数据库级别分页搜索
                totalCount = empInfoService.findCountByEmpNameLike(data);
                empInfos = empInfoService.findByEmpNameLikePaged(data, offset, pageSize);
            }else if("phone".equals(type)){
                // 优化：使用数据库级别分页搜索
                totalCount = empInfoService.findCountByPhoneLike(data);
                empInfos = empInfoService.findByPhoneLike(data, offset, pageSize);
            }else if("email".equals(type)){
                // 优化：使用数据库级别分页搜索
                totalCount = empInfoService.findCountByEmailLike(data);
                empInfos = empInfoService.findByEmailLike(data, offset, pageSize);
            }else if("status".equals(type)){
                Byte statusValue = "在职".equals(data) ? (byte)1 : (byte)0;
                // 优化：使用数据库级别分页搜索
                totalCount = empInfoService.findCountByStatus(statusValue);
                empInfos = empInfoService.findByStatusPaged(statusValue, offset, pageSize);
            }
            List<Map<String,Object>> lists=new ArrayList<>();
            if(!CommonUtil.isEmpty(empInfos) && !empInfos.isEmpty()){
                for(EmpInfo empInfo:empInfos){
                    SysUser sysUser=sysUserService.findByEmpId(empInfo.getEmpId());
                    SysUserRole sysUserRole=sysUserRoleService.findByUserId(sysUser.getUserId());
                    SysRole sysRole=sysRoleService.findById(sysUserRole.getRoleId());
                    String roleName=sysRole.getRoleName();
                    Map<String,Object> map=new HashMap<>();
                    map.put("empNo",empInfo.getEmpNo());//工号
                    map.put("name",empInfo.getEmpName());//姓名
                    map.put("phone",empInfo.getPhone());//手机号
                    map.put("email",empInfo.getEmail());//邮箱
                    map.put("status",empInfo.getStatus());//状态
                    map.put("position",roleName);//职位角色
                    map.put("deptId",empInfo.getDeptId());//部门ID
                    map.put("roleId",sysUserRole.getRoleId());//角色ID
                    map.put("sysUser",sysUser);
                    lists.add(map);
                }
                System.out.println(lists);

                return JsonUtil.buildJsonByTotalCount(lists,totalCount);
            }
            return JsonUtil.buildFalseJson(404,"获取数据失败！");

        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }

    /**
     * 新增员工
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addEmployee(HttpServletRequest request, HttpSession session) {
        try {
            String empName = request.getParameter("name");
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");
            String deptIdStr = request.getParameter("deptId");
            String roleIdStr = request.getParameter("roleId");

            // 参数验证
            if (CommonUtil.isEmpty(empName)) {
                return JsonUtil.buildFalseJson(400, "员工姓名不能为空！");
            }
            if (CommonUtil.isEmpty(deptIdStr)) {
                return JsonUtil.buildFalseJson(400, "请选择部门！");
            }

            Integer deptId = Integer.parseInt(deptIdStr);
            // 默认角色ID为2（普通员工）
            Integer roleId = CommonUtil.isEmpty(roleIdStr) ? 2 : Integer.parseInt(roleIdStr);

            // 生成工号 (Y + 年份 + 三位序号)
            String empNo = generateEmpNo();

            // 创建员工信息
            EmpInfo empInfo = new EmpInfo();
            empInfo.setEmpNo(empNo);
            empInfo.setEmpName(empName);
            empInfo.setPhone(phone);
            empInfo.setEmail(email);
            empInfo.setDeptId(deptId);
            empInfo.setStatus((byte) 1); // 在职
            empInfo.setCreateTime(new java.util.Date());
            empInfo.setUpdateTime(new java.util.Date());

            int empResult = empInfoService.insert(empInfo);
            if (empResult <= 0) {
                return JsonUtil.buildFalseJson(500, "创建员工信息失败！");
            }

            // 获取新插入的员工ID
            EmpInfo insertedEmp = empInfoService.findByNo(empNo);
            if (insertedEmp == null) {
                return JsonUtil.buildFalseJson(500, "获取新员工信息失败！");
            }

            // 生成用户名（拼音）
            String basePinyin = PinyinUtil.toPinyin(empName);
            String username = generateUniqueUsername(basePinyin);

            // 创建用户信息
            SysUser sysUser = new SysUser();
            sysUser.setEmpId(insertedEmp.getEmpId());
            sysUser.setUsername(username);
            sysUser.setPassword(BCryptUtil.hashPassword("123")); // 使用BCrypt加密默认密码
            sysUser.setStatus((byte) 1); // 启用
            sysUser.setCreateTime(new java.util.Date());

            int userResult = sysUserService.insert(sysUser);
            if (userResult <= 0) {
                return JsonUtil.buildFalseJson(500, "创建用户账号失败！");
            }

            // 获取新插入的用户ID
            SysUser insertedUser = sysUserService.findByUsername(username);
            if (insertedUser == null) {
                return JsonUtil.buildFalseJson(500, "获取新用户信息失败！");
            }

            // 分配角色
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(insertedUser.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRole.setCreateTime(new java.util.Date());

            int roleResult = sysUserRoleService.insert(sysUserRole);
            if (roleResult <= 0) {
                return JsonUtil.buildFalseJson(500, "分配角色失败！");
            }

            // 记录操作日志
            OperationLogUtil.log(session, sysOperationLogService, "员工管理", "新增",
                "新增员工:" + empName + "(工号:" + empNo + ", 用户名:" + username + ")");

            // 返回结果
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("empNo", empNo);
            resultData.put("username", username);
            resultData.put("defaultPassword", "123");

            return JsonUtil.objectToJson(resultData, "员工创建成功！用户名：" + username + "，默认密码：123");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 生成员工工号
     */
    private String generateEmpNo() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        String prefix = "Y" + year;

        // 查询当前年份的员工数量（修复：使用当年员工数而非总数）
        Integer countResult = empInfoService.findCountByYear(year);
        int count = (countResult != null) ? countResult : 0;
        int seq = count + 1;

        return prefix + String.format("%03d", seq);
    }

    /**
     * 生成唯一用户名
     */
    private String generateUniqueUsername(String basePinyin) {
        if (CommonUtil.isEmpty(basePinyin)) {
            basePinyin = "user";
        }

        String username = basePinyin;
        int suffix = 1;

        while (sysUserService.findByUsername(username) != null) {
            username = basePinyin + suffix;
            suffix++;
        }

        return username;
    }

    /**
     * 获取部门列表（用于下拉选择）
     */
    @RequestMapping(value = "/depts", method = RequestMethod.GET)
    @ResponseBody
    public String getDeptList() {
        try {
            List<SysDept> deptList = sysDeptService.findAll(0, 1000);
            List<Map<String, Object>> result = new ArrayList<>();

            if (deptList != null) {
                for (SysDept dept : deptList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("deptId", dept.getDeptId());
                    map.put("deptName", dept.getDeptName());
                    result.add(map);
                }
            }

            return JsonUtil.objectToJson(result, "获取成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取角色列表（用于下拉选择）
     */
    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    @ResponseBody
    public String getRoleList() {
        try {
            List<SysRole> roleList = sysRoleService.findAll();
            List<Map<String, Object>> result = new ArrayList<>();

            if (roleList != null) {
                for (SysRole role : roleList) {
                    // 排除超级管理员角色
                    if (role.getRoleType() != null && role.getRoleType() == 1) {
                        continue;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("roleId", role.getRoleId());
                    map.put("roleName", role.getRoleName());
                    result.add(map);
                }
            }

            return JsonUtil.objectToJson(result, "获取成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 同步更新部门主管ID
     * 当员工职位或部门变更时，自动更新部门表中的主管关联
     * @param empInfo 员工信息（包含新部门ID）
     * @param oldDeptId 旧部门ID
     * @param oldRoleId 旧角色ID
     * @param newRoleId 新角色ID
     * @param session 会话（用于记录日志）
     */
    private void syncDeptManagerId(EmpInfo empInfo, Integer oldDeptId, Integer oldRoleId, Integer newRoleId, HttpSession session) {
        try {
            Integer newDeptId = empInfo.getDeptId();
            Integer empId = empInfo.getEmpId();

            // 判断旧角色是否是主管
            boolean wasManager = isManagerRole(oldRoleId);

            // 判断新角色是否是主管
            boolean isManager = isManagerRole(newRoleId);

            // 部门是否变更
            boolean deptChanged = oldDeptId != null && !oldDeptId.equals(newDeptId);

            // 场景1：原来是主管，现在不是主管了 → 清除旧部门的manager_id
            if (wasManager && !isManager) {
                clearDeptManager(oldDeptId, empId);
            }

            // 场景2：原来不是主管，现在是主管 → 设置新部门的manager_id
            if (!wasManager && isManager) {
                setDeptManager(newDeptId, empId, session);
            }

            // 场景3：原来是主管，现在还是主管，但部门变了 → 清除旧部门，设置新部门
            if (wasManager && isManager && deptChanged) {
                clearDeptManager(oldDeptId, empId);
                setDeptManager(newDeptId, empId, session);
            }

            // 场景4：角色和部门都没变（主管→主管，同部门） → 不处理

        } catch (Exception e) {
            e.printStackTrace();
            // 记录错误但不影响主流程
        }
    }

    /**
     * 判断角色是否是主管角色
     */
    private boolean isManagerRole(Integer roleId) {
        if (roleId == null) {
            return false;
        }
        SysRole role = sysRoleService.findById(roleId);
        return role != null && role.getRoleName() != null && role.getRoleName().contains("主管");
    }

    /**
     * 清除部门的主管ID
     */
    private void clearDeptManager(Integer deptId, Integer empId) {
        if (deptId == null) {
            return;
        }
        SysDept dept = sysDeptService.findById(deptId);
        if (dept != null && dept.getManagerId() != null && dept.getManagerId().equals(empId)) {
            dept.setManagerId(null);
            sysDeptService.update(dept);
        }
    }

    /**
     * 设置部门的主管ID，并将原主管角色改为普通员工
     * @param deptId 部门ID
     * @param newManagerEmpId 新主管的员工ID
     * @param session 会话（用于记录日志）
     */
    private void setDeptManager(Integer deptId, Integer newManagerEmpId, HttpSession session) {
        if (deptId == null) {
            return;
        }
        SysDept dept = sysDeptService.findById(deptId);
        if (dept == null) {
            return;
        }

        // 普通员工角色ID
        final Integer REGULAR_EMPLOYEE_ROLE_ID = 8;

        // 检查是否有原主管，且原主管不是新主管
        Integer oldManagerEmpId = dept.getManagerId();
        if (oldManagerEmpId != null && !oldManagerEmpId.equals(newManagerEmpId)) {
            // 获取原主管信息
            EmpInfo oldManagerEmp = empInfoService.findById(oldManagerEmpId);
            SysUser oldManagerUser = sysUserService.findByEmpId(oldManagerEmpId);

            if (oldManagerUser != null) {
                // 获取原主管的旧角色名称
                SysUserRole oldUserRole = sysUserRoleService.findByUserId(oldManagerUser.getUserId());
                String oldRoleName = "未知";
                if (oldUserRole != null) {
                    SysRole oldRole = sysRoleService.findById(oldUserRole.getRoleId());
                    if (oldRole != null) {
                        oldRoleName = oldRole.getRoleName();
                    }
                }

                // 将原主管的角色改为普通员工
                sysUserRoleService.updateRoleByUserId(oldManagerUser.getUserId(), REGULAR_EMPLOYEE_ROLE_ID);

                // 发送职位变更通知给原主管
                if (oldManagerEmp != null) {
                    sendPositionChangeNotification(oldManagerUser.getUserId(), oldManagerEmp, oldRoleName, "普通员工");
                }

                // 记录原主管降职日志
                String oldManagerName = oldManagerEmp != null ? oldManagerEmp.getEmpName() : "未知";
                String oldManagerNo = oldManagerEmp != null ? oldManagerEmp.getEmpNo() : "未知";
                OperationLogUtil.log(session, sysOperationLogService, "员工管理", "修改",
                    "员工:" + oldManagerName + "(工号:" + oldManagerNo + "), 职位:" + oldRoleName + "→普通员工");
            }
        }

        // 设置新主管
        dept.setManagerId(newManagerEmpId);
        sysDeptService.update(dept);
    }

    /**
     * 发送职位变更通知给员工
     * @param userId 用户ID
     * @param empInfo 员工信息
     * @param oldRoleName 旧职位名称
     * @param newRoleName 新职位名称
     */
    private void sendPositionChangeNotification(Integer userId, EmpInfo empInfo, String oldRoleName, String newRoleName) {
        try {
            // 判断是晋升还是降职（根据角色名称中的关键字判断）
            String notificationType;
            String title;
            if (isPromotion(oldRoleName, newRoleName)) {
                notificationType = "promotion";
                title = "职位晋升通知";
            } else {
                notificationType = "demotion";
                title = "职位变更通知";
            }

            // 构建通知内容
            String content = String.format("您的职位已由【%s】变更为【%s】，请知悉。如有疑问，请联系人事部门。",
                oldRoleName, newRoleName);

            // 发送通知
            sysNotificationService.sendNotification(userId, notificationType, title, content, empInfo.getEmpId());
        } catch (Exception e) {
            // 记录错误但不影响主流程
            e.printStackTrace();
        }
    }

    /**
     * 判断职位变更是否为晋升
     * 简单规则：主管/经理级别高于普通员工
     */
    private boolean isPromotion(String oldRoleName, String newRoleName) {
        // 定义职级顺序（数字越大职级越高）
        int oldLevel = getRoleLevel(oldRoleName);
        int newLevel = getRoleLevel(newRoleName);
        return newLevel > oldLevel;
    }

    /**
     * 获取角色职级
     */
    private int getRoleLevel(String roleName) {
        if (roleName == null) {
            return 0;
        }
        if (roleName.contains("经理") || roleName.contains("总监")) {
            return 3;
        }
        if (roleName.contains("主管")) {
            return 2;
        }
        if (roleName.contains("员工") || roleName.contains("普通")) {
            return 1;
        }
        return 0;
    }

}
