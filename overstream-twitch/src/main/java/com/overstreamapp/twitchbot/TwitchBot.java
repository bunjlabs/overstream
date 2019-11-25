package com.overstreamapp.twitchbot;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.statemanager.*;
import com.overstreamapp.twitch.BadgeInfo;
import com.overstreamapp.twitch.ChatMessage;
import com.overstreamapp.twitch.MessageContent;
import com.overstreamapp.twitch.TwitchMi;
import com.overstreamapp.util.ArrayUtils;
import org.bson.Document;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TwitchBot {
    private Logger logger;
    private final TwitchBotSettings settings;
    private final TwitchMi twitchMi;
    private final State twitchChatState;

    private final List<CommandsHandler> commandsHandlers = new CopyOnWriteArrayList<>();
    private final List<ChatListener> chatListeners = new CopyOnWriteArrayList<>();

    @Inject
    public TwitchBot(Logger logger, TwitchBotSettings settings, TwitchMi twitchMi, StateManager stateManager) {
        this.logger = logger;
        this.settings = settings;
        this.twitchMi = twitchMi;
        this.twitchChatState = stateManager.createState(new StateOptions("TwitchChat", StateType.STATE, 1000, TwitchChatStateObject::new));
    }

    public void start() {
        settings.channels().forEach(twitchMi::joinChannel);
        twitchMi.subscribeChatMessage(this::onMessage);

        logger.info("Started");
    }

    public void registerCommandsHandler(CommandsHandler handler) {
        commandsHandlers.add(handler);
    }

    public void subscribeOnChat(ChatListener listener) {
        chatListeners.add(listener);
    }

    public Iterator<CommandReference> listAvailableCommands() {
        return ArrayUtils.multiIterator(commandsHandlers.iterator(), CommandsHandler::register);
    }

    public void say(String channel, String message) {
        twitchMi.sendMessage(channel, message);
    }

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

        twitchChatState.push(new TwitchChatStateObject(message));
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


    public static class TwitchChatStateObject implements StateObject {
        private ChatMessage message;

        TwitchChatStateObject() {
        }

        TwitchChatStateObject(ChatMessage message) {
            this.message = message;
        }

        @Override
        public void save(Document document) {
            document.put("messageId", message.getId());
            document.put("channelName", message.getChannelName());
            document.put("userId", message.getUserId());
            document.put("userName", message.getUserName());
            document.put("userColor", message.getUserColor());
            document.put("userLevel", message.getUserLevel());
            document.put("timestamp", message.getTimestamp());
            document.put("text", message.getText());

            var badges = new ArrayList<Document>();
            message.getBadges().forEach(b-> {
                var badge = new Document();
                badge.put("type", b.getType());
                badge.put("version", b.getVersion());
                badge.put("url", b.getUrl());
                badges.add(badge);
            });
            document.put("badges", badges);

            var contents = new ArrayList<Document>();
            message.getContent().forEach(c -> {
                var content = new Document();
                content.put("type", c.getType().name());
                content.put("value", c.getValue());
                contents.add(content);
            });
            document.put("content", contents);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void load(Document document) {
            var badges = new ArrayList<BadgeInfo>();
            for(Document badge : ((List<Document>) document.get("badges"))) {
                badges.add(new BadgeInfo(
                   badge.getString("type"),
                   badge.getInteger("version", 0),
                   badge.getString("url")
                ));
            }
            var contents = new ArrayList<MessageContent>();
            for(Document content : ((List<Document>) document.get("content"))) {
                contents.add(new MessageContent(
                        MessageContent.Type.valueOf(content.getString("type")),
                        content.getString("value")
                ));
            }

            message = new ChatMessage(
                    document.getString("id"),
                    document.getString("channelName"),
                    document.getString("userId"),
                    document.getString("userName"),
                    document.getString("userColor"),
                    document.getInteger("userLevel", 0),
                    document.getLong("timestamp"),
                    document.getString("text"),
                    badges, contents
            );
        }
    }

}
