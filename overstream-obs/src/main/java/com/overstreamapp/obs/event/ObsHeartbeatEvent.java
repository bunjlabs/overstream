package com.overstreamapp.obs.event;

import com.google.gson.annotations.SerializedName;
import com.overstreamapp.obs.types.ObsStats;

public class ObsHeartbeatEvent extends ObsEvent {
    @SerializedName("pulse")
    private boolean pulse;

    @SerializedName("current-profile")
    private String currentProfile;

    @SerializedName("current-scene")
    private String currentScene;

    @SerializedName("streaming")
    private boolean streaming;

    @SerializedName("total-stream-time")
    private int totalStreamTime;

    @SerializedName("total-stream-bytes")
    private int totalStreamBytes;

    @SerializedName("total-stream-frames")
    private int totalStreamFrames;

    @SerializedName("recording")
    private boolean recording;

    @SerializedName("total-record-time")
    private int totalRecordTime;

    @SerializedName("total-record-bytes")
    private int totalRecordBytes;

    @SerializedName("total-record-frames")
    private int totalRecordFrames;

    @SerializedName("stats")
    private ObsStats stats;

    public ObsHeartbeatEvent() {
    }

    public boolean pulse() {
        return pulse;
    }

    public String getCurrentProfile() {
        return currentProfile;
    }

    public String getCurrentScene() {
        return currentScene;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public int getTotalStreamTime() {
        return totalStreamTime;
    }

    public int getTotalStreamBytes() {
        return totalStreamBytes;
    }

    public int getTotalStreamFrames() {
        return totalStreamFrames;
    }

    public boolean isRecording() {
        return recording;
    }

    public int getTotalRecordTime() {
        return totalRecordTime;
    }

    public int getTotalRecordBytes() {
        return totalRecordBytes;
    }

    public int getTotalRecordFrames() {
        return totalRecordFrames;
    }

    public ObsStats getStats() {
        return stats;
    }
}
