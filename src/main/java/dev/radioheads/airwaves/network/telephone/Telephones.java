package dev.radioheads.airwaves.network.telephone;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.cable.CableGraph;
import dev.radioheads.airwaves.content.entity.block.TelephoneBlockEntity;
import dev.radioheads.airwaves.content.telephone.EarpieceBinding;
import dev.radioheads.airwaves.content.telephone.PhoneNumbers;
import dev.radioheads.airwaves.network.RadioVoice;
import dev.radioheads.airwaves.registry.AirwaveDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

// Telephone calls
@EventBusSubscriber(modid = Airwaves.MOD_ID)
public final class Telephones {
    public static final int RING_TICKS = 30 * 20;
    public static final double EARPIECE_RANGE = 8.0;
    private static final double DROP_RANGE = EARPIECE_RANGE - 1.5;
    // Pickup radius
    public static final double PICKUP_RANGE = 4.0;
    private static final int RANGE_TICKS = 5;
    private static final int ROUTE_TICKS = 20;

    private static final Map<UUID, TelephoneBlockEntity> LOADED = new HashMap<>();
    private static final Map<UUID, Call> CALLS = new HashMap<>();
    private static final List<Later> LATER = new ArrayList<>();
    private static volatile Map<UUID, List<Delivery>> feeds = Map.of();
    private static List<Leg> legs = List.of();

    // Voice route
    public record Delivery(UUID listener, UUID channel, long delayMs) {}

    // Call leg
    public record Leg(ServerLevel level, ServerPlayer from, ServerPlayer to, UUID channel, long delayMs) {
        public int delayTicks() {
            return (int) Math.round(this.delayMs / 50.0);
        }
    }

    private record Later(long tick, Runnable task) {}

    private static final class Call {
        private final ServerLevel level;
        private final UUID caller;
        private final UUID callee;
        private final long started;
        private boolean answered;
        // Call delay each way in ms
        private double forward;
        private OptionalDouble back;

        private Call(ServerLevel level, UUID caller, UUID callee, long started, double forward, OptionalDouble back) {
            this.level = level;
            this.caller = caller;
            this.callee = callee;
            this.started = started;
            this.forward = forward;
            this.back = back;
        }
    }

    private Telephones() {}

    public static void loaded(TelephoneBlockEntity phone) {
        LOADED.put(phone.phone(), phone);
    }

    public static void unloaded(TelephoneBlockEntity phone) {
        if (LOADED.get(phone.phone()) == phone) {
            LOADED.remove(phone.phone());
            Call call = CALLS.get(phone.phone());
            if (call != null) {
                end(call);
            }
        }
    }

    public static void removed(ServerLevel level, TelephoneBlockEntity phone, boolean hooked) {
        Call call = CALLS.get(phone.phone());
        if (call != null) {
            end(call);
        }
        if (!phone.number().isEmpty()) {
            TelephoneDirectory.of(level).retire(phone.number());
        }
        if (hooked && phone.earpiece() != null) {
            Vec3 at = Vec3.atCenterOf(phone.getBlockPos());
            level.addFreshEntity(new ItemEntity(level, at.x, at.y, at.z, phone.earpieceStack()));
        }
    }

    public static boolean busy(TelephoneBlockEntity phone) {
        return CALLS.containsKey(phone.phone());
    }

    public static void refuse(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("airwaves.telephone." + key).withStyle(ChatFormatting.RED), true);
    }

    public static void dial(ServerLevel level, TelephoneBlockEntity caller, ServerPlayer player) {
        String number = caller.dialed();
        if (number.length() != PhoneNumbers.LENGTH) {
            refuse(player, "length");
            caller.clearDial();
            return;
        }
        TelephoneDirectory.Entry entry = TelephoneDirectory.of(level).find(number);
        if (entry == null) {
            refuse(player, "missing");
            caller.clearDial();
            return;
        }
        TelephoneBlockEntity callee = LOADED.get(entry.phone());
        CableGraph graph = CableGraph.of(level);
        OptionalDouble forward = callee == null || callee.getLevel() != level ? OptionalDouble.empty()
                : graph.route(caller.getBlockPos(), callee.getBlockPos());
        if (callee == null || callee == caller || entry.pos().isEmpty() || callee.earpiece() == null || !callee.hooked()
                || busy(callee) || forward.isEmpty()) {
            refuse(player, "unavailable");
            caller.clearDial();
            return;
        }
        Call call = new Call(level, caller.phone(), callee.phone(), level.getGameTime(), forward.getAsDouble(),
                graph.route(callee.getBlockPos(), caller.getBlockPos()));
        CALLS.put(caller.phone(), call);
        CALLS.put(callee.phone(), call);
        callee.ring(true);
    }

    public static void answer(TelephoneBlockEntity phone) {
        Call call = CALLS.get(phone.phone());
        if (call != null && call.callee.equals(phone.phone()) && !call.answered) {
            call.answered = true;
            phone.ring(false);
        }
    }

    public static void hungUp(TelephoneBlockEntity phone) {
        Call call = CALLS.get(phone.phone());
        if (call != null) {
            end(call);
        }
    }

    private static void end(Call call) {
        CALLS.remove(call.caller);
        CALLS.remove(call.callee);
        for (UUID end : List.of(call.caller, call.callee)) {
            TelephoneBlockEntity phone = LOADED.get(end);
            if (phone != null) {
                phone.ring(false);
                phone.clearDial();
            }
        }
    }

    // Earpiece holder
    @Nullable
    public static ServerPlayer holder(TelephoneBlockEntity phone) {
        UUID earpiece = phone.earpiece();
        if (earpiece == null || phone.hooked() || !(phone.getLevel() instanceof ServerLevel level)) {
            return null;
        }
        Vec3 at = Vec3.atCenterOf(phone.getBlockPos());
        for (ServerPlayer player : level.players()) {
            if (player.position().closerThan(at, EARPIECE_RANGE + 2.0)) {
                for (InteractionHand hand : InteractionHand.values()) {
                    EarpieceBinding binding = player.getItemInHand(hand).get(AirwaveDataComponents.EARPIECE);
                    if (binding != null && binding.earpiece().equals(earpiece)) {
                        return player;
                    }
                }
            }
        }
        return null;
    }

    public static List<Delivery> feedOf(UUID talker) {
        return feeds.getOrDefault(talker, List.of());
    }

    public static List<Leg> legs() {
        return legs;
    }

    public static void later(MinecraftServer server, int ticks, Runnable task) {
        if (ticks <= 0) {
            task.run();
        } else {
            LATER.add(new Later(server.getTickCount() + ticks, task));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int tick = server.getTickCount();

        List<Later> due = new ArrayList<>();
        for (Iterator<Later> it = LATER.iterator(); it.hasNext(); ) {
            Later later = it.next();
            if (later.tick() <= tick) {
                due.add(later);
                it.remove();
            }
        }
        due.forEach(later -> later.task().run());

        Set<Call> calls = new HashSet<>(CALLS.values());
        for (Call call : calls) {
            if (!call.answered && call.level.getGameTime() - call.started >= RING_TICKS) {
                end(call);
            } else if (tick % ROUTE_TICKS == 0) {
                reroute(call);
            }
        }

        if (tick % RANGE_TICKS == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                keepInRange(player);
            }
        }

        List<Leg> nextLegs = new ArrayList<>();
        Map<UUID, List<Delivery>> nextFeeds = new HashMap<>();
        for (Call call : new HashSet<>(CALLS.values())) {
            if (call.answered) {
                leg(call.level, call.caller, call.callee, OptionalDouble.of(call.forward), nextLegs, nextFeeds);
                leg(call.level, call.callee, call.caller, call.back, nextLegs, nextFeeds);
            }
        }
        legs = nextLegs;
        feeds = nextFeeds;
    }

    private static void reroute(Call call) {
        TelephoneBlockEntity caller = LOADED.get(call.caller);
        TelephoneBlockEntity callee = LOADED.get(call.callee);
        if (caller == null || callee == null) {
            end(call);
            return;
        }
        CableGraph graph = CableGraph.of(call.level);
        OptionalDouble forward = graph.route(caller.getBlockPos(), callee.getBlockPos());
        if (forward.isEmpty()) {
            end(call);
            return;
        }
        call.forward = forward.getAsDouble();
        call.back = graph.route(callee.getBlockPos(), caller.getBlockPos());
    }

    private static void leg(ServerLevel level, UUID from, UUID to, OptionalDouble delayMs, List<Leg> legs, Map<UUID, List<Delivery>> feeds) {
        TelephoneBlockEntity speaking = LOADED.get(from);
        TelephoneBlockEntity listening = LOADED.get(to);
        if (delayMs.isEmpty() || speaking == null || listening == null) {
            return;
        }
        ServerPlayer talkerSide = holder(speaking);
        ServerPlayer listener = holder(listening);
        if (talkerSide == null || listener == null) {
            return;
        }
        long delay = Math.round(delayMs.getAsDouble());
        legs.add(new Leg(level, talkerSide, listener, RadioVoice.channel(from, to), delay));
        for (ServerPlayer talker : level.players()) {
            if (talker != listener && talker.getEyePosition().closerThan(talkerSide.getEyePosition(), PICKUP_RANGE)) {
                feeds.computeIfAbsent(talker.getUUID(), id -> new ArrayList<>())
                        .add(new Delivery(listener.getUUID(), RadioVoice.channel(talker.getUUID(), to), delay));
            }
        }
    }

    // Cord length limit
    private static void keepInRange(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            EarpieceBinding binding = stack.get(AirwaveDataComponents.EARPIECE);
            TelephoneBlockEntity phone = binding == null ? null : LOADED.get(binding.phone());
            if (phone == null || phone.hooked() || !binding.earpiece().equals(phone.earpiece())) {
                continue;
            }
            Vec3 jack = Vec3.atCenterOf(phone.getBlockPos());
            if (phone.getLevel() != player.level()) {
                inventory.setItem(slot, ItemStack.EMPTY);
                player.drop(stack, false);
            } else if (!player.position().closerThan(jack, EARPIECE_RANGE)) {
                inventory.setItem(slot, ItemStack.EMPTY);
                Vec3 at = jack.add(player.position().subtract(jack).normalize().scale(DROP_RANGE));
                ItemEntity dropped = new ItemEntity(player.level(), at.x, at.y, at.z, stack);
                dropped.setDefaultPickUpDelay();
                player.level().addFreshEntity(dropped);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LOADED.clear();
        CALLS.clear();
        LATER.clear();
        feeds = Map.of();
        legs = List.of();
    }
}
