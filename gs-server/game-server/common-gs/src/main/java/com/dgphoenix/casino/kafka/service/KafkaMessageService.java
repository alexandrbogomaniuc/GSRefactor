package com.abs.casino.kafka.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.ReflectionUtils;
import com.abs.casino.kafka.config.KafkaConfiguration;
import com.abs.casino.kafka.config.KafkaProperties;
import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;
import com.dgphoenix.casino.kafka.dto.BooleanResponseDto;
import com.dgphoenix.casino.kafka.dto.KafkaHandlerException;
import com.dgphoenix.casino.kafka.dto.KafkaMessage;
import com.dgphoenix.casino.kafka.dto.KafkaRequest;
import com.dgphoenix.casino.kafka.dto.KafkaResponse;
import com.dgphoenix.casino.kafka.dto.PingRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import reactor.core.publisher.Mono;

import static com.dgphoenix.casino.gs.persistance.remotecall.KafkaResponseConverterUtil.convertToType;

@Service
public class KafkaMessageService {
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String SENDER_ID = "SENDER_ID";
    private static final String RECEIVER_ID = "RECEIVER_ID";

    private static final Logger LOGGER = LogManager.getLogger(KafkaMessageService.class);

    private final ExecutorService completableFutureExecutorService = Executors.newFixedThreadPool(50);

    private final ExecutorService producerExecutorService = Executors.newFixedThreadPool(50);

    private final ExecutorService consumerExecutorService = Executors.newFixedThreadPool(50);

    private final ConcurrentHashMap<String, CompletableFuture<KafkaResponse>> awaitingCompletableFutures = new ConcurrentHashMap<>();

    private KafkaProperties kafkaProperties;

    private static KafkaProducer<String, JsonNode> producer;

    private static ConcurrentHashMap<Pair<String, Integer>, Integer> topicPartitions = new ConcurrentHashMap<Pair<String, Integer>, Integer>();

    @Autowired
    public KafkaMessageService(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        KafkaMessageService.producer = createProducer(kafkaProperties.getKafkaHosts());
    }

    @PostConstruct
    void init() {
        String bootstrapServers = kafkaProperties.getKafkaHosts();

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,  "cg-gs-reply-spk");
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.getKafkaClientId());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        properties.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        initReplyToGsConsumer(properties);
    }

    private void initReplyToGsConsumer(Properties properties) {
        Thread kafkaReplyConsumerThread = new Thread() {
            @Override
            public void run() {
                List<String> topics = Arrays.asList(kafkaProperties.getKafkaReplyFromMpTopic(), kafkaProperties.getKafkaReplyInServiceTopic());
                List<TopicPartition> topicPartitions = new ArrayList<>();
                for (String topic : topics) {
                    int partition = getPartition(topic, kafkaProperties.getKafkaClientIdNum().intValue());
                    TopicPartition assignedPartition = new TopicPartition(topic, partition);
                    topicPartitions.add(assignedPartition);
                }
                LOGGER.info("Starting kafka listener for '"
                        + topicPartitions + "', for host '"
                        + kafkaProperties.getKafkaHosts() + "' and consumer group '"
                        + properties.get(ConsumerConfig.GROUP_ID_CONFIG) + "', clientId is '"
                        + properties.get(ConsumerConfig.CLIENT_ID_CONFIG) + "'");
                try (KafkaConsumer<String, JsonNode> consumer = new KafkaConsumer<>(properties)) {
                    consumer.assign(topicPartitions);
                    LOGGER.info("Successfully subscribed to kafka listener for " + topicPartitions);

                    ObjectMapper mapper = JsonMapper
                            .builder().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .build();

                    while (true) {
                        ConsumerRecords<String, JsonNode> records = null;
                        try {
                            records =
                                    consumer.poll(Duration.ofMillis(100));
                        } catch (Exception e) {
                            LOGGER.error("Error reading consumptions from Kafka: ", e);
                            initReplyToGsConsumer(properties);
                            return;
                        }

                        for (ConsumerRecord<String, JsonNode> record : records) {
                            try {
                                Header receiverIdHeader = record.headers().lastHeader(RECEIVER_ID);
                                String receiverId = Optional.ofNullable(receiverIdHeader).map(Header::value).map(String::new).orElse(null);

                                Header dataTypeHeader = record.headers().lastHeader(DATA_TYPE);
                                String dataType = new String(dataTypeHeader.value());

                                Object clientId = properties.get(ConsumerConfig.CLIENT_ID_CONFIG);
                                if (!clientId.equals(receiverId)) {
                                    LOGGER.debug("Skipping processing message of type '{}' as receiver '{}' not corresponds to client id '{}'", dataType, receiverId, clientId);
                                    continue;
                                }

                                consumerExecutorService.execute(() -> {
                                    try {
                                        KafkaResponse response = (KafkaResponse) mapper.convertValue(record.value(),
                                                ReflectionUtils.forNameWithCompatibilityAliases(dataType));
                                        LOGGER.debug("Recieved response " + record.key() + " :: " + response.getClass());
                                        awaitingCompletableFutures.computeIfPresent(record.key(), new BiFunction<String, CompletableFuture<KafkaResponse>, CompletableFuture<KafkaResponse>>() {
                                            @Override
                                            public CompletableFuture<KafkaResponse> apply(String messageId, CompletableFuture<KafkaResponse> completableFuture) {
                                                completableFuture.complete(response);
                                                return null;
                                            }
                                        });
                                    } catch (IllegalArgumentException | ClassNotFoundException e) {
                                        LOGGER.error("No class found for type " + dataType, e);
                                    } catch (Exception e) {
                                        LOGGER.warn("Error in processing record from kafka: ", e);
                                    }
                                });
                            } catch (Exception e) {
                                LOGGER.warn("Error in processing record from kafka: ", e);
                            }
                        }

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                }
            }
        };

        kafkaReplyConsumerThread.setDaemon(true);
        kafkaReplyConsumerThread.setName("kafka-consumption-" + kafkaProperties.getKafkaReplyFromMpTopic());
        kafkaReplyConsumerThread.start();
    }


    private static class KafkaProducerTask implements Runnable {
        private final KafkaProperties kafkaProperties;
        private final String kafkaTopic;
        private final String receiverId;

        private final String messageId;
        private final KafkaMessage message;

        private KafkaProducerTask(KafkaProperties kafkaProperties, String kafkaTopic, KafkaMessage message, String receiverId) {
            this.kafkaProperties = kafkaProperties;
            this.kafkaTopic = kafkaTopic;
            this.receiverId = receiverId;
            this.message = message;
            this.messageId = String.valueOf(System.currentTimeMillis()) + message.hashCode();
        }

        @Override
        public void run() {
            KafkaMessage sendMessage = sendMessageToKafka(kafkaTopic, getMessageId(), message, kafkaProperties.getKafkaClientId(), receiverId);

            LOGGER.info("Published a message to Kafka to {}: {}", kafkaTopic, sendMessage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("Cannot publish a message to Kafka to {}: {}", kafkaTopic, sendMessage);
            }
        }

        public String getMessageId() {
            return messageId;
        }
    }

    public void asyncRequestToRandomMP(KafkaRequest request) {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendToRandomMpTopic();
        sendToKafka(request, kafkaSendToGsTopic, null);
    }

    public void asyncRequestToSpecificMP(KafkaRequest request, long receiverServerId) {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendToSpecificMpTopic();
        sendToKafka(request, kafkaSendToGsTopic, KafkaConfiguration.fromMpServerId(receiverServerId));
    }

    public void asyncRequestToAllGS(KafkaRequest request) {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendInServiceAllTopic();
        sendToKafka(request, kafkaSendToGsTopic, null);
    }

    public void asyncRequestToSpecificGS(KafkaRequest request, long receiverServerId) {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendInServiceSpecificTopic();
        sendToKafka(request, kafkaSendToGsTopic, KafkaConfiguration.fromServerId(receiverServerId));
    }

    /**
     * Pings GS with serverId = receiverServerId
     * @param receiverServerId
     * @throws KafkaHandlerException - most probably Timeout if ping not succeed
     */
    public void tryPingGS(long receiverServerId) throws KafkaHandlerException {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendInServiceSpecificTopic();
        PingRequest request = new PingRequest();
        final String messageId = sendToKafka(request, kafkaSendToGsTopic, KafkaConfiguration.fromServerId(receiverServerId));

        Mono<KafkaResponse> response = getFutureResponse(messageId);
        try {
            convertToType(response, (r) -> new BooleanResponseDto(
                    r.isSuccess(),
                    r.getStatusCode(),
                    r.getReasonPhrases()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<KafkaResponse> syncRequestToSpecificGS(KafkaRequest request, long receiverServerId) {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendInServiceSpecificTopic();
        final String messageId = sendToKafka(request, kafkaSendToGsTopic, KafkaConfiguration.fromServerId(receiverServerId));
        return getFutureResponse(messageId);
    }

    public Mono<KafkaResponse> syncRequestToRandomMP(KafkaRequest request) {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendToRandomMpTopic();
        final String messageId = sendToKafka(request, kafkaSendToGsTopic, null);
        return getFutureResponse(messageId);
    }

    public void asyncRequestToAllMP(KafkaRequest request) {
        String kafkaSendToGsTopic = kafkaProperties.getKafkaSendToAllMpTopic();
        sendToKafka(request, kafkaSendToGsTopic, null);
    }

    private Mono<KafkaResponse> getFutureResponse(final String messageId) {
        final CompletableFuture<KafkaResponse> completableFuture = new CompletableFuture<>();

        Timer timer = new Timer(messageId);
        TimerTask timerTask = new TimerTask() {
            public void run() {
                awaitingCompletableFutures.computeIfPresent(messageId, new BiFunction<String, CompletableFuture<KafkaResponse>, CompletableFuture<KafkaResponse>>() {
                    @Override
                    public CompletableFuture<KafkaResponse> apply(String messageId, CompletableFuture<KafkaResponse> completableFuture) {
                        BasicKafkaResponse timeoutError = VoidKafkaResponse.unknownFailure(String.format("Timeout happened of %d ms", kafkaProperties.getKafkaTimeoutMs()));
                        completableFuture.complete(timeoutError);
                        return null;
                    }
                });
            }
        };

        timer.schedule(timerTask, kafkaProperties.getKafkaTimeoutMs());

        awaitingCompletableFutures.put(messageId, completableFuture);

        completableFuture.thenRunAsync(new Runnable() {
            @Override
            public void run() {
                awaitingCompletableFutures.remove(messageId);
            }
        }, completableFutureExecutorService);
        return Mono.fromCompletionStage(completableFuture);
    }

    private static int getNumPartitions(String topic) throws Exception {
        int numPartitions = producer.partitionsFor(topic).size();
        if (numPartitions == 0) {
            throw new RuntimeException("Partitions count for topic " + topic + " is 0. Unable to handle pub/sub for this topic.");
        }
        return numPartitions;
    }

    public static synchronized int getPartition(String topic, int serverId) {
        Integer partition = topicPartitions.get(new Pair<String, Integer>(topic, serverId));
        if (partition != null) {
            return partition;
        }
        int partitionsNum;
        try {
            partitionsNum = getNumPartitions(topic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get partitions", e);
        }
        partition = serverId - 1; // serverIds start with 1, make it with 0
        partition = partition % partitionsNum;
        topicPartitions.put(new Pair<String, Integer>(topic, serverId), partition);
        LOGGER.debug("Topic - partition, serverId, totalCount: {} - {}, {}, {}", topic, partition, serverId, partitionsNum);
        return partition;
    }

    public static KafkaMessage sendMessageToKafka(String topic, String key, KafkaMessage message, String senderId, String receiverId) {
        List<Header> headers = new ArrayList<Header>();
        headers.add(new RecordHeader(DATA_TYPE, message.getClass().getName().getBytes()));

        if (senderId != null) {
            headers.add(new RecordHeader(SENDER_ID, senderId.getBytes()));
        }

        Integer partition = null;
        if (receiverId != null) {
            headers.add(new RecordHeader(RECEIVER_ID, receiverId.getBytes()));
            int serverId = Integer.parseInt(receiverId.replaceAll("\\D+", ""));
            partition = getPartition(topic, serverId);
        }

        LOGGER.debug("Preparing sending message " + message.getClass() + " to " + topic + " with sender=" + senderId + " receiver=" + receiverId + " partition=" + partition);

        try {
            ObjectMapper mapper = JsonMapper
                    .builder()
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .build();

            // create a producer record
            ProducerRecord<String, JsonNode> producerRecord =
                    new ProducerRecord<>(topic, partition, key, mapper.valueToTree(message), headers);

            // send data - asynchronous
            Future<RecordMetadata> future = producer.send(producerRecord);

            RecordMetadata metadata = future.get(5, TimeUnit.SECONDS);
            LOGGER.debug("Message " + message.getClass() + " sent successfully to " + topic + " to  partition " + metadata.partition() +
                    " with offset " + metadata.offset() + " ; sender=" + senderId + " receiver=" + receiverId);
        } catch (Exception e) {
            LOGGER.error("Error sending message " + message.getClass() + " to " + topic + " :", e);
            throw new KafkaException("Can't send message" + message.getClass() + " to kafka to '" + topic + "' in 5 seconds", e);
        }

        return message;
    }

    private KafkaProducer<String, JsonNode> createProducer(String kafkaHosts) {
        String bootstrapServers = kafkaHosts;

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "0");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "0");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "4096");
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        properties.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "1000");

        // create the producer
        KafkaProducer<String, JsonNode> producer = new KafkaProducer<>(properties);
        return producer;
    }

    private String sendToKafka(KafkaRequest request, String kafkaSendToGsTopic, String receiverId) {
        KafkaProducerTask kafkaProducerTask = new KafkaProducerTask(kafkaProperties, kafkaSendToGsTopic, request, receiverId);
        producerExecutorService.submit(kafkaProducerTask);
        return kafkaProducerTask.getMessageId();
    }
}
