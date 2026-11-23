package dev.radioheads.airwaves.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.audio.RadioAudio;
import dev.radioheads.airwaves.api.link.LinkBudget;
import dev.radioheads.airwaves.api.link.LinkEnd;
import dev.radioheads.airwaves.api.link.RadioTerrain;
import dev.radioheads.airwaves.api.link.SkyState;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.radio.RadioStation;
import dev.radioheads.airwaves.api.radio.Tuning;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.Optional;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class AirwavesCommand {
    private AirwavesCommand() {}

    private static final SuggestionProvider<CommandSourceStack> BAND_SUGGESTIONS = (context, builder) -> {
        for (RadioBand band : AirwaveRegistries.RADIO_BAND_REGISTRY) {
            builder.suggest(String.valueOf(AirwaveRegistries.RADIO_BAND_REGISTRY.getKey(band)));
        }
        return builder.buildFuture();
    };

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Airwaves.MOD_ID)
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("sky").executes(context -> sky(context.getSource())))
                .then(Commands.literal("link")
                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                                .then(Commands.argument("to", BlockPosArgument.blockPos())
                                        .then(Commands.argument("mhz", DoubleArgumentType.doubleArg(0.1))
                                                .executes(context -> link(
                                                        context.getSource(),
                                                        BlockPosArgument.getBlockPos(context, "from"),
                                                        BlockPosArgument.getBlockPos(context, "to"),
                                                        DoubleArgumentType.getDouble(context, "mhz")
                                                ))))))
                .then(playCommand())
                .then(playRangeCommand()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playCommand() {
        return Commands.literal("play")
                .then(Commands.argument("sound", ResourceLocationArgument.id())
                        .suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                        .then(Commands.argument("band", ResourceLocationArgument.id())
                                .suggests(BAND_SUGGESTIONS)
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("mhz", DoubleArgumentType.doubleArg(0.0))
                                                .executes(context -> play(
                                                        context.getSource(),
                                                        ResourceLocationArgument.getId(context, "sound"),
                                                        ResourceLocationArgument.getId(context, "band"),
                                                        BlockPosArgument.getBlockPos(context, "pos"),
                                                        DoubleArgumentType.getDouble(context, "mhz"),
                                                        1.0F, 1.0F))
                                                .then(Commands.argument("volume", FloatArgumentType.floatArg(0.0F))
                                                        .then(Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F))
                                                                .executes(context -> play(
                                                                        context.getSource(),
                                                                        ResourceLocationArgument.getId(context, "sound"),
                                                                        ResourceLocationArgument.getId(context, "band"),
                                                                        BlockPosArgument.getBlockPos(context, "pos"),
                                                                        DoubleArgumentType.getDouble(context, "mhz"),
                                                                        FloatArgumentType.getFloat(context, "volume"),
                                                                        FloatArgumentType.getFloat(context, "pitch")))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playRangeCommand() {
        return Commands.literal("playrange")
                .then(Commands.argument("sound", ResourceLocationArgument.id())
                        .suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                        .then(Commands.argument("band", ResourceLocationArgument.id())
                                .suggests(BAND_SUGGESTIONS)
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("fromMhz", DoubleArgumentType.doubleArg(0.0))
                                                .then(Commands.argument("toMhz", DoubleArgumentType.doubleArg(0.0))
                                                        .executes(context -> playRange(
                                                                context.getSource(),
                                                                ResourceLocationArgument.getId(context, "sound"),
                                                                ResourceLocationArgument.getId(context, "band"),
                                                                BlockPosArgument.getBlockPos(context, "pos"),
                                                                DoubleArgumentType.getDouble(context, "fromMhz"),
                                                                DoubleArgumentType.getDouble(context, "toMhz"),
                                                                1.0F, 1.0F))
                                                        .then(Commands.argument("volume", FloatArgumentType.floatArg(0.0F))
                                                                .then(Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F))
                                                                        .executes(context -> playRange(
                                                                                context.getSource(),
                                                                                ResourceLocationArgument.getId(context, "sound"),
                                                                                ResourceLocationArgument.getId(context, "band"),
                                                                                BlockPosArgument.getBlockPos(context, "pos"),
                                                                                DoubleArgumentType.getDouble(context, "fromMhz"),
                                                                                DoubleArgumentType.getDouble(context, "toMhz"),
                                                                                FloatArgumentType.getFloat(context, "volume"),
                                                                                FloatArgumentType.getFloat(context, "pitch"))))))))));
    }

    private static int play(CommandSourceStack source, ResourceLocation soundId, ResourceLocation bandId, BlockPos pos, double mhz, float volume, float pitch) {
        RadioBand band = band(source, bandId);
        if (band == null) {
            return 0;
        }
        long hertz = Math.round(mhz * 1.0E6);
        if (!band.contains(hertz)) {
            source.sendFailure(Component.literal("Frequency " + mhz + " MHz is outside " + bandId));
            return 0;
        }

        Holder<SoundEvent> sound = Holder.direct(SoundEvent.createVariableRangeEvent(soundId));
        RadioAudio.playAt(source.getLevel(), pos, sound, new Tuning(hertz, Holder.direct(band)), volume, pitch);
        send(source, "Broadcast %s at %s on %s, volume %.2f, pitch %.2f", soundId, band.display(hertz), bandId, volume, pitch);
        return Command.SINGLE_SUCCESS;
    }

    private static int playRange(CommandSourceStack source, ResourceLocation soundId, ResourceLocation bandId,
                                 BlockPos pos, double fromMhz, double toMhz, float volume, float pitch) {
        RadioBand band = band(source, bandId);
        if (band == null) {
            return 0;
        }
        long from = Math.round(fromMhz * 1.0E6);
        long to = Math.round(toMhz * 1.0E6);
        if (!band.contains(from) || !band.contains(to)) {
            source.sendFailure(Component.literal("Frequency range " + fromMhz + "–" + toMhz + " MHz is outside " + bandId));
            return 0;
        }

        Holder<SoundEvent> sound = Holder.direct(SoundEvent.createVariableRangeEvent(soundId));
        RadioAudio.playBetween(source.getLevel(), pos, sound, new Tuning(from, Holder.direct(band)), from, to, volume, pitch);
        send(source, "Broadcast %s on %s–%s (%s), volume %.2f, pitch %.2f", soundId, band.display(from), band.display(to), bandId, volume, pitch);
        return Command.SINGLE_SUCCESS;
    }

    private static RadioBand band(CommandSourceStack source, ResourceLocation id) {
        RadioBand band = AirwaveRegistries.RADIO_BAND_REGISTRY.get(id);
        if (band == null) {
            StringBuilder available = new StringBuilder();
            for (RadioBand candidate : AirwaveRegistries.RADIO_BAND_REGISTRY) {
                if (!available.isEmpty()) {
                    available.append(", ");
                }
                available.append(AirwaveRegistries.RADIO_BAND_REGISTRY.getKey(candidate));
            }
            source.sendFailure(Component.literal("Unknown radio band " + id + ". Available: " + available));
        }
        return band;
    }

    private static int sky(CommandSourceStack source) {
        SkyState sky = SkyState.of(source.getLevel());
        send(source, "Daylight %.0f%%, solar activity %.0f%%, critical frequency %.1f MHz",
                sky.daylight() * 100.0, sky.solarActivity() * 100.0, sky.criticalMhz());
        send(source, "Ionosphere %s, flare %s, storm %s, tropo %s",
                sky.ionosphere(), sky.flare(), sky.storm(), sky.tropo());
        return Command.SINGLE_SUCCESS;
    }

    private static int link(CommandSourceStack source, BlockPos from, BlockPos to, double mhz) {
        long hertz = Math.round(mhz * 1.0E6);
        Optional<RadioBand> band = AirwaveRegistries.RADIO_BAND_REGISTRY.stream()
                .filter(candidate -> candidate.contains(hertz))
                .findFirst();
        if (band.isEmpty()) {
            source.sendFailure(Component.literal("No band covers " + mhz + " MHz"));
            return 0;
        }

        ServerLevel level = source.getLevel();
        RadioTerrain terrain = RadioTerrain.of(level);
        SkyState sky = SkyState.of(level);
        Modulation modulation = band.get().modulation().value();
        Vec3 a = antenna(level, from);
        Vec3 b = antenna(level, to);
        LinkEnd endA = linkEnd(level, from, mhz, terrain.siteHeight(a), b);
        LinkEnd endB = linkEnd(level, to, mhz, terrain.siteHeight(b), a);
        double blocks = a.distanceTo(b);
        LinkBudget.Surroundings towardB = LinkBudget.surroundings(terrain, mhz, a, b);
        LinkBudget.Surroundings towardA = LinkBudget.surroundings(terrain, mhz, b, a);

        send(source, "%s over %.0f blocks, ridge loss %.1f dB, %.0f%% sea, cover %.1f dB, tropo %s",
                band.get().display(hertz), blocks, towardB.obstructionDb(), towardB.seaFraction() * 100.0, towardB.coverDb(), sky.tropo());
        report(source, "From", endA, endB, LinkBudget.evaluate(endA, endB, hertz, modulation, blocks, towardB, sky));
        report(source, "To", endB, endA, LinkBudget.evaluate(endB, endA, hertz, modulation, blocks, towardA, sky));
        return Command.SINGLE_SUCCESS;
    }

    private static Vec3 antenna(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RadioStation station ? station.antenna() : Vec3.atCenterOf(pos);
    }

    private static LinkEnd linkEnd(ServerLevel level, BlockPos pos, double mhz, double height, Vec3 toward) {
        return level.getBlockEntity(pos) instanceof RadioStation station ? station.linkEnd(mhz, height, toward) : LinkEnd.handheld(height);
    }

    private static void report(CommandSourceStack source, String label, LinkEnd tx, LinkEnd rx, LinkBudget budget) {
        send(source, "%s: %.0f W at height %.0f, tx gain %.1f dB, rx gain %.1f dB, path %.1f dB via the %s, received %.1f dBm, noise %.1f dBm, margin %.1f dB, quality %.0f%%",
                label, tx.watts(), tx.heightBlocks(), tx.gainDb(), rx.gainDb(), budget.pathLossDb(), budget.viaSky() ? "sky" : "ground",
                budget.receivedDbm(), budget.noiseDbm(), budget.marginDb(), budget.quality() * 100.0F);
    }

    private static void send(CommandSourceStack source, String format, Object... args) {
        String message = String.format(Locale.ROOT, format, args);
        source.sendSuccess(() -> Component.literal(message), false);
    }
}
