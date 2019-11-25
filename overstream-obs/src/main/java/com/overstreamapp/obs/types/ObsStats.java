package com.overstreamapp.obs.types;

import com.google.gson.annotations.SerializedName;

public class ObsStats {
    @SerializedName("fps")
    private double fps;

    @SerializedName("render-total-frames")
    private int renderTotalFrames;

    @SerializedName("render-missed-frames")
    private int renderMissedFrames;

    @SerializedName("output-total-frames")
    private int outputTotalFrames;

    @SerializedName("output-skipped-frames")
    private int outputSkippedFrames;

    @SerializedName("average-frame-time")
    private double averageFrameTime;

    @SerializedName("cpu-usage")
    private double cpuUsage;

    @SerializedName("memory-usage")
    private double memoryUsage;

    @SerializedName("free-disk-space")
    private double freeDiskSpace;

    public ObsStats() {
    }

    public double getFps() {
        return fps;
    }

    public int getRenderTotalFrames() {
        return renderTotalFrames;
    }

    public int getRenderMissedFrames() {
        return renderMissedFrames;
    }

    public int getOutputTotalFrames() {
        return outputTotalFrames;
    }

    public int getOutputSkippedFrames() {
        return outputSkippedFrames;
    }

    public double getAverageFrameTime() {
        return averageFrameTime;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public double getFreeDiskSpace() {
        return freeDiskSpace;
    }
}