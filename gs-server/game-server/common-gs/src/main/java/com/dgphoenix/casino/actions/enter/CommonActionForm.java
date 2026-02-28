package com.dgphoenix.casino.actions.enter;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.ClientTypeFactory;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

public abstract class CommonActionForm extends ActionForm {
    private final static String VALID_NAME = "valid_error";
    protected Integer bankId;
    protected short subCasinoId;
    protected BankInfo bankInfo;
    protected ClientType clientType;
    private String host;

    public CommonActionForm() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(BankInfo bankInfo) {
        this.bankInfo = bankInfo;
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public short getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(short subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    abstract protected Logger getLogger();

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        String incorrectSubCasino = "error.login.incorrectSubCasino";
        ActionErrors actionErrors = new ActionErrors();
        this.host = request.getRemoteHost();
        String serverName = request.getServerName();
        //getLogger().warn("serverName=" + serverName + ", request=" + request + ", host=" + request.getHeader("Host"));

        try {
            String strSubCasinoId = request.getParameter("subCasinoId");
            if (!StringUtils.isTrimmedEmpty(strSubCasinoId)) {
                subCasinoId = Short.parseShort(strSubCasinoId);
            } else {
                SubCasino subCasino = SubCasinoCache.getInstance().getSubCasinoByDomainName(serverName);
                if (subCasino == null) {
                    actionErrors.add(VALID_NAME, new ActionMessage(incorrectSubCasino));
                    return actionErrors;
                }
                subCasinoId = (short) subCasino.getId();
            }
        } catch (Exception e) {
            ThreadLog.error("CommonActionForm::validate error:", e);
            actionErrors.add(VALID_NAME, new ActionMessage(incorrectSubCasino));
            return actionErrors;
        }

        String extBankId = BaseAction.extractRequestParameterIgnoreCase(request, getBankIdParamName());
        if (!StringUtils.isTrimmedEmpty(extBankId)) {
            bankInfo = BankInfoCache.getInstance().getBank(extBankId, subCasinoId);
        } else {
            bankInfo = BankInfoCache.getInstance().getBankInfoByDomainName(subCasinoId, serverName);
            if (bankInfo == null) {
                Long bankId = SubCasinoCache.getInstance().getDefaultBankId(subCasinoId);
                if (bankId == null) {
                    actionErrors.add(VALID_NAME, new ActionMessage("error.login.incorrectBank"));
                    return actionErrors;
                }
                bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            }
        }
        if (bankInfo == null) {
            ThreadLog.error("bankInfo is null, id=" + bankId);
            actionErrors.add(VALID_NAME, new ActionMessage("error.login.incorrectBank"));
            return actionErrors;
        }
        this.bankId = (int) bankInfo.getId();

        if (!SubCasinoCache.getInstance().isExist(subCasinoId, bankId)) {
            ThreadLog.error("subCasinoId is not found, id=" + subCasinoId);
            actionErrors.add(VALID_NAME, new ActionMessage(incorrectSubCasino));
            return actionErrors;
        }

        clientType = ClientTypeFactory.getByHttpRequest(request);

        if (isLaunchChecked()) {
            checkLaunchPermission(request, actionErrors);
        }

        return actionErrors;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    protected String getBankIdParamName() {
        return "bankId";
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        bankId = null;
        subCasinoId = 0;
        host = null;
        clientType = null;
    }

    protected boolean isLaunchChecked() {
        return false;
    }

    protected void checkLaunchPermission(HttpServletRequest request, ActionErrors errors) {
        long start = System.currentTimeMillis();

        String requestIp = request.getRemoteAddr();
        if (GameServerConfiguration.getInstance().isIpTrusted(requestIp)) {
            getLogger().debug("CommonActionForm::checkLaunchPermission ip " + requestIp + " is trusted");
            StatisticsManager.getInstance().updateRequestStatistics("checkLaunchPermission::ip trusted", System.currentTimeMillis() - start);
            return;
        }

        String referer = request.getHeader("Referer");
        String scheme = request.getScheme();
        String requestDomain = getDomainFromReferer(scheme, referer);

        if (GameServerConfiguration.getInstance().isCountryTrusted(requestIp)) {
            if (bankInfo.isDomainAllowed(requestDomain)) {
                getLogger().debug("CommonActionForm::checkLaunchPermission domain " + requestDomain + " is trusted for bankId: "
                        + bankInfo.getId());
                StatisticsManager.getInstance().updateRequestStatistics("checkLaunchPermission::domain trusted", System.currentTimeMillis() - start);
                return;
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("checkLaunchPermission::launch denied", System.currentTimeMillis() - start);
        getLogger().debug("CommonActionForm::checkLaunchPermission validation fail: access for 'ip " +
                requestIp + ", referer " + requestDomain + "' denied for bankId: " + bankInfo.getId());
        errors.add(VALID_NAME, new ActionMessage("error.startgame.launchDenied"));
    }

    public static String getDomainFromReferer(String scheme, String referer) {
        if (StringUtils.isTrimmedEmpty(referer)) {
            return null;
        }
        return org.apache.commons.lang.StringUtils.substringBetween(referer, scheme + "://", "/");
    }
}
