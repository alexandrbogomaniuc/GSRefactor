package com.abs.casino.kafka.config;

public class KafkaProperties {
    private final String kafkaHosts;

    private final String kafkaSendToMpTopic;

    private final String kafkaReplyFromMpTopic;

    private final String kafkaReceiveFromMpTopic;

    private final String kafkaReplyToMpTopic;

    private final String kafkaSendInServiceTopic;

    private final String kafkaReplyInServiceTopic;

    private final String kafkaClientId;

    private final Long kafkaClientIdNum;

    private final Long kafkaTimeoutMs;

    public KafkaProperties(String kafkaHosts,
            String kafkaSendToMpTopic,
            String kafkaReplyFromMpTopic,
            String kafkaReceiveFromMpTopic,
            String kafkaReplyToMpTopic,
            String kafkaSendInServiceTopic,
            String kafkaReplyInServiceTopic,
            String kafkaClientId,
            Long kafkaClientIdNum,
            Long kafkaTimeoutMs) {
        this.kafkaHosts = kafkaHosts;
        this.kafkaSendToMpTopic = kafkaSendToMpTopic;
        this.kafkaReplyFromMpTopic = kafkaReplyFromMpTopic;
        this.kafkaReceiveFromMpTopic = kafkaReceiveFromMpTopic;
        this.kafkaReplyToMpTopic = kafkaReplyToMpTopic;
        this.kafkaSendInServiceTopic = kafkaSendInServiceTopic;
        this.kafkaReplyInServiceTopic = kafkaReplyInServiceTopic;
        this.kafkaClientId = kafkaClientId;
        this.kafkaClientIdNum = kafkaClientIdNum;
        this.kafkaTimeoutMs = kafkaTimeoutMs;
    }

    public String getKafkaHosts() {
        return kafkaHosts;
    }

    public String getKafkaSendToRandomMpTopic() {
        return kafkaSendToMpTopic;
    }

    public String getKafkaSendToSpecificMpTopic() {
        return kafkaSendToMpTopic + "_spk";
    }

    public String getKafkaSendToAllMpTopic() {
        return kafkaSendToMpTopic + "_all";
    }

    public String getKafkaReplyFromMpTopic() {
        return kafkaReplyFromMpTopic;
    }

    public String getKafkaReceiveFromRandomMpTopic() {
        return kafkaReceiveFromMpTopic;
    }

    public String getKafkaReceiveFromSpecificMpTopic() {
        return kafkaReceiveFromMpTopic + "_spk";
    }

    public String getKafkaReplyToMpTopic() {
        return kafkaReplyToMpTopic;
    }

    public String getKafkaSendInServiceAllTopic() {
        return kafkaSendInServiceTopic;
    }

    public String getKafkaSendInServiceSpecificTopic() {
        return kafkaSendInServiceTopic + "_spk";
    }

    public String getKafkaReplyInServiceTopic() {
        return kafkaReplyInServiceTopic;
    }

    public String getKafkaClientId() {
        return kafkaClientId;
    }

    public Long getKafkaClientIdNum() {
        return kafkaClientIdNum;
    }

    public Long getKafkaTimeoutMs() {
        return kafkaTimeoutMs;
    }
}
