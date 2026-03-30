package com.hkd.service.impl;

import com.hkd.mapper.AttendanceRecordDao;
import com.hkd.pojo.AttendanceRecord;
import com.hkd.service.AttendanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Calendar;
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

    // ===================== 核心：获取本月考勤 =====================
    @Override
    public List<AttendanceRecord> getMonthAttendance(Integer empId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime(); // 月初

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime(); // 月末

        return attendanceRecordDao.findMonthRecordsByEmpId(empId, startDate, endDate);
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