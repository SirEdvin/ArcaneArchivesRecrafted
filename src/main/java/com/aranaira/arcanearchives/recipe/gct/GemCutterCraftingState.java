package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.Objects;
import java.util.List;
import java.util.stream.IntStream;
import java.util.function.BooleanSupplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Server-owned input and completed-output inventory. The runtime owner marks mutations dirty. */
public final class GemCutterCraftingState {
    private GemCutterInputHandler inputs = new GemCutterInputHandler();
    private ItemStack output = ItemStack.EMPTY;
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

    /** Matching input stacks first, then empty input slots. Never inserts into output. */
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

    public ItemStack getOutput() { return output.copy(); }

    /** Internal inventory write for crafting, extraction and loading; not an insertion API. */
    public void setOutput(ItemStack stack) {
        requireIdle();
        output = checkedOutput(stack);
    }

    private static ItemStack checkedOutput(ItemStack stack) {
        Objects.requireNonNull(stack, "output");
        if (!stack.isEmpty() && stack.getCount() > Math.min(64, stack.getMaxStackSize()))
            throw new IllegalArgumentException("Invalid Gem Cutter output count");
        return stack.copy();
    }

    private void requireIdle() {
        if (crafting) throw new IllegalStateException("Cannot change or save crafting state during a craft");
    }

    /**
     * Commit prepared input and output slots together. The menu immediately writes its prepared
     * player inputs, without external callbacks between those owned writes.
     */
    public boolean commitCraft(List<ItemStack> expected, List<ItemStack> remaining,
            ItemStack expectedOutput, ItemStack result, BooleanSupplier conditions) {
        requireIdle();
        Objects.requireNonNull(conditions, "conditions");
        if (expected.size() != inputSlots() || remaining.size() != inputSlots()) {
            throw new IllegalArgumentException("Expected eighteen table inputs");
        }
        crafting = true;
        try {
            ItemStack preparedOutput = checkedOutput(result);
            GemCutterInputHandler prepared = new GemCutterInputHandler();
            for (int slot = 0; slot < inputSlots(); slot++) prepared.setStackInSlot(slot, remaining.get(slot));
            if (!conditions.getAsBoolean()) return false;
            if (!ItemStack.matches(output, expectedOutput)) return false;
            for (int slot = 0; slot < inputSlots(); slot++) {
                ItemStack current = inputs.getStackInSlot(slot);
                if (current.getCount() != expected.get(slot).getCount()
                        || !ExtendedItemStackHandler.sameItemAndData(current, expected.get(slot))) return false;
            }
            inputs = prepared;
            output = preparedOutput;
            return true;
        } finally {
            crafting = false;
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        requireIdle();
        CompoundTag tag = new CompoundTag();
        tag.put("Inputs", inputs.serializeNBT(registries));
        ExtendedItemStackHandler savedOutput = new ExtendedItemStackHandler(1);
        savedOutput.setStackInSlot(0, output);
        tag.put("Output", savedOutput.serializeNBT(registries));
        return tag;
    }

    /** Decode both halves before replacement; a failed load leaves the entire live state untouched. */
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        requireIdle();
        if (!tag.contains("Inputs", Tag.TAG_COMPOUND) || !tag.contains("Output", Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Invalid crafting state format");
        }
        GemCutterInputHandler loadedInputs = new GemCutterInputHandler();
        loadedInputs.deserializeNBT(registries, tag.getCompound("Inputs"));
        ExtendedItemStackHandler loadedOutput = new ExtendedItemStackHandler(1);
        loadedOutput.deserializeNBT(registries, tag.getCompound("Output"));
        ItemStack result = checkedOutput(loadedOutput.getStackInSlot(0));
        inputs = loadedInputs;
        output = result;
    }
}
