package com.hkd.consumer;

import com.alibaba.fastjson.JSON;
import com.hkd.dto.NoticeMessage;
import com.hkd.mapper.SysNotificationDao;
import com.hkd.pojo.SysNotification;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

@Component
public class NoticeConsumer implements MessageListener {

    @Resource
    private SysNotificationDao sysNotificationDao;

    @Override
    public void onMessage(Message message) {
        try {
            String json = new String(message.getBody(), "UTF-8");
            NoticeMessage msg = JSON.parseObject(json, NoticeMessage.class);


            SysNotification notification = new SysNotification();
            // 【核心修复】防止 user_id 为 null
            if (notification == null || notification.getUserId() == null) {
                return;
            }
            notification.setUserId(msg.getUserId());
            notification.setType(msg.getType());
            notification.setTitle(msg.getTitle());
            notification.setContent(msg.getContent());
            notification.setRelatedId(msg.getRelatedId());
            notification.setIsRead((byte) 0);
            notification.setCreateTime(new Date());

            sysNotificationDao.insert(notification);

            // 手动ACK
            Channel channel = message.getMessageProperties().getHeader(AmqpHeaders.CHANNEL);
            long tag = message.getMessageProperties().getDeliveryTag();
            channel.basicAck(tag, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}