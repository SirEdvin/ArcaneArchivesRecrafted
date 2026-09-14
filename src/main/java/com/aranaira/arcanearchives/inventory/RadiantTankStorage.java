package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
//? if fabric {
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
//?} else if forge {
/*import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidTank;
*///?} else {
/*import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
*///?}

/** Native fluid identity/data and units remain owned by the target loader. */
public final class RadiantTankStorage
    //? if fabric {
    extends SingleVariantStorage<FluidVariant>
    //?} else {
    /*extends FluidTank
    *///?}
{
    private final RadiantTankBlockEntity owner;
    public RadiantTankStorage(RadiantTankBlockEntity owner) {
        //? if !fabric {
        /*super(16_000);
        *///?}
        this.owner = owner;
    }
    public static long capacityFor(int upgrades) {
        //? if fabric {
        return FluidConstants.BUCKET * 16 * (upgrades + 1);
        //?} else {
        /*return 16_000L * (upgrades + 1);
        *///?}
    }
    public long storedAmount() {
        //? if fabric {
        return amount;
        //?} else {
        /*return getFluidAmount();
        *///?}
    }
    public void updateCapacity() {
        //? if !fabric {
        /*setCapacity(Math.toIntExact(capacityFor(owner.upgrades().getUpgradesCount())));
        *///?}
    }
    public boolean interact(Player player, InteractionHand hand) {
        if (!owner.canUse(player)) return false;
        if (player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.RadiantAmphoraItem)
            return AmphoraFluidStorage.interactWithTank(player, hand, owner);
        //? if fabric {
        return FluidStorageUtil.interactWithFluidStorage(this, player, hand);
        //?} else {
        /*return FluidUtil.interactWithFluidHandler(player, hand, this);
        *///?}
    }
    //? if fabric {
    @Override protected FluidVariant getBlankVariant() { return FluidVariant.blank(); }
    @Override protected long getCapacity(FluidVariant variant) { return capacityFor(owner.upgrades().getUpgradesCount()); }
    @Override protected boolean canInsert(FluidVariant variant) { return owner.isLiveServerStorage(); }
    @Override protected boolean canExtract(FluidVariant variant) { return owner.isLiveServerStorage(); }
    @Override public long insert(FluidVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (owner.isLiveServerStorage() && owner.optionals().isVoiding()
                && amount == getCapacity(resource) && resource.equals(variant)) return maxAmount;
        return super.insert(resource, maxAmount, transaction);
    }
    @Override protected void onFinalCommit() { owner.storageChanged(); }
    //?} else {
    /*@Override public int fill(FluidStack stack, FluidAction action) {
        if (!owner.isLiveServerStorage() || stack.isEmpty()) return 0;
        if (owner.optionals().isVoiding() && getFluidAmount() == getCapacity() && getFluid().isFluidEqual(stack)) return stack.getAmount();
        return super.fill(stack, action);
    }
    @Override public FluidStack drain(FluidStack stack, FluidAction action) {
        return owner.isLiveServerStorage() ? super.drain(stack, action) : FluidStack.EMPTY;
    }
    @Override public FluidStack drain(int amount, FluidAction action) {
        return amount > 0 && owner.isLiveServerStorage() ? super.drain(amount, action) : FluidStack.EMPTY;
    }
    @Override protected void onContentsChanged() { owner.storageChanged(); }
    *///?}

    public CompoundTag writeState(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("amount", storedAmount());
        if (storedAmount() == 0) return tag;
        //? if fabric {
        //? if >=1.21 {
        tag.put("fluid", FluidVariant.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), variant)
            .result().orElseThrow(() -> new IllegalArgumentException("Cannot encode Tank fluid")));
        //?} else {
        /*tag.put("fluid", variant.toNbt());
        *///?}
        //?} else if forge {
        /*tag.put("fluid", writeToNBT(new CompoundTag()));
        *///?} else {
        /*tag.put("fluid", writeToNBT(registries, new CompoundTag()));
        *///?}
        return tag;
    }

    public void readState(CompoundTag tag, HolderLookup.Provider registries, int upgrades) {
        if (!tag.contains("amount", Tag.TAG_LONG)) throw new IllegalArgumentException("Missing Tank amount");
        long count = tag.getLong("amount");
        if (count < 0 || count > capacityFor(upgrades) || count > 0 && !tag.contains("fluid", Tag.TAG_COMPOUND))
            throw new IllegalArgumentException("Invalid Tank amount or fluid");
        //? if fabric {
        FluidVariant loaded = FluidVariant.blank();
        if (count > 0) {
            //? if >=1.21 {
            loaded = FluidVariant.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("fluid"))
                .result().orElseThrow(() -> new IllegalArgumentException("Cannot resolve Tank fluid"));
            //?} else {
            /*loaded = FluidVariant.fromNbt(tag.getCompound("fluid"));
            *///?}
            if (loaded.isBlank()) throw new IllegalArgumentException("Cannot resolve Tank fluid");
        }
        variant = loaded;
        amount = count;
        //?} else {
        /*FluidTank prepared = new FluidTank(Math.toIntExact(capacityFor(upgrades)));
        if (count > 0) {
            //? if forge {
            /^prepared.readFromNBT(tag.getCompound("fluid"));
            ^///?} else {
            prepared.readFromNBT(registries, tag.getCompound("fluid"));
            //?}
        }
        if (prepared.getFluidAmount() != count) throw new IllegalArgumentException("Cannot resolve Tank fluid amount");
        setFluid(prepared.getFluid().copy());
        setCapacity(prepared.getCapacity());
        *///?}
    }
}
