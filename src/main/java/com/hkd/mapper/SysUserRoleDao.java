package com.hkd.mapper;

import com.hkd.pojo.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserRoleDao {
    // 根据用户ID查询角色关联
    SysUserRole findByUserId(Integer userId);
    
    // 根据角色ID查询用户关联
    List<SysUserRole> findByRoleId(Integer roleId);
    
    // 新增用户角色关联
    int insert(SysUserRole sysUserRole);
    
    // 删除用户角色关联
    int delete(Integer id);
    
    // 根据用户ID和角色ID删除关联
    int deleteByUserIdAndRoleId(Integer userId, Integer roleId);
    
    // 根据用户ID删除所有关联
    int deleteByUserId(Integer userId);
    
    // 根据角色ID删除所有关联
    int deleteByRoleId(Integer roleId);

    // 根据用户ID更新角色
    int updateRoleByUserId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
}