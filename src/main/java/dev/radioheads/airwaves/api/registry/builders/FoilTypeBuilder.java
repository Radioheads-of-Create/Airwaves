package dev.radioheads.airwaves.api.registry.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.api.foil.FoilType;
import dev.radioheads.airwaves.api.registry.foil.FoilTypeEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FoilTypeBuilder<P> extends AbstractBuilder<FoilType, FoilType, P, FoilTypeBuilder<P>> {
    private final NonNullSupplier<FoilType> factory;

    public FoilTypeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<FoilType>> registryKey, NonNullSupplier<FoilType> factory) {
        super(owner, parent, name, callback, registryKey);
        this.factory = factory;
    }

    @Override
    protected FoilType createEntry() {
        return factory.get();
    }

    @Override
    protected RegistryEntry<FoilType, FoilType> createEntryWrapper(DeferredHolder<FoilType, FoilType> delegate) {
        return new FoilTypeEntry(getOwner(),  delegate);
    }

    @Override
    public FoilTypeEntry register() {
        return (FoilTypeEntry) super.register();
    }
}
