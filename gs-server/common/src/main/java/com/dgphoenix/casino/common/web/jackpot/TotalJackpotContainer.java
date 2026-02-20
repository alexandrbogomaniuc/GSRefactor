package com.dgphoenix.casino.common.web.jackpot;

import com.dgphoenix.casino.common.util.xml.XMLUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.HashSet;
import java.util.Set;

/**
 * User: plastical
 * Date: 17.08.2010
 */
@XStreamAlias("TotalJackpotContainer")
public class TotalJackpotContainer {
    public static final String REQUEST_GET_TOTAL_JACKPOTS = "GET_TOTAL_JACKPOTS";

    @XStreamAlias("TotalJackpots")
    private Set<TotalJackpotEntry> totalJackpots;

    public TotalJackpotContainer() {
        totalJackpots = new HashSet<>();
    }

    public TotalJackpotContainer(Set<TotalJackpotEntry> gameJackpots) {
        this.totalJackpots = gameJackpots;
    }

    public Set<TotalJackpotEntry> getTotalJackpots() {
        return totalJackpots;
    }

    public void setTotalJackpots(Set<TotalJackpotEntry> totalJackpots) {
        this.totalJackpots = totalJackpots;
    }

    public void add(TotalJackpotEntry totalJackpot) {
        this.totalJackpots.add(totalJackpot);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GameTotalJackpotRequest");
        sb.append("{totalJackpots=").append(totalJackpots);
        sb.append('}');
        return sb.toString();
    }

    public static void main(String[] args) {
        TotalJackpotEntry totalJackpotEntry = new TotalJackpotEntry(1l);

        Set<TotalJackpotEntry> totalJackpotEntries = new HashSet<TotalJackpotEntry>();
        totalJackpotEntries.add(totalJackpotEntry);

        TotalJackpotContainer container = new TotalJackpotContainer(totalJackpotEntries);

        String xml = XMLUtils.toXML(container);
        System.out.println("->" + xml);
        System.out.println("<-" + XMLUtils.fromXML(xml, TotalJackpotContainer.class, TotalJackpotEntry.class));
    }
}
