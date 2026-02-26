package com.abs.casino.promo.win;

import java.util.Objects;

public class PromoWin {
    private final long promoId;
    private final long timeWin;
    private final long accountId;
    private final long gameSessionId;
    private final long bankId;
    private final long gameId;
    private final long amount;
    private final long amountInPlayerCurrency;
    private final String transferStatus;

    public PromoWin(long promoId, long timeWin, long accountId, long gameSessionId, long bankId, long gameId, long amount, long amountInPlayerCurrency, String transferStatus) {
        this.promoId = promoId;
        this.timeWin = timeWin;
        this.accountId = accountId;
        this.gameSessionId = gameSessionId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.amount = amount;
        this.amountInPlayerCurrency = amountInPlayerCurrency;
        this.transferStatus = transferStatus;
    }

    public long getPromoId() {
        return promoId;
    }

    public long getTimeWin() {
        return timeWin;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getBankId() {
        return bankId;
    }

    public long getGameId() {
        return gameId;
    }

    public long getAmount() {
        return amount;
    }

    public long getAmountInPlayerCurrency() {
        return amountInPlayerCurrency;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromoWin promoWin = (PromoWin) o;
        return promoId == promoWin.promoId && timeWin == promoWin.timeWin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(promoId, timeWin);
    }

    @Override
    public String toString() {
        return "PromoWin{" +
                "promoId=" + promoId +
                ", timeWin=" + timeWin +
                ", accountId=" + accountId +
                ", gameSessionId=" + gameSessionId +
                ", bankId=" + bankId +
                ", gameId=" + gameId +
                ", amount=" + amount +
                ", amountInPlayerCurrency=" + amountInPlayerCurrency +
                ", transferStatus='" + transferStatus + '\'' +
                '}';
    }
}
