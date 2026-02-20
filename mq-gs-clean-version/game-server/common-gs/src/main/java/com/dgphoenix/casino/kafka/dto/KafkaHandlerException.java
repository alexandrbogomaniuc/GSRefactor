package com.dgphoenix.casino.kafka.dto;

public class KafkaHandlerException extends RuntimeException {
    private static final long serialVersionUID = -7408312232089893083L;

    private int code;

    public KafkaHandlerException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
