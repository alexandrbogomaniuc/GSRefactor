package com.dgphoenix.casino.common.client.canex.request.privateroom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InvitePlayersOutput {
    private static final Logger LOG = LogManager.getLogger(InvitePlayersOutput.class);

    @JsonProperty("EXTSYSTEM")
    @SerializedName("EXTSYSTEM")
    private InvitePlayersExtSystem extSystem;

    public InvitePlayersOutput() {
    }

    public InvitePlayersOutput(InvitePlayersRequest request, InvitePlayersResponse response, String time) {
        extSystem = new InvitePlayersExtSystem();
        extSystem.setRequest(request);
        extSystem.setResponse(response);
        extSystem.setTime(time);
    }

    public InvitePlayersExtSystem getExtSystem() {
        return extSystem;
    }

    public void setExtSystem(InvitePlayersExtSystem extSystem) {
        this.extSystem = extSystem;
    }
}
