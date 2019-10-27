package com.overstreamapp.osc.types;

import com.overstreamapp.osc.OscReadException;
import com.overstreamapp.osc.OscWriteException;

import java.nio.ByteBuffer;

public class OscFloat implements OscType {

    private float value;

    public OscFloat() {
    }

    public OscFloat(float value) {
        this.value = value;
    }

    @Override
    public void read(ByteBuffer buffer) throws OscReadException {
        this.value = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer) throws OscWriteException {
        buffer.putFloat(this.value);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Float.floatToIntBits(this.value);
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
        final OscFloat other = (OscFloat) obj;
        return Float.floatToIntBits(this.value) == Float.floatToIntBits(other.value);
    }

    @Override
    public String toString() {
        return "" + value;
    }

}
