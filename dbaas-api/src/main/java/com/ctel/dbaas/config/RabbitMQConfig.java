package com.ctel.dbaas.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    public static final String DBAAS_API = "dbaas.api";

    @Bean
    Queue queueConductor() {
        return new Queue(DBAAS_API, true, false, false);
    }

    @Bean
    DirectExchange directExchangeConductor() {
        return new DirectExchange(DBAAS_API, true, false);
    }

    @Bean
    public Declarables declarableExchangeConductor() {
        return new Declarables(
                BindingBuilder.bind(queueConductor()).to(directExchangeConductor()).with(DBAAS_API)
        );
    }

    public static final String DBAAS_TASK_MANAGER = "dbaas.taskmanager";
    public static final String ROUTING_KEY_TASK_MANAGER = "";

    @Bean
    Queue queueTaskMng() {
        return new Queue(DBAAS_TASK_MANAGER, true, false, false);
    }

    @Bean
    DirectExchange directExchangeTaskMng() {
        return new DirectExchange(DBAAS_TASK_MANAGER, true, false);
    }

    @Bean
    public Declarables declarableExchangeTaskManager() {
        return new Declarables(
                BindingBuilder.bind(queueTaskMng()).to(directExchangeTaskMng()).with(ROUTING_KEY_TASK_MANAGER)
        );
    }

    public static final String TRIGGER_BACKUP = "trigger_auto_backup";
    public static final String ROUTING_KEY_TRIGGER_BACKUP = "";

    @Bean
    Queue queueTriggerBackup() {
        return new Queue(TRIGGER_BACKUP, true, false, false);
    }

    @Bean
    DirectExchange directExchangeTriggerBackup() {
        return new DirectExchange(TRIGGER_BACKUP, true, false);
    }

    @Bean
    public Declarables declarableExchangeTriggerBackup() {
        return new Declarables(
                BindingBuilder.bind(queueTriggerBackup()).to(directExchangeTriggerBackup()).with(ROUTING_KEY_TRIGGER_BACKUP)
        );
    }

    // LB
    public static final String DBAAS_LB_AUTO_SCALE = "dbaas.lb.auto_scale";
    public static final String ROUTING_KEY_LB_AUTO_SCALE = "";

    @Bean
    Queue queueLbTaskMng() {
        return new Queue(DBAAS_LB_AUTO_SCALE, true, false, false);
    }

    @Bean
    DirectExchange directExchangeLbTaskMng() {
        return new DirectExchange(DBAAS_LB_AUTO_SCALE, true, false);
    }

    @Bean
    public Declarables declarableExchangeLbTaskManager() {
        return new Declarables(
                BindingBuilder.bind(queueLbTaskMng()).to(directExchangeLbTaskMng()).with(ROUTING_KEY_LB_AUTO_SCALE)
        );
    }
}
