package com.hkd.dto;
import lombok.Data;
import java.util.Date;

@Data
public class PunchMessage {
    private Integer empId;
    private Integer punchType;
    private Date checkTime;
    private Byte status;
    private String remark;
}