package com.abs.casino.payment.wallet.client.v2;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.promo.PromoWinInfo;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.NumberUtils;
import com.abs.casino.common.util.property.PropertyUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.util.xml.IXmlRequestResult;
import com.abs.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.Parser;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import com.abs.casino.statistics.http.HttpClientCallbackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.EMPTY;


public class RESTCWClient extends AbstractCWClient implements ICommonWalletClient, ILoggableCWClient {
    public static final String JP_BET = "jpContribution";
    public static final String JP_WIN = "jpWin";
    public static final String UNJ_BET = "unjContribution";
    public static final String UNJ_WIN = "unjWin";
    public static final String JP_BET_DETAILS = "jpContributionDetails";
    public static final long MAX_UNKNOWN_TRANSACTION_TIME = 5 * 60 * 1000;

    private static final Logger LOG = LogManager.getLogger(RESTCWClient.class);
    private static final String DOUBLEUP_CMD = "DOUBLEUP";
    private static final String TRIPLEUP_CMD = "TRIPLEUP";
    private static final String BONUS_CMD = "DOBONUS";
    private static final String WILD_CMD = "DOWILD";
    private static final String STAR_WILD_CMD = "DOSTAR";

    protected BankInfo bankInfo;
    private Map<String, String> specialRequestHeadersMap;
    protected final String specialRequestHeaders;

    private ILoggableContainer loggableContainer;
    protected final boolean useSingleGameIdForAllDevices;
    protected final boolean alwaysSendClientType;
    protected boolean sendGameIdOnAuth;
    protected boolean addGameIdToHashOnAuth;
    protected boolean addClientTypeToHashOnAuth;
    protected boolean addClientTypeToHashOnWager;
    protected boolean sendAmountInDollars;
    protected boolean sendRealBetWin;
    protected boolean sendSpecialWeaponBet;
    protected boolean supportPromoBalanceTransfer;

    public RESTCWClient(long bankId) {
        super(bankId);
        bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
        useSingleGameIdForAllDevices = bankInfo.isUseSingleGameIdForAllDevices();
        alwaysSendClientType = bankInfo.isAlwaysSendClientType();
        specialRequestHeaders = bankInfo.getCWSpecialRequestHeaders();
        sendGameIdOnAuth = bankInfo.isSendGameIdOnAuth();
        addGameIdToHashOnAuth = bankInfo.isAddGameIdToHashOnAuth();
        addClientTypeToHashOnAuth = PropertyUtils.getBooleanProperty(bankInfo.getProperties(), "ADD_CLIENTTYPE_TO_HASH_ON_AUTH");
        addClientTypeToHashOnWager = PropertyUtils.getBooleanProperty(bankInfo.getProperties(), "ADD_CLIENTTYPE_TO_HASH_ON_WAGER");
        sendAmountInDollars = bankInfo.isCWSendAmountInDollars();
        sendRealBetWin = bankInfo.isCWSendRealBetWin();
        sendSpecialWeaponBet = bankInfo.isCWSendSpecialWeaponBet();
        supportPromoBalanceTransfer = bankInfo.isSupportPromoBalanceTransfer();
        if (!StringUtils.isTrimmedEmpty(specialRequestHeaders)) {
            try {
                specialRequestHeadersMap = CollectionUtils.stringToMap(specialRequestHeaders);
            } catch (Exception e) {
                LOG.error("Cannot parse CW_SPECIAL_REQUEST_HEADERS: '{}', bankId={}", specialRequestHeaders, bankId, e);
            }
        }
    }

    protected Map<String, String> getSpecialRequestHeadersMap() {
        return specialRequestHeadersMap;
    }

    protected boolean isSendClientType() {
        return useSingleGameIdForAllDevices || alwaysSendClientType;
    }

    @Override
    public CommonWalletWagerResult wager(long accountId, String extUserId, String bet, //bet_amount|transactionID
                                         String win, //win_amount|transactionID
                                         Boolean isRoundFinished, long gsRoundId, long mpRoundId, long gameId, long bankId,
                                         CommonWalletOperation operation, CommonWallet wallet, ClientType clType,
                                         Currency curr)
            throws CommonException {

        long gameSessionId = operation.getGameSessionId();
        String additionalProperties = operation.getAdditionalProperties();
        Map<String, String> params = StringUtils.isTrimmedEmpty(additionalProperties) ?
                new HashMap<>() : CollectionUtils.stringToMap(additionalProperties);
        if (!params.containsKey(CLIENT_TYPE_PARAM) && isSendClientType()) {
            String clientType = clType == null ? ClientType.FLASH.name() : clType.name();
            params.put(CLIENT_TYPE_PARAM, clientType);
            String s = StringUtils.isTrimmedEmpty(additionalProperties) ? "" : additionalProperties + ";";
            //need save, operation may be failed and GameSession closed
            operation.setAdditionalProperties(s + CLIENT_TYPE_PARAM + "=" + clientType);
        }
        Map<String, String> result = prepareWagerParams(wallet, params, accountId, extUserId, bet, win,
                isRoundFinished, gsRoundId, mpRoundId, getExternalGameId(gameId, bankId), bankId,
            gameSessionId, operation.getNegativeBet(), clType, curr.getCode(), operation.getCmd());
        IXmlRequestResult output = request(result, getWagerUrl(bankId), bankId);
        if (!output.isSuccessful()) {
            String code = output.getResponseCode();
            if (isPreciseWagerError(code)) {
                return new CommonWalletWagerResult(code);
            }
            throw new WalletException("RESTCWClient:wager response was not successful", code);
        }
        String extSystemTransactionId = (String) output.getResponseParameters().get(CCommonWallet.CWTRANSACTIONID_TAG);
        double balance = 0.0;
        if (output.getResponseParameters().get(CCommonWallet.BALANCE_TAG) != null &&
                ((String) output.getResponseParameters().get(CCommonWallet.BALANCE_TAG)).length() > 0) {
            balance = Double.parseDouble((String) output.getResponseParameters().get(CCommonWallet.BALANCE_TAG));
        }
        return new CommonWalletWagerResult(extSystemTransactionId, balance, output.isSuccessful());
    }

    protected String getExternalGameId(long gameId, long bankId) {
        String externalGameId = BaseGameCache.getInstance().getExternalGameId(gameId, bankId);
        if (externalGameId == null) {
            externalGameId = String.valueOf(gameId);
        } else {
            LOG.info("Game was changed before sending from {} to {}", gameId, externalGameId);
        }
        return externalGameId;
    }

    protected void checkUnsuccessStatusCode(long accountId, String extUserId, long transactionId,
                                            long bankId, CommonWalletOperation operation,
                                            IXmlRequestResult output) throws CommonException {
        String code = output.getResponseCode();
        if (StringUtils.isTrimmedEmpty(code)) {
            LOG.warn("checkUnsuccessStatusCode: returned empty code={}", output);
            throw new CommonException("RESTCWClient:getStatus response was not successful with empty error code");
        }
        long operationStartTime = operation.getStartTime();
        if (code.equals(String.valueOf(CommonWalletErrors.UNKNOWN_TRANSACTION_ID.getCode()))) {
            if (System.currentTimeMillis() - operationStartTime > MAX_UNKNOWN_TRANSACTION_TIME) {
                LOG.warn("getStatus: accountId={}, extUserId={}, casinoTransactionId={}, received error code: {}, and MAX_UNKNOWN_TRANSACTION_TIME " +
                        "is reached. getStatus success", accountId, extUserId, transactionId, code);
            } else {
                throw new WalletException("RESTCWClient:getStatus response was not successful", code);
            }
        }
    }

    public CommonWalletStatusResult getExternalTransactionStatus(long accountId, String extUserId,
                                                                 long transactionId, long bankId,
                                                                 CommonWalletOperation operation)
            throws CommonException {
        Map<String, String> htbl = prepareStatusParams(extUserId, transactionId);

        if (getBankId() == 123l && operation != null && operation.getRoundId() != null) {
            htbl.put(CCommonWallet.PARAM_ROUNDID, String.valueOf(operation.getRoundId()));
        }
        IXmlRequestResult output = request(htbl, getStatusUrl(bankId), bankId);
        boolean success = output.isSuccessful();
        if (!success) {
            checkUnsuccessStatusCode(accountId, extUserId, transactionId, bankId, operation, output);
        }
        return new CommonWalletStatusResult((String) output.getResponseParameters().get(
                CCommonWallet.CWTRANSACTIONID_TAG), success);
    }

    public double getBalance(long accountId, String extUserId, long bankId, Currency currency) throws CommonException {
        Map<String, String> htbl = prepareGetBalanceParams(extUserId);
        IXmlRequestResult output = request(htbl, getBalanceUrl(bankId), bankId);
        if (!output.isSuccessful()) {
            String code = output.getResponseCode();
            if (StringUtils.isTrimmedEmpty(code)) {
                throw new CommonException("RESTCWClient:response was not successful");
            }
            throw new WalletException("RESTCWClient:can't get balance, response was not successful", code);
        }
        String strBalance = (String) output.getResponseParameters().get(CCommonWallet.BALANCE_TAG);
        if (StringUtils.isTrimmedEmpty(strBalance)) {
            LOG.error("Can't get balance for account {}, bankId = {}, url = {}", accountId, bankId, getBalanceUrl(bankId));
            throw new CommonException("RESTCWClient:unknown error.");
        }
        double balance;
        try {
            balance = Double.parseDouble(strBalance);
        } catch (NumberFormatException e) {
            LOG.error("Invalid balance for account {}, bankId = {}, url = {}.", accountId, bankId, getBalanceUrl(bankId));
            throw new CommonException("RESTCWClient:invalid balance value");
        }
        return balance;
    }

    public void cancelTransaction(long accountId, String extUserId, long transactionId, long bankId)
            throws CommonException {
        Map<String, String> htbl = prepareCancelParams(extUserId, transactionId);

        request(htbl, getCancelUrl(bankId), bankId);
    }

    @Override
    public void setAdditionalOperationProperties(CommonWalletOperation operation, IWalletDBLink dbLink) {
        if (dbLink == null) { //refund/revoke debit
            return;
        }
        //jp related code removed
    }

    @Override
    public void completeOperation(AccountInfo accountInfo, long gameId, WalletOperationStatus internalStatus,
                                  CommonGameWallet gameWallet, CommonWalletOperation operation,
                                  IExternalWalletTransactionHandler extHandler) {
        //nop by default
    }

    @Override
    public void revokeDebit(AccountInfo accountInfo, long bankId, long gameId, CommonWallet cWallet,
                            CommonWalletOperation debitOperation,
                            IExternalWalletTransactionHandler extHandler) throws WalletException {
        //nop by default
    }

    @Override
    public boolean isAlwaysCompleteFailedCreditOperations() {
        return false;
    }

    //return true if need re-throw exception
    @Override
    public boolean postProcessCreditException(WalletException e, CommonWallet cWallet,
                                              CommonWalletOperation creditOperation,
                                              AccountInfo accountInfo, long gameId) {
        return true;
    }

    @Override
    public void postProcessSuccessCredit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished,
                                         CommonWallet cWallet, CommonWalletOperation operation)
            throws WalletException {
        //nop by default
    }

    @Override
    public void postProcessSuccessDebit(AccountInfo accountInfo, long gameId, long betAmount, CommonWallet cWallet,
                                        CommonWalletOperation operation) throws WalletException {
        //nop by default
    }

    private String prepareAmount(long amount, String stringAmount) {
        if (amount < 0) {
            String fetchedAmount = fetchAmount(stringAmount);
            if (fetchedAmount.isEmpty()) {
                return "";
            }
            if (sendAmountInDollars) {
                return fetchedAmount.contains(".") ? fetchedAmount : getAmountInDollars(Long.parseLong(fetchedAmount));
            } else {
                return fetchedAmount;
            }
        } else {
            return sendAmountInDollars ? getAmountInDollars(amount) : String.valueOf(amount);
        }
    }

    private String getAmountInDollars(long amount) {
        return String.valueOf(NumberUtils.asMoney(amount / 100d));
    }

    private String fetchAmount(String amountWithTransactionId) {
        String[] amountAndTransactionId = amountWithTransactionId.split("\\|");
        return amountAndTransactionId[0];
    }

    protected Map<String, String> prepareWagerParams(CommonWallet wallet, Map<String, String> params, long accountId,
                                                     String extUserId,
                                                     String bet, String win, Boolean isRoundFinished, long gsRoundId, long mpRoundId,
                                                     String gameId, long bankId, long gameSessionId,
                                                     long negativeBet, ClientType clientType, String currencyCode,
                                                     String cmd)
            throws CommonException {
        params.put(CCommonWallet.PARAM_USERID, extUserId);
        boolean doubleUp;
        if (!bet.isEmpty()) {
            params.put(CCommonWallet.PARAM_BET, bet);
        }

        if (!win.isEmpty()) {
            params.put(CCommonWallet.PARAM_WIN, win);
            if (bankInfo.isSendBonusFlag()
                    && cmd != null && (cmd.equals(BONUS_CMD) || cmd.equals(WILD_CMD) || cmd.equals(STAR_WILD_CMD))) {
                params.put(CCommonWallet.IS_BONUS_ROUND, Boolean.TRUE.toString());
            }
            if (sendRealBetWin) {
                CommonWalletOperation operation = getCommonWalletOperation(gameId, bankId, wallet);
                if (operation != null) {
                    String realBet = prepareAmount(operation.getRealBet(), bet);
                    String realWin = prepareAmount(operation.getRealWin(), win);
                    if (!StringUtils.isTrimmedEmpty(realBet)) {
                        params.put(CCommonWallet.PARAM_REAL_BET, realBet);
                    }
                    if (!StringUtils.isTrimmedEmpty(realWin)) {
                        params.put(CCommonWallet.PARAM_REAL_WIN, realWin);
                    }
                }
            }
            if (sendSpecialWeaponBet) {
                CommonWalletOperation operation = getCommonWalletOperation(gameId, bankId, wallet);
                if (operation != null) {
                    long swBet = operation.getSwBet();
                    if (swBet >= 0) {
                        params.put(CCommonWallet.PARAM_SW_BET, prepareAmount(swBet, ""));
                    }
                }
            }
            if (supportPromoBalanceTransfer) {
                PromoWinInfo promoWinInfo = walletHelper.getPromoWinInfo(gsRoundId);
                if (promoWinInfo != null) {
                    String promoWinAmount = prepareAmount(promoWinInfo.getAmount(), "");
                    params.put(CCommonWallet.PROMO_WIN_AMOUNT, promoWinAmount);
                    params.put(CCommonWallet.PROMO_ID, String.valueOf(promoWinInfo.getCampaignId()));
                    params.put(CCommonWallet.PROMO_CAMPAIGN_TYPE, promoWinInfo.getCampaignType());
                }
            }
        } else {
            //tig bank
            if (bankId == 102L) {
                params.put(CCommonWallet.PARAM_WIN, "");
            }
        }
        if (isRoundFinished != null) {
            params.put(CCommonWallet.PARAM_ROUND_FINISHED, Boolean.toString(isRoundFinished));
        }

        params.put(CCommonWallet.PARAM_GAMESESSIONID, String.valueOf(gameSessionId));
        params.put(CCommonWallet.PARAM_ROUNDID, String.valueOf(gsRoundId));
        params.put(CCommonWallet.PARAM_REALGAMEROUNDID, String.valueOf(mpRoundId));
        params.put(CCommonWallet.PARAM_GAMEID, gameId);
        return params;
    }

    protected CommonWalletOperation getCommonWalletOperation(String gameId, long bankId,
                                                             CommonWallet wallet) throws CommonException {
        Long originalId = getOriginalGameId(gameId, bankId);
        return wallet.getGameWalletWinOperation(originalId.intValue());
    }

    protected Long getOriginalGameId(String gameId, long bankId) throws CommonException {
        Long originalId = BaseGameCache.getInstance().getOriginalGameId(gameId, bankId);
        if (originalId == null) {
            try {
                originalId = Long.valueOf(gameId);
            } catch (NumberFormatException e) {
                throw new CommonException("Original gameId not found for external gameId=" + gameId, e);
            }
        }
        return originalId;
    }

    protected Map<String, String> prepareStatusParams(String extUserId, long transactionId)
            throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(CCommonWallet.PARAM_USERID, extUserId);
        htbl.put(CCommonWallet.PARAM_CASINOTRANSACTIONID, String.valueOf(transactionId));
        return htbl;
    }

    protected Map<String, String> prepareGetBalanceParams(String extUserId) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(CCommonWallet.PARAM_USERID, extUserId);
        return htbl;
    }

    protected Map<String, String> prepareCancelParams(String extUserId, long transactionId)
            throws CommonException {
        Map<String, String> htbl = new HashMap<>();
        htbl.put(CCommonWallet.PARAM_USERID, extUserId);
        htbl.put(CCommonWallet.PARAM_CASINOTRANSACTIONID, String.valueOf(transactionId));
        return htbl;
    }

    protected String getWagerUrl(long bankId) {
        return bankInfo.getCWWagerUrl();
    }

    protected String getStatusUrl(long bankId) {
        return bankInfo.getCWStatusUrl();
    }

    protected String getBalanceUrl(long bankId) {
        return bankInfo.getCWBalanceUrl();
    }

    protected String getCancelUrl(long bankId) {
        return bankInfo.getCWCancelUrl();
    }

    protected boolean isAuthRequired(long bankId) {
        return bankInfo.isRequiredAuthParam();
    }

    protected String getAuthPassword(long bankId) {
        return bankInfo.getAuthPassword();
    }

    protected XmlRequestResult request(Map<String, String> htbl, String url, long bankId) throws CommonException {
        return request(htbl, url, bankId, 0);
    }

    protected XmlRequestResult request(Map<String, String> htbl, String url, long bankId, long timeout)
            throws CommonException {
        try {
            handleCWAuth(htbl, bankId);
            if (bankInfo.isSendBankIdToExtApi()) {
                htbl.put(CCommonWallet.PARAM_BANKID, bankInfo.getExternalBankId());
            }
            String requestParams = printRequestParams(htbl);
            LOG.info("RESTCWClient::request, request to url:{} bankId:{} is:{}", url, bankId, requestParams);
            return doRequest(htbl, url, bankId, timeout);
        } catch (Exception e) {
            LOG.error("RESTCWClient::request error, bankId = {}, url = {}", bankId, url, e);
            throw new CommonException(e);
        }
    }

    protected XmlRequestResult doRequest(Map<String, String> htbl, String url, long bankId, long timeout) throws CommonException {
        logUrl(url);
        logRequest(htbl);
        long now = System.currentTimeMillis();
        HttpClientConnection connection = HttpClientConnection.newInstance(HttpClientCallbackHandler.getInstance(), timeout);
        String sb = connection.doRequest(bankInfo.isUsesJava8Proxy(), url, htbl, isPost(), getSpecialRequestHeadersMap(), isUseHttpProxy());
        logResponse(sb);
        LOG.info("request, response from url:{} bankId:{} is:{} specialRequestHeaders:{} time: {}", url, bankId, sb, specialRequestHeaders,
                System.currentTimeMillis() - now);

        XmlRequestResult result = new XmlRequestResult();
        Parser parser = Parser.instance();
        parser.parse(sb, result);
        return result;
    }

    protected boolean isPost() {
        return true;
    }

    protected boolean isUseHttpProxy() {
        return bankInfo.isUseHttpProxy();
    }

    private void handleCWAuth(Map<String, String> htbl, long bankId) throws CommonException {
        boolean authRequired = isAuthRequired(bankId);

        if (authRequired) {
            LOG.debug("auth required for bankId={}", bankId);
            htbl.put(CCommonWallet.PARAM_APIPASSWORD, getAuthPassword(bankId));
        }
    }

    protected String printRequestParams(Map<String, String> htbl) {
        StringBuilder sb = new StringBuilder(" request parameters:");
        for (Map.Entry<String, String> entry : htbl.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }

    public boolean isStubMode() {
        return bankInfo.isStubMode();
    }

    protected boolean isPostByBankProperty() {
        return bankInfo.isPOST();
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
            StringBuilder sb = new StringBuilder(" request parameters:");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(";");
            }
            loggableContainer.logRequest(sb.toString());
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
