package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.utils.TObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class RoundResult extends TObject implements IRoundResult<Seat, WeaponSurplus, LevelInfo> {
    private double winAmount;
    private double winRebuyAmount;
    private long balance;
    private int hitCount;
    private int missCount;
    private long currentScore;
    private long totalScore;
    private int nextMapId;
    private List<Seat> seats;
    private int enemiesKilledCount;
    private long winAmountInCredits;
    private long unusedBulletsCount;
    private double unusedBulletsMoney;
    private double totalBuyInMoney;
    private long xpPrev;
    private List<WeaponSurplus> weaponSurplus;
    private long totalKillsXP;
    private int totalTreasuresCount;
    private long totalTreasuresXP;
    private LevelInfo beforeRound;
    private LevelInfo afterRound;
    private long surplusHvBonus;
    private int questsCompletedCount;
    private long questsPayouts;
    private long roundId;
    private List<WeaponSurplus> weaponsReturned;
    private int bulletsFired;
    private double realWinAmount;
    private int freeShotsWon; // number of received free shots of SW
    private int moneyWheelCompleted;
    private long moneyWheelPayouts;
    private double totalDamage;
    private List<BattlegroundRoundResult> battlegroundRoundResult;
    private Double crashMultiplier;

    public RoundResult(long date, int rid, double winAmount, double winRebuyAmount, long balance, long currentScore,
                       long totalScore, int hitCount, int missCount, int nextMapId, List<ITransportSeat> seats,
                       int enemiesKilledCount, long winAmountInCredits, long unusedBulletsCount,
                       double unusedBulletsMoney, double totalBuyInMoney, long xpPrev, List<IWeaponSurplus> weaponSurplus,
                       long totalKillsXP, int totalTreasuresCount, long totalTreasuresXP, ILevelInfo beforeRound,
                       ILevelInfo afterRound, long surplusHvBonus, int questsCompletedCount,
                       long questsPayouts, long roundId, List<IWeaponSurplus> weaponsReturned, int bulletsFired,
                       double realWinAmount, int freeShotsWon, int moneyWheelCompleted, long moneyWheelPayouts,
                       double totalDamage, List<IBattlegroundRoundResult> battlegroundRoundResult) {
        super(date, rid);
        this.freeShotsWon = freeShotsWon;
        this.moneyWheelCompleted = moneyWheelCompleted;
        this.moneyWheelPayouts = moneyWheelPayouts;
        this.winAmount = winAmount;
        this.winRebuyAmount = winRebuyAmount;
        this.balance = balance;
        this.currentScore = currentScore;
        this.totalScore = totalScore;
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.nextMapId = nextMapId;
        this.seats = Seat.convert(seats);
        this.enemiesKilledCount = enemiesKilledCount;
        this.winAmountInCredits = winAmountInCredits;
        this.unusedBulletsCount = unusedBulletsCount;
        this.unusedBulletsMoney = unusedBulletsMoney;
        this.totalBuyInMoney = totalBuyInMoney;
        this.xpPrev = xpPrev;
        if (weaponSurplus != null) {
            this.weaponSurplus = WeaponSurplus.convert(weaponSurplus);
        }
        this.totalKillsXP = totalKillsXP;
        this.totalTreasuresCount = totalTreasuresCount;
        this.totalTreasuresXP = totalTreasuresXP;
        this.beforeRound = LevelInfo.convert(beforeRound);
        this.afterRound = LevelInfo.convert(afterRound);
        this.surplusHvBonus = surplusHvBonus;
        this.questsCompletedCount = questsCompletedCount;
        this.questsPayouts = questsPayouts;
        this.roundId = roundId;
        if (weaponsReturned != null) {
            this.weaponsReturned = WeaponSurplus.convert(weaponsReturned);
        }
        this.bulletsFired = bulletsFired;
        this.realWinAmount = realWinAmount;
        this.totalDamage = totalDamage;
        this.battlegroundRoundResult = BattlegroundRoundResult.convert(battlegroundRoundResult);
    }

    @Override
    public double getWinAmount() {
        return winAmount;
    }

    @Override
    public void setWinAmount(double winAmount) {
        this.winAmount = winAmount;
    }

    @Override
    public double getWinRebuyAmount() {
        return winRebuyAmount;
    }

    @Override
    public void setWinRebuyAmount(double winRebuyAmount) {
        this.winRebuyAmount = winRebuyAmount;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public long getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public void setCurrentScore(long currentScore) {
        this.currentScore = currentScore;
    }

    @Override
    public long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void setTotalScore(long totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    @Override
    public int getMissCount() {
        return missCount;
    }

    public void setMissCount(int missCount) {
        this.missCount = missCount;
    }

    @Override
    public int getNextMapId() {
        return nextMapId;
    }

    public void setNextMapId(int nextMapId) {
        this.nextMapId = nextMapId;
    }

    @Override
    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    @Override
    public int getEnemiesKilledCount() {
        return enemiesKilledCount;
    }

    public void setEnemiesKilledCount(int enemiesKilledCount) {
        this.enemiesKilledCount = enemiesKilledCount;
    }

    @Override
    public long getWinAmountInCredits() {
        return winAmountInCredits;
    }

    public void setWinAmountInCredits(long winAmountInCredits) {
        this.winAmountInCredits = winAmountInCredits;
    }

    @Override
    public long getUnusedBulletsCount() {
        return unusedBulletsCount;
    }

    public void setUnusedBulletsCount(long unusedBulletsCount) {
        this.unusedBulletsCount = unusedBulletsCount;
    }

    @Override
    public double getUnusedBulletsMoney() {
        return unusedBulletsMoney;
    }

    public void setUnusedBulletsMoney(double unusedBulletsMoney) {
        this.unusedBulletsMoney = unusedBulletsMoney;
    }

    @Override
    public double getTotalBuyInMoney() {
        return totalBuyInMoney;
    }

    public void setTotalBuyInMoney(double totalBuyInMoney) {
        this.totalBuyInMoney = totalBuyInMoney;
    }

    @Override
    public long getXpPrev() {
        return xpPrev;
    }

    public void setXpPrev(long xpPrev) {
        this.xpPrev = xpPrev;
    }

    @Override
    public List<WeaponSurplus> getWeaponSurplus() {
        return weaponSurplus;
    }

    public void setWeaponSurplus(List<WeaponSurplus> weaponSurplus) {
        this.weaponSurplus = weaponSurplus;
    }

    @Override
    public LevelInfo getBeforeRound() {
        return beforeRound;
    }

    public void setBeforeRound(LevelInfo beforeRound) {
        this.beforeRound = beforeRound;
    }

    @Override
    public LevelInfo getAfterRound() {
        return afterRound;
    }

    public void setAfterRound(LevelInfo afterRound) {
        this.afterRound = afterRound;
    }

    @Override
    public long getTotalKillsXP() {
        return totalKillsXP;
    }

    public void setTotalKillsXP(long totalKillsXP) {
        this.totalKillsXP = totalKillsXP;
    }

    @Override
    public int getTotalTreasuresCount() {
        return totalTreasuresCount;
    }

    public void setTotalTreasuresCount(int totalTreasuresCount) {
        this.totalTreasuresCount = totalTreasuresCount;
    }

    @Override
    public long getTotalTreasuresXP() {
        return totalTreasuresXP;
    }

    public void setTotalTreasuresXP(long totalTreasuresXP) {
        this.totalTreasuresXP = totalTreasuresXP;
    }

    @Override
    public long getSurplusHvBonus() {
        return surplusHvBonus;
    }

    public void setSurplusHvBonus(long surplusHvBonus) {
        this.surplusHvBonus = surplusHvBonus;
    }

    @Override
    public int getQuestsCompletedCount() {
        return questsCompletedCount;
    }

    public void setQuestsCompletedCount(int questsCompletedCount) {
        this.questsCompletedCount = questsCompletedCount;
    }

    @Override
    public long getQuestsPayouts() {
        return questsPayouts;
    }

    public void setQuestsPayouts(long questsPayouts) {
        this.questsPayouts = questsPayouts;
    }

    @Override
    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    @Override
    public int getBulletsFired() {
        return bulletsFired;
    }

    public void setBulletsFired(int bulletsFired) {
        this.bulletsFired = bulletsFired;
    }

    @Override
    public double getRealWinAmount() {
        return realWinAmount;
    }

    @Override
    public void setRealWinAmount(double realWinAmount) {
        this.realWinAmount = realWinAmount;
    }

    public int getFreeShotsWon() {
        return freeShotsWon;
    }

    public void setFreeShotsWon(int freeShotsWon) {
        this.freeShotsWon = freeShotsWon;
    }

    public int getMoneyWheelCompleted() {
        return moneyWheelCompleted;
    }

    public void setMoneyWheelCompleted(int moneyWheelCompleted) {
        this.moneyWheelCompleted = moneyWheelCompleted;
    }

    public long getMoneyWheelPayouts() {
        return moneyWheelPayouts;
    }

    public void setMoneyWheelPayouts(long moneyWheelPayouts) {
        this.moneyWheelPayouts = moneyWheelPayouts;
    }

    public double getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(double totalDamage) {
        this.totalDamage = totalDamage;
    }

    public List<BattlegroundRoundResult> getBattlegroundRoundResult() {
        return battlegroundRoundResult;
    }

    public void setBattlegroundRoundResult(List<BattlegroundRoundResult> battlegroundRoundResult) {
        this.battlegroundRoundResult = battlegroundRoundResult;
    }

    @Override
    public Double getCrashMultiplier() {
        return crashMultiplier;
    }

    public void setCrashMultiplier(Double crashMultiplier) {
        this.crashMultiplier = crashMultiplier;
    }

    public IRoundResult copy() {
        return new RoundResult(
        date,
        rid,
        winAmount,
        winRebuyAmount,
        balance,
        currentScore,
        totalScore,
        hitCount,
        missCount,
        nextMapId,
        seats != null ?
                seats.stream().map(seat -> (ITransportSeat) seat).collect(Collectors.toList()) :
                null,
        enemiesKilledCount,
        winAmountInCredits,
        unusedBulletsCount,
        unusedBulletsMoney,
        totalBuyInMoney,
        xpPrev,
        weaponSurplus != null ?
                weaponSurplus.stream().map(weapon -> (IWeaponSurplus) weapon).collect(Collectors.toList()) :
                null,
        totalKillsXP,
        totalTreasuresCount,
        totalTreasuresXP,
        beforeRound,
        afterRound,
        surplusHvBonus,
        questsCompletedCount,
        questsPayouts,
        roundId,
        weaponsReturned != null ?
                weaponsReturned.stream().map(weapon -> (IWeaponSurplus) weapon).collect(Collectors.toList()) :
                null,
        bulletsFired,
        realWinAmount,
        freeShotsWon,
        moneyWheelCompleted,
        moneyWheelPayouts,
        totalDamage,
        battlegroundRoundResult != null ?
                battlegroundRoundResult.stream().map(bgRoundResult -> (IBattlegroundRoundResult) bgRoundResult).collect(Collectors.toList()) :
                null
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoundResult that = (RoundResult) o;
        if (winAmount != that.winAmount) return false;
        if (balance != that.balance) return false;
        if (currentScore != that.currentScore) return false;
        if (totalScore != that.totalScore) return false;
        return seats.equals(that.seats);
    }

    @Override
    public String toString() {
        return "RoundResult{" +
                "date=" + date +
                ", rid=" + rid +
                ", winAmount=" + winAmount +
                ", winRebuyAmount=" + winRebuyAmount +
                ", balance=" + balance +
                ", hitCount=" + hitCount +
                ", missCount=" + missCount +
                ", currentScore=" + currentScore +
                ", totalScore=" + totalScore +
                ", nextMapId=" + nextMapId +
                ", seats=" + seats +
                ", enemiesKilledCount=" + enemiesKilledCount +
                ", winAmountInCredits=" + winAmountInCredits +
                ", unusedBulletsCount=" + unusedBulletsCount +
                ", unusedBulletsMoney=" + unusedBulletsMoney +
                ", totalBuyInMoney=" + totalBuyInMoney +
                ", xpPrev=" + xpPrev +
                ", weaponSurplus=" + weaponSurplus +
                ", totalKillsXP=" + totalKillsXP +
                ", totalTreasuresCount=" + totalTreasuresCount +
                ", totalTreasuresXP=" + totalTreasuresXP +
                ", beforeRound=" + beforeRound +
                ", afterRound=" + afterRound +
                ", surplusHvBonus=" + surplusHvBonus +
                ", questsCompletedCount=" + questsCompletedCount +
                ", questsPayouts=" + questsPayouts +
                ", roundId=" + roundId +
                ", weaponsReturned=" + weaponsReturned +
                ", bulletsFired=" + bulletsFired +
                ", realWinAmount=" + realWinAmount +
                ", freeShotsWon=" + freeShotsWon +
                ", moneyWheelCompleted=" + moneyWheelCompleted +
                ", moneyWheelPayouts=" + moneyWheelPayouts +
                ", totalDamage=" + totalDamage +
                ", battlegroundRoundResult=" + battlegroundRoundResult +
                ", crashMultiplier=" + crashMultiplier +
                '}';
    }
}
