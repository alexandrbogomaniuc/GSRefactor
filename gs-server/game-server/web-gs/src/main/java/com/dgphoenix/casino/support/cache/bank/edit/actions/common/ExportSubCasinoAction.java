package com.abs.casino.support.cache.bank.edit.actions.common;

import com.abs.casino.gs.maintenance.CacheExporter;
import com.abs.casino.support.cache.bank.edit.forms.common.SubcasinoForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: flsh
 * Date: 25.02.13
 */
public class ExportSubCasinoAction extends Action {
    private static final Logger LOG = LogManager.getLogger(ExportSubCasinoAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SubcasinoForm subcasinoForm = (SubcasinoForm) form;
        LOG.debug("export: " + subcasinoForm);
        Long subCasinoId = Long.parseLong(subcasinoForm.getId());
        Long bankId = subcasinoForm.getExportedBankId();
        if (bankId <= 0) {
            bankId = null;
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=export_" + subCasinoId +
                (bankId == null ? "" : "_" + bankId) + ".xml");
        try {
            ServletOutputStream out = response.getOutputStream();
            CacheExporter.getInstance().exportSubCasinoToSingleFile(out, subCasinoId, bankId == null ? null :
                    String.valueOf(bankId));
        } catch (Exception e) {
            LOG.error("Export error", e);
        }
        return null;
    }
}
