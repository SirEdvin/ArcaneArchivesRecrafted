package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import java.util.IdentityHashMap;
import net.minecraft.world.level.Level;

/** Client-owned effects, retained independently of any open configuration screen. */
public final class BrazierRanges {
    public static final BrazierRanges INSTANCE = new BrazierRanges();
    private final IdentityHashMap<BrazierBlockEntity, BrazierRangeState> effects = new IdentityHashMap<>();
    private static boolean live(BrazierBlockEntity device, Level world) {
        return world != null && device.getLevel() == world && !device.isRemoved()
            && world.hasChunkAt(device.getBlockPos()) && world.getBlockEntity(device.getBlockPos()) == device;
    }
    public BrazierRangeState state(BrazierBlockEntity device) { return effects.get(device); }
    public void forEach(Level world, java.util.function.BiConsumer<BrazierBlockEntity, BrazierRangeState> draw) {
        effects.forEach((device, state) -> {
            if (state.showing() && live(device, world)) draw.accept(device, state);
        });
    }
    public void toggle(BrazierBlockEntity device, Level world) {
        if (!live(device, world)) return;
        var state = effects.computeIfAbsent(device, ignored -> new BrazierRangeState());
        state.toggle();
        if (!state.showing()) effects.remove(device);
    }
    public void tick(Level world) {
        effects.entrySet().removeIf(entry -> {
            entry.getValue().tick(live(entry.getKey(), world));
            return !entry.getValue().showing();
        });
    }
    public static void initialize() { ClientHooks.initialize(); }
    private static final class ClientHooks {
    private static void clientTick() {
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.level == null || !client.isPaused()) INSTANCE.tick(client.level);
    }
    public static void initialize() {
        //? if fabric {
        BrazierRangeRenderer.initialize();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> clientTick());
        //?} else if forge {
        /*BrazierRangeRenderer.initialize();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.TickEvent.ClientTickEvent event) -> {
                if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) clientTick();
            });
        *///?} else {
        /*BrazierRangeRenderer.initialize();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.client.event.ClientTickEvent.Post event) -> clientTick());
        *///?}
    }
    }
}
