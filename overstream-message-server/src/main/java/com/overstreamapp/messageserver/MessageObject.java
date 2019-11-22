package com.overstreamapp.messageserver;

import com.overstreamapp.messageserver.messages.Message;

class MessageObject {

    private final String type;
    private final Message message;

    MessageObject(Message message) {
        this.type = message.typeName();
        this.message = message;
    }
}
