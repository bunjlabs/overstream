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
public class BitsMessage {

    private int bitsUser;
    private String channelId;
    private String chatMessage;
    private String context;
    private boolean isAnonymous;
    private String messageId;
    private String messageType;
    private String time;
    private int totalBitsUsed;
    private String userId;
    private String userName;
    private String version;

    public int getBitsUser() {
        return bitsUser;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public String getContext() {
        return context;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getTime() {
        return time;
    }

    public int getTotalBitsUsed() {
        return totalBitsUsed;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getVersion() {
        return version;
    }
}
