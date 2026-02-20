package com.dgphoenix.casino.kafka.config;

public class KafkaProperties {
    private final String kafkaHosts;

    private final String kafkaSendToGsTopic;

    private final String kafkaReplyFromGsTopic;

    private final String kafkaReceiveFromGsTopic;

    private final String kafkaReplyToGsTopic;

    private final String kafkaSendInServiceTopic;

    private final String kafkaReplyInServiceTopic;

    private final String kafkaSendToBotServiceTopic;

    private final String kafkaReplyFromBotServiceTopic;

    private final String kafkaClientId;

    private final Integer kafkaClientIdNum;

    private final Long kafkaTimeoutMs;


    public KafkaProperties(String kafkaHosts,
            String kafkaSendToGsTopic,
            String kafkaReplyFromGsTopic,
            String kafkaReceiveFromGsTopic,
            String kafkaReplyToGsTopic,
            String kafkaSendInServiceTopic,
            String kafkaReplyInServiceTopic,
            String kafkaSendToBotServiceTopic,
            String kafkaReplyFromBotServiceTopic,
            String kafkaClientId,
            Integer kafkaClientIdNum,
            Long kafkaTimeoutMs) {
        this.kafkaHosts = kafkaHosts;
        this.kafkaSendToGsTopic = kafkaSendToGsTopic;
        this.kafkaReplyFromGsTopic = kafkaReplyFromGsTopic;
        this.kafkaReceiveFromGsTopic = kafkaReceiveFromGsTopic;
        this.kafkaReplyToGsTopic = kafkaReplyToGsTopic;
        this.kafkaSendInServiceTopic = kafkaSendInServiceTopic;
        this.kafkaReplyInServiceTopic = kafkaReplyInServiceTopic;
        this.kafkaSendToBotServiceTopic = kafkaSendToBotServiceTopic;
        this.kafkaReplyFromBotServiceTopic = kafkaReplyFromBotServiceTopic;
        this.kafkaClientIdNum = kafkaClientIdNum;
        this.kafkaClientId = kafkaClientId;
        this.kafkaTimeoutMs = kafkaTimeoutMs;
    }


    public String getKafkaHosts() {
        return kafkaHosts;
    }

    public String getKafkaSendToRandomGsTopic() {
        return kafkaSendToGsTopic;
    }

    public String getKafkaSendToSpecificGsTopic() {
        return kafkaSendToGsTopic + "_spk";
    }

    public String getKafkaReplyFromGsTopic() {
        return kafkaReplyFromGsTopic;
    }

    public String getKafkaReceiveFromRandomGsTopic() {
        return kafkaReceiveFromGsTopic;
    }

    public String getKafkaReceiveFromSpecificGsTopic() {
        return kafkaReceiveFromGsTopic + "_spk";
    }

    public String getKafkaReceiveFromAllGsTopic() {
        return kafkaReceiveFromGsTopic + "_all";
    }

    public String getKafkaReplyToGsTopic() {
        return kafkaReplyToGsTopic;
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

    public String getKafkaSendToBotServiceTopic() {
        return kafkaSendToBotServiceTopic;
    }

    public String getKafkaReplyFromBotServiceTopic() {
        return kafkaReplyFromBotServiceTopic;
    }

    public String getKafkaClientId() {
        return kafkaClientId;
    }

    public Integer getKafkaClientIdNum() {
        return kafkaClientIdNum;
    }

    public Long getKafkaTimeoutMs() {
        return kafkaTimeoutMs;
    }

}
