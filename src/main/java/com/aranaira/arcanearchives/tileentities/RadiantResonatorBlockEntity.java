package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.data.ResonatorSaveData;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.types.BlockPosDimension;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class RadiantResonatorBlockEntity extends BlockEntity {
    private UUID owner;
    private int growth;
    private boolean canTick;
    private boolean registered;

    public RadiantResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ContentRegistry.RADIANT_RESONATOR_ENTITY.get(), pos, state);
    }

    public UUID owner() { return owner; }
    public int progressPercentage() {
        return (int) Math.floor(growth / (double) ServerSideConfig.current().resonatorTickTime() * 100D);
    }
    public net.minecraft.network.chat.Component statusMessage() {
        String key;
        net.minecraft.ChatFormatting color;
        if (level.isEmptyBlock(worldPosition.above())) {
            key = canTick ? "resonating" : "offline";
            color = canTick ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.DARK_RED;
        } else if (level.getBlockState(worldPosition.above()).is(ContentRegistry.RAW_QUARTZ_CLUSTER.get())) {
            key = "harvestable";
            color = net.minecraft.ChatFormatting.GOLD;
        } else {
            key = "obstruction";
            color = net.minecraft.ChatFormatting.RED;
        }
        return net.minecraft.network.chat.Component.translatable("arcanearchives.data.tooltip.resonator_status." + key).withStyle(color);
    }

    public void setOwner(UUID value) {
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread())
            throw new IllegalStateException("Resonator ownership requires the server thread");
        owner = value;
        ResonatorSaveData.get(server.getServer()).register(new BlockPosDimension(worldPosition, level.dimension()), owner);
        registered = true;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RadiantResonatorBlockEntity resonator) {
        if (!(level instanceof ServerLevel server) || resonator.owner == null) return;
        if (!resonator.registered) {
            ResonatorSaveData.get(server.getServer()).register(new BlockPosDimension(pos, level.dimension()), resonator.owner);
            resonator.registered = true;
        }
        boolean online = server.getServer().getPlayerList().getPlayer(resonator.owner) != null;
        boolean changed = resonator.canTick != online;
        resonator.canTick = online;
        if (online && level.isEmptyBlock(pos.above())) {
            if (resonator.growth < ServerSideConfig.current().resonatorTickTime()) {
                resonator.growth++;
            } else {
                resonator.growth = 0;
                level.setBlockAndUpdate(pos.above(), ContentRegistry.RAW_QUARTZ_CLUSTER.get().defaultBlockState());
                BlockPos up = pos.above();
                AABB box = new AABB(up.getX() - .9, up.getY() - .9, up.getZ() - .9,
                    up.getX() + .9, up.getY() + .9, up.getZ() + .9);
                for (var cat : level.getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class, box,
                        animal -> animal instanceof Cat || animal instanceof Ocelot)) {
                    cat.setDeltaMovement(cat.getDeltaMovement().add((level.random.nextFloat() - .5F) * 3F,
                        level.random.nextFloat() * 5F, (level.random.nextFloat() - .5F) * 3F));
                    cat.hasImpulse = true;
                }
                level.playSound(null, pos, ContentRegistry.RESONATOR_COMPLETE.get(), SoundSource.BLOCKS, 1F, 1F);
                changed = true;
            }
            resonator.setChanged();
        }
        if (changed || level.getGameTime() % 50 == 0) {
            resonator.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    public int comparatorSignal() {
        if (level == null) return 0;
        if (level.getBlockState(worldPosition.above()).is(ContentRegistry.RAW_QUARTZ_CLUSTER.get())) return 15;
        if (!canTick || !level.isEmptyBlock(worldPosition.above())) return 0;
        int percentage = (int) Math.floor(growth / (double) ServerSideConfig.current().resonatorTickTime() * 100D);
        return Math.max(1, Math.min((int) Math.floor(percentage / 7.14) + 1, 14));
    }

    private void writeState(CompoundTag tag) {
        if (owner != null) tag.putUUID("owner", owner);
        tag.putInt("current_tick", growth);
        tag.putBoolean("can_tick", canTick);
    }

    private void readState(CompoundTag tag) {
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        growth = tag.getInt("current_tick");
        canTick = tag.getBoolean("can_tick");
        registered = false;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    //? if >=1.21 {
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        writeState(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readState(tag);
    }
    //?} else {
    /*@Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeState(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readState(tag);
    }
    *///?}
}
