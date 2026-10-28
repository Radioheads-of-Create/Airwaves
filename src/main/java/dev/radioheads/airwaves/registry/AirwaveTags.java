package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.AirwaveLang;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.antenna.Tier;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static dev.radioheads.airwaves.registry.AirwaveTags.NameSpace.COMMON;
import static dev.radioheads.airwaves.registry.AirwaveTags.NameSpace.MOD;

public final class AirwaveTags {
    private AirwaveTags() {}

    public enum NameSpace {
        MOD(Airwaves.MOD_ID),
        COMMON("c");

        public final String id;
        NameSpace(String id) {
            this.id = id;
        }
    }

    public enum Blocks {
        STANDARD_ANTENNA(MOD, "standard_antenna");

        public final TagKey<Block> tag;

        Blocks() {
            this(MOD);
        }

        Blocks(NameSpace namespace) {
            this(namespace, null);
        }

        Blocks(NameSpace namespace, String path) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace.id, path == null ? AirwaveLang.asId(name()) : path);
            this.tag = BlockTags.create(id);
        }

        Blocks(String namespace, String path) {
            this.tag = BlockTags.create(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }

    public enum Tiers {
        STANDARD;

        public final TagKey<Tier> tag;

        Tiers() {
            this(MOD);
        }

        Tiers(NameSpace namespace) {
            this(namespace, null);
        }

        Tiers(NameSpace namespace, String path) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace.id, path == null ? AirwaveLang.asId(name()) : path);
            this.tag = TagKey.create(AirwaveRegistries.TIER, id);
        }

        Tiers(String namespace, String path) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path == null ? AirwaveLang.asId(name()) : path);
            this.tag = TagKey.create(AirwaveRegistries.TIER, id);
        }
    }

    public enum Items {
        RAW_MATERIALS_SILVER(COMMON, "raw_materials/silver"),

        INGOTS_SILVER(COMMON, "ingots/silver"),
        NUGGETS_SILVER(COMMON, "nuggets/silver"),
        SHEETS_SILVER(COMMON, "sheets/silver"),
        ;

        public final TagKey<Item> tag;

        Items() {
            this(MOD);
        }
        Items(NameSpace namespace) {
            this(namespace, null);
        }
        Items(NameSpace namespace, String path) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace.id, path == null ? AirwaveLang.asId(name()) : path);
            this.tag = ItemTags.create(id);
        }
        Items(String namespace, String path) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path == null ? AirwaveLang.asId(name()) : path);
            this.tag = ItemTags.create(id);
        }
    }
}
