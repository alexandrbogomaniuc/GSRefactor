package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.DatePeriod;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.*;

/**
 * User: flsh
 * Date: 4.12.2020.
 */
public class NetworkPromoCampaign extends PromoCampaign implements INetworkPromoCampaign<NetworkPromoEvent> {
    private static final byte VERSION = 1;

    private Set<NetworkPromoEvent> events = new HashSet<NetworkPromoEvent>();
    private boolean singleClusterPromo;
    private String summaryFeedUrl;

    //key is lang (en,it, ..); english key is mandatory
    private Map<String, LocalizationTitles> localizationTitles;

    public NetworkPromoCampaign(long id, NetworkTournamentPromoTemplate template, String name, EnterType enterType,
                                DatePeriod period, Status status, Set<Long> bankIds, Set<Long> gameIds, String baseCurrency,
                                Map<Long, String> promoDetailURLs, PlayerIdentificationType playerIdentificationType,
                                Map<String, LocalizationTitles> localizationTitles) {
        super(id, template, name, enterType, period, status, bankIds, gameIds, baseCurrency, promoDetailURLs,
                playerIdentificationType);
        this.localizationTitles = localizationTitles;
    }

    @Override
    public NetworkPromoEvent getCurrentEvent() {
        for (NetworkPromoEvent event : events) {
            if (event.getPeriod().isDateBetween(new Date())) {
                return event;
            }
        }
        return null;
    }

    @Override
    public Set<NetworkPromoEvent> getEvents() {
        return events;
    }

    @Override
    public Set<NetworkPromoEvent> addEvent(NetworkPromoEvent event) throws CommonException {
        validateNewEvent(event);
        events.add(event);
        return events;
    }

    @Override
    public boolean isSingleClusterPromo() {
        return singleClusterPromo;
    }

    public void setSingleClusterPromo(boolean singleClusterPromo) {
        this.singleClusterPromo = singleClusterPromo;
    }

    @Override
    public Map<String, LocalizationTitles> getLocalizationTitles() {
        return localizationTitles;
    }

    @Override
    public LocalizationTitles getLocalizationTitle(String lang) {
        return localizationTitles == null ? null : localizationTitles.get(lang);
    }

    @Override
    public void addLocalizationTitle(String lang, LocalizationTitles title) {
        if(localizationTitles == null) {
            localizationTitles = new HashMap<String, LocalizationTitles>();
        }
        localizationTitles.put(lang, title);
    }

    @Override
    public long getTotalPrizePool() {
        long totalPrizePool = 0;
        for (NetworkPromoEvent event : events) {
            totalPrizePool += event.getNetworkPromoEventTemplate().getPrize();
        }
        return totalPrizePool;
    }

    @Override
    public boolean isNetworkPromoCampaign() {
        return true;
    }

    public String getSummaryFeedUrl() {
        return summaryFeedUrl;
    }

    public void setSummaryFeedUrl(String summaryFeedUrl) {
        this.summaryFeedUrl = summaryFeedUrl;
    }

    private void validateNewEvent(NetworkPromoEvent event) throws CommonException {
        if(events.contains(event)) {
            throw new CommonException("Event already added");
        }
        if(event.getParentPromoCampaignId() != getId()) {
            throw new CommonException("Parent promoCampaignId mismatch");
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeBoolean(singleClusterPromo);
        kryo.writeClassAndObject(output, events);
        kryo.writeClassAndObject(output, localizationTitles);
        kryo.writeObjectOrNull(output, summaryFeedUrl, String.class);
        super.write(kryo, output);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        singleClusterPromo = input.readBoolean();
        events = (Set<NetworkPromoEvent>) kryo.readClassAndObject(input);
        localizationTitles = (Map<String, LocalizationTitles>) kryo.readClassAndObject(input);
        if (ver > 0) {
            summaryFeedUrl = kryo.readObjectOrNull(input, String.class);
        }
        super.read(kryo, input);
    }

    @Override
    public String toString() {
        return "NetworkPromoCampaign[" +
                "id=" + getId() +
                ", singleClusterPromo=" + singleClusterPromo +
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
                ", events.size=" + getEvents().size() +
                ']';
    }
}
