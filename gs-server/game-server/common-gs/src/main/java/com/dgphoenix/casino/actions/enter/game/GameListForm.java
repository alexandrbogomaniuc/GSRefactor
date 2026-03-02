package com.abs.casino.actions.enter.game;


import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.util.string.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

import static com.abs.casino.common.util.string.StringUtils.getMD5;

public class GameListForm extends ActionForm {
    private final static Logger LOG = Logger.getLogger(GameListForm.class);
    private static final long serialVersionUID = -6407067073506717289L;

    private String bankId;
    private String version;
    private String test;
    private String hash;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        Long bankId;
        try {
            bankId = Long.valueOf(request.getParameter("bankId"));
        } catch (Exception e) {
            LOG.error("Validation error", e);
            errors.add("gameListForm", new ActionMessage("error.login.incorrectBank"));
            return errors;
        }

        BankInfo bankInfo;
        try {
            bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                throw new Exception("Bank info is null");
            }
        } catch (Exception e) {
            LOG.error("No such bankInfo for id: " + bankId, e);
            errors.add("gameListForm", new ActionMessage("error.login.incorrectBank"));
            return errors;
        }

        String bankFeedsPasskey = bankInfo.getBankFeedsPasskey();
        if (StringUtils.isTrimmedEmpty(bankFeedsPasskey)) {
            LOG.info("No need to validate bank: " + bankId);
            return errors;
        }

        String requestHash = request.getParameter("hash");

        if (StringUtils.isTrimmedEmpty(requestHash)) {
            LOG.error("No hash in request");
            errors.add("gameListForm", new ActionMessage("error.secure.invalidHash"));
            return errors;
        }

        try {
            hash = getMD5(bankId + bankFeedsPasskey);
            if (!hash.equals(requestHash)) {
                throw new Exception("Invalid hash");
            }
        } catch (Exception e) {
            LOG.error("Hash validation error", e);
            errors.add("gameListForm", new ActionMessage("error.secure.invalidHash"));
            return errors;
        }

        return errors;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
