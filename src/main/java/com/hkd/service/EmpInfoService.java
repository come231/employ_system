package com.hkd.service;

import com.hkd.pojo.EmpInfo;

import java.util.List;

public interface EmpInfoService {
    //查询所有员工的数量
    Integer findAllCount();

    // 查询所有员工信息
    List<EmpInfo> findAll(Integer offset,Integer pageSize);

    //查询所有在职员工数量
    Integer findCount(Integer status);

    
    // 根据员工工号查询员工信息
    List<EmpInfo> findByEmpNo(String empNo,Integer offset,Integer pageSize);

    //根据工号模糊查询员工数量
    Integer findCountByEmpNo(String empNo);


    // 新增员工信息
    int insert(EmpInfo empInfo);
    
    // 更新员工信息
    int update(EmpInfo empInfo);
    
    // 删除员工信息
    int delete(Integer empId);
    //根据员工工号查询员工
    EmpInfo findByNo(String empNo);

    //根据员工ID查询员工
    EmpInfo findById(Integer empId);

    // 根据部门ID查询员工信息
    List<EmpInfo> findByDeptId(Integer deptId);
    
    // 根据状态查询员工信息
    List<EmpInfo> findByStatus(Byte status);
    
    // 根据员工姓名模糊查询员工信息
    List<EmpInfo> findByEmpNameLike(String empName);

    // 根据年份查询员工数量（用于工号生成）
    Integer findCountByYear(int year);

    // 根据电话模糊查询员工（分页）
    List<EmpInfo> findByPhoneLike(String phone, Integer offset, Integer pageSize);
    Integer findCountByPhoneLike(String phone);

    // 根据邮箱模糊查询员工（分页）
    List<EmpInfo> findByEmailLike(String email, Integer offset, Integer pageSize);
    Integer findCountByEmailLike(String email);

    // 根据状态查询员工（分页）
    List<EmpInfo> findByStatusPaged(Byte status, Integer offset, Integer pageSize);
    Integer findCountByStatus(Byte status);

    // 根据姓名模糊查询员工（分页）
    List<EmpInfo> findByEmpNameLikePaged(String empName, Integer offset, Integer pageSize);
    Integer findCountByEmpNameLike(String empName);
}