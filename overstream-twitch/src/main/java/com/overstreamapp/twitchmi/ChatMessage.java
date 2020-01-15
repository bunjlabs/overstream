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

package com.overstreamapp.twitchmi;

import java.util.List;

public class ChatMessage {

    private String id;
    private String channelName;
    private String userId;
    private String userName;
    private String userColor;
    private int userLevel;
    private long timestamp;
    private String text;
    private List<BadgeInfo> badges;
    private List<MessageContent> content;

    public ChatMessage() {
    }

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

    public void setId(String id) {
        this.id = id;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserColor(String userColor) {
        this.userColor = userColor;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBadges(List<BadgeInfo> badges) {
        this.badges = badges;
    }

    public void setContent(List<MessageContent> content) {
        this.content = content;
    }
}
