package com.hkd.service.impl;

import com.hkd.mapper.EmpInfoDao;
import com.hkd.pojo.EmpInfo;
import com.hkd.service.EmpInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("empInfoService")
public class EmpInfoServiceImpl implements EmpInfoService {
    
    @Autowired
    private EmpInfoDao empInfoDao;
    
    @Override
    public List<EmpInfo> findAll(Integer offset,Integer pageSize) {
        return empInfoDao.findAll(offset,pageSize);
    }

    @Override
    public Integer findCount(Integer status) {
        return empInfoDao.findCount(status);
    }

    @Override
    public Integer findAllCount() {
        return empInfoDao.findAllCount();
    }

    @Override
    public List<EmpInfo> findByEmpNo(String empNo,Integer offset,Integer pageSize) {
        return empInfoDao.findByEmpNo(empNo,offset,pageSize);
    }

    @Override
    public Integer findCountByEmpNo(String empNo) {
        return empInfoDao.findCountByEmpNo(empNo);
    }

    @Override
    public int insert(EmpInfo empInfo) {
        return empInfoDao.insert(empInfo);
    }
    
    @Override
    public int update(EmpInfo empInfo) {
        return empInfoDao.update(empInfo);
    }
    
    @Override
    public int delete(Integer empId) {
        return empInfoDao.delete(empId);
    }

    @Override
    public EmpInfo findByNo(String empNo) {
        return empInfoDao.findByNo(empNo);
    }

    @Override
    public EmpInfo findById(Integer empId) {
        return empInfoDao.findById(empId);
    }

    @Override
    public List<EmpInfo> findByDeptId(Integer deptId) {
        return empInfoDao.findByDeptId(deptId);
    }
    
    @Override
    public List<EmpInfo> findByStatus(Byte status) {
        return empInfoDao.findByStatus(status);
    }
    
    @Override
    public List<EmpInfo> findByEmpNameLike(String empName) {
        return empInfoDao.findByEmpNameLike(empName);
    }

    @Override
    public Integer findCountByYear(int year) {
        return empInfoDao.findCountByYear(year);
    }

    @Override
    public List<EmpInfo> findByPhoneLike(String phone, Integer offset, Integer pageSize) {
        return empInfoDao.findByPhoneLike(phone, offset, pageSize);
    }

    @Override
    public Integer findCountByPhoneLike(String phone) {
        return empInfoDao.findCountByPhoneLike(phone);
    }

    @Override
    public List<EmpInfo> findByEmailLike(String email, Integer offset, Integer pageSize) {
        return empInfoDao.findByEmailLike(email, offset, pageSize);
    }

    @Override
    public Integer findCountByEmailLike(String email) {
        return empInfoDao.findCountByEmailLike(email);
    }

    @Override
    public List<EmpInfo> findByStatusPaged(Byte status, Integer offset, Integer pageSize) {
        return empInfoDao.findByStatusPaged(status, offset, pageSize);
    }

    @Override
    public Integer findCountByStatus(Byte status) {
        return empInfoDao.findCountByStatus(status);
    }

    @Override
    public List<EmpInfo> findByEmpNameLikePaged(String empName, Integer offset, Integer pageSize) {
        return empInfoDao.findByEmpNameLikePaged(empName, offset, pageSize);
    }

    @Override
    public Integer findCountByEmpNameLike(String empName) {
        return empInfoDao.findCountByEmpNameLike(empName);
    }
}