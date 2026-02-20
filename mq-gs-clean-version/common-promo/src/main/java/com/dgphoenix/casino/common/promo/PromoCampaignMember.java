package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.*;

import static com.dgphoenix.casino.common.promo.AwardedPrize.BY_AWARD_DATE;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * User: flsh
 * Date: 17.11.16.
 */
public class PromoCampaignMember implements Identifiable, KryoSerializable, IDistributedCacheEntry {
    private static final byte VERSION = 5;
    public static final int AWARDED_PRIZES_SIZE_LIMIT = 10;
    private long accountId;
    private long bankId;
    private String displayName;
    private long campaignId;
    private long enterTime;
    private int totalBetsCount;
    private long totalBetSum;
    private long totalWinSum;

    //prizes that have not yet won
    private List<DesiredPrize> desiredPrizes = new ArrayList<DesiredPrize>();
    private List<AwardedPrize> awardedPrizes = new ArrayList<AwardedPrize>();
    //played with this promo game sessions
    private Set<Long> gameSessionIds = new HashSet<Long>();
    private int lastEnteredServerId;
    private boolean webSocketSupport;
    private boolean welcomeMessageDisplayed;
    private boolean noPrizesMessageDisplayed;
    private int lastPrizeAwardCountDisplayed;
    private long lastMotivationMessageTime;
    private int lastPosition;

    public PromoCampaignMember() {
    }

    public PromoCampaignMember(long accountId, long bankId, String displayName, long campaignId, long enterTime) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.displayName = displayName;
        this.campaignId = campaignId;
        this.enterTime = enterTime;
        this.lastMotivationMessageTime = System.currentTimeMillis(); //do not show immediately after promo enter
    }

    @Override
    public long getId() {
        return accountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public List<AwardedPrize> getAwardedPrizes() {
        return Collections.unmodifiableList(awardedPrizes);
    }

    public void addAwardedPrize(AwardedPrize awardedPrize) {
        if (awardedPrize == null) {
            throw new NullPointerException("awardedPrize is null");
        }
        if (awardedPrizes.size() >= AWARDED_PRIZES_SIZE_LIMIT) {
            awardedPrizes.remove(0);
        }
        awardedPrizes.add(awardedPrize);
    }

    public int getTotalBetsCount() {
        return totalBetsCount;
    }

    public void setTotalBetsCount(int totalBetsCount) {
        this.totalBetsCount = totalBetsCount;
    }

    public long getTotalBetSum() {
        return totalBetSum;
    }

    public long getTotalWinSum() {
        return totalWinSum;
    }

    public List<DesiredPrize> getDesiredPrizes() {
        return desiredPrizes;
    }

    public void removeDesiredPrizes(Set<DesiredPrize> redundantPrizes) {
        if (isNotEmpty(redundantPrizes)) {
            desiredPrizes.removeAll(redundantPrizes);
        }
    }

    public void addDesiredPrizes(Set<DesiredPrize> newPrizes) {
        if (isNotEmpty(newPrizes)) {
            desiredPrizes.addAll(newPrizes);
        }
    }

    public List<DesiredPrize> getActiveDesiredPrizes() {
        List<DesiredPrize> activePrizes = new ArrayList<DesiredPrize>();
        for (DesiredPrize desiredPrize : desiredPrizes) {
            if (desiredPrize.getStatus() == PrizeStatus.ACTIVE) {
                activePrizes.add(desiredPrize);
            }
        }
        return activePrizes;
    }

    public void setDesiredPrizes(List<DesiredPrize> desiredPrizes) {
        this.desiredPrizes = desiredPrizes;
    }

    public Set<Long> getGameSessionIds() {
        return gameSessionIds;
    }

    public void addGameSessionId(long gameSessionId) {
        if(gameSessionIds == null) {
            gameSessionIds = new HashSet<Long>();
        }
        gameSessionIds.add(gameSessionId);
    }

    public int getLastEnteredServerId() {
        return lastEnteredServerId;
    }

    public void setLastEnteredServerId(int lastEnteredServerId) {
        this.lastEnteredServerId = lastEnteredServerId;
    }

    public void resetLastEnteredServerId() {
        lastEnteredServerId = 0;
    }

    public boolean hasEntered() {
        return lastEnteredServerId != 0;
    }

    public boolean hasWebSocketSupport() {
        return webSocketSupport;
    }

    public void setWebSocketSupport(boolean webSocketSupport) {
        this.webSocketSupport = webSocketSupport;
    }

    public void resetWebSocketSupport() {
        webSocketSupport = false;
    }

    public long getBankId() {
        return bankId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setTotalBetSum(long totalBetSum) {
        this.totalBetSum = totalBetSum;
    }

    public void setTotalWinSum(long totalWinSum) {
        this.totalWinSum = totalWinSum;
    }

    public void setAwardedPrizes(List<AwardedPrize> awardedPrizes) {
        this.awardedPrizes = new ArrayList<AwardedPrize>(awardedPrizes);
    }

    public void setGameSessionIds(Set<Long> gameSessionIds) {
        this.gameSessionIds = gameSessionIds;
    }

    public AwardedPrize getLastAwardedPrize() {
        return awardedPrizes.isEmpty() ? null : Collections.max(awardedPrizes, BY_AWARD_DATE);
    }

    public void updateBets(int incBetsCount, long incBetsSum) {
        totalBetsCount += incBetsCount;
        totalBetSum += incBetsSum;
    }

    public void updateWins(long incWinSum) {
        totalWinSum += incWinSum;
    }

    public boolean isWelcomeMessageDisplayed() {
        return welcomeMessageDisplayed;
    }

    public void setWelcomeMessageDisplayed(boolean welcomeMessageDisplayed) {
        this.welcomeMessageDisplayed = welcomeMessageDisplayed;
    }

    public boolean isNoPrizesMessageDisplayed() {
        return noPrizesMessageDisplayed;
    }

    public void setNoPrizesMessageDisplayed(boolean noPrizesMessageDisplayed) {
        this.noPrizesMessageDisplayed = noPrizesMessageDisplayed;
    }

    public int getLastPrizeAwardCountDisplayed() {
        return lastPrizeAwardCountDisplayed;
    }

    public void setLastPrizeAwardCountDisplayed(int lastPrizeAwardCountDisplayed) {
        this.lastPrizeAwardCountDisplayed = lastPrizeAwardCountDisplayed;
    }

    public long getLastMotivationMessageTime() {
        return lastMotivationMessageTime;
    }

    public void setLastMotivationMessageTime(long lastMotivationMessageTime) {
        this.lastMotivationMessageTime = lastMotivationMessageTime;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    @Override
    public String toString() {
        return "PromoCampaignMember[" +
                "accountId=" + accountId +
                ", bankId=" + bankId +
                ", displayName='" + displayName + '\'' +
                ", campaignId=" + campaignId +
                ", enterTime=" + enterTime +
                ", totalBetsCount=" + totalBetsCount +
                ", totalBetSum=" + totalBetSum +
                ", totalWinSum=" + totalWinSum +
                ", desiredPrizes=" + desiredPrizes +
                ", awardedPrizes=" + awardedPrizes +
                ", gameSessionIds=" + gameSessionIds +
                ", lastEnteredServerId=" + lastEnteredServerId +
                ", webSocketSupport=" + webSocketSupport +
                ", welcomeMessageDisplayed=" + welcomeMessageDisplayed +
                ", noPrizesMessageDisplayed=" + noPrizesMessageDisplayed +
                ", lastPrizeAwardCountDisplayed=" + lastPrizeAwardCountDisplayed +
                ", lastMotivationMessageTime=" + lastMotivationMessageTime +
                ", lastPosition=" + lastPosition +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PromoCampaignMember that = (PromoCampaignMember) o;

        if (accountId != that.accountId) return false;
        return campaignId == that.campaignId;
    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + (int) (campaignId ^ (campaignId >>> 32));
        return result;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeLong(bankId, true);
        output.writeString(displayName);
        output.writeLong(campaignId, true);
        output.writeLong(enterTime, true);
        output.writeInt(totalBetsCount, true);
        output.writeLong(totalBetSum, true);
        output.writeLong(totalWinSum, true);
        kryo.writeClassAndObject(output, desiredPrizes);
        kryo.writeClassAndObject(output, awardedPrizes);
        kryo.writeClassAndObject(output, gameSessionIds);
        output.writeInt(lastEnteredServerId, true);
        output.writeBoolean(webSocketSupport);
        output.writeBoolean(welcomeMessageDisplayed);
        output.writeBoolean(noPrizesMessageDisplayed);
        output.writeInt(lastPrizeAwardCountDisplayed);
        output.writeLong(lastMotivationMessageTime, true);
        output.writeInt(lastPosition, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        accountId = input.readLong(true);
        bankId = input.readLong(true);
        displayName = input.readString();
        campaignId = input.readLong(true);
        enterTime = input.readLong(true);
        totalBetsCount = input.readInt(true);
        totalBetSum = input.readLong(true);
        totalWinSum = input.readLong(true);
        desiredPrizes = (List<DesiredPrize>) kryo.readClassAndObject(input);
        awardedPrizes = (List<AwardedPrize>) kryo.readClassAndObject(input);
        gameSessionIds = (Set<Long>) kryo.readClassAndObject(input);
        lastEnteredServerId = input.readInt(true);
        webSocketSupport = input.readBoolean();
        if (ver > 0) {
            welcomeMessageDisplayed = input.readBoolean();
        }
        if (ver > 1) {
            noPrizesMessageDisplayed = input.readBoolean();
        }
        if (ver > 2) {
            lastPrizeAwardCountDisplayed = input.readInt();
        }
        if (ver > 3) {
            lastMotivationMessageTime = input.readLong(true);
        }
        if (ver > 4) {
            lastPosition = input.readInt(true);
        }
    }
}
