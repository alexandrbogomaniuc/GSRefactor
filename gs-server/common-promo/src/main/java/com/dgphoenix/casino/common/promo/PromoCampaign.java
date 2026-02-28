package com.dgphoenix.casino.common.promo;

import com.abs.casino.common.util.DatePeriod;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 21.11.16.
 */
public class PromoCampaign implements IPromoCampaign {
    private static final byte VERSION = 0;
    private long id;
    private IPromoTemplate template;
    private String name;
    private EnterType enterType;
    private DatePeriod period;
    private Status status;
    private Set<Long> bankIds;
    //gameIds may be null for all games
    private Set<Long> gameIds;
    //this currency used for all money amounts may be defined in template,prizePool,prizeQualifiers. cannot be null
    private String baseCurrency;
    //key is bankId
    private Map<Long, String> promoDetailURLs;
    private PlayerIdentificationType playerIdentificationType;

    private PromoCampaign() {
    }

    public PromoCampaign(long id, IPromoTemplate template, String name, EnterType enterType, DatePeriod period,
                         Status status, Set<Long> bankIds, Set<Long> gameIds, String baseCurrency,
                         Map<Long, String> promoDetailURLs, PlayerIdentificationType playerIdentificationType) {
        this.id = id;
        this.template = template;
        this.name = name;
        this.enterType = enterType;
        this.period = period;
        this.status = status;
        this.bankIds = new HashSet<Long>(bankIds);
        this.gameIds = gameIds != null ? new HashSet<Long>(gameIds) : null;
        this.baseCurrency = baseCurrency;
        this.promoDetailURLs = promoDetailURLs;
        this.playerIdentificationType = playerIdentificationType;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public IPromoTemplate getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EnterType getEnterType() {
        return enterType;
    }

    @Override
    public DatePeriod getActionPeriod() {
        return period;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Set<IPrize> getPrizePool() {
        return template.getPrizePool();
    }

    @Override
    public IPrize getPrize(long prizeId) {
        if (prizeId <= 0) {
            throw new RuntimeException("illegal prizeId value=" + prizeId);
        }
        Set<IPrize> prizePool = template.getPrizePool();
        for (IPrize prize : prizePool) {
            if (prize.getId() == prizeId) {
                return prize;
            }
        }
        return null;
    }

    public DatePeriod getPeriod() {
        return period;
    }

    @Override
    public Set<Long> getBankIds() {
        return bankIds;
    }

    @Override
    public Set<Long> getGameIds() {
        return gameIds;
    }

    public boolean isAppliedToGame(long gameId) {
        return gameIds == null || gameIds.contains(gameId);
    }

    @Override
    public boolean isActual(long gameId) {
        return isAppliedToGame(gameId) && Status.STARTED.equals(status) && period.isDateBetween(new Date());
    }

    @Override
    public boolean showNotifications(long gameId) {
        return isActual(gameId);
    }

    @Override
    public String getBaseCurrency() {
        return baseCurrency;
    }

    @Override
    public Map<Long, String> getPromoDetailURLs() {
        return promoDetailURLs;
    }

    @Override
    public String getPromoDetailURL(Long bankId) {
        return promoDetailURLs == null ? null : promoDetailURLs.get(bankId);
    }

    @Override
    public PlayerIdentificationType getPlayerIdentificationType() {
        return playerIdentificationType;
    }

    @Override
    public void setPlayerIdentificationType(PlayerIdentificationType playerIdentificationType) {
        this.playerIdentificationType = playerIdentificationType;
    }

    @Override
    public boolean isNetworkPromoCampaign() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromoCampaign that = (PromoCampaign) o;
        if (id != that.id) return false;
        if (template.getClass() != that.template.getClass()) return false;
        return period.equals(that.period);
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "PromoCampaign[" +
                "id=" + id +
                ", template=" + template +
                ", name='" + name + '\'' +
                ", enterType=" + enterType +
                ", period=" + period +
                ", status=" + status +
                ", baseCurrency=" + baseCurrency +
                ", bankIds=" + bankIds +
                ", gameIds=" + gameIds +
                ", playerIdentificationType=" + playerIdentificationType +
                ", promoDetailURLs=" + promoDetailURLs +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        kryo.writeClassAndObject(output, template);
        output.writeString(name);
        output.writeString(enterType.name());
        kryo.writeObject(output, period);
        output.writeString(status.name());
        kryo.writeClassAndObject(output, bankIds);
        kryo.writeClassAndObject(output, gameIds);
        output.writeString(baseCurrency);
        kryo.writeClassAndObject(output, promoDetailURLs);
        output.writeString(playerIdentificationType.name());
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        template = (IPromoTemplate) kryo.readClassAndObject(input);
        name = input.readString();
        String s = input.readString();
        enterType = EnterType.valueOf(s);
        period = kryo.readObject(input, DatePeriod.class);
        s = input.readString();
        status = Status.valueOf(s);
        //noinspection unchecked
        bankIds = (Set<Long>) kryo.readClassAndObject(input);
        //noinspection unchecked
        gameIds = (Set<Long>) kryo.readClassAndObject(input);
        baseCurrency = input.readString();
            //noinspection unchecked
        promoDetailURLs = (Map<Long, String>) kryo.readClassAndObject(input);
        playerIdentificationType = PlayerIdentificationType.valueOf(input.readString());
    }
}
