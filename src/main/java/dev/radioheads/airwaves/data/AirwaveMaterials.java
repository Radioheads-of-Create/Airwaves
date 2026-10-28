package dev.radioheads.airwaves.data;

import dev.radioheads.airwaves.Airwaves;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

// Improvised from
// https://github.com/cosmads/Chemica/blob/1.21.1/src/main/java/com/cosmads/chemica/data/ChemMaterial.java
public enum AirwaveMaterials {
    // Vanilla
    COPPER,
    GOLD,

    // Airwaves
    SILVER;

    public final String name;

    /**
     * True is this metal generates naturally. If false, the following tags are nonsense:
     * <ul>
     *     <li>{@link #ores}</li>
     *     <li>{@link #rawMaterial}</li>
     *     <li>{@link #rawStorageBlocks}</li>
     * </ul>
     */
    public final boolean isNatural;

    public final ItemLikeTag ores;
    public final TagKey<Item> rawMaterial;
    public final ItemLikeTag rawStorageBlocks;
    public final TagKey<Item> ingots;
    public final ItemLikeTag storageBlocks;
    public final TagKey<Item> nuggets;
    public final TagKey<Item> plates;
    public final TagKey<Item> wire;
    public final TagKey<Item> foil;

    AirwaveMaterials() {
        this(true);
    }

    AirwaveMaterials(boolean natural) {
        this.name = Lang.asId(name());

        this.isNatural = natural;

        this.ores = new ItemLikeTag("ores/" + this.name);
        this.rawMaterial = itemTag("raw_materials/" + this.name);
        this.rawStorageBlocks = new ItemLikeTag("storage_blocks/raw_" + this.name);
        this.ingots = itemTag("ingots/" + this.name);
        this.storageBlocks = new ItemLikeTag("storage_blocks/" + this.name);
        this.nuggets = itemTag("nuggets/" + this.name);
        this.plates = itemTag("plates/" + this.name);
        this.wire = itemTag("wire/" + this.name);
        this.foil = selfItemTag("foil/" + this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static TagKey<Item> selfItemTag(String path) {
        return TagKey.create(Registries.ITEM, Airwaves.asResource(path));
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, Airwaves.asCommon(path));
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, Airwaves.asCommon(path));
    }

    public record ItemLikeTag(TagKey<Item> items, TagKey<Block> blocks) {
        private ItemLikeTag(String path) {
            this(itemTag(path), blockTag(path));
        }
    }
}
