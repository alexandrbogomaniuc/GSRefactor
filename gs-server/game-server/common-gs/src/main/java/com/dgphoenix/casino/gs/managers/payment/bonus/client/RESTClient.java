package com.abs.casino.gs.managers.payment.bonus.client;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.util.xml.IXmlRequestResult;
import com.abs.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.Parser;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.AbstractBonusClient;
import com.dgphoenix.casino.gs.managers.payment.bonus.IBonusClient;
import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableCWClient;
import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableContainer;
import com.abs.casino.gs.singlegames.tools.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * User: ktd
 * Date: 29.03.11
 */

public class RESTClient extends AbstractBonusClient implements IBonusClient, ILoggableCWClient {
    private static final Logger LOG = LogManager.getLogger(RESTClient.class);
    private Map<String, String> specialRequestHeadersMap;
    private String specialRequestHeaders;
    private ILoggableContainer loggableContainer;
    private final CassandraGameSessionPersister gameSessionPersister;

    public RESTClient(long bankId) {
        super(bankId);
        specialRequestHeaders = bankInfo.getCWSpecialRequestHeaders();
        if (!com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(specialRequestHeaders)) {
            try {
                specialRequestHeadersMap = CollectionUtils.stringToMap(specialRequestHeaders);
            } catch (Exception e) {
                LOG.error("Cannot parse CW_SPECIAL_REQUEST_HEADERS: '" + specialRequestHeaders +
                        "' , bankId=" + bankId, e);
            }
        }
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
    }

    protected Map<String, String> getSpecialRequestHeadersMap() {
        return specialRequestHeadersMap;
    }

    protected Map<String, String> prepareReleaseParams(Bonus bonus, String extUserId) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(CBonus.PARAM_USERID, extUserId);
        htbl.put(CBonus.PARAM_BONUSID, String.valueOf(bonus.getId()));
        htbl.put(CBonus.PARAM_AMOUNT, String.valueOf(bonus.getBalance()));
        if (bankInfo.isSendDetailsOnBonusRelease() && bonus.getLastGameSessionId() != null) {
            Long gameSessionId = bonus.getLastGameSessionId();
            GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
            if (gameSession == null) {
                gameSession = gameSessionPersister.get(gameSessionId);
            }
            htbl.put(CBonus.PARAM_GAMESESSIONID, String.valueOf(gameSessionId));
            htbl.put(CBonus.PARAM_TRANSACTIONID, String.valueOf(bonus.getId()));

            if (gameSession != null) {
                long gameId = gameSession.getGameId();
                String extGameId = BaseGameCache.getInstance().getExternalGameId(gameId, getBankId());
                if (isTrimmedEmpty(extGameId)) {
                    extGameId = String.valueOf(gameId);
                }
                htbl.put(CBonus.PARAM_GAMEID, extGameId);
            } else {
                LOG.warn("prepareReleaseParams: gameSession non found for gameSessionId=" + gameSessionId);
            }
        }
        List<String> paramList = new ArrayList<>();
        paramList.add(extUserId);
        paramList.add(String.valueOf(bonus.getId()));
        paramList.add(String.valueOf(bonus.getBalance()));
        htbl.put(CBonus.PARAM_HASH, getHashValue(paramList));
        return htbl;
    }

    @Override
    public void bonusRelease(Bonus bonus, String extUserId) throws CommonException {
        Map<String, String> htbl = prepareReleaseParams(bonus, extUserId);
        IXmlRequestResult output = request(htbl, getBonusReleaseUrl());
        if (!output.isSuccessful()) {
            throw new BonusException("RESTClient:bonus release response was not successful");
        }
    }

    protected Map<String, String> prepareAuthParams(String token, String gameId) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(CBonus.PARAM_TOKEN, token);
        if (gameId != null) {
            htbl.put(CBonus.PARAM_GAMEID, gameId);
        }

        List<String> paramList = new ArrayList<>();
        paramList.add(token);
        htbl.put(CBonus.PARAM_HASH, getHashValue(paramList));

        return htbl;
    }

    protected Map<String, String> prepareAuthParams(String token) throws CommonException {
        return prepareAuthParams(token, null);
    }

    @Override
    public BonusAuthResult authenticate(String token, String gameId) throws CommonException {
        Map<String, String> htbl = prepareAuthParams(token, gameId);
        IXmlRequestResult output = request(htbl, getAuthUrl());

        if (!output.isSuccessful()) {
            throw new BonusException("RESTClient:auth response was not successful");
        }

        String userId = (String) output.getResponseParameters().get(CBonus.USERID_TAG);
        String userName = (String) output.getResponseParameters().get(CBonus.USERNAME_TAG);
        String firstName = (String) output.getResponseParameters().get(CBonus.FIRSTNAME_TAG);
        String lastName = (String) output.getResponseParameters().get(CBonus.LASTNAME_TAG);
        String email = (String) output.getResponseParameters().get(CBonus.EMAIL_TAG);
        String currency = (String) output.getResponseParameters().get(CBonus.CURRENCY_TAG);

        String countryCode = (String) output.getResponseParameters().get(CBonus.COUNTRYCODE_TAG);

        return new BonusAuthResult(userId, userName, firstName, lastName, email, currency, output.isSuccessful(),
                countryCode);
    }

    @Override
    public BonusAuthResult authenticate(String token) throws CommonException {
        return authenticate(token, null);
    }


    protected Map<String, String> prepareAccountParams(String userId) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(CBonus.PARAM_USERID, userId);

        List<String> paramList = new ArrayList<>();
        paramList.add(userId);
        htbl.put(CBonus.PARAM_HASH, getHashValue(paramList));
        return htbl;
    }

    @Override
    public BonusAccountInfoResult getAccountInfo(String userId) throws CommonException {
        Map<String, String> htbl = prepareAccountParams(userId);
        IXmlRequestResult output = request(htbl, getAccountInfo());
        if (!output.isSuccessful()) {
            throw new BonusException("RESTClient:account info response was not successful");
        }

        String userName = (String) output.getResponseParameters().get(CBonus.USERNAME_TAG);
        String firstName = (String) output.getResponseParameters().get(CBonus.FIRSTNAME_TAG);
        String lastName = (String) output.getResponseParameters().get(CBonus.LASTNAME_TAG);
        String email = (String) output.getResponseParameters().get(CBonus.EMAIL_TAG);
        String currency = (String) output.getResponseParameters().get(CBonus.CURRENCY_TAG);

        String countryCode = (String) output.getResponseParameters().get(CBonus.COUNTRYCODE_TAG);

        return new BonusAccountInfoResult(userName, firstName, lastName, email, currency, output.isSuccessful(),
                countryCode);
    }

    private String getBonusReleaseUrl() throws CommonException {
        return bankInfo.getBonusReleaseUrl();
    }

    private String getAuthUrl() throws CommonException {
        return bankInfo.getBonusAuthUrl();
    }

    protected String getAccountInfo() throws CommonException {
        return bankInfo.getBonusAccountInfoUrl();
    }

    protected String getHashValue(List params) throws BonusException {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object param : params) {
                sb.append(param);
            }
            sb.append(getBonusPass());

            return StringUtils.getMD5(sb.toString());
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    protected XmlRequestResult request(Map<String, String> htbl, String url) throws CommonException {
        try {

            if (bankInfo.isSendBankIdToExtApi()) {
                htbl.put(CBonus.PARAM_BANKID, bankInfo.getExternalBankId());
            }

            LOG.info("request, request to url:" + url + " bankId:" + getBankId() +
                    ", isPost: " + isPost() + ", is:" + printRequestParams(htbl));

            logUrl(url);
            logRequest(htbl);
            String sb = HttpClientConnection.newInstance().doRequest(bankInfo.isUsesJava8Proxy(), url, htbl, isPost(),
                    getSpecialRequestHeadersMap(), bankInfo.isUseHttpProxy());
            logResponse(sb);

            LOG.info("request, response from url:" + url + " bankId:" + getBankId() + " is:" + sb +
                    (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(specialRequestHeaders) ?
                            "" : " specialRequestHeaders:" + specialRequestHeaders));

            XmlRequestResult result = new XmlRequestResult();
            Parser parser = Parser.instance();
            parser.parse(sb, result);

            return result;
        } catch (Exception e) {
            LOG.error("RESTClient::request error:", e);
            throw new CommonException(e);
        }
    }

    protected boolean isPost() {
        return true;
    }

    private String printRequestParams(Map<String, String> htbl) {
        StringBuilder sb = new StringBuilder(" request parameters:");
        for (Map.Entry<String, String> entry : htbl.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }

    protected String getBonusPass() {
        return bankInfo.getBonusPassKey();
    }

    @Override
    public void setLoggableContainer(ILoggableContainer loggableContainer) {
        this.loggableContainer = loggableContainer;
    }

    @Override
    public void logUrl(String url) {
        if (loggableContainer != null) {
            loggableContainer.logUrl(url);
        }
    }

    @Override
    public void logRequest(Map<String, String> params) {
        if (loggableContainer != null) {
            loggableContainer.logRequest(printRequestParams(params));
        }
    }

    @Override
    public void logResponse(String response) {
        if (loggableContainer != null) {
            loggableContainer.logResponse(response);
        }
    }

    @Override
    public String getUrl() {
        return loggableContainer != null ? loggableContainer.getUrl() : EMPTY;
    }

    @Override
    public String getRequest() {
        return loggableContainer != null ? loggableContainer.getRequest() : EMPTY;
    }

    @Override
    public String getResponse() {
        return loggableContainer != null ? loggableContainer.getResponse() : EMPTY;
    }
}