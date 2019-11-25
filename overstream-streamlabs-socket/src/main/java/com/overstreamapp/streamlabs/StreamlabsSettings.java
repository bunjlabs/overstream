package com.overstreamapp.streamlabs;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("streamlabs")
public interface StreamlabsSettings {

    @SettingName("server-uri")
    @SettingDefault("wss://sockets.streamlabs.com/socket.io/?token=%s&EIO=3&transport=websocket")
    String serverUri();

    @SettingName("socket-token")
    String socketToken();
}
