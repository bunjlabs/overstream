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

package com.overstreamapp.twitchmi.support;

import com.bunjlabs.fuga.util.Assert;
import com.overstreamapp.twitchmi.TwitchMiTrigger;
import com.overstreamapp.twitchmi.domain.ChatMessage;
import org.slf4j.Logger;

import java.util.Set;
import java.util.function.Consumer;

abstract class AbstractTrigger implements TwitchMiTrigger {
    private final DefaultTwitchMi twitchMi;
    protected final Logger logger;
    protected final Set<String> aliases;
    private final Consumer<ChatMessage> consumer;

    AbstractTrigger(DefaultTwitchMi twitchMi, Set<String> aliases, Consumer<ChatMessage> consumer) {
        this.twitchMi = twitchMi;
        this.logger = twitchMi.getLogger();
        this.aliases = Assert.notNull(aliases);
        this.consumer = consumer;
    }

    boolean trigger(ChatMessage message) {
        if (check(message.getText().strip())) {
            try {
                consumer.accept(message);
            } catch (Throwable t) {
                logger.warn("Error processing chat trigger", t);
            }

            return true;
        }
        return false;
    }

    abstract boolean check(String message);

    @Override
    public void remove() {
        twitchMi.unregisterChatTrigger(aliases);
    }
}
