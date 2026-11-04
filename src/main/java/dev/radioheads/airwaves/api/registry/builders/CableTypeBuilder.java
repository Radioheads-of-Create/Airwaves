package dev.radioheads.airwaves.api.registry.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.api.cable.CableType;
import dev.radioheads.airwaves.api.registry.cable.CableTypeEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CableTypeBuilder<P> extends AbstractBuilder<CableType, CableType, P, CableTypeBuilder<P>> {
    private final NonNullSupplier<CableType> factory;

    public CableTypeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<CableType>> registryKey, NonNullSupplier<CableType> factory) {
        super(owner, parent, name, callback, registryKey);
        this.factory = factory;
    }

    @Override
    protected CableType createEntry() {
        return factory.get();
    }

    @Override
    protected RegistryEntry<CableType, CableType> createEntryWrapper(DeferredHolder<CableType, CableType> delegate) {
        return new CableTypeEntry(getOwner(), delegate);
    }

    @Override
    public CableTypeEntry register() {
        return (CableTypeEntry) super.register();
    }
}
