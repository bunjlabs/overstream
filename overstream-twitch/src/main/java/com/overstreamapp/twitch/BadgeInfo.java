package com.overstreamapp.twitch;

public class BadgeInfo {

    private final String type;
    private final int version;
    private final String url;

    public BadgeInfo(String type, int version, String url) {
        this.type = type;
        this.version = version;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }
}
