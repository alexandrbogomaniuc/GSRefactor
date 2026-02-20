package com.dgphoenix.casino.common.client.canex.request.onlinerooms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PushRoomsPlayersOutput {
    private static final Logger LOG = LogManager.getLogger(PushRoomsPlayersOutput.class);

    @JsonProperty("EXTSYSTEM")
    @SerializedName("EXTSYSTEM")
    private PushRoomsPlayersExtSystem extSystem;

    public PushRoomsPlayersOutput() {
    }

    public PushRoomsPlayersOutput(PushRoomsPlayersRequest request, PushRoomsPlayersResponse response, String time) {
        extSystem = new PushRoomsPlayersExtSystem();
        extSystem.setRequest(request);
        extSystem.setResponse(response);
        extSystem.setTime(time);
    }

    public PushRoomsPlayersExtSystem getExtSystem() {
        return extSystem;
    }

    public void setExtSystem(PushRoomsPlayersExtSystem extSystem) {
        this.extSystem = extSystem;
    }
}
