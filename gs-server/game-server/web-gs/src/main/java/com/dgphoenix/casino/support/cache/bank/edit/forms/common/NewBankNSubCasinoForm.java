package com.abs.casino.support.cache.bank.edit.forms.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.util.LabelValueBean;

import java.util.List;

/**
 * User: vik
 * Date: 16.01.13
 */
public class NewBankNSubCasinoForm extends ActionForm {

    private long subCasinoId;
    private long bankId;
    private String domainName;
    private List<LabelValueBean> bankIds;

    public long getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(long subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public List<LabelValueBean> getBankIds() {
        return bankIds;
    }

    public void setBankIds(List<LabelValueBean> bankIds) {
        this.bankIds = bankIds;
    }
}
