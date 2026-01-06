package com.hkd.service.impl;

import com.hkd.mapper.AttendanceRecordDao;
import com.hkd.pojo.AttendanceRecord;
import com.hkd.service.AttendanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service("attendanceRecordService")
public class AttendanceRecordServiceImpl implements AttendanceRecordService {
    
    @Autowired
    private AttendanceRecordDao attendanceRecordDao;
    
    @Override
    public List<AttendanceRecord> findAll(int offset,int pageSize) {
        return attendanceRecordDao.findAll(offset,pageSize);
    }

    @Override
    public Integer findAllCount() {
        return attendanceRecordDao.findAllCount();
    }

    @Override
    public List<AttendanceRecord> findById(Integer empId,int offset,int pageSize) {
        return attendanceRecordDao.findById(empId,offset,pageSize);
    }

    @Override
    public Integer findCountByEmpId(Integer empId) {
        return attendanceRecordDao.findCountByEmpId(empId);
    }


    @Override
    public List<AttendanceRecord> findByEmpIdAndDateRange(Integer empId, LocalDate startDate, LocalDate endDate, Integer status, Integer type) {
        return attendanceRecordDao.findByEmpIdAndDateRange(empId, startDate, endDate,status,type);
    }

    @Override
    public List<AttendanceRecord> findByCheckDate(Date checkDate) {
        return attendanceRecordDao.findByCheckDate(checkDate);
    }
    
    @Override
    public List<AttendanceRecord> findByStatus(Byte status) {
        return attendanceRecordDao.findByStatus(status);
    }
    
    @Override
    public int insert(AttendanceRecord attendanceRecord) {
        return attendanceRecordDao.insert(attendanceRecord);
    }
    
    @Override
    public int update(AttendanceRecord attendanceRecord) {
        return attendanceRecordDao.update(attendanceRecord);
    }
    
    @Override
    public int delete(Integer recordId) {
        return attendanceRecordDao.delete(recordId);
    }
    
    @Override
    public AttendanceRecord findByEmpIdAndCheckDate(Integer empId, Date checkDate) {
        return attendanceRecordDao.findByEmpIdAndCheckDate(empId, checkDate);
    }

    @Override
    public List<AttendanceRecord> findByDateRange(String startDate, String endDate, int offset, int pageSize) {
        return attendanceRecordDao.findByDateRange(startDate, endDate, offset, pageSize);
    }

    @Override
    public Integer findCountByDateRange(String startDate, String endDate) {
        return attendanceRecordDao.findCountByDateRange(startDate, endDate);
    }
}