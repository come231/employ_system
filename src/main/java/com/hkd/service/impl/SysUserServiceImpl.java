package com.hkd.service.impl;

import com.hkd.mapper.SysUserDao;
import com.hkd.pojo.SysUser;
import com.hkd.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("sysUserService")
public class SysUserServiceImpl implements SysUserService {
    
    @Autowired
    private SysUserDao sysUserDao;
    
    @Override
    public List<SysUser> findAll() {
        return sysUserDao.findAll();
    }
    
    @Override
    public SysUser findById(Integer userId) {
        return sysUserDao.findById(userId);
    }
    
    @Override
    public SysUser findByUsername(String username) {
        return sysUserDao.findByUsername(username);
    }
    
    @Override
    public SysUser findByEmpId(Integer empId) {
        return sysUserDao.findByEmpId(empId);
    }
    
    @Override
    public int insert(SysUser sysUser) {
        return sysUserDao.insert(sysUser);
    }
    
    @Override
    public int update(SysUser sysUser) {
        return sysUserDao.update(sysUser);
    }
    
    @Override
    public int delete(Integer userId) {
        return sysUserDao.delete(userId);
    }
    
    @Override
    public List<SysUser> findByStatus(Byte status) {
        return sysUserDao.findByStatus(status);
    }
    
    @Override
    public int updateLastLoginTime(Integer userId, Date lastLoginTime) {
        return sysUserDao.updateLastLoginTime(userId, lastLoginTime);
    }
    
    @Override
    public SysUser login(String username, String password) {
        SysUser user = sysUserDao.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
    @Override
    public int changePassword(Integer userId, String oldPassword, String newPassword) {
        SysUser user = sysUserDao.findById(userId);
        if (user != null && user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            return sysUserDao.update(user);
        }
        return 0;
    }
}