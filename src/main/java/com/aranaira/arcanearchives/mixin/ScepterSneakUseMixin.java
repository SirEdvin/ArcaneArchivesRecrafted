package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.StorageScepterItem;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Restore the item bypass at the native block-use gate, not player sneak state. */
@Mixin(net.minecraft.server.level.ServerPlayerGameMode.class)
public abstract class ScepterSneakUseMixin {
    @Redirect(method = "useItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
    private boolean arcanearchives$scepterSneakUse(ServerPlayer player) {
        return player.isSecondaryUseActive()
            && !StorageScepterItem.handsBypassSneakUse(player.getMainHandItem(), player.getOffhandItem());
    }
}
//?}
