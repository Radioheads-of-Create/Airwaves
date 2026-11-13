package dev.radioheads.airwaves.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.content.block.*;
import dev.radioheads.airwaves.content.entity.block.TransmitterBlockEntity;
import dev.radioheads.airwaves.content.item.OldModelNotCompleteYetBlockItem;
import dev.radioheads.airwaves.content.item.RadioItem;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.Tags;

import java.util.Map;

import static com.simibubi.create.foundation.data.BlockStateGen.simpleCubeAll;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.simibubi.create.foundation.data.TagGen.tagBlockAndItem;

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
                    .item(OldModelNotCompleteYetBlockItem::new)
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
                                yRotation(facing),
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

    public static final BlockEntry<TransmitterBlock>
            AM_TRANSMITTER = registerIncompleteTransmitter("am_transmitter", "AM Transmitter", "transmitter"),
            FM_TRANSMITTER = registerIncompleteTransmitter("fm_transmitter", "FM Transmitter", "transmitter"),
            VHF_BASE_STATION = registerIncompleteTransmitter("vhf_base_station", "VHF Base Station", "base_station"),
            UHF_BASE_STATION = registerIncompleteTransmitter("uhf_base_station", "UHF Base Station", "base_station"),
            VHF_REPEATER = registerIncompleteTransmitter("vhf_repeater", "VHF Repeater", "repeater"),
            UHF_REPEATER = registerIncompleteTransmitter("uhf_repeater", "UHF Repeater", "repeater");

    public static final BlockEntry<HfTransceiverBlock> HF_TRANSCEIVER = Airwaves.REGISTRATE
            .block("hf_transceiver", properties -> new HfTransceiverBlock(properties, 100, AirwaveRadioBands.HF))
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> new ConfiguredModel[] {
                    new ConfiguredModel(
                            prov.models().getExistingFile(prov.modLoc("block/hf_transceiver/" + state.getValue(HfTransceiverBlock.FINISH).getSerializedName() + "/body")),
                            0,
                            yRotation(state.getValue(HfTransceiverBlock.HORIZONTAL_FACING)),
                            false
                    )
            }))
            .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> TransmitterBlockEntity.BASE_STRESS))
            .item(RadioItem::new)
            .properties(p -> p.component(AirwaveDataComponents.TUNING, new Tuning(7_100_000L, AirwaveRadioBands.HF)))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/hf_transceiver/brass/item"))
                    .override()
                    .predicate(Airwaves.asResource("finish"), 1.0F)
                    .model(prov.getExistingFile(prov.modLoc("block/hf_transceiver/black/item")))
                    .end())
            .build()
            .loot((tables, block) -> tables.add(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .when(ExplosionCondition.survivesExplosion())
                            .add(LootItem.lootTableItem(block.asItem())
                                    .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                            .include(AirwaveDataComponents.TUNING))
                                    .apply(CopyBlockState.copyState(block).copy(HfTransceiverBlock.FINISH))))))
            .lang("HF Transceiver")
            .register();

    public static final BlockEntry<InsulatorBlock>
            ANDESITE_INSULATOR = registerInsulator("andesite"),
            BRASS_INSULATOR = registerInsulator("brass");

    private static BlockEntry<InsulatorBlock> registerInsulator(String material) {
        return Airwaves.REGISTRATE.block(material + "_insulator", InsulatorBlock::new)
                .initialProperties(() -> Blocks.TERRACOTTA)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .transform(pickaxeOnly())
                .blockstate((ctx, prov) -> prov.directionalBlock(ctx.getEntry(), prov.models().withExistingParent(ctx.getName(), prov.modLoc("block/insulator"))
                        .texture("insulator", Airwaves.asResource("block/" + ctx.getName()))))
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/insulator"))
                        .texture("insulator", Airwaves.asResource("block/" + ctx.getName())))
                .build()
                .register();
    }

    public static final BlockEntry<AmplifierBlock> AMPLIFIER = Airwaves.REGISTRATE.block("amplifier", AmplifierBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> new ConfiguredModel[] {
                    new ConfiguredModel(
                            prov.models().getExistingFile(prov.modLoc("block/amplifier/on")),
                            0,
                            yRotation(state.getValue(AmplifierBlock.FACING)),
                            false
                    )
            }))
            //TODO CHANGE
            .item(OldModelNotCompleteYetBlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/amplifier/on")))
            .build()
            .register();

    public static final BlockEntry<MicrophoneBlock> MICROPHONE = Airwaves.REGISTRATE.block("microphone", MicrophoneBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> new ConfiguredModel[] {
                    new ConfiguredModel(
                            prov.models().getExistingFile(prov.modLoc("block/microphone/" + (state.getValue(MicrophoneBlock.ON_AIR) ? "on" : "off"))),
                            0,
                            yRotation(state.getValue(MicrophoneBlock.FACING)),
                            false
                    )
            }))
            .item()
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/microphone/item")))
            .build()
            .register();

    public static final BlockEntry<AntennaRodBlock>
            COPPER_ANTENNA_ROD = registerAntennaRod("copper", 1.0),
            GOLD_ANTENNA_ROD = registerAntennaRod("gold", 2.44 / 1.68),
            SILVER_ANTENNA_ROD = registerAntennaRod("silver", 1.59 / 1.68);

    // Resistivity relative to copper
    private static BlockEntry<AntennaRodBlock> registerAntennaRod(String material, double resistivity) {
        return Airwaves.REGISTRATE.block(material + "_antenna_rod", properties -> new AntennaRodBlock(properties, resistivity))
                .initialProperties(SharedProperties::softMetal)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .transform(pickaxeOnly())
                .blockstate((ctx, prov) -> {
                    ModelFile link = antennaRodPart(prov, material, "link");
                    ModelFile tip = antennaRodPart(prov, material, "tip");
                    MultiPartBlockStateBuilder parts = prov.getMultipartBuilder(ctx.getEntry());

                    for (Direction side : Direction.values()) {
                        int x = side == Direction.UP ? 0 : side == Direction.DOWN ? 180 : 90;
                        int y = side.getAxis().isHorizontal() ? yRotation(side) : 0;
                        BooleanProperty linked = AntennaRodBlock.LINKS.get(side);

                        parts.part().modelFile(link).rotationX(x).rotationY(y).addModel()
                                .condition(linked, true).end();

                        MultiPartBlockStateBuilder.PartBuilder end = parts.part().modelFile(tip).rotationX(x).rotationY(y).addModel()
                                .condition(linked, false).condition(AntennaRodBlock.AXIS, side.getAxis());
                        for (Direction other : Direction.values()) {
                            if (other.getAxis() != side.getAxis()) {
                                end.condition(AntennaRodBlock.LINKS.get(other), false);
                            }
                        }
                    }

                    MultiPartBlockStateBuilder.PartBuilder hub = parts.part().modelFile(antennaRodPart(prov, material, "hub")).addModel().useOr();
                    for (Direction side : Direction.values()) {
                        for (Direction.Axis axis : Direction.Axis.values()) {
                            if (axis != side.getAxis()) {
                                hub.nestedGroup().condition(AntennaRodBlock.AXIS, axis).condition(AntennaRodBlock.LINKS.get(side), true).end();
                            }
                        }
                    }
                })
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/antenna_rod/item"))
                        .texture("rod", Airwaves.asResource("block/antenna_rod/" + material)))
                .build()
                .register();
    }

    public static final BlockEntry<RodBaseBlock>
            COPPER_ROD_BASE = registerRodBase("copper"),
            GOLD_ROD_BASE = registerRodBase("gold"),
            SILVER_ROD_BASE = registerRodBase("silver");

    private static BlockEntry<RodBaseBlock> registerRodBase(String material) {
        return Airwaves.REGISTRATE.block(material + "_rod_base", RodBaseBlock::new)
                .initialProperties(() -> Blocks.TERRACOTTA)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .transform(pickaxeOnly())
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), antennaRodPart(prov, material, "base")))
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/antenna_rod/base"))
                        .texture("rod", Airwaves.asResource("block/antenna_rod/" + material)))
                .build()
                .register();
    }

    private static ModelFile antennaRodPart(RegistrateBlockstateProvider prov, String material, String part) {
        return prov.models().withExistingParent("block/antenna_rod/" + material + "/" + part, prov.modLoc("block/antenna_rod/" + part))
                .texture("rod", Airwaves.asResource("block/antenna_rod/" + material));
    }

    private static BlockEntry<TransmitterBlock> registerIncompleteTransmitter(String name, String lang, String model) {
        return Airwaves.REGISTRATE.block(name, TransmitterBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .transform(pickaxeOnly())
                .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> new ConfiguredModel[] {
                        new ConfiguredModel(
                                prov.models().getExistingFile(prov.modLoc("block/" + model + "/on")),
                                0,
                                yRotation(state.getValue(TransmitterBlock.FACING)),
                                false
                        )
                }))
                .item(OldModelNotCompleteYetBlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + model + "/on")))
                .build()
                .lang(lang)
                .register();
    }

    private static BlockEntry<TransmitterBlock> registerTransmitter(String name, String lang, String model) {
        return Airwaves.REGISTRATE.block(name, TransmitterBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .transform(pickaxeOnly())
                .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> new ConfiguredModel[] {
                        new ConfiguredModel(
                                prov.models().getExistingFile(prov.modLoc("block/" + model + "/on")),
                                0,
                                yRotation(state.getValue(TransmitterBlock.FACING)),
                                false
                        )
                }))
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + model + "/on")))
                .build()
                .lang(lang)
                .register();
    }

    private static int yRotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }

    public static final BlockEntry<FoilApplicatorBlock>
        FOIL_APPLICATOR =
            Airwaves.REGISTRATE.block("foil_applicator", FoilApplicatorBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
                        Direction facing = state.getValue(FoilApplicatorBlock.HORIZONTAL_FACING);

                        return new ConfiguredModel[] {
                                new ConfiguredModel(
                                        prov.models().getExistingFile(prov.modLoc("foil_applicator")),
                                        0,
                                        facing.get2DDataValue() * 90,
                                        false
                                )
                        };
                    }))
                    .item()
                    .model(NonNullBiConsumer.noop())
                    .build()
                    .register();

    public static void init() {}
}
