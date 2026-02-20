package com.betsoft.casino.mp.model.bots.dto;

public class SimpleBot {
    private String id;
    private String nickname;
    private long roomId;
    private int bankId;
    private long gameId;
    private int serverId;
    private String token;
    private String sid;
    private String url;
    private long expiresAt;
    /**
     * 
     * @see BotState
     */
    public BotState botState;
    public SimpleBot roomBot;

    public SimpleBot() {
        super();
    }

    public SimpleBot(String id,
            String nickname,
            long roomId,
            int bankId,
            long gameId,
            int serverId,
            String token,
            String sid,
            String url,
            long expiresAt,
            BotState botState,
            SimpleBot roomBot) {
        super();
        this.id = id;
        this.nickname = nickname;
        this.roomId = roomId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.serverId = serverId;
        this.token = token;
        this.sid = sid;
        this.url = url;
        this.expiresAt = expiresAt;
        this.botState = botState;
        this.roomBot = roomBot;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public SimpleBot getRoomBot() {
        return roomBot;
    }

    public void setRoomBot(SimpleBot roomBot) {
        this.roomBot = roomBot;
    }
}
