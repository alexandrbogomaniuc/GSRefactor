package com.abs.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 26.10.2021.
 */
public class PromoWinInfo implements KryoSerializable {
    private static final byte VERSION = 0;

    private long date;
    private long amount;
    private long campaignId;
    private String campaignType;

    public PromoWinInfo() {
    }

    public PromoWinInfo(long date, long amount, long campaignId, String campaignType) {
        this.date = date;
        this.amount = amount;
        this.campaignId = campaignId;
        this.campaignType = campaignType;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(String campaignType) {
        this.campaignType = campaignType;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(date, true);
        output.writeLong(amount, true);
        output.writeLong(campaignId, true);
        output.writeString(campaignType);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        date = input.readLong(true);
        amount = input.readLong(true);
        campaignId = input.readLong(true);
        campaignType = input.readString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PromoWinInfo [");
        sb.append("date=").append(date);
        sb.append(", amount=").append(amount);
        sb.append(", campaignId=").append(campaignId);
        sb.append(", campaignType='").append(campaignType).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
