package com.aranaira.arcanearchives.data;

import java.util.UUID;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
//? if >=1.21 {
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
//?}

/** Per-player receipt and exceptional pending returns, owned by the overworld rather than the player entity. */
public final class PlayerSaveData extends SavedData {
    public static final String PREFIX = "ArcaneArchives-PlayerSavedData-";
    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            get(server, handler.player.getUUID()).deliverBrazierReturns(handler.player));
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) -> {
                if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                    get(player.server, player.getUUID()).deliverBrazierReturns(player);
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) -> {
                if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                    get(player.server, player.getUUID()).deliverBrazierReturns(player);
            });
        *///?}
    }
    private boolean receivedBook;
    private net.minecraft.nbt.Tag brazierPendingReturns;

    public boolean hasBrazierPendingReturns() { return brazierPendingReturns != null; }

    /** Preserve even undecodable payloads until the delivery path can validate them without loss. */
    public net.minecraft.nbt.Tag brazierPendingReturns() {
        return brazierPendingReturns == null ? null : brazierPendingReturns.copy();
    }

    /** Null clears successfully delivered returns; callers retain ownership of their supplied tag. */
    public void setBrazierPendingReturns(net.minecraft.nbt.Tag pending) {
        if (Objects.equals(brazierPendingReturns, pending)) return;
        brazierPendingReturns = pending == null ? null : pending.copy();
        setDirty();
    }

    public static PlayerSaveData get(MinecraftServer server, UUID player) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Player receipt data must be accessed on the server thread");
        }
        String name = PREFIX + Objects.requireNonNull(player, "player");
        //? if >=1.21 {
        return server.overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PlayerSaveData::new, (tag, registries) -> load(tag), DataFixTypes.LEVEL), name);
        //?} else {
        /*return server.overworld().getDataStorage().computeIfAbsent(PlayerSaveData::load, PlayerSaveData::new, name);
        *///?}
    }

    /** Encode the whole failed batch before replacing any saved state. */
    public void queueBrazierReturns(java.util.List<net.minecraft.world.item.ItemStack> stacks,
            net.minecraft.core.RegistryAccess registries) {
        if (hasBrazierPendingReturns()) throw new IllegalStateException("Pending Brazier returns must be delivered first");
        if (stacks.isEmpty()) return;
        if (stacks.size() > 36) throw new IllegalArgumentException("Brazier returns exceed main-inventory source slots");
        var entries = new net.minecraft.nbt.ListTag();
        for (var stack : stacks) {
            if (stack.isEmpty()) throw new IllegalArgumentException("Empty pending Brazier return");
            var unit = stack.copy();
            unit.setCount(1);
            var entry = new CompoundTag();
            entry.putInt("count", stack.getCount());
            //? if >=1.21 {
            entry.put("item", unit.save(registries));
            //?} else {
            /*entry.put("item", unit.save(new CompoundTag()));
            *///?}
            entries.add(entry);
        }
        var payload = new CompoundTag();
        payload.putInt("version", 1);
        payload.put("stacks", entries);
        setBrazierPendingReturns(payload);
    }

    /** An unreadable entry blocks the entire delivery; never turn failed decoding into item deletion. */
    public java.util.Optional<java.util.List<net.minecraft.world.item.ItemStack>> decodeBrazierReturns(
            net.minecraft.core.RegistryAccess registries) {
        if (!hasBrazierPendingReturns()) return java.util.Optional.of(java.util.List.of());
        if (!(brazierPendingReturns instanceof CompoundTag payload) || !payload.contains("version", 3)
                || payload.getInt("version") != 1 || !(payload.get("stacks") instanceof net.minecraft.nbt.ListTag entries)
                || entries.isEmpty() || entries.size() > 36 || entries.getElementType() != 10) return java.util.Optional.empty();
        var stacks = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            if (!entry.contains("count", 3) || entry.getInt("count") <= 0 || !entry.contains("item", 10))
                return java.util.Optional.empty();
            //? if >=1.21 {
            var stack = net.minecraft.world.item.ItemStack.parse(registries, entry.getCompound("item"))
                .orElse(net.minecraft.world.item.ItemStack.EMPTY);
            //?} else {
            /*var stack = net.minecraft.world.item.ItemStack.of(entry.getCompound("item"));
            *///?}
            if (stack.isEmpty() || stack.getCount() != 1) return java.util.Optional.empty();
            stack.setCount(entry.getInt("count"));
            stacks.add(stack);
        }
        return java.util.Optional.of(stacks);
    }

    /** Return only what fits; no world spawning, routing, offhand insertion or creative overflow deletion. */
    public boolean deliverBrazierReturns(net.minecraft.world.entity.player.Player player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level) || !level.getServer().isSameThread())
            throw new IllegalStateException("Brazier return delivery requires the server thread");
        if (!hasBrazierPendingReturns()) return true;
        var decoded = decodeBrazierReturns(level.registryAccess());
        if (decoded.isEmpty()) return false;
        var prepared = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        for (var stack : player.getInventory().items) prepared.add(stack.copy());
        var payload = (CompoundTag) brazierPendingReturns.copy();
        var original = payload.getList("stacks", 10);
        var remaining = new net.minecraft.nbt.ListTag();
        boolean moved = false;
        for (int index = 0; index < decoded.get().size(); index++) {
            var stack = decoded.get().get(index);
            int before = stack.getCount();
            com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity.insertWithdrawal(prepared, stack);
            moved |= before != stack.getCount();
            if (!stack.isEmpty()) {
                var entry = original.getCompound(index).copy();
                entry.putInt("count", stack.getCount());
                remaining.add(entry);
            }
        }
        if (!moved) return false;
        payload.put("stacks", remaining);
        for (int slot = 0; slot < prepared.size(); slot++) player.getInventory().items.set(slot, prepared.get(slot));
        setBrazierPendingReturns(remaining.isEmpty() ? null : payload);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        return !hasBrazierPendingReturns();
    }

    public boolean hasReceivedBook() {
        return receivedBook;
    }

    public void markBookReceived() {
        if (!receivedBook) {
            receivedBook = true;
            setDirty();
        }
    }

    public static PlayerSaveData load(CompoundTag tag) {
        PlayerSaveData data = new PlayerSaveData();
        data.receivedBook = tag.getBoolean("received_book");
        net.minecraft.nbt.Tag pending = tag.get("brazier_pending_returns");
        data.brazierPendingReturns = pending == null ? null : pending.copy();
        return data;
    }

    @Override
    //? if >=1.21 {
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    //?} else {
    /*public CompoundTag save(CompoundTag tag) {
    *///?}
        tag.putBoolean("received_book", receivedBook);
        if (brazierPendingReturns != null) tag.put("brazier_pending_returns", brazierPendingReturns.copy());
        else tag.remove("brazier_pending_returns");
        return tag;
    }
}
