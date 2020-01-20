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

package com.overstreamapp.http.support;

import com.overstreamapp.http.HttpMethod;
import com.overstreamapp.http.HttpRequest;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class InternalHttpRequest implements HttpRequest {

    private final HttpMethod method;
    private final URI uri;
    private final Map<String, String> headers = new HashMap<>();

    InternalHttpRequest(HttpMethod method, URI uri) {
        this.method = method;
        this.uri = uri;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }


}
