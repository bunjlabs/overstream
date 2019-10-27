package com.overstreamapp.websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface WebSocket {

    void send(String text);

    void send(ByteBuffer bytes);

    void send(byte[] bytes);

    void close(int code, String message);

    void close(int code);

    void close();

    InetSocketAddress getRemoteSocketAddress();

    InetSocketAddress getLocalSocketAddress();

    boolean isOpen();

    boolean isClosed();
}
