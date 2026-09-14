package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Device-local click history and source ordering only; selection never pays or routes items. */
public final class BrazierPlayerSelection {
    private final WeakHashMap<Player, ItemStack> references = new WeakHashMap<>();
    private UUID lastPlayer;
    private long lastClick;

    public record Selection(ItemStack reference, boolean wasHeld, List<Integer> slots) {}

    public Selection select(Player player, long now, boolean networkAvailable) {
        long elapsed = now - lastClick;
        boolean repeat = player.getUUID() == lastPlayer && elapsed <= 300;
        ItemStack previous = repeat ? references.getOrDefault(player, ItemStack.EMPTY) : ItemStack.EMPTY;
        if (!repeat && elapsed > 950) references.clear();
        lastPlayer = player.getUUID();
        lastClick = now;
        ItemStack reference = player.getMainHandItem().copy();
        boolean wasHeld = true;
        if (reference.isEmpty() && repeat) { reference = previous.copy(); wasHeld = false; }
        if (reference.isEmpty() || favorite(reference) || reference.is(ContentRegistry.SCEPTER_MANIPULATION.get())
                || reference.is(ContentRegistry.DEBUG_ORB.get()) || !networkAvailable) return null;
        var slots = new ArrayList<Integer>();
        int selected = player.getInventory().selected;
        if (wasHeld && !hasItemInventory(reference)) slots.add(selected);
        if (!repeat) references.put(player, reference.copy());
        else {
            var inventory = player.getInventory().items;
            for (int slot = 0; slot < inventory.size(); slot++) {
                // The original selected-slot extraction precedes this scan.
                if (!slots.contains(slot) && ExtendedItemStackHandler.sameItemAndData(inventory.get(slot), reference)) slots.add(slot);
            }
        }
        return new Selection(reference, wasHeld, List.copyOf(slots));
    }

    private static boolean favorite(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag().contains("Quark:FavoriteItem");
        //?} else {
        /*return stack.hasTag() && stack.getTag().contains("Quark:FavoriteItem");
        *///?}
    }

    private static boolean hasItemInventory(ItemStack stack) {
        //? if fabric {
        return net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.withConstant(stack)
            .find(com.aranaira.arcanearchives.inventory.TroveItemStorage.ITEM) != null;
        //?} else if forge {
        /*return stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, null).isPresent();
        *///?} else {
        /*return stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM) != null;
        *///?}
    }
}
