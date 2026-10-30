package dev.radioheads.airwaves.registry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogCTBehaviour;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.content.block.RadioBlock;
import dev.radioheads.airwaves.content.block.TuningTableBlock;
import dev.radioheads.airwaves.content.item.RadioItem;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.Tags;

import java.util.Map;

import static com.simibubi.create.foundation.data.BlockStateGen.simpleCubeAll;
import static com.simibubi.create.foundation.data.TagGen.*;

public final class AirwaveBlocks {
    private AirwaveBlocks() {}

    public static final BlockEntry<TuningTableBlock> TUNING_TABLE =
            Airwaves.REGISTRATE.block("tuning_table", TuningTableBlock::new)
                    .properties(p -> p
                            .strength(2.5f)
                            .mapColor(MapColor.WOOD)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                    )
                    .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
                        TuningTableBlock.Part part = state.getValue(TuningTableBlock.PART);
                        Direction facing = state.getValue(TuningTableBlock.FACING);

                        return new ConfiguredModel[] {
                                new ConfiguredModel(
                                        prov.models().getExistingFile(prov.modLoc("block/tuning_table/" + part.getSerializedName())),
                                        0,
                                        facing.get2DDataValue() * 90,
                                        false
                                )
                        };
                    }))
                    .item()
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/tuning_table/center")))
                    .build()
                    .register();

    public static final BlockEntry<Block>
        SILVER_ORE = Airwaves.REGISTRATE.block("silver_ore", Block::new)
            .initialProperties(() -> Blocks.GOLD_ORE)
            .properties(p -> p
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))
            .transform(TagGen.pickaxeOnly())
            .loot((lt, b) -> {
                HolderLookup.RegistryLookup<Enchantment> registryLookup = lt.getRegistries().lookupOrThrow(Registries.ENCHANTMENT);
                lt.add(b,
                        lt.createSilkTouchDispatchTable(b,
                                lt.applyExplosionDecay(b, LootItem.lootTableItem(AirwaveItems.RAW_SILVER.get())
                                        .apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE))))
                        )
                );
            })
            .tag(BlockTags.NEEDS_IRON_TOOL)
            .transform(TagGen.tagBlockAndItem(Map.of(
                    Tags.Blocks.ORES, Tags.Items.ORES,
                    CommonMetal.SILVER.ores.blocks(), CommonMetal.SILVER.ores.items(),
                    Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE
            )))
            .build()
            .register();

    public static final BlockEntry<Block>
            SILVER_BLOCK = Airwaves.REGISTRATE.block("silver_block", Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE)
                        .requiresCorrectToolForDrops())
                .transform(pickaxeOnly())
                .blockstate(simpleCubeAll("silver_block"))
                .tag(BlockTags.NEEDS_IRON_TOOL)
                .tag(Tags.Blocks.STORAGE_BLOCKS)
                .tag(BlockTags.BEACON_BASE_BLOCKS)
                .transform(tagBlockAndItem(CommonMetal.SILVER.storageBlocks))
                .tag(Tags.Items.STORAGE_BLOCKS)
                .build()
                .lang("Block of Silver")
                .register();


    public static final BlockEntry<CasingBlock>
            SILVER_CASING = Airwaves.REGISTRATE.block("silver_casing", CasingBlock::new)
                .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                .transform(BuilderTransformers.casing(() -> AirwaveSpriteShifts.SILVER_CASING))
                .register();

    public static final BlockEntry<RadioBlock>
        RADIO = Airwaves.REGISTRATE.block("radio", RadioBlock::new)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate((ctx, prov) ->  prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
                Direction facing = state.getValue(RadioBlock.HORIZONTAL_FACING);

                return new ConfiguredModel[] {
                        new ConfiguredModel(
                                prov.models().getExistingFile(prov.modLoc("block/radio")),
                                0,
                                facing.get2DDataValue() * 90,
                                false
                        )
                };
            }))
            .item(RadioItem::new)
            .properties(p -> p.component(AirwaveDataComponents.TUNING, Tuning.DEFAULT))
            .build()
            .loot((lt, b) -> lt.add(b, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .when(ExplosionCondition.survivesExplosion())
                            .add(LootItem.lootTableItem(b.asItem())
                                    .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                            .include(AirwaveDataComponents.TUNING))))))
            .register();

    public static void init() {}
}
