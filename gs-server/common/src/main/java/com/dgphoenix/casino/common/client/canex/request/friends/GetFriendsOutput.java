package com.dgphoenix.casino.common.client.canex.request.friends;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetFriendsOutput {
    private static final Logger LOG = LogManager.getLogger(GetFriendsOutput.class);

    @JsonProperty("EXTSYSTEM")
    @SerializedName("EXTSYSTEM")
    private GetFriendsExtSystem extSystem;

    public GetFriendsOutput() {
    }

    public GetFriendsOutput(GetFriendsRequest request, GetFriendsResponse response, String time) {
        extSystem = new GetFriendsExtSystem();
        extSystem.setRequest(request);
        extSystem.setResponse(response);
        extSystem.setTime(time);
    }

    public GetFriendsExtSystem getExtSystem() {
        return extSystem;
    }

    public void setExtSystem(GetFriendsExtSystem extSystem) {
        this.extSystem = extSystem;
    }
}
