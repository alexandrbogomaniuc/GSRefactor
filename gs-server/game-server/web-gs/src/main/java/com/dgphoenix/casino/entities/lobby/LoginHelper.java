package com.abs.casino.entities.lobby;

import com.dgphoenix.casino.helpers.login.CWHelper;
import com.dgphoenix.casino.helpers.login.CWv3Helper;

import java.util.HashMap;
import java.util.Map;

/**
 * User: isirbis
 * Date: 13.10.14
 */
public enum LoginHelper {
    CW("cw", CWHelper.getInstance()),
    CWv3("cwv3", CWv3Helper.getInstance()),
    GUEST("guest", CWHelper.getInstance());

    private final String name;
    private final com.dgphoenix.casino.helpers.login.LoginHelper helper;

    private static final Map<String, LoginHelper> helpers = new HashMap<>();

    static {
        for (LoginHelper helper : LoginHelper.values()) {
            helpers.put(helper.name, helper);
        }
    }

    LoginHelper(String name, com.dgphoenix.casino.helpers.login.LoginHelper helper) {
        this.name = name;
        this.helper = helper;
    }

    public static LoginHelper getByName(String name) {
        return helpers.get(name);
    }

    public String getName() {
        return name;
    }

    public com.dgphoenix.casino.helpers.login.LoginHelper getHelper() {
        return helper;
    }
}
