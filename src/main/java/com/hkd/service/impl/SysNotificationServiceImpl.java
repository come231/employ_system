package com.hkd.service.impl;

import com.hkd.mapper.SysNotificationDao;
import com.hkd.pojo.SysNotification;
import com.hkd.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("sysNotificationService")
public class SysNotificationServiceImpl implements SysNotificationService {

    @Autowired
    private SysNotificationDao sysNotificationDao;

    @Override
    public List<SysNotification> findByUserId(Integer userId, Integer offset, Integer pageSize) {
        return sysNotificationDao.findByUserId(userId, offset, pageSize);
    }

    @Override
    public Integer countUnreadByUserId(Integer userId) {
        Integer count = sysNotificationDao.countUnreadByUserId(userId);
        return count != null ? count : 0;
    }

    @Override
    public Integer countByUserId(Integer userId) {
        Integer count = sysNotificationDao.countByUserId(userId);
        return count != null ? count : 0;
    }

    @Override
    public SysNotification findById(Integer notificationId) {
        return sysNotificationDao.findById(notificationId);
    }

    @Override
    public int insert(SysNotification notification) {
        return sysNotificationDao.insert(notification);
    }

    @Override
    public int markAsRead(Integer notificationId) {
        return sysNotificationDao.markAsRead(notificationId);
    }

    @Override
    public int markAllAsRead(Integer userId) {
        return sysNotificationDao.markAllAsRead(userId);
    }

    @Override
    public int delete(Integer notificationId) {
        return sysNotificationDao.delete(notificationId);
    }

    @Override
    public void sendNotification(Integer userId, String type, String title, String content, Integer relatedId) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId != null ? relatedId : 0);
        notification.setIsRead((byte) 0);
        notification.setCreateTime(new Date());
        sysNotificationDao.insert(notification);
    }
}
