package com.overstreamapp.osc;

import com.overstreamapp.osc.types.OscPacket;


public interface OscChannel {

    void send(OscPacket packet) throws OscWriteException;
}
