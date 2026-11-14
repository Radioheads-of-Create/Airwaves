package dev.radioheads.airwaves.client.telephone;

import dev.radioheads.airwaves.content.entity.block.TelephoneBlockEntity;
import dev.radioheads.airwaves.registry.AirwaveSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

// Incoming call ring
public final class TelephoneRing extends AbstractTickableSoundInstance {
    // Retry interval in ticks
    private static final int RETRY_TICKS = 20;
    private static final Map<BlockPos, TelephoneRing> PLAYING = new HashMap<>();

    private final TelephoneBlockEntity phone;
    private final long startedAt;

    private TelephoneRing(TelephoneBlockEntity phone, long startedAt) {
        super(AirwaveSoundEvents.TELEPHONE_RING.getMainEvent(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.phone = phone;
        this.startedAt = startedAt;
        this.looping = true;
        this.delay = 0;
        Vec3 at = Vec3.atCenterOf(phone.getBlockPos());
        this.x = at.x;
        this.y = at.y;
        this.z = at.z;
    }

    public static void tick(TelephoneBlockEntity phone) {
        if (!phone.ringing() || phone.getLevel() == null) {
            return;
        }
        long now = phone.getLevel().getGameTime();
        SoundManager sounds = Minecraft.getInstance().getSoundManager();
        TelephoneRing ring = PLAYING.get(phone.getBlockPos());
        if (ring != null && (sounds.isActive(ring) || now - ring.startedAt < RETRY_TICKS)) {
            return;
        }
        ring = new TelephoneRing(phone, now);
        PLAYING.put(phone.getBlockPos(), ring);
        sounds.play(ring);
    }

    @Override
    public void tick() {
        if (this.phone.isRemoved() || !this.phone.ringing()) {
            stop();
            PLAYING.remove(this.phone.getBlockPos(), this);
        }
    }
}
