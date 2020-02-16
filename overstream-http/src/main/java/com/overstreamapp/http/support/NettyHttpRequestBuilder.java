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

import com.overstreamapp.http.HttpHandler;
import com.overstreamapp.http.HttpRequestBuilder;
import io.netty.handler.codec.http.*;

import java.net.URL;
import java.util.function.BiConsumer;

class NettyHttpRequestBuilder implements HttpRequestBuilder {
    private final FullHttpRequest request;
    private final URL url;
    private final AbstractHttpClient client;

    NettyHttpRequestBuilder(HttpMethod method, URL url, AbstractHttpClient client) {
        this.request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, url.getPath());
        this.url = url;
        this.client = client;
    }

    @Override
    public HttpRequestBuilder header(String name, Object value) {
        request.headers().add(name, value);
        return this;
    }

    @Override
    public void execute() {
        execute((HttpHandler) null);
    }

    @Override
    public void execute(HttpHandler handler) {
        var ssl = "https".equalsIgnoreCase(url.getProtocol());
        var host = url.getHost() == null ? "localhost" : url.getHost();
        var port = url.getPort() <= 0 ? (ssl ? 443 : 80) : url.getPort();
        var connectionPoint = new ConnectionPoint(host, port, ssl);

        client.execute(connectionPoint, request, handler);
    }

    @Override
    public void execute(BiConsumer<FullHttpResponse, Throwable> consumer) {
        execute(new HttpHandler() {

            @Override
            public void onResponse(FullHttpResponse response) {
                consumer.accept(response, null);
            }

            @Override
            public void onError(Throwable cause) {
                consumer.accept(null, cause);
            }
        });
    }
}
