import com.overstreamapp.messageserver.MessageServer
import com.overstreamapp.statemanager.StateManager
import com.overstreamapp.x32mixer.X32MixerClient
import com.overstreamapp.mpd.YmpdClient
import com.overstreamapp.twitch.TwitchMi
import com.overstreamapp.twitchbot.TwitchBot

def stateManager = injector.getInstance(StateManager.class)
def messageServer = injector.getInstance(MessageServer.class)
def x32MixerClient = injector.getInstance(X32MixerClient.class)
def ympd = injector.getInstance(YmpdClient.class)
def twitch = injector.getInstance(TwitchMi.class)
def twitchBot = injector.getInstance(TwitchBot.class)

// Start Web Socket server
messageServer.start()

// Configure local X32 mixer subscribes
x32MixerClient.connect()
x32MixerClient.subscribeChannelOn(1, 15, 19)
x32MixerClient.subscribeChannelGate(1, 19)

// Connect to local ympd server
ympd.connect()

// Connect to Twitch MI interface
twitch.connect()

// Enable twitch bot
twitchBot.enable();