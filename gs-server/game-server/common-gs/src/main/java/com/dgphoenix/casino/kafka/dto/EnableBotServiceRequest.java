package com.dgphoenix.casino.kafka.dto;

public class EnableBotServiceRequest implements KafkaRequest {
    private boolean enable;

    public EnableBotServiceRequest(){}

    public EnableBotServiceRequest(boolean enable) {
        this.setEnable(enable);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
