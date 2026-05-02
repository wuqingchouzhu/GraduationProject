package com.qiao.demo.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "inventory.exchange";
    public static final String QUEUE = "stock.out.queue";
    public static final String ROUTING_KEY = "stock.out.routing";

    public static final String DLX_EXCHANGE = "inventory.exchange.dlx";
    public static final String DLQ_QUEUE = "stock.out.queue.dlq";
    public static final String DLQ_ROUTING_KEY = "stock.out.dlq.routing";

    public static final String STOCK_IN_EXCHANGE = "inventory.stock.in.exchange";
    public static final String STOCK_IN_QUEUE = "stock.in.queue";
    public static final String STOCK_IN_ROUTING_KEY = "stock.in.routing";

    public static final String STOCK_IN_DLX_EXCHANGE = "inventory.stock.in.exchange.dlx";
    public static final String STOCK_IN_DLQ_QUEUE = "stock.in.queue.dlq";
    public static final String STOCK_IN_DLQ_ROUTING_KEY = "stock.in.dlq.routing";

    @Bean
    public DirectExchange inventoryExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange inventoryDlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange stockInExchange() {
        return new DirectExchange(STOCK_IN_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange stockInDlxExchange() {
        return new DirectExchange(STOCK_IN_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue stockOutQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        args.put("x-message-ttl", 60000);
        return QueueBuilder.durable(QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue stockOutDlq() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Queue stockInQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", STOCK_IN_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", STOCK_IN_DLQ_ROUTING_KEY);
        args.put("x-message-ttl", 60000);
        return QueueBuilder.durable(STOCK_IN_QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue stockInDlq() {
        return QueueBuilder.durable(STOCK_IN_DLQ_QUEUE).build();
    }

    @Bean
    public Binding stockOutBinding(Queue stockOutQueue, DirectExchange inventoryExchange) {
        return BindingBuilder.bind(stockOutQueue).to(inventoryExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding stockOutDlqBinding(Queue stockOutDlq, DirectExchange inventoryDlxExchange) {
        return BindingBuilder.bind(stockOutDlq).to(inventoryDlxExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding stockInBinding(Queue stockInQueue, DirectExchange stockInExchange) {
        return BindingBuilder.bind(stockInQueue).to(stockInExchange).with(STOCK_IN_ROUTING_KEY);
    }

    @Bean
    public Binding stockInDlqBinding(Queue stockInDlq, DirectExchange stockInDlxExchange) {
        return BindingBuilder.bind(stockInDlq).to(stockInDlxExchange).with(STOCK_IN_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
