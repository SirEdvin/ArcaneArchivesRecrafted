package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.Objects;
import java.util.List;
import java.util.stream.IntStream;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Server-thread-owned staging state. The runtime owner still must persist and deliver it safely. */
public final class GemCutterCraftingState {
    private GemCutterInputHandler inputs = new GemCutterInputHandler();
    private GCTCraftingResult pending;
    private boolean crafting;

    public ItemStack getInput(int slot) {
        return inputs.getStackInSlot(slot);
    }

    public int inputSlots() {
        return inputs.getSlots();
    }

    /** Ordered detached input slots, including empties. A preview never authorizes crafting. */
    public List<ItemStack> inputSnapshot() {
        return IntStream.range(0, inputs.getSlots()).mapToObj(inputs::getStackInSlot).toList();
    }

    /** Live routing information, not reserved insertion capacity. */
    public int countEmptyInputs() {
        int empty = 0;
        for (int slot = 0; slot < inputs.getSlots(); slot++) {
            if (inputs.getStackInSlot(slot).isEmpty()) empty++;
        }
        return empty;
    }

    public void setInput(int slot, ItemStack stack) {
        requireIdle();
        inputs.setStackInSlot(slot, stack);
    }

    /** Simulation reserves no capacity; the runtime owner must mark real changes dirty. */
    public ItemStack insertInput(int slot, ItemStack stack, boolean simulate) {
        requireIdle();
        return inputs.insertItem(slot, Objects.requireNonNull(stack, "stack"), simulate);
    }

    public ItemStack extractInput(int slot, int amount, boolean simulate) {
        requireIdle();
        return inputs.extractItem(slot, amount, simulate);
    }

    /** Matching stacks first, then empty slots. Incremental insertion, not pending-result delivery. */
    public ItemStack insertInputStacked(ItemStack stack, boolean simulate) {
        requireIdle();
        return inputs.insertItemStacked(Objects.requireNonNull(stack, "stack"), simulate);
    }

    /** Brazier input policy: top up matching occupied slots only. Real changes require runtime dirty marking. */
    public ItemStack acceptRoutingInput(ItemStack stack, boolean simulate) {
        requireIdle();
        ItemStack remainder = Objects.requireNonNull(stack, "stack").copy();
        for (int slot = 0; slot < inputs.getSlots() && !remainder.isEmpty(); slot++) {
            ItemStack existing = inputs.getStackInSlot(slot);
            if (!existing.isEmpty() && ExtendedItemStackHandler.sameItemAndData(existing, remainder)) {
                remainder = inputs.insertItem(slot, remainder, simulate);
            }
        }
        return remainder.copy();
    }

    /** Inspection only, not permission to grant these stacks to a player. */
    public Optional<GCTCraftingResult> pendingResult() {
        return Optional.ofNullable(pending);
    }

    public boolean craft(GCTRecipeList catalog, ResourceLocation name, UUID creator, String displayName,
            BooleanSupplier conditions) {
        requireIdle();
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(conditions, "conditions");
        if (pending != null) return false;
        crafting = true;
        try {
            pending = catalog.consumeForCraft(name, inputs, creator, displayName, conditions).orElse(null);
            return pending != null;
        } finally {
            crafting = false;
        }
    }

    private void requireIdle() {
        if (crafting) throw new IllegalStateException("Cannot change or save crafting state during a craft");
    }

    /**
     * Input half of an immediate native-menu craft. The menu must commit its prepared player/cursor
     * stacks immediately after success, with no external callbacks between those owned writes.
     * This never clears or delivers a previously staged result.
     */
    public boolean commitCraftInputs(List<ItemStack> expected, List<ItemStack> remaining, BooleanSupplier conditions) {
        requireIdle();
        Objects.requireNonNull(conditions, "conditions");
        if (expected.size() != inputSlots() || remaining.size() != inputSlots()) {
            throw new IllegalArgumentException("Expected eighteen table inputs");
        }
        if (pending != null) return false;
        crafting = true;
        try {
            GemCutterInputHandler prepared = new GemCutterInputHandler();
            for (int slot = 0; slot < inputSlots(); slot++) prepared.setStackInSlot(slot, remaining.get(slot));
            if (!conditions.getAsBoolean()) return false;
            for (int slot = 0; slot < inputSlots(); slot++) {
                ItemStack current = inputs.getStackInSlot(slot);
                if (current.getCount() != expected.get(slot).getCount()
                        || !ExtendedItemStackHandler.sameItemAndData(current, expected.get(slot))) return false;
            }
            inputs = prepared;
            return true;
        } finally {
            crafting = false;
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        requireIdle();
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 1);
        tag.put("Inputs", inputs.serializeNBT(registries));
        tag.putBoolean("HasPending", pending != null);
        if (pending != null) tag.put("Pending", pending.serializeNBT(registries));
        return tag;
    }

    /** Decode both halves before replacement; a failed load leaves the entire live state untouched. */
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        requireIdle();
        if (!tag.contains("Version", Tag.TAG_INT) || tag.getInt("Version") != 1
                || !tag.contains("Inputs", Tag.TAG_COMPOUND) || !tag.contains("HasPending", Tag.TAG_BYTE)) {
            throw new IllegalArgumentException("Invalid crafting state format");
        }
        byte flag = tag.getByte("HasPending");
        if (flag != 0 && flag != 1 || (flag == 1 ? !tag.contains("Pending", Tag.TAG_COMPOUND) : tag.contains("Pending"))) {
            throw new IllegalArgumentException("Invalid pending crafting state");
        }
        GemCutterInputHandler loadedInputs = new GemCutterInputHandler();
        loadedInputs.deserializeNBT(registries, tag.getCompound("Inputs"));
        GCTCraftingResult loadedPending = flag == 1
            ? GCTCraftingResult.deserializeNBT(registries, tag.getCompound("Pending")) : null;
        inputs = loadedInputs;
        pending = loadedPending;
    }
}
