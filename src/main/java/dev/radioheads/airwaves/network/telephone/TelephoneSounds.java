package dev.radioheads.airwaves.network.telephone;

import dev.radioheads.airwaves.Airwaves;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Sounds down the line
@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class TelephoneSounds {
    private static final int SONG_SCAN_TICKS = 5;

    private static final Map<Track, Playing> SONGS = new HashMap<>();

    private record Track(UUID listener, UUID channel) {}

    private record Playing(BlockPos jukebox, JukeboxSong song) {}

    private TelephoneSounds() {}

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
        for (Telephones.Leg leg : Telephones.legs()) {
            if (leg.level() == level && leg.from().getEyePosition().closerThan(position, Telephones.PICKUP_RANGE)) {
                ServerPlayer listener = leg.to();
                RadioSoundPayload payload = new RadioSoundPayload(sound, source, Vec3.ZERO, volume, pitch, true);
                Telephones.later(level.getServer(), leg.delayTicks(), () -> payload.sendTo(listener));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % SONG_SCAN_TICKS != 0) {
            return;
        }

        Set<Track> reached = new HashSet<>();
        for (Telephones.Leg leg : Telephones.legs()) {
            Optional<JukeboxBlockEntity> jukebox = nearby(leg.level(), leg.from().getEyePosition());
            if (jukebox.isEmpty()) {
                continue;
            }
            JukeboxSongPlayer player = jukebox.get().getSongPlayer();
            Track track = new Track(leg.to().getUUID(), leg.channel());
            Playing playing = new Playing(jukebox.get().getBlockPos(), player.getSong());
            reached.add(track);
            if (!playing.equals(SONGS.get(track))) {
                SONGS.put(track, playing);
                // Song offset
                long ticks = Math.max(0L, player.getTicksSinceSongStarted() - leg.delayTicks());
                RadioSongPayload payload = new RadioSongPayload(track.channel(), Optional.of(playing.song().soundEvent()), Vec3.ZERO, 1.0F, ticks, true);
                ServerPlayer listener = leg.to();
                Telephones.later(server, leg.delayTicks(), () -> payload.sendTo(listener));
            }
        }

        SONGS.keySet().removeIf(track -> {
            if (reached.contains(track)) {
                return false;
            }
            ServerPlayer listener = server.getPlayerList().getPlayer(track.listener());
            if (listener != null) {
                RadioSongPayload.stop(track.channel()).sendTo(listener);
            }
            return true;
        });
    }

    private static Optional<JukeboxBlockEntity> nearby(ServerLevel level, Vec3 at) {
        int reach = Mth.ceil(Telephones.PICKUP_RANGE);
        BlockPos centre = BlockPos.containing(at);
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-reach, -reach, -reach), centre.offset(reach, reach, reach))) {
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox && jukebox.getSongPlayer().getSong() != null
                    && Vec3.atCenterOf(pos).closerThan(at, Telephones.PICKUP_RANGE)) {
                return Optional.of(jukebox);
            }
        }
        return Optional.empty();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SONGS.clear();
    }
}
