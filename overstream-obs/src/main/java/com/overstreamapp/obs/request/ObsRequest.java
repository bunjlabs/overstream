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

package com.overstreamapp.obs.request;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public abstract class ObsRequest {

    @SerializedName("request-type")
    private String requestType;

    @SerializedName("message-id")
    private String messageId;

    public ObsRequest(String requestType) {
        this.requestType = requestType;
        this.messageId = requestType + "-" + UUID.randomUUID().toString();
    }

    public String getRequestType() {
        return requestType;
    }

    public String getMessageId() {
        return messageId;
    }
}
