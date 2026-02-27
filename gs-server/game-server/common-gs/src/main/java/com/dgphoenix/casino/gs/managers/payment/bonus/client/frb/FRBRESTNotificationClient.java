package com.dgphoenix.casino.gs.managers.payment.bonus.client.frb;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraFrBonusArchivePersister;
import com.dgphoenix.casino.cassandra.persist.CassandraFrBonusPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.util.xml.IXmlRequestResult;
import com.abs.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.Parser;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableCWClient;
import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.EMPTY;

public class FRBRESTNotificationClient implements ILoggableCWClient {
    private static final Logger LOG = LogManager.getLogger(FRBRESTNotificationClient.class);
    private static final FRBRESTNotificationClient instance = new FRBRESTNotificationClient();

    public static FRBRESTNotificationClient getInstance() {
        return instance;
    }

    private Map<String, String> specialRequestHeadersMap;
    private ILoggableContainer loggableContainer;

    public FRBRESTNotificationClient() {
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

    protected Map<String, String> prepareBonusNotificationParams(String userId, long bonusId, String extBonusId,
                                                                 long amount, long transactionId, BonusStatus bonusStatus,
                                                                 BankInfo bankInfo) throws CommonException {
        HashMap<String, String> htbl = new HashMap<>();
        htbl.put(CBonus.PARAM_USERID, userId);
        htbl.put(CBonus.PARAM_BONUSID, String.valueOf(bonusId));
        htbl.put(CBonus.PARAM_AMOUNT, String.valueOf(amount));
        htbl.put(CBonus.PARAM_TRANSACTIONID, String.valueOf(transactionId));
        if (bankInfo.isSendExtBonusId()) {
            String finalExtBonusId = extBonusId;
            if (StringUtils.isTrimmedEmpty(finalExtBonusId)) {
                CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                        .getBean("persistenceManager", CassandraPersistenceManager.class);
                CassandraFrBonusPersister frbPersister = persistenceManager.getPersister(CassandraFrBonusPersister.class);
                FRBonus frBonus = frbPersister.get(bonusId);
                if (frBonus == null) {
                    CassandraFrBonusArchivePersister archivePersister =
                            persistenceManager.getPersister(CassandraFrBonusArchivePersister.class);
                    frBonus = archivePersister.get(bonusId);
                }
                finalExtBonusId = (frBonus != null && !StringUtils.isTrimmedEmpty(frBonus.getExtId())) ?
                        frBonus.getExtId() : "unknown";
            }
            htbl.put(CBonus.PARAM_EXTBONUSID, finalExtBonusId);
        }
        if (bankInfo.getSubCasinoId() == 175 || bankInfo.getSubCasinoId() == 282) { // Guts & CARDINS customization
            htbl.put(CBonus.PARAM_GAMEID, "0");
        }
        htbl.put(CBonus.STATUS.toLowerCase(), bonusStatus.name());

        List<String> paramList = new ArrayList<>();
        paramList.add(userId);
        paramList.add(String.valueOf(bonusId));
        paramList.add(String.valueOf(amount));

        htbl.put(CBonus.PARAM_HASH, getHashValue(paramList, bankInfo));
        return htbl;
    }

    public void notify(FRBonusNotification notification) throws CommonException {

        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(notification.getAccountId());
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());

        Map<String, String> htbl = prepareBonusNotificationParams(accountInfo.getExternalId(),
                notification.getBonusId(), notification.getExtBonusId(), notification.getWinSum(),
                notification.getId(), notification.getBonusStatus(), bankInfo);
        IXmlRequestResult output = request(htbl, bankInfo);

        if (!output.isSuccessful()) {
            Object code = output.getResponseParameters().get("CODE");
            if (code != null) {
                throw new FRBException("FRB notify response was not successful", (String) code);
            } else {
                throw new CommonException("FRB notify response was not successful");
            }
        }
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

    protected XmlRequestResult request(Map<String, String> htbl, BankInfo bankInfo) throws CommonException {
        try {
            String url = getFRBonusNotificationURL(bankInfo);
            if (LOG.isDebugEnabled()) {
                LOG.debug("request, request to url:" + url + " bankId:" + bankInfo.getId() +
                        " isPost: " + isPost(bankInfo) + ", " + " is:" + printRequestParams(htbl));
            }

            logUrl(url);
            logRequest(htbl);
            String sb = HttpClientConnection.newInstance().doRequest(bankInfo.isUsesJava8Proxy(), url, htbl, isPost(bankInfo),
                    getSpecialRequestHeadersMap(bankInfo), isUseHttpProxy(bankInfo));
            logResponse(sb);

            if (LOG.isDebugEnabled()) {
                LOG.debug("request, response from url:" + url + " bankId:" + bankInfo.getId() + " is:" + sb +
                        (com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty(bankInfo.getCWSpecialRequestHeaders()) ?
                                "" : " specialRequestHeaders:" + bankInfo.getCWSpecialRequestHeaders()));

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

    private String getFRBonusNotificationURL(BankInfo bankInfo) throws CommonException {
        return bankInfo.getFRBonusWinURL();
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
