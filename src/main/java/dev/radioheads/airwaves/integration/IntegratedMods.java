package dev.radioheads.airwaves.integration;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.Locale;

public enum IntegratedMods {
    SABLE,
    COMPUTER_CRAFT("computercraft"),
    KUBE("kubejs"),
    CREATE_ORE_EXCAVATION("createoreexcavation")
    ;

    public final String modId;

    IntegratedMods() {
        modId = name().toLowerCase(Locale.ROOT);
    }

    IntegratedMods(String modId) {
        this.modId = modId;
    }

    public boolean isLoaded() {
        return isModLoaded(modId);
    }

    public ModLoadedCondition getCondition() {
        return new ModLoadedCondition(modId);
    }

    public static boolean isModLoaded(String modid) {
        return LoadingModList.get().getModFileById(modid) != null;
    }
}
