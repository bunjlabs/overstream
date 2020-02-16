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

import com.overstreamapp.twitchmi.domain.ChatMessage;

import java.util.Set;
import java.util.function.Consumer;

class EqualsTrigger extends AbstractTrigger {

    EqualsTrigger(DefaultTwitchMi twitchMi, Set<String> aliases, Consumer<ChatMessage> consumer) {
        super(twitchMi, aliases, consumer);
    }

    @Override
    boolean check(String message) {
        return aliases.stream().anyMatch(message::equalsIgnoreCase);
    }
}
