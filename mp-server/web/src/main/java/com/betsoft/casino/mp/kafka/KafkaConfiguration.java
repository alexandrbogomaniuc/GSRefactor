package com.betsoft.casino.mp.kafka;

import com.betsoft.casino.mp.service.IServerConfigService;
import com.abs.casino.kafka.config.KafkaProperties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan({
        "com.dgphoenix.casino.kafka.handler",
        "com.betsoft.casino.mp.kafka"
})
public class KafkaConfiguration {
    @Bean
    public KafkaProperties kafkaProperties(ApplicationContext ac, IServerConfigService serverConfig) {
        Environment env = ac.getEnvironment();

        String kafkaClientId = fromServerId(serverConfig.getServerId());

        return new KafkaProperties(
                env.getProperty("kafka.hosts"),
                env.getProperty("kafka.topic.send.to.gs"),
                env.getProperty("kafka.topic.reply.from.gs"),
                env.getProperty("kafka.topic.receive.from.gs"),
                env.getProperty("kafka.topic.reply.to.gs"),
                env.getProperty("kafka.topic.send.in-service"),
                env.getProperty("kafka.topic.reply.in-service"),
                env.getProperty("kafka.topic.send.to.bs"),
                env.getProperty("kafka.topic.reply.from.bs"),
                kafkaClientId,
                serverConfig.getServerId(),
                env.getProperty("kafka.timeout.ms", Long.class, 15000L));
    }

    public static String fromServerId(long serverId) {
        return "mp-" + serverId;
    }
}

