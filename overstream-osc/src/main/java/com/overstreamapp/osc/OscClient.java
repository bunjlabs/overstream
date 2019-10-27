package com.overstreamapp.osc;


import java.net.SocketAddress;

public interface OscClient extends OscChannel {

    void start(SocketAddress socketAddress, OscHandler handler);
}
