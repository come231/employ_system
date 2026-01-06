package com.hkd.service;

import com.hkd.pojo.AttendanceRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface AttendanceRecordService {
    // 查询所有考勤记录
    List<AttendanceRecord> findAll(int offset,int pageSize);

    //获取所有考勤记录的数量
    Integer findAllCount();

    // 根据员工工号模糊查询考勤记录
    List<AttendanceRecord> findById(Integer empId,int offset,int pageSize);

    //获取所有员工工号模糊查询考勤记录的数量
    Integer findCountByEmpId(Integer empId);

    // 根据员工ID和日期范围查询考勤记录
    List<AttendanceRecord> findByEmpIdAndDateRange(Integer empId, LocalDate startDate,
                                                   LocalDate endDate,Integer status,Integer type);

    // 根据日期查询考勤记录
    List<AttendanceRecord> findByCheckDate(Date checkDate);
    
    // 根据状态查询考勤记录
    List<AttendanceRecord> findByStatus(Byte status);
    
    // 新增考勤记录
    int insert(AttendanceRecord attendanceRecord);
    
    // 更新考勤记录
    int update(AttendanceRecord attendanceRecord);
    
    // 删除考勤记录
    int delete(Integer recordId);
    
    // 根据员工ID和日期查询考勤记录
    AttendanceRecord findByEmpIdAndCheckDate(Integer empId, Date checkDate);

    // 根据日期范围查询考勤记录
    List<AttendanceRecord> findByDateRange(String startDate, String endDate, int offset, int pageSize);

    // 获取日期范围内考勤记录数量
    Integer findCountByDateRange(String startDate, String endDate);
}
