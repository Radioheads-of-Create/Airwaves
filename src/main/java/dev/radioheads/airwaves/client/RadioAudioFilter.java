package dev.radioheads.airwaves.client;

import dev.radioheads.airwaves.api.radio.Modulation;

import java.util.Random;

public final class RadioAudioFilter {
    private static final float SAMPLE_RATE = 48000.0F;
    private static final float HIGH_PASS_HERTZ = 300.0F;
    private static final float HIGH_PASS = (float) Math.exp(-2.0 * Math.PI * HIGH_PASS_HERTZ / SAMPLE_RATE);
    private static final float DRIVE = 1.7F;
    private static final float CEILING = 0.8F;
    private static final float CRACKLE_DECAY = 0.86F;

    private final Random random = new Random();
    private float highInput;
    private float highOutput;
    private float low;
    private float lower;
    private float crackle;

    public short[] process(short[] pcm, Modulation modulation, float quality, float gain) {
        float lowPass = 1.0F - (float) Math.exp(-2.0 * Math.PI * modulation.audioHertz() / SAMPLE_RATE);
        float loss = 1.0F - quality;
        float hiss = 0.003F + 0.07F * loss * loss;
        float crackleChance = 0.002F * loss * loss;
        float level = 0.5F + 0.5F * quality;

        short[] out = new short[pcm.length];
        for (int i = 0; i < pcm.length; i++) {
            float sample = pcm[i] / 32768.0F;
            this.highOutput = HIGH_PASS * (this.highOutput + sample - this.highInput);
            this.highInput = sample;
            this.low += lowPass * (this.highOutput - this.low);
            this.lower += lowPass * (this.low - this.lower);

            if (this.random.nextFloat() < crackleChance) {
                this.crackle = (this.random.nextFloat() - 0.5F) * 0.5F;
            }

            float voice = (float) Math.tanh(this.lower * DRIVE) * CEILING * level;
            float mixed = (voice + (this.random.nextFloat() * 2.0F - 1.0F) * hiss + this.crackle) * gain;
            this.crackle *= CRACKLE_DECAY;
            out[i] = (short) Math.clamp(mixed * 32767.0F, -32768.0F, 32767.0F);
        }
        return out;
    }
}
