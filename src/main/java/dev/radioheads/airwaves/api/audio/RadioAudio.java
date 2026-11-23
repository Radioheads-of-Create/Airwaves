package dev.radioheads.airwaves.api.audio;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.network.RadioSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public final class RadioAudio {
    private RadioAudio() {}

    public static void playAt(Level level, BlockPos pos, SoundEvent sound, Tuning frequency) {
        playAt(level, pos, sound, frequency, 1.0F, 1.0F);
    }

    public static void playAt(Level level, BlockPos pos, SoundEvent sound, Tuning frequency, float volume, float pitch) {
        playAt(level, pos, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.RECORDS, frequency, volume, pitch);
    }

    public static void playAt(Level level, BlockPos pos, SoundEvent sound, SoundSource category, Tuning frequency, float volume, float pitch) {
        playAt(level, pos, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), category, frequency, volume, pitch);
    }

    public static void playAt(Level level, BlockPos pos, Holder<SoundEvent> sound, Tuning frequency) {
        playAt(level, pos, sound, frequency, 1.0F, 1.0F);
    }

    public static void playAt(Level level, BlockPos pos, Holder<SoundEvent> sound, Tuning frequency, float volume, float pitch) {
        playAt(level, pos, sound, SoundSource.RECORDS, frequency, volume, pitch);
    }

    public static void playAt(Level level, BlockPos pos, Holder<SoundEvent> sound, SoundSource category, Tuning frequency, float volume, float pitch) {
        long hertz = frequency.clamped().hertz();
        broadcast(level, pos, sound, category, frequency, hertz, hertz, volume, pitch);
    }

    public static void playBetween(Level level, BlockPos pos, SoundEvent sound, Tuning band, long fromHertz, long toHertz) {
        playBetween(level, pos, sound, band, fromHertz, toHertz, 1.0F, 1.0F);
    }

    public static void playBetween(Level level, BlockPos pos, SoundEvent sound, Tuning band, long fromHertz, long toHertz, float volume, float pitch) {
        playBetween(level, pos, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.RECORDS, band, fromHertz, toHertz, volume, pitch);
    }

    public static void playBetween(Level level, BlockPos pos, SoundEvent sound, SoundSource category, Tuning band, long fromHertz, long toHertz, float volume, float pitch) {
        playBetween(level, pos, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), category, band, fromHertz, toHertz, volume, pitch);
    }

    public static void playBetween(Level level, BlockPos pos, Holder<SoundEvent> sound, Tuning band, long fromHertz, long toHertz) {
        playBetween(level, pos, sound, band, fromHertz, toHertz, 1.0F, 1.0F);
    }

    public static void playBetween(Level level, BlockPos pos, Holder<SoundEvent> sound, Tuning band, long fromHertz, long toHertz, float volume, float pitch) {
        playBetween(level, pos, sound, SoundSource.RECORDS, band, fromHertz, toHertz, volume, pitch);
    }

    public static void playBetween(Level level, BlockPos pos, Holder<SoundEvent> sound, SoundSource category, Tuning band, long fromHertz, long toHertz, float volume, float pitch) {
        broadcast(level, pos, sound, category, band, fromHertz, toHertz, volume, pitch);
    }

    private static void broadcast(Level level, BlockPos pos, Holder<SoundEvent> sound, SoundSource category, Tuning band, long fromHertz, long toHertz, float volume, float pitch) {
        if (level instanceof ServerLevel server) {
            RadioSounds.broadcast(server, pos, sound, category, band.clamped().band(), fromHertz, toHertz, volume, pitch);
        }
    }
}
