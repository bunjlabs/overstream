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

import com.bunjlabs.fuga.util.Assert;

import java.util.Objects;

class ConnectionPoint {

    private final String host;
    private final int port;
    private final boolean ssl;

    ConnectionPoint(String host, int port, boolean ssl) {
        Assert.isTrue(port >= 1 && port <= 65535);
        this.host = Assert.hasText(host);
        this.port = port;
        this.ssl = ssl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSsl() {
        return ssl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionPoint that = (ConnectionPoint) o;
        return port == that.port &&
                ssl == that.ssl &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, ssl);
    }
}
