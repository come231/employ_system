package com.hkd.mapper;

import com.hkd.pojo.SysNotification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统通知DAO接口
 */
public interface SysNotificationDao {

    // 根据用户ID查询通知列表（分页）
    List<SysNotification> findByUserId(@Param("userId") Integer userId,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

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

    // 删除用户的所有通知
    int deleteByUserId(Integer userId);
}
