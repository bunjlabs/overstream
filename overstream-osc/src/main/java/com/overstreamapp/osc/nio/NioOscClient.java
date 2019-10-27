package com.overstreamapp.osc.nio;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.*;
import com.overstreamapp.osc.types.OscBundle;
import com.overstreamapp.osc.types.OscMessage;
import com.overstreamapp.osc.types.OscPacket;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NioOscClient implements OscClient {

    private final Logger logger;
    private final EventLoopGroupManager eventLoopGroupManager;
    private final ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
    private final ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
    private DatagramChannel channel;
    private SocketAddress remoteAddress;
    private OscHandler handler;
    private boolean enabled = false;

    @Inject
    public NioOscClient(Logger logger, EventLoopGroupManager eventLoopGroupManager) {
        this.logger = logger;
        this.eventLoopGroupManager = eventLoopGroupManager;
    }

    @Override
    public void start(SocketAddress remoteAddress, OscHandler handler) {
        try {
            this.channel = DatagramChannel.open();
            this.channel.bind(null);
            this.channel.connect(remoteAddress);
            this.remoteAddress = remoteAddress;
            this.handler = handler;

            this.enabled = true;
            this.eventLoopGroupManager.getWorkerEventLoopGroup().execute(this::receiveThread);
        } catch (IOException e) {
            logger.error("Unable to start nio osc client.", e);
        }
    }

    @Override
    public void send(OscPacket oscPacket) throws OscWriteException {
        logger.trace("Send packet {}", oscPacket);

        try {
            sendBuffer.clear();
            oscPacket.write(sendBuffer);
            sendBuffer.flip();
            channel.send(sendBuffer, remoteAddress);
        } catch (IOException ex) {
            throw new OscWriteException(ex);
        }
    }

    private void receiveThread() {
        while (enabled) {
            try {
                receiveBuffer.clear();
                channel.read(receiveBuffer);
                receiveBuffer.flip();
                onData(receiveBuffer.asReadOnlyBuffer());
            } catch (IOException ex) {
                logger.error("Error reading data.", ex);
            }
        }
    }

    private OscPacket parse(ByteBuffer buffer) throws OscReadException {
        OscPacket packet;

        if (OscUtils.isBundle(buffer)) {
            packet = new OscBundle();
        } else {
            packet = new OscMessage();
        }

        packet.read(buffer);

        return packet;
    }

    private void onData(ByteBuffer buffer) {
        try {
            OscPacket packet = parse(buffer);

            if (this.handler != null) {
                if (packet instanceof OscMessage) {
                    this.handler.onMessage(this, (OscMessage) packet);
                } else if (packet instanceof OscBundle) {
                    this.handler.onBundle(this, (OscBundle) packet);
                }
            }
        } catch (OscReadException ex) {
            logger.error("Unable to read osc packet.", ex);
        }
    }
}
