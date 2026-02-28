package com.dgphoenix.casino.common.transactiondata;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.account.PlayerGameSettings;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.abs.casino.common.transactiondata.TrackingInfo;
import com.abs.casino.common.transactiondata.TrackingState;
import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;

import java.util.Map;

/**
 * User: Grien
 * Date: 30.05.2014 16:40
 */
public interface ITransactionData extends IDistributedCacheEntry, Identifiable {
    long getVersion();

    void setVersion(long version);

    void incrementVersion();

    int getBankId();

    String getLockId();

    int getLastLockerId();

    AccountInfo getAccount();

    void setAccount(AccountInfo account);

    SessionInfo getPlayerSession();

    void setPlayerSession(SessionInfo playerSession);

    GameSession getGameSession();

    void setGameSession(GameSession gameSession);

    LasthandInfo getLasthand();

    void setLasthand(LasthandInfo lasthand);

    IWallet getWallet();

    void setWallet(IWallet wallet);

    PlayerBet getLastBet();

    void setLastBet(PlayerBet lastBet);

    boolean isNeedRemove();

    long getAccountId();

    void setAccountId(long accountId);

    String getLastUpdateInfo();

    void setLastUpdateInfo(String lastUpdateInfo);

    boolean isTrackingStateChanged();

    PlayerGameSettings getPlayerGameSettings();

    void setPlayerGameSettings(PlayerGameSettings playerGameSettings);

    Bonus getBonus();

    void setBonus(Bonus bonus);

    FRBonus getFrBonus();

    void setFrBonus(FRBonus frBonus);

    FRBonusWin getFrbWin();

    void setFrbWin(FRBonusWin frbWin);

    FRBonusNotification getFrbNotification();

    void setFrbNotification(FRBonusNotification frbNotification);

    PromoCampaignMemberInfos getPromoMemberInfos();

    void setPromoMemberInfos(PromoCampaignMemberInfos promoMemberInfos);

    PromoCampaignMember getPromoMember(Long campaignId);

    TrackingState getTrackingState();

    String getTrackingStateAsString();

    void setTrackingState(String trackingState);

    TrackingInfo getTrackingInfo();

    void updateTrackingState(int gameServerId);

    <T, I extends StoredItemInfo<T>> void add(StoredItemType type, T item, I identifier);

    <T, I extends StoredItemInfo<T>> StoredItem<T, I> get(StoredItemType type);

    Map<StoredItemType, StoredItem> getAtomicallyStoredData();

    PaymentTransaction getPaymentTransaction();

    void setPaymentTransaction(PaymentTransaction paymentTransaction);

    long getWriteTime();

    void setWriteTime(long writeTime);

    boolean isAppliedAutoFinishLogic();

    void setAppliedAutoFinishLogic(boolean flag);
}
