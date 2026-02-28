package com.abs.casino.kafka.dto;

public class MQQuestAmountDto {
    private int fromAmount;
    private int toAmount;

    public MQQuestAmountDto() {
    }

    public MQQuestAmountDto(int fromAmount, int toAmount) {
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
    }

    public int getFromAmount() {
        return fromAmount;
    }

    public int getToAmount() {
        return toAmount;
    }

    public void setFromAmount(int fromAmount) {
        this.fromAmount = fromAmount;
    }

    public void setToAmount(int toAmount) {
        this.toAmount = toAmount;
    }
}
