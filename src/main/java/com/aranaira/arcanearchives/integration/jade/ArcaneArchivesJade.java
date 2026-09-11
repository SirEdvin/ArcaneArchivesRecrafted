package com.aranaira.arcanearchives.integration.jade;

import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/** Optional Jade discovery owns loading; no main entrypoint references this class. */
@WailaPlugin
public final class ArcaneArchivesJade implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(Provider.INSTANCE, RadiantChestBlockEntity.class);
        registration.registerBlockDataProvider(Provider.INSTANCE, RadiantResonatorBlockEntity.class);
        registration.registerBlockDataProvider(Provider.INSTANCE, RadiantTroveBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(Provider.INSTANCE, Block.class);
    }

    public enum Provider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
        INSTANCE;

        @Override public ResourceLocation getUid() { return ArcaneArchivesMod.id("storage_overlay"); }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            Component first = null;
            Component second = null;
            var entity = accessor.getBlockEntity();
            if (entity == null || entity.isRemoved() || entity.getLevel() == null) return;
            if (entity instanceof RadiantChestBlockEntity chest && !chest.chestName().isEmpty()) {
                first = Component.translatable("arcanearchives.data.tooltip.chest_name").append(" " + chest.chestName());
            } else if (entity instanceof RadiantTroveBlockEntity trove) {
                var stack = trove.inventory().getStackInSlot(0);
                first = stack.isEmpty() ? Component.translatable("arcanearchives.data.tooltip.empty")
                    : Component.translatable("arcanearchives.data.tooltip.trove_count_waila", stack.getCount(), stack.getHoverName());
            } else if (entity instanceof RadiantResonatorBlockEntity resonator) {
                first = Component.translatable("arcanearchives.data.tooltip.resonator_progress")
                    .append(" " + resonator.progressPercentage() + "%");
                second = resonator.statusMessage();
            }
            if (first == null) return;
            var lines = new CompoundTag();
            first = first.copy().withStyle(ChatFormatting.GOLD);
            //? if >=1.21 {
            lines.putString("first", Component.Serializer.toJson(first, entity.getLevel().registryAccess()));
            if (second != null) lines.putString("second", Component.Serializer.toJson(second, entity.getLevel().registryAccess()));
            //?} else {
            /*lines.putString("first", Component.Serializer.toJson(first));
            if (second != null) lines.putString("second", Component.Serializer.toJson(second));
            *///?}
            data.put("arcanearchives:overlay", lines);
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var lines = accessor.getServerData().getCompound("arcanearchives:overlay");
            for (String key : new String[]{"first", "second"}) {
                if (!lines.contains(key, 8)) continue;
                //? if >=1.21 {
                Component line = Component.Serializer.fromJson(lines.getString(key), accessor.getLevel().registryAccess());
                //?} else {
                /*Component line = Component.Serializer.fromJson(lines.getString(key));
                *///?}
                if (line != null) tooltip.add(line);
            }
        }
    }
}
