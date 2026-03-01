package com.abs.casino.common.cache.data.bonus;

import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * User: flsh
 * Date: 11.07.13
 */
public abstract class MassAwardBonusTemplate implements Serializable, KryoSerializable {

    protected Long startDate = null;
    protected Long expirationDate = null;

    protected String description;
    protected String comment;

    protected List<Long> gameIds;

    protected long timeAwarded;

    private Long registeredFrom;

    private String countryCode;

    public MassAwardBonusTemplate() {}

    protected MassAwardBonusTemplate(Long startDate, Long expirationDate, String description, String comment,
                                     List<Long> gameIds, long timeAwarded) {
        this.startDate = startDate;
        this.expirationDate = expirationDate;
        this.description = description;
        this.comment = comment;
        this.gameIds = gameIds;
        this.timeAwarded = timeAwarded;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Long getStartDate() {
        return startDate;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public String getDescription() {
        return description;
    }

    public String getComment() {
        return comment;
    }

    public List<Long> getGameIds() {
        return gameIds;
    }

    public long getTimeAwarded() {
        return timeAwarded;
    }

    public Long getRegisteredFrom() {
        return registeredFrom;
    }

    public void setRegisteredFrom(Long registeredFrom) {
        this.registeredFrom = registeredFrom;
    }

    abstract public BaseBonus createBonus(long bonusId, long accountId, long bankId, long massAwardId, Long maxWinLimit,
                                          Double maxWinMultiplier);

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MassAwardBonusTemplate");
        sb.append("[startDate=").append(startDate == null ? "null" : new Date(startDate));
        sb.append(", expirationDate=").append(expirationDate == null ? "null" : new Date(expirationDate));
        sb.append(", description=").append(description);
        sb.append(", comment=").append(comment);
        sb.append(", gameIds=").append(gameIds);
        sb.append(", timeAwarded=").append(new Date(timeAwarded));
        sb.append(", registeredFrom=").append(registeredFrom);
        sb.append(", countryCode=").append(countryCode);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObjectOrNull(output, startDate, Long.class);
        kryo.writeObjectOrNull(output, expirationDate, Long.class);
        output.writeString(description);
        output.writeString(comment);
        output.writeString(CollectionUtils.listOfLongsToString(gameIds));
        output.writeLong(timeAwarded, true);
        kryo.writeObjectOrNull(output, registeredFrom, Long.class);
        output.writeString(countryCode);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        startDate = kryo.readObjectOrNull(input, Long.class);
        expirationDate = kryo.readObjectOrNull(input, Long.class);
        description = input.readString();
        comment = input.readString();
        gameIds = CollectionUtils.stringToListOfLongs(input.readString());
        timeAwarded = input.readLong(true);
        registeredFrom = kryo.readObjectOrNull(input, Long.class);
        countryCode = input.readString();
    }
}
