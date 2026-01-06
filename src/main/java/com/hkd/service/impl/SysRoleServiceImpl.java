package com.hkd.service.impl;

import com.hkd.mapper.SysRoleDao;
import com.hkd.pojo.SysRole;
import com.hkd.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("sysRoleService")
public class SysRoleServiceImpl implements SysRoleService {
    
    @Autowired
    private SysRoleDao sysRoleDao;
    
    @Override
    public List<SysRole> findAll() {
        return sysRoleDao.findAll();
    }
    
    @Override
    public SysRole findById(Integer roleId) {
        return sysRoleDao.findById(roleId);
    }
    
    @Override
    public int insert(SysRole sysRole) {
        return sysRoleDao.insert(sysRole);
    }
    
    @Override
    public int update(SysRole sysRole) {
        return sysRoleDao.update(sysRole);
    }
    
    @Override
    public int delete(Integer roleId) {
        return sysRoleDao.delete(roleId);
    }
    
    @Override
    public List<SysRole> findByRoleType(Byte roleType) {
        return sysRoleDao.findByRoleType(roleType);
    }
    
    @Override
    public List<SysRole> findByStatus(Byte status) {
        return sysRoleDao.findByStatus(status);
    }

    @Override
    public SysRole findByRoleName(String roleName) {
        return sysRoleDao.findByRoleName(roleName);
    }
}