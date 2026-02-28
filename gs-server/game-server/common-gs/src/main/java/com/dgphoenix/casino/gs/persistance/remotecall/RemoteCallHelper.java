package com.dgphoenix.casino.gs.persistance.remotecall;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.*;
import com.abs.casino.cassandra.persist.CassandraRemoteCallPersister;
import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.cache.data.bank.SubCasinoGroup;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.config.GameServerConfigTemplate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.PromoNotificationType;
import com.dgphoenix.casino.common.promo.*;
import com.abs.casino.common.remotecall.IRemoteCall;
import com.abs.casino.common.remotecall.PersistableCall;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusWinRequestFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.InvalidateAllBaseGameInfoRequest;
import com.abs.casino.kafka.dto.InvalidateBonusClientRequest;
import com.abs.casino.kafka.dto.InvalidateFrBonusClientRequest;
import com.abs.casino.kafka.dto.InvalidateFrBonusWinManagerRequest;
import com.abs.casino.kafka.dto.InvalidateLocalBaseGameInfoRequest;
import com.abs.casino.kafka.dto.InvalidatePromoCampaignCacheRequest;
import com.abs.casino.kafka.dto.InvalidateWalletManagerRequest;
import com.abs.casino.kafka.dto.NotifyPromoCampaignCreatedRequest;
import com.abs.casino.kafka.dto.NotifyPromoCampaignStatusChangedRequest;
import com.abs.casino.kafka.dto.NotifySessionClosedRequest;
import com.abs.casino.kafka.dto.RefreshConfigRequest;
import com.abs.casino.kafka.dto.SendPromoNotificationsRequest;
import com.abs.casino.kafka.dto.SendBalanceUpdatedRequest;
import com.abs.casino.kafka.dto.SendPlayerTournamentStateChangedRequest;
import com.abs.casino.kafka.dto.UpdateStubBalanceByExternalUserIdRequest;
import com.dgphoenix.casino.kafka.dto.*;
import com.abs.casino.kafka.service.KafkaMessageService;
import com.abs.casino.websocket.IWebSocketSessionsController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dgphoenix.casino.common.util.ApplicationContextHelper.getApplicationContext;

/**
 * User: flsh
 * Date: 10/9/12
 */
public class RemoteCallHelper implements IPromoCampaignsObserver, IRemotePromoNotifier {
    private static final Logger LOG = LogManager.getLogger(RemoteCallHelper.class);

    private final IWebSocketSessionsController webSocketSessionsController;
    private final CassandraServerConfigTemplatePersister serverConfigTemplatePersister;
    private final CassandraSubCasinoPersister subCasinoPersister;
    private final CassandraSubCasinoGroupPersister subCasinoGroupPersister;
    private final CassandraBankInfoPersister bankInfoPersister;
    private final CassandraBaseGameInfoPersister baseGameInfoPersister;
    private final CassandraBaseGameInfoTemplatePersister baseGameInfoTemplatePersister;
    private final CassandraRemoteCallPersister remoteCallPersister;
    private final InServiceServiceHandler inServiceServiceHandler;
    private final KafkaMessageService kafkaMessageService;

    public RemoteCallHelper(IWebSocketSessionsController webSocketSessionsController,
                            IPromoCampaignManager promoCampaignManager,
                            InServiceServiceHandler inServiceServiceHandler,
                            KafkaMessageService kafkaMessageService) {
        this.webSocketSessionsController = webSocketSessionsController;
        promoCampaignManager.registerPromoCampaignsObserver(this);
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        serverConfigTemplatePersister = persistenceManager.getPersister(CassandraServerConfigTemplatePersister.class);
        subCasinoPersister = persistenceManager.getPersister(CassandraSubCasinoPersister.class);
        subCasinoGroupPersister = persistenceManager.getPersister(CassandraSubCasinoGroupPersister.class);
        bankInfoPersister = persistenceManager.getPersister(CassandraBankInfoPersister.class);
        baseGameInfoPersister = persistenceManager.getPersister(CassandraBaseGameInfoPersister.class);
        baseGameInfoTemplatePersister = persistenceManager.getPersister(CassandraBaseGameInfoTemplatePersister.class);
        remoteCallPersister = persistenceManager.getPersister(CassandraRemoteCallPersister.class);
        this.inServiceServiceHandler = inServiceServiceHandler;
        this.kafkaMessageService = kafkaMessageService;
    }

    /**
     * @deprecated Should be used only for backward compatibility.
     */
    public static RemoteCallHelper getInstance() {
        return getApplicationContext()
                .getBean("remoteCallHelper", RemoteCallHelper.class);
    }

    public void saveAndSendNotification(GameServerConfigTemplate config) throws CommonException {
        serverConfigTemplatePersister.save(config);
        sendRefreshConfigCallToAllServers(new RefreshConfigCall(
                ServerConfigsCache.class.getCanonicalName(), String.valueOf(config.getId())));
    }

    public void saveAndSendNotification(SubCasino subCasino) throws CommonException {
        subCasinoPersister.persist(subCasino.getId(), subCasino);
        SubCasinoCache.getInstance().put(subCasino);
        sendRefreshConfigCallToAllServers(new RefreshConfigCall(
                SubCasinoCache.class.getCanonicalName(), String.valueOf(subCasino.getId())));
    }

    public void saveAndSendNotification(SubCasinoGroup group) throws CommonException {
        subCasinoGroupPersister.persist(group.getName(), group);
        SubCasinoGroupCache.getInstance().put(group);
        sendRefreshConfigCallToAllServers(new RefreshConfigCall(
                SubCasinoGroupCache.class.getCanonicalName(), group.getName()));
    }

    public void saveAndSendNotification(BankInfo bankInfo) throws CommonException {
        if (bankInfo.getSubCasinoId() <= 0) {
            LOG.error("saveAndSendNotification [bankInfo]: Possible error, incorrect subCasino: {}", bankInfo);
            if (bankInfo.getId() > 0) {
                throw new CommonException("Incorrect subCasinoId");
            }
        }
        BankInfoCache.getInstance().put(bankInfo);
        bankInfoPersister.save(bankInfo);
        sendRefreshConfigCallToAllServers(new RefreshConfigCall(
                BankInfoCache.class.getCanonicalName(), String.valueOf(bankInfo.getId())));
        inServiceServiceHandler.invalidateAllBaseGameInfo(bankInfo.getId());

        InvalidateAllBaseGameInfoRequest request = new InvalidateAllBaseGameInfoRequest(bankInfo.getId());
        kafkaMessageService.asyncRequestToAllGS(request);
    }


    public void saveAndSendNotification(IBaseGameInfo gameInfo) throws CommonException {
        if (gameInfo instanceof BaseGameInfo) {
            baseGameInfoPersister.save((BaseGameInfo) gameInfo);
        } else {
            LOG.error("Cannot save IBaseGameInfo instance: {}", gameInfo);
            throw new CommonException("Cannot save IBaseGameInfo instance: " + gameInfo.getClass());
        }
        inServiceServiceHandler.invalidateLocalBaseGameInfo(gameInfo.getBankId(), gameInfo.getId(), gameInfo.getCurrency());

        InvalidateLocalBaseGameInfoRequest request = new InvalidateLocalBaseGameInfoRequest(gameInfo.getBankId(), gameInfo.getId(), gameInfo.getCurrency().getCode());
        kafkaMessageService.asyncRequestToAllGS(request);
    }

    public void saveAndSendNotification(BaseGameInfoTemplate template) throws CommonException {
        baseGameInfoTemplatePersister.persist(template.getGameId(), template);
        sendRefreshConfigCallToAllServers(new RefreshConfigCall(
                BaseGameInfoTemplateCache.class.getCanonicalName(), String.valueOf(template.getId())));
    }

    public void sendRefreshConfigCallToAllServers(final RefreshConfigCall call) {
        RefreshConfigRequest request = new RefreshConfigRequest(call.getConfigName(), call.getId());
        kafkaMessageService.asyncRequestToAllGS(request);
    }



    public void sendCallToAllServers(IRemoteCall call) {
        final Collection<ServerInfo> servers = LoadBalancerCache.getInstance().getAllObjects().values();
        for (ServerInfo server : servers) {
            if (server.getServerId() == GameServer.getInstance().getServerId()) {
                continue;
            }
            if (!server.isServerOnline()) {
                LOG.debug("Skip sendCall, offline: {}", server);
                continue;
            }
            final long id = IdGenerator.getInstance().getNext(PersistableCall.class);
            PersistableCall persistableCall = new PersistableCall(id, call, System.currentTimeMillis(),
                    server.getServerId());
            remoteCallPersister.persist(persistableCall);
        }
    }

    @Override
    public void notifyCampaignCreated(long campaignId) {
        long now = System.currentTimeMillis();
        LOG.debug("notifyCampaignCreated: campaignId={}", campaignId);

        NotifyPromoCampaignCreatedRequest request = new NotifyPromoCampaignCreatedRequest(campaignId);
        kafkaMessageService.asyncRequestToAllGS(request);

        StatisticsManager.getInstance().updateRequestStatistics("RemoteCallHelper: notifyCampaignCreated",
                System.currentTimeMillis() - now);
    }

    @Override
    public void notifyCampaignChanged(final long campaignId) {
        InvalidatePromoCampaignCacheRequest request = new InvalidatePromoCampaignCacheRequest(campaignId);
        kafkaMessageService.asyncRequestToAllGS(request);
    }

    @Override
    public void notifyCampaignStatusChanged(final long campaignId, final Status oldStatus, final Status newStatus) {
        long now = System.currentTimeMillis();
        String callbackDescription = String.format("notifyCampaignStatusChanged: campaignId = %d, oldStatus = %s, " +
                "newStatus = %s", campaignId, oldStatus, newStatus);
        NotifyPromoCampaignStatusChangedRequest request = new NotifyPromoCampaignStatusChangedRequest(campaignId, oldStatus.name(), newStatus.name());
        kafkaMessageService.asyncRequestToAllGS(request);
        StatisticsManager.getInstance().updateRequestStatistics("RemoteCallHelper: notifyPromoCampaignStatusChanged",
                System.currentTimeMillis() - now);
    }

    @Override
    public void sendPromoNotifications(long accountId, final String sessionId, final long campaignId,
                                       Set<PromoNotificationType> notificationsTypes, int serverId) {
        LOG.debug("sendPromoNotifications: accountId = {}, sessionId = {}, campaignId = {}, notifications = {}, " +
                "serverId = {}", accountId, serverId, campaignId, notificationsTypes, serverId);
        long now = System.currentTimeMillis();
        final StatisticsManager statisticsManager = StatisticsManager.getInstance();
        if (serverId == GameServer.getInstance().getServerId()) {
            statisticsManager.updateRequestStatistics("RemoteCallHelper: sendPromoNotifications locally",
                    System.currentTimeMillis() - now);
        } else {
            try {
                Set<com.dgphoenix.casino.kafka.dto.PromoNotificationType> types = notificationsTypes.stream()
                        .map(Object::toString)
                        .map(com.dgphoenix.casino.kafka.dto.PromoNotificationType::valueOf)
                        .collect(Collectors.toSet());

                SendPromoNotificationsRequest request = new SendPromoNotificationsRequest(sessionId, campaignId, types);
                kafkaMessageService.asyncRequestToSpecificGS(request, serverId);

                statisticsManager.updateRequestStatistics("RemoteCallHelper: sendPromoNotifications remotely",
                        System.currentTimeMillis() - now);
            } catch (Exception e) {
                statisticsManager.updateRequestStatistics("RemoteCallHelper: sendPromoNotifications failed",
                        System.currentTimeMillis() - now);
                LOG.error("Cannot send promo notifications sessionId = {}, campaignId = {}, serverId = {}",
                        sessionId, campaignId, serverId, e);
            }
        }
    }

    public void notifySessionClosed(final String sessionId) {
        LOG.debug("notifySessionClosed: sessionId = {}", sessionId);
        long now = System.currentTimeMillis();
        webSocketSessionsController.closeWebSocketForPlayerSession(sessionId);

        NotifySessionClosedRequest request = new NotifySessionClosedRequest(sessionId);
        kafkaMessageService.asyncRequestToAllGS(request);

        StatisticsManager.getInstance().updateRequestStatistics("RemoteCallHelper: notifySessionClosed",
                System.currentTimeMillis() - now);
    }

    public void invalidateFrBonusClient(long bankId) {
        LOG.debug("invalidateFrBonusClient: bankId = {}", bankId);
        long now = System.currentTimeMillis();
        FRBonusManager.getInstance().invalidateClient(bankId);

        InvalidateFrBonusClientRequest request = new InvalidateFrBonusClientRequest(bankId);
        kafkaMessageService.asyncRequestToAllGS(request);

        StatisticsManager.getInstance().updateRequestStatistics("RemoteCallHelper: invalidateFrBonusClient",
                System.currentTimeMillis() - now);
    }

    public void invalidateBonusClient(long bankId) {
        LOG.debug("invalidateBonusClient: bankId = {}", bankId);
        long now = System.currentTimeMillis();
        BonusManager.getInstance().invalidateClient(bankId);

        InvalidateBonusClientRequest request = new InvalidateBonusClientRequest(bankId);
        kafkaMessageService.asyncRequestToAllGS(request);

        StatisticsManager.getInstance().updateRequestStatistics("RemoteCallHelper: invalidateBonusClient",
                System.currentTimeMillis() - now);
    }

    public void invalidateFrBonusWinManager(long bankId) {
        LOG.debug("invalidateFrBonusWinManager: bankId = {}", bankId);
        long now = System.currentTimeMillis();
        FRBonusWinRequestFactory.getInstance().invalidateManager(bankId);

        InvalidateFrBonusWinManagerRequest request = new InvalidateFrBonusWinManagerRequest(bankId);
        kafkaMessageService.asyncRequestToAllGS(request);

        StatisticsManager.getInstance().updateRequestStatistics("RemoteCallHelper: invalidateFrBonusWinManager",
                System.currentTimeMillis() - now);
    }

    public void invalidateWalletManager(long bankId) {
        LOG.debug("invalidateWalletManager: bankId = {}", bankId);
        long now = System.currentTimeMillis();
        WalletProtocolFactory.getInstance().invalidateManager(bankId);

        InvalidateWalletManagerRequest request = new InvalidateWalletManagerRequest(bankId);
        kafkaMessageService.asyncRequestToAllGS(request);

        StatisticsManager.getInstance().updateRequestStatistics("RemoteCallHelper: invalidateWalletManager",
                System.currentTimeMillis() - now);
    }

    public void updateStubBalance(String externalUserId, long balance) {
        LOG.debug("RemoteCallHelper::updateStubBalance externalUserId={}, balance={}", externalUserId, balance);

        UpdateStubBalanceByExternalUserIdRequest request = new UpdateStubBalanceByExternalUserIdRequest(externalUserId, balance);
        kafkaMessageService.asyncRequestToAllGS(request);
    }

    public void sendPlayerTournamentStateChanged(String sessionId, long tournamentId, boolean cannotJoin, boolean joined) {
        SendPlayerTournamentStateChangedRequest request = new SendPlayerTournamentStateChangedRequest(sessionId, tournamentId, cannotJoin, joined);
        kafkaMessageService.asyncRequestToAllGS(request);
    }

    public void sendBalanceUpdated(String sessionId, long balance) {
        SendBalanceUpdatedRequest request = new SendBalanceUpdatedRequest(sessionId, balance);
        kafkaMessageService.asyncRequestToAllGS(request);
    }

}
