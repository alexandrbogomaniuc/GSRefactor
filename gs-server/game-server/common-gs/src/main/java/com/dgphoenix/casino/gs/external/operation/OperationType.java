package com.abs.casino.gs.external.operation;

/**
 * Created by ANGeL
 * Date: Oct 13, 2008
 * Time: 1:13:42 PM
 */
public enum OperationType {
    BET(1),
    WIN(2),
    ROLLBACK(3);

    private int value;

    OperationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static OperationType valueOf(int value) {
        for (OperationType operationType : OperationType.values()) {
            if (operationType.getValue() == value) {
                return operationType;
            }
        }
        return null;
    }
}
