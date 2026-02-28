package com.abs.casino.websocket.tournaments.handlers;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.gs.socket.mq.TournamentBuyInHelper;
import com.dgphoenix.casino.kafka.dto.KafkaHandlerException;
import com.abs.casino.promo.tournaments.ErrorCodes;
import com.abs.casino.promo.tournaments.messages.Error;
import com.abs.casino.promo.tournaments.messages.JoinBattleground;
import com.abs.casino.promo.tournaments.messages.JoinBattlegroundResponse;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.abs.casino.websocket.tournaments.ISocketClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.function.Consumer;

import static com.abs.casino.promo.tournaments.ErrorCodes.NOT_LOGGED_IN;

public class JoinBattlegroundHandler extends AbstractJoinPromoHandler<JoinBattleground> {

    private static final Logger LOG = LogManager.getLogger(JoinBattlegroundHandler.class);
    private final TournamentBuyInHelper buyInHelper;
    private final MQServiceHandler mqServiceHandler;

    public JoinBattlegroundHandler(ErrorPersisterHelper errorPersisterHelper,
                                   TournamentBuyInHelper buyInHelper,
                                   MQServiceHandler mqServiceHandler) {
        super(errorPersisterHelper);
        this.buyInHelper = buyInHelper;
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public void handle(JoinBattleground message, ISocketClient client) {
        Consumer<Error> errorSaver = error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message);
        if (client.isConnected()) {
            try {
                Pair<Long, Integer> accountIdAndBankId = getAccountIdAndBankId(client, message.getRid(), errorSaver);
                AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountIdAndBankId.getKey());
                long balance;
                balance = getBalance(client, accountInfo);

                boolean isPlayerCanJoin = message.getBuyIn() > 0 && message.getBuyIn() <= balance;
                if (!isPlayerCanJoin) {
                    sendErrorMessageToClient(message.getRid(), client, errorSaver,
                            CommonWalletErrors.INSUFFICIENT_FUNDS.getCode(), "Not enough money");
                    return;
                }

                if (!buyInHelper.isBuyInCorrect(client.getSessionId(), accountInfo.getBankId(),
                        accountInfo.getCurrency().getCode(), message.getBuyIn(), message.getGameId())) {
                    sendErrorMessageToClient(message.getRid(), client, errorSaver, ErrorCodes.BAD_REQUEST, "Bad request");
                    return;
                }
                prepareAndSendMessages(message, client, accountIdAndBankId.getValue(), message.getBuyIn());
            } catch (Exception e) {
                processUnexpectedError(client, message, e,
                        error -> errorPersisterHelper.persistTournamentError(client.getSessionId(), error, message, e));
            }
        } else {
            sendErrorMessageToClient(message.getRid(), client, errorSaver, NOT_LOGGED_IN, "Not logged in");
        }
    }

    private long getBalance(ISocketClient client, AccountInfo accountInfo) throws KafkaHandlerException {
        long balance;
        try {
            balance = mqServiceHandler.getBalance(client.getSessionId(), GameMode.REAL.name());
            LOG.debug("getBalance: accountId={}, balance={}", accountInfo.getId(), balance);
        } catch (Exception e) {
            LOG.error("Cannot getBalance, account={}", accountInfo, e);
            throw e;
        }
        return balance;
    }

    private void prepareAndSendMessages(JoinBattleground message, ISocketClient client, Integer bankId, long buyIn)
            throws CommonException, UnsupportedEncodingException {
        String sessionId = client.getSessionId();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String domainName = bankInfo.getStartGameDomain();
        String lang = client.getLang();
        String protocol =
                client.getUpgradeRequest().isSecure() || isForceHttps(client) ? "https://" : "http://";
        String homeUrl = prepareHomeUrl(domainName, sessionId, lang, client.getCdn(), protocol);
        String encodedHomeUrl = URLEncoder.encode(homeUrl, "UTF-8");
        String link = createLink(client, message.getGameId(), bankId, domainName, encodedHomeUrl, lang, protocol, buyIn);
        client.sendMessage(new JoinBattlegroundResponse(System.currentTimeMillis(), message.getRid(), link));
    }


    private String prepareHomeUrl(String domainName, String sessionId, String lang, String cdn, String protocol) {
        return protocol + domainName + "/tournamentlobby.do?" +
                BaseAction.SESSION_ID_ATTRIBUTE + "=" + sessionId + "&" +
                BaseAction.LANG_ID_ATTRIBUTE + "=" + lang +
                (StringUtils.isTrimmedEmpty(cdn) || "null".equals(cdn) ? "" : "&" + BaseAction.KEY_CDN + "=" + cdn) + "&" +
                BaseAction.GAMEMODE_ATTRIBUTE + "=" + GameMode.REAL.getModePath() + "&" +
                BaseAction.SHOW_BATTLEGROUND_TAB + "=true";
    }


    private String createLink(ISocketClient client, long gameId, int bankId, String domainName,
                              String homeUrl, String lang, String protocol, long buyIn) {
        return protocol + domainName +
                "/battlegroundstartgame.do?gameId=" + gameId +
                "&sessionId=" + client.getSessionId() +
                "&bankId=" + bankId +
                "&buyIn=" + buyIn +
                "&" + BaseAction.PARAM_HOME_URL + "=" + homeUrl +
                "&" + BaseAction.LANG_ID_ATTRIBUTE + "=" + lang +
                getCdnParam(client);
    }


    @Override
    public Logger getLog() {
        return LOG;
    }
}
