package com.dgphoenix.casino.kafka.dto;

public class VoidKafkaResponse extends BasicKafkaResponse {
    public VoidKafkaResponse() {}

    public VoidKafkaResponse(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public static VoidKafkaResponse success() {
        return new VoidKafkaResponse(true, 0, "");
    }

    public static VoidKafkaResponse failure(int errorCode, String details) {
        return new VoidKafkaResponse(false, errorCode, details);
    }

    public static VoidKafkaResponse unknownFailure(String details) {
        return new VoidKafkaResponse(false, -1, details);
    }

    public static VoidKafkaResponse nullResponse() {
        return new VoidKafkaResponse(false, -100, "Response is null");
    }
}
