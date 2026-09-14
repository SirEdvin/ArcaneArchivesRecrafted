package com.aranaira.arcanearchives.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Native API differences only; all placement assertions are shared. */
final class RuntimeTestVersion {
    private RuntimeTestVersion() {}
    static void lectern(GameTestHelper helper, Player player) {
        var level = helper.getLevel();
        BrazierActivationLifecycle.run(helper, player,
            (hit, hand) -> level.getBlockState(hit.getBlockPos()).use(level, player, hand, hit).consumesAction(),
            hit -> level.getBlockState(hit.getBlockPos()).use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit).consumesAction());
        ManifestLecternLifecycle.run(helper, player,
            matrix -> level.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, matrix, level)
                .orElseThrow().assemble(matrix, level.registryAccess()),
            hit -> level.getBlockState(hit.getBlockPos()).use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit).consumesAction());
    }
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
