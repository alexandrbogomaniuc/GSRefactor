package com.abs.casino.kafka.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.dgphoenix.casino.common.configuration.IGameServerConfiguration;
import com.dgphoenix.casino.kafka.config.KafkaProperties;

@Configuration
@ComponentScan({
        "com.dgphoenix.casino.kafka.handler",
        "com.dgphoenix.casino.kafka.service"
})
@PropertySource("classpath:kafka.properties")
public class KafkaConfiguration {
    @Bean
    public KafkaProperties kafkaProperties(ApplicationContext ac, IGameServerConfiguration gameServerConfig) {
        Environment env = ac.getEnvironment();

        String kafkaClientId = fromServerId(gameServerConfig.getGameServerId());
        return new KafkaProperties(
                env.getProperty("kafka.hosts"),
                env.getProperty("kafka.topic.send.to.mp"),
                env.getProperty("kafka.topic.reply.from.mp"),
                env.getProperty("kafka.topic.receive.from.mp"),
                env.getProperty("kafka.topic.reply.to.mp"),
                env.getProperty("kafka.topic.send.in-service"),
                env.getProperty("kafka.topic.reply.in-service"),
                kafkaClientId,
                gameServerConfig.getGameServerId(),
                env.getProperty("kafka.timeout.ms", Long.class, 15000L));
    }

    public static String fromServerId(long serverId) {
        return "gs-" + serverId;
    }

    public static String fromMpServerId(long serverId) {
        return "mp-" + serverId;
    }
}
