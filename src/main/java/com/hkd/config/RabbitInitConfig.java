package com.hkd.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 项目启动时强制创建队列、交换机、绑定
 */
@Configuration
public class RabbitInitConfig {

    @Bean
    public Queue punchQueue() {
        return new Queue("punch.queue", true);
    }

    @Bean
    public Queue noticeQueue() {
        return new Queue("notice.queue", true);
    }

    @Bean
    public DirectExchange punchExchange() {
        return new DirectExchange("punch.exchange");
    }

    @Bean
    public DirectExchange noticeExchange() {
        return new DirectExchange("notice.exchange");
    }

    @Bean
    public Binding punchBinding() {
        return BindingBuilder.bind(punchQueue())
                .to(punchExchange())
                .with("punch.route");
    }

    @Bean
    public Binding noticeBinding() {
        return BindingBuilder.bind(noticeQueue())
                .to(noticeExchange())
                .with("notice.route");
    }

    // 强制初始化
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }
}