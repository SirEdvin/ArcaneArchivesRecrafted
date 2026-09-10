package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.config.ArsenalConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.ArcaneGemItem;
import net.minecraft.client.renderer.item.ItemProperties;

public final class ArsenalClient {
    private ArsenalClient() {}
    public static void initialize() {
        GemSocketKey.initialize();
        GemHud.initialize();
        ItemProperties.register(ContentRegistry.CHROMATIC_POWDER.get(), ContentRegistry.id("powder_color"),
            (stack, level, entity, seed) -> com.aranaira.arcanearchives.items.ChromaticPowderItem.color(stack) / 10F);
        for (var gem : java.util.List.of(ContentRegistry.PARCHTEAR.get(), ContentRegistry.RIVERTEAR.get(), ContentRegistry.AGEGLEAM.get(), ContentRegistry.SALVEGLEAM.get(), ContentRegistry.MINDSPINDLE.get(), ContentRegistry.ORDERSTONE.get(), ContentRegistry.CLEANSEGLEAM.get(), ContentRegistry.MUNCHSTONE.get(), ContentRegistry.SWITCHGLEAM.get(), ContentRegistry.ELIXIRSPINDLE.get(), ContentRegistry.PHOENIXWAY.get(), ContentRegistry.STORMWAY.get(), ContentRegistry.MOUNTAINTEAR.get(), ContentRegistry.MURDERGLEAM.get(), ContentRegistry.SLAUGHTERGLEAM.get())) {
            ItemProperties.register(gem, ContentRegistry.id("gem_empty"),
                (stack, level, entity, seed) -> ArcaneGemItem.isChargeEmpty(stack) ? 1F : 0F);
            ItemProperties.register(gem, ContentRegistry.id("colourblind"),
                (stack, level, entity, seed) -> ArsenalConfig.current().colourblindMode() ? 1F : 0F);
        }
        //? if fabric {
        net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback.EVENT.register((client, player, clicks) -> {
            if (clicks > 0 && client.hitResult != null && client.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.MISS)
                toggle(player);
            return false;
        });
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) -> toggle(event.getEntity()));
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) -> toggle(event.getEntity()));
        *///?}
    }
    private static void toggle(net.minecraft.world.entity.player.Player player) {
        if (!player.isSpectator() && player.getMainHandItem().getItem() instanceof ArcaneGemItem gem && gem.hasToggleMode())
            com.aranaira.arcanearchives.events.GemToggle.send();
    }
}
