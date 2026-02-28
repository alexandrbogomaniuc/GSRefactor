package com.abs.casino.common.web.bonus;

import java.io.Serializable;

/**
 * User: ktd
 * Date: 29.03.11
 */
public class BonusError implements Serializable {
    private final int code;
    private final String description;

    public BonusError(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BonusError");
        sb.append("[code=").append(code);
        sb.append(", description=").append(description);
        sb.append(']');
        return sb.toString();
    }
}
