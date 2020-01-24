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

package com.overstreamapp.twitchpubsub.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reward {

    private String id;
    private String channelId;
    private String title;
    private String prompt;
    private int cost;
    private boolean isUserInputRequired;
    private boolean isSubOnly;
    private TwitchImage image;
    private TwitchImage defaultImage;
    private String backgroundColor;
    private boolean isEnabled;
    private boolean isPaused;
    private boolean isInStock;
    private MaxPerStream maxPerStream;
    private boolean shouldRedemptionsSkipRequestQueue;

    public String getId() {
        return id;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getTitle() {
        return title;
    }

    public String getPrompt() {
        return prompt;
    }

    public int getCost() {
        return cost;
    }

    public boolean isUserInputRequired() {
        return isUserInputRequired;
    }

    public boolean isSubOnly() {
        return isSubOnly;
    }

    public TwitchImage getImage() {
        return image;
    }

    public TwitchImage getDefaultImage() {
        return defaultImage;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isInStock() {
        return isInStock;
    }

    public MaxPerStream getMaxPerStream() {
        return maxPerStream;
    }

    public boolean isShouldRedemptionsSkipRequestQueue() {
        return shouldRedemptionsSkipRequestQueue;
    }
}
