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
import com.overstreamapp.http.HttpRequestBuilder;
import com.overstreamapp.http.ResponseFuture;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class NettyHttpRequestBuilder implements HttpRequestBuilder {
    private final Map<String, String> headers = new HashMap<>();
    private final HttpMethod method;
    private final URL url;
    private final NettyHttpClient client;

    NettyHttpRequestBuilder(HttpMethod method, URL url, NettyHttpClient client) {
        this.method = method;
        this.url = url;
        this.client = client;
    }

    @Override
    public HttpRequestBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    @Override
    public ResponseFuture execute() {
        try {
        var request = new InternalHttpRequest(method, url.toURI());

        request.setHeaders(headers);

        var host = url.getHost() == null ? "localhost" : url.getHost();
        var port = url.getPort() <= 0 ? 80 : url.getPort();
        var ssl = "https".equalsIgnoreCase(url.getProtocol());
        var connectionPoint = new ConnectionPoint(host, port, ssl);

        var connection = client.getConnection(connectionPoint);

        connection.start(request);

        return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
