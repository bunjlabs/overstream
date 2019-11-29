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

package com.overstreamapp.twitch;

public abstract class TMIUtils {

    private final static String COLOR_HEX_LETERS = "0123456789ABCDEF";

    public static String getRandomColor() {
        StringBuilder sb = new StringBuilder("#");

        for (int i = 0; i < 6; i++) {
            sb.append(COLOR_HEX_LETERS.charAt((int) Math.floor(Math.random() * 16)));
        }
        return sb.toString();
    }

    public static String stringToColor(String str) {
        int hash = str.hashCode();

        StringBuilder color = new StringBuilder("#");
        for (int i = 0; i < 2; i++) {
            int value = (hash >> (i * 8)) & 0xFF;
            color.append(String.format("%02X", value).toUpperCase());
        }
        return color.append("FF").toString();
    }
}
