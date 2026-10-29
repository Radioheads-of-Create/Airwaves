package dev.radioheads.airwaves.datagen.advancement;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.annotations.NothingNullByDefault;
import dev.radioheads.airwaves.registry.AirwaveBlocks;
import dev.radioheads.airwaves.registry.AirwaveItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@NothingNullByDefault
public final class AirwaveAdvancementGen extends AdvancementProvider {
    public AirwaveAdvancementGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new AdvancementGenerator()));
    }

    private static final class AdvancementGenerator implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = root(saver);

            save(saver,
                    child(root, AirwaveItems.SPEAKER_CONE, "speaker_cone")
                            .addCriterion("get_speaker_cone", InventoryChangeTrigger.TriggerInstance.hasItems(AirwaveItems.SPEAKER_CONE)),
                    "radio");

            save(saver,
                    child(root, AirwaveItems.RESISTOR, "resistor")
                            .addCriterion("get_resistor", InventoryChangeTrigger.TriggerInstance.hasItems(AirwaveItems.RESISTOR)),
                    "resistance");
        }
    }


    private static Component title(String id) {
        return Component.translatable("advancement.airwaves." + id);
    }

    private static Component description(String id) {
        return Component.translatable("advancement.airwaves." + id + ".description");
    }

    private static AdvancementHolder root(Consumer<AdvancementHolder> saver) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        AirwaveBlocks.RADIO,
                        title("root"),
                        description("root"),
                        Airwaves.asResource("textures/gui/advancements/background.png"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false
                )
                .addCriterion("airwaves_root", PlayerTrigger.TriggerInstance.tick())
                .build(Airwaves.asResource("root"));
        saver.accept(root);
        return root;
    }

    private static Advancement.Builder child(AdvancementHolder parent, ItemLike icon, String id) {
        return child(parent, icon.asItem(), id, false);
    }

    private static Advancement.Builder child(AdvancementHolder parent, ItemLike icon, String id, boolean hidden) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                        icon,
                        title(id),
                        description(id),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        hidden
                );
    }

    private static AdvancementHolder save(Consumer<AdvancementHolder> saver, Advancement.Builder builder, String path) {
        AdvancementHolder holder = builder.build(Airwaves.asResource(path));
        saver.accept(holder);
        return holder;
    }
}