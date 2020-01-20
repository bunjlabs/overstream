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

package com.overstreamapp.twitchpubsub.domain;

public enum PubSubTopic {

    BITS("channel-bits-events-v2."),
    BITS_BADGE("channel-bits-badge-unlocks."),
    CHANNEL_POINTS("channel-points-channel-v1."),
    CHANNEL_SUBSCRIPTIONS("channel-subscribe-events-v1."),
    COMMERCE("channel-commerce-events-v1.");

    private final String topic;

    PubSubTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic(String channelId) {
        return topic + channelId;
    }

    public boolean isSame(String topic) {
        return topic != null && topic.startsWith(this.topic);
    }
}
