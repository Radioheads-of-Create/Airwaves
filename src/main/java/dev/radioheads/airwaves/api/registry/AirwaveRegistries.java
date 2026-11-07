package dev.radioheads.airwaves.api.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.CableType;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.registry.board.*;
import dev.radioheads.airwaves.api.registry.board.step.BoardStep;
import dev.radioheads.airwaves.api.registry.board.step.BoardStepMode;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public class AirwaveRegistries {
    private static List<Registry<?>> FOR_INIT = new ArrayList<>();

    public static final ResourceKey<Registry<BoardDesign>> BOARD = createRegistryKey("board");
    public static final ResourceKey<Registry<BoardPart>> BOARD_PART = createRegistryKey("board_part");
    public static final ResourceKey<Registry<BoardStage>> BOARD_STAGE = createRegistryKey("board_stage");
    public static final ResourceKey<Registry<BoardRule>> BOARD_RULE = createRegistryKey("board_rule");
    public static final ResourceKey<Registry<BoardSource>> BOARD_SOURCE = createRegistryKey("board_source");
    public static final ResourceKey<Registry<BoardStep>> BOARD_STEP = createRegistryKey("board_step");
    public static final ResourceKey<Registry<BoardStepMode>> BOARD_STEP_MODE = createRegistryKey("board_step_mode");
    public static final ResourceKey<Registry<RadioBand>> RADIO_BAND = createRegistryKey("radio_band");
    public static final ResourceKey<Registry<Modulation>> MODULATION = createRegistryKey("modulation");
    public static final ResourceKey<Registry<CableType>> CABLE_TYPE = createRegistryKey("cable_type");

    public static final Registry<BoardDesign> BOARD_DESIGN_REGISTRY = makeSyncedRegistry(BOARD);
    public static final Registry<BoardPart> BOARD_PART_REGISTRY = makeSyncedRegistry(BOARD_PART);
    public static final Registry<RadioBand> RADIO_BAND_REGISTRY = makeSyncedRegistry(RADIO_BAND);
    public static final Registry<Modulation> MODULATION_REGISTRY = makeSyncedRegistry(MODULATION);

    public static final Registry<BoardStage> BOARD_STAGE_REGISTRY = makeRegistry(BOARD_STAGE);
    public static final Registry<BoardRule> BOARD_RULE_REGISTRY = makeRegistry(BOARD_RULE);
    public static final Registry<BoardSource> BOARD_SOURCE_REGISTRY = makeRegistry(BOARD_SOURCE);
    public static final Registry<BoardStep> BOARD_STEP_REGISTRY = makeRegistry(BOARD_STEP);
    public static final Registry<BoardStepMode> BOARD_STEP_MODE_REGISTRY = makeRegistry(BOARD_STEP_MODE);
    public static final Registry<CableType> CABLE_TYPE_REGISTRY = makeRegistry(CABLE_TYPE);

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Airwaves.asResource(name));
    }

    private static <T> Registry<T> makeRegistry(ResourceKey<Registry<T>> registryKey) {
        var registry = new RegistryBuilder<>(registryKey).create();
        FOR_INIT.add(registry);
        return registry;
    }

    private static <T> Registry<T> makeSyncedRegistry(ResourceKey<Registry<T>> registryKey) {
        var registry = new RegistryBuilder<>(registryKey).sync(true).create();
        FOR_INIT.add(registry);
        return registry;
    }

    @SubscribeEvent
    public static void register(NewRegistryEvent event) {
        FOR_INIT.forEach(event::register);
        FOR_INIT = null;
    }
}
