package com.dgphoenix.casino.support.cache.bank.edit.forms.common;


import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class SubcasinoForm extends ActionForm {

    private String id;
    private String name;
    private String defaultBank;
    private List<LabelValueBean> bankIds;
    private List<String> additionalDomainNames;
    private String domainNameToEdit;
    private String oldDomainNameToEdit;
    private Long exportedBankId;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        String strSubCasinId = BaseAction.extractRequestParameterIgnoreCase(request, "id");

        if (StringUtils.isTrimmedEmpty(strSubCasinId)) {
            strSubCasinId = BaseAction.extractRequestParameterIgnoreCase(request, "subCasinoId");
        }

        if (StringUtils.isTrimmedEmpty(strSubCasinId)) {
            errors.add("subcasinoId", new ActionMessage("error.subcasinoForm.emptyId"));
        }

        Long longId = -1L;
        try {
            longId = Long.parseLong(strSubCasinId);
        } catch (NumberFormatException e) {
            errors.add("subcasinoId", new ActionMessage("error.subcasinoForm.incorrectId", "ID", strSubCasinId));
        }

        if (SubCasinoCache.getInstance().get(longId) == null) {
            errors.add("subcasinoId", new ActionMessage("error.subcasinoForm.noSuchSubcasino", "ID", strSubCasinId));
        }

        return errors;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultBank() {
        return defaultBank;
    }

    public void setDefaultBank(String defaultBank) {
        this.defaultBank = defaultBank;
    }

    public List<LabelValueBean> getBankIds() {
        return bankIds;
    }

    public void setBankIds(List<LabelValueBean> bankIds) {
        this.bankIds = bankIds;
    }

    public List<String> getAdditionalDomainNames() {
        return additionalDomainNames;
    }

    public void setAdditionalDomainNames(List<String> additionalDomainNames) {
        this.additionalDomainNames = additionalDomainNames;
    }

    public String getDomainNameToEdit() {
        return domainNameToEdit;
    }

    public void setDomainNameToEdit(String domainNameToEdit) {
        this.domainNameToEdit = domainNameToEdit;
    }

    public String getOldDomainNameToEdit() {
        return oldDomainNameToEdit;
    }

    public void setOldDomainNameToEdit(String oldDomainNameToEdit) {
        this.oldDomainNameToEdit = oldDomainNameToEdit;
    }

    public Long getExportedBankId() {
        return exportedBankId;
    }

    public void setExportedBankId(Long exportedBankId) {
        this.exportedBankId = exportedBankId;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SubcasinoForm");
        sb.append("[id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", defaultBank='").append(defaultBank).append('\'');
        sb.append(", bankIds=").append(bankIds);
        sb.append(", additionalDomainNames=").append(additionalDomainNames);
        sb.append(", domainNameToEdit='").append(domainNameToEdit).append('\'');
        sb.append(", oldDomainNameToEdit='").append(oldDomainNameToEdit).append('\'');
        sb.append(", exportedBankId=").append(exportedBankId);
        sb.append(']');
        return sb.toString();
    }
}
