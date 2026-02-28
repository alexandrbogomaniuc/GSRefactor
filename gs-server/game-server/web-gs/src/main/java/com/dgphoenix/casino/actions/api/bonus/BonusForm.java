package com.abs.casino.actions.api.bonus;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.bonus.BonusErrors;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class BonusForm extends CommonActionForm {
    private static final Logger LOG = LogManager.getLogger(BonusForm.class);
    public static final String FORMAT = "format";

    private String hash;
    private String extBankId;
    private boolean isSendBonusAwardTime;
    private boolean isSendDetailsOnFrbInfo;
    private boolean json;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getExtBankId() {
        return extBankId;
    }

    public void setExtBankId(String extBankId) {
        this.extBankId = extBankId;
    }

    public boolean isSendBonusAwardTime() {
        return isSendBonusAwardTime;
    }

    public void setSendBonusAwardTime(boolean sendBonusAwardTime) {
        isSendBonusAwardTime = sendBonusAwardTime;
    }

    public boolean isSendDetailsOnFrbInfo() {
        return isSendDetailsOnFrbInfo;
    }

    public void setSendDetailsOnFrbInfo(boolean sendDetailsOnFrbInfo) {
        isSendDetailsOnFrbInfo = sendDetailsOnFrbInfo;
    }

    public boolean isJson() {
        return json;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();

        this.json = "json".equalsIgnoreCase(BaseAction.extractRequestParameterIgnoreCase(request, FORMAT));

        short subCasinoId;
        try {
            subCasinoId = extractSubCasinoId(request);
        } catch (Exception e) {
            getLogger().error("validate error:", e);
            actionErrors.add("valid_error", new ActionMessage(BonusErrors.INVALID_PARAMETERS.getDescription(), false));
            actionErrors.add("valid_error_code", new ActionMessage(String.valueOf(BonusErrors.INVALID_PARAMETERS.getCode()), false));
            actionErrors.add("valid_error_comm", new ActionMessage("error.login.incorrectSubCasino"));
            return actionErrors;
        }
        setSubCasinoId(subCasinoId);

        BankInfo bankInfo = extractBankInfo(request);
        if (bankInfo == null) {
            getLogger().debug("bankInfo == null");
            actionErrors.add("valid_error", new ActionMessage(BonusErrors.INVALID_PARAMETERS.getDescription(), false));
            actionErrors.add("valid_error_code", new ActionMessage(String.valueOf(BonusErrors.INVALID_PARAMETERS.getCode()), false));
            actionErrors.add("valid_error_comm", new ActionMessage("error.login.incorrectBank"));
            return actionErrors;
        }

        long bankId = bankInfo.getId();
        if (!SubCasinoCache.getInstance().isExist(subCasinoId, bankId)) {
            getLogger().debug("bank=" + bankId + " not found in subcasino=" + subCasinoId);
            actionErrors.add("valid_error", new ActionMessage(BonusErrors.INVALID_PARAMETERS.getDescription(), false));
            actionErrors.add("valid_error_code", new ActionMessage(String.valueOf(BonusErrors.INVALID_PARAMETERS.getCode()), false));
            actionErrors.add("valid_error_comm", new ActionMessage("error.login.incorrectSubCasino"));
            return actionErrors;
        }

        setBankInfo(bankInfo);
        setBankId((int) bankId);

        return actionErrors;
    }

    public BankInfo extractBankInfo(HttpServletRequest request) {
        String bankIdParam = BaseAction.extractRequestParameterIgnoreCase(request, getBankIdParamName());
        extBankId = bankIdParam;

        if (StringUtils.isTrimmedEmpty(bankIdParam)) {
            return getDefaultBank();
        } else {
            BankInfo bank = getBankByExternalId(bankIdParam);
            if (bank == null) {
                try {
                    bank = getBankByInternalId(Long.valueOf(bankIdParam));
                } catch (NumberFormatException ignored) {
                    getLogger().debug("bankId param is not numeric: " + bankIdParam);
                }
            }
            return bank;
        }
    }

    private BankInfo getBankByInternalId(Long bankId) {
        getLogger().debug("Bank not found by externalId, try to find by internalId: " + bankId);
        return BankInfoCache.getInstance().getBankInfo(bankId);
    }

    private BankInfo getBankByExternalId(String bankId) {
        getLogger().info("extBankId " + bankId);
        return BankInfoCache.getInstance().getBank(bankId, subCasinoId);
    }

    private BankInfo getDefaultBank() {
        Long defaultBankId = SubCasinoCache.getInstance().getDefaultBankId(subCasinoId);
        getLogger().info("Use default bank, id=" + defaultBankId + ", for subcasino = " + subCasinoId);
        if (defaultBankId == null) {
            getLogger().debug("Default bankId is null for subcasino=" + subCasinoId);
            return null;
        } else {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(defaultBankId);
            getLogger().debug("Use default bank with id=" + bankInfo.getId() + " for subcasino=" + subCasinoId);
            return bankInfo;
        }
    }

    public short extractSubCasinoId(ServletRequest request) {
        String strSubCasinoId = request.getParameter("subCasinoId");
        short subCasinoId = strSubCasinoId == null
                ? (short) SubCasinoCache.getInstance().getSubCasinoByDomainName(request.getServerName()).getId()
                : Short.parseShort(strSubCasinoId);
        getLogger().info("subCasinoId " + subCasinoId);
        return subCasinoId;
    }

    @Override
    public String toString() {
        return "BonusForm" + "[ " + super.toString() + "    " + "hash='" + hash + '\'' + ']';
    }
}
