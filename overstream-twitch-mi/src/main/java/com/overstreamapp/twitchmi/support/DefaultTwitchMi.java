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
import com.overstreamapp.store.ValueAction;
import com.overstreamapp.twitchmi.TwitchMiSettings;
import com.overstreamapp.twitchmi.domain.ChatMessage;
import com.overstreamapp.twitchmi.state.TwitchChat;
import com.overstreamapp.websocket.client.WebSocketClient;
import org.slf4j.Logger;

public class DefaultTwitchMi extends AbstractTwitchMi {
    private final Logger logger;
    private final Store<TwitchChat> twitchChatState;

    @Inject
    public DefaultTwitchMi(Logger logger, TwitchMiSettings settings, EventLoopGroupManager loopGroupManager, WebSocketClient webSocketClient, StoreKeeper storeKeeper) {
        super(logger, settings, loopGroupManager, webSocketClient);
        this.logger = logger;
        this.twitchChatState = storeKeeper.storeBuilder(TwitchChat.class).persistence(settings.chatHistory()).build();
    }

    @Override
    void onChatMessage(ChatMessage message) {
        logger.debug("Chat Message: [{}] {} : {}",
                message.getChannelName(),
                message.getUserName(),
                message.getText());

        twitchChatState.dispatch(new ValueAction(new TwitchChat(message)));
    }
}
