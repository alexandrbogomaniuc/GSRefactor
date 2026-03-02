package com.abs.casino.actions.api.mq;

import com.abs.casino.actions.enter.CommonActionForm;
import com.abs.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

public class GetLeaderboardsForm extends CommonActionForm {
    private static final Logger LOG = LogManager.getLogger(GetLeaderboardsForm.class);
    private static final String SEPARATOR = "|";

    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);
        if (!actionErrors.isEmpty()) {
            return actionErrors;
        }

        hash = request.getParameter("hash");
        if (!getExpectedHash().equals(hash)) {
            getLogger().error("Invalid hash");
            actionErrors.add("valid_error", new ActionMessage("error.secure.invalidHash"));
            return actionErrors;
        }

        return actionErrors;
    }

    private String getExpectedHash() {
        return DigestUtils.sha256Hex(bankInfo.getExternalBankId() + SEPARATOR + getSecretKey());
    }

    private String getSecretKey() {
        return WalletProtocolFactory.getInstance().isWalletBank(bankId)
                ? bankInfo.getAuthPassword()
                : bankInfo.getCTPassKey();
    }
}
