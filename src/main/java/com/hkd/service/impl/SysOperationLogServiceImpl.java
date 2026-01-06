package com.hkd.service.impl;

import com.hkd.mapper.SysOperationLogDao;
import com.hkd.pojo.SysOperationLog;
import com.hkd.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("sysOperationLogService")
public class SysOperationLogServiceImpl implements SysOperationLogService {

    @Autowired
    private SysOperationLogDao sysOperationLogDao;

    @Override
    public Integer findAllCount() {
        return sysOperationLogDao.findAllCount();
    }

    @Override
    public List<SysOperationLog> findAll(Integer offset, Integer pageSize) {
        return sysOperationLogDao.findAll(offset, pageSize);
    }

    @Override
    public SysOperationLog findById(Long logId) {
        return sysOperationLogDao.findById(logId);
    }

    @Override
    public Integer findCountByUsername(String username) {
        return sysOperationLogDao.findCountByUsername(username);
    }

    @Override
    public List<SysOperationLog> findByUsername(String username, Integer offset, Integer pageSize) {
        return sysOperationLogDao.findByUsername(username, offset, pageSize);
    }

    @Override
    public Integer findCountByModule(String module) {
        return sysOperationLogDao.findCountByModule(module);
    }

    @Override
    public List<SysOperationLog> findByModule(String module, Integer offset, Integer pageSize) {
        return sysOperationLogDao.findByModule(module, offset, pageSize);
    }

    @Override
    public Integer findCountByOperationType(String operationType) {
        return sysOperationLogDao.findCountByOperationType(operationType);
    }

    @Override
    public List<SysOperationLog> findByOperationType(String operationType, Integer offset, Integer pageSize) {
        return sysOperationLogDao.findByOperationType(operationType, offset, pageSize);
    }

    @Override
    public Integer findCountByDateRange(Date startDate, Date endDate) {
        return sysOperationLogDao.findCountByDateRange(startDate, endDate);
    }

    @Override
    public List<SysOperationLog> findByDateRange(Date startDate, Date endDate, Integer offset, Integer pageSize) {
        return sysOperationLogDao.findByDateRange(startDate, endDate, offset, pageSize);
    }

    @Override
    public int insert(SysOperationLog sysOperationLog) {
        return sysOperationLogDao.insert(sysOperationLog);
    }
}
