package com.abs.casino.common.cache.data.bonus;

import com.abs.casino.common.cache.JsonSelfSerializable;
import com.abs.casino.common.cache.VersionedDistributedCacheEntry;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseBonus<BB extends BaseBonus> extends VersionedDistributedCacheEntry 
    implements KryoSerializable, JsonSelfSerializable<BB> {

    private static final Logger LOG = LogManager.getLogger(BaseBonus.class);

    protected long id;
    protected long accountId;
    protected long bankId;
    protected String extId;
    protected String description;
    protected String comment;
    protected List<Long> gameIds;
    protected long betSum;
    protected long timeAwarded;
    protected Long endTime = null;
    protected BonusStatus status;
    protected boolean internal = false;
    protected Long massAwardId = null;
    protected long lastUpdateDate;

    public abstract Collection<Long> getValidGameIds(Collection<Long> fullList);

    protected BaseBonus() {
    }

    protected BaseBonus(long id, long accountId, long bankId, String extId, String description, String comment,
                        List<Long> gameIds, long betSum, long timeAwarded, Long endTime, BonusStatus status,
                        boolean internal, Long massAwardId) {
        this.id = id;
        this.accountId = accountId;
        this.bankId = bankId;
        this.extId = extId;
        this.description = description;
        this.comment = comment;
        this.gameIds = gameIds;
        this.betSum = betSum;
        this.timeAwarded = timeAwarded;
        this.endTime = endTime;
        this.status = status;
        this.internal = internal;
        this.massAwardId = massAwardId;
    }

    public Long getMassAwardId() {
        return massAwardId;
    }

    public synchronized void setMassAwardId(long massAwardId) {
        this.massAwardId = massAwardId;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setStatusAndEndTime(BonusStatus status, Long endTime) {
        this.status = status;
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getId() {
        return id;
    }

    public synchronized void setId(long id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public List<Long> getGameIds() {
        if (gameIds == null) {
            gameIds = new ArrayList<Long>();
        }
        return gameIds;
    }

    public void setGameIds(List<Long> gameIds) {
        this.gameIds = gameIds;
    }

    public boolean addGameIdWithCheck(long gameId) {
        List<Long> ids = getGameIds();
        if (ids != null && !ids.contains(gameId)) {
            ids.add(gameId);
            return true;
        }
        return false;
    }

    public long getBetSum() {
        return betSum;
    }

    public void setBetSum(long betSum) {
        this.betSum = betSum;
    }

    public long getTimeAwarded() {
        return timeAwarded;
    }

    public void setTimeAwarded(long timeAwarded) {
        this.timeAwarded = timeAwarded;
    }

    public BonusStatus getStatus() {
        return status;
    }

    public void setStatus(BonusStatus status) {
        this.status = status;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public synchronized void incrementBetSum(long delta) throws CommonException {
        LOG.debug("incrementBetSum bonusId:" + getId() + " currentBalance:" +
                getBetSum() + " delta:" + delta + " thread:" + Thread.currentThread().getId());
        this.betSum += delta;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(id, true);
        output.writeLong(accountId, true);
        output.writeLong(bankId, true);
        output.writeString(extId);
        output.writeString(description);
        output.writeString(comment);
        output.writeString(CollectionUtils.listOfLongsToString(gameIds));
        output.writeLong(betSum, true);
        output.writeLong(timeAwarded, true);
        kryo.writeObjectOrNull(output, endTime, Long.class);
        output.writeString(status.name());
        output.writeBoolean(internal);
        kryo.writeObjectOrNull(output, massAwardId, Long.class);
        output.writeLong(lastUpdateDate, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readLong(true);
        accountId = input.readLong(true);
        bankId = input.readLong(true);
        extId = input.readString();
        description = input.readString();
        comment = input.readString();
        gameIds = CollectionUtils.stringToListOfLongs(input.readString());
        betSum = input.readLong(true);
        timeAwarded = input.readLong(true);
        endTime = kryo.readObjectOrNull(input, Long.class);
        status = BonusStatus.valueOf(input.readString());
        internal = input.readBoolean();
        massAwardId = kryo.readObjectOrNull(input, Long.class);
        lastUpdateDate = input.readLong(true);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("id", id);
        gen.writeNumberField("accountId", accountId);
        gen.writeNumberField("bankId", bankId);
        gen.writeStringField("extId", extId);
        gen.writeStringField("description", description);
        gen.writeStringField("comment", comment);
        gen.writeStringField("gameIds", CollectionUtils.listOfLongsToString(gameIds));
        gen.writeNumberField("betSum", betSum);
        gen.writeNumberField("timeAwarded", timeAwarded);
        serializeNumberOrNull(gen, "endTime", endTime);
        gen.writeStringField("status", status.name());
        gen.writeBooleanField("internal", internal);
        serializeNumberOrNull(gen, "massAwardId", massAwardId);
        gen.writeNumberField("lastUpdateDate", lastUpdateDate);

        serializeAdditional(gen, serializers);
    }

    @Override
    public BB deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        id = node.get("id").longValue();
        accountId = node.get("accountId").longValue();
        bankId = node.get("bankId").longValue();
        extId = readNullableText(node, "extId");
        description = readNullableText(node, "description");
        comment = readNullableText(node, "comment");
        gameIds = CollectionUtils.stringToListOfLongs(node.get("gameIds").textValue());
        betSum = node.get("betSum").asLong();
        timeAwarded = node.get("timeAwarded").longValue();
        endTime = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("endTime"), Long.class);
        status = BonusStatus.valueOf(readNullableText(node, "status"));
        internal = node.get("internal").booleanValue();
        massAwardId = deserializeOrNull((ObjectMapper)p.getCodec(), node.get("massAwardId"), Long.class);
        lastUpdateDate = node.get("lastUpdateDate").longValue();

        deserializeAdditional(p, node, ctxt);

        return getDeserialize();
    }

    protected abstract void serializeAdditional(JsonGenerator gen, SerializerProvider serializers) throws IOException;
    protected abstract void deserializeAdditional(JsonParser p, JsonNode node, DeserializationContext ctxt) throws IOException;
    protected abstract BB getDeserialize();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseBonus [");
        sb.append("id=").append(id);
        sb.append(", accountId=").append(accountId);
        sb.append(", bankId=").append(bankId);
        sb.append(", extId='").append(extId).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", gameIds=").append(gameIds);
        sb.append(", betSum=").append(betSum);
        sb.append(", timeAwarded=").append(timeAwarded);
        sb.append(", endTime=").append(endTime);
        sb.append(", status=").append(status);
        sb.append(", internal=").append(internal);
        sb.append(", massAwardId=").append(massAwardId);
        sb.append(']');
        return sb.toString();
    }
}
