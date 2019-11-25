package com.overstreamapp.obs.event;

import com.google.gson.annotations.SerializedName;

public abstract class ObsEvent {

    @SerializedName("update-type")
    private String updateType;

    @SerializedName("stream-timecode")
    private String streamTimecode;

    @SerializedName("rec-timecode")
    private String recTimecode;

    public String getUpdateType() {
        return updateType;
    }

    public String getStreamTimecode() {
        return streamTimecode;
    }

    public String getRecTimecode() {
        return recTimecode;
    }
}
