package com.aranaira.arcanearchives.mixin;

import com.aranaira.arcanearchives.items.MindspindleItem;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class MindspindlePickupMixin {
    @Shadow public int value;
    @Unique private int arcanearchives$originalValue;
    @Unique private boolean arcanearchives$amplified;

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private void arcanearchives$amplifyPickup(Player player, CallbackInfo callback) {
        arcanearchives$originalValue = value;
        value = MindspindleItem.amplifyPickup(player, value);
        arcanearchives$amplified = value != arcanearchives$originalValue;
    }

    @Inject(method = "playerTouch", at = @At("RETURN"))
    private void arcanearchives$restoreRemainingPickups(Player player, CallbackInfo callback) {
        // Modern orbs may contain several pickups; never compound the remaining units.
        if (arcanearchives$amplified) {
            value = arcanearchives$originalValue;
            arcanearchives$amplified = false;
        }
    }
}
