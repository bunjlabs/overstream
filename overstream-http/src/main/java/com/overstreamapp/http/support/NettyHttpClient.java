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
import com.overstreamapp.http.*;
import com.overstreamapp.network.EventLoopGroupManager;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyHttpClient extends AbstractHttpClient {

    private final Logger logger;
    private final EventLoopGroupManager loopGroupManager;
    private final Map<ConnectionPoint, NettyHttpConnection> connectionPool;

    @Inject
    public NettyHttpClient(Logger logger, EventLoopGroupManager loopGroupManager) {
        this.logger = logger;
        this.loopGroupManager = loopGroupManager;
        this.connectionPool = new ConcurrentHashMap<>();
    }

    @Override
    void execute(ConnectionPoint connectionPoint, FullHttpRequest request, HttpHandler handler) {
        var context = new InternalContext(this, connectionPoint, request, handler);
        var connection = getConnection(connectionPoint);

        connection.execute(context);
    }

    private NettyHttpConnection getConnection(ConnectionPoint connectionPoint) {
        var connection = connectionPool.get(connectionPoint);

        if (connection == null) {
            connection = new NettyHttpConnection(logger, loopGroupManager.getWorkerEventLoopGroup(), connectionPoint);
            connectionPool.put(connectionPoint, connection);
        }

        return connection;
    }
}
