package com.abs.casino.gs.managers.payment.bonus.client.frb;

import com.abs.casino.account.AccountManager;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.abs.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWinOperation;
import com.abs.casino.common.exception.BonusException;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.exception.FRBException;
import com.abs.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.IdGenerator;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.util.xml.IXmlRequestResult;
import com.abs.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.Parser;
import com.abs.casino.common.web.bonus.CBonus;
import com.abs.casino.gs.managers.payment.bonus.AbstractBonusClient;
import com.abs.casino.gs.managers.payment.bonus.FRBonusWinRequestFactory;
import com.abs.casino.gs.managers.payment.bonus.IDescriptionProducer;
import com.abs.casino.gs.managers.payment.bonus.IFRBonusClient;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;
import com.abs.casino.gs.managers.payment.wallet.ILoggableCWClient;
import com.abs.casino.gs.managers.payment.wallet.ILoggableContainer;
import com.abs.casino.gs.singlegames.tools.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.EMPTY;

public class FRBRESTClient extends AbstractBonusClient implements IFRBonusClient, ILoggableCWClient {
    private static final Logger LOG = LogManager.getLogger(FRBRESTClient.class);
    private static final String ZERO_AMOUNT = "0.00";

    private Map<String, String> specialRequestHeadersMap;
    private String specialRequestHeaders;
    private ILoggableContainer loggableContainer;

    public FRBRESTClient(long bankId) {
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
    }

    protected Map<String, String> getSpecialRequestHeadersMap() {
        return specialRequestHeadersMap;
    }

    protected Map<String, String> prepareBonusWinParams(String userId, long bonusId, long amount, IFRBonusWinOperation operation, long gameId) throws CommonException {
        HashMap<String, String> params = new HashMap<>();
        params.put(CBonus.PARAM_USERID, userId);
        params.put(CBonus.PARAM_BONUSID, String.valueOf(bonusId));
        params.put(CBonus.PARAM_AMOUNT, String.valueOf(amount));
        params.put(CBonus.PARAM_TRANSACTIONID, String.valueOf(operation.getId()));

        List<String> hashParams = new ArrayList<>();
        hashParams.add(userId);
        hashParams.add(String.valueOf(bonusId));
        hashParams.add(String.valueOf(amount));

        params.put(CBonus.PARAM_HASH, getHashValue(hashParams));
        return params;
    }

    protected Map<String, String> prepareBonusBetParams(String userId, long bonusId, long transactionId,
                                                        String extGameId, Long roundId, Long gameSessionId,
                                                        boolean roundFinished)
            throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(CBonus.PARAM_USERID, userId);
        htbl.put(CBonus.PARAM_BONUSID, String.valueOf(bonusId));
        htbl.put(CBonus.PARAM_BETAMOUNT, ZERO_AMOUNT);
        htbl.put(CBonus.PARAM_TRANSACTIONID, String.valueOf(transactionId));
        if (bankInfo.isSendDetailsOnFrbWin()) {
            htbl.put(CBonus.PARAM_GAMEID, extGameId);
            htbl.put(CBonus.PARAM_ROUNDID, String.valueOf(roundId));
            htbl.put(CBonus.PARAM_ISROUNDFINISHED, String.valueOf(roundFinished).toLowerCase());
            htbl.put(CBonus.PARAM_GAMESESSIONID, String.valueOf(gameSessionId));
        }
        List<String> paramList = new ArrayList<>();
        paramList.add(userId);
        paramList.add(String.valueOf(bonusId));
        paramList.add(ZERO_AMOUNT);

        htbl.put(CBonus.PARAM_HASH, getHashValue(paramList));
        return htbl;
    }

    @Override
    public FRBonusWinResult bonusWin(long accountId, String extUserId, Boolean isRoundFinished, long bonusId, String extBonusId, long amount,
                                     FRBWinOperation operation, long gameId, String extGameId, FRBonusWin frbonusWin) throws CommonException {

        Map<String, String> params = prepareBonusWinParams(extUserId, bonusId, amount, operation, gameId);
        if (bankInfo.isSendExtBonusId()) {
            params.put(CBonus.PARAM_EXTBONUSID, extBonusId);
        }
        if (bankInfo.isSendGameIdOnFrbWin()) {
            params.put(CBonus.PARAM_GAMEID, extGameId);
        }
        FRBWinOperation winOperation = frbonusWin.getFRBonusWinOperation(gameId);
        Long roundId = winOperation.getRoundId();
        Long gameSessionId = winOperation.getGameSessionId();
        boolean roundFinished = frbonusWin.isRoundFinished(gameId);
        if (bankInfo.isAddTokenMode()) {
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
            if (accountInfo != null) {
                params.put(CBonus.PARAM_TOKEN, accountInfo.getFinsoftSessionId());
            }
        }
        if (bankInfo.isSendDetailsOnFrbWin()) {
            params.put(CBonus.PARAM_GAMEID, extGameId);
            params.put(CBonus.PARAM_ROUNDID, String.valueOf(roundId));
            params.put(CBonus.PARAM_ISROUNDFINISHED, String.valueOf(roundFinished).toLowerCase());
            params.put(CBonus.PARAM_GAMESESSIONID, String.valueOf(gameSessionId));
        }
        if (bankInfo.isSendClientTypeOnFRBWin() && operation.getClientType() != null) {
            params.put(CBonus.PARAM_CLIENT_TYPE, operation.getClientType().name());
        }
        if (bankInfo.isSendZeroBetOnFrbWin() && frbonusWin.isNewRound(gameId)) {
            Map<String, String> betParams = prepareBonusBetParams(extUserId, bonusId, IdGenerator.getInstance().getNext(FRBWinOperation.class),
                    extGameId, roundId, gameSessionId, isRoundFinished);
            String betUrl = bankInfo.getSendZeroBetFrbUrl();
            if (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(betUrl)) {
                betUrl = getFRBonusWinURL();
            }
            IXmlRequestResult output = request(betParams, betUrl);
            if (!output.isSuccessful()) {
                Object code = output.getResponseParameters().get("CODE");
                if (code != null) {
                    throw new FRBException("FRB win (send zero bet) response was not successful", (String) code);
                } else {
                    throw new CommonException("FRB win (send zero bet) response was not successful");
                }
            }
            frbonusWin.setNewRound(gameId, false);
        }
        if (FRBonusWinRequestFactory.getInstance().getFRBonusWinManager(bankInfo.getId()) instanceof IDescriptionProducer
                && !com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(operation.getDescription())) {
            params.put(CBonus.PARAM_DESCRIPTION, operation.getDescription());
        }
        IXmlRequestResult output = request(params, getFRBonusWinURL());

        if (!output.isSuccessful()) {
            Object code = output.getResponseParameters().get("CODE");
            if (code != null) {
                throw new FRBException("FRB win response was not successful", (String) code);
            } else {
                throw new CommonException("FRB win response was not successful");
            }
        }

        long balance = 0;
        String balanceStr = (String) output.getResponseParameters().get(CBonus.BALANCE);
        if (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(balanceStr)) {
            LOG.warn("balance tag in not found");
        } else {
            try {
                if (balanceStr.contains(".")) {
                    balance = Double.valueOf(balanceStr).longValue();
                } else {
                    balance = Long.parseLong(balanceStr);
                }
            } catch (NumberFormatException e) {
                LOG.error("Cannot parse balance: " + balanceStr);
            }
        }

        return new FRBonusWinResult(balance, output.isSuccessful());
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

        IXmlRequestResult output = request(htbl, getAccountInfoURL());

        if (!output.isSuccessful()) {
            throw new BonusException("FRBRESTClient:account info response was not successful");
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

            if (LOG.isDebugEnabled()) {
                LOG.debug("request, request to url:" + url + " bankId:" + getBankId() +
                        " isPost: " + isPost() + ", " + " is:" + printRequestParams(htbl));
            }

            logUrl(url);
            logRequest(htbl);
            String sb = HttpClientConnection.newInstance().doRequest(bankInfo.isUsesJava8Proxy(), url, htbl, isPost(),
                    getSpecialRequestHeadersMap(), isUseHttpProxy());
            logResponse(sb);

            if (LOG.isDebugEnabled()) {
                LOG.debug("request, response from url:" + url + " bankId:" + getBankId() + " is:" + sb +
                        (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(specialRequestHeaders) ?
                                "" : " specialRequestHeaders:" + specialRequestHeaders));

            }
            XmlRequestResult result = new XmlRequestResult();
            Parser parser = Parser.instance();
            parser.parse(sb, result);
            return result;
        } catch (Exception e) {
            LOG.error("request error:", e);
            throw new CommonException(e);
        }
    }

    private String printRequestParams(Map<String, String> htbl) {
        StringBuilder sb = new StringBuilder(" request parameters:");
        for (Map.Entry<String, String> entry : htbl.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }

    private String getFRBonusWinURL() throws CommonException {
        return bankInfo.getFRBonusWinURL();
    }

    private String getAccountInfoURL() throws CommonException {
        return bankInfo.getBonusAccountInfoUrl();
    }

    protected boolean isPost() {
        return true;
    }

    protected boolean isUseHttpProxy() {
        return bankInfo.isUseHttpProxy();
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
