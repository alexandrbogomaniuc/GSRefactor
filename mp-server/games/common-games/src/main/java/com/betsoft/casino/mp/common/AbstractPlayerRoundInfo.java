package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.abs.casino.common.kpi.RoundKPIInfo;
import com.abs.casino.common.util.KryoHelper;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
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
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.common.AbstractActionPlayGameState.REGULAR_WEAPON;

/**
 * User: flsh
 * Date: 12.02.19.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractPlayerRoundInfo<ENEMY_TYPE extends IEnemyType, ENEMY_STAT extends IEnemyStat, PRI extends IPlayerRoundInfo>
        implements IPlayerRoundInfo<ENEMY_TYPE, ENEMY_STAT, PRI> {
    private static final byte VERSION = 1;

    protected static final Logger LOGGER = LogManager.getLogger(AbstractPlayerRoundInfo.class.getName());
    protected final static String DELIMETER_FIELDS = ";";
    protected final static String DELIMETER_PARAM_FIELD = "&";
    protected final static String DELIMETER_VALUE_PARAM = "=";
    protected final static String DELIMETER_VALUE_PARAM_SECOND = ",";
    /** total bets in round */
    protected Money totalBets = Money.ZERO;
    /** total wins in round */
    protected Money totalPayouts = Money.ZERO;
    protected Money roundStartBalance = Money.ZERO;
    protected IExperience xpearned;
    /** external round of player (debit/credit) */
    protected long playerRoundId;
    /** roundId in room (common for all players for one round) */
    protected long roomRoundId;
    /** room Id */
    protected long roomId;
    /** end time of round */
    protected long timeOfRoundEnd;
    /** total amount of buyIn (cents) */
    protected int ammoAmountBuyIn;
    /** total amount of returned money (cents) */
    protected int ammoAmountReturned;
    /** stake of room (cents) */
    protected long roomStake;
    /** extended stats by enemies (for action games) */
    protected Map<String, ENEMY_STAT> statByEnemies = new HashMap<>();
    /** extended stats for  boss (for action games) */
    protected BossStat statBoss;

    /** compensation for poor shots or unused weapons */
    protected String weaponSurplusVBA;
    protected Money weaponSurplusMoney = Money.ZERO;

    public int questCompletedCount = 0;
    /** quests payouts for old action games */
    public Money questPayouts = Money.ZERO;
    protected Map<Integer, Integer> questWeapons;
    protected String additionalData = "";
    protected long maxShotTotalWin = 0;
    protected Map<Integer, RealWeaponCounts> realShotsCount;
    protected Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins;
    protected Map<Integer, LootBoxStat> lootBoxStat; // cost (cents), count
    protected Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons;
    protected Map<String, Map<WeaponSource, Integer>> weaponStatBySources;
    protected transient int gameId;
    private int freeShotsWon; // number of received free shots of SW
    protected int moneyWheelCompleted;
    protected long moneyWheelPayouts;
    protected double totalDamage;
    protected RoundKPIInfo roundKPIInfo;
    /** true if is battleground mode */
    protected boolean battlegroundMode;
    /** total bet in battle */
    protected long battleBet = 0;
    /** total win in battle */
    protected long battleWin = 0;

    public AbstractPlayerRoundInfo() {
    }

    public AbstractPlayerRoundInfo(long roomId, int gameId) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.statBoss = new BossStat(gameId);
        for (IEnemyType enemyType : getEnemyTypes()) {
            if (!enemyType.isBoss()) {
                this.statByEnemies.put(enemyType.getName(), (ENEMY_STAT) new EnemyStat(enemyType.isHVenemy(), gameId));
            }
        }
        questWeapons = new HashMap<>();
        Arrays.stream(SpecialWeaponType.values()).forEach(specialWeaponType -> {
            if (specialWeaponType.getAvailableGameIds().contains(gameId))
                questWeapons.put(specialWeaponType.getId(), 0);
        });
        realShotsCount = new HashMap<>();
        additionalWins = new HashMap<>();
        weaponStatBySources = new HashMap<>();
        freeShotsWon = 0;
        moneyWheelCompleted = 0;
        moneyWheelPayouts = 0;
        totalDamage = 0;
        roundKPIInfo = new RoundKPIInfo(0, 0, 0, 0, 0,
                System.currentTimeMillis(), 0, new HashMap<>(), new HashMap<>(), new HashMap<>(),
                new HashMap<>(), new HashMap<>(), 0);

        battleBet = 0;
        battleWin = 0;
    }


    public void updateRealShotCount(int weaponId, boolean paidMode, double bet) {
        RealWeaponCounts weaponCounts = getRealShotsCount().get(weaponId);
        if (weaponCounts == null) {
            weaponCounts = new RealWeaponCounts();
        }
        weaponCounts.updateData(paidMode, bet);
        realShotsCount.put(weaponId, weaponCounts);
    }

    public int getFreeShotsWon() {
        return freeShotsWon;
    }

    public void addFreeShotsWon(int freeShotsWon) {
        this.freeShotsWon += freeShotsWon;
    }

    public int getMoneyWheelCompleted() {
        return moneyWheelCompleted;
    }

    public void addMoneyWheelCompleted(int moneyWheelCompleted) {
        this.moneyWheelCompleted += moneyWheelCompleted;
    }

    public long getMoneyWheelPayouts() {
        return moneyWheelPayouts;
    }

    public void addMoneyWheelPayouts(long moneyWheelPayouts) {
        this.moneyWheelPayouts += moneyWheelPayouts;
    }

    public Map<String, Pair<AtomicInteger, AtomicDouble>> getAdditionalWins() {
        return additionalWins == null ? new HashMap<>() : additionalWins;
    }


    public void updateAdditionalWin(String description, Money win) {
        totalPayouts = totalPayouts.add(win);
        Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins = getAdditionalWins();
        Pair<AtomicInteger, AtomicDouble> pair = this.additionalWins.get(description);
        if (pair == null) {
            additionalWins.put(description, new Pair<>(new AtomicInteger(1), new AtomicDouble(win.toDoubleCents())));
        } else {
            pair.getKey().incrementAndGet();
            pair.getValue().addAndGet(win.toDoubleCents());
        }
    }

    @Override
    public Money getTotalBets() {
        return totalBets;
    }

    @Override
    public void addTotalBets(Money totalBets) {
        this.totalBets = this.totalBets.add(totalBets);
    }

    @Override
    public Money getTotalPayouts() {
        return totalPayouts;
    }

    @Override
    public Money getRoundStartBalance() {
        return roundStartBalance;
    }

    @Override
    public void addTotalPayouts(Money totalPayouts) {
        this.totalPayouts = this.totalPayouts.add(totalPayouts);
    }

    @Override
    public IExperience getXpearned() {
        return xpearned;
    }

    @Override
    public void addXpearned(IExperience xpearned) {
        if (this.xpearned != null)
            this.xpearned.add(xpearned);
    }

    @Override
    public long getPlayerRoundId() {
        return playerRoundId;
    }

    @Override
    public void setPlayerRoundId(long playerRoundId) {
        this.playerRoundId = playerRoundId;
    }

    @Override
    public long getRoomRoundId() {
        return roomRoundId;
    }

    @Override
    public void setRoomRoundId(long roomRoundId) {
        this.roomRoundId = roomRoundId;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public long getTimeOfRoundEnd() {
        return timeOfRoundEnd;
    }

    @Override
    public void setTimeOfRoundEnd(long timeOfRoundEnd) {
        this.timeOfRoundEnd = timeOfRoundEnd;
    }

    @Override
    public int getAmmoAmountBuyIn() {
        return ammoAmountBuyIn;
    }

    @Override
    public void addAmmoAmountBuyIn(int ammoAmountByeIn) {
        this.ammoAmountBuyIn += ammoAmountByeIn;
    }

    @Override
    public int getAmmoAmountReturned() {
        return ammoAmountReturned;
    }

    @Override
    public void addAmmoAmountReturned(int ammoAmountReturned) {
        this.ammoAmountReturned += ammoAmountReturned;
    }

    @Override
    public void setAmmoAmountReturned(int ammoAmountReturned) {
        this.ammoAmountReturned = ammoAmountReturned;
    }

    @Override
    public long getRoomStake() {
        return roomStake;
    }

    @Override
    public void setRoomStake(long roomStake) {
        this.roomStake = roomStake;
    }

    @Override
    public Map<String, ENEMY_STAT> getStatByEnemies() {
        return statByEnemies;
    }

    @Override
    public void setStatByEnemies(Map<String, ENEMY_STAT> statByEnemies) {
        this.statByEnemies = statByEnemies;
    }

    public BossStat getStatBoss() {
        return statBoss;
    }

    public void setStatBoss(BossStat statBoss) {
        this.statBoss = statBoss;
    }

    @Override
    public void setTotalBets(Money totalBets) {
        this.totalBets = totalBets;
    }

    @Override
    public void setTotalPayouts(Money totalPayouts) {
        this.totalPayouts = totalPayouts;
    }
    
    @Override
    public void setRoundStartBalance(Money roundStartBalance) {
        this.roundStartBalance = roundStartBalance;
    }

    @Override
    public void setXpearned(IExperience xpearned) {
        this.xpearned = xpearned;
    }

    @Override
    public void setAmmoAmountBuyIn(int ammoAmountBuyIn) {
        this.ammoAmountBuyIn = ammoAmountBuyIn;
    }

    public void updateStatOnEndRound(int ammoAmountBuyIn, IExperience xpearned, int ammoAmountReturned) {
        this.ammoAmountBuyIn = ammoAmountBuyIn;
        this.xpearned = xpearned;
        this.ammoAmountReturned = ammoAmountReturned;
        this.timeOfRoundEnd = System.currentTimeMillis();
    }

    @Override
    public String getWeaponSurplusVBA() {
        return weaponSurplusVBA;
    }

    @Override
    public void setWeaponSurplusVBA(String weaponSurplusVBA) {
        this.weaponSurplusVBA = weaponSurplusVBA;
    }

    @Override
    public void addWeaponSurplusVBA(String weaponSurplusVBA) {
        if (this.weaponSurplusVBA == null) {
            this.weaponSurplusVBA = weaponSurplusVBA;
        } else {
            if (!weaponSurplusVBA.isEmpty())
                this.weaponSurplusVBA += weaponSurplusVBA;
        }
    }

    public long getMaxShotTotalWin() {
        return maxShotTotalWin;
    }

    public void setMaxShotTotalWin(long maxShotTotalWin) {
        this.maxShotTotalWin = maxShotTotalWin;
    }

    @Override
    public Money getWeaponSurplusMoney() {
        return weaponSurplusMoney;
    }

    @Override
    public void setWeaponSurplusMoney(Money weaponSurplusMoney) {
        this.weaponSurplusMoney = weaponSurplusMoney;
    }

    @Override
    public void addWeaponSurplusMoney(Money weaponSurplusMoney) {
        this.weaponSurplusMoney = this.weaponSurplusMoney.add(weaponSurplusMoney);
    }

    @Override
    public void updateQuestCompletedTotalData(Money win, int weaponTypeId, int newShots) {
        questCompletedCount++;
        if (win.greaterThan(Money.ZERO)) {
            questPayouts = questPayouts.add(win);
            totalPayouts = totalPayouts.add(win);
        }

        if (weaponTypeId != REGULAR_WEAPON) {
            Integer newCount = questWeapons.get(weaponTypeId) + newShots;
            questWeapons.put(weaponTypeId, newCount);
            addWeaponSourceStat(WeaponSource.QUEST.getTitle(), SpecialWeaponType.values()[weaponTypeId].getTitle(), newShots);
        }
        LOGGER.debug("updateQuestCompletedTotalData : {}, weaponTypeId: {}, newShots: {}, ", win, weaponTypeId, newShots);
    }

    @Override
    public void updateStatNew(Money stake, boolean isBoss, boolean isSpecialWeapon,
                              String specialWeapon, Money payout, boolean isKilled,
                              String enemyKey, Money betPayWeapon) {
        totalBets = totalBets.add(stake);
        totalPayouts = totalPayouts.add(payout);
        ENEMY_STAT enemyStat = statByEnemies.get(enemyKey);
        LOGGER.debug("updateStatNew  enemyKey: {}", enemyKey);
        enemyStat.updateData(stake, isSpecialWeapon, specialWeapon, payout, isKilled, betPayWeapon);
    }

    @Override
    public void updateStatNewWithMultiplier(Money stake, boolean isBoss, boolean isSpecialWeapon,
                                String specialWeapon, Money payout, boolean isKilled,
                                String enemyKey, Money betPayWeapon, int chMult, String specialItemName) {
        totalBets = totalBets.add(stake);
        totalPayouts = totalPayouts.add(payout);
        ENEMY_STAT enemyStat = statByEnemies.get(enemyKey);
        LOGGER.debug("updateStatNew  enemyKey: {}", enemyKey);
        enemyStat.updateData(stake, isSpecialWeapon, specialWeapon, payout, isKilled, betPayWeapon, chMult, specialItemName);
    }

    public void updatePayoutsFromItemsWithMultiplier(Money stake, boolean isBoss, boolean isSpecialWeapon,
                                                     String specialWeapon, Money payout, boolean isKilled,
                                                     String enemyKey, Money betPayWeapon, int chMult, String specialItemName) {
        ENEMY_STAT enemyStat = statByEnemies.get(enemyKey);
        enemyStat.updateData(stake, isSpecialWeapon, specialWeapon, payout, isKilled, betPayWeapon, chMult, specialItemName);
    }

    public void updatePayoutsFromItems(Money payout, String enemyKey, String specialItemName) {
        ENEMY_STAT enemyStat = statByEnemies.get(enemyKey);
        enemyStat.updatePayoutsFromItems(payout, specialItemName);
    }

    public void updateKillAwardWin(Money killAwardWin, String enemyKey) {
        ENEMY_STAT enemyStat = statByEnemies.get(enemyKey);
        enemyStat.updateKillAwardWin(killAwardWin);
    }

    public void updateKillAwardWinWithLevelUp(Money killAwardWin, String enemyKey, boolean isSpecialWeapon, String specialWeapon) {
        ENEMY_STAT enemyStat = statByEnemies.get(enemyKey);
        enemyStat.updateKillAwardWinWithLevelUp(killAwardWin, isSpecialWeapon, specialWeapon);
    }

    @Override
    public void updateStat(Money stake, boolean isBoss, Money extraBossPayout, Money mainBossPayout,
                           boolean isSpecialWeapon, String specialWeapon,
                           Money payout, boolean isKilled,
                           String enemy, Money betPayWeapon) {

        totalBets = totalBets.add(stake);
        totalPayouts = totalPayouts.add(payout);

        if (isBoss) {
            statBoss.updateData(stake, payout, isKilled, isSpecialWeapon, specialWeapon, betPayWeapon);
        } else {
            statByEnemies.get(enemy).updateData(stake, isSpecialWeapon, specialWeapon, payout, isKilled, betPayWeapon);
        }

        Money check = Money.ZERO;
        check = check.add(statBoss.getBetsExtra());
        for (ENEMY_STAT stat : statByEnemies.values()) {
            check = check.add(stat.getMainBets());
            check = check.add(stat.getHvBets());
        }
        if (!check.equals(totalBets)) {
            LOGGER.debug("bet error, check: {}, totalBets: {}, specialWeapon: {}, stake: {}",
                    check, totalBets, specialWeapon, stake);
        }

        Money checkPay = Money.ZERO;
        checkPay = checkPay.add(statBoss.getPayoutsExtra());
        checkPay = checkPay.add(statBoss.getPayoutsMain());
        for (ENEMY_STAT stat : statByEnemies.values()) {
            checkPay = checkPay.add(stat.getPayouts());
            EnemyStat enemyStat = (EnemyStat) stat;
            for (IWeaponStat weaponStat : enemyStat.getSpecialWeaponsStats().values()) {
                checkPay = checkPay.add(weaponStat.getPayouts());
            }
        }

        for (WeaponStat weaponStat : statBoss.getSpecialWeaponsStats().values()) {
            checkPay = checkPay.add(weaponStat.getPayouts());
        }

        checkPay = checkPay.add(questPayouts);

        AtomicLong additionalWin = new AtomicLong();
        if (additionalWins != null && !additionalWins.isEmpty()) {
            additionalWins.forEach((s, atomicIntegerAtomicDoublePair) ->
                    additionalWin.addAndGet((long) atomicIntegerAtomicDoublePair.getValue().get()));
        }
        checkPay = checkPay.add(Money.fromCents(additionalWin.get()));

        checkPay(checkPay);
    }

    public void checkPay(Money checkPay) {
        if (!checkPay.equals(totalPayouts)) {
            LOGGER.debug("pay error, totalPayouts: {}, checkPay: {}, statBoss.getPayoutsMain(): {}, questPayouts: {} ",
                    totalPayouts, checkPay, statBoss.getPayoutsMain(), questPayouts);
        }
    }

    /**
     * Prepares player bet data for sending to GS. Will be used in VBA.
     * @param newPlayerBet {@code player bet for saving data}
     * @param returnedBet returned bet amount
     * @return prepared player bet
     */
    @Override
    public IPlayerBet getPlayerBet(IPlayerBet newPlayerBet, int returnedBet) {
        newPlayerBet.setWin(totalPayouts.toDoubleCents());
        newPlayerBet.setDateTime(timeOfRoundEnd);
        updateRoundKPIInfo(getRoundKPIInfo());
        String data = prepareVBAData(returnedBet);
        String kpiData = Base64.encodeBase64String(KryoHelper.serializeToBytes(getRoundKPIInfo()));
        newPlayerBet.setData(data + ";kpiInfo=" + kpiData);

        RoundKPIInfo roundKPIInfoRestored = KryoHelper.deserializeFrom(Base64.decodeBase64(kpiData), RoundKPIInfo.class);
        LOGGER.debug("roundKPIInfoRestored: {}", roundKPIInfoRestored);
        return newPlayerBet;
    }

    /**
     * Updates and Prepares roundKPIInfo for sending data to gs. roundKPIInfo is common statistic data for Casino Manager
     * @param roundKPIInfo stat data
     */
    protected void updateRoundKPIInfo(RoundKPIInfo roundKPIInfo) {
        roundKPIInfo.setEndTime(timeOfRoundEnd);
        roundKPIInfo.setRealBet((long) (totalBets.toDoubleCents()));
        roundKPIInfo.setRealWin((long) totalPayouts.toDoubleCents());
        roundKPIInfo.setSwCompensation(weaponSurplusMoney == null ? 0 : (long) weaponSurplusMoney.toDoubleCents());
        roundKPIInfo.setRoundId(playerRoundId);
        roundKPIInfo.setRoomCoinInCents(battlegroundMode ? 1L : getRoomStake());
    }

    public void addKpiInfoSWShotsCount(int weaponId, int cnt, int betLevel, boolean isFreeWeapon) {
        RoundKPIInfo roundKPIInfo = getRoundKPIInfo();
        Map<Integer, Map<Integer, Integer>> swShotsByBetLevel = isFreeWeapon ?
                roundKPIInfo.getSwFreeShotsByBetLevel() : roundKPIInfo.getSwShotsByBetLevel();
        Map<Integer, Integer> mapByLevel = swShotsByBetLevel.getOrDefault(betLevel, new HashMap<>());
        Integer cntOld = mapByLevel.getOrDefault(weaponId, 0);
        mapByLevel.put(weaponId, cntOld + cnt);
        swShotsByBetLevel.put(betLevel, mapByLevel);
    }

    public void addKpiInfoFreeShotsCount(int cnt) {
        RoundKPIInfo roundKPIInfo = getRoundKPIInfo();
        roundKPIInfo.setSwFreeShotsCount(roundKPIInfo.getSwFreeShotsCount() + cnt);
    }

    public void addKpiInfoPaidRegularShots(long costShotInCents) {
        updateCountsForMapKPIInfo(getRoundKPIInfo().getPaidRegularShotsStat(), costShotInCents);
    }

    public void addKpiInfoSwShots(long costShotInCents) {
        updateCountsForMapKPIInfo(getRoundKPIInfo().getSwShotsStat(), costShotInCents);
    }

    public void addKpiInfoLootbox(long costShotInCents) {
        updateCountsForMapKPIInfo(getRoundKPIInfo().getLootboxStat(), costShotInCents);
    }

    private void updateCountsForMapKPIInfo(Map<Long, Integer> map, long costShotInCents) {
        int newCount = map.getOrDefault(costShotInCents, 0) + 1;
        map.put(costShotInCents, newCount);
    }


    protected String prepareVBAData(int returnedBet) {
        StringBuilder sb = new StringBuilder();
        sb.append("timeOfRoundEnd").append(DELIMETER_VALUE_PARAM).append(timeOfRoundEnd).append(DELIMETER_FIELDS);
        sb.append("xpearned").append(DELIMETER_VALUE_PARAM).append(xpearned == null ? 0 : xpearned.getLongAmount()).append(DELIMETER_FIELDS);
        sb.append("playerRoundId").append(DELIMETER_VALUE_PARAM).append(playerRoundId).append(DELIMETER_FIELDS);
        sb.append("roomRoundId").append(DELIMETER_VALUE_PARAM).append(roomRoundId).append(DELIMETER_FIELDS);
        sb.append("roomId").append(DELIMETER_VALUE_PARAM).append(roomId).append(DELIMETER_FIELDS);
        sb.append("ammoAmountBuyIn").append(DELIMETER_VALUE_PARAM).append(ammoAmountBuyIn).append(DELIMETER_FIELDS);
        sb.append("ammoAmountReturned").append(DELIMETER_VALUE_PARAM).append(returnedBet != -1 ? returnedBet : ammoAmountReturned).append(DELIMETER_FIELDS);
        sb.append("roomStake").append(DELIMETER_VALUE_PARAM).append(roomStake).append(DELIMETER_FIELDS);

        for (ENEMY_TYPE enemyType : getEnemyTypes()) {
            if (!enemyType.isBoss()) {
                String name = enemyType.getName();
                ENEMY_STAT enemyStat = statByEnemies.get(name);
                if (enemyStat != null) {
                    sb.append("name").append(DELIMETER_VALUE_PARAM).append(name).append(DELIMETER_PARAM_FIELD);
                    sb.append("cntShotsToEnemy").append(DELIMETER_VALUE_PARAM).append(enemyStat.getCntShotsToEnemy()).append(DELIMETER_PARAM_FIELD);
                    sb.append("payouts").append(DELIMETER_VALUE_PARAM).append(enemyStat.getPayouts().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
                    sb.append("cntKills").append(DELIMETER_VALUE_PARAM).append(enemyStat.getCntKills()).append(DELIMETER_PARAM_FIELD);
                    sb.append("hvBets").append(DELIMETER_VALUE_PARAM).append(enemyStat.getHvBets().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
                    sb.append("isHV").append(DELIMETER_VALUE_PARAM).append(enemyStat.isHighValueEnemy()).append(DELIMETER_PARAM_FIELD);
                    sb.append("mainBets").append(DELIMETER_VALUE_PARAM).append(enemyStat.getMainBets().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
                    addSpecialWeapons(sb, enemyStat.getSpecialWeaponsStats());
                    sb.append(DELIMETER_FIELDS);
                }
            }
        }

        sb.append("bossCnt").append(DELIMETER_VALUE_PARAM).append(statBoss.getCntToBoss()).append(DELIMETER_PARAM_FIELD);
        sb.append("bossCntKills").append(DELIMETER_VALUE_PARAM).append(statBoss.getCntKillsOfBoss()).append(DELIMETER_PARAM_FIELD);
        sb.append("bossBetsExtra").append(DELIMETER_VALUE_PARAM).append(statBoss.getBetsExtra().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
        sb.append("bossPayoutsExtra").append(DELIMETER_VALUE_PARAM).append(statBoss.getPayoutsExtra().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
        sb.append("bossPayoutsMain").append(DELIMETER_VALUE_PARAM).append(statBoss.getPayoutsMain().toDoubleCents()).append(DELIMETER_PARAM_FIELD);
        addSpecialWeapons(sb, statBoss.getSpecialWeaponsStats());
        sb.append(DELIMETER_FIELDS);
        sb.append("weaponSurplusVBA").append(DELIMETER_VALUE_PARAM).append(weaponSurplusVBA == null ? "" : weaponSurplusVBA).append(DELIMETER_FIELDS);
        sb.append("weaponSurplusMoney").append(DELIMETER_VALUE_PARAM).append(weaponSurplusMoney == null ? 0 :
                weaponSurplusMoney.toDoubleCents()).append(DELIMETER_FIELDS);

        sb.append(DELIMETER_FIELDS);
        sb.append("lbContributions").append(DELIMETER_VALUE_PARAM);
        sb.append(DELIMETER_FIELDS);
        sb.append("quests").append(DELIMETER_VALUE_PARAM);
        sb.append(questCompletedCount).append(DELIMETER_PARAM_FIELD);
        sb.append(questPayouts.toDoubleCents()).append(DELIMETER_PARAM_FIELD);

        questWeapons.forEach((id, count) ->
                sb.append(id).append(DELIMETER_VALUE_PARAM_SECOND).append(count).append(DELIMETER_PARAM_FIELD));
        sb.append(DELIMETER_FIELDS);
        sb.append("additionalData").append(DELIMETER_VALUE_PARAM).append(additionalData);
        sb.append(DELIMETER_FIELDS);
        sb.append("maxShotTotalWin").append(DELIMETER_VALUE_PARAM).append(maxShotTotalWin);
        sb.append(DELIMETER_FIELDS);

        Pair<Double, String> realShotData = getRealShotData();
        sb.append("realWeapon").append(DELIMETER_VALUE_PARAM).append(realShotData.getKey())
                .append(DELIMETER_PARAM_FIELD).append(realShotData.getValue());
        sb.append(DELIMETER_FIELDS);


        Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins = getAdditionalWins();
        if (!additionalWins.isEmpty()) {
            StringBuilder sbAdditionalWins = new StringBuilder();
            additionalWins.forEach((s, pair) -> sbAdditionalWins.append(s).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(pair.getKey()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(pair.getValue()).append(DELIMETER_PARAM_FIELD));

            sb.append("additionalWins").append(DELIMETER_VALUE_PARAM).append(sbAdditionalWins);
            sb.append(DELIMETER_FIELDS);
        } else {
            sb.append("additionalWins").append(DELIMETER_VALUE_PARAM).append("no additional wins");
            sb.append(DELIMETER_FIELDS);
        }

        getLootBoxStat().forEach((cost, lootBoxStat) -> {
            StringBuilder lootBox = new StringBuilder();
            lootBox.append("lootBox").append(DELIMETER_VALUE_PARAM)
                    .append(lootBoxStat.getCost()).append(DELIMETER_PARAM_FIELD);
            lootBoxStat.getWeaponShots().forEach((wName, param) ->
                    lootBox.append("w_").append(wName).append(DELIMETER_VALUE_PARAM_SECOND)
                            .append(param.getKey()).append(DELIMETER_VALUE_PARAM_SECOND)
                            .append(param.getValue()).append(DELIMETER_VALUE_PARAM_SECOND)
                            .append(DELIMETER_PARAM_FIELD));
            sb.append(lootBox).append(DELIMETER_FIELDS);
        });

        StringBuilder hitMissStat = new StringBuilder();
        getHitMissStatByWeapons().forEach((weaponId, data) ->
                hitMissStat.append(weaponId).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfRealShots()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfMiss()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfKilledMiss()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfCompensateHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(data.getNumberOfMathHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                        .append(DELIMETER_PARAM_FIELD));
        sb.append("hitMiss").append(DELIMETER_VALUE_PARAM).append(hitMissStat).append(DELIMETER_FIELDS);


        StringBuilder wStatBySources = new StringBuilder();
        getWeaponStatBySources().forEach((s, weaponSourceIntegerMap) -> {
            wStatBySources.append(s).append(DELIMETER_VALUE_PARAM_SECOND);
            weaponSourceIntegerMap.forEach((source, integer) ->
                    wStatBySources.append(source.getTitle())
                            .append(DELIMETER_VALUE_PARAM)
                            .append(integer)
                            .append(DELIMETER_VALUE_PARAM_SECOND));
            wStatBySources.append(DELIMETER_PARAM_FIELD);
        });
        sb.append("wStatBySources").append(DELIMETER_VALUE_PARAM).append(wStatBySources).append(DELIMETER_FIELDS);

        sb.append("freeShotsWon").append(DELIMETER_VALUE_PARAM).append(freeShotsWon).append(DELIMETER_FIELDS);
        sb.append("moneyWheelCompleted").append(DELIMETER_VALUE_PARAM).append(moneyWheelCompleted).append(DELIMETER_FIELDS);
        sb.append("moneyWheelPayouts").append(DELIMETER_VALUE_PARAM).append(moneyWheelPayouts).append(DELIMETER_FIELDS);

        return sb.toString();
    }

    protected void addSpecialWeapons(StringBuilder sb, Map<String, WeaponStat> weaponsStats) {
        for (Map.Entry<String, WeaponStat> weaponStatEntry : weaponsStats.entrySet()) {
            String nameWeapon = weaponStatEntry.getKey();
            WeaponStat wStats = weaponStatEntry.getValue();
            sb.append("w_").append(nameWeapon).append(DELIMETER_VALUE_PARAM)
                    .append(wStats.getPayouts().toDoubleCents()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(wStats.getCntHits()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(wStats.getCnt()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(wStats.getPayBets().toDoubleCents()).append(DELIMETER_VALUE_PARAM_SECOND)
                    .append(DELIMETER_PARAM_FIELD);
        }
    }


    public Pair<Double, String> getRealShotData() {
        StringBuilder shots = new StringBuilder();
        AtomicReference<Double> totalBetsByStats = new AtomicReference<>((double) 0);
        realShotsCount.forEach((integer, realWeaponCounts) -> {
            Pair<String, Double> data = realWeaponCounts.getData();
            totalBetsByStats.updateAndGet(v -> v + data.getValue());
            shots.append(integer).append(DELIMETER_VALUE_PARAM_SECOND).append(data.getKey()).append(DELIMETER_PARAM_FIELD);
        });
        return new Pair<>(totalBetsByStats.get(), shots.toString());
    }

    protected void fillPlayerRoundInfo(AbstractPlayerRoundInfo res, String data, double bets, double wins) {
        String[] params = data.split(DELIMETER_FIELDS);
        int idx = 0;
        res.setTimeOfRoundEnd(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setXpearned(new Experience(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1])));
        res.setPlayerRoundId(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setRoomRoundId(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setRoomId(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setAmmoAmountBuyIn(Integer.parseInt(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setAmmoAmountReturned(Integer.parseInt(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setRoomStake(Long.parseLong(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        Money betsSpecialWeapons = getMoney(Double.parseDouble(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setTotalBets(getMoney(bets).subtract(betsSpecialWeapons));
        Map<String, EnemyStat> statByEnemies = new HashMap<>();

        for (int i = 0; i < res.getBaseEnemies().size(); i++) {
            String[] enemyParams = params[idx++].split(DELIMETER_PARAM_FIELD);
            String name = enemyParams[0].split(DELIMETER_VALUE_PARAM)[1];
            boolean isHV = enemyParams[5].split(DELIMETER_VALUE_PARAM)[1].equals("true");
            EnemyStat enemyStat = new EnemyStat(isHV, 808);
            enemyStat.setCntShotsToEnemy(Integer.parseInt(enemyParams[1].split(DELIMETER_VALUE_PARAM)[1]));
            enemyStat.setPayouts(getMoney(Double.parseDouble(enemyParams[2].split(DELIMETER_VALUE_PARAM)[1])));
            enemyStat.setCntKills(Integer.parseInt(enemyParams[3].split(DELIMETER_VALUE_PARAM)[1]));
            enemyStat.setHvBets(getMoney(Double.parseDouble(enemyParams[4].split(DELIMETER_VALUE_PARAM)[1])));
            enemyStat.setMainBets(getMoney(Double.parseDouble(enemyParams[6].split(DELIMETER_VALUE_PARAM)[1])));
            enemyStat.setSpecialWeaponsStats(parseSpecialWeapons(enemyParams, 7));
            statByEnemies.put(name, enemyStat);
        }
        res.setStatByEnemies(statByEnemies);

        String bossStats = params[idx++];
        String[] bossParams = bossStats.split(DELIMETER_PARAM_FIELD);
        int bossCnt = Integer.parseInt(bossParams[0].split(DELIMETER_VALUE_PARAM)[1]);
        int bossCntKilled = Integer.parseInt(bossParams[1].split(DELIMETER_VALUE_PARAM)[1]);
        Money bossBetsExtra = getMoney(Double.parseDouble(bossParams[2].split(DELIMETER_VALUE_PARAM)[1]));
        Money bossPayoutsExtra = getMoney(Double.parseDouble(bossParams[3].split(DELIMETER_VALUE_PARAM)[1]));
        Money bossPayoutsMain = getMoney(Double.parseDouble(bossParams[4].split(DELIMETER_VALUE_PARAM)[1]));
        res.setStatBoss(new BossStat(bossBetsExtra, bossPayoutsExtra, bossPayoutsMain, bossCnt, bossCntKilled,
                parseSpecialWeapons(bossParams, 5)));

        res.setWeaponSurplusVBA(params[idx++]);
        Money weaponSurplusMoney = getMoney(Double.parseDouble(params[idx++].split(DELIMETER_VALUE_PARAM)[1]));
        res.setWeaponSurplusMoney(weaponSurplusMoney);
        res.setTotalPayouts(getMoney(wins).subtract(bossPayoutsMain).subtract(weaponSurplusMoney));
        idx++; // skip old field
        idx++; // skip leaderbords
        if (params.length > idx) {
            String[] quests = params[idx++].split(DELIMETER_VALUE_PARAM)[1].split("&");
            res.questCompletedCount = Integer.parseInt(quests[0]);
            res.questPayouts = getMoney(Double.parseDouble(quests[1]));
        }

        if (params.length >= 30) {
            String paramWins = params[30].split(DELIMETER_VALUE_PARAM)[1];
            String[] additionalWins = paramWins.split(DELIMETER_PARAM_FIELD);
            Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins_ = new HashMap<>();
            for (String additionalWin : additionalWins) {
                String[] split = additionalWin.split(DELIMETER_VALUE_PARAM_SECOND);
                String description = split[0];
                AtomicInteger number = new AtomicInteger(Integer.parseInt(split[1]));
                AtomicDouble win = new AtomicDouble(Double.parseDouble(split[2]));
                additionalWins_.put(description, new Pair<>(number, win));
            }
            res.setAdditionalWins(additionalWins_);
        }

    }

    protected static Map<String, WeaponStat> parseSpecialWeapons(String[] params, int from) {
        Map<String, WeaponStat> res = new HashMap<>();
        for (int j = from; j < params.length; j++) {
            String[] split = params[j].split(DELIMETER_VALUE_PARAM);
            String wName = split[0].substring(2);
            String[] params_weapon = split[1].split(DELIMETER_VALUE_PARAM_SECOND);
            Money pay = getMoney(Double.parseDouble(params_weapon[0]));
            int cntHits = Integer.parseInt(params_weapon[1]);
            int cnt = Integer.parseInt(params_weapon[2]);
            Money payBets = getMoney(Double.parseDouble(params_weapon[3]));
            res.put(wName, new WeaponStat(cnt, pay, cntHits, payBets));
        }
        return res;
    }


    protected double[] update(Map<String, WeaponStat> swStat, Map<String, SWEntry> weaponStats) {
        double totalSpecialWinEnemy = 0;
        double cntShotsFromSpecialWeapons = 0;
        for (Map.Entry<String, WeaponStat> weaponStatEntry : swStat.entrySet()) {
            WeaponStat weaponStatEntryValue = weaponStatEntry.getValue();
            totalSpecialWinEnemy += weaponStatEntryValue.getPayouts().toDoubleCents();
            cntShotsFromSpecialWeapons += weaponStatEntryValue.getCnt();
            weaponStats.get(weaponStatEntry.getKey()).update(weaponStatEntryValue.getCnt(), weaponStatEntryValue.getPayouts().toDoubleCents());
        }

        return new double[]{totalSpecialWinEnemy, cntShotsFromSpecialWeapons};
    }

    public void addRoundInfo(AbstractPlayerRoundInfo<ENEMY_TYPE, ENEMY_STAT, PRI> newRoundInfo) {
        this.addXpearned(newRoundInfo.xpearned);
        this.totalBets = this.totalBets.add(newRoundInfo.getTotalBets());
        this.totalPayouts = this.totalPayouts.add(newRoundInfo.getTotalPayouts());
        this.addAmmoAmountBuyIn(newRoundInfo.ammoAmountBuyIn);
        this.addAmmoAmountReturned(newRoundInfo.ammoAmountReturned);
        this.weaponSurplusMoney = this.weaponSurplusMoney.add(newRoundInfo.getWeaponSurplusMoney());

        for (Map.Entry<String, ENEMY_STAT> statEntry : this.statByEnemies.entrySet()) {
            String key = statEntry.getKey();
            ENEMY_STAT enemyStat = statEntry.getValue();
            Map<String, ENEMY_STAT> statByEnemies = newRoundInfo.getStatByEnemies();
            EnemyStat newEnemyStat = (EnemyStat) statByEnemies.get(key);
            if (newEnemyStat != null) {
                enemyStat.setHvBets(enemyStat.getHvBets().add(newEnemyStat.getHvBets()));
                enemyStat.setMainBets(enemyStat.getMainBets().add(newEnemyStat.getMainBets()));
                enemyStat.setPayouts(enemyStat.getPayouts().add(newEnemyStat.getPayouts()));
                enemyStat.setCntShotsToEnemy(enemyStat.getCntShotsToEnemy() + newEnemyStat.getCntShotsToEnemy());
                enemyStat.setCntKills(enemyStat.getCntKills() + newEnemyStat.getCntKills());
                updateWeapons(enemyStat.getSpecialWeaponsStats(), newEnemyStat.getSpecialWeaponsStats());
            }
        }

        BossStat addBoss = newRoundInfo.statBoss;
        BossStat boss = this.statBoss;
        boss.setBetsExtra(boss.getBetsExtra().add(addBoss.getBetsExtra()));
        boss.setPayoutsExtra(boss.getPayoutsExtra().add(addBoss.getPayoutsExtra()));
        boss.setPayoutsMain(boss.getPayoutsMain().add(addBoss.getPayoutsMain()));
        boss.setCntKillsOfBoss(boss.getCntKillsOfBoss() + addBoss.getCntKillsOfBoss());
        boss.setCntToBoss(boss.getCntToBoss() + addBoss.getCntToBoss());
        updateWeapons(boss.getSpecialWeaponsStats(), addBoss.getSpecialWeaponsStats());

        questCompletedCount += newRoundInfo.questCompletedCount;
        questPayouts = questPayouts.add(newRoundInfo.questPayouts);

        Map<String, Pair<AtomicInteger, AtomicDouble>> newRoundInfoAdditionalWins = newRoundInfo.getAdditionalWins();

        for (Map.Entry<String, Pair<AtomicInteger, AtomicDouble>> pairEntry : newRoundInfoAdditionalWins.entrySet()) {
            String description = pairEntry.getKey();
            Pair<AtomicInteger, AtomicDouble> pair = pairEntry.getValue();
            Pair<AtomicInteger, AtomicDouble> doublePair = additionalWins.get(description);
            if (doublePair == null) {
                doublePair = new Pair<>(new AtomicInteger(pair.getKey().get())
                        , new AtomicDouble(pair.getValue().get()));
                additionalWins.put(description, doublePair);
            } else {
                doublePair.getKey().addAndGet(pair.getKey().get());
                doublePair.getValue().addAndGet(pair.getValue().get());
            }
        }

        addFreeShotsWon(newRoundInfo.freeShotsWon);
        addMoneyWheelCompleted(newRoundInfo.moneyWheelCompleted);
        addMoneyWheelPayouts(newRoundInfo.moneyWheelPayouts);

        addDamage(newRoundInfo.totalDamage);
    }

    private void updateWeapons(Map<String, WeaponStat> weaponsStats, Map<String, WeaponStat> newWeaponsStats) {
        for (Map.Entry<String, WeaponStat> entry : weaponsStats.entrySet()) {
            WeaponStat baseWeaponStat = entry.getValue();
            WeaponStat newWeaponStat = newWeaponsStats.get(entry.getKey());
            if (newWeaponStat != null) {
                baseWeaponStat.setPayouts(baseWeaponStat.getPayouts().add(newWeaponStat.getPayouts()));
                baseWeaponStat.setCntHits(baseWeaponStat.getCntHits() + newWeaponStat.getCntHits());
                baseWeaponStat.setCnt(baseWeaponStat.getCnt() + newWeaponStat.getCnt());
                baseWeaponStat.setPayBets(baseWeaponStat.getPayBets().add(newWeaponStat.getPayBets()));
            }
        }
    }

    protected static Money getMoney(double value) {
        return Money.fromCents(new BigDecimal(value, MathContext.DECIMAL32).longValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractPlayerRoundInfo that = (AbstractPlayerRoundInfo) o;
        return xpearned == that.xpearned &&
                playerRoundId == that.playerRoundId &&
                roomRoundId == that.roomRoundId &&
                roomId == that.roomId &&
                timeOfRoundEnd == that.timeOfRoundEnd &&
                ammoAmountBuyIn == that.ammoAmountBuyIn &&
                ammoAmountReturned == that.ammoAmountReturned &&
                roomStake == that.roomStake &&
                Objects.equals(totalBets, that.totalBets) &&
                Objects.equals(totalPayouts, that.totalPayouts) &&
                Objects.equals(statByEnemies, that.statByEnemies) &&
                Objects.equals(statBoss, that.statBoss);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalBets, totalPayouts, xpearned, playerRoundId, roomRoundId, roomId,
                timeOfRoundEnd, ammoAmountBuyIn, ammoAmountReturned, roomStake, statByEnemies, statBoss);
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "PlayerRoundInfo[" +
                ", totalBets=" + totalBets +
                ", totalPayouts=" + totalPayouts +
                ", playerRoundId=" + playerRoundId +
                ", roomRoundId=" + roomRoundId +
                ", roomId=" + roomId +
                ", timeOfRoundEnd=" + timeOfRoundEnd +
                ", ammoAmountBuyIn=" + ammoAmountBuyIn +
                ", ammoAmountReturned=" + ammoAmountReturned +
                ", roomStake=" + roomStake +
                ", weaponSurplusVBA=" + weaponSurplusVBA +
                ", weaponSurplusMoney=" + weaponSurplusMoney +
                ", questCompletedCount=" + questCompletedCount +
                ", questPayouts=" + questPayouts +
                ", questWeapons=" + questWeapons +
                ", additionalData=" + additionalData +
                ", maxShotTotalWin=" + maxShotTotalWin +
                ", realShotsCount=" + getRealShotsCount() +
                ", additionalWins=" + additionalWins +
                ", lootBoxStat=" + getLootBoxStat() +
                ", hitMissStatByWeapons=" + getHitMissStatByWeapons() +
                ", freeShotsWon=" + freeShotsWon +
                ", moneyWheelCompleted=" + moneyWheelCompleted +
                ", moneyWheelPayouts=" + moneyWheelPayouts +
                ", moneyWheelPayouts=" + getRoundKPIInfo() +
                ", battlegroundMode=" + battlegroundMode +
                ", battleBet=" + battleBet +
                ", battleWin=" + battleWin +
                ']';
    }

    @Override
    public void updateAdditionalData(String currentModel) {
        if (StringUtils.isTrimmedEmpty(additionalData) && !StringUtils.isTrimmedEmpty(currentModel) && playerRoundId > 0) {
            int idx = playerRoundId % 2 == 0 ? 0 : 2;
            StringBuilder sb = new StringBuilder();
            String[] params = currentModel.split(",");
            int roomsSize = Integer.parseInt(params[1]);
            for (int i = 0; i < 3; i++) {
                if (i == idx) {
                    sb.append(params[0]);
                } else {
                    sb.append(RNG.nextInt(roomsSize) + 1);
                }
            }
            int number = Integer.parseInt(sb.toString());
            additionalData = String.valueOf(number * playerRoundId);
        }
    }

    public Map<Integer, RealWeaponCounts> getRealShotsCount() {
        return realShotsCount;
    }

    public void setRealShotsCount(Map<Integer, RealWeaponCounts> realShotsCount) {
        this.realShotsCount = realShotsCount;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObject(output, totalBets);
        kryo.writeObject(output, totalPayouts);
        kryo.writeObject(output, roundStartBalance);
        kryo.writeClassAndObject(output, xpearned);
        output.writeLong(playerRoundId, true);
        output.writeLong(roomRoundId, true);
        output.writeLong(roomId, true);
        output.writeLong(timeOfRoundEnd, true);
        output.writeInt(ammoAmountBuyIn, true);
        output.writeInt(ammoAmountReturned, true);
        output.writeLong(roomStake, true);
        kryo.writeClassAndObject(output, statByEnemies);
        kryo.writeObjectOrNull(output, statBoss, BossStat.class);
        output.writeString(weaponSurplusVBA);
        kryo.writeObject(output, weaponSurplusMoney);
        output.writeInt(questCompletedCount);
        kryo.writeObject(output, questPayouts);
        kryo.writeClassAndObject(output, questWeapons);
        output.writeString(additionalData);
        output.writeLong(maxShotTotalWin, true);
        kryo.writeClassAndObject(output, realShotsCount);
        kryo.writeClassAndObject(output, additionalWins);
        kryo.writeClassAndObject(output, lootBoxStat);
        kryo.writeClassAndObject(output, hitMissStatByWeapons);
        kryo.writeClassAndObject(output, weaponStatBySources);
        output.writeInt(freeShotsWon, true);
        output.writeInt(moneyWheelCompleted, true);
        output.writeLong(moneyWheelPayouts, true);
        output.writeDouble(totalDamage);
        kryo.writeClassAndObject(output, roundKPIInfo);
        output.writeBoolean(battlegroundMode);
        output.writeLong(battleBet, true);
        output.writeLong(battleWin, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        totalBets = kryo.readObject(input, Money.class);
        totalPayouts = kryo.readObject(input, Money.class);
        roundStartBalance = kryo.readObject(input, Money.class);
        xpearned = (IExperience) kryo.readClassAndObject(input);
        playerRoundId = input.readLong(true);
        roomRoundId = input.readLong(true);
        roomId = input.readLong(true);
        timeOfRoundEnd = input.readLong(true);
        ammoAmountBuyIn = input.readInt(true);
        ammoAmountReturned = input.readInt(true);
        roomStake = input.readLong(true);
        statByEnemies = (Map<String, ENEMY_STAT>) kryo.readClassAndObject(input);
        statBoss = kryo.readObjectOrNull(input, BossStat.class);
        weaponSurplusVBA = input.readString();
        weaponSurplusMoney = kryo.readObject(input, Money.class);
        questCompletedCount = input.readInt();
        questPayouts = kryo.readObject(input, Money.class);
        questWeapons = (Map<Integer, Integer>) (kryo.readClassAndObject(input));
        additionalData = input.readString();
        maxShotTotalWin = input.readLong(true);
        realShotsCount = (Map<Integer, RealWeaponCounts>) kryo.readClassAndObject(input);

        additionalWins = (Map<String, Pair<AtomicInteger, AtomicDouble>>) kryo.readClassAndObject(input);

        lootBoxStat = (Map<Integer, LootBoxStat>) kryo.readClassAndObject(input);
        hitMissStatByWeapons = (Map<Integer, AdditionalWeaponStat>) kryo.readClassAndObject(input);

        weaponStatBySources = (Map<String, Map<WeaponSource, Integer>>) kryo.readClassAndObject(input);

        freeShotsWon = input.readInt(true);
        moneyWheelCompleted = input.readInt(true);
        moneyWheelPayouts = input.readLong(true);
        totalDamage = input.readDouble();

        roundKPIInfo = (RoundKPIInfo) kryo.readClassAndObject(input);
        battlegroundMode = input.readBoolean();
        if (version > 0) {
            battleBet = input.readLong(true);
            battleWin = input.readLong(true);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("totalBets", totalBets);
        gen.writeObjectField("totalPayouts", totalPayouts);
        gen.writeObjectField("roundStartBalance", roundStartBalance);
        gen.writeObjectField("xpearned", xpearned);
        gen.writeNumberField("playerRoundId", playerRoundId);
        gen.writeNumberField("roomRoundId", roomRoundId);
        gen.writeNumberField("roomId", roomId);
        gen.writeNumberField("timeOfRoundEnd", timeOfRoundEnd);
        gen.writeNumberField("ammoAmountBuyIn", ammoAmountBuyIn);
        gen.writeNumberField("ammoAmountReturned", ammoAmountReturned);
        gen.writeNumberField("roomStake", roomStake);
        serializeMapField(gen, "statByEnemies", statByEnemies, new TypeReference<Map<String,ENEMY_STAT>>() {});
        gen.writeObjectField("statBoss", statBoss);
        gen.writeStringField("weaponSurplusVBA", weaponSurplusVBA);
        gen.writeObjectField("weaponSurplusMoney", weaponSurplusMoney);
        gen.writeNumberField("questCompletedCount", questCompletedCount);
        gen.writeObjectField("questPayouts", questPayouts);
        serializeMapField(gen, "questWeapons", questWeapons, new TypeReference<Map<Integer,Integer>>() {});
        gen.writeStringField("additionalData", additionalData);
        gen.writeNumberField("maxShotTotalWin", maxShotTotalWin);
        serializeMapField(gen, "realShotsCount", realShotsCount, new TypeReference<Map<Integer,RealWeaponCounts>>() {});
        serializeMapField(gen, "additionalWins", additionalWins, new TypeReference<Map<String, Pair<AtomicInteger, AtomicDouble>>>() {});
        serializeMapField(gen, "lootBoxStat", lootBoxStat, new TypeReference<Map<Integer, LootBoxStat>>() {});
        serializeMapField(gen, "hitMissStatByWeapons", hitMissStatByWeapons, new TypeReference<Map<Integer, AdditionalWeaponStat>>() {});
        serializeMapField(gen, "weaponStatBySources", weaponStatBySources, new TypeReference<Map<String, Map<WeaponSource, Integer>>>() {});
        gen.writeNumberField("freeShotsWon", freeShotsWon);
        gen.writeNumberField("moneyWheelCompleted", moneyWheelCompleted);
        gen.writeNumberField("moneyWheelPayouts", moneyWheelPayouts);
        gen.writeNumberField("totalDamage", totalDamage);
        gen.writeObjectField("roundKPIInfo", roundKPIInfo);
        gen.writeBooleanField("battlegroundMode", battlegroundMode);
        gen.writeNumberField("battleBet", battleBet);
        gen.writeNumberField("battleWin", battleWin);

        serializeAdditionalFields(gen, serializers);
    }

    @Override
    public PRI deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        totalBets = om.convertValue(node.get("totalBets"), Money.class);
        totalPayouts = om.convertValue(node.get("totalPayouts"), Money.class);
        roundStartBalance = om.convertValue(node.get("roundStartBalance"), Money.class);
        xpearned = om.convertValue(node.get("xpearned"), IExperience.class);
        playerRoundId = node.get("playerRoundId").longValue();
        roomRoundId = node.get("roomRoundId").longValue();
        roomId = node.get("roomId").longValue();
        timeOfRoundEnd = node.get("timeOfRoundEnd").longValue();
        ammoAmountBuyIn = node.get("ammoAmountBuyIn").intValue();
        ammoAmountReturned = node.get("ammoAmountReturned").intValue();
        roomStake = node.get("roomStake").longValue();
        statByEnemies = om.convertValue(node.get("statByEnemies"), new TypeReference<Map<String, ENEMY_STAT>>() {});
        statBoss = om.convertValue(node.get("statBoss"), BossStat.class);
        weaponSurplusVBA = node.get("weaponSurplusVBA").textValue();
        weaponSurplusMoney = om.convertValue(node.get("weaponSurplusMoney"), Money.class);
        questCompletedCount = node.get("questCompletedCount").intValue();
        questPayouts = om.convertValue(node.get("questPayouts"), Money.class);
        questWeapons = om.convertValue(node.get("questWeapons"), new TypeReference<Map<Integer, Integer>>() {});
        additionalData = node.get("additionalData").textValue();
        maxShotTotalWin = node.get("maxShotTotalWin").longValue();
        realShotsCount = om.convertValue(node.get("realShotsCount"), new TypeReference<Map<Integer, RealWeaponCounts>>() {});

        additionalWins = om.convertValue(node.get("additionalWins"), new TypeReference<Map<String, Pair<AtomicInteger, AtomicDouble>>>(){});

        lootBoxStat = om.convertValue(node.get("lootBoxStat"), new TypeReference<Map<Integer, LootBoxStat>>() {});
        hitMissStatByWeapons = om.convertValue(node.get("hitMissStatByWeapons"), new TypeReference<Map<Integer, AdditionalWeaponStat>>() {});

        weaponStatBySources = om.convertValue(node.get("weaponStatBySources"), new TypeReference<Map<String, Map<WeaponSource, Integer>>>(){});

        freeShotsWon = node.get("freeShotsWon").intValue();
        moneyWheelCompleted = node.get("moneyWheelCompleted").intValue();
        moneyWheelPayouts = node.get("moneyWheelPayouts").longValue();
        totalDamage = node.get("totalDamage").doubleValue();

        roundKPIInfo = om.convertValue(node.get("roundKPIInfo"), RoundKPIInfo.class);
        battlegroundMode = node.get("battlegroundMode").booleanValue();
        battleBet = node.get("battleBet").longValue();
        battleWin = node.get("battleWin").longValue();

        deserializeAdditionalFields(p, node, ctxt);

        return getDeserialized();
    }

    protected abstract PRI getDeserialized();

    protected abstract void deserializeAdditionalFields(JsonParser p,
                                                        JsonNode node,
                                                        DeserializationContext ctxt) throws IOException;

    protected abstract void serializeAdditionalFields(JsonGenerator gen,
                                                      SerializerProvider serializers) throws IOException;

    public class SWEntry {
        double cnt;
        double payout;

        public double getCnt() {
            return cnt;
        }

        public void setCnt(double cnt) {
            this.cnt = cnt;
        }

        public double getPayout() {
            return payout;
        }

        public void setPayout(double payout) {
            this.payout = payout;
        }

        public void update(double cnt, double payout) {
            this.cnt += cnt;
            this.payout += payout;
        }

        public double getAvgPayout() {
            return payout / cnt;
        }
    }

    @Override
    public Set<String> getWeaponTitles(int gameId) {
        return Arrays.stream(SpecialWeaponType.values())
                .filter(specialWeaponType -> specialWeaponType.getAvailableGameIds().contains(gameId))
                .map(SpecialWeaponType::getTitle).collect(Collectors.toSet());
    }

    public void setAdditionalWins(Map<String, Pair<AtomicInteger, AtomicDouble>> additionalWins) {
        this.additionalWins = additionalWins;
    }

    public int getQuestCompletedCount() {
        return questCompletedCount;
    }

    public Money getQuestPayouts() {
        return questPayouts;
    }

    public Map<Integer, Integer> getQuestWeapons() {
        return questWeapons;
    }

    public Map<Integer, LootBoxStat> getLootBoxStat() {
        if (lootBoxStat == null) {
            lootBoxStat = new HashMap<>();
        }
        return lootBoxStat;
    }

    public void addLootBoxStat(int cost, int boxId, int shots, String weaponTitle) {
        LootBoxStat lootBox = getLootBoxStat().getOrDefault(boxId, new LootBoxStat(cost));
        lootBox.addWeaponShots(weaponTitle, shots);
        lootBoxStat.putIfAbsent(boxId, lootBox);
        addWeaponSourceStat(WeaponSource.LOOT_BOX.getTitle(), weaponTitle, shots);
    }


    public void addRealShotsCounter(int weaponId, int cnt) {
        addHitAndMissStat(weaponId, cnt, 0, 0, 0, 0, 0);
    }

    public void addMathHitCounter(int weaponId, int cnt) {
        addHitAndMissStat(weaponId, 0, 0, 0, 0, 0, cnt);
    }

    public void addHitCounter(int weaponId, int cnt) {
        addHitAndMissStat(weaponId, 0, cnt, 0, 0, 0, 0);
    }

    public void addMissCounter(int weaponId, int cnt) {
        addHitAndMissStat(weaponId, 0, 0, cnt, 0, 0, 0);
    }

    public void addKilledMissCounter(int weaponId, int cnt) {
        addHitAndMissStat(weaponId, 0, 0, 0, cnt, 0, 0);
    }

    public void addCompensateHitsCounter(int weaponId, int cnt) {
        addHitAndMissStat(weaponId, 0, 0, 0, 0, cnt, 0);
    }

    public void addHitAndMissStat(int weaponId, int cntRealShots, int cntHits, int cntMiss,
                                  int cntKilledMiss, int cntCompensateHits, int cntMathHits) {
        AdditionalWeaponStat stat = getHitMissStatByWeapons().getOrDefault(weaponId, new AdditionalWeaponStat());
        stat.addNumberOfRealShots(cntRealShots);
        stat.addNumberOfHits(cntHits);
        stat.addNumberOfMiss(cntMiss);
        stat.addNumberOfKilledMiss(cntKilledMiss);
        stat.addNumberOfCompensateHits(cntCompensateHits);
        stat.addNumberOfMathHits(cntMathHits);
        hitMissStatByWeapons.putIfAbsent(weaponId, stat);
    }

    public Map<Integer, AdditionalWeaponStat> getHitMissStatByWeapons() {
        if (hitMissStatByWeapons == null) {
            hitMissStatByWeapons = new HashMap<>();
        }
        return hitMissStatByWeapons;
    }

    public int getGameId() {
        return gameId;
    }

    public void setLootBoxStat(Map<Integer, LootBoxStat> lootBoxStat) {
        this.lootBoxStat = lootBoxStat;
    }

    public void setHitMissStatByWeapons(Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons) {
        this.hitMissStatByWeapons = hitMissStatByWeapons;
    }

    public Map<String, Map<WeaponSource, Integer>> getWeaponStatBySources() {
        return weaponStatBySources == null ? new HashMap<>() : weaponStatBySources;
    }

    public void setWeaponStatBySources(Map<String, Map<WeaponSource, Integer>> weaponStatBySources) {
        this.weaponStatBySources = weaponStatBySources;
    }

    public void addWeaponSourceStat(String source, String weaponTitle, int shots) {
        WeaponSource weaponSource = WeaponSource.getSourceByTitle(source);
        Map<String, Map<WeaponSource, Integer>> weaponStatBySources = getWeaponStatBySources();
        Map<WeaponSource, Integer> weaponStat = weaponStatBySources.getOrDefault(weaponTitle, new HashMap<>());
        Integer oldValue = weaponStat.getOrDefault(weaponSource, 0);
        weaponStat.put(weaponSource, oldValue + shots);
        weaponStatBySources.putIfAbsent(weaponTitle, weaponStat);
    }

    @Override
    public double getTotalDamage() {
        return totalDamage;
    }

    public void addDamage(double damage) {
        totalDamage += damage;
    }

    public RoundKPIInfo getRoundKPIInfo() {
        if (roundKPIInfo == null) {
            roundKPIInfo = new RoundKPIInfo(0, 0, 0, 0, 0,
                    System.currentTimeMillis(), 0, new HashMap<>(), new HashMap<>(), new HashMap<>(),
                    new HashMap<>(), new HashMap<>(), 0);
        }
        return roundKPIInfo;
    }

    @Override
    public long getBattleBet() {
        return battleBet;
    }

    @Override
    public void setBattleBet(long battleBet) {
        this.battleBet = battleBet;
    }

    @Override
    public long getBattleWin() {
        return battleWin;
    }

    @Override
    public void setBattleWin(long battleWin) {
        this.battleWin = battleWin;
    }

    public int getKilledMissedNumber(int weaponId) {
        Map<Integer, AdditionalWeaponStat> hitMissStatByWeapons = getHitMissStatByWeapons();
        AdditionalWeaponStat additionalWeaponStat = hitMissStatByWeapons.get(weaponId);
        return additionalWeaponStat == null ? 0 : additionalWeaponStat.getNumberOfKilledMiss();
    }

    public boolean isShotSuccess(int weaponId, int oldNumberKilledMissed) {
        return getKilledMissedNumber(weaponId) == oldNumberKilledMissed;
    }




}
