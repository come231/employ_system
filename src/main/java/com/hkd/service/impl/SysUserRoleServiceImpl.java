package com.hkd.service.impl;

import com.hkd.mapper.SysUserRoleDao;
import com.hkd.pojo.SysUserRole;
import com.hkd.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("sysUserRoleService")
public class SysUserRoleServiceImpl implements SysUserRoleService {
    
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    
    @Override
    public SysUserRole findByUserId(Integer userId) {
        return sysUserRoleDao.findByUserId(userId);
    }
    
    @Override
    public List<SysUserRole> findByRoleId(Integer roleId) {
        return sysUserRoleDao.findByRoleId(roleId);
    }
    
    @Override
    public int insert(SysUserRole sysUserRole) {
        return sysUserRoleDao.insert(sysUserRole);
    }
    
    @Override
    public int delete(Integer id) {
        return sysUserRoleDao.delete(id);
    }
    
    @Override
    public int deleteByUserIdAndRoleId(Integer userId, Integer roleId) {
        return sysUserRoleDao.deleteByUserIdAndRoleId(userId, roleId);
    }
    
    @Override
    public int deleteByUserId(Integer userId) {
        return sysUserRoleDao.deleteByUserId(userId);
    }
    
    @Override
    public int deleteByRoleId(Integer roleId) {
        return sysUserRoleDao.deleteByRoleId(roleId);
    }
    
    @Override
    public int batchInsert(List<SysUserRole> userRoleList) {
        int count = 0;
        for (SysUserRole userRole : userRoleList) {
            count += sysUserRoleDao.insert(userRole);
        }
        return count;
    }
    
    @Override
    public int batchDelete(List<Integer> idList) {
        int count = 0;
        for (Integer id : idList) {
            count += sysUserRoleDao.delete(id);
        }
        return count;
    }

    @Override
    public int updateRoleByUserId(Integer userId, Integer roleId) {
        return sysUserRoleDao.updateRoleByUserId(userId, roleId);
    }
}