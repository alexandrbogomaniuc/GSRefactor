package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ISitOutResponse;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 09.06.17.
 */
public class SitOutResponse extends TObject implements ISitOutResponse {
    private int id;
    private String nickname;
    private long outDate;
    private long compensateSpecialWeapons;
    private long surplusHvBonus;
    private long totalReturnedSpecialWeapons;
    private long nextRoomId = -1;
    private boolean hasNextFrb;

    public SitOutResponse(long date, int rid, int id, String nickname, long outDate,
                          long compensateSpecialWeapons, long surplusHvBonus, long totalReturnedSpecialWeapons,
                          long nextRoomId, boolean hasNextFrb) {
        super(date, rid);
        this.id = id;
        this.nickname = nickname;
        this.outDate = outDate;
        this.compensateSpecialWeapons = compensateSpecialWeapons;
        this.surplusHvBonus = surplusHvBonus;
        this.totalReturnedSpecialWeapons = totalReturnedSpecialWeapons;
        this.nextRoomId = nextRoomId;
        this.hasNextFrb = hasNextFrb;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public long getOutDate() {
        return outDate;
    }

    public void setOutDate(long outDate) {
        this.outDate = outDate;
    }

    @Override
    public long getCompensateSpecialWeapons() {
        return compensateSpecialWeapons;
    }

    public void setCompensateSpecialWeapons(long compensateSpecialWeapons) {
        this.compensateSpecialWeapons = compensateSpecialWeapons;
    }

    @Override
    public long getSurplusHvBonus() {
        return surplusHvBonus;
    }

    public void setSurplusHvBonus(long surplusHvBonus) {
        this.surplusHvBonus = surplusHvBonus;
    }

    @Override
    public long getTotalReturnedSpecialWeapons() {
        return totalReturnedSpecialWeapons;
    }

    public void setTotalReturnedSpecialWeapons(long totalReturnedSpecialWeapons) {
        this.totalReturnedSpecialWeapons = totalReturnedSpecialWeapons;
    }

    public long getNextRoomId() {
        return nextRoomId;
    }

    public void setNextRoomId(long nextRoomId) {
        this.nextRoomId = nextRoomId;
    }

    public boolean isHasNextFrb() {
        return hasNextFrb;
    }

    public void setHasNextFrb(boolean hasNextFrb) {
        this.hasNextFrb = hasNextFrb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SitOutResponse seat = (SitOutResponse) o;

        if (id != seat.id) return false;
        if (outDate != seat.outDate) return false;
        if (compensateSpecialWeapons != seat.compensateSpecialWeapons) return false;
        if (surplusHvBonus != seat.surplusHvBonus) return false;
        return nickname.equals(seat.nickname);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + nickname.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SitOutResponse[" +
                "id=" + id +
                ", rid=" + rid +
                ", date=" + date +
                ", nickname='" + nickname + '\'' +
                ", outDate=" + outDate +
                ", compensateSpecialWeapons=" + compensateSpecialWeapons +
                ", surplusHvBonus=" + surplusHvBonus +
                ", totalReturnedSpecialWeapons=" + totalReturnedSpecialWeapons +
                ", nextRoomId=" + nextRoomId +
                ", hasNextFrb=" + hasNextFrb +
                ']';
    }
}
