package com.hkd.service.impl;

import com.hkd.mapper.LeaveApplicationDao;
import com.hkd.pojo.LeaveApplication;
import com.hkd.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service("leaveApplicationService")
public class LeaveApplicationServiceImpl implements LeaveApplicationService {
    
    @Autowired
    private LeaveApplicationDao leaveApplicationDao;
    
    @Override
    public List<LeaveApplication> findAll(int offset, int pageSize) {
        return leaveApplicationDao.findAll(offset,pageSize);
    }

    @Override
    public Integer findAllCount() {
        return leaveApplicationDao.findAllCount();
    }

    @Override
    public LeaveApplication findById(Integer leaveId) {
        return leaveApplicationDao.findById(leaveId);
    }
    
    @Override
    public List<LeaveApplication> findByEmpId(Integer empId,int offset,int pageSize) {
        return leaveApplicationDao.findByEmpId(empId,offset,pageSize);
    }

    @Override
    public Integer findCountByEmpId(Integer empId) {
        return leaveApplicationDao.findCountByEmpId(empId);
    }

    @Override
    public List<LeaveApplication> findByApproverId(Integer approverId) {
        return leaveApplicationDao.findByApproverId(approverId);
    }
    
    @Override
    public List<LeaveApplication> findByApproveStatus(Byte approveStatus,int offset,int pageSize) {
        return leaveApplicationDao.findByApproveStatus(approveStatus,offset,pageSize);
    }

    @Override
    public Integer findCountByStatus(Byte status) {
        return leaveApplicationDao.findCountByStatus(status);
    }

    @Override
    public List<LeaveApplication> findByLeaveType(String leaveType,int offset,int pageSize) {
        return leaveApplicationDao.findByLeaveType(leaveType,offset,pageSize);
    }

    @Override
    public Integer findCountByLeaveType(String leaveType) {
        return leaveApplicationDao.findCountByLeaveType(leaveType);
    }

    @Override
    public List<LeaveApplication> findByDateRange(Integer empId, LocalDateTime startDate,LocalDateTime endDate, Integer status, String type) {
        return leaveApplicationDao.findByDateRange(empId,startDate, endDate,status,type);
    }
    
    @Override
    public int insert(LeaveApplication leaveApplication) {
        return leaveApplicationDao.insert(leaveApplication);
    }
    
    @Override
    public int update(LeaveApplication leaveApplication) {
        return leaveApplicationDao.update(leaveApplication);
    }
    
    @Override
    public int delete(Integer leaveId) {
        return leaveApplicationDao.delete(leaveId);
    }
    
    @Override
    public int updateApproveStatus(Integer leaveId, Byte approveStatus, String approveRemark) {
        return leaveApplicationDao.updateApproveStatus(leaveId, approveStatus, approveRemark);
    }
}