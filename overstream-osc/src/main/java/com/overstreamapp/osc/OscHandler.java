package com.overstreamapp.osc;

import com.overstreamapp.osc.types.OscBundle;
import com.overstreamapp.osc.types.OscMessage;

public interface OscHandler {

    void onMessage(OscChannel channel, OscMessage message);

    void onBundle(OscChannel channel, OscBundle bundle);
}
