package com.overstreamapp.mpd;

import com.bunjlabs.fuga.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.statemanager.*;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import org.bson.Document;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class YmpdClient {
    private final Logger logger;

    private final YmpdClientSettings settings;
    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final StateManager stateManager;
    private final Handler webSocketHandler;
    private final ObjectMapper mapper;
    private final State songState;
    private final State playerState;

    private ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocket webSocket;

    @Inject
    public YmpdClient(Logger logger, YmpdClientSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, StateManager stateManager) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;
        this.webSocketHandler = new Handler();
        this.stateManager = stateManager;
        this.mapper = new ObjectMapper();

        this.songState = stateManager.createState(new StateOptions("YmpdSong", StateType.STATE, settings.historySize(), SongStateObject::new));
        this.playerState = stateManager.createState(new StateOptions("YmpdPlayer", StateType.STATE, PlayerStateObject::new));
    }

    public void connect() {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.RECONNECTING) {

            this.state = ConnectionState.CONNECTING;

            this.webSocketClient.connect(URI.create(settings.serverUri()), webSocketHandler);
        }
    }

    public void disconnect() {
        if (state != ConnectionState.DISCONNECTED) {
            state = ConnectionState.DISCONNECTING;
            this.webSocket.close();
            this.webSocket = null;
        }
    }

    public void reconnect() {
        state = ConnectionState.RECONNECTING;
        disconnect();
        loopGroupManager.getWorkerEventLoopGroup().schedule(this::connect, 2, TimeUnit.SECONDS);
    }

    private void onData(String type, JsonNode data) {
        logger.debug("Received message: {}", data);

        if ("song_change".equals(type)) {
            YmpdSong song = new YmpdSong(
                    data.get("pos").asInt(0),
                    data.get("title").asText(""),
                    data.get("artist").asText(""),
                    data.get("album").asText("")
            );

            this.songState.push(new SongStateObject(song));
        } else if ("state".equals(type)) {
            this.playerState.push(new PlayerStateObject(
                    data.get("state").asInt(0),
                    data.get("totalTime").asInt(0),
                    data.get("elapsedTime").asInt(0)
            ));
        }
    }

    private enum ConnectionState {
        DISCONNECTING,
        RECONNECTING,
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private class Handler implements WebSocketHandler {

        @Override
        public void onOpen(WebSocket socket) {
            YmpdClient.this.webSocket = socket;
            state = ConnectionState.CONNECTED;
            logger.info("Connected to {}", settings.serverUri());
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            state = ConnectionState.DISCONNECTED;
            logger.info("Disconnected from {}", settings.serverUri());
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.trace("Received message: {}", message);

            try {
                JsonNode root = mapper.readTree(message);

                if (root != null && root.isObject() && root.has("type") && root.has("data")) {
                    onData(root.get("type").asText(""), root.get("data"));
                }
            } catch (Exception e) {
                logger.error("Error parsing message", e);
            }
        }

        @Override
        public void onMessage(WebSocket socket, ByteBuffer bytes) {

        }

        @Override
        public void onError(WebSocket socket, Throwable ex) {
            logger.error("Error", ex);
        }

        @Override
        public void onStart() {
            logger.debug("Web socket client started");
        }
    }


    public static class PlayerStateObject implements StateObject {
        private int state;
        private int totalTime;
        private int elapsedTime;

        public PlayerStateObject() {
        }

        public PlayerStateObject(int state, int totalTime, int elapsedTime) {
            this.state = state;
            this.totalTime = totalTime;
            this.elapsedTime = elapsedTime;
        }

        @Override
        public void save(Document document) {
            document.put("state", state);
            document.put("totalTime", totalTime);
            document.put("elapsedTime", elapsedTime);
        }

        @Override
        public void load(Document document) {
            state = document.getInteger("state");
            totalTime = document.getInteger("totalTime");
            elapsedTime = document.getInteger("elapsedTime");
        }
    }


    public static class SongStateObject implements StateObject {

        private YmpdSong song;

        public SongStateObject() {
        }

        public SongStateObject(YmpdSong song) {
            this.song = song;
        }

        @Override
        public void save(Document document) {
            document.put("position", song.getPosition());
            document.put("title", song.getTitle());
            document.put("artist", song.getArtist());
            document.put("album", song.getAlbum());
        }

        @Override
        public void load(Document document) {
            this.song = new YmpdSong(
                    document.getInteger("position"),
                    document.getString("title"),
                    document.getString("artist"),
                    document.getString("album")
            );
        }
    }
}
