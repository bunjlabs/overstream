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

package com.overstreamapp.http;

import java.net.MalformedURLException;
import java.net.URL;

public interface HttpClient {

    default HttpRequestBuilder get(String url) throws MalformedURLException {
        return get(new URL(url));
    }

    HttpRequestBuilder get(URL url);

    default HttpRequestBuilder head(String url) throws MalformedURLException {
        return head(new URL(url));
    }

    HttpRequestBuilder head(URL url);

    default HttpRequestBuilder put(String url) throws MalformedURLException {
        return put(new URL(url));
    }

    HttpRequestBuilder put(URL url);

    default HttpRequestBuilder post(String url) throws MalformedURLException {
        return post(new URL(url));
    }

    HttpRequestBuilder post(URL url);

    default HttpRequestBuilder delete(String url) throws MalformedURLException {
        return delete(new URL(url));
    }

    HttpRequestBuilder delete(URL url);

    default HttpRequestBuilder options(String url) throws MalformedURLException {
        return options(new URL(url));
    }

    HttpRequestBuilder options(URL url);
}
