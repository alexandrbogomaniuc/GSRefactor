package com.betsoft.casino.mp.kafka;

import com.abs.casino.common.util.ReflectionUtils;
import com.abs.casino.kafka.config.KafkaProperties;
import com.abs.casino.kafka.dto.KafkaHandlerException;
import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.KafkaResponse;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaInServiceRequestHandlerFactory;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandlerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

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

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class KafkaRecieverService {
    private static final Logger LOGGER = LogManager.getLogger(KafkaRecieverService.class);
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
        String bootstrapServers = kafkaProperties.getKafkaHosts();

        Properties properties1 = new Properties();
        properties1.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties1.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties1.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties1.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-mp-receive");
        properties1.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties1.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties1.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties1.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties1.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromGsConsumer(properties1, TopicType.RANDOM);

        Properties properties2 = new Properties();
        properties2.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties2.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties2.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties2.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-mp-receive-spk");
        properties2.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties2.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties2.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties2.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties2.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromGsConsumer(properties2, TopicType.SPECIFIC);

        Properties properties3 = new Properties();
        properties3.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties3.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties3.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties3.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-mp-receive-all_" + kafkaProperties.getKafkaClientId());
        properties3.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties3.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties3.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties3.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties3.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromGsConsumer(properties3, TopicType.ALL);

        Properties properties4 = new Properties();
        properties4.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties4.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties4.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties4.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-mp-in-service-spk");
        properties4.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties4.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties4.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties4.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties4.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromOtherMPNode(properties4, TopicType.SPECIFIC);

        Properties properties5 = new Properties();
        properties5.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties5.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties5.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties5.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-mp-in-service_" + kafkaProperties.getKafkaClientId());
        properties5.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties5.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties5.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties5.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties5.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromOtherMPNode(properties5, TopicType.ALL);
    }
    private void initReceiveFromGsConsumer(Properties properties, TopicType topicType) {
        Thread kafkaReplyConsumerThread = new Thread() {
            @Override
            public void run() {
                String topic = null;

                switch(topicType) {
                    case ALL:
                        topic = kafkaProperties.getKafkaReceiveFromAllGsTopic();
                        break;
                    case RANDOM:
                        topic = kafkaProperties.getKafkaReceiveFromRandomGsTopic();
                        break;
                    case SPECIFIC:
                        topic = kafkaProperties.getKafkaReceiveFromSpecificGsTopic();
                        break;
                    default:
                        break;
                }

                int partition = KafkaMessageService.getPartition(topic, kafkaProperties.getKafkaClientIdNum().intValue());
                TopicPartition tp = TopicType.SPECIFIC.equals(topicType) ? new TopicPartition(topic, partition) : null;
                LOGGER.info("Starting kafka listener for '"
                        + (TopicType.SPECIFIC.equals(topicType) ? tp : topic) + "', for host '"
                        + kafkaProperties.getKafkaHosts() + "' and consumer group '"
                        + properties.get(ConsumerConfig.GROUP_ID_CONFIG) + "', clientId is '"
                        + properties.get(ConsumerConfig.CLIENT_ID_CONFIG) + "'");
                try (KafkaConsumer<String, JsonNode> consumer = new KafkaConsumer<>(properties)) {
                    if (TopicType.SPECIFIC.equals(topicType)) {
                        consumer.assign(Arrays.asList(tp));
                    } else {
                        consumer.subscribe(Arrays.asList(topic));
                    }
                    LOGGER.info("Successfully subscribed to kafka listener for " + (TopicType.SPECIFIC.equals(topicType) ? tp : topic));

                    ObjectMapper mapper = new ObjectMapper();

                    while (true) {
                        ConsumerRecords<String, JsonNode> records = null;
                        try {
                            records = consumer.poll(Duration.ofMillis(100));
                        } catch (RecordDeserializationException e) {
                            LOGGER.error("Error deserializing record. Seeking partition {} for offset {}", e.topicPartition(), e.offset());
                            consumer.seek(e.topicPartition(), e.offset() + 1);
                            continue;
                        } catch (Exception e) {
                            LOGGER.error("Error reading consumptions from Kafka: ", e);
                            initReceiveFromGsConsumer(properties, topicType);
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

                                consumerExecutorService.execute(() -> {
                                    try {
                                        KafkaRequest request = (KafkaRequest) mapper.convertValue(record.value(),
                                                ReflectionUtils.forNameWithCompatibilityAliases(dataType));

                                        try {
                                            KafkaResponse response = kafkaOuterRequestHandlerFactory.getRequestHandler(request).handle(request);
                                            if (response == null) {
                                                response = VoidKafkaResponse.nullResponse();
                                            }
                                            KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyToGsTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                        } catch (KafkaHandlerException e) {
                                            LOGGER.error("Error handling kafka request: ", e);
                                            KafkaResponse response = VoidKafkaResponse.failure(e.getCode(), "Exception happened during handling: " + e.getMessage());
                                            KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyToGsTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
                                        }
                                        
                                    } catch (IllegalArgumentException | ClassNotFoundException e) {
                                        LOGGER.error("No class found for type " + dataType, e);
                                    } catch (Exception e) {
                                        LOGGER.error("Error handling kafka request: ", e);
                                        KafkaResponse response = VoidKafkaResponse.unknownFailure("Exception happened during handling: " + e.getMessage());
                                        KafkaMessageService.sendMessageToKafka(kafkaProperties.getKafkaReplyToGsTopic(), record.key(), response, kafkaProperties.getKafkaClientId(), senderId);
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
        kafkaReplyConsumerThread.setName("kafka-consumption-" + (TopicType.SPECIFIC.equals(topicType) ? kafkaProperties.getKafkaReceiveFromSpecificGsTopic() : kafkaProperties.getKafkaReceiveFromRandomGsTopic()));
        kafkaReplyConsumerThread.start();
    }

    private void initReceiveFromOtherMPNode(Properties properties, TopicType topicType) {
        Thread kafkaReplyConsumerThread = new Thread() {
            @Override
            public void run() {
                
                String topic = null;

                switch(topicType) {
                    case ALL:
                        topic = kafkaProperties.getKafkaSendInServiceAllTopic();
                        break;
                    case RANDOM:
                        return;
                    case SPECIFIC:
                        topic = kafkaProperties.getKafkaSendInServiceSpecificTopic();
                        break;
                    default:
                        break;
                }

                int partition = KafkaMessageService
                        .getPartition(topic, kafkaProperties.getKafkaClientIdNum().intValue());
                TopicPartition tp = new TopicPartition(topic, partition);
                LOGGER.info("Starting kafka listener for '"
                        + (topicType.equals(TopicType.SPECIFIC) ? tp : topic) + "', for host '"
                        + kafkaProperties.getKafkaHosts() + "' and consumer group '"
                        + properties.get(ConsumerConfig.GROUP_ID_CONFIG) + "', clientId is '"
                        + properties.get(ConsumerConfig.CLIENT_ID_CONFIG) + "'");
                try (KafkaConsumer<String, JsonNode> consumer = new KafkaConsumer<>(properties)) {
                    if (topicType.equals(TopicType.SPECIFIC)) {
                        consumer.assign(Arrays.asList(tp));
                    } else {
                        consumer.subscribe(Arrays.asList(topic));
                    }
                    LOGGER.info("Successfully subscribed to kafka listener for " + (topicType.equals(TopicType.SPECIFIC) ? tp : topic));

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
                            initReceiveFromOtherMPNode(properties, topicType);
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
                + (topicType.equals(TopicType.SPECIFIC) ? kafkaProperties.getKafkaSendInServiceSpecificTopic()
                : kafkaProperties.getKafkaSendInServiceAllTopic()));
        kafkaReplyConsumerThread.start();
    }

    private static enum TopicType {
        RANDOM,
        ALL,
        SPECIFIC
    }
}
