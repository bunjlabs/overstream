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
