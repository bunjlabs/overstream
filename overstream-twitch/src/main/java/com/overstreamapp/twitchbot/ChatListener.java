package com.overstreamapp.twitchbot;

import com.overstreamapp.twitch.ChatMessage;

public interface ChatListener {
    void onChat(ChatMessage message);
}
