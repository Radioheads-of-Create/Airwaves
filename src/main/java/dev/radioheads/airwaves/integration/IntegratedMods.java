package dev.radioheads.airwaves.integration;

import net.neoforged.fml.loading.LoadingModList;

import java.util.Locale;

public enum IntegratedMods {
    SABLE,
    COMPUTER_CRAFT("computercraft"),
    KUBE("kubejs"),
    ;

    public final String modId;

    IntegratedMods() {
        modId = name().toLowerCase(Locale.ROOT);
    }

    IntegratedMods(String modId) {
        this.modId = modId;
    }

    public boolean isLoaded() {
        // English just in case
        return isModLoaded(name().toLowerCase(Locale.ENGLISH));
    }

    public static boolean isModLoaded(String modid) {
        return LoadingModList.get().getModFileById(modid) != null;
    }
}
