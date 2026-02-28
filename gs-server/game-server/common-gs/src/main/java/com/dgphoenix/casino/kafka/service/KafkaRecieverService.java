package com.abs.casino.kafka.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.common.util.ReflectionUtils;
import com.abs.casino.kafka.config.KafkaProperties;
import com.dgphoenix.casino.kafka.dto.KafkaHandlerException;
import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;
import com.abs.casino.kafka.dto.PingRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaInServiceRequestHandlerFactory;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandlerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Service
public class KafkaRecieverService {
    private static final Logger LOGGER = LogManager.getLogger(KafkaMessageService.class);
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String SENDER_ID = "SENDER_ID";
    private static final String RECEIVER_ID = "RECEIVER_ID";

    private final KafkaOuterRequestHandlerFactory kafkaOuterRequestHandlerFactory;
    private final KafkaInServiceRequestHandlerFactory kafkaInServiceRequestHandlerFactory;
    private final KafkaProperties kafkaProperties;

    private final ExecutorService consumerExecutorService = Executors.newFixedThreadPool(50);

    @Autowired
    public KafkaRecieverService(KafkaOuterRequestHandlerFactory kafkaOuterRequestHandlerFactory,
                                KafkaInServiceRequestHandlerFactory kafkaInServiceRequestHandlerFactory,
                                KafkaProperties kafkaProperties) {
        this.kafkaOuterRequestHandlerFactory = kafkaOuterRequestHandlerFactory;
        this.kafkaInServiceRequestHandlerFactory = kafkaInServiceRequestHandlerFactory;
        this.kafkaProperties = kafkaProperties;
    }

    @PostConstruct
    void init() {
        long timeoutMillis = 30_000;
        long intervalMillis = 5_000;
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            boolean result = GameServer.getInstance().isInitialized();
            if (result) {
                LOGGER.info("GS is initialized. Initializing kafka receivers.");
                break;
            }

            LOGGER.info("GS is NOT initialized. Waiting a bit...");
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                LOGGER.warn("GS init wait was interrupted. Initializing kafka receivers.");
            }
        }

        LOGGER.info("Initializing kafka receivers.");

        String bootstrapServers = kafkaProperties.getKafkaHosts();

        Properties properties1 = new Properties();
        properties1.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties1.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties1.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties1.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-gs-receive");
        properties1.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties1.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties1.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties1.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties1.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        initReceiveFromMpConsumer(properties1, false);

        Properties properties2 = new Properties();
        properties2.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties2.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties2.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties2.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-gs-receive-spk");
        properties2.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties2.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties2.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties2.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties2.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromMpConsumer(properties2, true);

        Properties properties3 = new Properties();
        properties3.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties3.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties3.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties3.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-gs-in-service-spk");
        properties3.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties3.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties3.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties3.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties3.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromOtherGSNode(properties3, true);

        Properties properties4 = new Properties();
        properties4.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties4.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties4.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties4.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-gs-in-service_" + kafkaProperties.getKafkaClientId());
        properties4.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties4.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties4.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties4.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties4.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromOtherGSNode(properties4, false);
    }

    private void initReceiveFromMpConsumer(Properties properties, boolean fromSpecific) {
        Thread kafkaReplyConsumerThread = new Thread() {
            @Override
            public void run() {
                String topic = (fromSpecific ? kafkaProperties.getKafkaReceiveFromSpecificMpTopic() : kafkaProperties.getKafkaReceiveFromRandomMpTopic());
                int partition = KafkaMessageService.getPartition(topic, kafkaProperties.getKafkaClientIdNum().intValue());
                TopicPartition tp = fromSpecific ? new TopicPartition(topic, partition) : null;
                LOGGER.info("Starting kafka listener for '"
                        + (fromSpecific ? tp : topic)  + "', for host '"
                        + kafkaProperties.getKafkaHosts() + "' and consumer group '"
                        + properties.get(ConsumerConfig.GROUP_ID_CONFIG) + "', clientId is '"
                        + properties.get(ConsumerConfig.CLIENT_ID_CONFIG) + "'");
                try (KafkaConsumer<String, JsonNode> consumer = new KafkaConsumer<>(properties)) {
                    if (fromSpecific) {
                        consumer.assign(Arrays.asList(tp));
                    } else {
                        consumer.subscribe(Arrays.asList(topic));
                    }
                    LOGGER.info("Successfully subscribed to kafka listener for " + (fromSpecific ? tp : topic));

                    ObjectMapper mapper = JsonMapper
                            .builder()
                            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .build();

                    while (true) {
                        ConsumerRecords<String, JsonNode> records = null;
                        try {
                            records =
                                    consumer.poll(Duration.ofMillis(100));
                        } catch (RecordDeserializationException e) {
                            LOGGER.error("Error deserializing record. Seeking partition {} for offset {}", e.topicPartition(), e.offset());
                            consumer.seek(e.topicPartition(), e.offset() + 1);
                            continue;
                        } catch (Exception e) {
                            LOGGER.error("Error reading consumptions from Kafka: ", e);
                            initReceiveFromMpConsumer(properties, fromSpecific);
                            return;
                        }

                        for (ConsumerRecord<String, JsonNode> record : records) {
                            try {
                                Header dataTypeHeader = record.headers().lastHeader(DATA_TYPE);
                                String dataType = new String(dataTypeHeader.value());

                                Header receiverIdHeader = record.headers().lastHeader(RECEIVER_ID);
                                String receiverId = Optional.ofNullable(receiverIdHeader).map(Header::value).map(String::new).orElse(null);

                                String clientId = (String) properties.get(ConsumerConfig.CLIENT_ID_CONFIG);

                                if (receiverId != null && !receiverId.equals(clientId)) {
                                    LOGGER.debug("Skipping processing message of type {} as receiverId '{}' does not correspond to client id '{}'", dataType, receiverId, clientId);
                                    continue;
                                }

                                Header senderIdHeader = record.headers().lastHeader(SENDER_ID);
                                String senderId = Optional.ofNullable(senderIdHeader).map(Header::value).map(String::new).orElse(null);

                                consumerExecutorService.submit(() -> {
                                    try {
                                        KafkaRequest request = (KafkaRequest) mapper.convertValue(record.value(),
                                                ReflectionUtils.forNameWithCompatibilityAliases(dataType));
                                        
                                        try {
                                            KafkaResponse response = kafkaOuterRequestHandlerFactory.getRequestHandler(request).handle(request);
                                            if (response == null) {
                                                response = VoidKafkaResponse.nullResponse();
                                            }
                                            KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyToMpTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                        } catch (KafkaHandlerException e) {
                                            LOGGER.error("Error handling kafka request: ", e);
                                            KafkaResponse response = VoidKafkaResponse.failure(e.getCode(), "Exception happened during handling: " + e.getMessage());
                                            KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyToMpTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                        }
                                        
                                    } catch (IllegalArgumentException | ClassNotFoundException e) {
                                        LOGGER.error("No class found for type " + dataType, e);
                                    } catch (Exception e) {
                                        LOGGER.error("Error handling kafka request: ", e);
                                        KafkaResponse response = VoidKafkaResponse.unknownFailure("Exception happened during handling: " + e.getMessage());
                                        KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyToMpTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                    }
                                });
                            } catch (Exception e) {
                                LOGGER.warn("Error in processing record from kafka: ", e);
                            }
                        }

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        kafkaReplyConsumerThread.setDaemon(true);
        kafkaReplyConsumerThread.setName("kafka-consumption-" + (fromSpecific ? kafkaProperties.getKafkaReceiveFromSpecificMpTopic() : kafkaProperties.getKafkaReceiveFromRandomMpTopic()));
        kafkaReplyConsumerThread.start();
    }

    private void initReceiveFromOtherGSNode(Properties properties, boolean fromSpecific) {
        Thread kafkaReplyConsumerThread = new Thread() {
            @Override
            public void run() {
                String topic = fromSpecific ? kafkaProperties.getKafkaSendInServiceSpecificTopic()
                        : kafkaProperties.getKafkaSendInServiceAllTopic();
                int partition = KafkaMessageService
                        .getPartition(topic, kafkaProperties.getKafkaClientIdNum().intValue());
                TopicPartition tp = new TopicPartition(topic, partition);
                LOGGER.info("Starting kafka listener for '"
                        + (fromSpecific ? tp : topic) + "', for host '"
                        + kafkaProperties.getKafkaHosts() + "' and consumer group '"
                        + properties.get(ConsumerConfig.GROUP_ID_CONFIG) + "', clientId is '"
                        + properties.get(ConsumerConfig.CLIENT_ID_CONFIG) + "'");
                try (KafkaConsumer<String, JsonNode> consumer = new KafkaConsumer<>(properties)) {
                    if (fromSpecific) {
                        consumer.assign(Arrays.asList(tp));
                    } else {
                        consumer.subscribe(Arrays.asList(topic));
                    }
                    LOGGER.info("Successfully subscribed to kafka listener for " + (fromSpecific ? tp : topic));

                    ObjectMapper mapper = JsonMapper
                            .builder()
                            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .build();

                    while (true) {
                        ConsumerRecords<String, JsonNode> records = null;
                        try {
                            records =
                                    consumer.poll(Duration.ofMillis(100));
                        } catch (RecordDeserializationException e) {
                            LOGGER.error("Error deserializing record. Seeking partition {} for offset {}", e.topicPartition(), e.offset());
                            consumer.seek(e.topicPartition(), e.offset() + 1);
                            continue;
                        } catch (Exception e) {
                            LOGGER.error("Error reading consumptions from Kafka: ", e);
                            initReceiveFromOtherGSNode(properties, fromSpecific);
                            return;
                        }

                        for (ConsumerRecord<String, JsonNode> record : records) {
                            try {
                                Header dataTypeHeader = record.headers().lastHeader(DATA_TYPE);
                                String dataType = new String(dataTypeHeader.value());

                                Header senderIdHeader = record.headers().lastHeader(SENDER_ID);
                                String senderId = Optional.ofNullable(senderIdHeader).map(Header::value).map(String::new).orElse(null);

                                Header receiverIdHeader = record.headers().lastHeader(RECEIVER_ID);
                                String receiverId = Optional.ofNullable(receiverIdHeader).map(Header::value).map(String::new).orElse(null);

                                Object clientId = properties.get(ConsumerConfig.CLIENT_ID_CONFIG);

                                // if ping request, process and continue
                                if (dataType.equals(PingRequest.class.getName())) {
                                    consumerExecutorService.submit(() -> {
                                        KafkaMessageService.sendMessageToKafka(
                                                topic, record.key(),
                                                VoidKafkaResponse.success(),
                                                kafkaProperties.getKafkaClientId(), senderId);
                                    });
                                    continue;
                                }

                                if (clientId.equals(senderId)) {
                                    LOGGER.debug("Skipping processing message as senderId '{}' corresponds to client id '{}'", senderId, clientId);
                                    continue;
                                }

                                if (receiverId != null && !receiverId.equals(clientId)) {
                                    LOGGER.debug("Skipping processing message as receiverId '{}' does not correspond to client id '{}'", senderId, clientId);
                                    continue;
                                }

                                consumerExecutorService.submit(() -> {
                                    try {
                                        KafkaRequest request = (KafkaRequest) mapper.convertValue(record.value(),
                                                ReflectionUtils.forNameWithCompatibilityAliases(dataType));

                                        try {
                                            KafkaResponse response = kafkaInServiceRequestHandlerFactory.getRequestHandler(request).handle(request);
                                            if (response == null) {
                                                response = VoidKafkaResponse.nullResponse();
                                            }
                                            KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyInServiceTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                        } catch (KafkaHandlerException e) {
                                            LOGGER.error("Error handling kafka request: ", e);
                                            KafkaResponse response = VoidKafkaResponse.failure(e.getCode(), "Exception happened during handling: " + e.getMessage());
                                            KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyInServiceTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                        }
                                        
                                    } catch (IllegalArgumentException | ClassNotFoundException e) {
                                        LOGGER.error("No class found for type " + dataType, e);
                                    } catch (Exception e) {
                                        LOGGER.error("Error handling kafka request: ", e);
                                        KafkaResponse response = VoidKafkaResponse.unknownFailure("Exception happened during handling: " + e.getMessage());
                                        KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyInServiceTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                    }
                                });
                            } catch (Exception e) {
                                LOGGER.warn("Error in processing record from kafka: ", e);
                            }
                        } 

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        kafkaReplyConsumerThread.setDaemon(true);
        kafkaReplyConsumerThread.setName("kafka-consumption-"
                + (fromSpecific ? kafkaProperties.getKafkaSendInServiceSpecificTopic()
                        : kafkaProperties.getKafkaSendInServiceAllTopic()));
        kafkaReplyConsumerThread.start();
    }
}
