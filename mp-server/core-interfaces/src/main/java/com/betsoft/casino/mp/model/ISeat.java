package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.InboundObject;
import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public interface ISeat<WEAPON extends IWeapon, PLAYER_ROUND_INFO extends IPlayerRoundInfo, TREASURE extends ITreasure, RPI extends IRoomPlayerInfo, S extends ISeat>
        extends Identifiable, IPlayEventProducer, KryoSerializable, JsonSelfSerializable<S> {

    /**
     * reset and init player data before round
     * @param playerInfo {@code AbstractPlayerRoundInfo} room player info
     */
    void initCurrentRoundInfo(RPI playerInfo);

    WEAPON createWeapon(int shots, SpecialWeaponType type);

    boolean isLevelUp();

    long getAccountId();

    long getBankId();

    void setJoinDate(long joinDate);

    String getNickname();

    IAvatar getAvatar();

    Money getStake();

    void setStake(Money stake);

    IExperience getTotalScore();

    IExperience getCurrentScore();

    double addScore(double score);

    void resetCurrentScore();

    long getJoinDate();

    void setSitOutStarted(boolean sitOutStarted);

    IGameSocketClient getSocketClient();

    void setSocketClient(IGameSocketClient client);

    RPI getPlayerInfo();

    void setPlayerInfo(RPI playerInfo);

    default void sendMessage(ITransportObject message) {
        if (!isDisconnected()) {
            getSocketClient().sendMessage(message);
        }
    }

    default void sendMessage(ITransportObject message, InboundObject inboundObject) {
        if (!isDisconnected()) {
            getSocketClient().sendMessage(message, inboundObject);
        }
    }

    default boolean isDisconnected() {
        return getSocketClient() == null || getSocketClient().getConnection() == null
                || getSocketClient().isDisconnected();
    }

    boolean isWantSitOut();

    void setWantSitOut(boolean wantSitOut);

    boolean isSitOutStarted();

    void updatePlayerRoundInfo(long externalRoundId);

    long getRoundWinInCredits();

    int getBetLevel();

    void setBetLevel(int betLevel);

    Money getRoundWin();

    void setRoundWin(Money roundWin);

    void incrementRoundWin(Money win);

    void incrementShotTotalWin(Money win);

    Money getRebuyFromWin();

    void setRebuyFromWin(Money rebuyFromWin);

    void makeRebuyFromWin(Money rebuyFromWin) throws CommonException;

    void revertRebuyFromWin(Money rebuyFromWin);

    Money retrieveRoundWin();

    Money getPossibleBalanceAmount();

    void rollbackRoundWinAndAmmo(Money roundWin, int ammoAmount);

    Money getTotalWin();

    void setTotalWin(Money totalWin);

    void transferRoundWin();

    boolean isBot();

    int getLevel();

    void setLastWin(Money lastWin);

    void addLastWin(Money win);

    Money getLastWin();

    double getCurrentRate();

    Money getQualifyWin();

    void setQualifyWin(Money qualifyWin);

    PLAYER_ROUND_INFO getCurrentPlayerRoundInfo();

    int getQuestsCompletedCount();

    void setQuestsCompletedCount(int questsCompletedCount);

    long getQuestsPayouts();

    void setQuestsPayouts(long questsPayouts);

    boolean isKingOfHill();

    void  setKingOfHill(boolean isKing);

    default Long getSpecialModeId() {
        IRoomPlayerInfo playerInfo = getPlayerInfo();
        if(playerInfo == null) {
            throw new RuntimeException("RoomPlayerInfo not found");
        }
        if(playerInfo.getTournamentSession() != null) {
            return playerInfo.getTournamentSession().getTournamentId();
        } else if(playerInfo.getActiveCashBonusSession() != null) {
            return playerInfo.getActiveCashBonusSession().getId();
        } else if(playerInfo.getActiveFrbSession() != null) {
            return playerInfo.getActiveFrbSession().getBonusId();
        }
        return null;
    }

    /**
     * can be true only for private rooms
     * @return true if a player is owner of room, otherwise returns false
     */
    default boolean isOwner() {
        return false;
    }

    /**
     * set an owner flag for private rooms
     */
    default void setOwner(boolean isOwner) {
        //ignore
    }
}
