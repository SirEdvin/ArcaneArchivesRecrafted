package com.aranaira.arcanearchives.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Native API differences only; all placement assertions are shared. */
final class RuntimeTestVersion {
    private RuntimeTestVersion() {}
    static net.minecraft.nbt.CompoundTag saveEntity(net.minecraft.world.level.block.entity.BlockEntity entity) {
        return entity.saveWithFullMetadata();
    }
    static void loadEntity(net.minecraft.world.level.block.entity.BlockEntity entity, net.minecraft.nbt.CompoundTag tag) {
        entity.load(tag);
    }
    static void itemEntityData(ItemStack stack, net.minecraft.nbt.CompoundTag tag) {
        stack.getOrCreateTag().put("BlockEntityTag", tag.copy());
    }
    static Player player(GameTestHelper helper) {
        return helper.makeMockPlayer();
    }
    static void rootOverride(ItemStack stack) {
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("part", "2");
        stack.getOrCreateTag().put("BlockStateTag", tag);
    }
    static void markItem(ItemStack stack) {
        stack.getOrCreateTag().putString("fixture", "must survive interrupted placement");
    }
}
