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

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.http.HttpClient;
import com.overstreamapp.http.HttpMethod;
import com.overstreamapp.http.HttpRequestBuilder;
import com.overstreamapp.network.EventLoopGroupManager;
import org.slf4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NettyHttpClient implements HttpClient {
    private final Map<ConnectionPoint, NettyHttpConnection> connectionPool = new HashMap<>();

    private final Logger logger;
    private final EventLoopGroupManager loopGroupManager;

    @Inject
    public NettyHttpClient(Logger logger, EventLoopGroupManager loopGroupManager) {
        this.logger = logger;
        this.loopGroupManager = loopGroupManager;
    }

    @Override
    public HttpRequestBuilder get(URL url) {
        return createBuilder(HttpMethod.GET, url);
    }

    @Override
    public HttpRequestBuilder head(URL url) {
        return createBuilder(HttpMethod.HEAD, url);
    }

    @Override
    public HttpRequestBuilder put(URL url) {
        return createBuilder(HttpMethod.PUT, url);
    }

    @Override
    public HttpRequestBuilder post(URL url) {
        return createBuilder(HttpMethod.POST, url);
    }

    @Override
    public HttpRequestBuilder delete(URL url) {
        return createBuilder(HttpMethod.DELETE, url);
    }

    @Override
    public HttpRequestBuilder options(URL url) {
        return createBuilder(HttpMethod.OPTIONS, url);
    }

    private HttpRequestBuilder createBuilder(HttpMethod method, URL url) {
        return new NettyHttpRequestBuilder(method, url, this);
    }

    NettyHttpConnection getConnection(ConnectionPoint connectionPoint) {
        var connection = connectionPool.get(connectionPoint);

        if (connection == null) {
            connection = new NettyHttpConnection(logger, this, loopGroupManager.getWorkerEventLoopGroup(), connectionPoint);
            connectionPool.put(connectionPoint, connection);
        }

        return connection;
    }
}
