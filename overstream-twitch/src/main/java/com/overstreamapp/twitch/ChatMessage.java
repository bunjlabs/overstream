package com.overstreamapp.twitch;

import java.util.List;

public class ChatMessage {

    private final String id;
    private final String channelName;
    private final String userId;
    private final String userName;
    private final String userColor;
    private final int userLevel;
    private final long timestamp;
    private final String text;
    private final List<BadgeInfo> badges;
    private final List<MessageContent> content;

    public ChatMessage(String id, String channelName, String userId, String userName, String userColor, int userLevel, long timestamp, String text, List<BadgeInfo> badges, List<MessageContent> content) {
        this.id = id;
        this.channelName = channelName;
        this.userId = userId;
        this.userName = userName;
        this.userColor = userColor;
        this.userLevel = userLevel;
        this.timestamp = timestamp;
        this.text = text;
        this.badges = badges;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserColor() {
        return userColor;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public List<BadgeInfo> getBadges() {
        return badges;
    }

    public List<MessageContent> getContent() {
        return content;
    }
}
