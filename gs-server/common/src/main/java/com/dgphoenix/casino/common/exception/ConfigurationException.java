package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 5:16:34 PM
 */
public class ConfigurationException extends CommonException {

    private static final long serialVersionUID = -4484556657417237689L;

    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
