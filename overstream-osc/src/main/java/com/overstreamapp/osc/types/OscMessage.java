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
import java.util.*;

public class OscMessage implements OscPacket {

    private static final Map<Character, Class<? extends OscType>> TYPETAG_ASSOCIATIONS = new HashMap<>();
    private static final Map<Class<? extends OscType>, Character> TYPECLASS_ASSOCIATIONS = new HashMap<>();

    static {
        TYPETAG_ASSOCIATIONS.put('i', OscInt.class);
        TYPECLASS_ASSOCIATIONS.put(OscInt.class, 'i');

        TYPETAG_ASSOCIATIONS.put('f', OscFloat.class);
        TYPECLASS_ASSOCIATIONS.put(OscFloat.class, 'f');

        TYPETAG_ASSOCIATIONS.put('s', OscString.class);
        TYPECLASS_ASSOCIATIONS.put(OscString.class, 's');

        TYPETAG_ASSOCIATIONS.put('b', OscBlob.class);
        TYPECLASS_ASSOCIATIONS.put(OscBlob.class, 'b');
    }

    private String address;
    private List<OscType> arguments;

    public OscMessage() {
        this.address = "";
        this.arguments = Collections.emptyList();
    }

    public OscMessage(String address) {
        this.address = address;
        this.arguments = Collections.emptyList();
    }

    public OscMessage(String address, List<OscType> arguments) {
        this.address = address;
        this.arguments = arguments;
    }

    public OscMessage(String address, Object... objArguments) {
        this.address = address;

        this.arguments = new ArrayList<>(objArguments.length);
        for (Object obj : objArguments) {
            if (obj instanceof String) {
                this.arguments.add(new OscString((String) obj));
            } else if (obj instanceof Integer) {
                this.arguments.add(new OscInt((Integer) obj));
            } else if (obj instanceof Float) {
                this.arguments.add(new OscFloat((Float) obj));
            }
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<OscType> getArguments() {
        return arguments;
    }

    public void setArguments(List<OscType> arguments) {
        this.arguments = arguments;
    }

    @Override
    public void read(ByteBuffer buffer) throws OscReadException {
        OscString oscAddress = new OscString();
        OscString oscTypeTags = new OscString();

        oscAddress.read(buffer);

        this.address = oscAddress.getValue();

        if (buffer.hasRemaining()) {
            if (OscUtils.isTypeTagSeparator(buffer)) {
                buffer.get();

                oscTypeTags.read(buffer);
            } else {
                throw new OscReadException("No typetag separator after message address");
            }
        }

        String typeTags = oscTypeTags.getValue();

        this.arguments = new ArrayList<>(typeTags.length());
        for (int i = 0; i < typeTags.length(); i++) {
            Class<? extends OscType> typeClass = TYPETAG_ASSOCIATIONS.get(typeTags.charAt(i));

            if (typeClass == null) {
                throw new OscReadException("Unsupported osc type: " + typeTags.charAt(i));
            }
            try {
                OscType typeValue = typeClass.getConstructor().newInstance();
                typeValue.read(buffer);

                this.arguments.add(typeValue);
            } catch (Exception ex) {
                throw new OscReadException("Unable to instantiate OscType");
            }
        }
    }

    @Override
    public void write(ByteBuffer buffer) throws OscWriteException {
        OscString oscAddress = new OscString(this.address);
        OscString oscTypeTags = new OscString();

        oscAddress.write(buffer);

        OscUtils.writeTypeTagSeparator(buffer);

        StringBuilder typeTagBuilder = new StringBuilder();
        for (OscType argument : arguments) {
            Character typeTag = TYPECLASS_ASSOCIATIONS.get(argument.getClass());

            if (typeTag == null) {
                throw new OscWriteException("Unsupported osc type:" + argument.getClass().getCanonicalName());
            }

            typeTagBuilder.append(typeTag);
        }

        oscTypeTags.setValue(typeTagBuilder.toString());
        oscTypeTags.write(buffer);

        for (OscType argument : arguments) {
            argument.write(buffer);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(address);

        sb.append(", ");

        for (OscType arg : arguments) {
            sb.append(arg.toString());
            sb.append(" ");
        }

        return sb.toString();
    }

}
