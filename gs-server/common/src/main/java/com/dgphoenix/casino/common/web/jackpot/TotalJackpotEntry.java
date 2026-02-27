package com.abs.casino.common.web.jackpot;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * User: plastical
 * Date: 17.08.2010
 */
@XStreamAlias("TotalJackpotEntry")
public class TotalJackpotEntry {
    @XStreamAsAttribute
    private long bankId;
    @XStreamAsAttribute
    private Long gameId;
    @XStreamAsAttribute
    private String currencyCode;
    @XStreamAsAttribute
    private Double totalJackpot;
    @XStreamAsAttribute
    private long betId;

    public TotalJackpotEntry() {
    }

    public TotalJackpotEntry(long bankId) {
        this.bankId = bankId;
    }

    public TotalJackpotEntry(long bankId, Long gameId, String currencyCode) {
        this.bankId = bankId;
        this.gameId = gameId;
        this.currencyCode = currencyCode;
    }

    public TotalJackpotEntry(long bankId, Long gameId, String currencyCode, long betId) {
        this.bankId = bankId;
        this.gameId = gameId;
        this.currencyCode = currencyCode;
        this.betId = betId;
    }

    public boolean isBankTotalJackpot() {
        return gameId == null;
    }

    public boolean isBankTotalJackpotByCurrency() {
        return gameId == -1 && currencyCode != null;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Double getTotalJackpot() {
        return totalJackpot;
    }

    public void setTotalJackpot(Double totalJackpot) {
        this.totalJackpot = totalJackpot;
    }


    public long getBetId() {
        return betId;
    }

    public void setBetId(long betId) {
        this.betId = betId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (bankId ^ (bankId >>> 32));
        result = prime * result + (int) (betId ^ (betId >>> 32));
        result = prime * result
                + ((currencyCode == null) ? 0 : currencyCode.hashCode());
        result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
        result = prime * result
                + ((totalJackpot == null) ? 0 : totalJackpot.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TotalJackpotEntry other = (TotalJackpotEntry) obj;
        if (bankId != other.bankId)
            return false;
        if (betId != other.betId)
            return false;
        if (currencyCode == null) {
            if (other.currencyCode != null)
                return false;
        } else if (!currencyCode.equals(other.currencyCode))
            return false;
        if (gameId == null) {
            if (other.gameId != null)
                return false;
        } else if (!gameId.equals(other.gameId))
            return false;
        if (totalJackpot == null) {
            if (other.totalJackpot != null)
                return false;
        } else if (!totalJackpot.equals(other.totalJackpot))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TotalJackpotEntry [bankId=" + bankId + ", gameId=" + gameId
                + ", currencyCode=" + currencyCode + ", totalJackpot="
                + totalJackpot + ", betId=" + betId + "]";
    }


}
