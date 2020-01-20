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

package com.overstreamapp.obs.obsevent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.overstreamapp.obs.types.ObsStats;

@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObsHeartbeat extends ObsEvent {
    private boolean pulse;
    private String currentProfile;
    private String currentScene;
    private boolean streaming;
    private int totalStreamTime;
    private int totalStreamBytes;
    private int totalStreamFrames;
    private boolean recording;
    private int totalRecordTime;
    private int totalRecordBytes;
    private int totalRecordFrames;
    private ObsStats stats;

    public boolean isPulse() {
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
