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

    // Creates the queue in RabbitMQ
    @Bean
    public Queue jobPostedQueue() {
        return new Queue(queue, true); // true = durable (survives restart)
    }

    // Creates the exchange
    @Bean
    public DirectExchange jobPortalExchange() {
        return new DirectExchange(exchange);
    }

    // Binds queue to exchange using routing key
    @Bean
    public Binding binding(Queue jobPostedQueue, DirectExchange jobPortalExchange) {
        return BindingBuilder
                .bind(jobPostedQueue)
                .to(jobPortalExchange)
                .with(routingKey);
    }

    // Converts Java objects to JSON automatically
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}