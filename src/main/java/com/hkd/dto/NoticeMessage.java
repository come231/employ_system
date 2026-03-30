package com.hkd.dto;
import lombok.Data;

@Data
public class NoticeMessage {
    private Integer userId;
    private String type;
    private String title;
    private String content;
    private Integer relatedId;
}