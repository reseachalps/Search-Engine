package com.datapublica.companies.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 */
@Configuration
public class RabbitConfiguration {
    @Value("${rabbit.hosts:localhost}")
    private String hosts;
    @Value("${rabbit.default.throttle:1000}")
    private int throttle;
    @Value("${rabbit.user:guest}")
    private String user;
    @Value("${rabbit.pass:guest}")
    private String pass;

    @Profile("!test")
    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory rabbit = new CachingConnectionFactory();
        // no parsing because it is the same syntax
        rabbit.setAddresses(hosts);
        if (!user.isEmpty()) {
            rabbit.setUsername(user);
            rabbit.setPassword(pass);
        }
        return rabbit;
    }

    @Profile("!test")
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory) {
        return new RabbitTemplate(rabbitConnectionFactory);
    }

    @Profile("!test")
    @Bean
    public AmqpAdmin rabbitAdmin(ConnectionFactory rabbitConnectionFactory) {
        return new RabbitAdmin(rabbitConnectionFactory);
    }

    public int getDefaultThrottle() {
        return throttle;
    }
}
