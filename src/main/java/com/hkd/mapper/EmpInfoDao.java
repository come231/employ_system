package com.hkd.mapper;

import com.hkd.pojo.EmpInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmpInfoDao {
    //查询所有的员工数量
    Integer findAllCount();

    // 查询所有员工
    List<EmpInfo> findAll(@Param("offset") Integer offset,@Param("pageSize") Integer pageSize);
    //

    //查询所有在职员工数量
    Integer findCount(Integer status);
    
    // 根据工号模糊查询员工
    List<EmpInfo> findByEmpNo(@Param("empNo") String empNo,@Param("offset") Integer offset,
                        @Param("pageSize") Integer pageSize);
   //根据工号模糊查询员工数量
    Integer findCountByEmpNo(String empNo);

    // 新增员工
    int insert(EmpInfo empInfo);
    
    // 更新员工
    int update(EmpInfo empInfo);
    
    // 删除员工
    int delete(Integer empId);

    //根据员工工号查询员工
    EmpInfo findByNo(String empNo);

    //根据员工ID查询员工
    EmpInfo findById(Integer empId);

    // 根据部门ID查询员工
    List<EmpInfo> findByDeptId(Integer deptId);
    
    // 根据状态查询员工
    List<EmpInfo> findByStatus(Byte status);
    
    // 根据姓名模糊查询员工
    List<EmpInfo> findByEmpNameLike(String empName);

    // 根据年份查询员工数量（用于工号生成）
    Integer findCountByYear(@Param("year") int year);

    // 根据电话模糊查询员工（分页）
    List<EmpInfo> findByPhoneLike(@Param("phone") String phone, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    Integer findCountByPhoneLike(@Param("phone") String phone);

    // 根据邮箱模糊查询员工（分页）
    List<EmpInfo> findByEmailLike(@Param("email") String email, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    Integer findCountByEmailLike(@Param("email") String email);

    // 根据状态查询员工（分页）
    List<EmpInfo> findByStatusPaged(@Param("status") Byte status, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    Integer findCountByStatus(@Param("status") Byte status);

    // 根据姓名模糊查询员工（分页）
    List<EmpInfo> findByEmpNameLikePaged(@Param("empName") String empName, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    Integer findCountByEmpNameLike(@Param("empName") String empName);
}