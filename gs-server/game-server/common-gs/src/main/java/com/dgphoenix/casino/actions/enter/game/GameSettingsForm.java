package com.abs.casino.actions.enter.game;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: shegan
 * Date: 19.03.15
 */
public class GameSettingsForm extends GameListForm {
    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors(super.validate(mapping, request));
        if (!errors.isEmpty()) {
            return errors;
        }

        String s = request.getParameter("bankId");
        long bankId = Long.parseLong(s);

        SubCasino subCasino = SubCasinoCache.getInstance().getSubCasinoByDomainName(request.getServerName());

        if (subCasino == null) {
            errors.add("gameSettingsForm", new ActionMessage("error.secure.invalidSubCasino"));
            return errors;
        }

        if (!subCasino.getBankIds().contains(bankId)) {
            errors.add("gameSettingsForm", new ActionMessage("error.secure.bankNotFoundInSubCasino"));
            return errors;
        }

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            errors.add("gameSettingsForm", new ActionMessage("error.secure.bankNotExist"));
            return errors;
        }

        return errors;
    }
}
