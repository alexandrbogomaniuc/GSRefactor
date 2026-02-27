package com.abs.casino.helpers.login.serializers;

import com.abs.casino.forms.game.CommonStartGameForm;
import com.abs.casino.forms.login.CommonLoginForm;
import com.abs.casino.sm.login.LoginRequest;

import java.lang.reflect.InvocationTargetException;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class SerializeLoginForm<L extends CommonLoginForm, S extends CommonStartGameForm> {
    public LoginRequest getLoginRequest(L form) throws InvocationTargetException, IllegalAccessException {
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setToken(form.getToken());
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setBankId(form.getBankId());
        loginRequest.setRemoteHost(form.getHost());
        loginRequest.setClientType(form.getClientType());

        return loginRequest;
    }

    public LoginRequest getLoginRequest(S form) throws InvocationTargetException, IllegalAccessException {
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setToken(form.getToken());
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setBankId(form.getBankId());
        loginRequest.setRemoteHost(form.getHost());
        loginRequest.setClientType(form.getClientType());

        return loginRequest;
    }
}
