package dev.radioheads.airwaves.network;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioStation;
import dev.radioheads.airwaves.api.util.number.RangedFloat;
import dev.radioheads.airwaves.network.payload.RadioLinkPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class RadioVoice {
    public static final float SPEAKER_RANGE = 24.0F;
    private static final double MIC_RANGE = 2.5;
    private static final long SPEAKING_HOLD_MS = 400L;
    private static final int LINK_RESEND_TICKS = 20;

    private static final Map<UUID, Long> LAST_SPOKEN = new ConcurrentHashMap<>();
    private static final CaptureTable CAPTURE = new CaptureTable();
    private static volatile Map<UUID, Feed> feeds = Map.of();

    public record Feed(UUID station, @Nullable UUID microphone, List<Delivery> deliveries) {
        public void spoke() {
            RadioVoice.spoke(this.station);
            if (this.microphone != null) {
                RadioVoice.spoke(this.microphone);
            }
        }
    }

    private record Source(RadioStation station, @Nullable UUID microphone, float gain) {}

    public record Delivery(UUID listener, UUID receiver, Vec3 speaker, RadioLinkPayload.Kind kind,
                           double marginDb, boolean squelched, Holder<Modulation> modulation, float gain) {
        public boolean passes(UUID talker) {
            return CAPTURE.passes(this.receiver, talker, this.marginDb, this.modulation.value().captureDb(), System.currentTimeMillis());
        }
    }

    private RadioVoice() {}

    @Nullable
    public static Feed feedOf(UUID talker) {
        return feeds.get(talker);
    }

    public static void spoke(UUID station) {
        LAST_SPOKEN.put(station, System.currentTimeMillis());
    }

    public static boolean speaking(UUID station) {
        Long last = LAST_SPOKEN.get(station);
        return last != null && System.currentTimeMillis() - last < SPEAKING_HOLD_MS;
    }

    public static UUID channel(UUID talker, UUID receiver) {
        return new UUID(talker.getMostSignificantBits() ^ Long.rotateLeft(receiver.getLeastSignificantBits(), 21),
                talker.getLeastSignificantBits() ^ Long.rotateLeft(receiver.getMostSignificantBits(), 21));
    }

    private static Optional<Source> sourceOf(ServerPlayer talker, Collection<RadioStation> stations) {
        Vec3 mouth = talker.getEyePosition();
        RadioStation nearest = null;
        double closest = MIC_RANGE;
        for (RadioStation station : stations) {
            double distance = station.position().distanceTo(mouth);
            if (station.transmits() && distance <= closest) {
                nearest = station;
                closest = distance;
            }
        }
        if (nearest != null) {
            return Optional.of(new Source(nearest, null, 1.0F));
        }

        return Microphones.hearing(talker.level(), mouth, BlockPos.containing(mouth)).stream().findFirst()
                .flatMap(microphone -> microphone.station().map(station -> new Source(station, microphone.id(), microphone.gain())));
    }

    // Gain is the source output level
    public static Feed feed(ServerLevel level, UUID station, @Nullable UUID microphone, float gain) {
        List<Delivery> deliveries = new ArrayList<>();
        for (RadioNetwork.Route route : RadioNetwork.routes(level, station)) {
            RadioStation receiver = route.receiver();
            float quality = Math.clamp(receiver.modulation().value().quality(route.marginDb()), 0.0F, 1.0F);
            boolean squelched = !receiver.opensSquelch(quality);
            Vec3 speaker = receiver.position();
            for (ServerPlayer player : level.players()) {
                if (player.position().closerThan(speaker, SPEAKER_RANGE)) {
                    deliveries.add(new Delivery(player.getUUID(), receiver.id(), speaker, RadioLinkPayload.Kind.SINK,
                            route.marginDb(), squelched, receiver.modulation(), receiver.volume() * gain));
                }
            }
        }
        return new Feed(station, microphone, deliveries);
    }

    private static void sendLinks(ServerLevel level, UUID talker, Feed feed) {
        for (Delivery delivery : feed.deliveries()) {
            if (!delivery.squelched() && level.getPlayerByUUID(delivery.listener()) instanceof ServerPlayer listener) {
                new RadioLinkPayload(
                        channel(talker, delivery.receiver()),
                        delivery.kind(),
                        RangedFloat.clamped(delivery.modulation().value().quality(delivery.marginDb()), 0.0F, 1.0F),
                        delivery.modulation(),
                        delivery.gain(),
                        level.getGameTime()
                ).sendTo(listener);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        boolean resend = event.getServer().getTickCount() % LINK_RESEND_TICKS == 0;
        Map<UUID, Feed> previous = feeds;
        Map<UUID, Feed> next = new HashMap<>();

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer talker : level.players()) {
                sourceOf(talker, RadioNetwork.stations(level)).ifPresent(source -> {
                    Feed feed = feed(level, source.station().id(), source.microphone(), source.gain());
                    next.put(talker.getUUID(), feed);
                    if (resend || !previous.containsKey(talker.getUUID())) {
                        sendLinks(level, talker.getUUID(), feed);
                    }
                });
            }
        }

        feeds = next;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        feeds = Map.of();
        LAST_SPOKEN.clear();
        CAPTURE.clear();
    }
}
