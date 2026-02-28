package com.abs.casino.support.cache.bank.edit.actions.common;


import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.support.cache.bank.edit.actions.common.AbstractCRUDAction;
import com.abs.casino.support.cache.bank.edit.forms.common.SubcasinoForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * User: vik
 * Date: 21.12.12
 */
public class DomainNameAction extends AbstractCRUDAction<SubcasinoForm> {
    private static final Logger LOG = LogManager.getLogger(DomainNameAction.class);
    public static final String ADD_DOMAIN_NAME = "Add Domain Name";
    public static final String EDIT_DOMAIN_NAME = "Edit Domain Name";
    public static final String REMOVE_DOMAIN_NAME = "Remove Domain Name";

    private final RemoteCallHelper remoteCallHelper;

    public DomainNameAction() {
        remoteCallHelper = ApplicationContextHelper.getBean(RemoteCallHelper.class);
    }

    @Override
    public void create(SubcasinoForm form) throws Exception {
        SubCasino subCasino = getSubcasino(form);
        String newDomainName = form.getDomainNameToEdit();
        if (!StringUtils.isTrimmedEmpty(newDomainName)) {
            subCasino.addDomainName(newDomainName);
            remoteCallHelper.saveAndSendNotification(subCasino);
        }
    }

    @Override
    public void edit(SubcasinoForm form) throws Exception {
        String newDomainName = form.getDomainNameToEdit();
        LOG.info("newDomainName={}", newDomainName);
        if (StringUtils.isTrimmedEmpty(newDomainName)) {
            return;
        }
        SubCasino subCasino = getSubcasino(form);
        String oldDomainName = form.getOldDomainNameToEdit();
        LOG.info("oldDomainName={}", oldDomainName);
        if (StringUtils.isTrimmedEmpty(oldDomainName) || !subCasino.getDomainNames().contains(oldDomainName)) {
            return;
        }
        subCasino.removeDomainName(oldDomainName);
        subCasino.addDomainName(newDomainName);
        remoteCallHelper.saveAndSendNotification(subCasino);
    }

    @Override
    public void remove(SubcasinoForm form) throws Exception {
        SubCasino subCasino = getSubcasino(form);
        String oldDomainName = form.getOldDomainNameToEdit();
        if (StringUtils.isTrimmedEmpty(oldDomainName) || !subCasino.getDomainNames().contains(oldDomainName)) {
            return;
        }
        subCasino.removeDomainName(oldDomainName);
        remoteCallHelper.saveAndSendNotification(subCasino);
    }

    private SubCasino getSubcasino(SubcasinoForm form) throws Exception {
        SubCasino subCasino = SubCasinoCache.getInstance().get(Long.parseLong(form.getId()));
        if (subCasino != null) {
            return subCasino;
        } else {
            LOG.error("Subcasino is null");
            throw new Exception("Subcasino is null");
        }
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward forward = super.execute(mapping, form, request, response);
        if (mapping.findForward("success") == forward) {
            return BaseAction.getActionRedirectByHost(request, "/support/subCasino.do?subcasinoId=" + ((SubcasinoForm) form).getId());
        } else {
            return forward;
        }
    }

    @Override
    public String getCreateCommand(HttpServletRequest request) {
        return ADD_DOMAIN_NAME;
    }

    @Override
    public String getEditCommand(HttpServletRequest request) {
        return EDIT_DOMAIN_NAME;
    }

    @Override
    public String getRemoveCommand(HttpServletRequest request) {
        return REMOVE_DOMAIN_NAME;
    }


}
