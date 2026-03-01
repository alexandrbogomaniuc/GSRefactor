package com.abs.casino.common.promo;

import com.abs.casino.common.promo.INetworkPromoEvent;
import com.abs.casino.common.util.DatePeriod;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 5.12.2020.
 */
public class NetworkPromoEvent extends PromoCampaign implements INetworkPromoEvent {
    private static final byte VERSION = 1;
    private long parentPromoCampaignId;
    private String summaryFeedUrl;

    public NetworkPromoEvent(long id, long parentPromoCampaignId, INetworkPromoEventTemplate template, String name,
                             EnterType enterType, DatePeriod period, Status status, Set<Long> bankIds,
                             Set<Long> gameIds, String baseCurrency, Map<Long, String> promoDetailURLs,
                             PlayerIdentificationType playerIdentificationType) {
        super(id, template, name, enterType, period, status, bankIds, gameIds, baseCurrency, promoDetailURLs,
                playerIdentificationType);
        this.parentPromoCampaignId = parentPromoCampaignId;
    }

    @Override
    public long getParentPromoCampaignId() {
        return parentPromoCampaignId;
    }

    public void setParentPromoCampaignId(long parentPromoCampaignId) {
        this.parentPromoCampaignId = parentPromoCampaignId;
    }

    public String getSummaryFeedUrl() {
        return summaryFeedUrl;
    }

    public void setSummaryFeedUrl(String summaryFeedUrl) {
        this.summaryFeedUrl = summaryFeedUrl;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(parentPromoCampaignId, true);
        kryo.writeObjectOrNull(output, summaryFeedUrl, String.class);
        super.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        parentPromoCampaignId = input.readLong(true);
        if (ver > 0) {
            summaryFeedUrl = kryo.readObjectOrNull(input, String.class);
        }
        super.read(kryo, input);
    }

    @Override
    public INetworkPromoEventTemplate getNetworkPromoEventTemplate() {
        return (INetworkPromoEventTemplate) getTemplate();
    }

    @Override
    public String toString() {
        return "NetworkPromoEvent[" +
                "id=" + getId() +
                ", parentPromoCampaignId=" + parentPromoCampaignId +
                ", summaryFeedUrl=" + summaryFeedUrl +
                ", template=" + getTemplate() +
                ", name='" + getName() + '\'' +
                ", enterType=" + getEnterType() +
                ", period=" + getPeriod() +
                ", status=" + getStatus() +
                ", baseCurrency=" + getBaseCurrency() +
                ", bankIds=" + getBankIds() +
                ", gameIds=" + getGameIds() +
                ", playerIdentificationType=" + getPlayerIdentificationType() +
                ", promoDetailURLs=" + getPromoDetailURLs() +
                ']';
    }
}
