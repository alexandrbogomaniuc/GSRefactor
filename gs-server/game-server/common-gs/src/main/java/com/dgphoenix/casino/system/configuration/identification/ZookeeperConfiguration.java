package com.abs.casino.system.configuration.identification;

import com.abs.casino.system.configuration.identification.ZookeeperProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:zookeeper.properties")
public class ZookeeperConfiguration {
    @Bean
    public ZookeeperProperties zookeeperProperties(ApplicationContext ac) {
        Environment env = ac.getEnvironment();

        return new ZookeeperProperties(
                env.getProperty("zookeeper.connect"),
                env.getProperty("zookeeper.pool-size", Integer.class),
                env.getProperty("zookeeper.ttl-millis", Long.class),
                env.getProperty("zookeeper.heartbeat-interval", Long.class),
                env.getProperty("zookeeper.server-id-path"),
                env.getProperty("zookeeper.server-id-pool-path"));
    }
}
