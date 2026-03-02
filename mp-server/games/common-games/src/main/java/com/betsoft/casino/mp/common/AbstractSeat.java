package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * User: flsh
 * Date: 12.02.19.
 */
@SuppressWarnings("rawtypes")

/**
 * Abstract class for players.
 */
public abstract class AbstractSeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IPlayerRoundInfo,
        TREASURE extends ITreasure, RPI extends IRoomPlayerInfo, S extends ISeat>
        implements ISeat<WEAPON, PLAYER_ROUND_INFO, TREASURE, RPI, S> {
    private static final byte VERSION = 1;
    /** player info (sessionId, bankId, accountId, ...)  {@link IRoomPlayerInfo} */
    protected RPI playerInfo;
    protected long joinDate;
    /** current stake of player, for action game should be equal stake of room */
    protected Money stake = Money.ZERO;
    protected Money qualifyWin = Money.ZERO;
    /** current round win of player, for action game can be converted to ammo */
    protected Money roundWin = Money.ZERO;
    protected Money rebuyFromWin = Money.ZERO;
    protected Money shotTotalWin = Money.ZERO;
    /** sum of all wins of player, for stats */
    protected Money totalWin = Money.ZERO;
    /** last win of player */
    protected Money lastWin = Money.ZERO;
    /** currency rate of player currency to room currency (not used) */
    protected double currentRate;
    /** detail information of round. will be sent to gs for vba history   {@link IPlayerRoundInfo}*/
    protected PLAYER_ROUND_INFO currentPlayerRoundInfo;
    protected int level;
    /** player is in process of sitout */
    protected boolean sitOutStarted;
    /** game socket client for player */
    private transient IGameSocketClient socketClient;
    protected int questsCompletedCount;
    protected long questsPayouts;
    /** current bet level of player stake. Can be used in action games. */
    protected int betLevel = 1;
    /** player is king of hill in battleground games */
    protected boolean kingOfHill;
    /** Additional counters for seat in the room. Can be used in different ways in different games. Designed to avoid raising VERSION parameter */
    protected Map<String, Integer> additionalTempCounters = new HashMap<>();

    protected AbstractSeat() {
    }

    /**
     * @param playerInfo - player info {@link IRoomPlayerInfo}
     * @param socketClient - game socketClient of player
     * @param currentRate - currency rate
     */
    protected AbstractSeat(RPI playerInfo, IGameSocketClient socketClient, double currentRate) {
        this.playerInfo = playerInfo;
        this.joinDate = System.currentTimeMillis();
        this.socketClient = socketClient;
        this.stake = Money.fromCents(playerInfo.getStake());
        this.currentRate = currentRate;
        initCurrentRoundInfo(playerInfo);
    }

    //need for disconnected seats
    protected AbstractSeat(RPI playerInfo, Money stake) {
        this.playerInfo = playerInfo;
        this.joinDate = System.currentTimeMillis();
        this.stake = stake;
    }

    protected abstract Logger getLogger();

    protected abstract void readPlayerRoundInfo(byte version, Kryo kryo, Input input);

    @Override
    public void initCurrentRoundInfo(RPI playerInfo) {
        this.currentPlayerRoundInfo.setRoomStake(stake.toCents());
        this.qualifyWin = Money.ZERO;
        this.rebuyFromWin = Money.ZERO;
        this.questsPayouts = 0;
        this.questsCompletedCount = 0;
        this.shotTotalWin = Money.ZERO;
        this.additionalTempCounters = new HashMap<>();
    }

    @Override
    public boolean isBot() {
        return socketClient != null && socketClient.isBot();
    }

    @Override
    public boolean isSitOutStarted() {
        return sitOutStarted;
    }

    @Override
    public void setSitOutStarted(boolean sitOutStarted) {
        this.sitOutStarted = sitOutStarted;
    }

    @Override
    public IGameSocketClient getSocketClient() {
        return socketClient;
    }

    @Override
    public void setSocketClient(IGameSocketClient client) {
        this.socketClient = client;
    }

    @Override
    public RPI getPlayerInfo() {
        return playerInfo;
    }

    @Override
    public void setPlayerInfo(RPI playerInfo) {
        this.playerInfo = playerInfo;
    }

    @Override
    public String getNickname() {
        return playerInfo.getNickname();
    }

    @Override
    public IAvatar getAvatar() {
        return playerInfo.getAvatar();
    }

    @Override
    public long getJoinDate() {
        return joinDate;
    }

    @Override
    public void setJoinDate(long joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public long getId() {
        return playerInfo.getId();
    }

    @Override
    public long getAccountId() {
        return playerInfo.getId();
    }

    @Override
    public long getBankId() {
        return playerInfo.getBankId();
    }

    @Override
    public Money getRoundWin() {
        return roundWin;
    }

    @Override
    public void setRoundWin(Money roundWin) {
        this.roundWin = roundWin;
    }

    @Override
    public void incrementRoundWin(Money win) {
        roundWin = roundWin.add(win);
    }

    @Override
    public void incrementShotTotalWin(Money win) {
        shotTotalWin = shotTotalWin.add(win);
    }

    @Override
    public Money getRebuyFromWin() {
        return rebuyFromWin;
    }

    @Override
    public void setRebuyFromWin(Money rebuyFromWin) {
        this.rebuyFromWin = rebuyFromWin;
    }


    /**
     * trying to buy more ammo from the current winnings
     * @param rebuyFromWin - the requested amount of money to buy new ammo
     */
    @Override
    public void makeRebuyFromWin(Money rebuyFromWin) throws CommonException {
        if (rebuyFromWin.greaterThan(roundWin)) {
            throw new CommonException("Cannot rebuy, roundWin < rebuyFromWin");
        }
        if (rebuyFromWin.lessOrEqualsTo(Money.ZERO)) {
            throw new CommonException("Negative rebuy from win: " + rebuyFromWin);
        }
        this.rebuyFromWin = this.rebuyFromWin.add(rebuyFromWin);
        this.roundWin = this.roundWin.subtract(rebuyFromWin);
    }

    @Override
    public void revertRebuyFromWin(Money rebuyFromWin) {
        if (rebuyFromWin.lessOrEqualsTo(Money.ZERO)) {
            getLogger().error("Failed to revert negative rebuy from win: {}", rebuyFromWin);
            return;
        }
        this.rebuyFromWin = this.rebuyFromWin.subtract(rebuyFromWin);
        this.roundWin = this.roundWin.add(rebuyFromWin);
    }

    /**
     * Get round win and reset.
     * @return {@code Money} return money of round win
     */
    @Override
    public Money retrieveRoundWin() {
        Money result = roundWin;
        roundWin = Money.ZERO;
        return result;
    }

    @Override
    public Money getPossibleBalanceAmount() {
        Money res = Money.ZERO;
        res = res.add(roundWin);
        return res;
    }

    @Override
    public void rollbackRoundWinAndAmmo(Money roundWin, int ammoAmount) {
        this.roundWin = roundWin;
    }

    public void resetShotTotalWin() {
        this.shotTotalWin = Money.ZERO;
    }

    public void updateScoreShotTotalWin() {
        long currentWin = shotTotalWin.toCents();
        long maxShotTotalWin = getCurrentPlayerRoundInfo().getMaxShotTotalWin();
        if (currentWin > maxShotTotalWin) {
            getCurrentPlayerRoundInfo().setMaxShotTotalWin(currentWin);
            getLogger().debug("updateScoreShotTotalWin aid: {},  scoreShotTotalWin: {}",
                    getAccountId(), maxShotTotalWin);
        }
    }

    @Override
    public Money getTotalWin() {
        return totalWin;
    }

    @Override
    public void setTotalWin(Money totalWin) {
        this.totalWin = totalWin;
    }

    @Override
    public void transferRoundWin() {
        this.totalWin = this.totalWin.add(roundWin);
    }

    /**
     * update externalRoundId in PlayerRoundInfo
     * @param externalRoundId - external roundId. Used for debit/credit requests on gs side and vba history
     */
    @Override
    public void updatePlayerRoundInfo(long externalRoundId) {
        if (currentPlayerRoundInfo != null) {
            getLogger().debug("updatePlayerRoundInfo, old: {}, new: {}", currentPlayerRoundInfo.getPlayerRoundId(),
                    externalRoundId);
            currentPlayerRoundInfo.setPlayerRoundId(externalRoundId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSeat seat = (AbstractSeat) o;
        return getAccountId() == seat.getAccountId() &&
                getBankId() == seat.getBankId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountId(), getBankId());
    }

    @Override
    public Money getStake() {
        return stake;
    }

    @Override
    public void setStake(Money stake) {
        this.stake = stake;
    }

    @Override
    public IExperience getTotalScore() {
        return playerInfo.getStats().getScore();
    }

    @Override
    public double addScore(double score) {
        if (System.currentTimeMillis() > 1543618800000L) {
            score /= 10;
        }
        return playerInfo.getRoundStats().addScore(score);
    }

    @Override
    public IExperience getCurrentScore() {
        return playerInfo.getRoundStats().getScore();
    }

    @Override
    public void resetCurrentScore() {
        playerInfo.setNewRoundStats();
    }

    @Override
    public boolean isWantSitOut() {
        return playerInfo.isWantSitOut();
    }

    @Override
    public void setWantSitOut(boolean wantSitOut) {
        playerInfo.setWantSitOut(wantSitOut);
    }

    @Override
    public long getRoundWinInCredits() {
        return Money.ZERO.equals(stake) || Money.ZERO.equals(roundWin) ? 0 : roundWin.divideBy(stake);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLastWin(Money lastWin) {
        this.lastWin = lastWin;
    }

    @Override
    public void addLastWin(Money win) {
        lastWin = lastWin.add(win);
    }

    @Override
    public Money getLastWin() {
        return lastWin;
    }

    @Override
    public double getCurrentRate() {
        return currentRate;
    }

    @Override
    public Money getQualifyWin() {
        return qualifyWin;
    }

    @Override
    public void setQualifyWin(Money qualifyWin) {
        this.qualifyWin = qualifyWin;
    }

    @Override
    public int getQuestsCompletedCount() {
        return questsCompletedCount;
    }

    @Override
    public void setQuestsCompletedCount(int questsCompletedCount) {
        this.questsCompletedCount = questsCompletedCount;
    }

    @Override
    public long getQuestsPayouts() {
        return questsPayouts;
    }

    @Override
    public void setQuestsPayouts(long questsPayouts) {
        this.questsPayouts = questsPayouts;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        sb.append("accountId=").append(getAccountId());
        sb.append(", bankId=").append(getBankId());
        sb.append(", nickname='").append(getNickname()).append('\'');
        sb.append(", joinDate=").append(joinDate);
        sb.append(", stake=").append(stake);
        sb.append(", totalScore=").append(getTotalScore());
        sb.append(", currentScore=").append(getCurrentScore());
        sb.append(", roundWin=").append(getRoundWin());
        sb.append(", rebuyFromWin=").append(getRebuyFromWin());
        sb.append(", totalWin=").append(getTotalWin());
        sb.append(", currentRate=").append(currentRate);
        sb.append(", qualifyWin=").append(qualifyWin);
        sb.append(", sitOutStarted=").append(sitOutStarted);
        sb.append(", socketClient=").append(socketClient);
        sb.append(", questsCompletedCount=").append(questsCompletedCount);
        sb.append(", questsPayouts=").append(questsPayouts);
        sb.append(", shotTotalWin=").append(shotTotalWin.toCents());
        sb.append(", betLevel=").append(getBetLevel());
        sb.append(", kingOfHill=").append(kingOfHill);
        sb.append(", additionalTempCounters=").append(additionalTempCounters);
        return sb.toString();
    }

    protected void writeAdditionalFields(Kryo kryo, Output output) {
        //nop by default
    }

    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        //nop by default
    }

    public int getBetLevel() {
        return betLevel;
    }

    public void setBetLevel(int betLevel) {
        this.betLevel = betLevel;
    }

    @Override
    public boolean isKingOfHill() {
        return kingOfHill;
    }

    @Override
    public void setKingOfHill(boolean kingOfHill) {
        this.kingOfHill = kingOfHill;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(joinDate, true);
        kryo.writeObject(output, stake);
        kryo.writeObject(output, qualifyWin);
        kryo.writeObject(output, roundWin);
        kryo.writeObject(output, rebuyFromWin);
        kryo.writeObject(output, totalWin);
        kryo.writeObject(output, lastWin);
        output.writeDouble(currentRate);
        kryo.writeClassAndObject(output, currentPlayerRoundInfo);
        output.writeInt(level, true);
        output.writeBoolean(sitOutStarted);
        kryo.writeClassAndObject(output, playerInfo);
        output.writeInt(questsCompletedCount, true);
        output.writeLong(questsPayouts, true);
        kryo.writeObject(output, shotTotalWin);
        output.writeInt(betLevel, true);
        output.writeBoolean(kingOfHill);
        kryo.writeObject(output, getAdditionalTempCounters());
        writeAdditionalFields(kryo, output);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        joinDate = input.readLong(true);
        stake = kryo.readObject(input, Money.class);
        qualifyWin = kryo.readObject(input, Money.class);
        roundWin = kryo.readObject(input, Money.class);
        rebuyFromWin = kryo.readObject(input, Money.class);
        totalWin = kryo.readObject(input, Money.class);
        lastWin = kryo.readObject(input, Money.class);
        currentRate = input.readDouble();
        readPlayerRoundInfo(version, kryo, input);
        level = input.readInt(true);
        sitOutStarted = input.readBoolean();
        playerInfo = (RPI) kryo.readClassAndObject(input);
        questsCompletedCount = input.readInt(true);
        questsPayouts = input.readLong(true);
        shotTotalWin = kryo.readObject(input, Money.class);
        betLevel = input.readInt(true);
        kingOfHill = input.readBoolean();
        additionalTempCounters = kryo.readObject(input, HashMap.class);
        readAdditionalFields(version, kryo, input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("joinDate", joinDate);
        gen.writeObjectField("stake", stake);
        gen.writeObjectField("qualifyWin", qualifyWin);
        gen.writeObjectField("roundWin", roundWin);
        gen.writeObjectField("rebuyFromWin", rebuyFromWin);
        gen.writeObjectField("totalWin", totalWin);
        gen.writeObjectField("lastWin", lastWin);
        gen.writeNumberField("currentRate", currentRate);
        gen.writeObjectField("currentPlayerRoundInfo", currentPlayerRoundInfo);
        gen.writeNumberField("level", level);
        gen.writeBooleanField("sitOutStarted", sitOutStarted);
        gen.writeObjectField("playerInfo", playerInfo);
        gen.writeNumberField("questsCompletedCount", questsCompletedCount);
        gen.writeNumberField("questsPayouts", questsPayouts);
        gen.writeObjectField("shotTotalWin", shotTotalWin);
        gen.writeNumberField("betLevel", betLevel);
        gen.writeBooleanField("kingOfHill", kingOfHill);
        serializeMapField(gen, "additionalTempCounters", getAdditionalTempCounters(), new TypeReference<Map<String, Integer>>() {});
        serializeAdditionalFields(gen, serializers);
    }

    @Override
    public S deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        joinDate = node.get("joinDate").longValue();
        stake = om.convertValue(node.get("stake"),  Money.class);
        qualifyWin = om.convertValue(node.get("qualifyWin"), Money.class);
        roundWin = om.convertValue(node.get("roundWin"), Money.class);
        rebuyFromWin = om.convertValue(node.get("rebuyFromWin"), Money.class);
        totalWin = om.convertValue(node.get("totalWin"), Money.class);
        lastWin = om.convertValue(node.get("lastWin"), Money.class);
        currentRate = node.get("currentRate").doubleValue();
        deserializePlayerRoundInfo(p, node, ctxt);
        level = node.get("level").intValue();
        sitOutStarted = node.get("sitOutStarted").booleanValue();
        playerInfo = (RPI) om.convertValue(node.get("playerInfo"), Object.class);
        questsCompletedCount = node.get("questsCompletedCount").intValue();
        questsPayouts = node.get("questsPayouts").longValue();
        shotTotalWin = om.convertValue(node.get("shotTotalWin"), Money.class);
        betLevel = node.get("betLevel").intValue();
        kingOfHill = node.get("kingOfHill").booleanValue();
        additionalTempCounters = om.convertValue(node.get("additionalTempCounters"), new TypeReference<Map<String, Integer>>() {});
        deserializeAdditionalFields(p, node, ctxt);

        return getDeserializer();
    }

    protected abstract void serializeAdditionalFields(JsonGenerator gen,
                                                      SerializerProvider serializers) throws IOException;

    protected abstract void deserializeAdditionalFields(JsonParser p,
                                                        JsonNode node,
                                                        DeserializationContext ctxt);

    protected abstract void deserializePlayerRoundInfo(JsonParser p,
                                                       JsonNode node,
                                                       DeserializationContext ctxt);

    protected abstract S getDeserializer();

    public Map<String, Integer> getAdditionalTempCounters() {
        return additionalTempCounters == null ? new HashMap<>() : additionalTempCounters;
    }

    public void resetAdditionalTempCounter(String key) {
        additionalTempCounters.remove(key);
    }

    public int getAdditionalTempCounters(String key) {
        return this.additionalTempCounters.getOrDefault(key, 0);
    }

    public void addAdditionalTempCounter(String key, int cnt) {
        this.additionalTempCounters.put(key, getAdditionalTempCounters(key) + cnt);
    }
}
