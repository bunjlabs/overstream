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
public class CommerceMessage {

    private String userName;
    private String displayName;
    private String channelName;
    private String userId;
    private String channelId;
    private String time;
    private String itemImageUrl;
    private String itemDescription;
    private boolean supportsChannel;
    private TwitchMessage purchaseMessage;

    public String getUserName() {
        return userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getUserId() {
        return userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getTime() {
        return time;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public boolean isSupportsChannel() {
        return supportsChannel;
    }

    public TwitchMessage getPurchaseMessage() {
        return purchaseMessage;
    }
}
