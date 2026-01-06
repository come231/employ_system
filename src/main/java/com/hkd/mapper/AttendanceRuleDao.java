package com.hkd.mapper;

import com.hkd.pojo.AttendanceRule;

import java.util.List;

public interface AttendanceRuleDao {
    // 查询所有考勤规则
    List<AttendanceRule> findAll();
    
    // 根据ID查询考勤规则
    AttendanceRule findById(Integer ruleId);
    
    // 查询最新的考勤规则
    AttendanceRule findLatest();
    
    // 新增考勤规则
    int insert(AttendanceRule attendanceRule);
    
    // 更新考勤规则
    int update(AttendanceRule attendanceRule);
    
    // 删除考勤规则
    int delete(Integer ruleId);
}