package com.hkd.service;

import com.hkd.pojo.SupplementCard;

import java.util.List;

public interface SupplementCardService {
    // 查询所有补卡申请数量
    Integer findAllCount();
    
    // 查询所有补卡申请（分页）
    List<SupplementCard> findAll(Integer offset, Integer pageSize);
    
    // 根据ID查询补卡申请
    SupplementCard findById(Integer supplementId);
    
    // 根据员工ID查询补卡申请数量
    Integer findCountByEmpId(Integer empId);
    
    // 根据员工ID查询补卡申请（分页）
    List<SupplementCard> findByEmpId(Integer empId, Integer offset, Integer pageSize);
    
    // 根据审批状态查询数量
    Integer findCountByStatus(Byte approveStatus);
    
    // 根据审批状态查询补卡申请（分页）
    List<SupplementCard> findByApproveStatus(Byte approveStatus, Integer offset, Integer pageSize);
    
    // 根据补卡类型查询数量
    Integer findCountByCheckType(Byte checkType);
    
    // 根据补卡类型查询补卡申请（分页）
    List<SupplementCard> findByCheckType(Byte checkType, Integer offset, Integer pageSize);
    
    // 新增补卡申请
    int insert(SupplementCard supplementCard);
    
    // 更新补卡申请
    int update(SupplementCard supplementCard);
    
    // 删除补卡申请
    int delete(Integer supplementId);
    
    // 更新审批状态
    int updateApproveStatus(Integer supplementId, Byte approveStatus, String approveOpinion, Integer approverId);
}