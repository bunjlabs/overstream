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

package com.overstreamapp.osc.types;

import com.overstreamapp.osc.OscReadException;
import com.overstreamapp.osc.OscWriteException;

import java.nio.ByteBuffer;


public class OscInt implements OscType {

    private int value;

    public OscInt() {
    }

    public OscInt(int value) {
        this.value = value;
    }

    @Override
    public void read(ByteBuffer buffer) throws OscReadException {
        this.value = buffer.getInt();
    }

    @Override
    public void write(ByteBuffer buffer) throws OscWriteException {
        int curValue = value;

        final byte[] intBytes = new byte[4];
        intBytes[3] = (byte) curValue;
        curValue >>>= 8;
        intBytes[2] = (byte) curValue;
        curValue >>>= 8;
        intBytes[1] = (byte) curValue;
        curValue >>>= 8;
        intBytes[0] = (byte) curValue;

        buffer.put(intBytes);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.value;
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
        final OscInt other = (OscInt) obj;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        return "" + value;
    }

}
