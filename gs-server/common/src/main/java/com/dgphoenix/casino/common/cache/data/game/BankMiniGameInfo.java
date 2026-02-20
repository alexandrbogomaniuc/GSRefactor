package com.dgphoenix.casino.common.cache.data.game;

import com.dgphoenix.casino.common.cache.data.bank.ICoin;
import com.google.common.base.Strings;

import java.util.Collections;
import java.util.List;

public class BankMiniGameInfo extends MiniGameInfo {

    private static final String KEY_BANK = "%d+%d";
    private static final String KEY_BANK_CURRENCY = "%d+%d+%s";

    private final long bankId;
    private final String currencyCode;

    public BankMiniGameInfo(long bankId, String currencyCode, long gameId) {
        super(gameId);
        this.bankId = bankId;
        this.currencyCode = currencyCode;
    }

    public long getBankId() {
        return bankId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getOriginalGameKey() {
        return Strings.isNullOrEmpty(currencyCode) ? String.format(KEY_BANK, bankId, getOriginalGameId()) :
                String.format(KEY_BANK_CURRENCY, bankId, getOriginalGameId(), currencyCode);
    }

    public ImmutableBaseGameInfoWrapper toImmutableBaseGameInfo(BaseGameInfo baseGameInfo) {
        baseGameInfo.setId(getGameId());
        baseGameInfo.setName(getName());
        List<String> languages = Collections.unmodifiableList(baseGameInfo.getLanguages());
        List<ICoin> coins = baseGameInfo.getCoins() == null ? null : Collections.unmodifiableList(baseGameInfo.getCoins());
        return new ImmutableBaseGameInfoWrapper(getGameId(), getName(), baseGameInfo.getBankId(), baseGameInfo, baseGameInfo.isEnabled(),
                baseGameInfo.getProperties(), languages, baseGameInfo.getLimit(), coins);
    }

    @Override
    public String toString() {
        return "BankMiniGameInfo{" +
                "super=" + super.toString() +
                ", bankId=" + bankId +
                ", currencyCode='" + currencyCode + '\'' +
                '}';
    }

}
