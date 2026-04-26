package com.qiao.demo.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "inventory.exchange";
    public static final String QUEUE = "stock.out.queue";
    public static final String ROUTING_KEY = "stock.out.routing";

    @Bean
    public DirectExchange inventoryExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue stockOutQueue() {
        return new Queue(QUEUE, true, false, false);
    }

    @Bean
    public Binding stockOutBinding(Queue stockOutQueue, DirectExchange inventoryExchange) {
        return BindingBuilder.bind(stockOutQueue).to(inventoryExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
