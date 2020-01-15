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

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.keeper.Keeper;
import com.overstreamapp.keeper.State;
import com.overstreamapp.twitchbot.state.TwitchChatState;
import com.overstreamapp.twitchmi.ChatMessage;
import com.overstreamapp.twitchmi.TwitchMi;
import com.overstreamapp.util.ArrayUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultTwitchBot implements TwitchBot {
    private Logger logger;
    private final TwitchBotSettings settings;
    private final TwitchMi twitchMi;
    private final State<TwitchChatState> twitchChatState;

    private final List<CommandsHandler> commandsHandlers = new CopyOnWriteArrayList<>();
    private final List<ChatListener> chatListeners = new CopyOnWriteArrayList<>();

    @Inject
    public DefaultTwitchBot(Logger logger, TwitchBotSettings settings, TwitchMi twitchMi, Keeper keeper) {
        this.logger = logger;
        this.settings = settings;
        this.twitchMi = twitchMi;
        this.twitchChatState = keeper.stateBuilder(TwitchChatState.class)
                .persistenceListCapped().history(settings.chatHistory()).build();
    }

    @Override
    public void start() {
        settings.channels().forEach(twitchMi::joinChannel);
        twitchMi.subscribeChatMessage(this::onMessage);

        logger.info("Started");
    }

    @Override
    public void registerCommandsHandler(CommandsHandler handler) {
        commandsHandlers.add(handler);
    }

    @Override
    public void subscribeOnChat(ChatListener listener) {
        chatListeners.add(listener);
    }

    @Override
    public Iterator<CommandReference> listAvailableCommands() {
        return ArrayUtils.multiIterator(commandsHandlers.iterator(), CommandsHandler::register);
    }

    @Override
    public void say(String channel, String message) {
        twitchMi.sendMessage(channel, message);
    }

    @Override
    public void say(String message) {
        String channel = settings.channels().get(0);
        say(channel, message);
    }

    /*
     * Private
     */
    private void onMessage(ChatMessage message) {
        try {
            processChat(message);
        } catch (Exception ex) {
            logger.error("Unable to process chat message", ex);
            return;
        }

        if (message.getText().startsWith("!")) {
            String text = message.getText();
            String[] commandArray = text.split(" ");
            String command = commandArray[0].substring(1);
            String[] args = Arrays.copyOfRange(commandArray, 1, commandArray.length);

            CommandReference commandReference = null;

            Iterator<CommandReference> commandReferenceIterator = listAvailableCommands();

            while (commandReferenceIterator.hasNext()) {
                CommandReference next = commandReferenceIterator.next();

                if (next.getCommand().equals(command)) {
                    commandReference = next;
                    break;
                }
            }

            if (commandReference == null) return;

            CommandsHandler handler = commandReference.getHandler();

            if (!handler.checkCooldown(commandReference.getCooldown(), message.getTimestamp())) return;
            if (commandReference.getLevel() > message.getUserLevel()) return;

            handler.setCooldownTime(message.getTimestamp());

            CommandContext context = new CommandContext(message, message.getChannelName(), command, args, commandReference);

            processCommand(context);
        }
    }

    private void processChat(ChatMessage message) {
        logger.debug("Chat Message: [{}] {} : {}",
                message.getChannelName(),
                message.getUserName(),
                message.getText());

        twitchChatState.push(new TwitchChatState(message));
        chatListeners.forEach(l -> l.onChat(message));
    }

    private void processCommand(CommandContext context) {
        logger.debug("Chat CommandsHandler: [{}] {} : !{} {}",
                context.getMessage().getChannelName(),
                context.getMessage().getUserName(),
                context.getCommand(),
                context.getArgs());

        context.getReference().getConsumer().accept(context, context.getArgs());
    }


}
