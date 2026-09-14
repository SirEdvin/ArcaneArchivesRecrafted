package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.RadiantCraftingMenu;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}

public final class RadiantCraftingTableBlockEntity extends BlockEntity implements MenuProvider {
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final ResourceLocation[] recipes = new ResourceLocation[3];
    private UUID owner;
    private boolean dropped;

    public RadiantCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ContentRegistry.RADIANT_CRAFTING_TABLE_ENTITY.get(), pos, state);
    }

    public NonNullList<ItemStack> items() { return items; }
    @Override public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    //? if >=1.21 {
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, registries);
        return tag;
    }
    //?} else {
    /*@Override public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items);
        return tag;
    }
    *///?}
    public UUID owner() { return owner; }
    public void setOwner(UUID value) { owner = value; setChanged(); }
    public ResourceLocation recipe(int index) { return recipes[index]; }
    public void setRecipe(int index, ResourceLocation value) { recipes[index] = value; setChanged(); }

    public boolean canUse(Player player) {
        return level instanceof ServerLevel server && server.getServer().isSameThread()
            && !isRemoved() && !dropped && level.getBlockEntity(worldPosition) == this
            && player.level() == level && player.isAlive() && !player.isSpectator()
            && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64
            && level.mayInteract(player, worldPosition);
    }

    @Override public Component getDisplayName() { return Component.translatable("block.arcanearchives.radiant_crafting_table"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return canUse(player) ? new RadiantCraftingMenu(id, inventory, this) : null;
    }

    public void dropContents() {
        if (!(level instanceof ServerLevel) || dropped) return;
        dropped = true;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.set(slot, ItemStack.EMPTY);
            Block.popResource(level, worldPosition, stack);
        }
        setChanged();
    }

    private void writeState(CompoundTag tag) {
        if (owner != null) tag.putUUID("owner", owner);
        for (int index = 0; index < recipes.length; index++)
            if (recipes[index] != null) tag.putString("recipe" + (index + 1), recipes[index].toString());
    }
    private void readState(CompoundTag tag) {
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        for (int index = 0; index < recipes.length; index++)
            recipes[index] = ResourceLocation.tryParse(tag.getString("recipe" + (index + 1)));
        dropped = false;
    }

    //? if >=1.21 {
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        writeState(tag);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        readState(tag);
    }
    //?} else {
    /*@Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        writeState(tag);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        readState(tag);
    }
    *///?}
}
