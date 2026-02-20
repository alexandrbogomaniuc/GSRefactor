package com.dgphoenix.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: flsh
 * Date: 24.11.16.
 */
public abstract class AbstractParticipantEvent<T extends IParticipantEvent> implements IParticipantEvent<T> {
    protected long gameId;
    protected long eventDate;
    protected long accountId;
    protected String accountExternalId;
    protected TournamentMemberRank tournamentMemberRank;
    protected Long roundId;
    protected Set<PromoType> wonPromoTypes;

    public AbstractParticipantEvent() {
    }

    public AbstractParticipantEvent(long gameId, long eventDate, long accountId, String accountExternalId) {
        this.gameId = gameId;
        this.eventDate = eventDate;
        this.accountId = accountId;
        this.accountExternalId = accountExternalId;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    @Override
    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getAccountExternalId() {
        return accountExternalId;
    }

    public void setAccountExternalId(String accountExternalId) {
        this.accountExternalId = accountExternalId;
    }

    public TournamentMemberRank getTournamentMemberRank() {
        return tournamentMemberRank;
    }

    public void setTournamentMemberRank(TournamentMemberRank tournamentMemberRank) {
        this.tournamentMemberRank = tournamentMemberRank;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Set<PromoType> getWonPromoTypes() {
        return wonPromoTypes;
    }

    public void addWonPromoType(PromoType promoType) {
        if (wonPromoTypes == null) {
            wonPromoTypes = new HashSet<PromoType>();
        }
        wonPromoTypes.add(promoType);
    }

    public boolean hasWonPromoType(PromoType promoType) {
        return wonPromoTypes != null && wonPromoTypes.contains(promoType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractParticipantEvent that = (AbstractParticipantEvent) o;

        if (gameId != that.gameId) return false;
        if (eventDate != that.eventDate) return false;
        return accountId == that.accountId;
    }

    @Override
    public int hashCode() {
        int result = (int) (gameId ^ (gameId >>> 32));
        result = 31 * result + (int) (eventDate ^ (eventDate >>> 32));
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                "gameId=" + gameId +
                ", eventDate=" + eventDate +
                ", accountId=" + accountId +
                ", accountExternalId='" + accountExternalId + '\'' +
                ", tournamentMemberRank='" + tournamentMemberRank + '\'' +
                ", roundId='" + roundId + '\'' +
                ", wonPromoTypes=" + wonPromoTypes +
                ']';
    }

    public void baseSerialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("gameId", gameId);
        gen.writeNumberField("eventDate", eventDate);
        gen.writeNumberField("accountId", accountId);
        gen.writeStringField("accountExternalId", accountExternalId);
        gen.writeObjectField("tournamentMemberRank", tournamentMemberRank);
        serializeNumberOrNull(gen, "roundId", roundId);
        serializeSetField(gen, "wonPromoTypes", wonPromoTypes, new TypeReference<Set<PromoType>>() {});
    }

    public void baseDeserialize(JsonParser p, JsonNode node, DeserializationContext ctxt) throws JsonProcessingException, IllegalArgumentException {
        gameId = node.get("gameId").longValue();
        eventDate = node.get("eventDate").longValue();
        accountId = node.get("accountId").longValue();
        accountExternalId = readNullableText(node, "accountExternalId");
        tournamentMemberRank = ((ObjectMapper) p.getCodec()).convertValue(node.get("tournamentMemberRank"), new TypeReference<TournamentMemberRank>() {});
        roundId = deserializeOrNull((ObjectMapper) p.getCodec(), node, Long.class);
        wonPromoTypes = ((ObjectMapper) p.getCodec()).convertValue(node.get("wonPromoTypes"), new TypeReference<Set<PromoType>>() {});
    }

    public void baseWrite(Kryo kryo, Output output) {
        output.writeLong(gameId, true);
        output.writeLong(eventDate, true);
        output.writeLong(accountId, true);
        output.writeString(accountExternalId);
        kryo.writeClassAndObject(output, tournamentMemberRank);
        kryo.writeObjectOrNull(output, roundId, Long.class);
        kryo.writeClassAndObject(output, wonPromoTypes);
    }

    public void baseRead(Kryo kryo, Input input) {
        gameId = input.readLong(true);
        eventDate = input.readLong(true);
        accountId = input.readLong(true);
        accountExternalId = input.readString();
    }

    public void baseReadV1(Kryo kryo, Input input) {
        tournamentMemberRank = (TournamentMemberRank) kryo.readClassAndObject(input);
        roundId = kryo.readObjectOrNull(input, Long.class);
    }

    public void baseReadV2(Kryo kryo, Input input) {
        wonPromoTypes = (Set<PromoType>) kryo.readClassAndObject(input);
    }
}
