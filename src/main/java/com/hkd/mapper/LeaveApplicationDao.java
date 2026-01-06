package com.hkd.mapper;

import com.hkd.pojo.LeaveApplication;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface LeaveApplicationDao {
    // 查询所有请假申请
    List<LeaveApplication> findAll(@Param("offset") int offset,@Param("pageSize") int pageSize);

    //获取所有请假的数量
    Integer findAllCount();
    
    // 根据ID查询请假申请
    LeaveApplication findById(Integer leaveId);

    
    // 根据员工ID查询请假申请
    List<LeaveApplication> findByEmpId(@Param("empId") Integer empId,@Param("offset") int offset,
                                       @Param("pageSize") int pageSize);

    //获取数量
    Integer findCountByEmpId(Integer empId);
    
    // 根据审批人ID查询请假申请
    List<LeaveApplication> findByApproverId(Integer approverId);
    
    // 根据审批状态查询请假申请
    List<LeaveApplication> findByApproveStatus(@Param("approveStatus") Byte approveStatus,@Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    //获取数量
    Integer findCountByStatus(Byte status);
    
    // 根据请假类型查询请假申请
    List<LeaveApplication> findByLeaveType(@Param("leaveType") String leaveType,@Param("offset") int offset,
                                           @Param("pageSize") int pageSize);

    //获取数量
    Integer findCountByLeaveType(String leaveType);
    
    // 根据日期范围查询请假申请
    List<LeaveApplication> findByDateRange(@Param("empId") Integer empId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate,
                                           @Param("status") Integer status, @Param("type") String type);
    
    // 新增请假申请
    int insert(LeaveApplication leaveApplication);
    
    // 更新请假申请
    int update(LeaveApplication leaveApplication);
    
    // 删除请假申请
    int delete(Integer leaveId);
    
    // 更新审批状态
    int updateApproveStatus(Integer leaveId, Byte approveStatus, String approveOpinion);

    // 获取已审批通过的申请数量
    Integer getApprovedCount();

    // 获取已审批（通过+驳回）的总数量
    Integer getDecidedCount();

    // 获取平均审批时长（秒）
    Double getAverageApprovalTimeInSeconds();
}