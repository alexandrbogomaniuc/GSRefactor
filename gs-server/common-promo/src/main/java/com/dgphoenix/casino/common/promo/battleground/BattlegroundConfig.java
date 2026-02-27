package com.abs.casino.common.promo.battleground;

import com.dgphoenix.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.MoreObjects;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 24.06.2021.
 */
public class BattlegroundConfig implements KryoSerializable, JsonSelfSerializable<BattlegroundConfig> {
    private static final byte VERSION = 2;
    private long gameId;
    private String icon;
    private String rulesLink;
    private List<Long> buyInsForDefaultCurrency;
    private Map<String, List<Long>> buyInsByCurrencyMap;
    private List<Long> availableBuyIns;
    private boolean enabled;
    private double rake;
    private int maxNumberPlayers;

    public BattlegroundConfig() {}

    public BattlegroundConfig(long gameId, String icon, String rulesLink, List<Long> buyInsForDefaultCurrency,
                              Map<String, List<Long>> buyInsByCurrencyMap, List<Long> availableBuyIns,
                              boolean enabled, double rake, int maxNumberPlayers) {
        this.gameId = gameId;
        this.icon = icon;
        this.rulesLink = rulesLink;
        this.buyInsForDefaultCurrency = buyInsForDefaultCurrency;
        this.buyInsByCurrencyMap = buyInsByCurrencyMap;
        this.availableBuyIns = availableBuyIns;
        this.enabled = enabled;
        this.rake = rake;
        this.maxNumberPlayers = maxNumberPlayers;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getRulesLink() {
        return rulesLink;
    }

    public void setRulesLink(String rulesLink) {
        this.rulesLink = rulesLink;
    }

    public List<Long> getBuyInsForDefaultCurrency() {
        return buyInsForDefaultCurrency;
    }

    public void setBuyInsForDefaultCurrency(List<Long> buyInsForDefaultCurrency) {
        this.buyInsForDefaultCurrency = buyInsForDefaultCurrency;
    }

    public Map<String, List<Long>> getBuyInsByCurrencyMap() {
        return buyInsByCurrencyMap;
    }

    public void setBuyInsByCurrencyMap(Map<String, List<Long>> buyInsByCurrencyMap) {
        this.buyInsByCurrencyMap = buyInsByCurrencyMap;
    }

    public List<Long> getBuyInsByCurrency(String currency) {
        return buyInsByCurrencyMap == null ? null : buyInsByCurrencyMap.get(currency);
    }

    public List<Long> getAvailableBuyIns() {
        return availableBuyIns;
    }

    public void setAvailableBuyIns(List<Long> availableBuyIns) {
        this.availableBuyIns = availableBuyIns;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getRake() {
        return rake;
    }

    public void setRake(double rake) {
        this.rake = rake;
    }

    public int getMaxNumberPlayers() {
        return maxNumberPlayers;
    }

    public void setMaxNumberPlayers(int maxNumberPlayers) {
        this.maxNumberPlayers = maxNumberPlayers;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(gameId, true);
        output.writeString(icon);
        output.writeString(rulesLink);
        kryo.writeClassAndObject(output, buyInsForDefaultCurrency);
        kryo.writeClassAndObject(output, buyInsByCurrencyMap);
        kryo.writeClassAndObject(output, availableBuyIns);
        output.writeBoolean(enabled);
        output.writeDouble(rake);
        output.writeInt(maxNumberPlayers, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        gameId = input.readLong(true);
        icon = input.readString();
        rulesLink = input.readString();
        buyInsForDefaultCurrency = (List<Long>) kryo.readClassAndObject(input);
        buyInsByCurrencyMap = (Map<String, List<Long>>) kryo.readClassAndObject(input);
        if (ver > 1) {
            availableBuyIns = (List<Long>) kryo.readClassAndObject(input);
            enabled = input.readBoolean();
            rake = input.readDouble();
            maxNumberPlayers = input.readInt(true);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("gameId", gameId);
        gen.writeStringField("icon", icon);
        gen.writeStringField("rulesLink", rulesLink);
        serializeListField(gen, "buyInsForDefaultCurrency", buyInsForDefaultCurrency, new TypeReference<List<Long>>() {});
        serializeMapField(gen, "buyInsByCurrencyMap", buyInsByCurrencyMap, new TypeReference<Map<String, List<Long>>>() {});
        serializeListField(gen, "availableBuyIns", availableBuyIns, new TypeReference<List<Long>>() {});
        gen.writeBooleanField("enabled", enabled);
        gen.writeNumberField("rake", rake);
        gen.writeNumberField("maxNumberPlayers", maxNumberPlayers);
    }

    @Override
    public BattlegroundConfig deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        gameId = node.get("gameId").longValue();
        icon = node.get("icon").textValue();
        rulesLink = node.get("rulesLink").textValue();
        buyInsForDefaultCurrency = om.convertValue(node.get("buyInsForDefaultCurrency"), new TypeReference<List<Long>>() {});
        buyInsByCurrencyMap = om.convertValue(node.get("buyInsByCurrencyMap"), new TypeReference<Map<String, List<Long>>>() {});
        availableBuyIns = om.convertValue(node.get("availableBuyIns"), new TypeReference<List<Long>>() {});
        enabled = node.get("enabled").booleanValue();
        rake = node.get("rake").doubleValue();
        maxNumberPlayers = node.get("maxNumberPlayers").intValue();

        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("gameId", gameId)
                .add("icon", icon)
                .add("rulesLink", rulesLink)
                .add("buyInsForDefaultCurrency", buyInsForDefaultCurrency)
                .add("buyInsByCurrencyMap", buyInsByCurrencyMap)
                .add("availableBuyIns", availableBuyIns)
                .add("enabled", enabled)
                .add("rake", rake)
                .add("maxNumberPlayers", maxNumberPlayers)
                .toString();
    }
}
