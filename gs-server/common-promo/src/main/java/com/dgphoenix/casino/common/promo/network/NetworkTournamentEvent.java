package com.abs.casino.common.promo.network;

import com.abs.casino.common.promo.INetworkPromoEventTemplate;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Map;

public class NetworkTournamentEvent implements KryoSerializable {
    private static final byte VERSION = 0;

    private long id;
    private String name;
    private INetworkPromoEventTemplate template;
    private long startDate;
    private long endDate;
    //key is langCode (en, ru, ..), value = localized string
    private Map<String, String> localizationTitlesMap;

    public NetworkTournamentEvent() {}

    public NetworkTournamentEvent(long id, String name, INetworkPromoEventTemplate template, long startDate,
                                  long endDate, Map<String, String> localizationTitlesMap) {
        this.id = id;
        this.name = name;
        this.template = template;
        this.startDate = startDate;
        this.endDate = endDate;
        this.localizationTitlesMap = localizationTitlesMap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public INetworkPromoEventTemplate getTemplate() {
        return template;
    }

    public void setTemplate(INetworkPromoEventTemplate template) {
        this.template = template;
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

    public Map<String, String> getLocalizationTitlesMap() {
        return localizationTitlesMap;
    }

    public void setLocalizationTitlesMap(Map<String, String> localizationTitlesMap) {
        this.localizationTitlesMap = localizationTitlesMap;
    }

    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(getVersion());
        output.writeLong(id);
        output.writeString(name);
        kryo.writeClassAndObject(output, template);
        output.writeLong(startDate);
        output.writeLong(endDate);
        kryo.writeClassAndObject(output, localizationTitlesMap);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readLong();
        name = input.readString();
        template = (INetworkPromoEventTemplate) kryo.readClassAndObject(input);
        startDate = input.readLong();
        endDate = input.readLong();
        localizationTitlesMap = (Map<String, String>) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetworkTournamentEvent [");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", template=").append(template);
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", localizationTitlesMap=").append(localizationTitlesMap);
        sb.append(']');
        return sb.toString();
    }
}
