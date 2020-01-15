/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public void setFps(double fps) {
        this.fps = fps;
    }

    public void setRenderTotalFrames(int renderTotalFrames) {
        this.renderTotalFrames = renderTotalFrames;
    }

    public void setRenderMissedFrames(int renderMissedFrames) {
        this.renderMissedFrames = renderMissedFrames;
    }

    public void setOutputTotalFrames(int outputTotalFrames) {
        this.outputTotalFrames = outputTotalFrames;
    }

    public void setOutputSkippedFrames(int outputSkippedFrames) {
        this.outputSkippedFrames = outputSkippedFrames;
    }

    public void setAverageFrameTime(double averageFrameTime) {
        this.averageFrameTime = averageFrameTime;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setFreeDiskSpace(double freeDiskSpace) {
        this.freeDiskSpace = freeDiskSpace;
    }
}