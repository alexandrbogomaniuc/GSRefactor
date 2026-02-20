package com.dgphoenix.casino.helpers.login.serializers;

import com.dgphoenix.casino.forms.game.CommonStartGameForm;
import com.dgphoenix.casino.forms.login.cw.CWLoginForm;
import com.dgphoenix.casino.sm.login.CWLoginRequest;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class CWSerializeLoginForm<L extends CWLoginForm, F extends CommonStartGameForm> extends SerializeLoginForm<L, F> {
    @Override
    public CWLoginRequest getLoginRequest(L form) throws InvocationTargetException, IllegalAccessException {
        CWLoginRequest loginRequest = new CWLoginRequest();
        BeanUtils.copyProperties(loginRequest, super.getLoginRequest(form));

        loginRequest.setBalance(Long.parseLong(form.getBalance()));

        return loginRequest;
    }

    public CWLoginRequest getLoginRequest(F form) throws InvocationTargetException, IllegalAccessException {
        CWLoginRequest loginRequest = new CWLoginRequest();
        BeanUtils.copyProperties(loginRequest, super.getLoginRequest(form));

        return loginRequest;
    }
}
