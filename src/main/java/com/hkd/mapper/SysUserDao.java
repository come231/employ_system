package com.hkd.mapper;

import com.hkd.pojo.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SysUserDao {
    // 查询所有用户
    List<SysUser> findAll();
    
    // 根据ID查询用户
    SysUser findById(Integer userId);
    
    // 根据用户名查询用户
    SysUser findByUsername(String username);
    
    // 根据员工ID查询用户
    SysUser findByEmpId(Integer empId);
    
    // 新增用户
    int insert(SysUser sysUser);
    
    // 更新用户
    int update(SysUser sysUser);
    
    // 删除用户
    int delete(Integer userId);
    
    // 根据状态查询用户
    List<SysUser> findByStatus(Byte status);
    
    // 更新用户最后登录时间
    int updateLastLoginTime(@Param("userId") Integer userId,@Param("lastLoginTime")Date lastLoginTime);
}