package com.hkd.service.impl;

import com.hkd.mapper.SysDeptDao;
import com.hkd.pojo.SysDept;
import com.hkd.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("sysDeptService")
public class SysDeptServiceImpl implements SysDeptService {
    
    @Autowired
    private SysDeptDao sysDeptDao;


    @Override
    public List<SysDept> findAll(Integer offset, Integer pageSize) {
        return sysDeptDao.findAll(offset, pageSize);
    }

    @Override
    public Integer findAllCount() {
        return sysDeptDao.findAllCount();
    }

    @Override
    public List<SysDept> findByDeptName(String deptName, Integer offset, Integer pageSize) {
        return sysDeptDao.findByDeptName(deptName, offset, pageSize);
    }

    @Override
    public Integer findCount(String deptName) {
        return sysDeptDao.findCount(deptName);
    }
    
    @Override
    public SysDept findById(Integer deptId) {
        return sysDeptDao.findById(deptId);
    }

    @Override
    public int insert(SysDept sysDept) {
        return sysDeptDao.insert(sysDept);
    }
    
    @Override
    public int update(SysDept sysDept) {
        return sysDeptDao.update(sysDept);
    }
    
    @Override
    public int delete(Integer deptId) {
        return sysDeptDao.delete(deptId);
    }
    
    @Override
    public List<SysDept> findByStatus(Byte status) {
        return sysDeptDao.findByStatus(status);
    }
    
    @Override
    public List<SysDept> findByManagerId(Integer managerId) {
        return sysDeptDao.findByManagerId(managerId);
    }
}