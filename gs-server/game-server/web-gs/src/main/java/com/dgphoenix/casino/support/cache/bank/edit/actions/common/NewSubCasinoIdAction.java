package com.abs.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.abs.casino.support.cache.bank.edit.forms.common.NewBankNSubCasinoForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: vik
 * Date: 10.01.13
 */
public class NewSubCasinoIdAction extends Action {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        NewBankNSubCasinoForm bankNSubCasinoForm = (NewBankNSubCasinoForm) form;
        long subcasinoId = bankNSubCasinoForm.getSubCasinoId();
        SubCasino subCasino = SubCasinoCache.getInstance().get(subcasinoId);
        if (subCasino != null) {
            throw new Exception("SubCasino already exist, id=" + subcasinoId);
        }
        logger.info("subcasinoid=" + subcasinoId);

        long bankId = bankNSubCasinoForm.getBankId();
        for (Map.Entry<Long, SubCasino> entry : SubCasinoCache.getInstance().getAllObjects().entrySet()) {
            for (long bId : entry.getValue().getBankIds()) {
                if (bId == bankId) {
                    throw new Exception("BankId already exist, id=" + bId);
                }
            }
        }
        subCasino = new SubCasino(subcasinoId);
        List<Long> bankIds = new ArrayList<Long>();
        bankIds.add(bankId);
        String domainName = bankNSubCasinoForm.getDomainName();
        if (StringUtils.isTrimmedEmpty(domainName)) {
            throw new Exception("Please, specify domain name");
        }
        List<String> additional = new ArrayList<String>();
        additional.add(domainName);
        subCasino.setDomainNames(additional);
        subCasino.setBankIds(bankIds);
        SubCasinoCache.getInstance().put(subCasino);

        ActionRedirect redirect = BaseAction.getActionRedirectByHost(request, mapping.findForward("success").getPath());
        redirect.addParameter("subcasinoId", String.valueOf(subcasinoId));

        return redirect;
    }
}
