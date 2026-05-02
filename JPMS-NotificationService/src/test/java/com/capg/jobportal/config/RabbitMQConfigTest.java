package com.capg.jobportal.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

class RabbitMQConfigTest {

    @Test
    void messageConverter_returnsJackson2Converter() {
        RabbitMQConfig config = new RabbitMQConfig();
        Jackson2JsonMessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}
