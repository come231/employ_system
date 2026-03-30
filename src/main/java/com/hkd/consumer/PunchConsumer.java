package com.hkd.consumer;

import com.alibaba.fastjson.JSON;
import com.hkd.dto.PunchMessage;
import com.hkd.mapper.AttendanceRecordDao;
import com.hkd.pojo.AttendanceRecord;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;

@Component
public class PunchConsumer {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    // 关键：换成这个写法，才能自动注入 Channel
    public void onMessage(Message message, Channel channel) throws IOException {
        String json = new String(message.getBody(), "UTF-8");
        PunchMessage msg = JSON.parseObject(json, PunchMessage.class);

        AttendanceRecord record = new AttendanceRecord();
        record.setEmpId(msg.getEmpId());
        record.setType(msg.getPunchType());
        record.setCheckDate(java.sql.Date.valueOf(LocalDate.now()));
        record.setCheckInTime(msg.getCheckTime());
        record.setStatus(msg.getStatus());
        record.setRemark(msg.getRemark());
        record.setCreateTime(msg.getCheckTime());

        attendanceRecordDao.insert(record);

        // 正确的手动ACK
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}