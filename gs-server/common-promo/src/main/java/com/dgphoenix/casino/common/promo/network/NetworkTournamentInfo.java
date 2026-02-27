package com.abs.casino.common.promo.network;

import com.dgphoenix.casino.common.promo.EnterType;
import com.dgphoenix.casino.common.promo.LocalizationTitles;
import com.dgphoenix.casino.common.promo.PlayerIdentificationType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;
import java.util.Map;

public class NetworkTournamentInfo implements KryoSerializable {
    private static final byte VERSION = 0;

    private String name;
    private EnterType enterType;
    private long startDate;
    private long endDate;
    private List<Long> bankIds;
    private List<Long> gameIds;
    private List<Long> cmBankIds;
    private String baseCurrency;
    //key is langCode (en, ru, ..)
    private Map<String, LocalizationTitles> localizationTitlesMap;
    //key is bankId, value promoDetail URL
    private Map<Long, String> promoDetailURLs;
    private PlayerIdentificationType playerIdentificationType;

    public NetworkTournamentInfo() {}

    public NetworkTournamentInfo(String name, EnterType enterType, long startDate, long endDate, List<Long> bankIds,
                                 List<Long> gameIds, List<Long> cmBankIds, String baseCurrency,
                                 Map<String, LocalizationTitles> localizationTitlesMap, Map<Long, String> promoDetailURLs,
                                 PlayerIdentificationType playerIdentificationType) {
        this.name = name;
        this.enterType = enterType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bankIds = bankIds;
        this.gameIds = gameIds;
        this.cmBankIds = cmBankIds;
        this.baseCurrency = baseCurrency;
        this.localizationTitlesMap = localizationTitlesMap;
        this.promoDetailURLs = promoDetailURLs;
        this.playerIdentificationType = playerIdentificationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnterType getEnterType() {
        return enterType;
    }

    public void setEnterType(EnterType enterType) {
        this.enterType = enterType;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public List<Long> getBankIds() {
        return bankIds;
    }

    public void setBankIds(List<Long> bankIds) {
        this.bankIds = bankIds;
    }

    public List<Long> getGameIds() {
        return gameIds;
    }

    public void setGameIds(List<Long> gameIds) {
        this.gameIds = gameIds;
    }

    public List<Long> getCmBankIds() {
        return cmBankIds;
    }

    public void setCmBankIds(List<Long> cmBankIds) {
        this.cmBankIds = cmBankIds;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Map<String, LocalizationTitles> getLocalizationTitlesMap() {
        return localizationTitlesMap;
    }

    public void setLocalizationTitlesMap(Map<String, LocalizationTitles> localizationTitlesMap) {
        this.localizationTitlesMap = localizationTitlesMap;
    }

    public Map<Long, String> getPromoDetailURLs() {
        return promoDetailURLs;
    }

    public void setPromoDetailURLs(Map<Long, String> promoDetailURLs) {
        this.promoDetailURLs = promoDetailURLs;
    }

    public PlayerIdentificationType getPlayerIdentificationType() {
        return playerIdentificationType;
    }

    public void setPlayerIdentificationType(PlayerIdentificationType playerIdentificationType) {
        this.playerIdentificationType = playerIdentificationType;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(name);
        output.writeString(enterType.name());
        output.writeLong(startDate);
        output.writeLong(endDate);
        kryo.writeClassAndObject(output, bankIds);
        kryo.writeClassAndObject(output, gameIds);
        kryo.writeClassAndObject(output, cmBankIds);
        output.writeString(baseCurrency);
        kryo.writeClassAndObject(output, localizationTitlesMap);
        kryo.writeClassAndObject(output, promoDetailURLs);
        output.writeString(playerIdentificationType.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        name = input.readString();
        enterType = EnterType.valueOf(input.readString());
        startDate = input.readLong();
        endDate = input.readLong();
        bankIds = (List<Long>) kryo.readClassAndObject(input);
        gameIds = (List<Long>) kryo.readClassAndObject(input);
        cmBankIds = (List<Long>) kryo.readClassAndObject(input);
        baseCurrency = input.readString();
        localizationTitlesMap = (Map<String, LocalizationTitles>) kryo.readClassAndObject(input);
        promoDetailURLs = (Map<Long, String>) kryo.readClassAndObject(input);
        playerIdentificationType = PlayerIdentificationType.valueOf(input.readString());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetworkTournamentInfo [");
        sb.append("name='").append(name).append('\'');
        sb.append(", enterType=").append(enterType);
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", bankIds=").append(bankIds);
        sb.append(", gameIds=").append(gameIds);
        sb.append(", cmBankIds=").append(cmBankIds);
        sb.append(", baseCurrency='").append(baseCurrency).append('\'');
        sb.append(", localizationTitlesMap=").append(localizationTitlesMap);
        sb.append(", promoDetailURLs=").append(promoDetailURLs);
        sb.append(", playerIdentificationType=").append(playerIdentificationType);
        sb.append(']');
        return sb.toString();
    }
}
