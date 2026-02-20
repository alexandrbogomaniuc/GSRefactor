package com.dgphoenix.casino.kafka.dto.bots.request;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class BotStatusRequest implements KafkaRequest {
    private String userName;
    private String password;
    private String botNickName;
    private long bankId;
    private long gameId;

    public BotStatusRequest() {}

    public BotStatusRequest(String userName, String password, String botNickName, long bankId, long gameId) {
        this.userName = userName;
        this.password = password;
        this.botNickName = botNickName;
        this.bankId = bankId;
        this.gameId = gameId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getBotNickName() {
        return botNickName;
    }

    public long getBankId() {
        return bankId;
    }

    public long getGameId() {
        return gameId;
    }
}
