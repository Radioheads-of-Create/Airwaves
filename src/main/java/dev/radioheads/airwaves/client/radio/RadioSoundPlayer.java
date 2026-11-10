package dev.radioheads.airwaves.client.radio;

import dev.radioheads.airwaves.network.RadioVoice;
import dev.radioheads.airwaves.network.payload.RadioSongPayload;
import dev.radioheads.airwaves.network.payload.RadioSoundPayload;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class RadioSoundPlayer {
    private static final Map<UUID, SoundInstance> SONGS = new HashMap<>();
    private static final int SKIP_BYTES = 1 << 16;

    private RadioSoundPlayer() {}

    public static void play(RadioSoundPayload payload) {
        Minecraft.getInstance().getSoundManager().play(instance(payload.sound(), payload.source(), payload.speaker(), payload.volume(), payload.pitch()));
    }

    public static void song(RadioSongPayload payload) {
        SoundManager sounds = Minecraft.getInstance().getSoundManager();
        SoundInstance playing = SONGS.get(payload.channelId());
        if (playing instanceof Song song && payload.song().filter(song::plays).isPresent() && sounds.isActive(song)) {
            song.level = payload.volume();
            return;
        }

        SONGS.remove(payload.channelId());
        if (playing != null) {
            sounds.stop(playing);
        }
        payload.song().ifPresent(song -> {
            SoundInstance instance = new Song(song, payload.speaker(), payload.volume(), (double) payload.ticks() / SharedConstants.TICKS_PER_SECOND);
            SONGS.put(payload.channelId(), instance);
            sounds.play(instance);
        });
    }

    // Played at the receiver's speaker
    private static SoundInstance instance(Holder<SoundEvent> sound, SoundSource source, Vec3 speaker, float volume, float pitch) {
        return new SimpleSoundInstance(sound.value().getLocation(), source, volume, pitch, SoundInstance.createUnseededRandom(),
                false, 0, SoundInstance.Attenuation.LINEAR, speaker.x, speaker.y, speaker.z, false);
    }

    // Speaker falloff to silence at the speaker range
    static float loudness(float level, double distance) {
        return (float) Math.clamp(level * (1.0 - distance / RadioVoice.SPEAKER_RANGE), 0.0, 1.0);
    }

    // Jukebox song joined at the jukebox's place in it
    private static final class Song extends AbstractTickableSoundInstance {
        private final double seconds;
        private final long receivedNanos = System.nanoTime();
        private float level;

        private Song(Holder<SoundEvent> sound, Vec3 speaker, float level, double seconds) {
            super(sound.value(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
            this.x = speaker.x;
            this.y = speaker.y;
            this.z = speaker.z;
            // Falloff in tick for live level changes
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.seconds = seconds;
            this.level = level;
            tick();
        }

        private boolean plays(Holder<SoundEvent> sound) {
            return getLocation().equals(sound.value().getLocation());
        }

        @Override
        public void tick() {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            this.volume = loudness(this.level, camera.getPosition().distanceTo(new Vec3(this.x, this.y, this.z)));
        }

        // Silent start keeps song time unless records are muted
        @Override
        public boolean canStartSilent() {
            return Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.RECORDS) > 0.0F;
        }

        @Override
        public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
            return super.getStream(buffers, sound, looping).thenApply(stream -> skip(stream, this.seconds, this.receivedNanos));
        }
    }

    // Song seconds plus the time since the packet came
    static AudioStream skip(AudioStream stream, double seconds, long fromNanos) {
        AudioFormat format = stream.getFormat();
        if (format.getFrameRate() <= 0.0F || format.getFrameSize() <= 0) {
            return stream;
        }

        long skipped = 0L;
        try {
            while (true) {
                double target = seconds + (System.nanoTime() - fromNanos) / 1.0E9;
                long left = (long) (target * format.getFrameRate()) * format.getFrameSize() - skipped;
                if (left <= 0L) {
                    return stream;
                }
                ByteBuffer read = stream.read((int) Math.min(left, SKIP_BYTES));
                if (!read.hasRemaining()) {
                    return stream;
                }
                skipped += read.remaining();
            }
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }
}
