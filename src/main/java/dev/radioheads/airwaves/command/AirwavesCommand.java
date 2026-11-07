package dev.radioheads.airwaves.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.link.LinkBudget;
import dev.radioheads.airwaves.api.link.LinkEnd;
import dev.radioheads.airwaves.api.link.RadioTerrain;
import dev.radioheads.airwaves.api.link.SkyState;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.radio.RadioStation;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.Optional;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class AirwavesCommand {
    private AirwavesCommand() {}

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
                                                )))))));
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
