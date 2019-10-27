package com.overstreamapp.osc.types;

import com.overstreamapp.osc.OscReadException;
import com.overstreamapp.osc.OscUtils;
import com.overstreamapp.osc.OscWriteException;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class OscBlob implements OscType {

    private byte[] value;

    public OscBlob() {
        value = new byte[0];
    }

    public OscBlob(byte[] value) {
        this.value = value;
    }

    @Override
    public void read(ByteBuffer buffer) throws OscReadException {
        OscInt size = new OscInt();
        size.read(buffer);

        value = new byte[size.getValue()];

        buffer.get(value);

        OscUtils.alignSkip(buffer);
    }

    @Override
    public void write(ByteBuffer buffer) throws OscWriteException {
        OscInt size = new OscInt(value.length);

        size.write(buffer);

        buffer.put(value);

        OscUtils.alignWrite(buffer);
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Arrays.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OscBlob other = (OscBlob) obj;
        return Arrays.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return "[BLOB]";
    }

}
