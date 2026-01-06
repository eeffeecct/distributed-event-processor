package com.example;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;


@Configuration
public class RabbitConfig {
    @Bean
    public Queue eventQueue() {
        return new Queue(RabbitQueueConstants.QUEUE_RAW_EVENTS, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Event to JSON converter
        return new Jackson2JsonMessageConverter();
    }
}
