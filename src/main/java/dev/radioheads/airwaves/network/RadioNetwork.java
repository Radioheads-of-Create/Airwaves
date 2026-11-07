package dev.radioheads.airwaves.network;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.link.LinkBudget;
import dev.radioheads.airwaves.api.link.Propagation;
import dev.radioheads.airwaves.api.link.RadioTerrain;
import dev.radioheads.airwaves.api.link.SkyState;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.radio.RadioStation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class RadioNetwork {
    private static final int REFRESH_TICKS = 20;
    private static final Map<ResourceKey<Level>, Set<RadioStation>> STATIONS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<UUID, List<Route>>> ROUTES = new HashMap<>();

    public record Route(RadioStation receiver, double marginDb) {}

    private RadioNetwork() {}

    public static void add(Level level, RadioStation station) {
        STATIONS.computeIfAbsent(level.dimension(), key -> new LinkedHashSet<>()).add(station);
    }

    public static void remove(Level level, RadioStation station) {
        Set<RadioStation> stations = STATIONS.get(level.dimension());
        if (stations != null) {
            stations.remove(station);
        }
    }

    public static Set<RadioStation> stations(Level level) {
        return STATIONS.getOrDefault(level.dimension(), Set.of());
    }

    public static List<Route> routes(Level level, UUID station) {
        return ROUTES.getOrDefault(level.dimension(), Map.of()).getOrDefault(station, List.of());
    }

    public static Map<UUID, List<Route>> refresh(Collection<RadioStation> stations, RadioTerrain terrain, SkyState sky) {
        return refresh(stations, terrain, sky, 0L);
    }

    public static Map<UUID, List<Route>> refresh(Collection<RadioStation> stations, RadioTerrain terrain, SkyState sky, long gameTime) {
        TreeMap<Long, List<RadioStation>> sources = new TreeMap<>();
        for (RadioStation station : stations) {
            if (station.transmits()) {
                sources.computeIfAbsent(station.hertz(), hertz -> new ArrayList<>()).add(station);
            }
        }

        Map<UUID, List<Route>> routes = new HashMap<>();
        for (RadioStation receiver : stations) {
            if (receiver.receives()) {
                long bandwidth = receiver.modulation().value().bandwidthHertz();
                List<RadioStation> nearby = new ArrayList<>();
                sources.subMap(receiver.hertz() - bandwidth, false, receiver.hertz() + bandwidth, false).values().forEach(nearby::addAll);
                receiver.hear(listen(receiver, nearby, routes, terrain, sky, gameTime));
            }
        }
        return routes;
    }

    private record Signal(RadioStation source, double dbm, double noiseDbm) {}

    // Co-channel and adjacent channel interference with FM capture
    private static float listen(RadioStation receiver, List<RadioStation> sources, Map<UUID, List<Route>> routes, RadioTerrain terrain, SkyState sky, long gameTime) {
        Modulation modulation = receiver.modulation().value();
        List<Signal> signals = new ArrayList<>();
        for (RadioStation source : sources) {
            if (source == receiver) {
                continue;
            }

            LinkBudget budget = LinkBudget.between(source, receiver, terrain, sky);
            double dbm = budget.receivedDbm()
                    - Propagation.channelRejectionDb(source.hertz() - receiver.hertz(), modulation.bandwidthHertz())
                    + Fading.db(source.id(), receiver.id(), budget.viaSky(), source.moving() || receiver.moving(), gameTime);
            signals.add(new Signal(source, dbm, budget.noiseDbm()));
        }

        float strongest = 0.0F;
        for (Signal signal : signals) {
            double interference = 0.0;
            for (Signal other : signals) {
                if (other != signal && other.source().onAir() && other.dbm() > signal.dbm() - modulation.captureDb()) {
                    interference += Math.pow(10.0, other.dbm() / 10.0);
                }
            }

            double marginDb = signal.dbm() - 10.0 * Math.log10(Math.pow(10.0, signal.noiseDbm() / 10.0) + interference) - modulation.thresholdDb();
            if (marginDb < 0.0) {
                continue;
            }

            RadioStation source = signal.source();
            routes.computeIfAbsent(source.id(), id -> new ArrayList<>()).add(new Route(receiver, marginDb));
            if (source.onAir()) {
                strongest = Math.max(strongest, modulation.quality(marginDb));
            }
        }
        return strongest;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.getGameTime() % REFRESH_TICKS == 0) {
            ROUTES.put(level.dimension(), refresh(stations(level), RadioTerrain.of(level), SkyState.of(level), level.getGameTime()));
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        STATIONS.clear();
        ROUTES.clear();
    }
}
