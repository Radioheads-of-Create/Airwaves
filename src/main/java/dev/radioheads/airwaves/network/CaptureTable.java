package dev.radioheads.airwaves.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CaptureTable {
    private static final long HOLD_MS = 150L;

    private final Map<UUID, Holder> holders = new ConcurrentHashMap<>();

    private record Holder(UUID talker, double marginDb, long lastMs) {}

    public boolean passes(UUID receiver, UUID talker, double marginDb, double captureDb, long nowMs) {
        Holder held = this.holders.compute(receiver, (id, current) ->
                current == null || nowMs - current.lastMs() > HOLD_MS || current.talker().equals(talker) || marginDb > current.marginDb()
                        ? new Holder(talker, marginDb, nowMs)
                        : current);
        return held.talker().equals(talker) || held.marginDb() - marginDb < captureDb;
    }

    public void clear() {
        this.holders.clear();
    }
}
