package com.abs.casino.forms.game.ct;

import com.abs.casino.forms.game.CommonStartGameForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * User: isirbis
 * Date: 10.10.14
 */
public abstract class CommonCTStartGameForm extends CommonStartGameForm {
    private Long balance;

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        this.balance = 0l;
    }
}
