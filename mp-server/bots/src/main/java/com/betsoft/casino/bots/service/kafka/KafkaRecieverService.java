package com.betsoft.casino.bots.service.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.betsoft.casino.bots.handlers.kafka.KafkaBotRequestHandlerFactory;
import com.dgphoenix.casino.common.util.ReflectionUtils;
import com.abs.casino.kafka.dto.KafkaHandlerException;
import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.KafkaResponse;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class KafkaRecieverService {
    private static final Logger LOGGER = LogManager.getLogger(KafkaRecieverService.class);
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String SENDER_ID = "SENDER_ID";
    private static final String CLIENT_ID = "bs-1";

    private final KafkaBotRequestHandlerFactory kafkaBotRequestHandlerFactory;
    private final String kafkaHost;
    private final String kafkaTopic;
    private final String kafkaResponseTopic;

    private final KafkaMessageService kafkaMessageService;

    private final ExecutorService consumerExecutorService = Executors.newFixedThreadPool(50);

    public KafkaRecieverService(KafkaBotRequestHandlerFactory kafkaBotRequestHandlerFactory,
                                String kafkaHost,
                                Integer kafkaPort,
                                String kafkaTopic,
                                String kafkaResponseTopic,
                                KafkaMessageService kafkaMessageService) {
        this.kafkaBotRequestHandlerFactory = kafkaBotRequestHandlerFactory;
        this.kafkaHost = String.format("%s:%d", kafkaHost, kafkaPort);
        this.kafkaTopic = kafkaTopic;
        this.kafkaResponseTopic = kafkaResponseTopic;
        this.kafkaMessageService = kafkaMessageService;
    }

    public void init() {
        String bootstrapServers = kafkaHost;

        Properties properties1 = new Properties();
        properties1.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties1.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties1.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties1.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cg-bs-receive");
        properties1.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        properties1.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties1.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties1.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties1.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        initReceiveFromMpConsumer(properties1);
    }

    private void initReceiveFromMpConsumer(Properties properties) {
        Thread kafkaReplyConsumerThread = new Thread() {
            @Override
            public void run() {
                LOGGER.info("Starting kafka listener for '"
                        + kafkaTopic + "', for host '"
                        + kafkaHost + "' and consumer group '"
                        + properties.get(ConsumerConfig.GROUP_ID_CONFIG) + "', clientId is '"
                        + properties.get(ConsumerConfig.CLIENT_ID_CONFIG) + "'");
                try (KafkaConsumer<String, JsonNode> consumer = new KafkaConsumer<>(properties)) {
                    consumer.subscribe(Arrays.asList(kafkaTopic));
                    LOGGER.info("Successfully subscribed to kafka listener for " + kafkaTopic);

                    ObjectMapper mapper = JsonMapper
                            .builder()
                            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .build();

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
                            initReceiveFromMpConsumer(properties);
                            return;
                        }

                        for (ConsumerRecord<String, JsonNode> record : records) {
                            try {
                                Header dataTypeHeader = record.headers().lastHeader(DATA_TYPE);
                                String dataType = new String(dataTypeHeader.value());

                                Header senderIdHeader = record.headers().lastHeader(SENDER_ID);
                                String senderId = Optional.ofNullable(senderIdHeader).map(Header::value).map(String::new).orElse(null);

                                consumerExecutorService.submit(() -> {
                                    try {
                                        KafkaRequest request = (KafkaRequest) mapper.convertValue(record.value(),
                                                ReflectionUtils.forNameWithCompatibilityAliases(dataType));
                                        
                                        try {
                                            KafkaResponse response = kafkaBotRequestHandlerFactory.getRequestHandler(request).handle(request);
                                            if (response == null) {
                                                response = VoidKafkaResponse.nullResponse();
                                            }
                                            kafkaMessageService.sendMessageToKafka(kafkaResponseTopic, record.key(), response, CLIENT_ID, senderId);
                                        } catch (KafkaHandlerException e) {
                                            LOGGER.error("Error handling kafka request: ", e);
                                            KafkaResponse response = VoidKafkaResponse.failure(e.getCode(), "Exception happened during handling: " + e.getMessage());
                                            kafkaMessageService.sendMessageToKafka(kafkaResponseTopic, record.key(), response, CLIENT_ID, senderId);
                                        }
                                        
                                    } catch (IllegalArgumentException | ClassNotFoundException e) {
                                        LOGGER.error("No class found for type " + dataType, e);
                                    } catch (Exception e) {
                                        LOGGER.error("Error handling kafka request: ", e);
                                        KafkaResponse response = VoidKafkaResponse.unknownFailure("Exception happened during handling: " + e.getMessage());
                                        kafkaMessageService.sendMessageToKafka(kafkaResponseTopic, record.key(), response, CLIENT_ID, senderId);
                                    }
                                });
                            } catch (Throwable e) {
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
        kafkaReplyConsumerThread.setName("kafka-consumption-" + kafkaTopic);
        kafkaReplyConsumerThread.start();
    }
}
