package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.maintenance.CacheExporter;
import com.abs.casino.support.cache.bank.edit.forms.language.UploadForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;
import org.apache.struts.upload.FormFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;

/**
 * User: flsh
 * Date: 27.02.13
 */
public class ImportSubCasinoAction extends Action {
    private static final Logger LOG = LogManager.getLogger(ImportSubCasinoAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UploadForm uploadForm = (UploadForm) form;
        long subCasinoId;
        try {
            FormFile file = uploadForm.getFile();
            final byte[] fileData = file.getFileData();
            ByteArrayInputStream stream = new ByteArrayInputStream(fileData);
            subCasinoId = CacheExporter.getInstance().importSubCasinoFromSingleFile(stream);
        } catch (Exception e) {
            ActionMessages messages = getErrors(request);
            messages.add(BaseAction.MESSAGE_TYPE_ERROR, new ActionMessage("error.common.genericError", e.getMessage()));
            addErrors(request, messages);
            LOG.error("Processing error", e);
            return mapping.findForward(BaseAction.ERROR_FORWARD);
        }

        ActionRedirect redirect = BaseAction.getActionRedirectByHost(request, "/support/subCasino.do");
        redirect.addParameter("subcasinoId", subCasinoId);
        return redirect;
    }
}
