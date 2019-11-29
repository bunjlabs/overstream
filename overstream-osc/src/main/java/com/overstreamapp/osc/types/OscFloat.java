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
