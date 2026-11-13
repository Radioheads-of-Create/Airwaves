package dev.radioheads.airwaves.network.telephone;

import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.content.telephone.PhoneNumbers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Telephone numbers per level
@NothingNullByDefault
public final class TelephoneDirectory extends SavedData {
    private static final String NAME = "airwaves_telephones";
    private static final SavedData.Factory<TelephoneDirectory> FACTORY = new SavedData.Factory<>(TelephoneDirectory::new, TelephoneDirectory::load, null);
    private static final int PHONE_DIGITS = PhoneNumbers.LENGTH - PhoneNumbers.NETWORK_DIGITS;

    private final Map<String, Entry> numbers = new HashMap<>();

    // Telephone position
    public record Entry(UUID phone, Optional<BlockPos> pos) {}

    public static TelephoneDirectory of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public String assign(int network, UUID phone, BlockPos pos, RandomSource random) {
        String number;
        do {
            StringBuilder digits = new StringBuilder(String.valueOf(network));
            for (int i = 0; i < PHONE_DIGITS; i++) {
                digits.append(1 + random.nextInt(PhoneNumbers.DIGITS));
            }
            number = digits.toString();
        } while (this.numbers.containsKey(number));
        this.numbers.put(number, new Entry(phone, Optional.of(pos.immutable())));
        setDirty();
        return number;
    }

    @Nullable
    public Entry find(String number) {
        return this.numbers.get(number);
    }

    public void place(String number, UUID phone, BlockPos pos) {
        this.numbers.put(number, new Entry(phone, Optional.of(pos.immutable())));
        setDirty();
    }

    public void retire(String number) {
        Entry entry = this.numbers.get(number);
        if (entry != null && entry.pos().isPresent()) {
            this.numbers.put(number, new Entry(entry.phone(), Optional.empty()));
            setDirty();
        }
    }

    public static TelephoneDirectory load(CompoundTag tag, HolderLookup.Provider registries) {
        TelephoneDirectory directory = new TelephoneDirectory();
        for (Tag entry : tag.getList("Numbers", Tag.TAG_COMPOUND)) {
            CompoundTag number = (CompoundTag) entry;
            Optional<BlockPos> pos = number.contains("Pos") ? Optional.of(BlockPos.of(number.getLong("Pos"))) : Optional.empty();
            directory.numbers.put(number.getString("Number"), new Entry(number.getUUID("Phone"), pos));
        }
        return directory;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<String, Entry> number : this.numbers.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Number", number.getKey());
            entry.putUUID("Phone", number.getValue().phone());
            number.getValue().pos().ifPresent(pos -> entry.putLong("Pos", pos.asLong()));
            list.add(entry);
        }
        tag.put("Numbers", list);
        return tag;
    }
}
