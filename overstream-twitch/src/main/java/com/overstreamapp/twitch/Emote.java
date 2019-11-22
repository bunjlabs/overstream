package com.overstreamapp.twitch;

public class Emote {
    private final String id;
    private final String code;
    private final int start;
    private final int end;
    private final String url;

    public Emote(String id, String code, int start, int end, String url) {
        this.id = id;
        this.code = code;
        this.start = start;
        this.end = end;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getUrl() {
        return url;
    }
}
