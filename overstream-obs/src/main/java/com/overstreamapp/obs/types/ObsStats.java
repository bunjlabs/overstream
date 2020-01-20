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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObsStats {
    private double fps;
    private int renderTotalFrames;
    private int renderMissedFrames;
    private int outputTotalFrames;
    private int outputSkippedFrames;
    private double averageFrameTime;
    private double cpuUsage;
    private double memoryUsage;
    private double freeDiskSpace;

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