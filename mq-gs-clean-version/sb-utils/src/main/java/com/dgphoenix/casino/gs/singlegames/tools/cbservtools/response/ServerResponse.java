package com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response;

/**
 * Created by vladislav on 2/15/17.
 */
public abstract class ServerResponse extends ServerMessage {
    private static final String SERVER_RESPONSE_PREFIX = "response";

    private long requestId;

    public void setRequestId(long requestId) {
        String id = composeId(SERVER_RESPONSE_PREFIX, requestId);
        super.setId(id);
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return super.toString() + ", requestId=" + requestId;
    }
}
