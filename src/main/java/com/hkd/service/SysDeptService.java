package com.hkd.service;

import com.hkd.pojo.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptService {
    // 查询所有部门
    List<SysDept> findAll(Integer offset, Integer pageSize);

    //返回所有部门的数量
    Integer findAllCount();

    // 根据部门名称模糊查询部门
    List<SysDept> findByDeptName(String deptName,Integer offset,
                                 Integer pageSize);

    //返回该名称部门的数量
    Integer findCount(String deptName);
    
    // 根据部门ID查询部门
    SysDept findById(Integer deptId);
    
    // 新增部门
    int insert(SysDept sysDept);
    
    // 更新部门
    int update(SysDept sysDept);
    
    // 删除部门
    int delete(Integer deptId);
    
    // 根据状态查询部门
    List<SysDept> findByStatus(Byte status);
    
    // 根据部门经理ID查询部门
    List<SysDept> findByManagerId(Integer managerId);
}