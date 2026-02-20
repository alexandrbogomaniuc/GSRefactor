package com.dgphoenix.casino.kafka.dto;

public class NotifyPrivateRoomWasDeactivatedRequest implements KafkaRequest {
    private String privateRoomId;
    private String reason;
    private long bankId;

    public NotifyPrivateRoomWasDeactivatedRequest() {}

    public NotifyPrivateRoomWasDeactivatedRequest(String privateRoomId,
                                                  String reason,
                                                  long bankId) {
        this.privateRoomId = privateRoomId;
        this.reason = reason;
        this.bankId = bankId;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public String getReason() {
        return reason;
    }

    public long getBankId() {
        return bankId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }
}
