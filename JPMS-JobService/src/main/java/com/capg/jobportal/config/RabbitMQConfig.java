package com.capg.jobportal.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue}")
    private String queue;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    /** Declares the job.posted.queue (durable — survives broker restart) */
    @Bean
    public Queue jobPostedQueue() {
        return new Queue(queue, true);
    }

    /** Declares the shared direct exchange */
    @Bean
    public DirectExchange jobPortalExchange() {
        return new DirectExchange(exchange);
    }

    /** Binds job.posted.queue to the exchange with routing key job.posted */
    @Bean
    public Binding binding(Queue jobPostedQueue, DirectExchange jobPortalExchange) {
        return BindingBuilder
                .bind(jobPostedQueue)
                .to(jobPortalExchange)
                .with(routingKey);
    }

    /** JSON message converter for automatic serialization */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** RabbitTemplate configured with JSON converter */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
