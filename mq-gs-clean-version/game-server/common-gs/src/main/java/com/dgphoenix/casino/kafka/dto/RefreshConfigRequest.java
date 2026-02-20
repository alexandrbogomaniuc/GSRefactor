package com.dgphoenix.casino.kafka.dto;

public class RefreshConfigRequest implements KafkaRequest {
    private String configName;
    private String objectId;

    public RefreshConfigRequest() {}

    public RefreshConfigRequest(String configName, String objectId) {
        this.configName = configName;
        this.objectId = objectId;
    }

    public String getConfigName() {
        return configName;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
