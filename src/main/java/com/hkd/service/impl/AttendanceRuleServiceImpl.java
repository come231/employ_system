package com.hkd.service.impl;

import com.hkd.mapper.AttendanceRuleDao;
import com.hkd.pojo.AttendanceRule;
import com.hkd.service.AttendanceRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("attendanceRuleService")
public class AttendanceRuleServiceImpl implements AttendanceRuleService {
    
    @Autowired
    private AttendanceRuleDao attendanceRuleDao;
    
    @Override
    public List<AttendanceRule> findAll() {
        return attendanceRuleDao.findAll();
    }
    
    @Override
    public AttendanceRule findById(Integer ruleId) {
        return attendanceRuleDao.findById(ruleId);
    }
    
    @Override
    public AttendanceRule findLatest() {
        return attendanceRuleDao.findLatest();
    }
    
    @Override
    public int insert(AttendanceRule attendanceRule) {
        return attendanceRuleDao.insert(attendanceRule);
    }
    
    @Override
    public int update(AttendanceRule attendanceRule) {
        return attendanceRuleDao.update(attendanceRule);
    }
    
    @Override
    public int delete(Integer ruleId) {
        return attendanceRuleDao.delete(ruleId);
    }
}