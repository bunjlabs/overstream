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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class OscString implements OscType {

    private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private String value;

    public OscString() {
        this.value = "";
    }

    public OscString(String value) {
        this.value = value;
    }

    @Override
    public void read(ByteBuffer buffer) throws OscReadException {
        int len = 0;
        while (buffer.get(buffer.position() + len) != 0) {
            len++;
        }

        ByteBuffer strBuffer = buffer.slice();
        strBuffer.limit(len);

        CharsetDecoder decoder = DEFAULT_CHARSET.newDecoder();

        try {
            this.value = decoder.decode(strBuffer).toString();
        } catch (CharacterCodingException ex) {
            throw new OscReadException("unable to read string", ex);
        }

        buffer.position(buffer.position() + len + 1); // null termintated string

        OscUtils.alignSkip(buffer);
    }

    @Override
    public void write(ByteBuffer buffer) {
        byte[] stringBytes = this.value.getBytes(DEFAULT_CHARSET);

        buffer.put(stringBytes);
        buffer.put((byte) 0);

        OscUtils.alignWrite(buffer);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.value);
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
        final OscString other = (OscString) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return value;
    }

}
