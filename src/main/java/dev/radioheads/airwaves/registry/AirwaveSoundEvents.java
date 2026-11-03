package dev.radioheads.airwaves.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.radioheads.airwaves.Airwaves;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class AirwaveSoundEvents {
    private AirwaveSoundEvents() {}

    public static final Map<ResourceLocation, SoundEntry> ALL = new HashMap<>();


    public static final SoundEntry
    TEXURA = create("texura")
            .category(SoundSource.BLOCKS)
            .attenuationDistance(20)
            .build(),

    TEXURA2 = create("texura2")
            .category(SoundSource.BLOCKS)
            .attenuationDistance(20)
            .build(),

    TEXTURA_SHORT = create("textura_short")
            .category(SoundSource.BLOCKS)
            .attenuationDistance(20)
            .build()

    ;

    private static AirwaveSoundEvents.SoundEntryBuilder create(String name) {
        return create(Airwaves.asResource(name));
    }

    public static AirwaveSoundEvents.SoundEntryBuilder create(ResourceLocation id) {
        return new AirwaveSoundEvents.SoundEntryBuilder(id);
    }

    public static void prepare() {
        for (AirwaveSoundEvents.SoundEntry entry : ALL.values())
            entry.prepare();
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.SOUND_EVENT, helper -> {
            for (AirwaveSoundEvents.SoundEntry entry : ALL.values())
                entry.register(helper);
        });
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (AirwaveSoundEvents.SoundEntry entry : ALL.values())
            if (entry.hasSubtitle())
                consumer.accept(entry.getSubtitleKey(), entry.getSubtitle());
    }

    public static AirwaveSoundEvents.SoundEntryProvider provider(DataGenerator generator) {
        return new AirwaveSoundEvents.SoundEntryProvider(generator);
    }

    public static void playItemPickup(Player player) {
        player.level()
                .playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
                        1f + player.level().random.nextFloat());
    }

    public static class SoundEntryProvider implements DataProvider {
        private final PackOutput output;

        public SoundEntryProvider(@NotNull DataGenerator generator) {
            output = generator.getPackOutput();
        }

        @Override
        public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
            return generate(output.getOutputFolder(), cache);
        }

        @Override
        public @NotNull String getName() {
            return "Airwave's Custom Sounds";
        }

        public CompletableFuture<?> generate(Path path, CachedOutput cache) {
            path = path.resolve("assets/airwaves");
            JsonObject json = new JsonObject();
            ALL.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> entry.getValue().write(json));
            return DataProvider.saveStable(cache, json, path.resolve("sounds.json"));
        }

    }

    public record ConfiguredSoundEvent(Supplier<SoundEvent> event, float volume, float pitch) {
    }

    public static class SoundEntryBuilder {

        protected ResourceLocation id;
        protected String subtitle = "unregistered";
        protected SoundSource category = SoundSource.BLOCKS;
        protected List<ConfiguredSoundEvent> wrappedEvents;
        protected List<ResourceLocation> variants;
        protected int attenuationDistance;

        public SoundEntryBuilder(ResourceLocation id) {
            wrappedEvents = new ArrayList<>();
            variants = new ArrayList<>();
            this.id = id;
        }

        public AirwaveSoundEvents.SoundEntryBuilder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public AirwaveSoundEvents.SoundEntryBuilder attenuationDistance(int distance) {
            this.attenuationDistance = distance;
            return this;
        }

        public AirwaveSoundEvents.SoundEntryBuilder noSubtitle() {
            this.subtitle = null;
            return this;
        }

        public AirwaveSoundEvents.SoundEntryBuilder category(SoundSource category) {
            this.category = category;
            return this;
        }

        public AirwaveSoundEvents.SoundEntryBuilder addVariant(String name) {
            return addVariant(Airwaves.asResource(name));
        }

        public AirwaveSoundEvents.SoundEntryBuilder addVariant(ResourceLocation id) {
            variants.add(id);
            return this;
        }

        public AirwaveSoundEvents.SoundEntryBuilder playExisting(Supplier<SoundEvent> event, float volume, float pitch) {
            wrappedEvents.add(new AirwaveSoundEvents.ConfiguredSoundEvent(event, volume, pitch));
            return this;
        }

        public AirwaveSoundEvents.SoundEntryBuilder playExisting(SoundEvent event, float volume, float pitch) {
            return playExisting(() -> event, volume, pitch);
        }

        public AirwaveSoundEvents.SoundEntryBuilder playExisting(SoundEvent event) {
            return playExisting(event, 1, 1);
        }

        public AirwaveSoundEvents.SoundEntryBuilder playExisting(Holder<SoundEvent> event) {
            return playExisting(event::value, 1, 1);
        }

        public AirwaveSoundEvents.SoundEntry build() {
            AirwaveSoundEvents.SoundEntry entry =
                    wrappedEvents.isEmpty() ? new AirwaveSoundEvents.CustomSoundEntry(id, variants, subtitle, category, attenuationDistance)
                            : new AirwaveSoundEvents.WrappedSoundEntry(id, subtitle, wrappedEvents, category, attenuationDistance);
            ALL.put(entry.getId(), entry);
            return entry;
        }

    }

    public static abstract class SoundEntry {

        protected ResourceLocation id;
        protected String subtitle;
        protected SoundSource category;
        protected int attenuationDistance;

        public SoundEntry(ResourceLocation id, String subtitle, SoundSource category, int attenuationDistance) {
            this.id = id;
            this.subtitle = subtitle;
            this.category = category;
            this.attenuationDistance = attenuationDistance;
        }

        public abstract void prepare();

        public abstract void register(RegisterEvent.RegisterHelper<SoundEvent> registry);

        public abstract void write(JsonObject json);

        public abstract Holder<SoundEvent> getMainEventHolder();

        public abstract SoundEvent getMainEvent();

        public String getSubtitleKey() {
            return id.getNamespace() + ".subtitle." + id.getPath();
        }

        public ResourceLocation getId() {
            return id;
        }

        public boolean hasSubtitle() {
            return subtitle != null;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void playOnServer(Level world, Vec3i pos) {
            playOnServer(world, pos, 1, 1);
        }

        public void playOnServer(Level world, Vec3i pos, float volume, float pitch) {
            play(world, null, pos, volume, pitch);
        }

        public void play(Level world, Player entity, Vec3i pos) {
            play(world, entity, pos, 1, 1);
        }

        public void playFrom(Entity entity) {
            playFrom(entity, 1, 1);
        }

        public void playFrom(Entity entity, float volume, float pitch) {
            if (!entity.isSilent())
                play(entity.level(), null, entity.blockPosition(), volume, pitch);
        }

        public void play(Level world, Player entity, Vec3i pos, float volume, float pitch) {
            play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
        }

        public void play(Level world, Player entity, Vec3 pos, float volume, float pitch) {
            play(world, entity, pos.x(), pos.y(), pos.z(), volume, pitch);
        }

        public abstract void play(Level world, Player entity, double x, double y, double z, float volume, float pitch);

        public void playAt(Level world, Vec3i pos, float volume, float pitch, boolean fade) {
            playAt(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch, fade);
        }

        public void playAt(Level world, Vec3 pos, float volume, float pitch, boolean fade) {
            playAt(world, pos.x(), pos.y(), pos.z(), volume, pitch, fade);
        }

        public abstract void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade);

    }

    private static class WrappedSoundEntry extends AirwaveSoundEvents.SoundEntry {

        private final List<AirwaveSoundEvents.ConfiguredSoundEvent> wrappedEvents;
        private final List<AirwaveSoundEvents.WrappedSoundEntry.CompiledSoundEvent> compiledEvents;

        public WrappedSoundEntry(ResourceLocation id, String subtitle,
                                 List<AirwaveSoundEvents.ConfiguredSoundEvent> wrappedEvents, SoundSource category, int attenuationDistance) {
            super(id, subtitle, category, attenuationDistance);
            this.wrappedEvents = wrappedEvents;
            compiledEvents = new ArrayList<>();
        }

        @Override
        public void prepare() {
            for (int i = 0; i < wrappedEvents.size(); i++) {
                AirwaveSoundEvents.ConfiguredSoundEvent wrapped = wrappedEvents.get(i);
                ResourceLocation location = getIdOf(i);
                DeferredHolder<SoundEvent, SoundEvent> event = DeferredHolder.create(Registries.SOUND_EVENT, location);
                compiledEvents.add(new AirwaveSoundEvents.WrappedSoundEntry.CompiledSoundEvent(event, wrapped.volume(), wrapped.pitch()));
            }
        }

        @Override
        public void register(RegisterEvent.RegisterHelper<SoundEvent> helper) {
            for (AirwaveSoundEvents.WrappedSoundEntry.CompiledSoundEvent compiledEvent : compiledEvents) {
                ResourceLocation location = compiledEvent.event().getId();
                helper.register(location, SoundEvent.createVariableRangeEvent(location));
            }
        }

        @Override
        public Holder<SoundEvent> getMainEventHolder() {
            return compiledEvents.getFirst().event();
        }

        @Override
        public SoundEvent getMainEvent() {
            return compiledEvents.getFirst().event().get();
        }

        protected ResourceLocation getIdOf(int i) {
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), i == 0 ? id.getPath() : id.getPath() + "_compounded_" + i);
        }

        @Override
        public void write(JsonObject json) {
            for (int i = 0; i < wrappedEvents.size(); i++) {
                AirwaveSoundEvents.ConfiguredSoundEvent event = wrappedEvents.get(i);
                JsonObject entry = new JsonObject();
                JsonArray list = new JsonArray();
                JsonObject s = new JsonObject();
                s.addProperty("name", event.event()
                        .get()
                        .getLocation()
                        .toString());
                s.addProperty("type", "event");
                if (attenuationDistance != 0)
                    s.addProperty("attenuation_distance", attenuationDistance);
                list.add(s);
                entry.add("sounds", list);
                if (i == 0 && hasSubtitle())
                    entry.addProperty("subtitle", getSubtitleKey());
                json.add(getIdOf(i).getPath(), entry);
            }
        }

        @Override
        public void play(Level world, Player entity, double x, double y, double z, float volume, float pitch) {
            for (AirwaveSoundEvents.WrappedSoundEntry.CompiledSoundEvent event : compiledEvents) {
                world.playSound(entity, x, y, z, event.event().get(), category, event.volume() * volume,
                        event.pitch() * pitch);
            }
        }

        @Override
        public void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
            for (AirwaveSoundEvents.WrappedSoundEntry.CompiledSoundEvent event : compiledEvents) {
                world.playLocalSound(x, y, z, event.event().get(), category, event.volume() * volume,
                        event.pitch() * pitch, fade);
            }
        }

        private record CompiledSoundEvent(DeferredHolder<SoundEvent, SoundEvent> event, float volume, float pitch) {
        }

    }

    private static class CustomSoundEntry extends AirwaveSoundEvents.SoundEntry {

        protected List<ResourceLocation> variants;
        protected DeferredHolder<SoundEvent, SoundEvent> event;

        public CustomSoundEntry(ResourceLocation id, List<ResourceLocation> variants, String subtitle,
                                SoundSource category, int attenuationDistance) {
            super(id, subtitle, category, attenuationDistance);
            this.variants = variants;
        }

        @Override
        public void prepare() {
            event = DeferredHolder.create(Registries.SOUND_EVENT, id);
        }

        @Override
        public void register(RegisterEvent.RegisterHelper<SoundEvent> helper) {
            ResourceLocation location = event.getId();
            helper.register(location, SoundEvent.createVariableRangeEvent(location));
        }

        @Override
        public Holder<SoundEvent> getMainEventHolder() {
            return event;
        }

        @Override
        public SoundEvent getMainEvent() {
            return event.get();
        }

        @Override
        public void write(JsonObject json) {
            JsonObject entry = new JsonObject();
            JsonArray list = new JsonArray();

            JsonObject s = new JsonObject();
            s.addProperty("name", id.toString());
            s.addProperty("type", "file");
            if (attenuationDistance != 0)
                s.addProperty("attenuation_distance", attenuationDistance);
            list.add(s);

            for (ResourceLocation variant : variants) {
                s = new JsonObject();
                s.addProperty("name", variant.toString());
                s.addProperty("type", "file");
                if (attenuationDistance != 0)
                    s.addProperty("attenuation_distance", attenuationDistance);
                list.add(s);
            }

            entry.add("sounds", list);
            if (hasSubtitle())
                entry.addProperty("subtitle", getSubtitleKey());
            json.add(id.getPath(), entry);
        }

        @Override
        public void play(Level world, Player entity, double x, double y, double z, float volume, float pitch) {
            world.playSound(entity, x, y, z, event.get(), category, volume, pitch);
        }

        @Override
        public void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
            world.playLocalSound(x, y, z, event.get(), category, volume, pitch, fade);
        }

    }
}
