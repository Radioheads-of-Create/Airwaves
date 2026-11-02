package dev.radioheads.airwaves.api.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CableLinks {
    private final List<CableLink> links = new ArrayList<>();

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

    public void add(BlockPos other, CableType type) {
        this.links.add(new CableLink(other.immutable(), type));
    }

    public boolean remove(BlockPos other) {
        return this.links.removeIf(link -> link.other().equals(other));
    }

    public void save(CompoundTag compound) {
        ListTag list = new ListTag();
        for (CableLink link : this.links) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Other", link.other().asLong());
            tag.putString("Type", link.type().name().toLowerCase(Locale.ROOT));
            list.add(tag);
        }
        compound.put("Links", list);
    }

    public void load(CompoundTag compound) {
        this.links.clear();
        for (Tag entry : compound.getList("Links", Tag.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) entry;
            this.links.add(new CableLink(BlockPos.of(tag.getLong("Other")), CableType.valueOf(tag.getString("Type").toUpperCase(Locale.ROOT))));
        }
    }
}
