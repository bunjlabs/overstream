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

package com.overstreamapp.mongodb;

import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.fuga.inject.Provider;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDatabaseProvider implements Provider<MongoDatabase> {

    private final MongoSettings settings;
    private final MongoClient mongoClient;

    @Inject
    public MongoDatabaseProvider(MongoSettings settings, MongoClient mongoClient) {
        this.settings = settings;
        this.mongoClient = mongoClient;
    }

    @Override
    public MongoDatabase get() {
        return mongoClient.getDatabase(settings.database());
    }
}
