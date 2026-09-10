package com.aranaira.arcanearchives.mixin;

import com.aranaira.arcanearchives.items.WritOfExpulsionItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class WritAnvilMixin extends ItemCombinerMenu {
    protected WritAnvilMixin(MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access) {
        super(type, id, inventory, access);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void arcanearchives$bindWrit(CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.server.isSameThread()) {
            WritOfExpulsionItem.bindTarget(resultSlots.getItem(0), name -> serverPlayer.server.getProfileCache().get(name)
                .map(com.mojang.authlib.GameProfile::getId).orElse(null));
        }
    }
}
