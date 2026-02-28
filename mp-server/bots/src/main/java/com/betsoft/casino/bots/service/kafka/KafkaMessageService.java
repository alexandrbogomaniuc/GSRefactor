package com.betsoft.casino.bots.service.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.kafka.dto.KafkaMessage;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;


public class KafkaMessageService {
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String SENDER_ID = "SENDER_ID";
    private static final String RECEIVER_ID = "RECEIVER_ID";

    private static final Logger LOGGER = LogManager.getLogger(KafkaMessageService.class);

    private KafkaProducer<String, JsonNode> producer;

    private ConcurrentHashMap<Pair<String, Integer>, Integer> topicPartitions = new ConcurrentHashMap<Pair<String, Integer>, Integer>();

    private final String kafkaHost;

    public KafkaMessageService(String kafkaHost,
                               Integer kafkaPort) {
        this.kafkaHost = String.format("%s:%d", kafkaHost, kafkaPort);
        this.producer = createProducer(this.kafkaHost);
    }

    private int getNumPartitions(String topic) throws Exception {
        int numPartitions = producer.partitionsFor(topic).size();
        if (numPartitions == 0) {
            throw new RuntimeException("Partitions count for topic " + topic + " is 0. Unable to handle pub/sub for this topic.");
        }
        return numPartitions;
    }

    public synchronized int getPartition(String topic, int serverId) {
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

    public KafkaMessage sendMessageToKafka(String topic, String key, KafkaMessage message, String senderId, String receiverId) {
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
}

