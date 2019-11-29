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
