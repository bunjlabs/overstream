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

package com.overstreamapp.twitchmi.support;

import com.overstreamapp.twitchmi.TwitchMiTrigger;
import com.overstreamapp.twitchmi.TwitchMiTriggerBuilder;
import com.overstreamapp.twitchmi.TwitchMiTriggerMode;
import com.overstreamapp.twitchmi.domain.ChatMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class DefaultTwitchMiTriggerBuilder implements TwitchMiTriggerBuilder {

    private final DefaultTwitchMi twitchMi;
    private final Set<String> aliases = new HashSet<>();

    private TwitchMiTriggerMode mode = TwitchMiTriggerMode.PREFIX;
    private Consumer<ChatMessage> consumer = m -> {
    };

    DefaultTwitchMiTriggerBuilder(DefaultTwitchMi twitchMi, Iterable<String> aliases) {
        this.twitchMi = twitchMi;
        aliases.forEach(this.aliases::add);
    }

    @Override
    public TwitchMiTriggerBuilder alias(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    @Override
    public TwitchMiTriggerBuilder alias(Iterable<String> aliases) {
        aliases.forEach(this.aliases::add);
        return this;
    }

    @Override
    public TwitchMiTriggerBuilder mode(TwitchMiTriggerMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public TwitchMiTriggerBuilder consumer(Consumer<ChatMessage> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public TwitchMiTrigger build() {
        AbstractTrigger trigger;

        switch (mode) {
            case EQUALS:
                trigger = new EqualsTrigger(twitchMi, aliases, consumer);
                break;
            case CONTAINS:
                trigger = new ContainsTrigger(twitchMi, aliases, consumer);
                break;
            case PREFIX:
            default:
                trigger = new PrefixTrigger(twitchMi, aliases, consumer);
                break;
            case SUFFIX:
                trigger = new SuffixTrigger(twitchMi, aliases, consumer);
                break;
            case REGEX:
                trigger = new RegexTrigger(twitchMi, aliases, consumer);
                break;
        }

        twitchMi.registerChatTrigger(aliases, trigger);

        return trigger;
    }
}
