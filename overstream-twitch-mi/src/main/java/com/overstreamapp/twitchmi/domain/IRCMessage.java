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

package com.overstreamapp.twitchmi.domain;

import java.util.Collections;
import java.util.Map;

public class IRCMessage {

    private final Map<String, String> tags;
    private final String clientName;
    private final String command;
    private final String channelName;
    private final String message;
    private final String rawMessage;

    public IRCMessage(String rawMessage) {
        this.tags = Collections.emptyMap();
        this.clientName = "";
        this.command = "";
        this.channelName = "";
        this.message = "";
        this.rawMessage = rawMessage;
    }

    public IRCMessage(Map<String, String> tags, String clientName, String command, String channelName, String message, String rawMessage) {
        this.tags = tags;
        this.clientName = clientName;
        this.command = command;
        this.channelName = channelName;
        this.message = message;
        this.rawMessage = rawMessage;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getClientName() {
        return clientName;
    }

    public String getCommand() {
        return command;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getMessage() {
        return message;
    }

    public String getRawMessage() {
        return rawMessage;
    }
}
