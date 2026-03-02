package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.bots.strategies.IRoomNaturalBotStrategy;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.abs.casino.common.util.string.StringUtils;

import java.util.List;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class ShotRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final int weaponId;
    private final long enemyId;
    private final boolean isPaidSpecialShot;
    private boolean mainShotResultReceived = false;
    private final String bulletId;
    private final Integer weaponPrice;
    private float currentX;
    private float currentY;
    private String shotMetric;

    public ShotRequest(IRoomBot bot, ISocketClient client, int weaponId, long enemyId, boolean isPaidSpecialShot,
                       String bulletId, Integer weaponPrice, float currentX, float currentY, String shotMetric) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.weaponId = weaponId;
        this.enemyId = enemyId;
        this.isPaidSpecialShot = isPaidSpecialShot;
        this.bulletId = bulletId;
        this.weaponPrice = weaponPrice;
        this.currentX = currentX;
        this.currentY = currentY;
        this.shotMetric = shotMetric;
    }


    @Override
    public boolean isSingleResponse() {
        return mainShotResultReceived;
    }

    @Override
    public void send(int rid) {

        if(bot.getStrategy() instanceof IRoomNaturalBotStrategy && !StringUtils.isTrimmedEmpty(shotMetric)) {
            long timeMillis = System.currentTimeMillis();
            getLogger().debug("send: shot request was made by bot: {}, shotMetric: {}, timeMillis: {}",
                    bot.getId(), shotMetric, toHumanReadableFormat(timeMillis));
            ((IRoomNaturalBotStrategy) bot.getStrategy()).updateShootRequestTimeMetric(shotMetric, timeMillis);
        }

        client.sendMessage(new Shot(System.currentTimeMillis(), rid, weaponId, enemyId,
                currentX, currentY, isPaidSpecialShot, bulletId, weaponPrice != null ? weaponPrice : 1));

        bot.count(Stats.SHOTS);
    }

    @Override
    public void handle(ITransportObject response) {
        boolean makeOtherShot = true;
        String className = response.getClassName();
        int rid = response.getRid();

        boolean battleBot = bot.isBattleBot();
        bot.setLastReceivedServerTime(response.getDate());

        if(bot.getStrategy() instanceof IRoomNaturalBotStrategy && !StringUtils.isTrimmedEmpty(shotMetric)) {
            long timeMillis = System.currentTimeMillis();
            getLogger().debug("handle: shot response was received by bot: {}, shotMetric: {}, timeMillis: {}",
                    bot.getId(), shotMetric, toHumanReadableFormat(timeMillis));
            ((IRoomNaturalBotStrategy) bot.getStrategy()).updateShootResponseTimeMetric(shotMetric, timeMillis);
        }

        ManagedBattleGroundRoomBot managedBattleGroundRoomBot = null;
        if(bot instanceof ManagedBattleGroundRoomBot) {
            managedBattleGroundRoomBot = (ManagedBattleGroundRoomBot)bot;
        }

        if(managedBattleGroundRoomBot != null) {
            managedBattleGroundRoomBot.removeRicochetBulletByRid(rid);
        }

        switch (className) {
            case "Hit":
                bot.count(Stats.HITS);
                Hit hit = (Hit) response;

                if (hit.getAwardedWeaponId() != -1) {
                    int awardedWeaponId = hit.getAwardedWeaponId();
                    int availableShots = SpecialWeaponType.values()[awardedWeaponId].getAvailableShots();
                    bot.addWeapon(awardedWeaponId, availableShots);
                }

                mainShotResultReceived = hit.isLastResult() && rid != -1;

                List<Weapon> awardedWeapons = hit.getAwardedWeapons();
                if (awardedWeapons != null && !awardedWeapons.isEmpty()) {
                    for (Weapon awardedWeapon : awardedWeapons) {
                        bot.addWeapon(awardedWeapon.getId(), awardedWeapon.getShots());
                    }
                }

                long addNewAmmo = (long) (hit.getWin() / bot.getRoomStake());
                int remainingSWShots = hit.getRemainingSWShots();

                int usedSpecialWeapon = hit.getUsedSpecialWeapon();
                getLogger().debug("ShotRequest: addNewAmmo: {} remainingSWShots: {} usedSpecialWeapon: {} rid: {}",
                        addNewAmmo, remainingSWShots, usedSpecialWeapon, rid);
                if (usedSpecialWeapon > -1 && rid != -1) {
                    bot.getStrategy().updateWeapon(usedSpecialWeapon, remainingSWShots);
                }

                if (hit.getServerAmmo() > 0) {
                    bot.setServerAmmo(hit.getServerAmmo());
                }

                break;
            case "Miss":
                bot.count(Stats.MISSES);
                Miss miss = (Miss) response;

                mainShotResultReceived = miss.isLastResult() && rid != -1;
                if (miss.getAwardedWeaponId() != -1) {
                    bot.addWeapon(miss.getAwardedWeaponId(), SpecialWeaponType.values()[miss.getAwardedWeaponId()].getAvailableShots());
                }

                if (miss.getServerAmmo() > 0) {
                    bot.setServerAmmo(miss.getServerAmmo());
                }
                if (miss.isKilledMiss()) {
                    bot.removeEnemy(miss.getEnemyId());
                    bot.count(Stats.KILLED_MISSES);
                } else if (miss.isInvulnerable()) {
                    bot.count(Stats.INVULNERABLE);
                }

                int remainingSWShotsMiss = miss.getRemainingSWShots();
                int usedSpecialWeaponMiss = miss.getUsedSpecialWeapon();
                getLogger().debug("Miss shotRequest: remainingSWShots: {} usedSpecialWeapon: {} rid: {}",
                        remainingSWShotsMiss, usedSpecialWeaponMiss, rid);
                if (usedSpecialWeaponMiss > -1 && rid != -1) {
                    bot.getStrategy().updateWeapon(usedSpecialWeaponMiss, remainingSWShotsMiss);
                }

                if (rid != -1 && (miss.isInvulnerable() || miss.isKilledMiss())) {
                    getLogger().debug("bot: {}, reset focusEnemy for rid {} miss: {}", bot.getId(), rid, miss);
                    bot.resetFocusedEnemy();
                }
                break;
            case "EnemyDestroyed":
                EnemyDestroyed enemyDestroyed = (EnemyDestroyed) response;
                bot.removeEnemy(enemyDestroyed.getEnemyId());
                makeOtherShot = false;
                break;
            case "Error":
                bot.count(Stats.ERRORS);
                mainShotResultReceived = true;
                Error errorResponse = (Error) response;
                handleError(errorResponse);
                if(errorResponse.getCode() == ErrorCodes.NOT_ENOUGH_BULLETS) {
                    //next shot after buyIn
                    //makeOtherShot = false;
                }
                break;
            default:
                getLogger().error("ShotRequest: unexpected response type: {}", response);
                break;
        }
        if(makeOtherShot) {
            boolean needProcess = !battleBot || mainShotResultReceived;
            if(needProcess) {
                if(bot.getState() == BotState.WAITING_FOR_RESPONSE){
                    bot.setState(BotState.PLAYING, "ShotRequest: " + className);
                }
                bot.doActionWithSleep("ShotRequest[" + className + "]");
            }
            getLogger().debug("ShotRequest: battleBot: {} mainShotResultReceived: {}, needProcess: {} ",
                    battleBot, mainShotResultReceived, needProcess);
        }
    }

    private void handleError(Error error) {
        if(weaponId != -1) {
            bot.setDefaultWeapon();
        }
        getLogger().debug("ShotRequest: handleError error, :{}", error);
        switch (error.getCode()) {
            case ErrorCodes.NOT_SEATER:
                bot.clearShotRequests();
                break;
            case ErrorCodes.INTERNAL_ERROR:
                break;
            case ErrorCodes.NOT_ENOUGH_BULLETS:
                if (!bot.isBattleBot()) {
                    bot.clearAmmo();
                    bot.sendBuyInRequest(0);
                    bot.clearShotRequests();
                }
                break;
            case ErrorCodes.ROUND_NOT_STARTED:
                bot.clearShotRequests();
                bot.setState(BotState.IDLE, "ShotRequest: handleError");
                break;
            case ErrorCodes.WRONG_WEAPON:
//                bot.activateWeapon(-1);
                break;
            default:
                getLogger().error("ShotRequest: unhandled shot error: {}", error);
                break;
        }
    }

    @Override
    public String toString() {
        return "ShotRequest[" +
                "bot.id=" + bot.getId() +
                ", bot.balance=" + bot.getBalance() +
                ", bot.state=" + bot.getState() +
                ", bot.shots=" + bot.getStrategy().getShots() +
                ", weaponId=" + weaponId +
                ", enemyId=" + enemyId +
                ", isPaidSpecialShot=" + isPaidSpecialShot +
                ", currentX=" + currentX +
                ", currentY=" + currentY +
                ']';
    }
}
