package com.overstreamapp.osc.types;

import com.overstreamapp.osc.OscReadException;
import com.overstreamapp.osc.OscWriteException;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class OscBundle implements OscPacket {

    private final long timetag;
    private final List<OscPacket> packets;

    public OscBundle() {
        this.timetag = 0;
        this.packets = Collections.emptyList();
    }

    public OscBundle(long timetag) {
        this.timetag = timetag;
        this.packets = Collections.emptyList();
    }

    public OscBundle(long timetag, List<OscPacket> packets) {
        this.timetag = timetag;
        this.packets = packets;
    }

    public long getTimetag() {
        return timetag;
    }

    public List<OscPacket> getPackets() {
        return packets;
    }

    @Override
    public void read(ByteBuffer buffer) throws OscReadException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(ByteBuffer buffer) throws OscWriteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
