package dev.radioheads.airwaves.registry;

import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.menu.TuningTableMenu;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class AirwaveMenuTypes {
    private AirwaveMenuTypes() {}

    public static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(Registries.MENU, Airwaves.MOD_ID);

    public static final Supplier<MenuType<TuningTableMenu>> TUNING_TABLE = MENU.register("tuning_table", () -> IMenuTypeExtension.create((id, inv, buf) -> new TuningTableMenu(id, inv)));

    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(String name, MenuBuilder.ForgeMenuFactory<C> factory, NonNullSupplier<MenuBuilder.ScreenFactory<C, S>> screenFactory) {
        return Airwaves.registrate().menu(name, factory, screenFactory).register();
    }
}
