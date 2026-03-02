package com.abs.casino.gs.api.service;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.exception.BonusException;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.gs.api.service.xml.FundAccount;
import com.abs.casino.gs.api.service.xml.GetActiveToken;
import com.abs.casino.gs.api.service.xml.GetEnvironment;
import com.abs.casino.gs.managers.payment.wallet.ILoggableCWClient;
import com.abs.casino.gs.managers.payment.wallet.ILoggableContainer;
import com.abs.casino.gs.singlegames.tools.util.StringUtils;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.EMPTY;

public class RESTServiceClient implements ILoggableCWClient {
    private static final Logger LOG = LogManager.getLogger(RESTServiceClient.class);
    private static final RESTServiceClient instance = new RESTServiceClient();

    public static RESTServiceClient getInstance() {
        return instance;
    }

    private Map<String, String> specialRequestHeadersMap;
    private ILoggableContainer loggableContainer;

    private final static String
            PARAM_BANKID = "bankId",
            PARAM_USERID = "userId",
            PARAM_AMOUNT = "amount",
            PARAM_HASH = "hash";

    public RESTServiceClient() {
    }

    protected Map<String, String> getSpecialRequestHeadersMap(BankInfo bankInfo) {
        String specialRequestHeaders = bankInfo.getCWSpecialRequestHeaders();
        if (!com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(specialRequestHeaders)) {
            try {
                specialRequestHeadersMap = CollectionUtils.stringToMap(specialRequestHeaders);
            } catch (Exception e) {
                LOG.error("Cannot parse CW_SPECIAL_REQUEST_HEADERS: '" + specialRequestHeaders +
                        "' , bankId=" + bankInfo.getId(), e);
            }
        }
        return specialRequestHeadersMap;
    }

    protected Map<String, String> prepareGetEnvironmentParams(BankInfo bankInfo) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(PARAM_BANKID, bankInfo.getExternalBankId());

        List<String> paramList = new ArrayList<>();
        paramList.add(bankInfo.getExternalBankId());

        htbl.put(PARAM_HASH, getHashValue(paramList, bankInfo));
        return htbl;
    }

    protected Map<String, String> prepareFundAccountParams(String userId, long amount,
                                                           BankInfo bankInfo) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(PARAM_BANKID, bankInfo.getExternalBankId());
        htbl.put(PARAM_USERID, userId);
        htbl.put(PARAM_AMOUNT, String.valueOf(amount));

        List<String> paramList = new ArrayList<>();
        paramList.add(bankInfo.getExternalBankId());
        paramList.add(userId);
        paramList.add(String.valueOf(amount));

        htbl.put(PARAM_HASH, getHashValue(paramList, bankInfo));
        return htbl;
    }

    protected Map<String, String> prepareGetTokenParams(String userId,
                                                        BankInfo bankInfo) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(PARAM_BANKID, bankInfo.getExternalBankId());
        htbl.put(PARAM_USERID, userId);

        List<String> paramList = new ArrayList<>();
        paramList.add(bankInfo.getExternalBankId());
        paramList.add(userId);

        htbl.put(PARAM_HASH, getHashValue(paramList, bankInfo));
        return htbl;
    }

    public GetEnvironment getEnvironment(long bankId) throws CommonException {

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

        Map<String, String> htbl = prepareGetEnvironmentParams(bankInfo);

        XStream parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.abs.casino.gs.api.service.xml.**"});
        parser.autodetectAnnotations(true);
        parser.processAnnotations(GetEnvironment.class);
        GetEnvironment response = (GetEnvironment) parser.fromXML(
                request(htbl, bankInfo.getAPIServiceEnvironmentUrl(), bankInfo));

        if (!response.getResponse().isSuccess()) {
            String code = response.getResponse().getCode();
            if (code != null) {
                throw new CommonException("Get Environment response was not successful, code: " + code);
            } else {
                throw new CommonException("Get Environment response was not successful");
            }
        }
        return response;
    }

    public FundAccount fundAccount(long bankId, String userId, long amount) throws CommonException {

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

        Map<String, String> htbl = prepareFundAccountParams(userId, amount, bankInfo);

        XStream parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.abs.casino.gs.api.service.xml.**"});
        parser.autodetectAnnotations(true);
        parser.processAnnotations(FundAccount.class);
        FundAccount response = (FundAccount) parser.fromXML(
                request(htbl, bankInfo.getAPIServiceFundAccountUrl(), bankInfo));

        if (!response.getResponse().isSuccess()) {
            String code = response.getResponse().getCode();
            if (code != null) {
                throw new CommonException("Fund Account response was not successful, code: " + code);
            } else {
                throw new CommonException("Fund Account response was not successful");
            }
        }
        return response;
    }

    public GetActiveToken getActiveToken(long bankId, String userId) throws CommonException {

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

        Map<String, String> htbl = prepareGetTokenParams(userId, bankInfo);

        XStream parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.abs.casino.gs.api.service.xml.**"});
        parser.autodetectAnnotations(true);
        parser.processAnnotations(GetActiveToken.class);
        GetActiveToken response = (GetActiveToken) parser.fromXML(
                request(htbl, bankInfo.getAPIServiceActiveTokenUrl(), bankInfo));

        if (!response.getResponse().isSuccess()) {
            String code = response.getResponse().getCode();
            if (code != null) {
                throw new CommonException("Get Active Token response was not successful, code: " + code);
            } else {
                throw new CommonException("Get Active Token response was not successful");
            }
        }
        return response;
    }

    protected String getHashValue(List params, BankInfo bankInfo) throws BonusException {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object param : params) {
                sb.append(param);
            }
            sb.append(getBonusPass(bankInfo));

            return StringUtils.getMD5(sb.toString());
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    private String request(Map<String, String> htbl, String url, BankInfo bankInfo) throws CommonException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("request, request to url:" + url + " bankId:" + bankInfo.getId() +
                        " isPost: " + isPost(bankInfo) + ", " + " is:" + printRequestParams(htbl));
            }

            logRequest(htbl);
            String sb = HttpClientConnection.newInstance().doRequest(
                    bankInfo.isUsesJava8Proxy(), url, htbl, isPost(bankInfo),
                    getSpecialRequestHeadersMap(bankInfo), isUseHttpProxy(bankInfo));
            logResponse(sb);

            if (LOG.isDebugEnabled()) {
                LOG.debug("request, response from url:" + url + " bankId:" + bankInfo.getId() + " is:" + sb +
                        (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(
                                bankInfo.getCWSpecialRequestHeaders()) ?
                                "" : " specialRequestHeaders:" + bankInfo.getCWSpecialRequestHeaders()));

            }
            return sb;
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

    protected boolean isPost(BankInfo bankInfo) {
        return bankInfo.isPOST();
    }

    protected boolean isUseHttpProxy(BankInfo bankInfo) {
        return bankInfo.isUseHttpProxy();
    }

    protected String getBonusPass(BankInfo bankInfo) {
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
