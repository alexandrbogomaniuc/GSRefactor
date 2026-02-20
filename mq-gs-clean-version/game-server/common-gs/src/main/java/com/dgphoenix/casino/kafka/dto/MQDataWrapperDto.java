package com.dgphoenix.casino.kafka.dto;

public class MQDataWrapperDto extends BasicKafkaResponse {
    private MQDataDto data;

    public MQDataWrapperDto() {
        super();
    }

    public MQDataWrapperDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public MQDataWrapperDto(MQDataDto data, boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
        this.data = data;
    }

    public MQDataDto getData() {
        return data;
    }

    public void setData(MQDataDto data) {
        this.data = data;
    }
}
