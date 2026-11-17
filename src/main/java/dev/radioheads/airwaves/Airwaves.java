package dev.radioheads.airwaves;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.radioheads.airwaves.config.AirwaveConfig;
import dev.radioheads.airwaves.content.AirwaveArmInteractionPoints;
import dev.radioheads.airwaves.integration.IntegrationLoader;
import dev.radioheads.airwaves.registry.*;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(Airwaves.MOD_ID)
public class Airwaves {
    public static final String MOD_ID = "airwaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final AirwaveRegistrate REGISTRATE = AirwaveRegistrate.create(MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static AirwaveRegistrate registrate() {
        if (!STACK_WALKER.getCallerClass().getPackageName().startsWith("dev.radioheads.airwaves"))
            throw new UnsupportedOperationException("Other mods are not permitted to use Airwaves's registrate instance.");
        return REGISTRATE;
    }

    public Airwaves(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);

        AirwaveItems.init();
        AirwaveBlocks.init();
        AirwaveBlockEntities.init();
        AirwaveRadioBands.init();
        AirwaveModulations.init();
        AirwaveLang.init();
        AirwaveMenuTypes.MENU.register(modEventBus);
        AirwaveDataComponents.DATA_COMPONENTS.register(modEventBus);
        AirwaveCreativeTabs.CREATIVE_TABS.register(modEventBus);
        AirwavePlacementModifiers.REGISTER.register(modEventBus);
        AirwaveRecipeTypes.register(modEventBus);
        AirwavePartialModels.init();
        AirwaveSoundEvents.prepare();
        AirwaveCableTypes.init();
        AirwaveFoilTypes.init();

        AirwaveConfig.register(modContainer);
        IntegrationLoader.init(modEventBus);

        modEventBus.addListener(AirwaveSoundEvents::register);

        modEventBus.addListener(Airwaves::onClientSetup);
        modEventBus.addListener(Airwaves::onRegister);
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

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(Airwaves::check);
    }

    private static void check() {
        if (Minecraft.getInstance().getResourceManager().getResource(asResource("coconut.jpg")).isEmpty()) {
            System.exit(0);
        }
    }

    private static void onRegister(final RegisterEvent event) {
        AirwaveArmInteractionPoints.prepare();
    }
}
