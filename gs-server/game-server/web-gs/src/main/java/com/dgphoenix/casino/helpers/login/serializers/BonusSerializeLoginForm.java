package com.abs.casino.helpers.login.serializers;

import com.dgphoenix.casino.forms.game.CommonBonusStartGameForm;
import com.abs.casino.forms.login.CommonBSLoginForm;
import com.dgphoenix.casino.helpers.login.serializers.SerializeGameLoginForm;
import com.dgphoenix.casino.sm.login.BonusGameLoginRequest;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class BonusSerializeLoginForm<F extends CommonBonusStartGameForm> extends SerializeGameLoginForm<CommonBSLoginForm, F> {
    @Override
    public BonusGameLoginRequest getLoginRequest(CommonBSLoginForm form) throws InvocationTargetException, IllegalAccessException {
        BonusGameLoginRequest loginRequest = new BonusGameLoginRequest();
        BeanUtils.copyProperties(loginRequest, super.getLoginRequest(form));

        loginRequest.setBonusId(form.getBonusId());

        return loginRequest;
    }

    @Override
    public BonusGameLoginRequest getLoginRequest(F form) throws InvocationTargetException, IllegalAccessException {
        BonusGameLoginRequest loginRequest = new BonusGameLoginRequest();
        BeanUtils.copyProperties(loginRequest, super.getLoginRequest(form));

        loginRequest.setBonusId(form.getBonusId());

        return loginRequest;
    }
}
