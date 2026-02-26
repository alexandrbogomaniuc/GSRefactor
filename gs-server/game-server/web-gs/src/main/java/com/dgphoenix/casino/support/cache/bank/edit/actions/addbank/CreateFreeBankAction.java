package com.abs.casino.support.cache.bank.edit.actions.addbank;

/**
 * User: vik
 * Date: 15.01.13
 */
public class CreateFreeBankAction extends CreateBankAction {

    @Override
    public String getRedirectUrl() {
        return "/support/cache/bank/common/subcasinoSelect.jsp";
    }

    @Override
    public void putBankToSubCasinoCache() {
    }

}
