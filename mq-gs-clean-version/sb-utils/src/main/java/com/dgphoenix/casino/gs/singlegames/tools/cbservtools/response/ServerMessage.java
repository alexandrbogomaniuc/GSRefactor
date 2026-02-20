package com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;

/**
 * Created by vladislav on 12/6/16.
 */
public abstract class ServerMessage {
    protected static final String FIELDS_DELIMITER = ";";
    protected static final Joiner FIELDS_JOINER = Joiner.on(FIELDS_DELIMITER);

    private static final String ELEMENTS_DELIMITER = "|";
    protected static final Joiner ELEMENTS_JOINER = Joiner.on(ELEMENTS_DELIMITER);

    private static final String ID_DELIMITER = "_";
    private static final Joiner ID_JOINER = Joiner.on(ID_DELIMITER);

    public static final String VALUE_DELIMITER = "=";
    protected static final String PARAMS_DELIMITER = "&";
    protected static final MapJoiner PARAMS_JOINER = Joiner.on(PARAMS_DELIMITER).withKeyValueSeparator(VALUE_DELIMITER);

    protected String id;

    protected String composeId(Object... identifiers) {
        return ID_JOINER.join(identifiers);
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String httpFormat() {
        return id;
    }

    @Override
    public String toString() {
        return "id=" + id;
    }
}
