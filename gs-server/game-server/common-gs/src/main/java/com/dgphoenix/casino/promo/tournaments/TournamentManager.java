package com.abs.casino.promo.tournaments;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.abs.casino.common.promo.feed.tournament.TournamentFeed;
import com.abs.casino.common.upload.JSchUploadClient;
import com.abs.casino.common.util.string.StringIdGenerator;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.promo.icon.TournamentIcon;
import com.abs.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.abs.casino.promo.persisters.CassandraTournamentIconPersister;
import com.abs.casino.promo.tournaments.messages.NewTournament;
import com.abs.casino.promo.tournaments.messages.ShortTournamentInfo;
import com.abs.casino.promo.tournaments.messages.TournamentStateChanged;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.abs.casino.websocket.tournaments.ISocketClient;
import com.abs.casino.websocket.tournaments.TournamentWebSocketSessionsController;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.abs.casino.promo.feed.tournament.TournamentFeedWriter.TOURNAMENT_FEED_FILE_NAME;
import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;

public class TournamentManager implements IPromoCampaignsObserver {
    private static final Logger LOG = LogManager.getLogger(TournamentManager.class);

    private final IPromoCampaignManager promoCampaignManager;
    private final TournamentWebSocketSessionsController webSocketSessionsController;
    private final CassandraTournamentIconPersister iconPersister;
    private final ICurrencyRateManager currencyRateManager;
    private final TournamentLeaderboardBuilder leaderboardBuilder;
    private final GameServerConfiguration configuration;
    private final JSchUploadClient jSchClient;

    public TournamentManager(IPromoCampaignManager promoCampaignManager,
                             TournamentWebSocketSessionsController webSocketSessionsController,
                             CassandraPersistenceManager cpm,
                             ICurrencyRateManager currencyRateManager, GameServerConfiguration configuration) {
        this.promoCampaignManager = promoCampaignManager;
        this.webSocketSessionsController = webSocketSessionsController;
        this.iconPersister = cpm.getPersister(CassandraTournamentIconPersister.class);
        this.currencyRateManager = currencyRateManager;
        this.leaderboardBuilder = new TournamentLeaderboardBuilder(
                cpm.getPersister(CassandraMaxBalanceTournamentPersister.class),
                currencyRateManager);
        this.configuration = configuration;

        String updaterUser = configuration.getSshStatisticUpdaterUser();
        String updaterPass = configuration.getSshStatisticUpdaterPass();
        Set<String> hosts = configuration.getSshJackpotsUploadHosts();
        int port = configuration.getSshJackpotsUploadPort();
        this.jSchClient = new JSchUploadClient(updaterUser, updaterPass, hosts, port);
    }

    @PostConstruct
    public void init() {
        promoCampaignManager.registerPromoCampaignsObserver(this);
    }

    @PreDestroy
    public void shutdown() {
        jSchClient.closeClient();
    }

    @Override
    public void notifyCampaignCreated(long campaignId) {
        LOG.debug("notifyCampaignCreated: {}", campaignId);
        IPromoCampaign campaign = promoCampaignManager.getPromoCampaign(campaignId);
        if (campaign.getTemplate() instanceof MaxBalanceTournamentPromoTemplate) {
            MaxBalanceTournamentPromoTemplate template = (MaxBalanceTournamentPromoTemplate) campaign.getTemplate();
            Set<Long> bankIds = campaign.getBankIds();
            for (ISocketClient client : webSocketSessionsController.getClients().values()) {
                if (client.isConnected()) {
                    long bankId = StringIdGenerator.extractBankAndExternalUserId(client.getSessionId()).getKey();
                    if (bankIds.contains(bankId)) {
                        try {
                            long buyInPrice = (long) currencyRateManager
                                    .convert(template.getBuyInPrice(), campaign.getBaseCurrency(), client.getCurrency());
                            long reBuyPrice = (long) currencyRateManager
                                    .convert(template.getReBuyPrice(), campaign.getBaseCurrency(), client.getCurrency());
                            long prize = (long) currencyRateManager
                                    .convert(template.getPrize(), campaign.getBaseCurrency(), client.getCurrency());
                            Long networkPromoCampaignId = getNetworkPromoCampaignId(campaign);
                            client.sendMessage(new NewTournament(System.currentTimeMillis(), new ShortTournamentInfo(
                                    campaignId,
                                    campaign.getName(),
                                    campaign.getActionPeriod().getStartDate().getTime(),
                                    campaign.getActionPeriod().getEndDate().getTime(),
                                    buyInPrice,
                                    template.getBuyInAmount(),
                                    reBuyPrice,
                                    template.getReBuyAmount(),
                                    template.isReBuyEnabled(),
                                    template.getReBuyLimit(),
                                    prize,
                                    false,
                                    campaign.getStatus().name(),
                                    getIconUrl(template),
                                    new ArrayList<>(campaign.getGameIds()),
                                    false,
                                    networkPromoCampaignId
                            )));
                        } catch (CommonException e) {
                            LOG.error("Failed to convert currency, notification is not sent");
                        }
                    }
                }
            }
        }
    }

    private Long getNetworkPromoCampaignId(IPromoCampaign campaign) {
        if (campaign instanceof NetworkPromoEvent) {
            return ((NetworkPromoEvent) campaign).getParentPromoCampaignId();
        }
        return null;
    }

    private String getIconUrl(MaxBalanceTournamentPromoTemplate template) {
        TournamentIcon icon = iconPersister.getById(template.getIconId());
        if (icon != null) {
            return icon.getHttpAddress();
        }
        return "";
    }

    @Override
    public void notifyCampaignChanged(long campaignId) {

    }

    @Override
    public void notifyCampaignStatusChanged(long campaignId, Status oldStatus, Status newStatus) {
        IPromoCampaign campaign = promoCampaignManager.getPromoCampaign(campaignId);
        if (campaign.getTemplate().getPromoType().isScoreCounting()) {
            Set<Long> bankIds = campaign.getBankIds();
            for (ISocketClient client : webSocketSessionsController.getClients().values()) {
                if (client.isConnected()) {
                    long bankId = StringIdGenerator.extractBankAndExternalUserId(client.getSessionId()).getKey();
                    if (bankIds.contains(bankId)) {
                        String newStatusName = newStatus.name();
                        if (newStatus == Status.QUALIFICATION) {
                            newStatusName = Status.FINISHED.name();
                        }
                        client.sendMessage(new TournamentStateChanged(System.currentTimeMillis(), campaignId, newStatusName));
                    }
                }
            }
        }
    }

    public TournamentFeed getLeaderboard(IPromoCampaign tournament) throws CommonException {
        return leaderboardBuilder.createForExport(tournament);
    }

    public TournamentLeaderboard getLeaderboard(IPromoCampaign tournament, String currency,
                                                String sessionId) throws CommonException {
        if (tournament instanceof NetworkPromoEvent) {
            try {
                NetworkPromoCampaign campaign = (NetworkPromoCampaign) promoCampaignManager
                        .getPromoCampaign(((NetworkPromoEvent) tournament).getParentPromoCampaignId());
                if (!campaign.isSingleClusterPromo()) {
                    NetworkPromoEvent promoEvent = (NetworkPromoEvent) tournament;
                    return leaderboardBuilder.createForNetworkPlayer(tournament, currency, sessionId,
                            downloadFeed(promoEvent), promoEvent);
                }
            } catch (Exception e) {
                LOG.error("Some problem occurred during leaderboard preparing, tournament={}, currency={}, " +
                        "sessionId={}", tournament, currency, sessionId, e);
                throw new CommonException(e);
            }
        }
        return leaderboardBuilder.createForPlayer(tournament, currency, sessionId);
    }

    private String downloadFeed(NetworkPromoEvent event) {
        try {
            long timeoutMillis = TimeUnit.MINUTES.toMillis(1);
            int maxResponseSizeBytes = 20 * 1024 * 1024;
            return HttpClientConnection.newInstance(timeoutMillis, maxResponseSizeBytes)
                    .doRequest(event.getSummaryFeedUrl(), null);
        } catch (Exception e) {
            LOG.warn("Some problem occurred during feed downloading, event={}", event, e);
            return "";
        }
    }

    public String getFeedFromServer(long tournamentId) throws CommonException {
        String rootPath = configuration.getPromoFeedsRootPath();
        String remoteFeedsPath = String.format("%s/%d/%s.xml", rootPath, tournamentId, TOURNAMENT_FEED_FILE_NAME);

        String tournamentFeed = null;
        try {
            tournamentFeed = jSchClient.readFile(remoteFeedsPath);
        } catch (JSchException e) {
            LOG.error("Error while connection to server", e);
            throw new CommonException("Error while connection to server", e);
        } catch (SftpException e) {
            if (e.id == SSH_FX_NO_SUCH_FILE) {
                LOG.debug("Missing leaderboard file {}", remoteFeedsPath);
            } else {
                LOG.error("Can`t read the leaderboard file {}", remoteFeedsPath);
                throw new CommonException("Can`t read the leaderboard file", e);
            }
        }
        return tournamentFeed;
    }

    public ISocketClient getSocketClientBySessionId(String sessionId) {
        return webSocketSessionsController.getClients().get(sessionId);
    }
}
