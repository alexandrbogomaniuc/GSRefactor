package com.abs.casino.websocket.tournaments.handlers;

import com.abs.casino.account.AccountManager;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.CurrencyCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.exception.AlreadyExistsException;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.common.promo.*;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.gs.managers.payment.wallet.CWError;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.gs.socket.mq.ForceCreateDetailsException;
import com.abs.casino.gs.socket.mq.MQServiceHandler;
import com.abs.casino.gs.socket.mq.TournamentBuyInHelper;
import com.abs.casino.kafka.dto.KafkaHandlerException;
import com.abs.casino.promo.PlayerAliasManager;
import com.abs.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.abs.casino.promo.tournaments.messages.Error;
import com.abs.casino.promo.tournaments.messages.JoinTournament;
import com.abs.casino.promo.tournaments.messages.JoinTournamentResponse;
import com.abs.casino.promo.tournaments.messages.PlayerTournamentStateChanged;
import com.abs.casino.support.ErrorPersisterHelper;
import com.abs.casino.websocket.tournaments.ISocketClient;
import com.abs.casino.websocket.tournaments.TournamentWebSocketMessageListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;

import static com.abs.casino.promo.tournaments.ErrorCodes.*;

public class JoinTournamentHandler extends AbstractJoinPromoHandler<JoinTournament> {

    private static final Logger LOG = LogManager.getLogger(JoinTournamentHandler.class);

    private final IPromoCampaignManager promoCampaignManager;
    private final CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister;
    private final TournamentBuyInHelper buyInHelper;
    private final MQServiceHandler serviceHandler;
    private final List<TournamentWebSocketMessageListener> webSocketMessageListeners;
    private final PlayerAliasManager playerAliasManager;

    public JoinTournamentHandler(IPromoCampaignManager promoCampaignManager,
                                 CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister,
                                 TournamentBuyInHelper buyInHelper,
                                 List<TournamentWebSocketMessageListener> webSocketMessageListeners,
                                 ErrorPersisterHelper errorPersisterHelper, PlayerAliasManager playerAliasManager) {
        super(errorPersisterHelper);
        this.promoCampaignManager = promoCampaignManager;
        this.maxBalanceTournamentPersister = maxBalanceTournamentPersister;
        this.buyInHelper = buyInHelper;
        this.serviceHandler = ApplicationContextHelper.getApplicationContext().getBean(MQServiceHandler.class);
        this.webSocketMessageListeners = webSocketMessageListeners;
        this.playerAliasManager = playerAliasManager;
    }

    @Override
    public void handle(JoinTournament message, ISocketClient client) {
        Consumer<Error> errorSaver = error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message);
        if (client.isConnected()) {
            try {
                long tournamentId = message.getTournamentId();
                IPromoCampaign campaign = getCampaign(message, tournamentId);
                if (isTournament(campaign)) {
                    if (!campaign.getStatus().isActual()) {
                        sendCampaignErrorMessage(message, client, errorSaver, campaign);
                        return;
                    }
                    String assignedPlayerAlias = null;
                    boolean renamed = false;
                    String playerAlias = message.getPlayerAlias();
                    if (campaign.isNetworkPromoCampaign()) {
                        NetworkPromoCampaign networkCampaign = (NetworkPromoCampaign) campaign;
                        if (playerAlias == null || checkPlayerAliasHasForbiddenCharacters(playerAlias)) {
                            sendErrorMessageToClient(message, client, errorSaver,
                                    PLAYER_ALIAS_NOT_ALLOWED, "Player alias has forbidden characters");
                            return;
                        }
                        try {
                            playerAliasManager.checkAliasLength(playerAlias);
                            if (playerAliasManager.checkObscene(playerAlias)) {
                                sendErrorMessageToClient(message, client, errorSaver,
                                        PLAYER_ALIAS_ALREADY_REGISTERED, "Alias is obscene and banned");
                                return;
                            }
                            if (networkCampaign.isSingleClusterPromo()) {
                                playerAliasManager.saveForSingleCluster(tournamentId, playerAlias);
                                assignedPlayerAlias = playerAlias;
                            } else {
                                assignedPlayerAlias = playerAliasManager.saveForMultiCluster(tournamentId, playerAlias);
                                renamed = !assignedPlayerAlias.equals(playerAlias);
                            }
                        } catch (AlreadyExistsException e) {
                            sendErrorMessageToClient(message, client, errorSaver,
                                    PLAYER_ALIAS_ALREADY_REGISTERED, "Player alias already registered");
                            return;
                        } catch (CommonException e) {
                            sendErrorMessageToClient(message, client, errorSaver,
                                    PLAYER_ALIAS_NOT_ALLOWED, e.getMessage());
                            return;
                        }
                    }
                    TournamentPromoTemplate<?> template = (TournamentPromoTemplate<?>) campaign.getTemplate();
                    Pair<Long, Integer> accountIdAndBankId = getAccountIdAndBankId(client, message.getRid(), errorSaver);
                    Long accountId = accountIdAndBankId.getKey();
                    MaxBalanceTournamentPlayerDetails details = maxBalanceTournamentPersister
                            .getForAccount(accountId, tournamentId);
                    try {
                        String sessionId = client.getSessionId();
                        if (details == null) {
                            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
                            long buyInPrice = 0;
                            long buyInAmount = 0;
                            if (template instanceof INetworkPromoEventTemplate) {
                                INetworkPromoEventTemplate<IPrize, ? extends INetworkPromoEventTemplate> networkPromoEventTemplate =
                                        (INetworkPromoEventTemplate) template;
                                buyInPrice = networkPromoEventTemplate.getBuyInPrice();
                                buyInAmount = networkPromoEventTemplate.getBuyInAmount();
                            }
                            performBuyIn(tournamentId, campaign, accountId, sessionId, accountInfo, buyInPrice,
                                    buyInAmount);
                            details = registerPlayer(accountInfo.getBankId(), accountInfo.getExternalId(),
                                    accountId, tournamentId, buyInAmount, buyInPrice);
                        }
                        savePlayerAlias(campaign, assignedPlayerAlias, accountId, details);
                        prepareAndSendMessages(message, client, tournamentId, campaign, assignedPlayerAlias, renamed,
                                template, accountIdAndBankId, details);
                    } catch (WalletException e) {
                        Integer errorCode = e.tryToGetNumericErrorCode();
                        if (errorCode != null) {
                            CWError cwError = CommonWalletErrors.getCWErrorByCode(errorCode);
                            if (errorCode == CommonWalletErrors.INSUFFICIENT_FUNDS.getCode()) {
                                errorCode = NOT_ENOUGH_MONEY;
                            }
                            sendErrorMessageToClient(message, client, errorSaver, errorCode, cwError.getDescription());
                        } else {
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                processUnexpectedError(client, message, e,
                        error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message, e));
            }
        } else {
            sendErrorMessageToClient(message, client, errorSaver, NOT_LOGGED_IN, "Not logged in");
        }
    }

    private void savePlayerAlias(IPromoCampaign campaign, String assignedPlayerAlias, Long accountId, MaxBalanceTournamentPlayerDetails details) {
        if (assignedPlayerAlias != null) {
            details.setNickname(assignedPlayerAlias);
            maxBalanceTournamentPersister.persist(details);
        }
        if (campaign instanceof NetworkPromoEvent) {
            NetworkPromoEvent networkPromoEvent = (NetworkPromoEvent) campaign;
            MaxBalanceTournamentPlayerDetails networkDetails = maxBalanceTournamentPersister
                    .getForAccount(accountId, networkPromoEvent.getParentPromoCampaignId());
            details.setNickname(networkDetails.getNickname());
            maxBalanceTournamentPersister.persist(details);
        }
    }

    private void prepareAndSendMessages(JoinTournament message, ISocketClient client, long tournamentId,
                                        IPromoCampaign campaign, String assignedPlayerAlias, boolean renamed,
                                        TournamentPromoTemplate<?> template, Pair<Long, Integer> accountIdAndBankId,
                                        MaxBalanceTournamentPlayerDetails details)
            throws CommonException, UnsupportedEncodingException, KafkaHandlerException {
        String sessionId = client.getSessionId();
        long minCoin = getMinCoin(accountIdAndBankId.getValue(), message.getGameId(),
                campaign.getBaseCurrency());
        boolean playerCanJoin = isPlayerCanJoin(template, details, minCoin, campaign);
        String domainName = message.getDomainName();
        String lang = client.getLang();
        String protocol =
                client.getUpgradeRequest().isSecure() || isForceHttps(client) ? "https://" : "http://";
        String homeUrl = prepareHomeUrl(domainName, sessionId, lang, client.getCdn(), protocol, message);
        String encodedHomeUrl = URLEncoder.encode(homeUrl, "UTF-8");
        String link = createLink(client, tournamentId, message.getGameId(),
                accountIdAndBankId.getValue(), domainName, encodedHomeUrl, lang, protocol);
        client.sendMessage(new JoinTournamentResponse(System.currentTimeMillis(), message.getRid(),
                tournamentId, details.getCurrentBalance(), link, renamed, assignedPlayerAlias));
        client.sendMessage(new PlayerTournamentStateChanged(tournamentId, !playerCanJoin, playerCanJoin,
                System.currentTimeMillis(), message.getRid()));
        sendBalanceUpdatedToAllServers(sessionId, serviceHandler.getBalance(sessionId, GameMode.REAL.name()));
    }

    private void performBuyIn(long tournamentId, IPromoCampaign campaign, Long accountId, String sessionId,
                              AccountInfo accountInfo, long buyInPrice, long buyInAmount) throws CommonException {
        try {
            buyInHelper.performBuyIn(sessionId, tournamentId, buyInPrice,
                    campaign.getBaseCurrency(), 1, buyInAmount, true);
        } catch (ForceCreateDetailsException e) {
            registerPlayer(accountInfo.getBankId(), accountInfo.getExternalId(), accountId,
                    tournamentId, buyInAmount, buyInPrice);
            throw e.getException();
        }
    }

    private void sendCampaignErrorMessage(JoinTournament message, ISocketClient client, Consumer<Error> errorSaver,
                                          IPromoCampaign campaign) {
        if (campaign.getStatus() == Status.CANCELLED) {
            sendErrorMessageToClient(message, client, errorSaver,
                    TOURNAMENT_NOT_FOUND, "Tournament canceled");
        } else {
            sendErrorMessageToClient(message, client, errorSaver,
                    TOURNAMENT_EXPIRED, "Tournament expired");
        }
    }

    private void sendErrorMessageToClient(JoinTournament message, ISocketClient client, Consumer<Error> errorSaver,
                                          int errorCode, String cause) {
        client.sendMessage(createErrorMessage(errorCode, cause, message.getRid(), errorSaver));
    }

    private IPromoCampaign getCampaign(JoinTournament message, long tournamentId) throws NoSuchFieldException {
        Long networkTournamentId = message.getNetworkTournamentId();
        IPromoCampaign campaign;
        if (networkTournamentId != null) {
            NetworkPromoCampaign promoCampaign =
                    (NetworkPromoCampaign) promoCampaignManager.getPromoCampaign(networkTournamentId);
            campaign = promoCampaign.getEvents().stream()
                    .filter(networkPromoEvent -> networkPromoEvent.getId() == tournamentId)
                    .findFirst()
                    .orElseThrow(NoSuchFieldException::new);
        } else {
            campaign = promoCampaignManager.getPromoCampaign(tournamentId);
        }
        return campaign;
    }

    private String prepareHomeUrl(String domainName, String sessionId, String lang, String cdn, String protocol,
                                  JoinTournament message) {
        String realModeUrl = message.getRealModeUrl();
        String homeUrl = protocol + domainName + "/tournamentlobby.do?" +
                BaseAction.SESSION_ID_ATTRIBUTE + "=" + sessionId + "&" +
                BaseAction.LANG_ID_ATTRIBUTE + "=" + lang +
                (StringUtils.isTrimmedEmpty(cdn) || "null".equals(cdn) ? "" : "&" + BaseAction.KEY_CDN + "=" + cdn) +
                (StringUtils.isTrimmedEmpty(realModeUrl) ? "" : "&" + BaseAction.REAL_MODE_URL + "=" + realModeUrl) + "&" +
                BaseAction.GAMEMODE_ATTRIBUTE + "=" + GameMode.REAL.getModePath();
        if (message.getNetworkTournamentId() != null) {
            homeUrl += "&" + BaseAction.GAME_ID_ATTRIBUTE + "=0";
        }
        return homeUrl;
    }

    private boolean isPlayerCanJoin(TournamentPromoTemplate<?> template, MaxBalanceTournamentPlayerDetails details,
                                    long minCoin, IPromoCampaign campaign) {
        if (campaign.isNetworkPromoCampaign()) {
            return details == null;
        }
        INetworkPromoEventTemplate<IPrize, ? extends INetworkPromoEventTemplate> networkPromoEventTemplate = (INetworkPromoEventTemplate) template;
        return (networkPromoEventTemplate.isReBuyEnabled() &&
                (networkPromoEventTemplate.getReBuyLimit() == -1 || details.getReBuyCount() < networkPromoEventTemplate.getReBuyLimit())) ||
                details.getCurrentBalance() >= minCoin;
    }

    private long getMinCoin(long bankId, long gameId, String currency) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        return serviceHandler.getCoins(bankInfo, gameId, CurrencyCache.getInstance().get(currency))
                .stream()
                .min(Long::compareTo)
                .orElse(1L);
    }

    private boolean isTournament(IPromoCampaign promo) {
        return promo != null && promo.getTemplate().getPromoType().isTournamentLogic();
    }

    private MaxBalanceTournamentPlayerDetails registerPlayer(long bankId, String extAccountId, long accountId, long tournamentId,
                                                             long buyInAmount, long buyInPrice) {
        MaxBalanceTournamentPlayerDetails details = new MaxBalanceTournamentPlayerDetails(bankId, extAccountId,
                accountId, tournamentId, "", buyInAmount, buyInPrice, 0, 0, buyInAmount,
                System.currentTimeMillis(), 0);
        LOG.info("Persist MaxBalanceTournamentPlayerDetails={}", details);
        maxBalanceTournamentPersister.persist(details);
        return details;
    }

    private String createLink(ISocketClient client, long tournamentId, long gameId, int bankId, String domainName,
                              String homeUrl, String lang, String protocol) {
        return protocol + domainName +
                "/tournamentstartgame.do?tournamentId=" + tournamentId +
                "&gameId=" + gameId +
                "&sessionId=" + client.getSessionId() +
                "&bankId=" + bankId +
                "&" + BaseAction.PARAM_HOME_URL + "=" + homeUrl +
                "&" + BaseAction.LANG_ID_ATTRIBUTE + "=" + lang +
                getCdnParam(client);
    }

    private void sendBalanceUpdatedToAllServers(String sessionId, long balance) {
        for (TournamentWebSocketMessageListener webSocketMessageListener : webSocketMessageListeners) {
            webSocketMessageListener.sendUpdateBalanceToAllServers(sessionId, balance);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
