package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.blocks.GemCuttersTable;
import com.aranaira.arcanearchives.inventory.GemCuttersTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.aranaira.arcanearchives.recipe.gct.GemCutterCraftingState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Owns the saved Gem Cutter input and completed-output inventory. */
public final class GemCuttersTableBlockEntity extends NetworkOwnedBlockEntity implements MenuProvider {
    private GemCutterCraftingState crafting = new GemCutterCraftingState();
    private boolean contentsDropped;

    public GemCuttersTableBlockEntity(BlockPos pos, BlockState state) {
        super(ContentRegistry.GEMCUTTERS_TABLE_ENTITY.get(), pos, state);
    }

    public ItemStack getInput(int slot) {
        return crafting.getInput(slot);
    }

    public ItemStack getOutput() { return crafting.getOutput(); }

    public boolean stillValid(Player player) {
        if (level == null || isRemoved() || contentsDropped || player.level() != level
                || level.getBlockEntity(worldPosition) != this
                || player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) > 64) return false;
        BlockState state = level.getBlockState(worldPosition);
        if (!state.is(ContentRegistry.GEMCUTTERS_TABLE.get()) || state.getValue(GemCuttersTable.ACCESSOR)) return false;
        BlockState other = level.getBlockState(GemCuttersTable.connectedPos(worldPosition, state));
        return other.is(state.getBlock()) && other.getValue(GemCuttersTable.ACCESSOR)
            && other.getValue(GemCuttersTable.FACING) == state.getValue(GemCuttersTable.FACING);
    }

    @Override
    public Component getDisplayName() { return Component.translatable("block.arcanearchives.gemcutters_table"); }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        requireServer();
        return stillValid(player) ? new GemCuttersTableMenu(id, inventory, new MenuInputs(), () -> crafting, () -> {
            requireServer();
            return stillValid(player);
        }) : null;
    }

    /** Menu-local adapter: do not expose detached getters to vanilla hopper in-place merging. */
    private final class MenuInputs implements Container {
        @Override
        public int getContainerSize() { return crafting.inputSlots(); }

        @Override
        public boolean isEmpty() { return crafting.countEmptyInputs() == getContainerSize(); }

        @Override
        public ItemStack getItem(int slot) { return getInput(slot); }

        @Override
        public ItemStack removeItem(int slot, int amount) { return extractInput(slot, amount, false); }

        @Override
        public ItemStack removeItemNoUpdate(int slot) { return extractInput(slot, 64, false); }

        @Override
        public void setItem(int slot, ItemStack stack) {
            requireServer();
            crafting.setInput(slot, stack);
            setChanged();
        }

        @Override
        public void setChanged() { GemCuttersTableBlockEntity.this.setChanged(); }

        @Override
        public boolean stillValid(Player player) { return GemCuttersTableBlockEntity.this.stillValid(player); }

        @Override
        public void clearContent() {
            requireServer();
            for (int slot = 0; slot < getContainerSize(); slot++) crafting.setInput(slot, ItemStack.EMPTY);
            setChanged();
        }
    }

    public ItemStack insertInput(int slot, ItemStack stack, boolean simulate) {
        requireServer();
        ItemStack remainder = crafting.insertInput(slot, stack, simulate);
        if (!simulate && remainder.getCount() != stack.getCount()) setChanged();
        return remainder;
    }

    public ItemStack extractInput(int slot, int amount, boolean simulate) {
        requireServer();
        ItemStack result = crafting.extractInput(slot, amount, simulate);
        if (!simulate && !result.isEmpty()) setChanged();
        return result;
    }

    private void requireServer() {
        if (level == null || level.isClientSide || isRemoved() || contentsDropped
                || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Gem Cutter mutations require its live owning server thread");
        }
    }

    /** Called once from master removal, never from the accessor. */
    public void dropContents() {
        requireServer();
        ItemStack table = new ItemStack(ContentRegistry.GEMCUTTERS_TABLE_ITEM.get());
        contentsDropped = true;
        for (ItemStack stack : crafting.inputSnapshot()) Block.popResource(level, worldPosition, stack);
        Block.popResource(level, worldPosition, crafting.getOutput());
        Block.popResource(level, worldPosition, table);
    }

    //? if >=1.21 {
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Crafting", crafting.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        loadCrafting(tag, registries);
        super.loadAdditional(tag, registries);
    }
    //?} else {
    /*@Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Crafting", crafting.serializeNBT(null));
    }

    @Override
    public void load(CompoundTag tag) {
        loadCrafting(tag, null);
        super.load(tag);
    }
    *///?}

    private void loadCrafting(CompoundTag tag, HolderLookup.Provider registries) {
        GemCutterCraftingState loaded = new GemCutterCraftingState();
        if (tag.contains("Crafting")) {
            if (!tag.contains("Crafting", Tag.TAG_COMPOUND)) throw new IllegalArgumentException("Invalid Gem Cutter data");
            loaded.deserializeNBT(registries, tag.getCompound("Crafting"));
        }
        crafting = loaded;
    }
}
