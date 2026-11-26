package dev.radioheads.airwaves.network;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.antenna.AntennaProfile;
import dev.radioheads.airwaves.api.audio.AudioRecorder;
import dev.radioheads.airwaves.api.link.LinkEnd;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.radio.RadioStation;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.network.payload.RadioSongPayload;
import dev.radioheads.airwaves.network.payload.RadioSoundPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.function.Predicate;

// Game sounds and jukebox songs that microphones send over the air
@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class RadioSounds {
    private static final int SONG_SCAN_TICKS = 5;
    // Smallest level change worth sending
    private static final float LEVEL_STEP = 0.01F;

    private static final Map<UUID, Relay> RELAYS = new HashMap<>();

    private record Playing(BlockPos jukebox, JukeboxSong song, long ticks) {}

    private record Track(UUID listener, UUID channel) {}

    private static final class Relay {
        private final Playing playing;
        private long ticks;
        // Level last sent to each track
        private final Map<Track, Float> tracks = new HashMap<>();

        private Relay(Playing playing) {
            this.playing = playing;
            this.ticks = playing.ticks();
        }

        private boolean carries(Playing playing) {
            return this.playing.jukebox().equals(playing.jukebox()) && this.playing.song().equals(playing.song()) && playing.ticks() >= this.ticks;
        }
    }

    private RadioSounds() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSoundAtPosition(PlayLevelSoundEvent.AtPosition event) {
        if (!event.isCanceled() && event.getSound() != null && event.getLevel() instanceof ServerLevel level) {
            relay(level, event.getSound(), event.getSource(), event.getPosition(), event.getNewVolume(), event.getNewPitch());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
        if (!event.isCanceled() && event.getSound() != null && event.getLevel() instanceof ServerLevel level) {
            relay(level, event.getSound(), event.getSource(), event.getEntity().getBoundingBox().getCenter(), event.getNewVolume(), event.getNewPitch());
        }
    }

    private static void relay(ServerLevel level, Holder<SoundEvent> sound, SoundSource source, Vec3 position, float volume, float pitch) {
        Set<UUID> stations = new HashSet<>();
        for (AudioRecorder recorder : Microphones.hearing(level, position, BlockPos.containing(position))) {
            Optional<RadioStation> station = recorder.station();
            if (station.isEmpty() || !stations.add(station.get().id())) {
                continue;
            }

            RadioVoice.Feed feed = RadioVoice.feed(level, station.get().id(), recorder.id(), recorder.gain());
            feed.spoke();
            for (RadioVoice.Delivery delivery : feed.deliveries()) {
                float quality = quality(delivery);
                if (quality > 0.0F && level.getPlayerByUUID(delivery.listener()) instanceof ServerPlayer listener) {
                    new RadioSoundPayload(sound, source, delivery.speaker(), volume * quality * delivery.gain(), pitch, false).sendTo(listener);
                }
            }
        }
    }

    public static void broadcast(ServerLevel level, BlockPos pos, Holder<SoundEvent> sound, SoundSource category,
                                 Holder<RadioBand> band, long fromHertz, long toHertz, float volume, float pitch) {
        if (!level.isLoaded(pos) || !(volume > 0.0F) || !(pitch > 0.0F)) {
            return;
        }

        long low = band.value().clamp(Math.min(fromHertz, toHertz));
        long high = band.value().clamp(Math.max(fromHertz, toHertz));
        Predicate<RadioStation> target = station -> station.receives()
                && station.hertz() >= low && station.hertz() <= high
                && band.value().modulation().value().equals(station.modulation().value());

        Set<Long> frequencies = new TreeSet<>();
        for (RadioStation station : RadioNetwork.stations(level)) {
            if (target.test(station)) {
                frequencies.add(station.hertz());
            }
        }

        for (long hertz : frequencies) {
            BroadcastStation transmitter = new BroadcastStation(pos, new Tuning(hertz, band));
            List<RadioNetwork.Route> routes = RadioNetwork.routesTo(level, transmitter, target).getOrDefault(transmitter.id(), List.of());
            for (RadioNetwork.Route route : routes) {
                RadioStation receiver = route.receiver();
                float quality = quality(receiver, route.marginDb());
                if (quality <= 0.0F) {
                    continue;
                }
                float loudness = volume * quality * receiver.volume();
                if (loudness <= 0.0F) {
                    continue;
                }
                Vec3 speaker = receiver.position();
                for (ServerPlayer listener : level.players()) {
                    if (listener.position().closerThan(speaker, RadioVoice.SPEAKER_RANGE)) {
                        new RadioSoundPayload(sound, category, speaker, loudness, pitch, false).sendTo(listener);
                    }
                }
            }
        }
    }

    private static float quality(RadioStation receiver, double marginDb) {
        float quality = Mth.clamp(receiver.modulation().value().quality(marginDb), 0.0F, 1.0F);
        return receiver.opensSquelch(quality) ? quality : 0.0F;
    }

    // Temporary
    // I mean it works. It's a virtual radio station
    private static final class BroadcastStation implements RadioStation {
        private static final double BROADCAST_WATTS = 100.0;
        private static final double LIGHT = 300.0;

        private final BlockPos position;
        private final Tuning tuning;
        private final AntennaProfile antenna;
        private final UUID id;

        private BroadcastStation(BlockPos position, Tuning tuning) {
            this.position = position;
            this.tuning = tuning;
            double mhz = tuning.hertz() / 1.0E6;
            double length = Mth.clamp(LIGHT / mhz / 4.0, 0.25, 8.0);
            this.antenna = AntennaProfile.whip(length, Vec3.atCenterOf(position).add(0.0, length, 0.0));
            this.id = new UUID(tuning.hertz(), position.asLong());
        }

        @Override
        public UUID id() {
            return this.id;
        }

        @Override
        public Vec3 antenna() {
            return this.antenna.radiator();
        }

        @Override
        public Vec3 position() {
            return Vec3.atCenterOf(this.position);
        }

        @Override
        public long hertz() {
            return this.tuning.hertz();
        }

        @Override
        public Holder<Modulation> modulation() {
            return this.tuning.band().value().modulation();
        }

        @Override
        public LinkEnd linkEnd(double mhz, double heightBlocks, Vec3 toward) {
            return this.antenna.linkEnd(BROADCAST_WATTS, 0.0, 0.0, heightBlocks, mhz, toward);
        }

        @Override
        public boolean transmits() {
            return true;
        }

        @Override
        public boolean onAir() {
            return true;
        }
    }

    private static float quality(RadioVoice.Delivery delivery) {
        return delivery.squelched() ? 0.0F : Mth.clamp(delivery.modulation().value().quality(delivery.marginDb()), 0.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % SONG_SCAN_TICKS != 0) {
            return;
        }

        Set<UUID> listening = new HashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (AudioRecorder recorder : Microphones.in(level)) {
                listening.add(recorder.id());
                listen(level, recorder);
            }
        }

        RELAYS.entrySet().removeIf(entry -> {
            if (listening.contains(entry.getKey())) {
                return false;
            }
            stop(server, entry.getValue());
            return true;
        });
    }

    private static void listen(ServerLevel level, AudioRecorder recorder) {
        UUID id = recorder.id();
        Optional<RadioStation> station = recorder.station();
        boolean live = recorder.active();
        Optional<Playing> playing = live ? playing(level, recorder) : Optional.empty();

        Relay relay = RELAYS.get(id);
        if (relay != null && (playing.isEmpty() || !relay.carries(playing.get()))) {
            stop(level.getServer(), relay);
            RELAYS.remove(id);
            relay = null;
        }
        if (playing.isEmpty()) {
            return;
        }
        if (relay == null) {
            relay = new Relay(playing.get());
            RELAYS.put(id, relay);
        }
        relay.ticks = playing.get().ticks();

        RadioVoice.Feed feed = RadioVoice.feed(level, station.get().id(), id, recorder.gain());
        feed.spoke();
        Set<Track> reached = new HashSet<>();
        for (RadioVoice.Delivery delivery : feed.deliveries()) {
            if (delivery.squelched() || !(level.getPlayerByUUID(delivery.listener()) instanceof ServerPlayer listener)) {
                continue;
            }

            Track track = new Track(delivery.listener(), RadioVoice.channel(id, delivery.receiver()));
            // Signal quality times AF and the microphone knob
            float speakerLevel = quality(delivery) * delivery.gain();
            Float sent = relay.tracks.get(track);
            if (sent == null && speakerLevel <= 0.0F) {
                continue;
            }
            reached.add(track);
            if (sent == null || Math.abs(speakerLevel - sent) >= LEVEL_STEP || (speakerLevel == 0.0F) != (sent == 0.0F)) {
                relay.tracks.put(track, speakerLevel);
                new RadioSongPayload(track.channel(), Optional.of(relay.playing.song().soundEvent()), delivery.speaker(),
                        speakerLevel, relay.ticks, false).sendTo(listener);
            }
        }

        relay.tracks.keySet().removeIf(track -> {
            if (reached.contains(track)) {
                return false;
            }
            stop(level.getServer(), track);
            return true;
        });
    }

    // Jukebox in earshot of the capsule
    private static Optional<Playing> playing(ServerLevel level, AudioRecorder recorder) {
        Vec3 capsule = recorder.capsule();
        int reach = Mth.ceil(Microphones.RANGE);
        BlockPos centre = BlockPos.containing(capsule);
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-reach, -reach, -reach), centre.offset(reach, reach, reach))) {
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                JukeboxSongPlayer player = jukebox.getSongPlayer();
                JukeboxSong song = player.getSong();
                if (song != null && Microphones.hears(Microphones.shapes(level), capsule, recorder.getBlockPos(), Vec3.atCenterOf(pos), pos)) {
                    return Optional.of(new Playing(pos.immutable(), song, player.getTicksSinceSongStarted()));
                }
            }
        }
        return Optional.empty();
    }

    private static void stop(MinecraftServer server, Relay relay) {
        relay.tracks.keySet().forEach(track -> stop(server, track));
        relay.tracks.clear();
    }

    private static void stop(MinecraftServer server, Track track) {
        ServerPlayer listener = server.getPlayerList().getPlayer(track.listener());
        if (listener != null) {
            RadioSongPayload.stop(track.channel()).sendTo(listener);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        RELAYS.clear();
    }
}
