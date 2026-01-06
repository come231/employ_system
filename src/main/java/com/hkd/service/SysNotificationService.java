package com.hkd.service;

import com.hkd.pojo.SysNotification;

import java.util.List;

public interface SysNotificationService {

    // 根据用户ID查询通知列表（分页）
    List<SysNotification> findByUserId(Integer userId, Integer offset, Integer pageSize);

    // 根据用户ID查询未读通知数量
    Integer countUnreadByUserId(Integer userId);

    // 根据用户ID查询通知总数
    Integer countByUserId(Integer userId);

    // 根据ID查询通知
    SysNotification findById(Integer notificationId);

    // 新增通知
    int insert(SysNotification notification);

    // 标记为已读
    int markAsRead(Integer notificationId);

    // 标记用户所有通知为已读
    int markAllAsRead(Integer userId);

    // 删除通知
    int delete(Integer notificationId);

    // 发送通知的便捷方法
    void sendNotification(Integer userId, String type, String title, String content, Integer relatedId);
}
