package com.betsoft.casino.bots;

import com.betsoft.casino.bots.handlers.kafka.KafkaBotRequestHandlerFactory;
import com.betsoft.casino.bots.mqb.IApiClient;
import com.betsoft.casino.bots.mqb.MQBApiClient;
import com.betsoft.casino.bots.mqb.StubApiClient;
import com.betsoft.casino.bots.service.MQBBotServiceHandler;
import com.betsoft.casino.bots.service.kafka.KafkaMessageService;
import com.betsoft.casino.bots.service.kafka.KafkaRecieverService;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.GenericApplicationContext;

/**
 * User: flsh
 * Date: 07.07.2022.
 */
@ComponentScan
@SpringBootApplication
public class ManagedStarter {
    private static final Logger LOG = LogManager.getLogger(ManagedStarter.class);
    private static final String BOT_SERVER_KAFKA_HOST = "BOT_SERVER_KAFKA_HOST";
    private static final String BOT_SERVER_KAFKA_PORT = "BOT_SERVER_KAFKA_PORT";
    private static final String BOT_SERVER_KAFKA_TOPIC = "BOT_SERVER_KAFKA_TOPIC";
    private static final String BOT_SERVER_KAFKA_RESPONSE_TOPIC = "BOT_SERVER_KAFKA_RESPONSE_TOPIC";
    public static final String MQB_SITE_BOT_API_URL = "MQB_SITE_BOT_API_URL";
    public static final String MQB_SITE_SECRET_API_KEY = "MQB_SITE_SECRET_API_KEY";
    public static final String FAKE_MQB_API = "FAKE_MQB_API";
    public static final String DOMAIN_LAUNCH_URL = "DOMAIN_LAUNCH_URL";
    public static final String BASIC_AUTH_PASSWORD = "BASIC_AUTH_PASSWORD";

    private String kafkaHost;
    private int kafkaPort;
    private String kafkaBotTopic;
    private String kafkaBotResponseTopic;
    private String getMqbSiteBotApiUrl;
    private String secretAPIKey;
    private boolean isFakeMqbApi;
    private String domainLaunchUrl;
    private String basicAuthPassword;

    public static void main(String[] args) {
        new ManagedStarter().start(args);
        LOG.debug("Started");
    }

    private void start(String[] args) {
        initSystemVariables();
        try {
            LOG.debug("startMQBBotServer with params:" +
                    " kafkaHost={}, kafkaPort={}, kafkaBotTopic={}, getMqbSiteBotApiUrl={}, secretAPIKey={}, isFakeMqbApi={}, domainLaunchUrl={}",
                    kafkaHost, kafkaPort, kafkaBotTopic, getMqbSiteBotApiUrl, secretAPIKey, isFakeMqbApi, domainLaunchUrl);
            startMQBBotServer(args);
        } catch (CommonException e) {
            LOG.error("start error, exit", e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void initSystemVariables() {
        kafkaHost = System.getProperty(BOT_SERVER_KAFKA_HOST);
        if (StringUtils.isTrimmedEmpty(kafkaHost)) {
            throwIllegalArgumentException(BOT_SERVER_KAFKA_HOST);
        }
        String portProperty = System.getProperty(BOT_SERVER_KAFKA_PORT);
        if (StringUtils.isTrimmedEmpty(portProperty)) {
            throwIllegalArgumentException(BOT_SERVER_KAFKA_PORT);
        }
        kafkaPort = Integer.parseInt(portProperty);
        kafkaBotTopic = System.getProperty(BOT_SERVER_KAFKA_TOPIC);
        if (StringUtils.isTrimmedEmpty(kafkaBotTopic)) {
            throwIllegalArgumentException(BOT_SERVER_KAFKA_TOPIC);
        }
        kafkaBotResponseTopic = System.getProperty(BOT_SERVER_KAFKA_RESPONSE_TOPIC);
        if (StringUtils.isTrimmedEmpty(kafkaBotResponseTopic)) {
            kafkaBotResponseTopic = kafkaBotTopic + "_response";
        }
        getMqbSiteBotApiUrl = System.getProperty(MQB_SITE_BOT_API_URL);
        if (StringUtils.isTrimmedEmpty(getMqbSiteBotApiUrl)) {
            throwIllegalArgumentException(MQB_SITE_BOT_API_URL);
        }
        secretAPIKey = System.getProperty(MQB_SITE_SECRET_API_KEY);
        if (StringUtils.isTrimmedEmpty(secretAPIKey)) {
            throwIllegalArgumentException(MQB_SITE_SECRET_API_KEY);
        }
        isFakeMqbApi = Boolean.parseBoolean(System.getProperty(FAKE_MQB_API));
        if (isFakeMqbApi) {
            domainLaunchUrl = System.getProperty(DOMAIN_LAUNCH_URL);
            if (StringUtils.isTrimmedEmpty(domainLaunchUrl)) {
                throwIllegalArgumentException(DOMAIN_LAUNCH_URL);
            }
        }
        basicAuthPassword = System.getProperty(BASIC_AUTH_PASSWORD);
    }

    private void throwIllegalArgumentException(String propertyName) {
        throw new IllegalArgumentException(propertyName + " system variable not found");
    }

    private void startMQBBotServer(String[] args) throws CommonException {
        try {
            IApiClient mqbApiClient = new MQBApiClient(getMqbSiteBotApiUrl, secretAPIKey, basicAuthPassword);
            IApiClient fakeApiClient = isFakeMqbApi ?
                    new StubApiClient(domainLaunchUrl) :
                    mqbApiClient;

            MQBBotServiceHandler handler = new MQBBotServiceHandler(mqbApiClient, fakeApiClient);

            ConfigurableApplicationContext beanContext = new SpringApplicationBuilder(ManagedStarter.class)
                .initializers((ApplicationContextInitializer<GenericApplicationContext>) context -> {
                    context.registerBean(MQBBotServiceHandler.class, () -> handler);
                })
                .run(args);

            KafkaBotRequestHandlerFactory kafkaBotRequestHandlerFactory =
                    beanContext.getBean(KafkaBotRequestHandlerFactory.class);
            KafkaMessageService kafkaMessageService = new KafkaMessageService(kafkaHost, kafkaPort);
            KafkaRecieverService kafkaBotsRequestService =
                    new KafkaRecieverService(kafkaBotRequestHandlerFactory, kafkaHost, kafkaPort,
                            kafkaBotTopic, kafkaBotResponseTopic, kafkaMessageService);
            kafkaBotsRequestService.init();

        } catch (Exception e) {
            throw new CommonException("Cannot start MQBBot server", e);
        }
    }
}
