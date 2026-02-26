package com.abs.casino.forms.game.ct;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 10.10.14
 */
public class CTOnlyStartGameForm extends CommonCTStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CTOnlyStartGameForm.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        setCheckToken(false);

        ActionErrors errors = super.validate(mapping, request);

        if (!errors.isEmpty()) {
            return errors;
        }

        String sessionId = BaseAction.extractRequestParameterIgnoreCase(request, "sessionId");
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            errors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
            return errors;
        }
        this.token = sessionId;
        Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);

/*
        SessionInfo sessionInfo = CassandraPlayerSessionPersister.getInstance().get(sessionId);
        if (sessionInfo == null) {
            errors.add("empty_credentials", new ActionMessage("error.login.loginRequired"));
            return errors;
        }

        AccountInfo account = AccountManager.getInstance().getAccountInfo(sessionInfo.getAccountId());
        setBankId(account.getBankId());
*/
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(pair.getKey());
        setBankInfo(bankInfo);
        setBankId((int) bankInfo.getId());

        return errors;
    }
}
