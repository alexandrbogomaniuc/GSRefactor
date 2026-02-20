package com.dgphoenix.casino.kafka.dto;

public class SitInRequest implements KafkaRequest {
    private String sessionId;
    private long gameId;
    private String mode;
    private String lang;
    private long bonusId;
    private long oldGameSessionId;
    private long oldRoundId;
    private long roomId;
    private int betNumber;
    private long tournamentId;
    private String nickname;

    public SitInRequest() {}

    public SitInRequest(String sessionId,
            long gameId,
            String mode,
            String lang,
            long bonusId,
            long oldGameSessionId,
            long oldRoundId,
            long roomId,
            int betNumber,
            long tournamentId,
            String nickname) {
        super();
        this.sessionId = sessionId;
        this.gameId = gameId;
        this.mode = mode;
        this.lang = lang;
        this.bonusId = bonusId;
        this.oldGameSessionId = oldGameSessionId;
        this.oldRoundId = oldRoundId;
        this.roomId = roomId;
        this.betNumber = betNumber;
        this.tournamentId = tournamentId;
        this.nickname = nickname;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getGameId() {
        return gameId;
    }

    public String getMode() {
        return mode;
    }

    public String getLang() {
        return lang;
    }

    public long getBonusId() {
        return bonusId;
    }

    public long getOldGameSessionId() {
        return oldGameSessionId;
    }

    public long getOldRoundId() {
        return oldRoundId;
    }

    public long getRoomId() {
        return roomId;
    }

    public int getBetNumber() {
        return betNumber;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public void setOldGameSessionId(long oldGameSessionId) {
        this.oldGameSessionId = oldGameSessionId;
    }

    public void setOldRoundId(long oldRoundId) {
        this.oldRoundId = oldRoundId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setBetNumber(int betNumber) {
        this.betNumber = betNumber;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
