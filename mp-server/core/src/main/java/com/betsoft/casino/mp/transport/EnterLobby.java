package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 06.06.17.
 */
public class EnterLobby extends TInboundObject {
    private String sid;
    private String lang;
    private int serverId;
    private String mode;
    private boolean noFRB;
    private int gameId;
    private Long tournamentId;
    private Long bonusId;
    private long battlegroundBuyIn;
    private boolean privateRoom;
    private boolean continueIncompleteRound;

    public EnterLobby(long date, String sid, String lang, int rid, int serverId, String mode, boolean noFRB, int gameId,
                      long battlegroundBuyIn, boolean privateRoom, boolean continueIncompleteRound) {
        super(date, rid);
        this.sid = sid;
        this.lang = lang;
        this.serverId = serverId;
        this.mode = mode;
        this.noFRB = noFRB;
        this.gameId = gameId;
        this.battlegroundBuyIn = battlegroundBuyIn;
        this.privateRoom = privateRoom;
        this.continueIncompleteRound = continueIncompleteRound;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isNoFRB() {
        return noFRB;
    }

    public void setNoFRB(boolean noFRB) {
        this.noFRB = noFRB;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    public Long getBattlegroundBuyIn() {
        return battlegroundBuyIn;
    }

    public void setBattlegroundBuyIn(Long battlegroundBuyIn) {
        this.battlegroundBuyIn = battlegroundBuyIn;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    public boolean isContinueIncompleteRound() {
        return continueIncompleteRound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnterLobby that = (EnterLobby) o;

        if (rid != that.rid) return false;
        return sid.equals(that.sid);

    }

    @Override
    public int hashCode() {
        int result = sid.hashCode();
        result = 31 * result + rid;
        return result;
    }

    @Override
    public String toString() {
        return "EnterLobby{" +
                "date=" + date +
                ", rid=" + rid +
                ", sid='" + sid + '\'' +
                ", lang='" + lang + '\'' +
                ", serverId=" + serverId +
                ", mode='" + mode + '\'' +
                ", noFRB=" + noFRB +
                ", gameId=" + gameId +
                ", tournamentId=" + tournamentId +
                ", bonusId=" + bonusId +
                ", battlegroundBuyIn=" + battlegroundBuyIn +
                ", privateRoom=" + privateRoom +
                ", continueIncompleteRound=" + continueIncompleteRound +
                '}';
    }
}
