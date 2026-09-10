package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Shared extended inventory. Forge interfaces are adapters, not the owner of storage semantics. */
public class ExtendedItemStackHandler
    //? if forge {
    /*implements net.minecraftforge.items.IItemHandlerModifiable
    *///?} else if neoforge {
    /*implements net.neoforged.neoforge.items.IItemHandlerModifiable
    *///?}
{
    protected final NonNullList<ItemStack> stacks;

    public ExtendedItemStackHandler() {
        this(1);
    }

    public ExtendedItemStackHandler(int size) {
        if (size < 1) throw new IllegalArgumentException("Inventory size must be positive");
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public int getSlots() {
        return stacks.size();
    }

    protected void validateSlotIndex(int slot) {
        Objects.checkIndex(slot, getSlots());
    }

    /** As with the upstream item-handler contract, callers must not mutate this stack. */
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        stacks.set(slot, Objects.requireNonNull(stack).copy());
        onContentsChanged(slot);
    }

    public int getSlotLimit(int slot) {
        validateSlotIndex(slot);
        return 64 * ServerSideConfig.current().radiantMultiplier();
    }

    public int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize() * ServerSideConfig.current().radiantMultiplier());
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        return true;
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack.copy();
        ItemStack existing = stacks.get(slot);
        if (!existing.isEmpty() && !sameItemAndData(existing, stack)) return stack.copy();
        int available = getStackLimit(slot, stack) - existing.getCount();
        if (available <= 0) return stack.copy();
        int inserted = Math.min(available, stack.getCount());
        if (!simulate) {
            stacks.set(slot, withCount(stack, existing.getCount() + inserted));
            onContentsChanged(slot);
        }
        return withCount(stack, stack.getCount() - inserted);
    }

    /**
     * Fill matching occupied slots before empty slots, returning a detached remainder.
     * Uses each slot's insertion policy. Simulation reserves no space; real insertion is
     * incremental, not an atomic crafting transaction. Call on the owning server thread.
     */
    public ItemStack insertItemStacked(ItemStack stack, boolean simulate) {
        ItemStack remainder = Objects.requireNonNull(stack, "stack").copy();
        for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++) {
            for (int slot = 0; slot < getSlots() && !remainder.isEmpty(); slot++) {
                ItemStack existing = getStackInSlot(slot);
                if (pass == 0 ? !existing.isEmpty() && sameItemAndData(existing, remainder) : existing.isEmpty()) {
                    remainder = insertItem(slot, remainder, simulate);
                }
            }
        }
        return remainder.copy();
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        if (amount < 0) throw new IllegalArgumentException("Extraction amount must not be negative");
        ItemStack existing = stacks.get(slot);
        if (amount == 0 || existing.isEmpty()) return ItemStack.EMPTY;
        int limit = existing.getMaxStackSize() == 1 ? 1
            : existing.getMaxStackSize() * ServerSideConfig.current().radiantMultiplier();
        int extracted = Math.min(existing.getCount(), Math.min(amount, limit));
        ItemStack result = withCount(existing, extracted);
        if (!simulate) {
            stacks.set(slot, withCount(existing, existing.getCount() - extracted));
            onContentsChanged(slot);
        }
        return result;
    }

    protected static ItemStack withCount(ItemStack stack, int count) {
        if (count == 0) return ItemStack.EMPTY;
        ItemStack result = stack.copy();
        result.setCount(count);
        return result;
    }

    public static boolean sameItemAndData(ItemStack first, ItemStack second) {
        //? if >=1.21 {
        return ItemStack.isSameItemSameComponents(first, second);
        //?} else {
        /*return ItemStack.isSameItemSameTags(first, second);
        *///?}
    }

    /** Fresh-world format: serialize a unit stack so vanilla's count codec cannot truncate extended counts. */
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        ListTag items = new ListTag();
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", slot);
            entry.putInt("ExtendedCount", stack.getCount());
            ItemStack unit = withCount(stack, 1);
            //? if >=1.21 {
            entry.put("Stack", unit.save(registries));
            //?} else {
            /*entry.put("Stack", unit.save(new CompoundTag()));
            *///?}
            items.add(entry);
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("Size", getSlots());
        tag.put("Items", items);
        return tag;
    }

    /** Decode completely before replacing live storage; malformed data must not partially empty an inventory. */
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        if (!tag.contains("Size", Tag.TAG_INT) || tag.getInt("Size") != getSlots()
            || !tag.contains("Items", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Inventory shape does not match saved data");
        }
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        if (items.size() != ((ListTag) tag.get("Items")).size()) {
            throw new IllegalArgumentException("Inventory entries must be compounds");
        }
        NonNullList<ItemStack> loaded = NonNullList.withSize(getSlots(), ItemStack.EMPTY);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag entry = items.getCompound(index);
            if (!entry.contains("Slot", Tag.TAG_INT) || !entry.contains("ExtendedCount", Tag.TAG_INT)
                || !entry.contains("Stack", Tag.TAG_COMPOUND)) {
                throw new IllegalArgumentException("Invalid inventory entry");
            }
            int slot = entry.getInt("Slot");
            validateSlotIndex(slot);
            int count = entry.getInt("ExtendedCount");
            if (count < 1 || !loaded.get(slot).isEmpty()) {
                throw new IllegalArgumentException("Invalid count or duplicate slot in saved inventory");
            }
            //? if >=1.21 {
            ItemStack stack = ItemStack.parse(registries, entry.getCompound("Stack"))
                .orElseThrow(() -> new IllegalArgumentException("Cannot resolve saved item"));
            //?} else {
            /*ItemStack stack = ItemStack.of(entry.getCompound("Stack"));
            *///?}
            if (stack.isEmpty()) throw new IllegalArgumentException("Cannot resolve saved item");
            loaded.set(slot, withCount(stack, count));
        }
        validateLoadedStacks(loaded);
        for (int slot = 0; slot < getSlots(); slot++) stacks.set(slot, loaded.get(slot));
        onLoad();
    }

    public int calcRedstone() {
        int occupied = 0;
        float fullness = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (!stack.isEmpty()) {
                fullness += (float) stack.getCount() / getStackLimit(slot, stack);
                occupied++;
            }
        }
        return Mth.floor(fullness / getSlots() * 14F) + (occupied > 0 ? 1 : 0);
    }

    public void onContentsChanged(int slot) {}

    protected void validateLoadedStacks(NonNullList<ItemStack> loaded) {}

    protected void onLoad() {}
}
