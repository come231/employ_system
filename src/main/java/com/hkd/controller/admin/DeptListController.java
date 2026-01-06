package com.hkd.controller.admin;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/27
 * @description 部门信息分析数据Controller
 */
@Controller
@RequestMapping("/admin/dept")
public class DeptListController {

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private EmpInfoService empInfoService;

    @Autowired
    private SysOperationLogService sysOperationLogService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysNotificationService sysNotificationService;
    
    /**
     * 获取全部部门列表
     */
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    @ResponseBody
    public String deptList(HttpServletRequest request) {
        try {
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset, pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            
            int totalCount = sysDeptService.findAllCount();
            List<Map<String, Object>> lists = new ArrayList<>();
            List<SysDept> deptList = sysDeptService.findAll(offset, pageSize);

            if (!CommonUtil.isEmpty(deptList) && deptList.size() > 0) {
                for (SysDept sysDept : deptList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("deptId", sysDept.getDeptId());
                    map.put("deptName", sysDept.getDeptName());
                    map.put("status", sysDept.getStatus());
                    map.put("time", sysDept.getCreateTime());
                    map.put("managerId", sysDept.getManagerId());

                    // 处理主管为空的情况
                    if (sysDept.getManagerId() != null) {
                        EmpInfo empInfo = empInfoService.findById(sysDept.getManagerId());
                        map.put("empName", empInfo != null ? empInfo.getEmpName() : "未设置");
                    } else {
                        map.put("empName", "未设置");
                    }

                    List<EmpInfo> empInfoList = empInfoService.findByDeptId(sysDept.getDeptId());
                    map.put("empInfoList", empInfoList);

                    lists.add(map);
                }
                return JsonUtil.buildJsonByTotalCount(lists, totalCount);
            }
            return JsonUtil.buildFalseJson(404, "获取数据失败！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 搜索部门
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public String search(HttpServletRequest request) {
        try {
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            String deptName = request.getParameter("deptName");
            
            PageQuery pageQuery = new PageQuery(offset, pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            
            int totalCount = sysDeptService.findCount(deptName);
            List<Map<String, Object>> lists = new ArrayList<>();
            List<SysDept> deptList = sysDeptService.findByDeptName(deptName, offset, pageSize);

            if (!CommonUtil.isEmpty(deptList) && deptList.size() > 0) {
                for (SysDept sysDept : deptList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("deptId", sysDept.getDeptId());
                    map.put("deptName", sysDept.getDeptName());
                    map.put("status", sysDept.getStatus());
                    map.put("time", sysDept.getCreateTime());
                    map.put("managerId", sysDept.getManagerId());

                    // 处理主管为空的情况
                    if (sysDept.getManagerId() != null) {
                        EmpInfo empInfo = empInfoService.findById(sysDept.getManagerId());
                        map.put("empName", empInfo != null ? empInfo.getEmpName() : "未设置");
                    } else {
                        map.put("empName", "未设置");
                    }

                    List<EmpInfo> empInfoList = empInfoService.findByDeptId(sysDept.getDeptId());
                    map.put("empInfoList", empInfoList);

                    lists.add(map);
                }
                return JsonUtil.buildJsonByTotalCount(lists, totalCount);
            }
            return JsonUtil.buildFalseJson(404, "获取数据失败！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }
    
    /**
     * 获取部门员工列表
     */
    @RequestMapping(value = "/employees", method = RequestMethod.GET)
    @ResponseBody
    public String getDeptEmployees(HttpServletRequest request) {
        try {
            Integer deptId = Integer.parseInt(request.getParameter("deptId"));
            
            // 获取该部门的所有员工
            List<EmpInfo> employees = empInfoService.findByDeptId(deptId);
            
            // 手动分页
            int page = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            int offset = (page - 1) * pageSize;
            
            int totalCount = employees.size();
            int fromIndex = Math.min(offset, employees.size());
            int toIndex = Math.min(offset + pageSize, employees.size());
            List<EmpInfo> pagedEmployees = employees.subList(fromIndex, toIndex);
            
            // 构建返回数据
            List<Map<String, Object>> lists = new ArrayList<>();
            for (EmpInfo emp : pagedEmployees) {
                Map<String, Object> map = new HashMap<>();
                map.put("empNo", emp.getEmpNo());
                map.put("empName", emp.getEmpName());
                map.put("phone", emp.getPhone());
                map.put("email", emp.getEmail());
                map.put("status", emp.getStatus());
                lists.add(map);
            }
            
            return JsonUtil.buildJsonByTotalCount(lists, totalCount);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 编辑部门信息
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public String editDept(HttpServletRequest request, HttpSession session) {
        try {
            Integer deptId = Integer.parseInt(request.getParameter("deptId"));
            String deptName = request.getParameter("deptName");
            String managerIdStr = request.getParameter("managerId");
            Byte status = Byte.parseByte(request.getParameter("status"));

            // 查找部门
            SysDept dept = sysDeptService.findById(deptId);
            if (CommonUtil.isEmpty(dept)) {
                OperationLogUtil.logError(session, sysOperationLogService, "部门管理", "编辑",
                    "编辑部门ID:" + deptId, "部门不存在");
                return JsonUtil.buildFalseJson(404, "部门不存在！");
            }

            Integer oldManagerId = dept.getManagerId();
            Integer newManagerId = CommonUtil.isEmpty(managerIdStr) ? null : Integer.parseInt(managerIdStr);

            // 更新部门信息
            dept.setDeptName(deptName);
            dept.setStatus(status);

            // 处理主管变更时的角色同步
            if (!Objects.equals(oldManagerId, newManagerId)) {
                // 恢复旧主管为普通员工
                if (oldManagerId != null) {
                    revertManagerRole(oldManagerId);
                }
                // 设置新主管并分配主管角色
                if (newManagerId != null) {
                    dept.setManagerId(newManagerId);
                    assignManagerRole(newManagerId, deptName);
                } else {
                    dept.setManagerId(null);
                }
            }

            int result = sysDeptService.update(dept);
            if (result > 0) {
                OperationLogUtil.log(session, sysOperationLogService, "部门管理", "编辑",
                    "编辑部门:" + deptName + "(ID:" + deptId + ")");
                return JsonUtil.objectToJson(null, "编辑成功！");
            }
            OperationLogUtil.logError(session, sysOperationLogService, "部门管理", "编辑",
                "编辑部门ID:" + deptId, "更新数据库失败");
            return JsonUtil.buildFalseJson(500, "编辑失败！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 删除部门
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteDept(HttpServletRequest request, HttpSession session) {
        try {
            Integer deptId = Integer.parseInt(request.getParameter("deptId"));

            // 查找部门
            SysDept dept = sysDeptService.findById(deptId);
            if (CommonUtil.isEmpty(dept)) {
                OperationLogUtil.logError(session, sysOperationLogService, "部门管理", "删除",
                    "删除部门ID:" + deptId, "部门不存在");
                return JsonUtil.buildFalseJson(404, "部门不存在！");
            }

            // 检查部门下是否还有员工
            List<EmpInfo> employees = empInfoService.findByDeptId(deptId);
            if (!CommonUtil.isEmpty(employees) && employees.size() > 0) {
                OperationLogUtil.logError(session, sysOperationLogService, "部门管理", "删除",
                    "删除部门:" + dept.getDeptName(), "该部门下还有员工，无法删除");
                return JsonUtil.buildFalseJson(400, "该部门下还有员工，无法删除！");
            }

            String deptName = dept.getDeptName();
            int result = sysDeptService.delete(deptId);
            if (result > 0) {
                SysRole sysRole = sysRoleService.findByRoleName(deptName+"主管");
                int roleId = sysRole.getRoleId();
                int result1=sysRoleService.delete(roleId);
                if(result1>0){
                    OperationLogUtil.log(session, sysOperationLogService, "部门管理", "删除",
                            "删除部门:" + deptName + "(ID:" + deptId + ")");

                    return JsonUtil.objectToJson(null, "删除成功！");
                }

            }
            OperationLogUtil.logError(session, sysOperationLogService, "部门管理", "删除",
                "删除部门ID:" + deptId, "删除数据库记录失败");
            return JsonUtil.buildFalseJson(500, "删除失败！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 新增部门
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDept(HttpServletRequest request, HttpSession session) {
        try {
            String deptName = request.getParameter("deptName");
            String managerIdStr = request.getParameter("managerId");

            // 参数验证
            if (CommonUtil.isEmpty(deptName)) {
                return JsonUtil.buildFalseJson(400, "部门名称不能为空！");
            }

            // 检查部门名称是否已存在
            List<SysDept> existingDepts = sysDeptService.findByDeptName(deptName, 0, 1);
            if (!CommonUtil.isEmpty(existingDepts)) {
                for (SysDept d : existingDepts) {
                    if (d.getDeptName().equals(deptName)) {
                        return JsonUtil.buildFalseJson(400, "部门名称已存在！");
                    }
                }
            }

            Integer managerId = null;
            if (!CommonUtil.isEmpty(managerIdStr)) {
                managerId = Integer.parseInt(managerIdStr);
            }

            // 创建部门
            SysDept dept = new SysDept();
            dept.setDeptName(deptName);
            dept.setManagerId(managerId);
            dept.setStatus((byte) 1); // 启用
            dept.setCreateTime(new java.util.Date());

            int result = sysDeptService.insert(dept);
            if (result > 0) {
                // 如果设置了主管，分配主管角色
                if (managerId != null) {
                    assignManagerRole(managerId, deptName);
                }
                SysRole sysRole=new SysRole();
                sysRole.setRoleName(deptName+"主管");
                sysRole.setRoleType((byte) 2);
                sysRole.setRoleDesc("管理"+deptName+"员工，审批请假/补卡");
                sysRole.setCreateTime(new java.util.Date());
                sysRole.setStatus((byte) 1);
                int result1=sysRoleService.insert(sysRole);
                if(result1>0){
                    // 记录操作日志
                    OperationLogUtil.log(session, sysOperationLogService, "部门管理", "新增",
                            "新增部门:" + deptName);

                    return JsonUtil.objectToJson(null, "部门创建成功！");
                }


            }
            return JsonUtil.buildFalseJson(500, "创建部门失败！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取普通员工列表（用于选择部门主管，只返回"普通员工"角色的员工）
     */
    @RequestMapping(value = "/allEmployees", method = RequestMethod.GET)
    @ResponseBody
    public String getAllEmployees() {
        try {
            // 获取"普通员工"角色
            SysRole regularRole = sysRoleService.findByRoleName("普通员工");
            if (regularRole == null) {
                // 如果没有找到普通员工角色，返回所有在职员工
                return getAllActiveEmployees();
            }

            // 获取该角色的所有用户
            List<SysUserRole> userRoles = sysUserRoleService.findByRoleId(regularRole.getRoleId());
            List<Map<String, Object>> result = new ArrayList<>();

            if (userRoles != null) {
                for (SysUserRole ur : userRoles) {
                    SysUser user = sysUserService.findById(ur.getUserId());
                    if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                        EmpInfo emp = empInfoService.findById(user.getEmpId());
                        if (emp != null && emp.getStatus() != null && emp.getStatus() == 1) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("empId", emp.getEmpId());
                            map.put("empName", emp.getEmpName());
                            map.put("empNo", emp.getEmpNo());
                            map.put("deptId", emp.getDeptId());
                            result.add(map);
                        }
                    }
                }
            }

            return JsonUtil.objectToJson(result, "获取成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取所有在职员工（备用方法）
     */
    private String getAllActiveEmployees() {
        List<EmpInfo> employees = empInfoService.findAll(0, 1000);
        List<Map<String, Object>> result = new ArrayList<>();

        if (employees != null) {
            for (EmpInfo emp : employees) {
                if (emp.getStatus() != null && emp.getStatus() == 1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("empId", emp.getEmpId());
                    map.put("empName", emp.getEmpName());
                    map.put("empNo", emp.getEmpNo());
                    map.put("deptId", emp.getDeptId());
                    result.add(map);
                }
            }
        }
        return JsonUtil.objectToJson(result, "获取成功！");
    }

    /**
     * 分配主管角色给员工
     * 创建部门专属主管角色（如果不存在），并更新用户角色
     */
    private void assignManagerRole(Integer empId, String deptName) {
        try {
            // 构建主管角色名称（如"研发部主管"）
            String managerRoleName = deptName + "主管";
            SysRole managerRole = sysRoleService.findByRoleName(managerRoleName);

            // 如果角色不存在，创建新角色
            if (managerRole == null) {
                managerRole = new SysRole();
                managerRole.setRoleName(managerRoleName);
                managerRole.setRoleDesc(deptName + "部门主管角色");
                managerRole.setRoleType((byte) 2); // 2 = 部门主管类型
                managerRole.setStatus((byte) 1);
                managerRole.setCreateTime(new java.util.Date());
                sysRoleService.insert(managerRole);

                // 重新获取以获得生成的roleId
                managerRole = sysRoleService.findByRoleName(managerRoleName);
            }

            // 更新用户角色
            if (managerRole != null) {
                SysUser user = sysUserService.findByEmpId(empId);
                if (user != null) {
                    sysUserRoleService.updateRoleByUserId(user.getUserId(), managerRole.getRoleId());

                    // 发送升职通知
                    EmpInfo emp = empInfoService.findById(empId);
                    String empName = emp != null ? emp.getEmpName() : "员工";
                    sysNotificationService.sendNotification(
                        user.getUserId(),
                        "promotion",
                        "职位变更通知",
                        "恭喜您！您已被任命为" + deptName + "部门主管。",
                        empId
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 恢复员工角色为普通员工
     */
    private void revertManagerRole(Integer empId) {
        try {
            // 获取"普通员工"角色
            SysRole regularRole = sysRoleService.findByRoleName("普通员工");
            if (regularRole == null) {
                return;
            }

            // 更新用户角色
            SysUser user = sysUserService.findByEmpId(empId);
            if (user != null) {
                sysUserRoleService.updateRoleByUserId(user.getUserId(), regularRole.getRoleId());

                // 发送职位变更通知
                sysNotificationService.sendNotification(
                    user.getUserId(),
                    "demotion",
                    "职位变更通知",
                    "您的部门主管职位已变更，当前角色为普通员工。",
                    empId
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
