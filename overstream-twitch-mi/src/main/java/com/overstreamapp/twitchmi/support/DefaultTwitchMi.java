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

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.twitchmi.TwitchMiSettings;
import com.overstreamapp.twitchmi.TwitchMiTriggerBuilder;
import com.overstreamapp.twitchmi.domain.ChatMessage;
import com.overstreamapp.twitchmi.state.TwitchChat;
import com.overstreamapp.websocket.client.WebSocketClient;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTwitchMi extends AbstractTwitchMi {
    private final Logger logger;
    private final Store<TwitchChat> twitchChatState;
    private final Map<Set<String>, AbstractTrigger> triggers = new ConcurrentHashMap<>();

    @Inject
    public DefaultTwitchMi(Logger logger, TwitchMiSettings settings, EventLoopGroupManager loopGroupManager, WebSocketClient webSocketClient, StoreKeeper storeKeeper) {
        super(logger, settings, loopGroupManager, webSocketClient);
        this.logger = logger;
        this.twitchChatState = storeKeeper.storeBuilder(TwitchChat.class).persistence(settings.chatHistory()).build();
    }

    @Override
    public TwitchMiTriggerBuilder createTrigger(String... aliases) {
        return new DefaultTwitchMiTriggerBuilder(this, Arrays.asList(aliases));
    }

    @Override
    public TwitchMiTriggerBuilder createTrigger(Iterable<String> aliases) {
        return new DefaultTwitchMiTriggerBuilder(this, aliases);
    }

    @Override
    void onChatMessage(ChatMessage message) {
        logger.debug("Chat Message: [{}] {} : {}",
                message.getChannelName(),
                message.getUserName(),
                message.getText());

        twitchChatState.dispatch(new TwitchChat(message));
        processTriggers(message);
    }

    Logger getLogger() {
        return logger;
    }

    void registerChatTrigger(Set<String> aliases, AbstractTrigger trigger) {
        triggers.put(aliases, trigger);
    }

    void unregisterChatTrigger(Set<String> aliases) {
        triggers.remove(aliases);
    }

    private void processTriggers(ChatMessage message) {
        triggers.values().forEach(trigger -> trigger.trigger(message));
    }
}
