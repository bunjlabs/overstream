package com.overstreamapp.obs.request;

public class ObsSetHeartbeatRequest extends ObsRequest {
    private boolean enable;

    public ObsSetHeartbeatRequest(boolean enable) {
        super("SetHeartbeat");
        this.enable = enable;
    }
}
