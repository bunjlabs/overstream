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

public class BadgeInfo {

    private final String type;
    private final int version;
    private final String url;

    public BadgeInfo(String type, int version, String url) {
        this.type = type;
        this.version = version;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }
}
