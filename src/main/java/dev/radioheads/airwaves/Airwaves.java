package dev.radioheads.airwaves;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.radioheads.airwaves.config.AirwaveConfig;
import dev.radioheads.airwaves.integration.IntegrationLoader;
import dev.radioheads.airwaves.registry.*;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

@Mod(Airwaves.MOD_ID)
public class Airwaves {
    public static final String MOD_ID = "airwaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final AirwaveRegistrate REGISTRATE = AirwaveRegistrate.create(MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public Airwaves(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);

        AirwaveItems.init();
        AirwaveBlocks.init();
        AirwaveBlockEntities.init();
        AirwaveRadioBands.init();
        AirwaveModulations.init();
        AirwaveTiers.init();
        AirwaveAntennaTypes.init();
        AirwaveLang.init();
        AirwaveMenuTypes.MENU.register(modEventBus);
        AirwaveDataComponents.DATA_COMPONENTS.register(modEventBus);
        AirwaveCreativeTabs.CREATIVE_TABS.register(modEventBus);
        AirwavePlacementModifiers.REGISTER.register(modEventBus);

        AirwaveConfig.register(modContainer);
        IntegrationLoader.init(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return path.contains(":")
                ? ResourceLocation.tryParse(path)
                : ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static ResourceLocation asCommon(String path) {
        return path.contains(":")
                ? ResourceLocation.tryParse(path)
                : ResourceLocation.fromNamespaceAndPath("c", path);
    }

}
