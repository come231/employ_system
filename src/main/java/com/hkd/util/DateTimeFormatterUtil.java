package com.hkd.util;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间格式化工具类
 */
public class DateTimeFormatterUtil {
    // 定义常用格式器（全局静态，避免重复创建）
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 24小时制时分格式器（适配上班/下班时间）
    public static final DateTimeFormatter TIME_24H_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * LocalDate 转 yyyy-MM-dd 字符串
     */
    public static String formatLocalDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(DATE_FORMATTER);
    }

    /**
     * LocalDateTime 转 yyyy-MM-dd HH:mm:ss 字符串
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(DATETIME_FORMATTER);
    }


    /**
     * 字符串转 LocalDateTime（用于前端传参解析）
     */
    public static LocalDateTime parseToLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
    }

    /**
     * 字符串转 LocalDate（用于前端传参解析）
     */
    public static LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

}