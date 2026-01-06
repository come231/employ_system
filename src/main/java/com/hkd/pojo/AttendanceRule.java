package com.hkd.pojo;

import java.sql.Time;
import java.util.Date;

public class AttendanceRule {
    private Integer ruleId;
    private Time workStartTime;
    private Time workEndTime;
    private Integer lateThreshold;
    private Integer earlyThreshold;
    private Integer supplementValidHours;
    private Integer fullAttendanceDays;
    private Date updateTime;

    // getter and setter methods
    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public Time getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(Time workStartTime) {
        this.workStartTime = workStartTime;
    }

    public Time getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(Time workEndTime) {
        this.workEndTime = workEndTime;
    }

    public Integer getLateThreshold() {
        return lateThreshold;
    }

    public void setLateThreshold(Integer lateThreshold) {
        this.lateThreshold = lateThreshold;
    }

    public Integer getEarlyThreshold() {
        return earlyThreshold;
    }

    public void setEarlyThreshold(Integer earlyThreshold) {
        this.earlyThreshold = earlyThreshold;
    }

    public Integer getSupplementValidHours() {
        return supplementValidHours;
    }

    public void setSupplementValidHours(Integer supplementValidHours) {
        this.supplementValidHours = supplementValidHours;
    }

    public Integer getFullAttendanceDays() {
        return fullAttendanceDays;
    }

    public void setFullAttendanceDays(Integer fullAttendanceDays) {
        this.fullAttendanceDays = fullAttendanceDays;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}