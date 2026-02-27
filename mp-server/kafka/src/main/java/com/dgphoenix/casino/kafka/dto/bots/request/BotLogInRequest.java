package com.abs.casino.kafka.dto.bots.request;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class BotLogInRequest implements KafkaRequest {
    private long botId;
    private String userName;
    private String password;
    private long bankId;
    private long gameId;
    private long buyIn;
    private String botNickname;
    private long roomId;
    private String lang;
    private int gameServerId;
    private String enterLobbyWsUrl;
    private String openRoomWSUrl;
    private long expiresAt;
    private double shootsRate;
    private double bulletsRate;

    public BotLogInRequest() {}

    public BotLogInRequest(long botId,
            String userName,
            String password,
            long bankId,
            long gameId,
            long buyIn,
            String botNickname,
            long roomId,
            String lang,
            int gameServerId,
            String enterLobbyWsUrl,
            String openRoomWSUrl,
            long expiresAt,
            double shootsRate,
            double bulletsRate) {
        super();
        this.botId = botId;
        this.userName = userName;
        this.password = password;
        this.bankId = bankId;
        this.gameId = gameId;
        this.buyIn = buyIn;
        this.botNickname = botNickname;
        this.roomId = roomId;
        this.lang = lang;
        this.gameServerId = gameServerId;
        this.enterLobbyWsUrl = enterLobbyWsUrl;
        this.openRoomWSUrl = openRoomWSUrl;
        this.expiresAt = expiresAt;
        this.shootsRate = shootsRate;
        this.bulletsRate = bulletsRate;
    }

    public long getBotId() {
        return botId;
    }

    public void setBotId(long botId) {
        this.botId = botId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    public String getBotNickname() {
        return botNickname;
    }

    public void setBotNickname(String botNickname) {
        this.botNickname = botNickname;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getGameServerId() {
        return gameServerId;
    }

    public void setGameServerId(int gameServerId) {
        this.gameServerId = gameServerId;
    }

    public String getEnterLobbyWsUrl() {
        return enterLobbyWsUrl;
    }

    public void setEnterLobbyWsUrl(String enterLobbyWsUrl) {
        this.enterLobbyWsUrl = enterLobbyWsUrl;
    }

    public String getOpenRoomWSUrl() {
        return openRoomWSUrl;
    }

    public void setOpenRoomWSUrl(String openRoomWSUrl) {
        this.openRoomWSUrl = openRoomWSUrl;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public double getShootsRate() {
        return shootsRate;
    }

    public void setShootsRate(double shootsRate) {
        this.shootsRate = shootsRate;
    }

    public double getBulletsRate() {
        return bulletsRate;
    }

    public void setBulletsRate(double bulletsRate) {
        this.bulletsRate = bulletsRate;
    }

}
