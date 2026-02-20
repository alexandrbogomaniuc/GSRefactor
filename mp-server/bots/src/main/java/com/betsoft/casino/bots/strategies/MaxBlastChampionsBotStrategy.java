package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.AstronautBetData;
import com.betsoft.casino.bots.IManagedLobbyBot;
import com.betsoft.casino.bots.IUnifiedBotStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class MaxBlastChampionsBotStrategy implements IUnifiedBotStrategy {

    private static final Logger LOG = LogManager.getLogger(MaxBlastChampionsBotStrategy.class);
    protected IManagedLobbyBot bot;
    protected static final Random RANDOM = new Random();
    private final double multiplierMin;
    private final double multiplierMax;
    private long requestedByInAmount;
    private String crashMultiplierFunction = "Math.exp((t * 0.06012) / 1000)";
    private String crashTimeFunction = "Math.log(t) * 1000 / 0.06012";
    private final ScriptEngine scriptEngine;

    public MaxBlastChampionsBotStrategy(long crashTimeMinMs, long crashTimeMaxMs, long requestedByInAmount) {
        this(null, crashTimeMinMs, crashTimeMaxMs, requestedByInAmount);
    }

    public MaxBlastChampionsBotStrategy(ScriptEngine scriptEngine, long crashTimeMinMs, long crashTimeMaxMs, long requestedByInAmount) {
        this.scriptEngine = scriptEngine != null ? scriptEngine: new ScriptEngineManager().getEngineByName("JavaScript");
        this.multiplierMin = Math.max(calcCrashMultiplier(crashTimeMinMs), 1.01);
        this.multiplierMax = calcCrashMultiplier(crashTimeMaxMs);
        this.requestedByInAmount = requestedByInAmount;
        LOG.info("MaxBlastChampionsBotStrategy::MaxBlastChampionsBotStrategy: " +
                        "crashTimeMinMs={}, crashTimeMaxMs={}, requestedByInAmount={}, multiplierMin={}, multiplierMax={}",
                crashTimeMinMs, crashTimeMaxMs, requestedByInAmount, multiplierMin, multiplierMax);
    }

    public IManagedLobbyBot getBot() {
        return bot;
    }

    public void setBot(IManagedLobbyBot bot) {
        this.bot = bot;
    }

    private double calcCrashMultiplier(long milliseconds) {
        try {
            double crashMultiplier = calc(this.crashMultiplierFunction, milliseconds);
            BigDecimal bd = BigDecimal.valueOf(crashMultiplier);
            return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            LOG.error("Unable calculate crashMultiplier for bot=({},{}), , error: {}",
                    bot != null ? bot.getId() : null, bot != null ? bot.getNickname() : null, e.getMessage());
            return -1;
        }
    }

    private long calcCrashTime(double crashMultiplier) {
        try {
            return (long) calc(this.crashTimeFunction, crashMultiplier);
        } catch (Exception e) {
            LOG.error("Unable calculate crashTime for bot=({},{}), error: {}",
                    bot != null ? bot.getId() : null, bot != null ? bot.getNickname() : null, e.getMessage());
            return -1;
        }
    }

    protected double calc(String func, double time) throws ScriptException {
        scriptEngine.put("t", time);
        return (double) scriptEngine.eval(func);
    }

    public String getCrashMultiplierFunction() {
        return crashMultiplierFunction;
    }

    public void setCrashMultiplierFunction(String crashMultiplierFunction) {
        this.crashMultiplierFunction = crashMultiplierFunction;
    }

    public long getCrashBetRequestTime(long minTime, long maxTime) {
        long crashBetRequestTime;
        if(minTime < maxTime) {
            crashBetRequestTime = minTime + (long) (RANDOM.nextDouble() * (maxTime - minTime + 1));
            LOG.info("MaxBlastChampionsBotStrategy::getCrashBetRequestTime: bot=({},{}), crashBetRequestTime={} is random from minTime={} and maxTime={}",
                    bot != null ? bot.getId() : null, bot != null ? bot.getNickname() : null,
                    toHumanReadableFormat(crashBetRequestTime), toHumanReadableFormat(minTime), toHumanReadableFormat(maxTime));
        } else {
            crashBetRequestTime = maxTime;
            LOG.info("MaxBlastChampionsBotStrategy::getCrashBetRequestTime: bot=({},{}), crashBetRequestTime={} is maxTime={}",
                    bot != null ? bot.getId() : null, bot != null ? bot.getNickname() : null,
                    toHumanReadableFormat(crashBetRequestTime), toHumanReadableFormat(maxTime));
        }
        return crashBetRequestTime;
    }

    public double getMultiplierMin() {
        return multiplierMin;
    }

    public double getMultiplierMax() {
        return multiplierMax;
    }

    public long getRequestedByInAmount() {
        return requestedByInAmount;
    }

    public void setRequestedByInAmount(int requestedByInAmount) {
        this.requestedByInAmount = requestedByInAmount;
    }

    @Override
    public int getShots() {
        return 0;
    }

    @Override
    public void resetShots() {

    }

    @Override
    public boolean shouldShoot(String botId) {
        return false;
    }

    public boolean shouldShoot() {
        return false;
    }

    @Override
    public boolean shouldPurchaseWeaponLootBox() {
        return false;
    }

    @Override
    public boolean shouldSwitchWeapon() {
        return false;
    }

    @Override
    public boolean shouldPurchaseBullets() {
        return false;
    }

    @Override
    public long getWaitTime() {
        return 500L;
    }

    @Override
    public int getBuyInAmmoAmount(long balance, float stake, int minAmmo) {
        return 0;
    }

    @Override
    public void addWeapon(int id, int shots) {

    }

    @Override
    public int getWeaponId() {
        return 0;
    }

    @Override
    public void consumeAmmo(int weaponId) {

    }

    @Override
    public void resetWeapons() {

    }

    @Override
    public int getShotsForWeapon(int weaponId) {
        return 0;
    }

    @Override
    public AstronautBetData generateMultiplierForFirst(String nickname) {
        return null;
    }

    @Override
    public AstronautBetData generateMultiplierForSecond(String nickname) {
        return null;
    }

    @Override
    public AstronautBetData generateMultiplierForThird(String nickname) {
        return null;
    }

    @Override
    public int getNumberRoundBeforeRestart() {
        return 0;
    }

    @Override
    public double generateMultiplier() {
        double multiplier = multiplierMin + (multiplierMax - multiplierMin) * RANDOM.nextDouble();
        multiplier = BigDecimal.valueOf(multiplier).setScale(2, RoundingMode.HALF_UP).doubleValue();

        LOG.info("MaxBlastChampionsBotStrategy::generateMultiplier: botId={}, multiplier={} random from multiplierMin={} till multiplierMax={}",
                bot.getId(), multiplier, multiplierMin, multiplierMax);

        return multiplier;
    }
}
