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
