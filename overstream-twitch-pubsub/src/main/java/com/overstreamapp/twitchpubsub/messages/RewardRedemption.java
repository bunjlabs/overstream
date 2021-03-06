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
public class RewardRedemption {

    private String id;
    private User user;
    private String channelId;
    private String redeemedAt;
    private Reward reward;
    private String userInput;
    private String status;

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getRedeemedAt() {
        return redeemedAt;
    }

    public Reward getReward() {
        return reward;
    }

    public String getUserInput() {
        return userInput;
    }

    public String getStatus() {
        return status;
    }
}
