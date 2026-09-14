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
            (hit, hand) -> com.aranaira.arcanearchives.init.ContentRegistry.BRAZIER.get().useItemOn(player.getItemInHand(hand),
                level.getBlockState(hit.getBlockPos()), level, hit.getBlockPos(), player, hand, hit).consumesAction(),
            hit -> level.getBlockState(hit.getBlockPos()).useWithoutItem(level, player, hit).consumesAction());
        ManifestLecternLifecycle.run(helper, player,
            matrix -> level.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, matrix.asCraftInput(), level)
                .orElseThrow().value().assemble(matrix.asCraftInput(), level.registryAccess()),
            hit -> level.getBlockState(hit.getBlockPos()).useWithoutItem(level, player, hit).consumesAction());
    }
    static net.minecraft.nbt.CompoundTag saveEntity(net.minecraft.world.level.block.entity.BlockEntity entity) {
        return entity.saveWithFullMetadata(entity.getLevel().registryAccess());
    }
    static void loadEntity(net.minecraft.world.level.block.entity.BlockEntity entity, net.minecraft.nbt.CompoundTag tag) {
        entity.loadWithComponents(tag, entity.getLevel().registryAccess());
    }
    static void itemEntityData(ItemStack stack, net.minecraft.nbt.CompoundTag tag) {
        stack.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
            net.minecraft.world.item.component.CustomData.of(tag));
    }
    static Player player(GameTestHelper helper) {
        return helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
    }
    static void rootOverride(ItemStack stack) {
        stack.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
            new net.minecraft.world.item.component.BlockItemStateProperties(java.util.Map.of("part", "2")));
    }
    static void markItem(ItemStack stack) {
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
            net.minecraft.network.chat.Component.literal("must survive interrupted placement"));
    }
}
