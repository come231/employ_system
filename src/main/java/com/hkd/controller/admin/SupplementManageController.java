package com.hkd.controller.admin;

import com.hkd.pojo.*;
import com.hkd.service.*;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.OperationLogUtil;
import com.hkd.util.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/27
 * @description 补卡管理Controller
 */
@Controller
@RequestMapping("/admin/supplement")
public class SupplementManageController {
    @Autowired
    private SupplementCardService supplementCardService;
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

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    @ResponseBody
    public String findAllSupplementCard(HttpServletRequest request) {
        try {
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset, pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount = supplementCardService.findAllCount();
            List<Map<String, Object>> lists = new ArrayList<>();
            List<SupplementCard> supplementCards = supplementCardService.findAll(offset, pageSize);
            if (!CommonUtil.isEmpty(supplementCards) && supplementCards.size() > 0) {
                for (SupplementCard supplementCard : supplementCards) {
                    Map<String, Object> map = new HashMap<>();
                    EmpInfo empInfo = empInfoService.findById(supplementCard.getEmpId());
                    map.put("Id", supplementCard.getSupplementId());
                    map.put("empNo", empInfo.getEmpNo());
                    map.put("checkDate", supplementCard.getCheckDate());
                    map.put("checkType", supplementCard.getCheckType());
                    map.put("actualTime", supplementCard.getActualTime());
                    map.put("reason", supplementCard.getReason());
                    map.put("time", supplementCard.getCreateTime());
                    
                    SysUser sysUser = sysUserService.findById(supplementCard.getApproverId());
                    SysUserRole sysUserRole = sysUserRoleService.findByUserId(supplementCard.getApproverId());
                    SysRole sysRole = null;
                    if (!CommonUtil.isEmpty(sysUser) && !CommonUtil.isEmpty(sysUserRole)) {
                        sysRole = sysRoleService.findById(sysUserRole.getRoleId());
                        String approveName = sysRole.getRoleName() + " " + sysUser.getUsername();
                        map.put("approveName", approveName);
                    } else {
                        map.put("approveName", "");
                    }
                    map.put("opinion", supplementCard.getApproveOpinion());
                    map.put("status", supplementCard.getApproveStatus());
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

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public String findSearchSupplementCard(@RequestParam("type") String type,
                                           HttpServletRequest request) {
        try {
            String data = request.getParameter("data");
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset, pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount = 0;
            List<SupplementCard> supplementCards = new ArrayList<>();
            
            // 根据员工工号查询
            if ("empNo".equals(type)) {
                EmpInfo empInfo = empInfoService.findByNo(data);
                if (!CommonUtil.isEmpty(empInfo)) {
                    totalCount = supplementCardService.findCountByEmpId(empInfo.getEmpId());
                    supplementCards = supplementCardService.findByEmpId(empInfo.getEmpId(), offset, pageSize);
                }
            } else if ("checkType".equals(type)) {
                // 根据补卡类型查询(上班/下班/上下班)
                Byte checkTypeValue = 0;
                if ("上班".equals(data)) {
                    checkTypeValue = 1;
                } else if ("下班".equals(data)) {
                    checkTypeValue = 2;
                } else if ("上下班".equals(data)) {
                    checkTypeValue = 3;
                }
                totalCount = supplementCardService.findCountByCheckType(checkTypeValue);
                supplementCards = supplementCardService.findByCheckType(checkTypeValue, offset, pageSize);
            } else if ("status".equals(type)) {
                // 根据审批状态查询(待审批/通过/驳回)
                if ("待审批".equals(data)) {
                    data = String.valueOf(0);
                } else if ("通过".equals(data)) {
                    data = String.valueOf(1);
                } else if ("驳回".equals(data)) {
                    data = String.valueOf(2);
                }
                totalCount = supplementCardService.findCountByStatus(Byte.valueOf(data));
                supplementCards = supplementCardService.findByApproveStatus(Byte.valueOf(data), offset, pageSize);
            }
            
            List<Map<String, Object>> lists = new ArrayList<>();
            if (!CommonUtil.isEmpty(supplementCards) && supplementCards.size() > 0) {
                for (SupplementCard supplementCard : supplementCards) {
                    Map<String, Object> map = new HashMap<>();
                    EmpInfo empInfo = empInfoService.findById(supplementCard.getEmpId());
                    map.put("Id", supplementCard.getSupplementId());
                    map.put("empNo", empInfo.getEmpNo());
                    map.put("checkDate", supplementCard.getCheckDate());
                    map.put("checkType", supplementCard.getCheckType());
                    map.put("actualTime", supplementCard.getActualTime());
                    map.put("reason", supplementCard.getReason());
                    map.put("time", supplementCard.getCreateTime());
                    
                    SysUser sysUser = sysUserService.findById(supplementCard.getApproverId());
                    SysUserRole sysUserRole = sysUserRoleService.findByUserId(supplementCard.getApproverId());
                    SysRole sysRole = null;
                    if (!CommonUtil.isEmpty(sysUser) && !CommonUtil.isEmpty(sysUserRole)) {
                        sysRole = sysRoleService.findById(sysUserRole.getRoleId());
                        String approveName = sysRole.getRoleName() + " " + sysUser.getUsername();
                        map.put("approveName", approveName);
                    } else {
                        map.put("approveName", "");
                    }
                    map.put("opinion", supplementCard.getApproveOpinion());
                    map.put("status", supplementCard.getApproveStatus());
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

    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    @ResponseBody
    public String approveSupplementCard(HttpServletRequest request, HttpSession session) {
        try {
            Integer supplementId = Integer.parseInt(request.getParameter("supplementId"));
            Byte approveStatus = Byte.parseByte(request.getParameter("approveStatus"));
            String approveOpinion = request.getParameter("approveOpinion");
            Integer approverId = Integer.parseInt(request.getParameter("approverId"));

            // 获取补卡申请信息
            SupplementCard supplementCard = supplementCardService.findById(supplementId);
            if (supplementCard == null) {
                return JsonUtil.buildFalseJson(404, "补卡申请不存在");
            }

            int result = supplementCardService.updateApproveStatus(supplementId, approveStatus, approveOpinion, approverId);
            if (result > 0) {
                String statusText = approveStatus == 1 ? "通过" : "驳回";
                OperationLogUtil.log(session, sysOperationLogService, "补卡管理", "审批",
                    "审批补卡申请ID:" + supplementId + ",结果:" + statusText);

                // 发送消息通知给申请人
                SysUser empUser = sysUserService.findByEmpId(supplementCard.getEmpId());
                if (empUser != null) {
                    String notificationContent = "您的补卡申请已被" + statusText + "。";
                    if (approveOpinion != null && !approveOpinion.trim().isEmpty()) {
                        notificationContent += "审批意见：" + approveOpinion;
                    }
                    sysNotificationService.sendNotification(
                        empUser.getUserId(),
                        "supplement_approve",
                        "补卡审批结果通知",
                        notificationContent,
                        supplementId
                    );
                }

                return JsonUtil.objectToJson(null, "审批成功");
            }
            OperationLogUtil.logError(session, sysOperationLogService, "补卡管理", "审批",
                "审批补卡申请ID:" + supplementId, "更新数据库失败");
            return JsonUtil.buildFalseJson(500, "审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }
}
