package dev.radioheads.airwaves.integration.svc;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.client.RadioAudioFilter;
import dev.radioheads.airwaves.network.RadioLinkStore;
import dev.radioheads.airwaves.network.RadioVoice;
import dev.radioheads.airwaves.network.payload.RadioLinkPayload;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ForgeVoicechatPlugin
public class AirwavesVoicechatPlugin implements VoicechatPlugin {
    private final Map<UUID, RadioAudioFilter> filters = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return Airwaves.MOD_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
        registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, this::onReceive);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatConnection sender = event.getSenderConnection();
        if (sender == null) {
            return;
        }

        UUID talker = sender.getPlayer().getUuid();
        RadioVoice.Feed feed = RadioVoice.feedOf(talker);
        if (feed == null) {
            return;
        }

        feed.spoke();
        for (RadioVoice.Delivery delivery : feed.deliveries()) {
            if (delivery.passes(talker) && !delivery.squelched() && !delivery.listener().equals(talker)) {
                deliver(event.getVoicechat(), event.getPacket(), RadioVoice.channel(talker, delivery.receiver()), delivery);
            }
        }
    }

    private void deliver(VoicechatServerApi api, MicrophonePacket packet, UUID channel, RadioVoice.Delivery delivery) {
        VoicechatConnection connection = api.getConnectionOf(delivery.listener());
        if (connection == null) {
            return;
        }

        Vec3 speaker = delivery.speaker();
        api.sendLocationalSoundPacketTo(connection, packet.locationalSoundPacketBuilder()
                .channelId(channel)
                .position(api.createPosition(speaker.x, speaker.y, speaker.z))
                .distance(RadioVoice.SPEAKER_RANGE)
                .build());
    }

    private void onReceive(ClientReceiveSoundEvent event) {
        short[] audio = event.getRawAudio();
        Optional<RadioLinkPayload> link = RadioLinkStore.get(event.getId());
        if (audio != null && link.isPresent()) {
            RadioLinkPayload payload = link.get();
            RadioAudioFilter filter = this.filters.computeIfAbsent(event.getId(), id -> new RadioAudioFilter());
            event.setRawAudio(filter.process(audio, payload.modulation().value(), payload.quality().value(), payload.gain()));
        }
    }
}
