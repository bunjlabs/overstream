package com.overstreamapp.statemanager;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("state-manager")
public interface StateManagerSettings {

    @SettingName("mongo-client")
    MongoClientSettings mongoClient();

    interface MongoClientSettings {

        @SettingDefault("localhost")
        String host();

        @SettingDefault("27017")
        int port();


        @SettingDefault("overstream")
        String database();
    }
}
