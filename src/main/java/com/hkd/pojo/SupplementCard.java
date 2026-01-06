package com.hkd.pojo;

import java.util.Date;

public class SupplementCard {
    private Integer supplementId;
    private Integer empId;
    private Date checkDate;
    private Byte checkType;
    private Date actualTime;
    private String reason;
    private Integer approverId;
    private Byte approveStatus;
    private String approveOpinion;
    private Date createTime;
    private Date updateTime;

    // getter and setter methods
    public Integer getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(Integer supplementId) {
        this.supplementId = supplementId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public Byte getCheckType() {
        return checkType;
    }

    public void setCheckType(Byte checkType) {
        this.checkType = checkType;
    }

    public Date getActualTime() {
        return actualTime;
    }

    public void setActualTime(Date actualTime) {
        this.actualTime = actualTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getApproverId() {
        return approverId;
    }

    public void setApproverId(Integer approverId) {
        this.approverId = approverId;
    }

    public Byte getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(Byte approveStatus) {
        this.approveStatus = approveStatus;
    }

    public String getApproveOpinion() {
        return approveOpinion;
    }

    public void setApproveOpinion(String approveOpinion) {
        this.approveOpinion = approveOpinion;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}