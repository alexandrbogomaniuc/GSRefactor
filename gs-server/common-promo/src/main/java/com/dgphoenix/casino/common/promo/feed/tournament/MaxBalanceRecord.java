package com.abs.casino.common.promo.feed.tournament;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("record")
public class MaxBalanceRecord implements ITournamentFeedRecord<MaxBalanceRecord> {
    private int place;
    private long bankId;
    private String playerId;
    private String nickname;
    private String score;
    private String prize;

    public MaxBalanceRecord(int place, long bankId, String playerId, String nickname, String score, String prize) {
        this.place = place;
        this.bankId = bankId;
        this.playerId = playerId;
        this.nickname = nickname;
        this.score = score;
        this.prize = prize;
    }

    @Override
    public String getRank() {
        return String.valueOf(place);
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }

    @Override
    public String getNickName() {
        return nickname;
    }

    @Override
    public String getScoreAsString() {
        return String.valueOf(score);
    }

    @Override
    public int compareTo(MaxBalanceRecord o) {
        return (place < o.place) ? -1 : ((place == o.place) ? 0 : 1);
    }

    public int getPlace() {
        return place;
    }

    public long getBankId() {
        return bankId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getScore() {
        return score;
    }

    public String getPrize() {
        return prize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaxBalanceRecord that = (MaxBalanceRecord) o;

        if (place != that.place) return false;
        if (bankId != that.bankId) return false;
        if (playerId != null ? !playerId.equals(that.playerId) : that.playerId != null) return false;
        if (nickname != null ? !nickname.equals(that.nickname) : that.nickname != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;
        return prize != null ? prize.equals(that.prize) : that.prize == null;
    }

    @Override
    public int hashCode() {
        int result = place;
        result = 31 * result + (int) (bankId ^ (bankId >>> 32));
        result = 31 * result + (playerId != null ? playerId.hashCode() : 0);
        result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (prize != null ? prize.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MaxBalanceRecord[" +
                "place=" + place +
                ", bankId=" + bankId +
                ", playerId='" + playerId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", score='" + score + '\'' +
                ", prize='" + prize + '\'' +
                ']';
    }
}
