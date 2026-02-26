package com.abs.casino.forms.login.ct;

import com.abs.casino.forms.login.CommonLoginForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class CTLoginForm extends CommonLoginForm {
    private final static Logger LOG = LogManager.getLogger(CTLoginForm.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public String toString() {
        return null;
    }
}
