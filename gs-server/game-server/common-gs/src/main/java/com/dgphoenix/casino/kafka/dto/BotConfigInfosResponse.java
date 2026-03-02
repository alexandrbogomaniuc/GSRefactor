package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.BotConfigInfoDto;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

import java.util.List;

public class BotConfigInfosResponse extends BasicKafkaResponse {
    private List<BotConfigInfoDto> list;

    public BotConfigInfosResponse() {}

    public BotConfigInfosResponse(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public BotConfigInfosResponse(List<BotConfigInfoDto> list) {
        super(true, 0, "");
        this.list = list;
    }

    public List<BotConfigInfoDto> getList() {
        return list;
    }

    public void setList(List<BotConfigInfoDto> list) {
        this.list = list;
    }
}
