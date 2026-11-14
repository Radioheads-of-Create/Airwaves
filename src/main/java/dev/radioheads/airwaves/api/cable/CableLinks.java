package dev.radioheads.airwaves.api.cable;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class CableLinks {
    private final List<CableLink> links = new ArrayList<>();
    // In the cable graph
    private boolean graphed;

    public boolean graphed() {
        return this.graphed;
    }

    public void graphed(boolean graphed) {
        this.graphed = graphed;
    }

    public List<CableLink> all() {
        return this.links;
    }

    public boolean isEmpty() {
        return this.links.isEmpty();
    }

    public int size() {
        return this.links.size();
    }

    public boolean has(BlockPos other) {
        return this.links.stream().anyMatch(link -> link.other().equals(other));
    }

    public boolean has(CableLink.Flow flow) {
        return this.links.stream().anyMatch(link -> link.flow() == flow);
    }

    public void add(BlockPos other, CableType type, CableLink.Flow flow) {
        this.links.add(new CableLink(other.immutable(), type, flow));
    }

    public boolean remove(BlockPos other) {
        return this.links.removeIf(link -> link.other().equals(other));
    }

    public void save(CompoundTag compound) {
        ListTag list = new ListTag();
        for (CableLink link : this.links) {
            ResourceLocation key = AirwaveRegistries.CABLE_TYPE_REGISTRY.getKey(link.type());
            if (key == null) {
                throw new IllegalStateException("Unknown cable type: " + link.type());
            }

            CompoundTag tag = new CompoundTag();
            tag.putLong("Other", link.other().asLong());
            tag.putString("Type", key.toString());
            tag.putByte("Flow", (byte) link.flow().ordinal());
            list.add(tag);
        }
        compound.put("Links", list);
        compound.putBoolean("Graphed", this.graphed);
    }

    public void load(CompoundTag compound) {
        this.links.clear();
        this.graphed = compound.getBoolean("Graphed");
        for (Tag entry : compound.getList("Links", Tag.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) entry;

            String raw = tag.getString("Type");
            ResourceLocation key = Airwaves.asResource(raw);
            if (key == null) {
                throw new IllegalStateException("Malformed cable type id: " + raw);
            }

            CableType type = AirwaveRegistries.CABLE_TYPE_REGISTRY.get(key);
            if (type == null) {
                throw new IllegalStateException("Unknown cable type in save data: " + key);
            }

            CableLink.Flow[] flows = CableLink.Flow.values();
            int flow = tag.contains("Flow") ? tag.getByte("Flow") : CableLink.Flow.BOTH.ordinal();
            this.links.add(new CableLink(BlockPos.of(tag.getLong("Other")), type, flows[Math.floorMod(flow, flows.length)]));
        }
    }
}
