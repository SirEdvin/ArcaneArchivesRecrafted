package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Detached output and consumed inputs; the caller still owns persistence and remainder delivery. */
public final class GCTCraftingResult {
    private final ItemStack output;
    private final List<ItemStack> consumed;

    GCTCraftingResult(ItemStack output, List<ItemStack> consumed) {
        if (output.isEmpty()) throw new IllegalArgumentException("Crafting output cannot be empty");
        if (consumed.size() > 18 || consumed.stream().anyMatch(stack -> stack.isEmpty()
                || stack.getCount() > Math.min(64, stack.getMaxStackSize()))) {
            throw new IllegalArgumentException("Invalid consumed Gem Cutter inputs");
        }
        this.output = output.copy();
        this.consumed = consumed.stream().map(ItemStack::copy).toList();
    }

    public ItemStack output() {
        return output.copy();
    }

    public List<ItemStack> consumed() {
        return consumed.stream().map(ItemStack::copy).toList();
    }

    /** Fresh-world data only. The owner must save this together with the deducted input state. */
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        ExtendedItemStackHandler savedOutput = new ExtendedItemStackHandler(1);
        savedOutput.setStackInSlot(0, output);
        GemCutterInputHandler savedConsumed = new GemCutterInputHandler();
        for (int slot = 0; slot < consumed.size(); slot++) savedConsumed.setStackInSlot(slot, consumed.get(slot));
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 1);
        tag.put("Output", savedOutput.serializeNBT(registries));
        tag.put("Consumed", savedConsumed.serializeNBT(registries));
        return tag;
    }

    /** Decode a detached result without granting items or mutating any live inventory. */
    public static GCTCraftingResult deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        if (!tag.contains("Version", Tag.TAG_INT) || tag.getInt("Version") != 1
                || !tag.contains("Output", Tag.TAG_COMPOUND) || !tag.contains("Consumed", Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Invalid crafting result format");
        }
        ExtendedItemStackHandler savedOutput = new ExtendedItemStackHandler(1);
        savedOutput.deserializeNBT(registries, tag.getCompound("Output"));
        GemCutterInputHandler savedConsumed = new GemCutterInputHandler();
        savedConsumed.deserializeNBT(registries, tag.getCompound("Consumed"));
        List<ItemStack> consumed = new ArrayList<>();
        boolean gap = false;
        for (int slot = 0; slot < savedConsumed.getSlots(); slot++) {
            ItemStack stack = savedConsumed.getStackInSlot(slot);
            if (stack.isEmpty()) {
                gap = true;
            } else {
                if (gap) throw new IllegalArgumentException("Consumed stacks must be contiguous");
                consumed.add(stack);
            }
        }
        return new GCTCraftingResult(savedOutput.getStackInSlot(0), consumed);
    }
}
