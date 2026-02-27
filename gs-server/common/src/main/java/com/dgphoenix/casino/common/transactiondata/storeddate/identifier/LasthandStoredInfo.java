package com.abs.casino.common.transactiondata.storeddate.identifier;

import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;

/**
 * User: Grien
 * Date: 22.12.2014 12:30
 */
public class LasthandStoredInfo implements StoredItemInfo<LasthandInfo> {
    private long accountId;
    private long gameId;
    private Long bonusId;
    private BonusSystemType bonusSystemType;

    public LasthandStoredInfo(long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType) {
        this.accountId = accountId;
        this.gameId = gameId;
        this.bonusId = bonusId;
        this.bonusSystemType = bonusSystemType;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    public BonusSystemType getBonusSystemType() {
        return bonusSystemType;
    }

    public void setBonusSystemType(BonusSystemType bonusSystemType) {
        this.bonusSystemType = bonusSystemType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LasthandStoredInfo)) return false;

        LasthandStoredInfo that = (LasthandStoredInfo) o;

        if (accountId != that.accountId) return false;
        if (gameId != that.gameId) return false;
        if (bonusId != null ? !bonusId.equals(that.bonusId) : that.bonusId != null) return false;
        if (bonusSystemType != that.bonusSystemType) return false;

        return true;
    }

    public boolean equals(long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType) {
        if (this.accountId != accountId) return false;
        if (this.gameId != gameId) return false;
        if (this.bonusId != null ? !this.bonusId.equals(bonusId) : bonusId != null) return false;
        if (this.bonusSystemType != bonusSystemType) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + (int) (gameId ^ (gameId >>> 32));
        result = 31 * result + (bonusId != null ? bonusId.hashCode() : 0);
        result = 31 * result + (bonusSystemType != null ? bonusSystemType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LasthandStoredInfo");
        sb.append("[accountId=").append(accountId);
        sb.append(", gameId=").append(gameId);
        sb.append(", bonusId=").append(bonusId);
        sb.append(", bonusSystemType=").append(bonusSystemType);
        sb.append(']');
        return sb.toString();
    }
}
