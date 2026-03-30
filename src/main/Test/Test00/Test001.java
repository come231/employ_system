package Test00;

import com.hkd.pojo.*;
import com.hkd.service.*;
import com.hkd.util.BCryptUtil;
import com.hkd.util.CommonUtil;
import com.hkd.util.PageQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 19:41
 * @description TODO: 测试管理员页面功能
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml","classpath:springmvc.xml"})
public class Test001 {
    @Autowired
    SysUserRoleQueryService sysUserRoleQueryService;
    @Autowired
    AttendanceRecordService attendanceRecordService;
    @Autowired
    EmpInfoService empInfoService;
    @Autowired
    LeaveApplicationService leaveApplicationService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysDeptService sysDeptService;


    //测试管理员页面的登录功能
    @Test
    public void test1(){
        Integer roleType=1;
        SysUser sysUserRoleQuery = sysUserService.login("zhangsan","$2a$10$Ng1FXTFiwc09rU0gUd.23uqx.8seIyfB0HLpB5HQYcl3UUn8FkMDG");
        System.out.println(sysUserRoleQuery);
        if(!CommonUtil.isEmpty(sysUserRoleQuery)){
            System.out.println(sysUserRoleQuery.getUserId());
            System.out.println(BCryptUtil.checkPassword("123","$2a$10$Ng1FXTFiwc09rU0gUd.23uqx.8seIyfB0HLpB5HQYcl3UUn8FkMDG"));
        }
    }

    //测试管理员首页的数据显示
    @Test
    public void test2(){
        LocalDate localDate =LocalDate.now();
        LocalDateTime localTime=LocalDateTime.of(localDate, LocalTime.MIN);
        LocalDateTime endTime =LocalDateTime.of(localDate,LocalTime.MAX);
        Map<String,Object> result=new HashMap<>();
        result.put("code",200);
        result.put("msg","获取数据成功！");
        result.put("employCount",empInfoService.findCount(1));
        result.put("successCount",attendanceRecordService.findByEmpIdAndDateRange(-1,localDate,localDate,0,1).size());
        result.put("laterCount",attendanceRecordService.findByEmpIdAndDateRange(-1,localDate,localDate,1,1).size());
        result.put("leaveCount",leaveApplicationService.findByDateRange(-1,localTime,endTime,-1,"").size());
        System.out.println(result);
    }

    //测试员工信息展示
    @Test
    public void test3() {
        PageQuery pageQuery = new PageQuery();
        int offset = pageQuery.getOffset();
        int pageSize = pageQuery.getPageSize();
        List<EmpInfo> empInfos = empInfoService.findByEmpNo("Y2025",offset,pageSize);
        List<Map<String, Object>> lists = new ArrayList<>();
        if (!CommonUtil.isEmpty(empInfos) && empInfos.size() > 0) {
            for (EmpInfo empInfo : empInfos) {
                SysUser sysUser = sysUserService.findByEmpId(empInfo.getEmpId());
                SysUserRole sysUserRole = sysUserRoleService.findByUserId(sysUser.getUserId());
                SysRole sysRole = sysRoleService.findById(sysUserRole.getRoleId());
                String roleName = sysRole.getRoleName();
                Map<String, Object> map = new HashMap<>();
                map.put("code", 200);
                map.put("msg", "success");
                map.put("empNo", empInfo.getEmpNo());//工号
                map.put("username", empInfo.getEmpName());//姓名
                map.put("phone", empInfo.getPhone());//手机号
                map.put("email", empInfo.getEmail());//邮箱
                map.put("status", empInfo.getStatus());//状态
                map.put("roleName", roleName);//职位角色
                map.put("sysUser", sysUser);
                lists.add(map);
            }
            System.out.println(lists);
        }
    }

    //测试部门全部员工信息的展示
    @Test
    public void test40() {
        int totalCount = sysDeptService.findAllCount();
        List<Map<String, Object>> lists = new ArrayList<>();
        List<SysDept> deptList = sysDeptService.findAll(0, 5);
        if (!CommonUtil.isEmpty(deptList) && deptList.size() > 0) {
            for (SysDept sysDept : deptList) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("code", 200);
                map.put("msg", "获取数据成功！");
                map.put("deptId", sysDept.getDeptId());
                map.put("deptName", sysDept.getDeptName());
                map.put("status", sysDept.getStatus());
                map.put("time", sysDept.getCreateTime());
                EmpInfo empInfo = empInfoService.findById(sysDept.getManagerId());
                map.put("empName", empInfo.getEmpName());
                List<EmpInfo> empInfoList = empInfoService.findByDeptId(sysDept.getDeptId());
                map.put("empInfoList", empInfoList);
                lists.add(map);
            }
        }
        System.out.println(lists);
    }
    //测试部门名称模糊查询
    @Test
    public void test41() {
        int totalCount = sysDeptService.findCount("人");
        List<Map<String, Object>> lists = new ArrayList<>();
        List<SysDept> deptList = sysDeptService.findByDeptName("人",0,5);
        if (!CommonUtil.isEmpty(deptList) && deptList.size() > 0) {
            for (SysDept sysDept : deptList) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("code", 200);
                map.put("msg", "获取数据成功！");
                map.put("deptId", sysDept.getDeptId());
                map.put("deptName", sysDept.getDeptName());
                map.put("status", sysDept.getStatus());
                map.put("time", sysDept.getCreateTime());
                EmpInfo empInfo = empInfoService.findById(sysDept.getManagerId());
                map.put("empName", empInfo.getEmpName());
                List<EmpInfo> empInfoList = empInfoService.findByDeptId(sysDept.getDeptId());
                map.put("empInfoList", empInfoList);
                lists.add(map);
            }
        }
        System.out.println(lists);
    }

    //测试考勤记录展示
    @Test
    public void test50() {
        int totalCount=attendanceRecordService.findAllCount();
        List<Map<String,Object>> lists=new ArrayList<>();
        List<AttendanceRecord> records=attendanceRecordService.findAll(0,5);
        if(!CommonUtil.isEmpty(records) && records.size()>0) {
            for (AttendanceRecord record : records) {
                Map<String, Object> map = new HashMap<>();
                EmpInfo empInfo = empInfoService.findById(record.getEmpId());
                map.put("recordId", record.getRecordId());
                map.put("empNo", empInfo.getEmpNo());
                map.put("checkDate", record.getCheckDate());
                map.put("checkInTime", record.getCheckInTime());
                map.put("status", record.getStatus());
                map.put("remark", record.getRemark());
                lists.add(map);
            }
        }
        System.out.println(lists);
    }

    //测试考勤记录根据工号查询展示
    @Test
    public void test51() {
        EmpInfo empInfo1 = empInfoService.findByNo("Y2025001");
        int empId = empInfo1.getEmpId();
        int totalCount = attendanceRecordService.findCountByEmpId(empId);
        List<Map<String, Object>> lists = new ArrayList<>();
        List<AttendanceRecord> records = attendanceRecordService.findById(empId, 0, 3);
        if (!CommonUtil.isEmpty(records) && records.size() > 0) {
            for (AttendanceRecord record : records) {
                Map<String, Object> map = new HashMap<>();
                map.put("recordId", record.getRecordId());
                map.put("empNo", "Y2025001");
                map.put("checkDate", record.getCheckDate());
                map.put("checkInTime", record.getCheckInTime());
                map.put("status", record.getStatus());
                map.put("remark", record.getRemark());
                lists.add(map);
            }
            System.out.println(lists);
        }
    }

    //测试请假管理所有信息
    @Test
    public void test60() {
        int totalCount=leaveApplicationService.findAllCount();
        List<Map<String,Object>> lists=new ArrayList<Map<String,Object>>();
        List<LeaveApplication> leaveApplications=leaveApplicationService.findAll(0,5);
        if(!CommonUtil.isEmpty(leaveApplications)&&leaveApplications.size()>0) {
            for (LeaveApplication leaveApplication : leaveApplications) {
                Map<String, Object> map = new HashMap<String, Object>();
                EmpInfo empInfo = empInfoService.findById(leaveApplication.getEmpId());
                map.put("Id", leaveApplication.getLeaveId());
                map.put("empNo", empInfo.getEmpNo());
                map.put("time", leaveApplication.getCreateTime());
                map.put("leaveDays", leaveApplication.getLeaveDays());
                map.put("reason", leaveApplication.getReason());
                map.put("type", leaveApplication.getLeaveType());
                SysUser sysUser = sysUserService.findById(leaveApplication.getApproverId());
                SysUserRole sysUserRole = sysUserRoleService.findByUserId(leaveApplication.getApproverId());
                SysRole sysRole = sysRoleService.findById(sysUserRole.getRoleId());
                String approveName = sysRole.getRoleName() + " " + sysUser.getUsername();
                map.put("approveName", approveName);
                map.put("status", leaveApplication.getApproveStatus());
                lists.add(map);
            }
        }
        System.out.println(lists);
    }

    //测试请假管理的搜索功能
    @Test
    public void test61() {
        String type="type";
        String data="事假";
        int totalCount=0;
        List<LeaveApplication> leaveApplications=new ArrayList<>();
        //根据员工工号查询
        if("empNo".equals(type)){
            EmpInfo empInfo=empInfoService.findByNo(data);
            totalCount=leaveApplicationService.findCountByEmpId(empInfo.getEmpId());
            leaveApplications=leaveApplicationService.findByEmpId(empInfo.getEmpId(),0,5);
        }else if("type".equals(type)){
            //根据请假类型查询(事假/病假/年假)
            totalCount=leaveApplicationService.findCountByLeaveType(data);
            leaveApplications=leaveApplicationService.findByLeaveType(data,0,5);
        }else if("status".equals(type)){

            //根据审批状态查询(待审批/通过/驳回)
            if("待审批".equals(data)){
                data= String.valueOf(0);
            }else if("通过".equals(data)){
                data= String.valueOf(1);
            }else if ("驳回".equals(data)){
                data= String.valueOf(2);
            }
            totalCount=leaveApplicationService.findCountByStatus(Byte.valueOf(data));
            leaveApplications=leaveApplicationService.findByApproveStatus(Byte.valueOf(data),0,5);
        }
        List<Map<String,Object>> lists=new ArrayList<Map<String,Object>>();
        if(!CommonUtil.isEmpty(leaveApplications)&&leaveApplications.size()>0){
            for(LeaveApplication leaveApplication:leaveApplications){
                Map<String,Object> map=new HashMap<String,Object>();
                EmpInfo empInfo=empInfoService.findById(leaveApplication.getEmpId());
                map.put("Id",leaveApplication.getLeaveId());
                map.put("empNo",empInfo.getEmpNo());
                map.put("time",leaveApplication.getCreateTime());
                map.put("leaveDays",leaveApplication.getLeaveDays());
                map.put("reason",leaveApplication.getReason());
                map.put("type",leaveApplication.getLeaveType());
                SysUser sysUser=sysUserService.findById(leaveApplication.getApproverId());
                SysUserRole sysUserRole=sysUserRoleService.findByUserId(leaveApplication.getApproverId());
                SysRole sysRole=null;
                if(!CommonUtil.isEmpty(sysUser)&&!CommonUtil.isEmpty(sysUserRole)){
                    sysRole=sysRoleService.findById(sysUserRole.getRoleId());
                    String approveName=sysRole.getRoleName()+" "+sysUser.getUsername();
                    map.put("approveName",approveName);
                }else{
                    map.put("approveName","");
                }


                map.put("status",leaveApplication.getApproveStatus());
                lists.add(map);
            }
        }
        System.out.println(lists);
    }
    //测试部门管理的新增部门功能
    @Test
    public void test70() {
        String deptName="宣传部";
        SysRole sysRole=new SysRole();
        sysRole.setRoleName(deptName+"主管");
        sysRole.setRoleType((byte) 2);
        sysRole.setRoleDesc("管理"+deptName+"员工，审批请假/补卡");
        sysRole.setCreateTime(new java.util.Date());
        sysRole.setStatus((byte) 1);
        int result1=sysRoleService.insert(sysRole);
        System.out.println(result1);
    }

}
