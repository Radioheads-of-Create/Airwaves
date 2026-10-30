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
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(
                            AirwaveBlocks.RADIO,
                            Component.translatable("advancement.airwaves.root"),
                            Component.translatable("advancement.airwaves.root.description"),
                            Airwaves.asResource("textures/gui/advancements/background.png"),
                            AdvancementType.TASK,
                            false,
                            false,
                            false
                    )
                    .addCriterion("airwaves_root", PlayerTrigger.TriggerInstance.tick())
                    .build(Airwaves.asResource("root"));
            saver.accept(root);

            //TODO Change?
            AdvancementHolder speakerCone = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            AirwaveItems.SPEAKER_CONE,
                            Component.translatable("advancement.airwaves.speaker_cone"),
                            Component.translatable("advancement.airwaves.speaker_cone.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_speaker_cone", InventoryChangeTrigger.TriggerInstance.hasItems(AirwaveItems.SPEAKER_CONE))
                    .build(Airwaves.asResource("radio"));
            saver.accept(speakerCone);

            AdvancementHolder resistor = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            AirwaveItems.RESISTOR,
                            Component.translatable("advancement.airwaves.resistor"),
                            Component.translatable("advancement.airwaves.resistor.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_resistor", InventoryChangeTrigger.TriggerInstance.hasItems(AirwaveItems.RESISTOR))
                    .build(Airwaves.asResource("resistance"));
            saver.accept(resistor);

            AdvancementHolder silver_sheet = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            AirwaveItems.SILVER_SHEET,
                            Component.translatable("advancement.airwaves.pressed_shape"),
                            Component.translatable("advancement.airwaves.pressed_shape.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    ).addCriterion("get_silver_sheet", InventoryChangeTrigger.TriggerInstance.hasItems(AirwaveItems.SILVER_SHEET))
                    .build(Airwaves.asResource("pressed_shape"));
            saver.accept(silver_sheet);
        }
    }
}