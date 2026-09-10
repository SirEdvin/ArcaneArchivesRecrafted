package com.aranaira.arcanearchives.mixin;

//? if fabric {
import com.aranaira.arcanearchives.items.StorageScepterItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Restore the item bypass at the native block-use gate, not player sneak state. */
@Mixin(net.minecraft.client.multiplayer.MultiPlayerGameMode.class)
public abstract class ScepterSneakUseClientMixin {
    @Redirect(method = "performUseItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"))
    private boolean arcanearchives$scepterSneakUse(LocalPlayer player) {
        return player.isSecondaryUseActive()
            && !StorageScepterItem.handsBypassSneakUse(player.getMainHandItem(), player.getOffhandItem());
    }
}
//?}
