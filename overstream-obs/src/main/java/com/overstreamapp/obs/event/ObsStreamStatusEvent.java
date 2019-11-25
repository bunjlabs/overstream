package com.overstreamapp.obs.event;

import com.google.gson.annotations.SerializedName;

public class ObsStreamStatusEvent extends ObsEvent {
    @SerializedName("streaming")
    private boolean streaming;

    @SerializedName("recording")
    private boolean recording;

    @SerializedName("replay-buffer-active")
    private boolean replayBufferActive;

    @SerializedName("bytes-per-sec")
    private int bytesPerSec;

    @SerializedName("kbits-per-sec")
    private int kbitsPerSec;

    @SerializedName("strain")
    private double strain;

    @SerializedName("total-stream-time")
    private int totalStreamTime;

    @SerializedName("num-total-frames")
    private int numTotalFrames;

    @SerializedName("num-dropped-frames")
    private int numDroppedFrames;

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

    @SerializedName("preview-only")
    private boolean previewOnly;

    public ObsStreamStatusEvent() {
    }

    public boolean isStreaming() {
        return streaming;
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean isReplayBufferActive() {
        return replayBufferActive;
    }

    public int getBytesPerSec() {
        return bytesPerSec;
    }

    public int getKbitsPerSec() {
        return kbitsPerSec;
    }

    public double getStrain() {
        return strain;
    }

    public int getTotalStreamTime() {
        return totalStreamTime;
    }

    public int getNumTotalFrames() {
        return numTotalFrames;
    }

    public int getNumDroppedFrames() {
        return numDroppedFrames;
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

    public boolean isPreviewOnly() {
        return previewOnly;
    }
}
