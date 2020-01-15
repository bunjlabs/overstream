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

package com.overstreamapp.twitchbot;


import com.overstreamapp.twitchmi.ChatMessage;

public class CommandContext {

    private final ChatMessage message;
    private final String channel;
    private final String command;
    private final String[] args;
    private final CommandReference reference;

    public CommandContext(ChatMessage message, String channel, String command, String[] args, CommandReference reference) {
        this.message = message;
        this.channel = channel;
        this.command = command;
        this.args = args;
        this.reference = reference;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public String getChannel() {
        return channel;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public CommandReference getReference() {
        return reference;
    }
}
