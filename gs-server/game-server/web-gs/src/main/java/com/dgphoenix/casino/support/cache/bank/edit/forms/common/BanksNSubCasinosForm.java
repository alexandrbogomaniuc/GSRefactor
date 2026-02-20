package com.dgphoenix.casino.support.cache.bank.edit.forms.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.util.LabelValueBean;

import java.util.List;

/**
 * User: vik
 * Date: 17.01.13
 */
public class BanksNSubCasinosForm extends ActionForm {

    private long subCasinoId;
    private long bankId;
    private List<LabelValueBean> subCasinoList;
    private List<LabelValueBean> bankIdList;

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

    public List<LabelValueBean> getSubCasinoList() {
        return subCasinoList;
    }

    public void setSubCasinoList(List<LabelValueBean> subCasinoList) {
        this.subCasinoList = subCasinoList;
    }

    public List<LabelValueBean> getBankIdList() {
        return bankIdList;
    }

    public void setBankIdList(List<LabelValueBean> bankIdList) {
        this.bankIdList = bankIdList;
    }
}
