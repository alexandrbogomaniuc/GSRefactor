package com.dgphoenix.casino.common.transactiondata;

import com.dgphoenix.casino.common.cache.IDistributedCache;
import com.dgphoenix.casino.common.cache.VersionedDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.account.PlayerGameSettings;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.abs.casino.common.cache.data.payment.bonus.CommonFRBonusWin;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.dgphoenix.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;
import com.dgphoenix.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * User: flsh
 * Date: 7/4/12
 */
public class TransactionData extends VersionedDistributedCacheEntry implements ITransactionData {
    private long accountId;
    private String lockId;
    private int bankId;
    private AccountInfo account;
    private SessionInfo playerSession;
    private GameSession gameSession;
    private LasthandInfo lasthand;
    private IWallet wallet;
    private PlayerBet lastBet;
    private String lastUpdateInfo;
    private Bonus bonus;
    private FRBonus frBonus;
    private FRBonusWin frbWin;
    private FRBonusNotification frbNotification;
    private transient TrackingState trackingState;
    private transient boolean trackingStateChanged = true;
    private transient Map<StoredItemType, StoredItem> atomicallyStoredData = new EnumMap<>(StoredItemType.class);
    private PaymentTransaction paymentTransaction;
    private transient long writeTime;
    private PromoCampaignMemberInfos promoMemberInfos;
    private transient boolean appliedAutoFinishLogic = false;

    public TransactionData(String lockId) {
        this.lockId = lockId;
        this.bankId = StringIdGenerator.extractBankAndExternalUserIdFromUserHash(lockId).getKey();
    }

    @Override
    public long getId() {
        return accountId;
    }

    @Override
    public String getLockId() {
        return lockId;
    }

    @Override
    public int getBankId() {
        return bankId;
    }

    @Override
    public int getLastLockerId() {
        return trackingState != null ? trackingState.getGameServerId() : -1;
    }

    @Override
    public AccountInfo getAccount() {
        return account;
    }

    @Override
    public void setAccount(AccountInfo account) {
        this.account = account;
        if (account != null) {
            this.accountId = account.getId();
        }
    }

    @Override
    public SessionInfo getPlayerSession() {
        return playerSession;
    }

    @Override
    public void setPlayerSession(SessionInfo playerSession) {
        this.playerSession = playerSession;
        if (playerSession != null && accountId <= 0) {
            accountId = playerSession.getAccountId();
        }
    }

    @Override
    public GameSession getGameSession() {
        return gameSession;
    }

    @Override
    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
        if (gameSession != null && accountId <= 0) {
            accountId = gameSession.getAccountId();
        }
    }

    @Override
    public LasthandInfo getLasthand() {
        return lasthand;
    }

    @Override
    public void setLasthand(LasthandInfo lasthand) {
        this.lasthand = lasthand;
    }

    @Override
    public IWallet getWallet() {
        return wallet;
    }

    @Override
    public void setWallet(IWallet wallet) {
        this.wallet = wallet;
        if (wallet != null && accountId <= 0) { //if not initialized
            this.accountId = wallet.getAccountId();
        }
    }

    @Override
    public PlayerBet getLastBet() {
        return lastBet;
    }

    @Override
    public void setLastBet(PlayerBet lastBet) {
        this.lastBet = lastBet;
    }

    @Override
    public boolean isNeedRemove() {
        return playerSession == null && gameSession == null && wallet == null
                && (frbWin == null || frbWin.getFRBonusWins().isEmpty())
                && frbNotification == null && paymentTransaction == null;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getLastUpdateInfo() {
        return lastUpdateInfo;
    }

    @Override
    public void setLastUpdateInfo(String lastUpdateInfo) {
        this.lastUpdateInfo = lastUpdateInfo;
    }

    @Override
    public boolean isTrackingStateChanged() {
        return trackingStateChanged;
    }

    @Override
    public PlayerGameSettings getPlayerGameSettings() {
        return account.getGameSettings();
    }

    @Override
    public void setPlayerGameSettings(PlayerGameSettings playerGameSettings) {
        this.account.setGameSettings(playerGameSettings);
    }

    @Override
    public Bonus getBonus() {
        return bonus;
    }

    @Override
    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
        if (bonus != null && accountId <= 0) {
            accountId = bonus.getAccountId();
        }
    }

    @Override
    public FRBonus getFrBonus() {
        return frBonus;
    }

    @Override
    public void setFrBonus(FRBonus frBonus) {
        this.frBonus = frBonus;
        if (frBonus != null && accountId <= 0) {
            accountId = frBonus.getAccountId();
        }
    }

    @Override
    public FRBonusWin getFrbWin() {
        return frbWin;
    }

    @Override
    public void setFrbWin(FRBonusWin frbWin) {
        this.frbWin = frbWin;
        if (frbWin != null && accountId <= 0) {
            accountId = frbWin.getAccountId();
        }
    }

    @Override
    public FRBonusNotification getFrbNotification() {
        return frbNotification;
    }

    @Override
    public void setFrbNotification(FRBonusNotification frbNotification) {
        this.frbNotification = frbNotification;
        if (frbNotification != null && accountId <= 0) {
            accountId = frbNotification.getAccountId();
        }
    }

    @Override
    public PromoCampaignMemberInfos getPromoMemberInfos() {
        return promoMemberInfos;
    }

    @Override
    public void setPromoMemberInfos(PromoCampaignMemberInfos promoMemberInfos) {
        this.promoMemberInfos = promoMemberInfos;
    }

    @Override
    public PromoCampaignMember getPromoMember(Long campaignId) {
        return promoMemberInfos == null ? null : promoMemberInfos.get(campaignId);
    }

    @Override
    public TrackingState getTrackingState() {
        return trackingState;
    }

    @Override
    public String getTrackingStateAsString() {
        if (this.trackingState == null) {
            return null;
        }
        return this.trackingState.getGameServerId() + IDistributedCache.ID_DELIMITER + this.trackingState.getStatus();
    }

    @Override
    public TrackingInfo getTrackingInfo() {
        return new TrackingInfo(getAccountId(), isNeedProcessWallet(), isNeedProcessFrbWin(),
                isNeedProcessFrbNotification(), getPaymentTransaction() != null);
    }

    @Override
    public void setTrackingState(String trackingState) {
        this.trackingState = getTrackingStateFromString(trackingState);
    }

    @Override
    public void updateTrackingState(int gameServerId) {
        TrackingState prevState = getTrackingState();
        TrackingStatus newStatus = TrackingStatus.PENDING;
        if (getPlayerSession() != null) {
            newStatus = TrackingStatus.ONLINE;
        } else {
            if (getFrbWin() != null) {
                newStatus = isNeedProcessFrbWin() ? TrackingStatus.TRACKING : TrackingStatus.PENDING;
            }
            if (getFrbNotification() != null) {
                newStatus = isNeedProcessFrbNotification() ? TrackingStatus.TRACKING : TrackingStatus.PENDING;
            }
            if (getWallet() != null && newStatus == TrackingStatus.PENDING) {
                newStatus = isNeedProcessWallet() ? TrackingStatus.TRACKING : TrackingStatus.PENDING;
            }
            if (getPaymentTransaction() != null && newStatus == TrackingStatus.PENDING) {
                newStatus = TrackingStatus.TRACKING;
            }
        }
        if (prevState == null || !prevState.equals(gameServerId, newStatus)) {
            this.trackingState = new TrackingState(gameServerId, newStatus);
            this.trackingStateChanged = true;
        } else {
            this.trackingStateChanged = false;
        }
    }

    private boolean isNeedProcessFrbWin() {
        FRBonusWin frbWin = getFrbWin();
        if (frbWin == null) {
            return false;
        }
        if (!frbWin.hasAnyOperation()) {
            return false;
        }
        boolean hasPendingFRBOPs = false;
        Collection<CommonFRBonusWin> aFRBonusWins = frbWin.getFRBonusWins().values();
        for (CommonFRBonusWin win : aFRBonusWins) {
            FRBWinOperation operation;
            if (win != null && (operation = win.getOperation()) != null) {
                if (operation.getExternalStatus() != FRBWinOperationStatus.PEENDING_SEND_ALERT) {
                    return true;
                } else {
                    hasPendingFRBOPs = true;
                }
            }
        }
        return !hasPendingFRBOPs;
    }

    private boolean isNeedProcessFrbNotification() {
        FRBonusNotification frBonusNotification = getFrbNotification();
        return frBonusNotification != null;
    }

    private boolean isNeedProcessWallet() {
        IWallet wallet = getWallet();
        if (wallet == null) {
            return false;
        }
        boolean hasPendingWOPs = false;
        boolean hasEmptyGameWallets = false; //need also process, just clear it
        Set<Integer> gamesIds = wallet.getWalletGamesIds();
        for (Integer gamesId : gamesIds) {
            if (gamesId == null) {
                ThreadLog.warn("Strange wallet, gameId is null, accountId=" + accountId);
                continue;
            }
            IWalletOperation operation = wallet.getCurrentWalletOperation(gamesId);
            if (operation != null) {
                if (operation.getExternalStatus() != WalletOperationStatus.PEENDING_SEND_ALERT) {
                    return true;
                } else {
                    hasPendingWOPs = true;
                }
            } else if (!hasEmptyGameWallets) {
                CommonGameWallet gameWallet = wallet.getGameWallet(gamesId);
                hasEmptyGameWallets = gameWallet != null && gameWallet.getRoundId() == null;
            }
        }
        return !hasPendingWOPs || hasEmptyGameWallets;
    }

    public static TrackingState getTrackingStateFromString(String trackingState) {
        if (!StringUtils.isTrimmedEmpty(trackingState)) {
            String[] split = trackingState.split(Pattern.quote(IDistributedCache.ID_DELIMITER));
            int gameServerId = Integer.parseInt(split[0]);
            String state = split[1];
            return new TrackingState(gameServerId, TrackingStatus.valueOf(state));
        }
        return null;
    }

    @Override
    public <T, I extends StoredItemInfo<T>> void add(StoredItemType type, T item, I identifier) {
        StoredItem old = atomicallyStoredData.put(type, new StoredItem<>(item, identifier));
        if (old != null) {
            if (old.getItem() == null) {
                ThreadLog.warn("Strange local cached item (null sub item). item=" + old + "; type=" + type);
            } else if (item == null || !old.getItem().equals(item)) {
                ThreadLog.warn("Changed local cached item. old=" + old + "; new item=" + item +
                        " with identifier=" + identifier);
            }
        }
    }

    @Override
    public <T, I extends StoredItemInfo<T>> StoredItem<T, I> get(StoredItemType type) {
        return atomicallyStoredData.get(type);
    }

    @Override
    public Map<StoredItemType, StoredItem> getAtomicallyStoredData() {
        return atomicallyStoredData;
    }

    @Override
    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    @Override
    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
        if (paymentTransaction != null && accountId <= 0) {
            accountId = paymentTransaction.getAccountId();
        }
    }

    @Override
    public long getWriteTime() {
        return writeTime;
    }

    @Override
    public void setWriteTime(long writeTime) {
        this.writeTime = writeTime;
    }

    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionData data = (TransactionData) o;

        if (accountId != data.accountId) return false;
        if (account != null ? !account.equals(data.account) : data.account != null) return false;
        if (gameSession != null ? !gameSession.equals(data.gameSession) : data.gameSession != null) return false;
        if (lastUpdateInfo != null ? !lastUpdateInfo.equals(data.lastUpdateInfo) : data.lastUpdateInfo != null)
            return false;
        if (lasthand != null ? !lasthand.equals(data.lasthand) : data.lasthand != null) return false;
        if (lockId != null ? !lockId.equals(data.lockId) : data.lockId != null) return false;
        if (playerSession != null ? !playerSession.equals(data.playerSession) : data.playerSession != null)
            return false;
        if (wallet != null ? !wallet.equals(data.wallet) : data.wallet != null) return false;
        if (paymentTransaction != null ? !paymentTransaction.equals(data.paymentTransaction) :
                data.paymentTransaction != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + (playerSession != null ? playerSession.hashCode() : 0);
        result = 31 * result + (gameSession != null ? gameSession.hashCode() : 0);
        result = 31 * result + (lastUpdateInfo != null ? lastUpdateInfo.hashCode() : 0);
        return result;
    }

    @Override
    public boolean isAppliedAutoFinishLogic() {
        return appliedAutoFinishLogic;
    }

    @Override
    public void setAppliedAutoFinishLogic(boolean flag) {
        appliedAutoFinishLogic = flag;
    }

    @Override
    public String toString() {
        return "TransactionData" +
                "[version=" + version +
                ", writeTime=" + writeTime +
                ", accountId=" + accountId +
                ", account=" + account +
                ", playerSession=" + playerSession +
                ", gameSession=" + gameSession +
                ", lasthand=" + lasthand +
                ", lastBet=" + lastBet +
                ", wallet=" + wallet +
                ", paymentTransaction=" + paymentTransaction +
                ", lastUpdateInfo='" + lastUpdateInfo + '\'' +
                ", bonus=" + bonus +
                ", frBonus=" + frBonus +
                ", frbWin=" + frbWin +
                ", frbNotification=" + frbNotification +
                ", promoMemberInfos=" + promoMemberInfos +
                ", trackingState=" + trackingState +
                ", trackingStateChanged=" + trackingStateChanged +
                ", atomicallyStoredData=" + atomicallyStoredData +
                ", appliedAutoFinishLogic=" + appliedAutoFinishLogic +
                ']';
    }
}
