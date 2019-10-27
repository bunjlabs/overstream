package com.overstreamapp.osc.types;

import com.overstreamapp.osc.OscReadException;
import com.overstreamapp.osc.OscWriteException;

import java.nio.ByteBuffer;


public interface OscType {

    void read(ByteBuffer buffer) throws OscReadException;

    void write(ByteBuffer buffer) throws OscWriteException;

    @Override
    String toString();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

}
