package com.hkd.mapper;

import com.hkd.pojo.SupplementCard;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SupplementCardDao {
    // 查询所有补卡申请数量
    Integer findAllCount();
    
    // 查询所有补卡申请（分页）
    List<SupplementCard> findAll(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    
    // 根据ID查询补卡申请
    SupplementCard findById(Integer supplementId);
    
    // 根据员工ID查询补卡申请数量
    Integer findCountByEmpId(Integer empId);
    
    // 根据员工ID查询补卡申请（分页）
    List<SupplementCard> findByEmpId(@Param("empId") Integer empId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    
    // 根据审批状态查询数量
    Integer findCountByStatus(Byte approveStatus);
    
    // 根据审批状态查询补卡申请（分页）
    List<SupplementCard> findByApproveStatus(@Param("approveStatus") Byte approveStatus, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    
    // 根据补卡类型查询数量
    Integer findCountByCheckType(Byte checkType);
    
    // 根据补卡类型查询补卡申请（分页）
    List<SupplementCard> findByCheckType(@Param("checkType") Byte checkType, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    
    // 新增补卡申请
    int insert(SupplementCard supplementCard);
    
    // 更新补卡申请
    int update(SupplementCard supplementCard);
    
    // 删除补卡申请
    int delete(Integer supplementId);
    
    // 更新审批状态
    int updateApproveStatus(@Param("supplementId") Integer supplementId, @Param("approveStatus") Byte approveStatus, @Param("approveOpinion") String approveOpinion, @Param("approverId") Integer approverId);

    // 获取已审批通过的申请数量
    Integer getApprovedCount();

    // 获取已审批（通过+驳回）的总数量
    Integer getDecidedCount();
}