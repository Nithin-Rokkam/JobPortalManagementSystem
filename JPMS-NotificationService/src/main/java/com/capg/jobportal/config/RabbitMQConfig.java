package com.capg.jobportal.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: RabbitMQConfig
 * DESCRIPTION:
 * This configuration class is responsible for setting up RabbitMQ
 * messaging infrastructure for the Job Portal application.
 *
 * It defines:
 * 1. Queues for job posted and job applied events.
 * 2. Direct exchange for routing messages.
 * 3. Bindings between queues and exchange using routing keys.
 * 4. JSON message converter for serializing/deserializing messages.
 *
 * NOTE:
 * This configuration ensures reliable communication between
 * microservices using asynchronous messaging.
 * ================================================================
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue}")
    private String queue;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    /* ================================================================
     * METHOD: jobPostedQueue
     * DESCRIPTION:
     * Declares the queue for job posted events.
     * Durable queue ensures messages survive broker restarts.
     * ================================================================ */
    @Bean
    public Queue jobPostedQueue() {
        return new Queue(queue, true);
    }

    
    /* ================================================================
     * METHOD: jobPortalExchange
     * DESCRIPTION:
     * Declares a direct exchange used for routing messages
     * based on routing keys.
     * ================================================================ */
    @Bean
    public DirectExchange jobPortalExchange() {
        return new DirectExchange(exchange);
    }

    
    /* ================================================================
     * METHOD: binding
     * DESCRIPTION:
     * Binds the job posted queue to the exchange using the
     * specified routing key.
     * ================================================================ */
    @Bean
    public Binding binding(Queue jobPostedQueue, DirectExchange jobPortalExchange) {
        return BindingBuilder
                .bind(jobPostedQueue)
                .to(jobPortalExchange)
                .with(routingKey);
    }

    
    /* ================================================================
     * METHOD: messageConverter
     * DESCRIPTION:
     * Configures JSON message converter to automatically
     * serialize and deserialize messages.
     * ================================================================ */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    

    @Value("${rabbitmq.applied.queue}")
    private String appliedQueue;

    @Value("${rabbitmq.applied.routing-key}")
    private String appliedRoutingKey;

    
    /* ================================================================
     * METHOD: jobAppliedQueue
     * DESCRIPTION:
     * Declares the queue for job applied events.
     * ================================================================ */
    @Bean
    public Queue jobAppliedQueue() {
        return new Queue(appliedQueue, true);
    }

    
    /* ================================================================
     * METHOD: appliedBinding
     * DESCRIPTION:
     * Binds the job applied queue to the exchange using the
     * applied routing key.
     * ================================================================ */
    @Bean
    public Binding appliedBinding(Queue jobAppliedQueue, DirectExchange jobPortalExchange) {
        return BindingBuilder
                .bind(jobAppliedQueue)
                .to(jobPortalExchange)
                .with(appliedRoutingKey);
    }
}