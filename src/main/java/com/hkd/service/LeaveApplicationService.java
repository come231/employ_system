package com.hkd.service;

import com.hkd.pojo.LeaveApplication;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface LeaveApplicationService {
    // 查询所有请假申请
    List<LeaveApplication> findAll(int offset,int pageSize);
    //获取所有请假的数量
    Integer findAllCount();
    
    // 根据ID查询请假申请
    LeaveApplication findById(Integer leaveId);
    
    // 根据员工ID查询请假申请
    List<LeaveApplication> findByEmpId(Integer empId,int offset,int pageSize);

    //获取数量
    Integer findCountByEmpId(Integer empId);
    
    // 根据审批人ID查询请假申请
    List<LeaveApplication> findByApproverId(Integer approverId);
    
    // 根据审批状态查询请假申请
    List<LeaveApplication> findByApproveStatus(Byte approveStatus,int offset,int pageSize);
    //获取数量
    Integer findCountByStatus(Byte status);
    
    // 根据请假类型查询请假申请
    List<LeaveApplication> findByLeaveType(String leaveType,int offset,int pageSize);
    //获取数量
    Integer findCountByLeaveType(String leaveType);
    
    // 根据日期范围查询请假申请
    List<LeaveApplication> findByDateRange(Integer empId, LocalDateTime startDate, LocalDateTime endDate,
                                           Integer status, String type);
    
    // 新增请假申请
    int insert(LeaveApplication leaveApplication);
    
    // 更新请假申请
    int update(LeaveApplication leaveApplication);
    
    // 删除请假申请
    int delete(Integer leaveId);
    
    // 更新审批状态
    int updateApproveStatus(Integer leaveId, Byte approveStatus, String approveRemark);
}
