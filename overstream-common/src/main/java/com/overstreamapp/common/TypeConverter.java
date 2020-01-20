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

package com.overstreamapp.common;

import com.bunjlabs.fuga.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

public class TypeConverter {
    private final Logger logger;
    private final ObjectMapper objectMapper;

    @Inject
    public TypeConverter(Logger logger, ObjectMapper objectMapper) {
        this.logger = logger;
        this.objectMapper = objectMapper;
    }

    public String objectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            logger.error("Unable to convert object {} to json: {}", object, ex);
        }

        return "{}";
    }

    public <T> T jsonToObject(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (Exception ex) {
            logger.error("Unable to convert json to object {}: {}", valueType, ex);
        }

        return null;
    }
}
