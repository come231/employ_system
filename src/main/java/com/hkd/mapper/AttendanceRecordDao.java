package com.hkd.mapper;

import com.hkd.pojo.AttendanceRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface AttendanceRecordDao {
    // 查询所有考勤记录
    List<AttendanceRecord> findAll(@Param("offset") int offset,@Param("pageSize") int pageSize);

    //获取所有考勤记录的数量
    Integer findAllCount();

    // 根据员工Id模糊查询考勤记录
    List<AttendanceRecord> findById(@Param("empId") Integer empId,@Param("offset") int offset,@Param("pageSize") int pageSize);

    //获取所有员工id模糊查询考勤记录的数量
    Integer findCountByEmpId(Integer empId);

    // 根据员工工号和日期范围查询考勤记录
    List<AttendanceRecord> findByEmpIdAndDateRange(@Param("empId") Integer empId,@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,@Param("status") Integer status,@Param("type") Integer type);

    // 根据日期查询考勤记录
    List<AttendanceRecord> findByCheckDate(Date checkDate);


    // 根据员工ID和打卡日期查询考勤记录
    AttendanceRecord findByEmpIdAndCheckDate(Integer empId, Date checkDate);

    // 新增考勤记录
    int insert(AttendanceRecord attendanceRecord);

    // 更新考勤记录
    int update(AttendanceRecord attendanceRecord);

    // 删除考勤记录
    int delete(Integer recordId);

    // 根据状态查询考勤记录
    List<AttendanceRecord> findByStatus(Byte status);

    // 根据类型查询考勤记录
    List<AttendanceRecord> findByType(Integer type);

    // 根据日期范围查询考勤记录
    List<AttendanceRecord> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    // 获取日期范围内考勤记录数量
    Integer findCountByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
