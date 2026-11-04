package dev.radioheads.airwaves.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.api.antenna.AntennaType;
import dev.radioheads.airwaves.api.antenna.Tier;
import dev.radioheads.airwaves.api.cable.CableType;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import dev.radioheads.airwaves.api.registry.builders.*;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class AirwaveRegistrate extends CreateRegistrate {
    protected AirwaveRegistrate(String modid) {
        super(modid);
    }

    public AirwaveRegistrate setTooltipModifierFactory(@Nullable Function<Item, TooltipModifier> factory) {
        currentTooltipModifierFactory = factory;
        return this;
    }


    public static String autoLang(String id) {
        StringBuilder builder = new StringBuilder();
        boolean b = true;
        for (char c: id.toCharArray()) {
            if(c == '_') {
                builder.append(' ');
                b = true;
            } else {
                builder.append(b ? String.valueOf(c).toUpperCase() : c);
                b = false;
            }
        }
        return builder.toString();
    }

    public static AirwaveRegistrate create(String id) {
        return new AirwaveRegistrate(id);
    }

    public RadioBandBuilder<AirwaveRegistrate> radioBand(NonNullSupplier<RadioBand> factory) {
        return radioBand((AirwaveRegistrate) self(), factory);
    }

    public RadioBandBuilder<AirwaveRegistrate> radioBand(String name, NonNullSupplier<RadioBand> factory) {
        return radioBand((AirwaveRegistrate) self(), name, factory);
    }

    public <P> RadioBandBuilder<P> radioBand(P parent, NonNullSupplier<RadioBand> factory) {
        return radioBand(parent, currentName(), factory);
    }

    public <P> RadioBandBuilder<P> radioBand(P parent, String name, NonNullSupplier<RadioBand> factory) {
        return entry(name, callback -> new RadioBandBuilder<>(this, parent, name, callback, AirwaveRegistries.RADIO_BAND, factory));
    }

    public ModulationBuilder<AirwaveRegistrate> modulation(NonNullSupplier<Modulation> factory) {
        return modulation((AirwaveRegistrate) self(), factory);
    }

    public ModulationBuilder<AirwaveRegistrate> modulation(String name, NonNullSupplier<Modulation> factory) {
        return modulation((AirwaveRegistrate) self(), name, factory);
    }

    public <P> ModulationBuilder<P> modulation(P parent, NonNullSupplier<Modulation> factory) {
        return modulation(parent, currentName(), factory);
    }

    public <P> ModulationBuilder<P> modulation(P parent, String name, NonNullSupplier<Modulation> factory) {
        return entry(name, callback -> new ModulationBuilder<>(this, parent, name, callback, AirwaveRegistries.MODULATION, factory));
    }

    public TierBuilder<AirwaveRegistrate> tier(NonNullSupplier<Tier> factory) {
        return tier((AirwaveRegistrate) self(), factory);
    }

    public TierBuilder<AirwaveRegistrate> tier(String name, NonNullSupplier<Tier> factory) {
        return tier((AirwaveRegistrate) self(), name, factory);
    }

    public <P> TierBuilder<P> tier(P parent, NonNullSupplier<Tier> factory) {
        return tier(parent, currentName(), factory);
    }

    public <P> TierBuilder<P> tier(P parent, String name, NonNullSupplier<Tier> factory) {
        return entry(name, callback -> new TierBuilder<>(this, parent, name, callback, AirwaveRegistries.TIER, factory));
    }

    public AntennaTypeBuilder<AirwaveRegistrate> antennaType(NonNullSupplier<AntennaType> factory) {
        return antennaType((AirwaveRegistrate) self(), factory);
    }

    public AntennaTypeBuilder<AirwaveRegistrate> antennaType(String name, NonNullSupplier<AntennaType> factory) {
        return antennaType((AirwaveRegistrate) self(), name, factory);
    }

    public <P> AntennaTypeBuilder<P> antennaType(P parent, NonNullSupplier<AntennaType> factory) {
        return antennaType(parent, currentName(), factory);
    }

    public <P> AntennaTypeBuilder<P> antennaType(P parent, String name, NonNullSupplier<AntennaType> factory) {
        return entry(name, callback -> new AntennaTypeBuilder<>(this, parent, name, callback, AirwaveRegistries.ANTENNA_TYPE, factory));
    }

    public CableTypeBuilder<AirwaveRegistrate> cableType(NonNullSupplier<CableType> factory) {
        return cableType((AirwaveRegistrate) self(), factory);
    }

    public CableTypeBuilder<AirwaveRegistrate> cableType(String name, NonNullSupplier<CableType> factory) {
        return cableType((AirwaveRegistrate) self(), name, factory);
    }

    public <P> CableTypeBuilder<P> cableType(P parent, NonNullSupplier<CableType> factory) {
        return cableType(parent, currentName(), factory);
    }

    public <P> CableTypeBuilder<P> cableType(P parent, String name, NonNullSupplier<CableType> factory) {
        return entry(name, callback -> new CableTypeBuilder<>(this, parent, name, callback, AirwaveRegistries.CABLE_TYPE, factory));
    }
}
