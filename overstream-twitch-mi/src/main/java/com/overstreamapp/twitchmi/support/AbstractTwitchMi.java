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

import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.twitchmi.TMIUtils;
import com.overstreamapp.twitchmi.TwitchMi;
import com.overstreamapp.twitchmi.TwitchMiConnectionState;
import com.overstreamapp.twitchmi.TwitchMiSettings;
import com.overstreamapp.twitchmi.domain.*;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import io.github.bucket4j.AsyncScheduledBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractTwitchMi implements TwitchMi {

    private final static Pattern MESSAGE_PATTERN = Pattern.compile("^(?:@(?<tags>.+?) )?(?<clientName>.+?)(?: (?<command>[A-Z0-9]+) )(?:#(?<channel>.*?) )?(?<payload>[:\\-\\+](?<message>.+))?$");
    private final static Pattern CLIENT_PATTERN = Pattern.compile("^:(.*?)!(.*?)@(.*?).tmi.twitch.tv$");
    private final static Pattern BADGES_PATTERN = Pattern.compile("(?:(\\w+)/(\\d+)(?:,|$))");
    private final static Pattern EMOTES_PATTERN = Pattern.compile("(?:(\\d+)-(\\d+)(?:,|$))");

    private final Logger logger;
    private final TwitchMiSettings settings;
    private final EventLoopGroupManager loopGroupManager;
    private final WebSocketClient webSocketClient;
    private final WebSocketHandler webSocketHandler;
    private final CircularFifoQueue<String> ircCommandQueue;
    private final AsyncScheduledBucket asyncMessageBucket;
    private final Set<String> joinedChannels = ConcurrentHashMap.newKeySet();

    private volatile TwitchMiConnectionState state = TwitchMiConnectionState.DISCONNECTED;
    private WebSocket webSocket;

    AbstractTwitchMi(Logger logger, TwitchMiSettings settings, EventLoopGroupManager loopGroupManager, WebSocketClient webSocketClient) {
        this.logger = logger;
        this.settings = settings;
        this.loopGroupManager = loopGroupManager;
        this.webSocketClient = webSocketClient;
        this.webSocketHandler = new Handler();

        this.ircCommandQueue = new CircularFifoQueue<>(200);
        var messageBucket = Bucket4j.builder()
                .addLimit(Bandwidth.simple(20, Duration.ofSeconds(30)))
                .build();
        this.asyncMessageBucket = messageBucket.asAsyncScheduler();
        this.joinedChannels.addAll(settings.join());
    }

    @Override
    public void connect() {
        if (state == TwitchMiConnectionState.DISCONNECTED || state == TwitchMiConnectionState.RECONNECTING) {
            logger.info("Connecting to Twitch IRC {}.", settings.serverUri());
            this.state = TwitchMiConnectionState.CONNECTING;
            this.webSocketClient.connect(URI.create(settings.serverUri()), webSocketHandler, settings.connectTimeout());
        }
    }

    @Override
    public void disconnect() {
        if (state != TwitchMiConnectionState.DISCONNECTED) {
            state = TwitchMiConnectionState.DISCONNECTING;
            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
        }
    }

    @Override
    public void reconnect() {
        if (state != TwitchMiConnectionState.RECONNECTING) {
            state = TwitchMiConnectionState.RECONNECTING;
            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
            loopGroupManager.getWorkerEventLoopGroup().schedule(this::connect, settings.reconnectDelay(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public TwitchMiConnectionState getConnectionState() {
        return state;
    }

    @Override
    public void joinChannel(String channelName) {
        if (!joinedChannels.contains(channelName)) {
            logger.debug("Joining channel #{}.", channelName.toLowerCase());
            sendCommand("join", "#" + channelName.toLowerCase());
            joinedChannels.add(channelName);
        }
    }

    @Override
    public void leaveChannel(String channelName) {
        if (joinedChannels.contains(channelName)) {
            logger.debug("Leaving channel #{}.", channelName.toLowerCase());
            sendCommand("part", "#" + channelName.toLowerCase());
            joinedChannels.remove(channelName);
        }
    }

    @Override
    public void sendMessage(String message) {
        this.sendMessage(settings.join().get(0), message);
    }

    @Override
    public void sendMessage(String channel, String message) {
        sendCommand("PRIVMSG", "#" + channel, ":" + message);
    }

    @Override
    public void sendPrivateMessage(String targetUser, String message) {
        sendCommand("PRIVMSG", "#" + settings.userName(), ":/w", targetUser, message);
    }

    //
    // Private
    //

    abstract void onChatMessage(ChatMessage message);

    private void sendCommand(String command, String... args) {
        String ircCommand = String.format("%s %s", command.toUpperCase(), String.join(" ", args));
        logger.trace("Queue irc command: {}", ircCommand);
        ircCommandQueue.add(ircCommand);

        asyncMessageBucket.consume(1, loopGroupManager.getWorkerEventLoopGroup()).thenAccept(this::sendQueuedRawCommand);
    }

    private void sendRawCommand(String ircCommand) {
        if (this.webSocket.isOpen()) {
            logger.debug("Send irc command: {}", ircCommand);
            this.webSocket.send(ircCommand);
        }
    }

    private void sendQueuedRawCommand(Void t) {
        var queuedIrcCommand = ircCommandQueue.remove();
        sendRawCommand(queuedIrcCommand);
    }


    private void onIRCMessage(IRCMessage ircMessage) {
        if (ircMessage.getCommand().equals("PRIVMSG")) {
            onPrivMessage(ircMessage);
        }
    }

    private void onPrivMessage(IRCMessage ircMessage) {
        logger.debug("IRC PRIVMSG: {} #{} :{}",
                ircMessage.getClientName(),
                ircMessage.getChannelName(),
                ircMessage.getMessage());

        ChatMessage message;

        try {
            message = parseChatMessage(ircMessage);
        } catch (Exception ex) {
            logger.error("Unable to parse chat message", ex);
            return;
        }

        onChatMessage(message);
    }

    private IRCMessage parseIRCMessage(String rawMessage) throws TwitchMessageParseException {
        Matcher messageMatcher = MESSAGE_PATTERN.matcher(rawMessage);

        if (messageMatcher.matches()) {
            Map<String, String> tags = parseTags(messageMatcher.group("tags"));
            String clientName = parseClientName(messageMatcher.group("clientName"));
            String command = messageMatcher.group("command");
            String channelName = messageMatcher.group("channel");
            String message = messageMatcher.group("message");

            if (clientName == null || command == null || channelName == null || message == null) {
                throw new TwitchMessageParseException("Required fields is empty");
            }

            return new IRCMessage(tags, clientName, command, channelName, message, rawMessage);
        }

        return new IRCMessage(rawMessage);
    }

    private ChatMessage parseChatMessage(IRCMessage ircMessage) {
        Map<String, String> tags = ircMessage.getTags();

        long timestamp = System.currentTimeMillis();
        String id = tags.get("id");
        String channelName = ircMessage.getChannelName();
        String clientName = ircMessage.getClientName();
        String userId = tags.get("user-id");
        String displayName = tags.get("display-name");
        String userColor = TMIUtils.stringToColor(clientName);
        String message = ircMessage.getMessage();

        String rawBadges = tags.get("badges");
        Map<String, BadgeInfo> badgesMap = new HashMap<>();
        List<BadgeInfo> badges = new ArrayList<>();
        if (rawBadges != null && !rawBadges.isEmpty()) {
            Matcher badgeMatcher = BADGES_PATTERN.matcher(rawBadges);
            while (badgeMatcher.find()) {
                String badgeType = badgeMatcher.group(1);
                int badgeVersion = Integer.parseInt(badgeMatcher.group(2));

                badgesMap.put(badgeType, new BadgeInfo(badgeType, badgeVersion, ""));
            }
        }
        badgesMap.forEach((type, info) -> badges.add(info));

        String rawEmotes = tags.get("emotes");
        List<Emote> emotes = new ArrayList<>();
        if (rawEmotes != null && !rawEmotes.isEmpty()) {
            for (String rawEmote : rawEmotes.split("/")) {
                String emoteId = rawEmote.substring(0, rawEmote.indexOf(':'));
                rawEmote = rawEmote.substring(rawEmote.indexOf(':') + 1);

                Matcher emoteMatcher = EMOTES_PATTERN.matcher(rawEmote);
                while (emoteMatcher.find()) {
                    int start = Integer.parseInt(emoteMatcher.group(1));
                    int end = Integer.parseInt(emoteMatcher.group(2)) + 1;
                    String code = message.substring(start, end);
                    String url = String.format(settings.emotesUri(), emoteId);

                    emotes.add(new Emote(emoteId, code, start, end, url));
                }
            }
        }

        boolean isOwner = badgesMap.containsKey("broadcaster");
        boolean isModerator = badgesMap.containsKey("moderator");
        boolean isSubscriber = badgesMap.containsKey("subscriber");
        int userLevel = isOwner ? 100 : isModerator ? 2 : isSubscriber ? 1 : 0;

        List<MessageContent> content = new ArrayList<>();
        int lastEndIndex = 0;

        for (Emote emote : emotes) {
            String leftMessage = message.substring(lastEndIndex, emote.getStart());

            if (!leftMessage.isEmpty()) {
                content.add(new MessageContent(MessageContent.Type.TEXT, leftMessage));
            }

            content.add(new MessageContent(MessageContent.Type.EMOTE, emote.getUrl()));
            lastEndIndex = emote.getEnd();
        }

        if (lastEndIndex < message.length()) {
            String rightMessage = message.substring(lastEndIndex);
            content.add(new MessageContent(MessageContent.Type.TEXT, rightMessage));
        }

        return new ChatMessage(
                id, channelName, userId,
                displayName == null ? clientName : displayName,
                userColor, userLevel,
                timestamp, message,
                badges, content);

    }

    private Map<String, String> parseTags(String raw) {
        Map<String, String> map = new HashMap<>();

        if (raw != null && !raw.trim().isEmpty()) {
            for (String tag : raw.split(";")) {
                String[] val = tag.split("=");
                map.put(val[0], (val.length > 1) ? val[1] : null);
            }
        }

        return Collections.unmodifiableMap(map); // formatting to Read-Only Map
    }

    private String parseClientName(String raw) {
        if (raw.equals(":tmi.twitch.tv") || raw.equals(":jtv")) {
            return null;
        }

        Matcher matcher = CLIENT_PATTERN.matcher(raw);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return raw;
    }

    private class Handler implements WebSocketHandler {

        @Override
        public void onOpen(WebSocket webSocket) {
            AbstractTwitchMi.this.webSocket = webSocket;

            sendRawCommand("CAP REQ :twitch.tv/tags twitch.tv/commands twitch.tv/membership");
            sendRawCommand("CAP END");

            sendRawCommand(String.format("pass oauth:%s", settings.accessToken()));
            sendRawCommand(String.format("nick %s", settings.userName()));

            sendCommand("join", "#" + settings.userName());
            joinedChannels.forEach(c -> sendCommand("join", "#" + c.toLowerCase()));

            AbstractTwitchMi.this.state = TwitchMiConnectionState.CONNECTED;

            logger.info("Connected to Twitch IRC {}", webSocket.getUri());
        }

        @Override
        public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
            if (state != TwitchMiConnectionState.DISCONNECTING && state != TwitchMiConnectionState.RECONNECTING) {
                logger.info("Connection to Twitch IRC lost: {} {}. Retrying ...", code, reason);

                reconnect();
            } else {
                state = TwitchMiConnectionState.DISCONNECTED;
                logger.info("Disconnected from Twitch IRC: {} {}", code, reason);
            }
        }

        @Override
        public void onError(WebSocket webSocket, Throwable cause) {
            logger.error("WebSocket error", cause);
        }

        @Override
        public void onStart() {
            logger.debug("Web socket client started");
        }

        @Override
        public void onMessage(WebSocket webSocket, String rawText) {
            String[] rawMessages = rawText
                    .replace("\n\r", "\n")
                    .replace("\r", "\n")
                    .split("\n");

            for (String rawMessage : rawMessages) {
                if (rawMessage.isEmpty()) continue;

                logger.trace("IRC Raw message: {}", rawMessage);

                if (rawMessage.contains(":req Invalid CAP command")) {
                    logger.error("Failed to acquire requested IRC capabilities!");
                } else if (rawMessage.contains(":tmi.twitch.tv CAP * ACK :")) {
                    List<String> capabilities = Arrays.asList(rawMessage.replace(":tmi.twitch.tv CAP * ACK :", "").split(" "));
                    capabilities.forEach(cap -> logger.trace("Acquired chat capability: " + cap));
                } else if (rawMessage.contains("PING :tmi.twitch.tv")) {
                    sendCommand("PONG :tmi.twitch.tv");
                    logger.trace("Responding to PING request!");
                } else if (rawMessage.equals(":tmi.twitch.tv NOTICE * :Login authentication failed")) {
                    logger.error("Invalid IRC Credentials. Login failed!");
                } else {
                    try {
                        onIRCMessage(parseIRCMessage(rawMessage));
                    } catch (Exception ex) {
                        logger.error("Unable to parse irc message", ex);
                    }
                }
            }
        }

        @Override
        public void onMessage(WebSocket socket, ByteBuffer bytes) {

        }
    }
}
