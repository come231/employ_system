package com.hkd.controller.admin;

import com.hkd.pojo.*;
import com.hkd.service.*;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.OperationLogUtil;
import com.hkd.util.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 18:20
 * @description TODO: 请假信息管理
 */
@Controller
@RequestMapping("/admin/leave")
public class LeaveManageController {
    @Autowired
    private LeaveApplicationService leaveApplicationService;
    @Autowired
    private EmpInfoService empInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysOperationLogService sysOperationLogService;
    @Autowired
    private SysNotificationService sysNotificationService;

    @RequestMapping(value = "/data",method = RequestMethod.GET)
    @ResponseBody
    public String findAllLeaveApplication(HttpServletRequest request){
        try{
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset,pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount=leaveApplicationService.findAllCount();
            List<Map<String,Object>> lists=new ArrayList<Map<String,Object>>();
            List<LeaveApplication> leaveApplications=leaveApplicationService.findAll(offset,pageSize);
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
                    map.put("opinion",leaveApplication.getApproveOpinion());
                    map.put("status",leaveApplication.getApproveStatus());
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

    @RequestMapping(value = "/search",method = RequestMethod.GET)
    @ResponseBody
    public String findSearchLeaveApplication(@RequestParam("type") String type,
                                             HttpServletRequest request){
        try{
            String data=request.getParameter("data");
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset,pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount=0;
            List<LeaveApplication> leaveApplications=new ArrayList<>();
            //根据员工工号查询
            if("empNo".equals(type)){
                EmpInfo empInfo=empInfoService.findByNo(data);
                totalCount=leaveApplicationService.findCountByEmpId(empInfo.getEmpId());
                leaveApplications=leaveApplicationService.findByEmpId(empInfo.getEmpId(),offset,pageSize);
            }else if("type".equals(type)){
                //根据请假类型查询(事假/病假/年假)
                totalCount=leaveApplicationService.findCountByLeaveType(data);
                leaveApplications=leaveApplicationService.findByLeaveType(data,offset,pageSize);
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
                leaveApplications=leaveApplicationService.findByApproveStatus(Byte.valueOf(data),offset,pageSize);
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
                    map.put("opinion",leaveApplication.getApproveOpinion());
                    map.put("status",leaveApplication.getApproveStatus());
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

    @RequestMapping(value = "/approve",method = RequestMethod.POST)
    @ResponseBody
    public String approveLeaveApplication(HttpServletRequest request, HttpSession session){
        try{
            Integer leaveId = Integer.parseInt(request.getParameter("leaveId"));
            Byte approveStatus = Byte.parseByte(request.getParameter("approveStatus"));
            String approveOpinion = request.getParameter("approveOpinion");
            Integer approverId = Integer.parseInt(request.getParameter("approverId"));
            
            LeaveApplication leaveApplication = leaveApplicationService.findById(leaveId);
            if(CommonUtil.isEmpty(leaveApplication)){
                OperationLogUtil.logError(session, sysOperationLogService, "请假管理", "审批", 
                    "审批请假申请ID:" + leaveId, "请假申请不存在");
                return JsonUtil.buildFalseJson(404,"请假申请不存在");
            }
            leaveApplication.setApproveStatus(approveStatus);
            leaveApplication.setApproveOpinion(approveOpinion);
            leaveApplication.setApproverId(approverId);
            int result = leaveApplicationService.update(leaveApplication);
            if(result > 0){
                String statusText = approveStatus == 1 ? "通过" : "驳回";
                OperationLogUtil.log(session, sysOperationLogService, "请假管理", "审批",
                    "审批请假申请ID:" + leaveId + ",结果:" + statusText);

                // 发送消息通知给申请人
                SysUser empUser = sysUserService.findByEmpId(leaveApplication.getEmpId());
                if (empUser != null) {
                    String notificationContent = "您的请假申请已被" + statusText + "。";
                    if (approveOpinion != null && !approveOpinion.trim().isEmpty()) {
                        notificationContent += "审批意见：" + approveOpinion;
                    }
                    sysNotificationService.sendNotification(
                        empUser.getUserId(),
                        "leave_approve",
                        "请假审批结果通知",
                        notificationContent,
                        leaveId
                    );
                }

                return JsonUtil.objectToJson(null,"审批成功");
            }
            OperationLogUtil.logError(session, sysOperationLogService, "请假管理", "审批", 
                "审批请假申请ID:" + leaveId, "更新数据库失败");
            return JsonUtil.buildFalseJson(500,"审批失败");
        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }

    /**
     * 根据时间范围查询请假申请
     */
    @RequestMapping(value = "/searchByDate",method = RequestMethod.GET)
    @ResponseBody
    public String searchByDateRange(@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                                    @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                                    HttpServletRequest request){
        try{
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset,pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            
            // 获取时间范围内的请假申请
            List<LeaveApplication> allLeaves = leaveApplicationService.findByDateRange(-1, startDate, endDate, -1, "");
            int totalCount = allLeaves.size();
            
            // 手动分页
            int fromIndex = Math.min(offset, allLeaves.size());
            int toIndex = Math.min(offset + pageSize, allLeaves.size());
            List<LeaveApplication> leaveApplications = allLeaves.subList(fromIndex, toIndex);
            
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
                    map.put("opinion",leaveApplication.getApproveOpinion());
                    map.put("status",leaveApplication.getApproveStatus());
                    lists.add(map);
                }
                return JsonUtil.buildJsonByTotalCount(lists,totalCount);
            }
            return JsonUtil.buildFalseJson(404,"该时间范围内无请假记录");
        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,e.getMessage());
        }
    }
}
