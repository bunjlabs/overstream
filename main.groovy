
import com.overstreamapp.OverStreamApp
import com.overstreamapp.x32mixer.X32MixerClient

def app = injector.getInstance(OverStreamApp.class)
def x32MixerClient = app.getInjector().getInstance(X32MixerClient.class)

x32MixerClient.subscribeChannelOn(1, 15, 19)
x32MixerClient.subscribeChannelGate(1, 19)
