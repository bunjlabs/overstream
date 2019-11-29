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

public class Emote {
    private final String id;
    private final String code;
    private final int start;
    private final int end;
    private final String url;

    public Emote(String id, String code, int start, int end, String url) {
        this.id = id;
        this.code = code;
        this.start = start;
        this.end = end;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getUrl() {
        return url;
    }
}
