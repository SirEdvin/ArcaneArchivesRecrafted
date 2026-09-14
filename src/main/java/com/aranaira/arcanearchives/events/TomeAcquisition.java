package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.data.PlayerSaveData;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


/** Original receipt-before-spawn semantics, explicitly retained by the decision in 0111. */
public final class TomeAcquisition {
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> BOOKSHELVES =
        net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ContentRegistry.id("tome_bookshelves"));

    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> {
            bookshelf(player, state);
            return true;
        });
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.level.BlockEvent.BreakEvent event) -> bookshelf(event.getPlayer(), event.getState()));
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) -> bookshelf(event.getPlayer(), event.getState()));
        *///?}
    }

    public static void bookshelf(Player player, net.minecraft.world.level.block.state.BlockState state) {
        if (ServerSideConfig.current().bookFromBookshelf() && state.is(BOOKSHELVES)) grant(player, true);
    }

    public static void crafted(Player player, ItemStack stack) {
        if (!(player.level() instanceof ServerLevel level) || !ServerSideConfig.current().bookFromResonator()) return;
        if (stack.is(ContentRegistry.RADIANT_RESONATOR_ITEM.get())) grant(player, false);
        else if (stack.is(ContentRegistry.TOME_OF_ARCANA.get())) {
            PlayerSaveData.get(level.getServer(), player.getUUID()).markBookReceived();
            level.getServer().overworld().getDataStorage().save();
        }
    }

    private static void grant(Player player, boolean bookshelf) {
        if (!(player.level() instanceof ServerLevel level)) return;
        var receipt = PlayerSaveData.get(level.getServer(), player.getUUID());
        if (receipt.hasReceivedBook()) return;
        receipt.markBookReceived();
        level.getServer().overworld().getDataStorage().save();
        var entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(),
            new ItemStack(ContentRegistry.TOME_OF_ARCANA.get()));
        entity.setNoPickUpDelay();
        player.sendSystemMessage(Component.translatable("arcanearchives.message.book_received."
            + (bookshelf ? "bookshelf" : "resonator")).withStyle(ChatFormatting.GOLD));
        // Intentionally do not retry or roll back receipt on rejected spawning (0111).
        level.addFreshEntity(entity);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOL_FALL, SoundSource.PLAYERS, 1F, 1F);
    }
}
