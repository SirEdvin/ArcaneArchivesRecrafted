package com.aranaira.arcanearchives.items;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** One live worn socket; detached gem state is published only to its unchanged owner. */
public final class WornGemSocket {
    private static final Map<Player, State> STATES = new WeakHashMap<>();
    private WornGemSocket() {}

    public static void initialize() {
        //? if fabric {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("trinkets")) Native.register();
        //?} else if forge {
        /*if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) Native.register();
        *///?} else {
        /*if (net.neoforged.fml.ModList.get().isLoaded("curios")) Native.register();
        *///?}
    }

    private record Binding(ItemStack socket, Runnable changed) {}
    private static final class State {
        final ItemStack socket;
        final ItemStack gem;
        ItemStack snapshot;
        State(ItemStack socket, Player player) {
            this.socket = socket;
            this.gem = GemSocketItem.gem(socket, player.level());
            this.snapshot = socket.copy();
        }
    }
    private static Binding binding(Player player) {
        if (!player.isAlive() || player.isSpectator()) return null;
        if (!player.level().isClientSide && (!(player.level() instanceof ServerLevel level)
                || !level.getServer().isSameThread())) return null;
        // Keep optional API classes behind their loader-presence boundary.
        //? if fabric {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("trinkets")) return Native.find(player);
        //?} else if forge {
        /*if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) return Native.find(player);
        *///?} else {
        /*if (net.neoforged.fml.ModList.get().isLoaded("curios")) return Native.find(player);
        *///?}
        return null;
    }
    public static ItemStack socket(Player player) {
        Binding binding = binding(player);
        return binding == null ? ItemStack.EMPTY : binding.socket();
    }
    public static ItemStack gem(Player player) {
        ItemStack socket = socket(player);
        // Client potion-use prediction reads synchronized equipment without sharing the server cache.
        if (player.level().isClientSide)
            return socket.isEmpty() ? ItemStack.EMPTY : GemSocketItem.gem(socket, player.level());
        if (socket.isEmpty()) {
            STATES.remove(player);
            return ItemStack.EMPTY;
        }
        State state = STATES.get(player);
        if (state == null || state.socket != socket || !ItemStack.matches(state.snapshot, socket)) {
            state = new State(socket, player);
            STATES.put(player, state);
        }
        return state.gem;
    }
    public static void forget(Player player) {
        if (!player.level().isClientSide) STATES.remove(player);
    }
    public static void save(Player player) {
        if (player.level().isClientSide) return;
        State state = STATES.get(player);
        if (state == null) return;
        Binding current = binding(player);
        if (current == null || current.socket() != state.socket || !ItemStack.matches(state.snapshot, state.socket)) {
            STATES.remove(player);
            return;
        }
        GemSocketItem.save(state.socket, state.gem, player.level());
        state.snapshot = state.socket.copy();
        current.changed().run();
    }
    public static void changed(Player player, ItemStack socket) {
        if (player.level().isClientSide) return;
        Binding current = binding(player);
        if (current != null && current.socket() == socket) current.changed().run();
    }

    private static final class Native {
        static void register() {
            var id = net.minecraft.resources.ResourceLocation.tryParse("arcanearchives:gemsocket");
            //? if fabric {
            dev.emi.trinkets.api.TrinketsApi.registerTrinketPredicate(id, (stack, slot, entity) ->
                stack.getItem() instanceof GemSocketItem
                    ? net.fabricmc.fabric.api.util.TriState.TRUE : net.fabricmc.fabric.api.util.TriState.FALSE);
            //?} else {
            /*top.theillusivec4.curios.api.CuriosApi.registerCurioPredicate(id,
                result -> result.stack().getItem() instanceof GemSocketItem);
            *///?}
        }
        static Binding find(Player player) {
            //? if fabric {
            var component = dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player).orElse(null);
            if (component == null) return null;
            var group = component.getInventory().get("arcanearchives");
            if (group == null) return null;
            var inventory = group.get("gemsocket");
            if (inventory == null) return null;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack socket = inventory.getItem(slot);
                if (socket.getItem() instanceof GemSocketItem && socket.getCount() == 1)
                    return new Binding(socket, inventory::setChanged);
            }
            //?} else {
            /*var inventory = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).orElse(null);
            if (inventory == null) return null;
            var matches = new java.util.ArrayList<>(inventory.findCurios("arcanearchives_gemsocket"));
            matches.sort(java.util.Comparator.comparingInt(result -> result.slotContext().index()));
            for (var result : matches) {
                ItemStack socket = result.stack();
                if (socket.getItem() instanceof GemSocketItem && socket.getCount() == 1)
                    return new Binding(socket, () -> {});
            }
            *///?}
            return null;
        }
    }
}
